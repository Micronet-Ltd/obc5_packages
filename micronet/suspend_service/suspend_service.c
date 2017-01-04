#include <stdio.h>

#define DEBUG_TRACE
#include <stdlib.h>
#include <unistd.h>
#include <libgen.h>
#include <limits.h>

#include <sys/un.h>
#include <sys/socket.h>
#include <sys/uio.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <stdbool.h>
#include <stdint.h>
#include <inttypes.h>

#include <pthread.h>
#include <sched.h>

#include <string.h>
#include <strings.h>
#include <errno.h>

#include <sys/select.h>

#include <termios.h>
#include <sys/inotify.h>
#include "log.h"

//#define EVENT_SIZE      (sizeof(struct inotify_event))
//#define EVENT_BUF_LEN   (4 * (EVENT_SIZE + 16))
#define RESET_SLEEP_TIMEOUT 1

#define MIN_SUSPEND_TM  10000
#define MAX_SUSPEND_TM  0x7FFFFFFF

const char* fname = "/dev/suspend_timeout";
const char* db_fname = "/data/data/com.android.providers.settings/databases/settings.db";
const char* tmp_fname = "/sys/devices/suspend_timeout/suspend_timeout_tmp";
//const char* val_fname = "/sys/devices/suspend_timeout/suspend_timeout_value";

const char* db_put = "settings put system screen_off_timeout";
const char* db_get = "settings get system screen_off_timeout";
const char* db_put_st = "settings put system sleep_timeout";
const char* db_get_st = "settings get system sleep_timeout";

struct suspend_tm_thcontext {
        int     fd_dev;
};
int get_settings(char* buf, const char* cmd, const char* tmp_file)
{
    int fd_tmp, ret, length;
    sprintf(buf, "%s > %s", cmd, tmp_file);
    ret = system(buf);
    if(0 > ret) {
        DERR ("system call failed, %s", strerror(errno));
        return -1;
    }

    fd_tmp = open(tmp_file, O_RDWR, O_NDELAY);
    if (0 > fd_tmp) {
        DERR ("cannot open file %s err %s", tmp_fname, strerror(errno));
        return -1;
    }
    length = read(fd_tmp, buf, (sizeof(uint32_t) + 1) * 2);
    DTRACE("read len %d:: %s", length, buf);
    close(fd_tmp);

    return length;
}
int test_timeout(const char* buf)
{
    uint32_t val = strtol(buf, 0, 10);
    if((-1 != (int32_t)val) && (MIN_SUSPEND_TM > val || MAX_SUSPEND_TM < val) )
    {
        DERR ("value is not valid %d (%s)", val, buf);
        return 0;
    }
    return 1;
}
bool file_exists(const char * filename)
{
    int s;
    struct stat st;

    s = stat(filename, &st);
    if(0 == s)
	return true;
    return false;
}

void * db_proc(void * cntx)
{
    struct suspend_tm_thcontext* ptc = (struct suspend_tm_thcontext*)cntx;
    int fd_db, wd, length, i, found = 0;
    uint32_t last_val = 0, tmp_val = 0;
    char cmd_buf[256];

    DINFO("db_proc starts");

    if (!file_exists(db_fname)) {
        DERR ("db %s is not exists", db_fname);
        return NULL;
    }

    fd_db = inotify_init();
    if (0 > fd_db) {
        DERR ("inotify_init err %s", strerror(errno));
        return NULL;
    }
    wd = inotify_add_watch(fd_db, db_fname, IN_MODIFY);

    found = 1;//for the 1st init read
    while(1) 
    {
        if(found) 
        {
            memset(cmd_buf, sizeof(cmd_buf), 0);
            length = get_settings(cmd_buf, db_get, tmp_fname);
            if(0 < length) 
            {
                tmp_val = strtol(cmd_buf, 0, 10);
                DINFO("tmp_val %d; last_val %d", tmp_val, last_val);
                if(0 != tmp_val && tmp_val != last_val) 
                {
                    length = write(ptc->fd_dev, cmd_buf, length); 
                    if (0 < length) 
                    {
                        last_val = tmp_val; 
                    }
                }
            }
            //
#if     RESET_SLEEP_TIMEOUT
            length = get_settings(cmd_buf, db_get_st, tmp_fname);
            if(0 < length) 
            {
                if(-1 != strtol(cmd_buf, 0, 10)) 
                {
                   sprintf(cmd_buf, "%s %d", db_put_st, -1);
                   system(cmd_buf);
                }
            }
#endif
        }
        found = 0;
        length = read(fd_db, cmd_buf, sizeof(cmd_buf));
        if (0 < length) 
        {
            struct inotify_event* event = (struct inotify_event*)&cmd_buf[0];

            for(i = 0; i < length; i += sizeof(struct inotify_event) + event->len) 
            {
                event = (struct inotify_event*)&cmd_buf[i];
                DTRACE("mask = %d; len = %d", event->mask, event->len);

                if(event->mask & IN_MODIFY) 
                {
                    found = 1;
                    break;
                }                
            }
        }
    }

    inotify_rm_watch(fd_db, wd);
    close(fd_db);

    return NULL;
}

int main(int argc __attribute__((unused)), char * argv[] __attribute__((unused)))
{
    int    ret;
    fd_set set;
    struct timeval timeout;
    pthread_t db_thread;
    char readbuffer[32];
    char outbuffer[64];
    struct suspend_tm_thcontext tc;

    DINFO("started.");

    tc.fd_dev = open(fname, O_RDWR, O_NDELAY);
    if(0 > tc.fd_dev)
    {
   	DERR ("error open device");//DERR ("error open device %s", fname);
	return -1;
    }

    pthread_create(&db_thread, NULL, db_proc, &tc);

    while(1)
    {
        timeout.tv_sec = 5;
        timeout.tv_usec = 0;
        FD_ZERO (&set);
        FD_SET (tc.fd_dev, &set);

        ret = select(FD_SETSIZE, &set, NULL, NULL, &timeout);
    	if (ret != 1)
    	{
            DTRACE ("select error size %d", ret);
            continue;
    	}
	ret = read(tc.fd_dev, readbuffer, sizeof(readbuffer)/sizeof(readbuffer[0]));
        if (ret < 1) 
        {
            DTRACE ("read error size %d", ret);
            continue;
        }
#if SUSP_DEBUG
	{
            DINFO ("read size %d", ret);
            int cnt;
            for (cnt = 0; cnt < ret; cnt++) {
                DINFO("Byte [%d] -- %c", cnt, readbuffer[cnt] );
            }
	}
#endif
        if(0 == test_timeout(readbuffer)) 
        {
            continue;
        }

        sprintf(outbuffer, "%s %s\n", db_put, readbuffer); 
        system(outbuffer);
///temp!!!
//        memset(cmd_buf, 0, sizeof(cmd_buf)/sizeof(cmd_buf[0]));    
//        ret = get_settings(cmd_buf, db_get, tmp_fname);
//        if(0 < ret) 
//        {
//            int32_t tmp_val = strtol(cmd_buf, 0, 10);
//            DINFO("val %d", tmp_val);
//            //always write for the end of operation
//            ret = write(tc.fd_dev, cmd_buf, ret); 
//        }
///////
        DINFO("%s", outbuffer);
    }

    DINFO("exit.");
    return 0;
}
