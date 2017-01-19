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

#define RTC_BCD_SIZE	8
#define RTC_FLAGS_ADDR 	0x0F

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
	uint8_t sock_resp[MAX_COMMAND_PACKET_SIZE];

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

	num_bytes = iosocket_recvmsg(fd, sock_resp, resp_size + 1);
	if(-1 == num_bytes)
	{
		iosocket_disconnect(fd);
		return RX_MSG_FAILURE;
	}

	if (req[2] != sock_resp[0])
	{
		return INVALID_RESP_MSG_TYPE;
	}
	memcpy(resp, &sock_resp[1], resp_size);
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

int get_adc_or_gpi_voltage(int * fd, uint8_t gpi_num, uint32_t * gpi_voltage, size_t size)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_ADC_OR_GPI_INPUT_VOLTAGE, gpi_num};
	return get_command(fd, req, sizeof(req), (uint8_t *)gpi_voltage, size);
}

int get_led_status(int * fd, uint8_t led_num, uint8_t *brightness, uint8_t *red, uint8_t *green, uint8_t *blue)
{
	int ret = 0;
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_LED_STATUS, led_num};
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
	return set_command(fd, req, sizeof(req));
}

int get_power_on_reason(int * fd, uint8_t *power_on_reason)
{
	int ret = 0;
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_POWER_ON_REASON };
	ret = get_command(fd, req, sizeof(req), power_on_reason, sizeof(uint8_t));
	return ret;
}

int set_device_power_off(int * fd, uint8_t wait_time)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_DEVICE_POWER_OFF, wait_time};
	return set_command(fd, req, sizeof(req));
}

/* converts RTC bcd array format to string */
void rtc_convert_bcd_to_string(uint8_t * dt_bcd, char * dt_str, bool print_time)
{
	uint8_t hundreth_sec_int = (dt_bcd[0]>>4) + (dt_bcd[0]&0x0F);
	uint8_t seconds = (((dt_bcd[1]>>4)&0x7) * 10) + (dt_bcd[1]&0x0F);
	uint8_t minutes = (((dt_bcd[2]>>4)&0x7) * 10) + (dt_bcd[2]&0x0F);
	uint8_t hours = (((dt_bcd[3]>>4)&0x3) * 10) + (dt_bcd[3]&0x0F);
	uint8_t century = (dt_bcd[3]>>6);
	//uint8_t day_of_week = dt[4]&0x7;
	uint8_t day_of_month = (((dt_bcd[5]>>4)&0x3) * 10) + (dt_bcd[5]&0x0F);
	uint8_t month = (((dt_bcd[6]>>4)&0x1) * 10) + (dt_bcd[6]&0x0F);
	uint16_t year = ((dt_bcd[7]>>4) * 10) + (dt_bcd[7]&0x0F);

	year = 2000 + (century * 100) + year;

	snprintf(dt_str, RTC_STRING_SIZE , "%04d-%02d-%02d %02d:%02d:%02d.%02d ",
			year, month, day_of_month, hours, minutes, seconds, hundreth_sec_int);
	if (print_time)
	{
		printf("rtc date_time: %04d-%02d-%02d %02d:%02d:%02d.%02d\n",
				year, month, day_of_month, hours, minutes, seconds, hundreth_sec_int);
	}
}

/* converts rtc string to bcd format array that RTC expects */
void rtc_convert_string_to_bcd(uint8_t * dt_bcd, char * dt_str, bool print_bcd)
{
	unsigned int i;
	unsigned int hundreth_sec_int = 0, seconds = 0, minutes = 0, hours = 0, \
			century_bits = 0, day_of_month = 0, month = 0;
	unsigned int year = 0;

	sscanf(dt_str, "%04x-%02x-%02x %02x:%02x:%02x.%02x",
			&year, &month, &day_of_month, &hours, &minutes, &seconds, &hundreth_sec_int);

	century_bits = (year>>8)&0x0f;

	dt_bcd[0] = (uint8_t)hundreth_sec_int;
	dt_bcd[1] = (uint8_t)seconds;
	dt_bcd[2] = (uint8_t)minutes;
	dt_bcd[3] = (uint8_t)((century_bits<<6) | hours);
	dt_bcd[4] = 0x0; /* Day of week is not used (might cause problems) */
	dt_bcd[5] = (uint8_t)day_of_month;
	dt_bcd[6] = (uint8_t)month;
	dt_bcd[7] = (uint8_t)(year&0xff);

	if (print_bcd)
	{
		printf("rtc bcd date_time: ");
		for (i = 0; i < 8; i++)
		{
			printf("%x, ", dt_bcd[i]);
		}
		printf("\n");
	}
}

/* returned dt_str format : year-month-day hour:min:sec.deciseconds
 * 					   Ex :	2016-03-29 19:09:06.58
*/
int get_rtc_date_time(int * fd, char * dt_str)
{
	int ret = 0;
	uint8_t dt_bcd[RTC_BCD_SIZE] = {0};
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_RTC_DATE_TIME };

	ret = get_command(fd, req, sizeof(req), dt_bcd, sizeof(dt_bcd));
	rtc_convert_bcd_to_string(dt_bcd, dt_str, false);
	return ret;
}

/* Expected dt_str format: year-month-day hour:min:sec.deciseconds
 * 					  Ex : 2016-03-29 19:09:06.58
*/
int set_rtc_date_time(int * fd, char * dt_str)
{
	uint8_t dt_bcd[RTC_BCD_SIZE] = {0};
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_RTC_DATE_TIME,
					0, 0, 0, 0, 0, 0, 0, 0};
	rtc_convert_string_to_bcd(dt_bcd, dt_str, false);
	memcpy(&req[3],dt_bcd, sizeof(dt_bcd));
	return set_command(fd, req, sizeof(req));
}

/* get_rtc_cal_reg: get rtc analog and digital calibration registers */
int get_rtc_cal_reg(int * fd, uint8_t * dig_cal, uint8_t * anal_cal)
{
	int ret = 0;
	uint8_t rtc_cal_reg[] = {0, 0};
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_RTC_CAL_REGISTERS };

	ret = get_command(fd, req, sizeof(req), rtc_cal_reg, sizeof(rtc_cal_reg));
	*dig_cal = rtc_cal_reg[0];
	*anal_cal = rtc_cal_reg[1];
	return ret;
}

/* set_rtc_cal_reg: set rtc analog and digital calibration registers */
int set_rtc_cal_reg(int * fd, uint8_t dig_cal, uint8_t analog_cal)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_RTC_CAL_REGISTERS,
					dig_cal, analog_cal};
	return set_command(fd, req, sizeof(req));
}

/* get_rtc_reg_dbg: get any RTC register */
int get_rtc_reg_dbg(int * fd, uint8_t address, uint8_t * data)
{
	int ret = 0;
	uint8_t rtc_reg[] = {0};
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_RTC_REG_DBG, address };

	ret = get_command(fd, req, sizeof(req), rtc_reg, sizeof(rtc_reg));
	data[0] = rtc_reg[0];
	return ret;
}

/* set_rtc_reg_reg: set any RTC register */
int set_rtc_reg_dbg(int * fd, uint8_t address, uint8_t data)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_RTC_REG_DBG,
					address, data};
	return set_command(fd, req, sizeof(req));
}

bool check_rtc_battery(int * fd)
{
	uint8_t address = RTC_FLAGS_ADDR;
	uint8_t flags = 0;
	get_rtc_reg_dbg(fd, address, &flags);
	if (flags & 0x10)
	{
		return false;
	}
	return true;
}

/* get_gpio_state_dbg: get MCU GPIO, be careful, gpio needs to be valid */
int get_gpio_state_dbg(int * fd, uint16_t gpio_num, uint8_t * gpio_val)
{
	int ret = 0;
	uint8_t gpio_value[] = {0};
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_MCU_GPIO_STATE_DBG, (uint8_t)(gpio_num>>8),(uint8_t)(gpio_num&0xFF)};

	ret = get_command(fd, req, sizeof(req), gpio_value, sizeof(gpio_value));
	gpio_val[0] = gpio_value[0];
	return ret;
}

/* set_gpio_state_dbg: set MCU GPIO, be careful, gpio needs to be valid */
int set_gpio_state_dbg(int * fd, uint16_t gpio_num, uint8_t gpio_val)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_MCU_GPIO_STATE_DBG,
					(uint8_t)(gpio_num>>8),(uint8_t)(gpio_num&0xFF), gpio_val};
	return set_command(fd, req, sizeof(req));
}

int set_app_watchdog_dbg(int * fd)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_APP_WATCHDOG_REQ};
	return set_command(fd, req, sizeof(req));
}

/* set_app_wiggle_en_dbg: dis_en = 0 disables wiggle count interrupt,
 * 						  dis_en = 1 enables wiggle count interrupt
 */
int set_app_wiggle_en_dbg(int * fd, uint8_t dis_en)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_WIGGLE_EN_REQ_DBG, dis_en};
	return set_command(fd, req, sizeof(req));
}

/* get_app_wiggle_count_dbg: Gets the app wiggle count */
int get_app_wiggle_count_dbg(int * fd, uint32_t * wiggle_count)
{
	int ret = 0;
	uint8_t wig_cnt_b[4] = {0, 0, 0, 0};
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_WIGGLE_COUNT_REQ_DBG};

	ret = get_command(fd, req, sizeof(req), wig_cnt_b, sizeof(wig_cnt_b));
	*wiggle_count = (wig_cnt_b[3]<<24) | (wig_cnt_b[2]<<16) | (wig_cnt_b[1]<<8) | wig_cnt_b[0];
	return ret;
}

/* set_accel_standby_active_dbg: set accel chip in standbye mode (0)
 * or set accel chip in active mode(1). fifo is disabled in standbye mode
 */
int set_accel_standby_active_dbg(int * fd, uint8_t standbye_active)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_ACCEL_STANDBY_ACTIVE_DBG, standbye_active};
	return set_command(fd, req, sizeof(req));
}

/* get_accel_reg_dbg: get any accelerometer chip register
 * Returns 1 byte of data
 *  */
int get_accel_reg_dbg(int * fd, uint8_t address, uint8_t * data)
{
	int ret = 0;
	uint8_t accel_req[] = {0};
	uint8_t req[] = { MCTRL_MAPI, MAPI_READ_RQ, MAPI_GET_ACCEL_REGISTER_DBG, address };

	ret = get_command(fd, req, sizeof(req), accel_req, sizeof(accel_req));
	data[0] = accel_req[0];
	return ret;
}

/* set_accel_reg_dbg: set any accelerometer chip register */
int set_accel_reg_dbg(int * fd, uint8_t address, uint8_t data)
{
	uint8_t req[] = { MCTRL_MAPI, MAPI_WRITE_RQ, MAPI_SET_ACCEL_REGISTER_DBG,
					address, data};
	return set_command(fd, req, sizeof(req));
}
