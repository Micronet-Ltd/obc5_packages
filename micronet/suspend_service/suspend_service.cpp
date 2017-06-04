#include <stdio.h>

//#define DEBUG_TRACE
#include <stdlib.h>
#include <unistd.h>
#include <libgen.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdbool.h>
#include <stdint.h>
#include <inttypes.h>
#include <pthread.h>
#include <string.h>
#include <strings.h>
#include <errno.h>
#include <sys/inotify.h>

#include "log.h"
#include "AWakeLock.h"

//#define EVENT_SIZE      (sizeof(struct inotify_event))
//#define EVENT_BUF_LEN   (4 * (EVENT_SIZE + 16))
#define RESET_SLEEP_TIMEOUT         1 //set SLEEP_TIMEOUT to -1
#define PM_SCREEN_BRIGHT_WAKE_LOCK  0x0000000a
#define PM_FULL_WAKE_LOCK           0x0000001a
#define PM_ON_AFTER_RELEASE         0x20000000

#define MIN_SUSPEND_TM              10000
#define MAX_SUSPEND_TM              0x7FFFFFFF

const char* fname =         "/dev/suspend_timeout";
const char* ign_tm_fname =  "/dev/ignition_timeout";
const char* ign_fname =     "/dev/ignition_level";
const char* db_fname =      "/data/data/com.android.providers.settings/databases/settings.db";
//const char* tmp_fname = "/sys/devices/suspend_timeout/suspend_timeout_tmp";
const char* tmp_fname =     "/data/local/tmp/suspend_timeout_tmp";
const char* i_tm_tmp_fname = "/data/local/tmp/ignition_timeout_tmp";
//const char* val_fname = "/sys/devices/suspend_timeout/suspend_timeout";

const char* db_put =    "settings put system screen_off_timeout";
const char* db_get =    "settings get system screen_off_timeout";
const char* db_put_st = "settings put system sleep_timeout";
const char* db_get_st = "settings get system sleep_timeout";
//ignition
//echo 692 > sys/class/gpio/export
//cat sys/class/gpio/gpio692/value
//const char* igni_val = "cat sys/class/gpio/gpio692/value";

struct suspend_tm_thcontext {
    int     fd_sp_tm;
    int     fd_ign_tm;
    int     fd_ign_lvl;
    int     last_ign_lvl;
    int     wl_flags;
    sp<AWakeLock>   mWakeLock;
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
                    length = write(ptc->fd_sp_tm, cmd_buf, length); 
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

static int  WakeLockSet(suspend_tm_thcontext* ptc, int val)
{
    if(val != ptc->last_ign_lvl) 
    {
        ptc->last_ign_lvl = val;
        if(ptc->last_ign_lvl) 
        {
            DINFO("acquire %d", ptc->last_ign_lvl);//temp!!!
            if(ptc->mWakeLock->acquire(ptc->wl_flags, "suspend_service") == 0)
            {
                DERR("CANNOT acquire wakelock!");
                return -1;
            }
        }
        else
        {
            ptc->mWakeLock->release(1);//force
            DINFO("release %d", ptc->last_ign_lvl);//temp!!!!
        }
    }
    DINFO("ignition %d", ptc->last_ign_lvl);
    return 0;
}
int main(int argc __attribute__((unused)), char * argv[] __attribute__((unused)))
{
    int             ret, len, tmp_val;
    fd_set          set;
    int             max_fd = -1;
    struct timeval  timeout;
    pthread_t       db_thread;//, ignition_thread;
    char            readbuffer[32];
    char            outbuffer[64];
    struct suspend_tm_thcontext tc;

    //struct igni_thcontext ign_tc;

    DINFO("started.");
    tc.last_ign_lvl = -1;
    tc.wl_flags     = PM_FULL_WAKE_LOCK | PM_ON_AFTER_RELEASE;

    tc.fd_sp_tm = open(fname, O_RDWR, O_NDELAY);
    if(0 > tc.fd_sp_tm)
    {
   	DERR ("error open device %s", fname);
	return -1;
    }

    tc.fd_ign_lvl = open(ign_fname, O_RDWR, O_NDELAY);
    if(0 > tc.fd_ign_lvl)
    {
        DERR ("error open device %s", ign_fname);
        return -1;
    }

    tc.fd_ign_tm = open(ign_tm_fname, O_RDWR, O_NDELAY);
    if(0 > tc.fd_ign_tm)
    {
        DERR ("error open device %s", ign_fname);
        return -1;
    }
    tc.mWakeLock = new AWakeLock();
    if(tc.mWakeLock == NULL) 
    {
        DERR("CANNOT create wakelock!");
        return -1;
    }
    //1st wakelock
    len = read(tc.fd_ign_lvl, readbuffer, sizeof(readbuffer)/sizeof(readbuffer[0]));//1st time don't wait
    if(0 < len) 
    {
        tmp_val = strtol(readbuffer, 0, 10);
        WakeLockSet(&tc, tmp_val);
    }

    pthread_create(&db_thread, NULL, db_proc, &tc);

//    pthread_create(&ignition_thread, NULL, ignition_proc, &ign_tc);

    if(max_fd < tc.fd_sp_tm)
        max_fd = tc.fd_sp_tm;
    if(max_fd < tc.fd_ign_tm)
        max_fd = tc.fd_ign_tm;
    if(max_fd < tc.fd_ign_lvl)
        max_fd = tc.fd_ign_lvl;

    while(1)
    {
        timeout.tv_sec = 5;
        timeout.tv_usec = 0;
        FD_ZERO (&set);
        FD_SET (tc.fd_sp_tm, &set);
        FD_SET (tc.fd_ign_tm, &set);
        FD_SET (tc.fd_ign_lvl, &set);

        ret = select(max_fd + 1, &set, NULL, NULL, &timeout);
    	if (ret != 1)
    	{
            DTRACE ("select error size %d", ret);
            continue;
    	}
        if(FD_ISSET(tc.fd_sp_tm, &set)) 
        {
            len = read(tc.fd_sp_tm, readbuffer, sizeof(readbuffer)/sizeof(readbuffer[0]));
            if(len > 0) 
            {
                if(0 != test_timeout(readbuffer)) 
                {
                    sprintf(outbuffer, "%s %s\n", db_put, readbuffer); 
                    system(outbuffer);
                }

                DINFO("%s", outbuffer);
            }
        }
        if(FD_ISSET(tc.fd_ign_tm, &set)) 
        {
            len = read(tc.fd_ign_tm, readbuffer, sizeof(readbuffer)/sizeof(readbuffer[0]));
            if(len > 0) 
            {
                //if(0 != test_timeout(readbuffer)) 
                //{
                //}

                DINFO("%s", readbuffer);
            }
        }
        if(FD_ISSET(tc.fd_ign_lvl, &set)) 
        {
            len = read(tc.fd_ign_lvl, readbuffer, sizeof(readbuffer)/sizeof(readbuffer[0]));//1st time don't wait

            if(0 < len) 
            {
                tmp_val = strtol(readbuffer, 0, 10);
                WakeLockSet(&tc, tmp_val);
            }
        }
    }
    DINFO("exit.");
    return 0;
}
