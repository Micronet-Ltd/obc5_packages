/** Anthony Best <anthony.best@micronet-inc.com>
 *  Userspace driver for OBC MCU
 */
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include <limits.h>

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

#include "misc.h"
#include "queue.h"
#include "frame.h"
/// structs and defines

// temp trace
#define DTRACE(x) do { fprintf(stderr, "%s:%d %s(): %s\n", __FILE__, __LINE__, __func__, x); } while(0)
//#define DTRACE(x) do {  } while(0)


bool file_exists(const char * filename)
{
	int s;
	struct stat st;

	s = stat(filename, &st);
	if(0 == s)
	{
		if(S_ISREG(st.st_mode) )
			return true;
	}
	return false;

}

void log_hex(void * data, size_t len)
{
	uint8_t *p = data;
	
	while(len--)
		printf("%02x ", *p++);
	printf("\n");
}

struct usb_thread_context
{
	//struct usb_msg_queue queue[QUEUE_SIZE];
	bool running;
	int fd;
	int gpio_fd;
	frame_t frame; // TODO: refactor name?
};

struct accel_thread_context
{
	char name[PATH_MAX];
};


/// functions

// returns < 0 on error, 0 on io, others undefined
static int usbio_wait(struct usb_thread_context * context)
{
	int r;
	fd_set fds;
	fprintf(stderr, "%s:%d %s()\n", __FILE__, __LINE__, __func__);

	// TODO: this may need to handle other conditions, it may need to be refactored

	do
	{
		FD_ZERO(&fds);
		FD_SET(context->fd, &fds);
		r = select(context->fd+1, &fds, NULL, NULL, NULL);

	} while(-1 == r && EINTR == errno);

	if(r == -1)
	{
		perror("select");
		return -1;
	}

	if( r > 0)
	{
		if(r != 1)
		{
			DTRACE("select did not return 1");
			fprintf(stderr, "select returned %d\n", r);
		}
		if(FD_ISSET(context->fd, &fds))
			return 0;
		DTRACE("select returned but no fd");
	}
	return -1;
}

static int usbio_gpio_input(struct usb_thread_context * context, uint8_t mask, uint8_t value)
{
	if(context->gpio_fd >= 0)
	{
		int r;
		uint8_t data[4] = {0, 0, 0, 0};
		data[2] = mask;
		data[3] = value;
		// The driver should never block, or return -EAGAIN, if the driver changes
		// this will need to be updated. NOTE: this can not block, so take care
		r = write(context->gpio_fd, data, sizeof(data));
		if(r != sizeof(data))
		{
			DTRACE("write error");
			if(-1 == r && -EAGAIN == errno)
				DTRACE("EAGAIN should not happen");
			else if(-1 == r && -EACCES == errno)
				DTRACE("EACCES memory is likely corrupted");
			else if(-1 == r && -EINVAL == errno)
				DTRACE("EINVAL data is invalid");
			else if(-1 == r)
			{
				DTRACE("other error, check logs");
				perror("write");
				fprintf(stderr, "%s:%d: write: %s\n", __FILE__, __LINE__, strerror(errno));
				return r;
			}
			DTRACE("wrong value for write");
			fprintf(stderr, "%s:%d: r = %d\n", __FILE__, __LINE__, r);
		}
	}
	return 0;
}

static int usbio_frame_receive(struct usb_thread_context * context, const uint8_t * data, size_t len)
{
	// NOTE: Do not block here.
	// TODO: check for sequence gaps
	//int seq = data[1]; // TODO:
	int packet_type;

	if(len < 1)
		return 0;

	packet_type = data[0];

	// TODO: handle messages
	switch (packet_type)
	{
		case 0:	// Sync/Info
			break;

		case 1: // Write register
			break;

		case 2: // Register read request
			break;

		case 3: // register read response
			break;

		case 4: // RTC Write
			break;

		case 5: // RTC read request
			break;

		case 6: // RTC read response
			break;

		case 7: // PING request
			break;

		case 8: // PING response
			break;

		case 9: // GPIO Interrupt
			usbio_gpio_input(context, data[2], data[3]);
			break;
	}
	return 0;
}

static int usbio_receive(struct usb_thread_context * context)
{
	ssize_t bytes_read; // NOTE signed type
	int offset;
	uint8_t readbuffer[1024];
	fprintf(stderr, "%s:%d %s()\n", __FILE__, __LINE__, __func__);

	bytes_read = read(context->fd, readbuffer, sizeof(readbuffer));
	if(bytes_read < 0)
	{
		if(EAGAIN == errno)
			return 0; //
		perror("read");
		abort();
	}
	if(0 == bytes_read)
	{
		DTRACE("port closed");
		return -1;
	}

	offset = 0;
	// NOTE: bytes_read and offset are signed types
	// bytes_read and offset must be positive
	while(bytes_read - offset > 0)
	{
		offset += frame_process_buffer(&context->frame, readbuffer + offset, bytes_read - offset);
		if(offset <= 0)
		{
			DTRACE("offset is <= 0");
			abort();
		}
		if(frame_data_ready(&context->frame))
		{
			int status;
			//process data
			status = usbio_frame_receive(context, context->frame.data, context->frame.data_len);
			if(status)
				DTRACE("status != 0");
			frame_reset(&context->frame);
		}
	}

	DTRACE("TODO: Read message from MCU");
	DTRACE("TODO: Handle messages");
	DTRACE("TODO: Check for more messages");

	return 0;
}

void * usb_proc(void * cntx)
{
	struct usb_thread_context * context = cntx;
	int status;
	context->running = true;

	//int cnt = 0;


	do
	{
		DTRACE("Main loop");
		// TODO: ifthe USB goes into an error state we may want to retry until USB is reconnected

		// TODO: we may have to wait for some events to happen, so a state machine may need to be implemented here

		// TODO: Waiting for events
		status = usbio_wait(context);
		if(status < 0)
		{
			fprintf(stderr, "%s:%d %s: usbio_wait returned %d\n", __FILE__, __LINE__, __func__, status);
			context->running = false;
			break;
		}
		DTRACE("After Wait");

		// TODO: actual work for this thread
		// if pending receive, this could by async instead, if serial this will be here
		status = usbio_receive(context);
		if(status < 0)
		{
			fprintf(stderr, "%s:%d %s: usbio_receive returned %d\n", __FILE__, __LINE__, __func__, status);
			context->running = false;
			break;
		}
		DTRACE("After recv");


	} while(context->running);


	fprintf(stderr, "usb thread exiting\n");
	return NULL;
}

// TODO: handle restore, and multiple devices...
struct termios saved_termios;
int have_saved_termios = 0;

void setup_tty(int fd)
{
	struct termios tio = {0};

	if(tcgetattr(fd, &saved_termios))
	{
		DTRACE("tcgetattr failed");
		perror("tcgetattr");
	}

	have_saved_termios = 1;

	tio = saved_termios;

	tio.c_iflag |= IGNBRK | ISTRIP | IGNPAR;
	tio.c_iflag &= ~ISTRIP;
	tio.c_oflag = 0;
	tio.c_lflag = 0;
	tio.c_cc[VERASE] = 0;
	tio.c_cc[VKILL] = 0;
	tio.c_cc[VMIN] = 0;
	tio.c_cc[VTIME] = 0;

	if(tcsetattr(fd, TCSANOW, &tio))
	{
		DTRACE("tcsetattr error");
		perror("tcsetattr");
	}
}

int open_serial(const char * name)
{
	int fd;
	struct termios tio = {0};

	if( 0 > (fd = open(name, O_RDWR, O_NOCTTY | O_NDELAY)))
	{
		DTRACE("open error");
		perror("open");
		abort();
	}

	fcntl(fd, F_SETFL, O_NDELAY);

	ioctl(fd, TIOCSCTTY, (void*)1);
	fcntl(fd, F_SETFL, 0);
	tcgetattr(fd, &tio);
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

void * accel_proc(void * cntx)
{
	struct accel_thread_context * context = cntx;
	int r;
	int fd_mcu;
	int fd_dev;
	frame_t frame;
	uint8_t databuffer[1024]; // >= 10 * (8*2)
	uint8_t readbuffer[1024]; // >= 10 * (8 * 2) * 2 + 2 + slop ( * 2 + XX)

	frame_setbuffer(&frame, databuffer, sizeof(databuffer));


	// TODO: make config
	// Accelerometer port
	fd_mcu = open_serial(context->name);
	fd_dev = open("/dev/vaccel", O_RDWR, O_NDELAY);


	do
	{
		do
		{
			fd_set fds;
			FD_ZERO(&fds);
			FD_SET(fd_mcu, &fds);
			r = select(fd_mcu + 1, &fds, NULL, NULL, NULL);
		} while(r == -1 && EINTR == errno);

		if(1 != r)
		{
			if(-1 == r)
				perror("select");
			else
				DTRACE("select did not return 1");
			abort();
		}

		r = read(fd_mcu, readbuffer, sizeof(readbuffer));
		if( r < 0)
		{
			if(EAGAIN == errno)
				continue;
			perror("read");
			abort();
		}
		if(0 == r)
		{
			DTRACE("port closed");
			abort(); // TODO: keep retying
		}

		int offset;

		offset = 0;
		// TODO: verifiy signedness bugs
		while(r - offset > 0)
		{
			offset += frame_process_buffer(&frame, readbuffer + offset, r - offset);
			if(offset <= 0)
			{
				DTRACE("offset is <= 0");
				abort();
			}
			if(frame_data_ready(&frame))
			{
				if(frame.data_len == 68)
				{
					int w;
					uint8_t data[10*16];
					int i;
					uint8_t ts_data[8];
					/*
					 *  Accel data input format:
					 *   Header:
					 *     <u64> timestamp  
					 *   Data samples * 10
					 *     <u16> <u16> <u16> x,y,z sample
					 *
					 *  Output format:
					 *   Data * 10  (timestamp per sample)
					 *     <u64> timestamp
					 *     <u16> <u16> <u16> x,y,z sample (Bytes swapped for compatability)
					 *     <u16>=0 padding
					 */

					memset(data, 0, sizeof(data));
					memcpy(data, frame.data, 8); // Copy timestamp as is and first xyz sample 
					memcpy(data + 8, frame.data + 8, 6);

					memcpy(ts_data, data, 8); // copy Just time stampe
					// TODO: make functions, verify this is correct
					for(i = 0; i < 10; ++i)
					{
						uint8_t * p_samp_in = frame.data + 8 + (i*6);
						uint8_t * p_samp_out = data + (i*16)+8;
						// NOTE: IVMM does not icrement ts per bundle
						memcpy(data + (i*16), ts_data, 8); // copy timestamp

						// Inthinc DMM reverses the bytes, this just reverses the the pairs
						p_samp_out[0] = p_samp_in[1];
						p_samp_out[1] = p_samp_in[0];

						p_samp_out[2] = p_samp_in[3];
						p_samp_out[3] = p_samp_in[2];

						p_samp_out[4] = p_samp_in[5];
						p_samp_out[5] = p_samp_in[4];

						p_samp_out[6] = 0;
						p_samp_out[7] = 0;

					}

					w = write(fd_dev, data, sizeof(data));
					if(w != sizeof(data))
					{
						if(-1 == w)
						{
							perror("write");
							DTRACE("write error");
							abort(); // TODO: wtd
						}
					}
				}
				frame_reset(&frame);
			}
		}

	} while(1); // TODO:

	return NULL;
}

int write_file(const char * filename, const char * str)
{
	int fd;
	int r;

	if(0 > (fd = open(filename, O_WRONLY)))
	{
		fprintf(stderr, " write_file: \n");
		perror(filename);

		return -1;
	}
	r = write(fd, str, strlen(str));
	if(r < 0)
	{
		perror("write");
	}
	close(fd);

	if(r < 0)
		return -1;

	return (unsigned)r == strlen(str) ? 0 : -1;
}

int write_file_int(const char * filename, int number)
{
	char str[12];

	snprintf(str, sizeof(str)-1, "%d", number);
	return	write_file(filename, str);
}

void set_otg(bool enable)
{
	// TODO: dont export if already exported
	write_file_int("/sys/class/gpio/export", 917);
	sleep(1);
	if(enable)
		write_file("/sys/class/gpio/gpio917/direction", "high");
	else
		write_file("/sys/class/gpio/gpio917/direction", "low");

	sleep(1);
}



/// main functions

void do_run()
{
	//pthread_t usb_thread;
	pthread_t accel_thread;

	//struct usb_thread_context usbctx;
	struct accel_thread_context accelctx /* = {0}*/; // GCC bug #53119

	memset(&accelctx, 0, sizeof(accelctx)); // GCC bug #53119

	set_otg(true);
	sleep(5); // wait for device...

	// Control port 
	//usbctx.fd = open_serial("/dev/ttyACM0");
	//usbctx.gpio_fd = open("/dev/vgpio", O_RDWR, O_NDELAY);

	// TODO: the thread should dynamically detect the device name based on physical connection
	snprintf(accelctx.name, sizeof(accelctx.name)-1, "/dev/ttyACM1");



	// TODO: add gpio output monitor thread.
	// TODO: add acceleromter thread
	// Disable control since it is missing in MCU currently (neet USB framework)
	//pthread_create(&usb_thread, NULL, usb_proc, &usbctx);
	pthread_create(&accel_thread, NULL, accel_proc, &accelctx);

	// TODO: main thread processing
	while(true) sleep(100);
}

int main(int argc __attribute__((unused)), char * argv[] __attribute__((unused)))
{
	pid_t pid;
	pid_t sid;

	pid = fork();

	if(pid > 0)
	{
		printf("iodriver pid = %d\n", pid);
		exit(0);
	}
	printf("iodriver running\n");

	sid = setsid();
	(void)sid; // avoid warning error


	do_run();
	return EXIT_SUCCESS;
}

