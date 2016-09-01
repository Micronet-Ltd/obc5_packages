#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>

#include <unistd.h>

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
#include <errno.h>

#include <sys/select.h>

#include <termios.h>
#include <fcntl.h>
#include <sys/ioctl.h>

#include "frame.h"

struct context_s
{
	int fd;
	frame_t frame;

	bool running;
};


struct termios saved_termios;
int have_saved_termios = 0;

void setup_tty(int fd)
{
	struct termios tio = {};

	if(tcgetattr(fd, &saved_termios))
	{
		perror("tcgetattr");
	}

	have_saved_termios = 1;

	tio = saved_termios;


	tio.c_iflag |= IGNBRK | IGNPAR;
	tio.c_iflag &= ~(BRKINT | ICRNL | INPCK | ISTRIP | IXON);
	tio.c_oflag = 0;
	//tio.c_oflag &= ~(OPOST);
	tio.c_lflag = 0;
	tio.c_cc[VERASE] = 0;
	tio.c_cc[VKILL] = 0;
	tio.c_cc[VMIN] = 0;
	tio.c_cc[VTIME] = 0;
	//tio.c_cflag |= (CS8);
	//tio.c_lflag &= ~(ECHO | ICANON | IEXTEN | ISIG);

	if(tcsetattr(fd, TCSANOW, &tio))
	{
		perror("tcsetattr");
	}
}

int open_serial(const char * name)
{
	int fd;
	struct termios tio = {};

	if(0 > (fd = open(name, O_RDWR, O_NOCTTY | O_NDELAY)))
	{
		return -1;
	}

	fcntl(fd, F_SETFL, O_NDELAY);

	ioctl(fd, TIOCSCTTY, (void*)1);
	fcntl(fd, F_SETFL, 0);
	tcgetattr(fd, &tio);

	//fprintf(stderr, "c_iflag = %08x\n", tio.c_iflag);
	//fprintf(stderr, "c_oflag = %08x\n", tio.c_oflag);
	//fprintf(stderr, "c_lflag = %08x\n", tio.c_lflag);

	// TODO: ACM does not have speed
	cfsetispeed(&tio, B57600);
	cfsetospeed(&tio, B57600);
	tio.c_cflag |= (CLOCAL | CREAD);
	tio.c_cflag &= ~PARENB;
	tio.c_cflag &= ~CSTOPB;
	tio.c_cflag &= ~CSIZE;
	tio.c_cflag |= CS8;

	tio.c_cflag &= ~CRTSCTS;
	tio.c_iflag &= ~(IXON | IXOFF | IXANY);

	tio.c_lflag &= ~(ECHO | ECHOE);
	tio.c_oflag &= ~OPOST;
	tcsetattr(fd, TCSANOW, &tio);

	setup_tty(fd);

	return fd;
}

#define FRAC_2d1	5000
#define FRAC_2d2	2500
#define FRAC_2d3	1250
#define FRAC_2d4	625
#define FRAC_2d5	313
#define FRAC_2d6	156
#define FRAC_2d7	78
#define FRAC_2d8	39
#define FRAC_2d9	20
#define FRAC_2d10	10
#define FRAC_2d11	5
#define FRAC_2d12	2

float get_fraction(short val)
{
	int fraction = 0;
	const float SCALE = 10000;

	if((val & 0x8000) == 0x8000) fraction += FRAC_2d1;
	if((val & 0x4000) == 0x4000) fraction += FRAC_2d2;
	if((val & 0x2000) == 0x2000) fraction += FRAC_2d3;
	if((val & 0x1000) == 0x1000) fraction += FRAC_2d4;

	if((val & 0x0800) == 0x0800) fraction += FRAC_2d5;
	if((val & 0x0400) == 0x0400) fraction += FRAC_2d6;
	if((val & 0x0200) == 0x0200) fraction += FRAC_2d7;
	if((val & 0x0100) == 0x0100) fraction += FRAC_2d8;

	if((val & 0x0080) == 0x0080) fraction += FRAC_2d9;
	if((val & 0x0040) == 0x0040) fraction += FRAC_2d10;

	return ((float) fraction / SCALE);
}

float get_g_val(uint32_t val)
{
	int hi_byte = 0;
	short temp = 0;
	float gVal = 0.0;

	hi_byte = ((val&0xfffc) & 0xff00) >> 8;
	temp = val;
	if(hi_byte > 0x7f)
	{
		temp = (~temp & 0xffff)+1;
		hi_byte = (temp & 0xff00) >> 8;
		gVal = (hi_byte & 0x70) >> 4;
		gVal += get_fraction(temp << 4);
		gVal *= -1;
	}
	else
	{
		gVal = (hi_byte & 0x70) >> 4;
		gVal += get_fraction(temp << 4);
	}

	return gVal;
}

static int process_payload(struct context_s * context, const uint8_t * data, size_t len)
{
	int i;

	/*
	for(i = 0; i < len; ++i)
	{
		printf("%02x ", data[i]);
	}
	printf("\n");
	*/

	for(i = 0; i < 8; i++)
		printf("%02x", data[i]);
	printf(" ");
	static uint64_t last_ts = 0;
	uint64_t ts;

	memcpy(&ts, data, sizeof(ts));
	if(last_ts)
		printf("%ld ", ts - last_ts);
	else
		printf("%ld ", ts);
	last_ts = ts;
	//printf("\n");

	{
		float x;
		float y;
		float z;

		int32_t xdata = 0;
		int32_t ydata = 0;
		int32_t zdata = 0;


		for(i = 0; i < 10; i++)
		{
			xdata = (data[8 + (i*6)+0] << 8) | data[8 + (i*6) + 1];
			ydata = (data[8 + (i*6)+2] << 8) | data[8 + (i*6) + 3];
			zdata = (data[8 + (i*6)+4] << 8) | data[8 + (i*6) + 5];
			x = get_g_val(xdata);
			y = get_g_val(ydata);
			z = get_g_val(zdata);

			printf("%0.3f %0.3f %0.3f, ", x, y, z);
		}
		printf("\n");



	}



	return 0;
}

static int accel_read(struct context_s * context)
{
	ssize_t bytes_read;
	int offset;
	uint8_t readbuffer[1024];

	bytes_read = read(context->fd, readbuffer, sizeof(readbuffer));
	
	if(bytes_read < 0)
	{
		if(EAGAIN != errno)
			return 0;
		perror("read");
		abort();
	}
	if(0 == bytes_read)
	{
		return -1;
	}

	offset = 0;
	offset = frame_process_buffer(&context->frame, readbuffer + offset, bytes_read - offset);
	if(offset <= 0)
	{
		abort();
	}

	if(frame_data_ready(&context->frame))
	{
		int status;
		if(context->frame.data_len != 68)
			printf("Wrong size %d\n", (int)context->frame.data_len);
		status = process_payload(context, context->frame.data, context->frame.data_len);
		if(status < 0)
			fprintf(stderr, "status = %d\n", status );

		frame_reset(&context->frame);
	}

	return 0;
}


static int usbio_wait(struct context_s * context)
{
	int r;

	fd_set fds;

	do
	{
		FD_ZERO(&fds);
		FD_SET(context->fd, &fds);
		r = select(context->fd+1, &fds, NULL, NULL, NULL);
		if(r == -1)
		{
			perror("select");
			close(context->fd);
			context->fd = -1;
			return -1;
		}
	} while(EINTR == errno);

	if(r>0)
	{
		if(r != 1)
			fprintf(stderr, "select returned %d\n", r);

		if(FD_ISSET(context->fd, &fds))
			return 0;
		fprintf(stderr, "select returned but no fd\n");
	}
	return -1;
}

		



static int accel_loop(struct context_s * context)
{

	int status;

	fprintf(stderr, "accel_loop\n");

	do
	{

		if(context->fd < 0)
		{
			fprintf(stderr, "no dev\n");
			if(0 > (context->fd = open_serial("/dev/ttyMICRONET_ACCEL")) )
			{
				fprintf(stderr, "failed to open serial\n");
				sleep(1);
				continue;
			}
			fprintf(stderr, "opend fd = %d\n", context->fd);
		}

		////printf("wait:\n");
		status = usbio_wait(context);
		//fprintf(stderr, "wait done\n");
		if(status < 0)
		{
			fprintf(stderr, "%s:%d %s: usbio_wait returned %d\n", __FILE__, __LINE__, __func__, status);
			break;
		}

		status = accel_read(context);

	} while(context->running);




	return 0;
}


int main(int argc, char * argv[])
{
	struct context_s context = {};
	
	uint8_t buffer[1024];
	context.running = true;
	context.fd = -1;
	frame_setbuffer(&context.frame, buffer, sizeof(buffer));

	accel_loop(&context);

	return EXIT_SUCCESS;
}
