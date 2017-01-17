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
#include <sys/time.h>

#include "util.h"
#include "iosocket.h"
#include "api.h"
#include "mcu_gpio_pins.h"


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

ssize_t format_timeval(struct timeval *tv, char *buf, size_t sz)
{
  ssize_t written = -1;
  struct tm *gm = gmtime(&tv->tv_sec);

  if (gm)
  {
    written = (ssize_t)strftime(buf, sz, "%Y-%m-%d %H:%M:%S", gm);
    if ((written > 0) && ((size_t)written < sz))
    {
      int w = snprintf(buf+written, sz-(size_t)written, ".%02d",(int) (tv->tv_usec/10000));
      written = (w > 0) ? written + w : -1;
      //printf("usec: %.06d, decisec: .%02d, buf_size = %d written = %d\n", (int) tv->tv_usec, (int) (tv->tv_usec/10000), (int) sz, (int)written);
    }
  }
  return written;
}

int send_api_hex2(int * fd, char * hexdata)
{
	uint8_t data[4096];
	uint32_t fpga_ver = 0;
	uint32_t gpi_voltage = 0;
	uint8_t led_num, brightness, red, green, blue, gpi_num, power_on_reason, wait_time;
	uint8_t rtc_dig_cal, rtc_analog_cal, rtc_reg_addr, rtc_reg_data, gpio_val, wig_en;
	uint8_t accel_standby_active, accel_reg_addr, accel_reg_data;
	uint16_t wiggle_count, wig_cnt_sample_period, ignition_threshold, gpio_num;
	uint32_t wiggle_count_32;
	int i;
	int ret = 0;
	char dt_str[RTC_STRING_SIZE] = "2016-03-29 19:09:06.58\0";
	struct timeval tv;

	if(strlen(hexdata) > (sizeof(data)>>1))
	{
		fprintf(stderr, "too much data\n");
		return -1;
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
			printf("MCU firmware version in hex %x.%x.%x.%x ret = %d \n", data[0], data[1], data[2], data[3], ret);
			break;
		case MAPI_GET_FPGA_VERSION:
			ret = get_fpga_version(fd, &fpga_ver, 4);
			printf("fpga ver 0x%x, ret = %d \n", fpga_ver, ret);
			break;
		case MAPI_GET_ADC_OR_GPI_INPUT_VOLTAGE:
			gpi_num = data[2];
			ret = get_adc_or_gpi_voltage(fd, gpi_num, &gpi_voltage, sizeof(gpi_voltage));
			printf("GPI %d, approx voltage = %d mV, ret = %d \n", gpi_num, gpi_voltage, ret);
			break;
		case MAPI_GET_LED_STATUS:
			led_num = data[2];
			ret = get_led_status(fd, led_num, &brightness, &red, &green, &blue);
			printf("get led num %d, brightness = %d, red = %d, green = %d, blue = %d, ret = %d \n", \
					led_num, brightness, red, green, blue, ret);
			break;
		case MAPI_SET_LED_STATUS:
			led_num = data[2];
			brightness = data[3];
			red = data[4];
			green = data[5];
			blue = data[6];
			ret = set_led_status(fd, led_num, brightness, red, green, blue);
			printf("set led num %d, brightness = %d, red = %d, green = %d, blue = %d, ret = %d  \n", \
					led_num, brightness, red, green, blue, ret);
			break;
		case MAPI_GET_POWER_ON_THRESHOLD:
			ret = get_power_on_threshold_cfg(fd, &wiggle_count, &wig_cnt_sample_period, &ignition_threshold);
			printf("get power on threshold  wiggle_count = %d, wig_cnt_sample_period = %d mS, ignition_threshold = %d mV, ret = %d  \n", \
					wiggle_count, wig_cnt_sample_period, ignition_threshold, ret);
			break;
		case MAPI_SET_POWER_ON_THRESHOLD:
			wiggle_count = (uint16_t)((data[2]<<8)|data[3]); //Big endian data
			wig_cnt_sample_period = (uint16_t)((data[4]<<8)|data[5]);;
			ignition_threshold = (uint16_t)((data[6]<<8)|data[7]);;
			ret = set_power_on_threshold_cfg(fd, wiggle_count, wig_cnt_sample_period, ignition_threshold);
			printf("set power on threshold  wiggle_count = %d, wig_cnt_sample_period = %d ms, ignition_threshold = %d mV, ret = %d  \n", \
								wiggle_count, wig_cnt_sample_period, ignition_threshold, ret);
			break;
		case MAPI_GET_POWER_ON_REASON:
			ret = get_power_on_reason(fd, &power_on_reason);
			printf("power on reason %d, ret = %d\n", power_on_reason, ret);
			break;
		case MAPI_SET_DEVICE_POWER_OFF:
			wait_time = data[2];
			ret = set_device_power_off(fd, wait_time);
			printf("device power off req with wait time %d sec., ret = %d\n", wait_time, ret);
			break;
		case MAPI_GET_RTC_DATE_TIME:
			ret = get_rtc_date_time(fd, dt_str);
			printf("get rtc %s, ret = %d\n", dt_str, ret);
			break;
		case MAPI_SET_RTC_DATE_TIME:
			if (gettimeofday(&tv, NULL) != 0)
			{
			    perror("gettimeofday");
			    break;
			}
			if (format_timeval(&tv, dt_str, sizeof(dt_str)) < 0)
			{
				perror("format_timeval");
				break;
			}
			ret = set_rtc_date_time(fd, dt_str);
			printf("set rtc %s, ret = %d\n", dt_str, ret);
			break;
		case MAPI_GET_RTC_CAL_REGISTERS:
			ret = get_rtc_cal_reg(fd, &rtc_dig_cal, &rtc_analog_cal);
			printf("get rtc cal registers, dig cal: 0x%x analog cal: 0x%x, ret = %d  \n", \
					rtc_dig_cal, rtc_analog_cal, ret);
			break;
		case MAPI_SET_RTC_CAL_REGISTERS:
			rtc_dig_cal = data[2];
			rtc_analog_cal = data[3];
			ret = set_rtc_cal_reg(fd, rtc_dig_cal, rtc_analog_cal);
			printf("set rtc cal registers, dig cal: 0x%x analog cal: 0x%x, ret = %d  \n", \
								rtc_dig_cal, rtc_analog_cal, ret);
			break;
		case MAPI_GET_RTC_REG_DBG:
			rtc_reg_addr = data[2];
			ret = get_rtc_reg_dbg(fd, rtc_reg_addr, &rtc_reg_data);
			printf("get rtc registers @ addr: 0x%x value read: 0x%x, ret = %d  \n", \
					rtc_reg_addr, rtc_reg_data, ret);
			break;
		case MAPI_SET_RTC_REG_DBG:
			rtc_reg_addr = data[2];
			rtc_reg_data = data[3];
			ret = set_rtc_reg_dbg(fd, rtc_reg_addr, rtc_reg_data);
			printf("set rtc registers @ addr: 0x%x value set: 0x%x, ret = %d  \n", \
					rtc_reg_addr, rtc_reg_data, ret);
			break;

		case MCTL_IS_RTC_BATTERY_GOOD:
			ret = check_rtc_battery(fd);
			if (ret)
			{
				printf("rtc battery good\n");
			}
			else
			{
				printf("rtc battery low or not present\n");
			}
			break;
		case MAPI_GET_MCU_GPIO_STATE_DBG:
			gpio_num = (data[2]<<8) | data[3];
			ret = get_gpio_state_dbg(fd, gpio_num, &gpio_val);
			printf("get mcu gpio state, gpio: %d value read: %d, ret = %d  \n", \
						gpio_num, gpio_val, ret);
			break;
		case MAPI_SET_MCU_GPIO_STATE_DBG:
			gpio_num = (data[2]<<8)| data[3];
			gpio_val = data[4];
			ret = set_gpio_state_dbg(fd, gpio_num, gpio_val);
			printf("set mcu gpio state, gpio: %d value set: %d, ret = %d  \n", \
					gpio_num, gpio_val, ret);
			break;
		case MAPI_SET_APP_WATCHDOG_REQ:
			ret = set_app_watchdog_dbg(fd);
			printf("set app watchdog req, ret = %d  \n", ret);
			break;
		case MCTL_GET_CAN1_J1708_PWR_ENABLE_GPIO:
			ret = get_gpio_state_dbg(fd, CAN1_J1708_PWR_ENABLE, &gpio_val);
			printf("get CAN1 J1708 pwr enable gpio: %d, value read: %d, ret = %d  \n", \
					CAN1_J1708_PWR_ENABLE, gpio_val, ret);
			break;
		case MCTL_SET_CAN1_J1708_PWR_ENABLE_GPIO:
			gpio_val = data[2];
			ret = set_gpio_state_dbg(fd, CAN1_J1708_PWR_ENABLE, gpio_val);
			printf("set CAN1 J1708 pwr enable , gpio: %d, value set: %d, ret = %d  \n", \
					CAN1_J1708_PWR_ENABLE, gpio_val, ret);
			break;
		case MAPI_SET_WIGGLE_EN_REQ_DBG:
			wig_en = data[2];
			ret = set_app_wiggle_en_dbg(fd, wig_en);
			printf("set app wiggle en to %d, ret = %d  \n", wig_en, ret);
			break;
		case MAPI_GET_WIGGLE_COUNT_REQ_DBG:
			wiggle_count_32 = data[2];
			ret = get_app_wiggle_count_dbg(fd, &wiggle_count_32);
			printf("get wiggle count:  %d, ret = %d  \n", wiggle_count_32, ret);
			break;
		case MAPI_SET_ACCEL_STANDBY_ACTIVE_DBG:
			accel_standby_active = data[2];
			ret = set_accel_standby_active_dbg(fd, accel_standby_active);
			printf("set accel standby active to %d, ret = %d  \n", accel_standby_active, ret);
			break;
		case MAPI_GET_ACCEL_REGISTER_DBG:
			accel_reg_addr = data[2];
			ret = get_accel_reg_dbg(fd, accel_reg_addr, &accel_reg_data);
			printf("get accel registers @ addr: 0x%x value read: 0x%x, ret = %d  \n", \
					accel_reg_addr, accel_reg_data, ret);
			break;
		case MAPI_SET_ACCEL_REGISTER_DBG:
			accel_reg_addr = data[2];
			accel_reg_data = data[3];
			ret = set_accel_reg_dbg(fd, accel_reg_addr, accel_reg_data);
			printf("set accel registers @ addr: 0x%x value set: 0x%x, ret = %d  \n", \
					accel_reg_addr, accel_reg_data, ret);
			break;

		default:
			printf("invalid api command \n");
		   	break;
	}
	return ret;
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
	int ret;

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
		{
			ret = send_api_hex2(&fd,argv[2]);
			if (ret < 0)
			{
				iosocket_disconnect(&fd);
				return EXIT_FAILURE;
			}
		}
		else
		{
			fprintf(stderr, "hex string must be multiple of 2 and non null\n");
		}
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
