//
// Created by brigham.diaz on 1/27/2017.
//

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

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env, jobject obj)
{

    int fd;
    char *tty;
    if ((fd = open (tty, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
        perror(tty);
        exit(1);
    }

    if (isatty(fd)) {
        struct termios  ios;

        tcgetattr(fd, &ios);

        bzero(&ios, sizeof(ios));

        cfmakeraw(&ios);
        //ios.c_iflag &= ~(IGNBRK | BRKINT | PARMRK | ISTRIP | INLCR | IGNCR | ICRNL | IXON);
        //ios.c_oflag &= ~OPOST;
        //ios.c_lflag &= ~(ECHO | ECHONL | ICANON | ISIG | IEXTEN);
        //ios.c_cflag &= ~(CSIZE | PARENB);
        //ios.c_cflag |= CS8;

        cfsetospeed(&ios, B115200);
        cfsetispeed(&ios, B115200);
        ios.c_cflag =  (B115200 | CS8 | CLOCAL | CREAD) & ~(CRTSCTS | CSTOPB | PARENB);
        ios.c_iflag = 0;
        ios.c_oflag = 0;
        ios.c_lflag = 0;        /* disable ECHO, ICANON, etc... */
        ios.c_cc[VTIME] = 10;   /* unit: 1/10 second. */
        ios.c_cc[VMIN] = 1;     /* minimal characters for reading */

        tcsetattr( fd, TCSANOW, &ios );
        tcflush(fd, TCIOFLUSH);
    } else {
        printf("Error isatty(fd): invalid terminal %s\n", strerror(errno));
        close(fd);
        exit(1);
    }

    return -1;
}