/** Anthony Best <anthony.best@micronet-inc.com>
 *  Userspace driver for OBC MCU
 */
// FIXME: This should be the POSIX compatability flag, and the function that depends on this should be commented
//#define _GNU_SOURCE
#include <stdio.h>

#define DEBUG_TRACE

#include <stdlib.h>
#include <unistd.h>
#include <libgen.h>
#include <linux/limits.h>

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
#include <fcntl.h>
#include <sys/ioctl.h>

#include "log.h"
#include "misc.h"
#include "queue.h"
#include "frame.h"
#include "util.h"
#include "tty.h"

#include "control.h"
#include "api_constants.h"

//#define IO_CONTROL_RECOVERY_DEBUG 1

#if defined (IO_CONTROL_RECOVERY_DEBUG)
#define IO_CONTROL_LOG "/cache/io_control.log"
static void redirect_stdio(const char* filename)
{
    // If these fail, there's not really anywhere to complain...
    freopen(filename, "a", stdout);
    setbuf(stdout, 0);
    freopen(filename, "a", stderr);
    setbuf(stderr, 0);
}
#endif

static int send_sock_data(struct control_thread_context * context, struct sockaddr_un * addr, uint8_t * data, size_t len);

struct sockaddr_un * g_rx_addr;

static uint8_t control_get_seq(struct control_thread_context * context)
{
	return context->seq++;
}

static int control_get_status_message(struct control_thread_context * context, char * buff, size_t size)
{
	snprintf(buff, size, "No status");
	return 0;
}


static int control_open_socket(struct control_thread_context * context __attribute__((unused)))
{
	struct sockaddr_un s_addr = {0};
	int fd;

	s_addr.sun_family = AF_UNIX;
	strncpy(s_addr.sun_path, UD_NAMESPACE, sizeof(s_addr.sun_path) - 1);
	s_addr.sun_path[0] = '\0'; // abstract socket namespace, replace '#' with '\0'

	if( -1 == (fd = socket(AF_UNIX, SOCK_DGRAM, 0)))
	{
		DERR("socket: %s", strerror(errno));
		exit(-1);
	}

	if(-1 == bind(fd, (struct sockaddr *)&s_addr, sizeof(struct sockaddr_un)))
	{
		DERR("bind: %s", strerror(errno));
		close(fd);
		exit(-1);
	}

	return fd;
}

// returns < 0 on error, 0 on io, others undefined
static int control_thread_wait(struct control_thread_context * context)
{
	int r;
	int max_fd = -1;

	if(max_fd < context->mcu_fd)
		max_fd = context->mcu_fd;
	if(max_fd < context->sock_fd)
		max_fd = context->sock_fd;
	if(max_fd < context->gpio_fd)
		max_fd = context->gpio_fd;
    if(max_fd < context->vled_fd)
        max_fd = context->vled_fd;

	//DTRACE("max_fd=%d", max_fd);

	// TODO: this may need to handle other conditions, it may need to be refactored
	if(max_fd < 0)
		return -1;

	do
	{
		struct timeval tv;
		FD_ZERO(&context->fds);

		if(context->mcu_fd >= 0)
		{
			FD_SET(context->mcu_fd, &context->fds);
			//DTRACE("FD_SET fd");
		}

		if(context->sock_fd >= 0)
		{
			FD_SET(context->sock_fd, &context->fds);
			//DTRACE("FD_SET sock");
		}

		if(context->gpio_fd >= 0)
		{
			FD_SET(context->gpio_fd, &context->fds);
			DTRACE("FD_SET gpio");
		}

        if (context->vled_fd >= 0) {
            FD_SET(context->vled_fd, &context->fds);
            DTRACE("select vled_fd");
        }

        tv.tv_sec = 1; // 1 to make less output for now
		tv.tv_usec = 0;

		//DTRACE("max_fd=%d about to select %d:%d", max_fd, (int)tv.tv_sec, (int)tv.tv_usec);
		r = select(max_fd+1, &context->fds, NULL, NULL, &tv);

	} while(-1 == r && EINTR == errno);

	//DTRACE("r = %d", r);

	if(r == -1)
	{
		DERR("select: %s", strerror(errno));
		return -1;
	}

	if( r > 0)
	{
		if( (context->mcu_fd > -1) && FD_ISSET(context->mcu_fd, &context->fds))
			return 0;
		if( (context->sock_fd > -1) && FD_ISSET(context->sock_fd, &context->fds))
			return 0;
		if( (context->gpio_fd > -1) && FD_ISSET(context->gpio_fd, &context->fds))
			return 0;
        if ((context->vled_fd > -1) && FD_ISSET(context->vled_fd, &context->fds)) {
            DTRACE("vled selected");
            return 0;
        }

		//DTRACE("select returned %d but no fd is set", r);
	}
	else
	{
		//DTRACE("no data idle");
		return 0;
	}
	return -1;
}

static int  control_send_mcu(struct control_thread_context * context, uint8_t * msg, size_t len)
{
	uint8_t encoded_buffer[1024*2+2];
	int r = -1;

	r = frame_encode(msg, encoded_buffer, len);
	if(r > 0)
	{
		size_t st;
		st = write(context->mcu_fd, encoded_buffer, r);
		if(st != r)
			return -1;
		return 0;
	}
	return -1;
}

static int control_gpio_input(struct control_thread_context * context, uint8_t mask, uint8_t value)
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
				DERR("write: %s", strerror(errno));
				return r;
			}
			DTRACE("r = %d", r);
		}
	}
	return 0;
}

static int control_frame_process(struct control_thread_context * context, uint8_t * data, size_t len)
{
	// NOTE: Do not block here.
	// TODO: check for sequence gaps
	//int seq = data[1]; // TODO:
	packet_type_enum packet_type;

	if(len < 1)
		return -1;
	if(len > (1024 + 2))
		return -2;

	packet_type = (packet_type_enum)data[1];

	// TODO: handle messages
	switch (packet_type)
	{
		case SYNC_INFO:	// Sync/Info
			break;

		case COMM_WRITE_REQ: // Write register
			data[0] = control_get_seq(context);
			data[1] = (uint8_t)COMM_WRITE_REQ;
			control_send_mcu(context, data, len);
			break;

		case COMM_READ_REQ: // Register read request
			data[0] = control_get_seq(context);
			data[1] = (uint8_t)COMM_READ_REQ;
			control_send_mcu(context, data, len);
			break;

		case COMM_READ_RESP: // register read response
			DTRACE("COMM_READ_RESPONSE: %x, %x, %x ... %x, %x, len= %d",\
					data[2], data[3], data[4], data[len -2], data[len-1], (int)len);
			send_sock_data(context, g_rx_addr, &data[2], len);
			break;

		case PING_REQ: // PING request
			{
				uint8_t msg[2];
				msg[0] = control_get_seq(context);
				msg[1] = (uint8_t)PING_RESP;
				return control_send_mcu(context, msg, sizeof(msg));
			}
			break;

		case PING_RESP: // PING response
			DTRACE("PING_RESP: %d", context->pong_recv);
			context->pong_recv++;
			break;

		case GPIO_INT_STATUS: // GPIO Interrupt
			DTRACE("GPIO_INT data2 %d data3 %d", data[2], data[3]);
			control_gpio_input(context, data[2], data[3]);
			break;
	}
	return 0;
}

static int control_receive_mcu(struct control_thread_context * context)
{
	ssize_t bytes_read; // NOTE signed type
	int offset;
	uint8_t readbuffer[1024];
	DTRACE("");

	bytes_read = read(context->mcu_fd, readbuffer, sizeof(readbuffer));
	if(bytes_read < 0)
	{
		if(EAGAIN == errno)
			return 0; //
		DERR("read: %s", strerror(errno));
		close(context->mcu_fd);
		context->mcu_fd = -1;
		frame_reset(&context->frame);
		return -1;
		//abort();
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
			status = control_frame_process(context, context->frame.data, context->frame.data_len);
			if(status)
				DTRACE("status != 0");
			frame_reset(&context->frame);
		}
	}

	//DTRACE("TODO: Read message from MCU");
	//DTRACE("TODO: Handle messages");
	//DTRACE("TODO: Check for more messages");

	return 0;
}

static int send_sock_data(struct control_thread_context * context, struct sockaddr_un * addr, uint8_t * data, size_t len)
{
	socklen_t sock_len;
	int r;

	sock_len = sizeof(struct sockaddr_un);

	if(len >= SOCK_MAX_MSG)
	{
		 DERR("msg len(%u) >= SOCK_MAX_MSG", (unsigned int)len);
		 return -1;
	}

	r = sendto(context->sock_fd, data, len, 0, (struct sockaddr*)addr, sock_len);
	DTRACE("r = %d", r);
	if(-1 == r)
	{
		DERR("sendto: %s", strerror(errno));
		return -1;
	}

	return 0;
}

static int send_sock_string_message(struct control_thread_context * context, struct sockaddr_un * addr, char * msg)
{
	char buf[SOCK_MAX_MSG];
	socklen_t sock_len;
	size_t len;
	int r;

	sock_len = sizeof(struct sockaddr_un);

	len = strlen(msg);
	if(0 == len)
	{
		DERR("msg len = 0");
		return -1;
	}
	if(len >= SOCK_MAX_MSG)
	{
		 DERR("msg len(%u) >= SOCK_MAX_MSG", (unsigned int)len);
		 return -1;
	}
	buf[0] = '\0';
	memcpy(buf+1, msg, len);

	r = sendto(context->sock_fd, buf, (1+len), 0, (struct sockaddr*)addr, sock_len);
	DTRACE("r = %d", r);
	if(-1 == r)
	{
		DERR("sendto: %s", strerror(errno));
		return -1;
	}

	return 0;
}

static int hex_value(uint8_t hex)
{
	//Probably a better portable way of doing this
	// if(a >= '0' && a <= '9') return '0'-a;
	// return 'a' - tolower(a);
	switch(hex)
	{
		default:
		case '0': return 0;
		case '1': return 1;
		case '2': return 2;
		case '3': return 3;
		case '4': return 4;
		case '5': return 5;
		case '6': return 6;
		case '7': return 7;
		case '8': return 8;
		case '9': return 9;
		case 'a':
		case 'A': return 0xa;
		case 'b':
		case 'B': return 0xb;
		case 'c':
		case 'C': return 0xc;
		case 'd':
		case 'D': return 0xd;
		case 'e':
		case 'E': return 0xe;
		case 'f':
		case 'F': return 0xf;
	}
}

static int control_handle_sock_raw(struct control_thread_context * context, struct sockaddr_un * addr, uint8_t * hex_data, size_t len)
{
	uint8_t data[1024];
	int i;
	int r;


	if(len%2)
	{
		DERR("Len not even, len=%u", (unsigned int)len);
		log_hex(hex_data, len);
		return -1;
	}

	for(i = 0; i*2 < len; i++)
	{
		data[i] = hex_value(hex_data[i*2]) << 4;
		data[i] |= hex_value(hex_data[i*2+1]);
	}
	data[0] = control_get_seq(context);

	r = control_send_mcu(context, data, i);
	if(r)
		return send_sock_string_message(context, addr, "ERROR");
	else
		return send_sock_string_message(context, addr, "OK");

}

static int control_handle_sock_command(struct control_thread_context * context, struct sockaddr_un * addr, uint8_t * data, size_t len)
{
	socklen_t sock_len;
	int r = -1;

	sock_len = sizeof(struct sockaddr_un);

	if(0 == memcmp(data, "status", strlen("status")+1))
	{
		char statusbuf[SOCK_MAX_MSG-2];
		if(0 == control_get_status_message(context, statusbuf, sizeof(statusbuf)))
		{
			if(send_sock_string_message(context, addr, statusbuf))
				r = -1;
			else
				r = 0;
		}
	}
	else if(0 == memcmp(data, "check", strlen("check")+1))
	{
		if(send_sock_string_message(context, addr, "Running"))
			r = -1;
		else
			r = 0;
	}

	if(0 > r)
		return send_sock_string_message(context, addr, "ERROR");
	if(1 == r)
	   return send_sock_string_message(context, addr, "OK");
	if(0 == r)
		return 0;
	return -1;
}

static int control_handle_api_command(struct control_thread_context * context, struct sockaddr_un * addr, uint8_t * data, size_t len)
{
	int r = -1;

	uint8_t *  mdata = (uint8_t *) malloc(len + 1);
	/* write req */
	if (data[0] == MAPI_WRITE_RQ)
	{
		mdata[1] = COMM_WRITE_REQ;
	}
	/* read req */
	else if (data[0] == MAPI_READ_RQ)
	{
		g_rx_addr = addr; /* the response will be sent back on this address */
		mdata[1] = COMM_READ_REQ;
	}
	memcpy(&mdata[2], &data[1], len - 1);
	r = control_frame_process(context, mdata, len + 1);
	free(mdata);
	return r;
}

static int control_receive_gpio(struct control_thread_context * context)
{
	int r;
	uint8_t data[4];
	uint8_t msg[4];

	msg[0] = control_get_seq(context);
	msg[1] = (uint8_t)GPIO_INT_STATUS;

	r = read(context->gpio_fd, data, sizeof(data));

	if(-1 == r)
	{
		DERR("read: %s", strerror(errno));
		return -1;
	}
	msg[2] = data[2];
	msg[3] = data[3];

	return control_send_mcu(context, msg, sizeof(msg));
}

#define LED_DAT_LEN 5
static int control_leds(struct control_thread_context * context)
{
    int err, i;
    uint8_t leds_data[16];
    uint8_t msg[8];

    err = read(context->vled_fd, leds_data, sizeof(leds_data));

    if (-1 == err) {
        DERR("failure to read[/dev/vleds] - %s", strerror(errno));
        return -1;
    }

    if ((uint8_t)-1 == leds_data[15]) {
        // nothing chenged
        return 0;
    }

    if (leds_data[15] > 3 || leds_data[15] < 1) {
        DERR("invalid led [%d]", leds_data[15]);
        return -1;
    }

    msg[0] = control_get_seq(context);
    msg[1] = (uint8_t)COMM_WRITE_REQ;
    msg[2] = (uint8_t)MAPI_SET_LED_STATUS;

    if (leds_data[15] & 1) {
        i = 0; 

        memcpy(&msg[3], &leds_data[i], LED_DAT_LEN);
        DINFO("set led[%d:%d] req[%d:%d:%d:%d]", msg[3], i, msg[4], msg[5], msg[6], msg[7]);
    #if defined (IO_CONTROL_RECOVERY_DEBUG)
        printf("%s: set led[%d:%d] req[%d:%d:%d:%d]\n", __func__, msg[3], i, msg[4], msg[5], msg[6], msg[7]);
    #endif
        err = control_send_mcu(context, msg, sizeof(msg));
        if (-1 == err) {
            DERR("failure to send command - %s", strerror(errno));
            return -1;
        }
    }

    if (leds_data[15] & 2) {
        i = LED_DAT_LEN; 

        memcpy(&msg[3], &leds_data[i], LED_DAT_LEN);
        DINFO("set led[%d:%d] req[%d:%d:%d:%d]", msg[3], i, msg[4], msg[5], msg[6], msg[7]);
    #if defined (IO_CONTROL_RECOVERY_DEBUG)
        printf("%s: set led[%d:%d] req[%d:%d:%d:%d]\n", __func__, msg[3], i, msg[4], msg[5], msg[6], msg[7]);
    #endif
        err = control_send_mcu(context, msg, sizeof(msg));
        if (-1 == err) {
            DERR("failure to send command - %s", strerror(errno));
            return -1;
        }
    }
    return 0;
}

static int control_receive_sock(struct control_thread_context * context)
{
	struct sockaddr_un c_addr = {0};
	uint8_t buf[SOCK_MAX_MSG];
	int ret = 0;
	int r = -1;

	socklen_t sock_len;
	ssize_t num_bytes;

	sock_len = sizeof(struct sockaddr_un);
	num_bytes = recvfrom(context->sock_fd, buf, sizeof(buf), 0, (struct sockaddr*)&c_addr, &sock_len);
	if(-1 == num_bytes)
	{
		DERR("recvfrom: %s", strerror(errno));
		exit(-1);
	}


	DTRACE("server recv %ld bytes from '%s'", (long)num_bytes,
			(c_addr.sun_path[0] ? 0 : 1) + c_addr.sun_path); // if first byte is null add one to address (sorry bad style here) assumes last byte in sub_path is null as it should be

	// TODO: remove for production.
	log_hex(buf, num_bytes);
	if(num_bytes < 1)
	{
		DERR("Empty message");
		return -1;
	}

	switch(buf[0])
	{
		// 0 String command
		case MCTRL_MSTR:
			r = control_handle_sock_command(context, &c_addr, buf+1, num_bytes-1);
			break;

		// 1 RAW command
		case MCTRL_MRAW:
			r = control_handle_sock_raw(context, &c_addr, buf+1, num_bytes-1);
			break;

		// 2 API Command
		case MCTRL_MAPI:
			r = control_handle_api_command(context, &c_addr, buf+1, num_bytes-1);
			break;

		default:
			DERR("Error, unknown command type %d", buf[0]);
			log_hex(buf, num_bytes);

			return send_sock_string_message(context, &c_addr, "ERROR");
	}

	return 0;
}


static void check_devices(struct control_thread_context * context)
{
	if( -1 == context->mcu_fd)
	{
		DTRACE("check for device '%s'", context->name);

		// TODO: This should search for the correct device name by
		// using /sys/bus/usb to find the current device name incase 
		// other devices enumerate, or the order changes, etc.
		if(file_exists(context->name))
		{
			if(!file_exists("/data/disable_control_tty"))
			{
				context->mcu_fd = open_serial(context->name);
				DINFO("opened %s fd = %d", context->name, context->mcu_fd);
			}
		}
		else
		{
			DINFO("%s does not exist", context->name);
		}
	}

	// TODO: add vgpio, and sockets here if needed
}



void * control_proc(void * cntx)
{
	struct control_thread_context * context = cntx;
	int status;
	uint8_t databuffer[1024]; // >= 10 * (8*2)
	time_t time_last_sent_ping = 0;
	bool on_init = true;
	int ret = 0;

#if defined (IO_CONTROL_RECOVERY_DEBUG)
    redirect_stdio(IO_CONTROL_LOG);
#endif
	frame_setbuffer(&context->frame, databuffer, sizeof(databuffer));

	context->running = true;

	context->gpio_fd = -1;
	context->mcu_fd = -1;
	context->sock_fd = -1;
    context->vled_fd = -1;

	// TODO: maby move to check_devies()
	if(file_exists("/dev/vgpio"))
		context->gpio_fd = open("/dev/vgpio", O_RDWR, O_NDELAY);

    if(file_exists("/dev/vleds"))
        context->vled_fd = open("/dev/vleds", O_RDONLY, O_NDELAY);

    // TODO: maby move to check_devies()
	context->sock_fd = control_open_socket(context);

	do
	{
		//DTRACE("Main loop");

		// Check for devices that need to be opened/reopened
		check_devices(context);

		// TODO: Waiting for events
		status = control_thread_wait(context);
		//DTRACE("control_thread_wait returned %d", status);

		if (on_init && (context->mcu_fd > -1 ))
		{
			on_init = false;
			/* Request for all the GPInput values, in case they were missed on bootup */
			uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_GPI_UPDATE_ALL_VALUES};
			ret = control_handle_api_command(context, NULL, req+1, (sizeof(req)-1));
		}

		if((context->mcu_fd > -1) && FD_ISSET(context->mcu_fd, &context->fds))
		{
			status = control_receive_mcu(context);
			if(status < 0)
			{
				DERR("control_receive_mcu returned %d", status);
				context->running = false;
				break;
			}
			DTRACE("After recv");
		}

		if((context->gpio_fd > -1) && FD_ISSET(context->gpio_fd, &context->fds))
		{
			status = control_receive_gpio(context);
			if(status < 0)
			{
				DERR("control_receive_gpio returned %d\n", status);
			}
			DTRACE("After sock receive");
		}

        if ((context->vled_fd > -1) && FD_ISSET(context->vled_fd, &context->fds)) {
            status = control_leds(context);
            if(status < 0) {
                DERR("failure to set led %d\n", status);
            }
            DTRACE("After sock receive");
        }

		if((context->sock_fd > -1) && FD_ISSET(context->sock_fd, &context->fds))
		{
			status = control_receive_sock(context);
			if(status < 0)
			{
				DERR("control_receive_sock returned %d\n", status);
			}
			DTRACE("After sock receive");
		}

		if(context->mcu_fd > -1)
		{
			if( (0 == time_last_sent_ping) || ((time(NULL) - time_last_sent_ping) > 1) )
			{
				uint8_t msg[2];
				msg[0] = control_get_seq(context);
				msg[1] = (uint8_t)PING_REQ;
				time_last_sent_ping = time(NULL);
				context->ping_sent++;

				control_send_mcu(context, msg, sizeof(msg));
			}
		}

	} while(context->running);

#if defined (IO_CONTROL_RECOVERY_DEBUG)
    redirect_stdio("/dev/tty");
#endif

	DINFO("control thread exiting");
	return NULL;
}

