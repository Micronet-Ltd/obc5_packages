#define LOG_TAG "Canbus"

#include "canbus.h"
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

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define ERR(...) LOGE(__VA_ARGS__)

#define  DD(...)   LOGD(__VA_ARGS__)

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

int setTermination(int fd, bool enable) {
    char buf[256];
    sprintf(buf, "O%d\r", (enable ? 1 : 0));
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
    sprintf(buf, "T%s\r", message);
    printf("Send %s\n", buf);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf );
        return -1;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(
        JNIEnv *env, jobject obj) {

    int fd;
    char *tty;
    DD("opening port: '%s'\n", CAN1_TTY);

    if ((fd = open(CAN1_TTY, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
        perror(tty);
        exit(EXIT_FAILURE);
    }

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

    if(setTermination(fd, true) == -1) {
        return -1;
    }

    const char * mesg = NULL;
    if(sendMessage(fd, mesg) == -1) {
        return -1;
    }
}