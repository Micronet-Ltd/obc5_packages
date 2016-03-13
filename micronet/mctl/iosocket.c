#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>

#include <unistd.h>

#include <sys/uio.h>

#include <sys/un.h>
#include <sys/socket.h>
#include <ctype.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define DEBUG_TRACE

#include <stdbool.h>
#include <stdint.h>
#include <inttypes.h>

#include <pthread.h>
#include <sched.h>

#include <string.h>
#include <errno.h>

#include <sys/select.h>

#include <termios.h>
#include <fcntl.h>
#include <sys/ioctl.h>

#include "util.h"
#include "iosocket.h"



int iosocket_connect()
{
	struct sockaddr_un c_addr = {0};
	int fd;

	fd = socket(AF_UNIX, SOCK_DGRAM, 0);
	if(-1 == fd)
	{
		perror("socket");
		exit(-1);
	}


	c_addr.sun_family = AF_UNIX;
	snprintf(c_addr.sun_path,
			sizeof(c_addr.sun_path),
			UD_FILENAME "_c.%ld",
			(long)getpid()); // TODO: thread id instead of pid if multi threaded!!
	c_addr.sun_path[0] = '\0'; // abstract socket namespace


	if(-1 == bind(fd, (struct sockaddr *)&c_addr, sizeof(struct sockaddr_un)))
	{
		perror("bind");
		exit(-1);
	}

	return fd;
}
void iosocket_disconnect(int * fd)
{
	close(*fd);

}

int iosocket_sendmsg(int * fd, uint8_t * data, size_t len)
{
	struct sockaddr_un s_addr = {0};

	s_addr.sun_family = AF_UNIX;
	strncpy(s_addr.sun_path, UD_FILENAME, sizeof(s_addr.sun_path) - 1);
	s_addr.sun_path[0] = '\0';


	if(-1 == sendto(*fd, data, len, 0, (struct sockaddr *)&s_addr, sizeof(struct sockaddr_un)))
	{
		perror("sendto");
		return -1;
	}
	return 0;
}


int iosocket_recvmsg(int * fd, uint8_t * data, size_t len)
{
	int num_bytes;

	num_bytes = recvfrom(*fd, data, len, 0, NULL, NULL);
	if(-1 == num_bytes)
	{
		perror("recvfrom");
		return -1;
	}
	return num_bytes;
}
