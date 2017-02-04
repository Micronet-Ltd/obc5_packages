#define LOG_TAG "Canbus"

#include "canbus.h"
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <getopt.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <termios.h>
#include <linux/tty.h>
#include <android/log.h>
#include <pthread.h>
#include <sys/prctl.h>
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define ERR(...) LOGE(__VA_ARGS__)

#define  DD(...)   LOGD(__VA_ARGS__)

#define CAN1_TTY    "/dev/ttyACM2"
#define CAN2_TTY    "/dev/ttyACM3"
#define J1708_TTY   "/dev/ttyACM4"
static pthread_t thread;
static int fd=-1; //File Descriptor (Handle)

int initTerminalInterface(int fd) {
    if (isatty(fd)) {
        struct termios ios;

        tcgetattr(fd, &ios);
        bzero(&ios, sizeof(ios));

        cfmakeraw(&ios);
        cfsetospeed(&ios, B115200);
        cfsetispeed(&ios, B115200);
        ios.c_cflag = (B115200 | CS8 | CLOCAL | CREAD) & ~(CRTSCTS | CSTOPB | PARENB);
        ios.c_iflag = 0;
        ios.c_oflag = 0;
        ios.c_lflag = 0;        /* disable ECHO, ICANON, etc... */
        ios.c_cc[VTIME] = 10;   /* unit: 1/10 second. */
        ios.c_cc[VMIN] = 1;     /* minimal characters for reading */

        tcsetattr(fd, TCSANOW, &ios);
        tcflush(fd, TCIOFLUSH);
    } else {
        ERR("Error isatty(fd): invalid terminal %s\n", strerror(errno));
        close(fd);
        exit(EXIT_FAILURE);
    }

    return 0;
}
int wait_for_data()
{
    int r;
    fd_set fds;

    // use a timeout to prevent thread from staying alive after interface is closed
    timeval delay = {0, 500000};

    do {
        FD_ZERO(&fds);
        FD_SET(fd, &fds);
        r= select(fd+1, &fds, NULL, NULL, &delay);
    } while (-1 == r && errno == EINTR);

    if(-1 == r)
    {
        LOGE("%s:%d select: %s\n", __FILE__, __LINE__, strerror(errno));
        return -1;
    }
    else if(r > 0)
    {
        if(r != 1)
            LOGE("select did not return 1, returned %d\n", r);

        if(FD_ISSET(fd, &fds))
        {
            return 0;
        }
        else
            LOGE("select returned, bu no fd, r = %d\n", r);

    }
    else { // don't process when r==0 to reduce overhead
    }

    return -1;
}

int closeCAN(int fd) {
// first always close the CAN module (bug #250)
    // http://192.168.1.234/redmine/issues/250
    char buf[256];
    sprintf(buf, "C\r");
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
        return -1;
    }
    return 0;
}

int setBitrate(int fd, int speed) {
    char buf[256];
    sprintf(buf, "C\rS%d\r", speed);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
        return -1;
    }
    return 0;
}

int setTermination(int fd, int term) {
    char buf[256];
    sprintf(buf, "O%d\r", term);
    if ( -1 == write(fd, buf, strlen(buf)) ) {
        ERR("Error write %s command\n", buf );
        return -1;
    }
    return 0;
}

int sendReadStatusCommand(int fd) {
    char buf[256];
    sprintf(buf, "F\r");
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
        return -1;
    }
    return 0;
}

int sendMessage(int fd, const char * message) {
    char buf[256];
    sprintf(buf, "t%s\r", message);
    printf("Send %s\n", buf);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf );
        return -1;
    }
    return 0;
}
static void *monitor_data_thread(void *param)
{
    uint8_t data[8*1024];
    uint8_t * p = data;

    prctl(PR_SET_NAME, "monitor_thread", 0, 0, 0);
    LOGD("monitor_thread started");
    LOGD("thread=%d", thread);
    int quit=0;
    while(!quit)
    {
        // sanity check to kill stale read thread
       /* if(thread != pthread_self()) {
            LOGD("read thread stale, thread=%d, pthread_self=%d", thread, pthread_self());
            break;
        }*/
        if(!wait_for_data())
        {
            int r;
            uint8_t * pend = NULL;

            r = read(fd, p, sizeof(data) - (p - data));

            if(0 == r)
            {
                quit = true;
                LOGD("quit1=%d", quit);
                break;
            }

            if(-1 == r)
            {
                if(EAGAIN == errno)
                    continue;
                LOGE("%s:%d read: %s\n", __func__, __LINE__, strerror(errno));
                abort();
            }

//            pend = p + r;
//
//            if(data[0] != 0x02)
//            {
//                uint8_t * sof;
//                LOGD("missing SOF\n");
//                sof = (uint8_t*)memchr(data, 0x02, pend - data);
//                if(sof)
//                {
//                    LOGD("Found offset\n");
//                    memmove(data, sof, pend - sof);
//                    pend = data + (pend-sof);  // todo: test this is correct
//                }
//                else
//                {
//                    p = data;
//                    continue;
//                }
//            }
//
//            if(pend - data < 6)
//            {
//                LOGD("not min size\n");
//                p = pend;
//                continue;
//            }
//
//            if(pend - data < data[1])
//            {
//                //LOGD("not full frame\n");
//                p = pend;
//                continue;
//            }
//
//            uint8_t * start;
//
//
//            start = process_buffer(data, pend);
//
//
//            if(start == data)
//            {
//                LOGD("start == data\n");
//                p = pend;
//                continue;
//            }
//            else
//            {
//                if( start == pend)
//                {
//                    p = data;
//                    continue;
//                }
//                else
//                {
//                    //LOGD("trailing data\n");
//                    memmove(data, start, pend-start);
//                    p = data + (pend - start);
//                }
//            }
        }
    }

//    LOGD("quit2=%d, thread=%d, pthread_self=%d", quit, thread, pthread_self());
//    LOGD("DetachCurrentThread");
//    g_canbus.g_vm->DetachCurrentThread();
    //LOGD("Must have received a Quit Event\n");
    return 0;
}
JNIEXPORT jint JNICALL
Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env,
                                                                      jobject instance) {

//    int fd;
    char *tty;
    DD("opening port: '%s'\n", CAN1_TTY);

    if ((fd = open(CAN1_TTY, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
        perror(tty);
        exit(EXIT_FAILURE);
    }

 //   int r = pthread_create(&thread, NULL, monitor_data_thread, 0);
    if (initTerminalInterface(fd) == -1) {
        return -1;
    }
    /* first always close the CAN module (bug #250)
     http://192.168.1.234/redmine/issues/250
     */
    if (closeCAN(fd) == -1) {
        return -1;
    }

    if(setBitrate(fd, 6) == -1) {
        return -1;
    }

    if(sendReadStatusCommand(fd) == -1) {
        return -1;
    }

    if(setTermination(fd, 1) == -1) {
        return -1;
    }

    const char * mesg = "7003112233";
    if(sendMessage(fd, mesg) == -1) {
        return -1;
    }

    return 0;

}
