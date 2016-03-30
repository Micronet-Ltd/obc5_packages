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
#include "api.h"


int client_command(int * fd, uint8_t * payload, size_t msg_len)
{
	uint8_t resp[SOCK_MAX_MSG];
	ssize_t num_bytes;


	if(iosocket_sendmsg(fd, payload, msg_len))
	{
        printf("%s: failure to send message\n", __func__);
		exit(-1);
	}

	num_bytes = iosocket_recvmsg(fd, resp, sizeof(resp));
	if(-1 == num_bytes)
	{
        printf("%s: failure to retrieve response\n", __func__);
		exit(-1);
	}

	
	if(num_bytes > 1)
	{
		if(0 == resp[0])
		{
			printf("R: %.*s\n", (int)num_bytes, resp+1);
		}
		else
		{
			//loghex
			printf("rx %d bytes\n", (int)num_bytes);
		}
	}
	else if(1 == num_bytes)
	{
		printf("Code: %d\n", (int)resp[0]);
	}
	else
	{
		printf("NULL response\n");
	}

	return 0;

}

void send_api_hex(int * fd, char * hexdata)
{
	uint8_t data[4096];
	int i;

	if(strlen(hexdata) > (sizeof(data)>>1))
	{
		fprintf(stderr, "too much data\n");
		return;
	}
	//FIXME: strlen(hexdata)%2 != 0
	for(i = 0; i*2 < (int)strlen(hexdata); i++)
	{
		data[i] = (hex_value(hexdata[i*2]) << 4) | (hex_value(hexdata[i*2+1]));
	}
	client_command(fd, data, i);
}

void send_api_hex2(int * fd, char * hexdata)
{
	uint8_t data[4096];
	uint32_t fpga_ver = 0;
	uint32_t gpi_voltage = 0;
	uint8_t led_num, brightness, red, green, blue, gpi_num;
	int i;
	int ret = 0;

	if(strlen(hexdata) > (sizeof(data)>>1))
	{
		fprintf(stderr, "too much data\n");
		return;
	}
	//FIXME: strlen(hexdata)%2 != 0
	for(i = 0; i*2 < (int)strlen(hexdata); i++)
	{
		data[i] = (hex_value(hexdata[i*2]) << 4) | (hex_value(hexdata[i*2+1]));
	}

	switch (data[1])
	{
		case MAPI_GET_MCU_FW_VERSION:
			ret = get_mcu_version(fd, data, 4);
			printf("MCU firmware version %x.%x.%x.%x ret = %d \n", data[0], data[1], data[2], data[3], ret);
			break;
		case MAPI_GET_FPGA_VERSION:
			ret = get_fpga_version(fd, &fpga_ver, 4);
			printf("fpga ver %x, ret = %d \n", fpga_ver, ret);
			break;
		case MAPI_GET_GPI_INPUT_VOLTAGE:
			gpi_num = data[2];
			ret = get_gpi_voltage(fd, gpi_num, &gpi_voltage, sizeof(gpi_voltage));
			printf("GPI %d, approx voltage = %d mV\n", gpi_num, gpi_voltage);
			break;
		case MAPI_GET_LED_STATUS:
			led_num = data[2];
			get_led_status(fd, led_num, &brightness, &red, &green, &blue);
			printf("get led num %d, brightness = %d, red = %d, green = %d, blue = %d \n", led_num, brightness, red, green, blue);
			break;
		case MAPI_SET_LED_STATUS:
			led_num = data[2];
			brightness = data[3];
			red = data[4];
			green = data[5];
			blue = data[6];
			set_led_status(fd, led_num, brightness, red, green, blue);
			printf("set led num %d, brightness = %d, red = %d, green = %d, blue = %d \n", led_num, brightness, red, green, blue);
			break;
		default: break;
	}
}

void send_raw(int * fd, char * hexdata)
{
	// iodriver will decode this
	uint8_t data[4069];

	data[0] = MCTRL_MRAW;
	strcpy((char*)data+1, hexdata);
	client_command(fd, data, 1 + strlen(hexdata));

}

static void display_help()
{
	fprintf(stderr, "Commands:\n\n");
	fprintf(stderr, "help\t\t\tDisplay this\n");
	fprintf(stderr, "status\t\t\tDisplay io diagnostic status (do not parse)\n");
	fprintf(stderr, "raw <hexdata>\t\tSend raw frame command to MCU (eg 'raw baadf00d')\n");
	fprintf(stderr, "api <hexdata>\t\tSend raw message to iodriver (eg 'api 00737461747573')\n");
	fprintf(stderr, "<cmd> [<cmd> ...]\tCommand handled by iodriver\n");
	fprintf(stderr, "\niodriver commands:\n");
	fprintf(stderr, "check\t\t\tCheck iodriver (does nothing useful).\n");

	fprintf(stderr, "\n");
}


int main(int argc, char * argv[])
{
	int fd;

	if(argc < 2)
	{
		fprintf(stderr, " argc = %d\n", argc);
		display_help();
		return EXIT_FAILURE;
	}

	if(0 > (fd = iosocket_connect()))
		return EXIT_FAILURE;

	// Simplify string compare macro
#define CMD(y) (0 == memcmp((argv[1]), (y), sizeof(y)))


	if(CMD("help"))
	{
		display_help();
	}
	else if(CMD("api"))
	{
		if(argv[2] != NULL && strlen(argv[2])%2 == 0)
			send_api_hex2(&fd,argv[2]);
			//send_api_hex(fd, argv[2]);

		else
			fprintf(stderr, "hex string must be multiple of 2 and non null\n");
	}
	else if(CMD("raw"))
	{
		if(argv[2] != NULL && strlen(argv[2])%2 == 0)
			send_raw(&fd, argv[2]);
		else
			fprintf(stderr, "hex string must be multple of 2 and non null\n");
	}
	else
	{
		char cmd_string[1024];
		char * p = cmd_string;
		char * pend = cmd_string + sizeof(cmd_string);
		int i = 1;

		do
		{
			*p++ = '\0';
			if((int)strlen(argv[i]) > (pend-p))
			{
				fprintf(stderr,"string too long\n");
				iosocket_disconnect(&fd);
				return EXIT_FAILURE;
			}

			// NOTE: each string is prefixed with a '\0'
			// The strings do NOT end with 0
			memcpy(p, argv[i], strlen(argv[i]));
			p += strlen(argv[i]);
			printf(" '%s' %d'\n", argv[i], (int)(pend-p));
		} while( ++i < argc);

		client_command(&fd, (uint8_t*)cmd_string, p-cmd_string);
	}


	iosocket_disconnect(&fd);

	return EXIT_SUCCESS;
}
