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

#include <string.h>
#include <errno.h>

#include "util.h"
#include "iosocket.h"

#include "api.h"

typedef struct led_param_s
{
	uint8_t brightness;
	uint8_t red;
	uint8_t green;
	uint8_t blue;
}led_param_t;

typedef struct pwr_on_cfg_s
{
	uint16_t wiggle_count;
	uint16_t wig_cnt_sample_period;
	uint16_t ignition_threshold;
}pwr_on_cfg_t;

static int get_command(int * fd, uint8_t * req, size_t req_size, uint8_t * resp, size_t resp_size)
{
	int num_bytes = 0;

	if (*fd < 0)
	{
		return CONNECTION_FAILURE;
	}
	//if(0 > (fd = iosocket_connect()))
	//	return -1;

	if(iosocket_sendmsg(fd, req, req_size))
	{
		iosocket_disconnect(fd);
		return TX_MSG_FAILURE;
	}

	uint8_t * mresp = (uint8_t *) malloc(resp_size + 1);
	num_bytes = iosocket_recvmsg(fd, mresp, resp_size + 1);
	if(-1 == num_bytes)
	{
		iosocket_disconnect(fd);
		return RX_MSG_FAILURE;
	}

	if (req[2] != mresp[0])
	{
		return INVALID_RESP_MSG_TYPE;
	}
	memcpy(resp, &mresp[1], resp_size);
	free(mresp);
	return num_bytes - 1;
}

static int set_command(int * fd, uint8_t * req, size_t req_size)
{
	if (*fd < 0)
	{
		return CONNECTION_FAILURE;
	}
	//if(0 > (fd = iosocket_connect()))
	//	return -1;
	if(iosocket_sendmsg(fd, req, req_size))
	{
		iosocket_disconnect(fd);
		return TX_MSG_FAILURE;
	}
	return SUCCESS;
}

// fw version is 4 bytes
int get_mcu_version(int * fd, uint8_t * fw_version, size_t size)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_MCU_FW_VERSION };
	return get_command(fd, req, sizeof(req), fw_version, size);
}

int get_fpga_version(int * fd, uint32_t * fpga_version, size_t size)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_FPGA_VERSION };
	return get_command(fd, req, sizeof(req), (uint8_t *)fpga_version, size);
}

int get_gpi_voltage(int * fd, uint8_t gpi_num, uint32_t * gpi_voltage, size_t size)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_GPI_INPUT_VOLTAGE, gpi_num};
	return get_command(fd, req, sizeof(req), (uint8_t *)gpi_voltage, size);
}

int get_led_status(int * fd, uint8_t led_num, uint8_t *brightness, uint8_t *red, uint8_t *green, uint8_t *blue)
{
	int ret = 0;
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_LED_STATUS };
	led_param_t led_params;
	ret = get_command(fd, req, sizeof(req), (uint8_t *)&led_params, sizeof(led_params));
	*brightness = led_params.brightness;
	*red = led_params.red;
	*green = led_params.green;
	*blue = led_params.blue;
	return ret;
}

int set_led_status(int * fd, uint8_t led_num, uint8_t brightness, uint8_t red, uint8_t green, uint8_t blue)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_LED_STATUS, led_num, brightness, red, green, blue};
	return set_command(fd, req, sizeof(req));
}

int get_power_on_threshold_cfg(int * fd, uint16_t *wiggle_count, uint16_t * wig_cnt_sample_period, uint16_t *ignition_threshold)
{
	int ret = 0;
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_POWER_ON_THRESHOLD };
	pwr_on_cfg_t power_on_params;
	ret = get_command(fd, req, sizeof(req), (uint8_t *)&power_on_params, sizeof(power_on_params));
	*wiggle_count = power_on_params.wiggle_count;
	*wig_cnt_sample_period = power_on_params.wig_cnt_sample_period;
	*ignition_threshold = power_on_params.ignition_threshold;
	return ret;
}

int set_power_on_threshold_cfg(int * fd, uint16_t wiggle_count, uint16_t wig_cnt_sample_period, uint16_t ignition_threshold)
{
	pwr_on_cfg_t power_on_params;
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_POWER_ON_THRESHOLD, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
	power_on_params.wiggle_count = wiggle_count;
	power_on_params.wig_cnt_sample_period = wig_cnt_sample_period;
	power_on_params.ignition_threshold = ignition_threshold;
	memcpy(&req[3], &power_on_params, sizeof(power_on_params));
	printf("req = %x,%x,%x,%x,%x,%x\n", req[3], req[4], req[5], req[6], req[7], req[8]);
	return set_command(fd, req, sizeof(req));
}

int get_power_on_reason(int * fd, uint8_t *power_on_reason)
{
	int ret = 0;
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_POWER_ON_REASON };
	ret = get_command(fd, req, sizeof(req), power_on_reason, sizeof(uint8_t));
	return ret;
}
