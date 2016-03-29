
#include "api_constants.h"

typedef enum api_err
{
	INVALID_RESP_MSG_TYPE = -4,
	RX_MSG_FAILURE = 		-3,
	TX_MSG_FAILURE = 		-2,
	CONNECTION_FAILURE = 	-1,
	SUCCESS = 0,
}api_err_t;

int get_mcu_version(int * fd, uint8_t * fw_version, size_t size);
int get_fpga_version(int * fd, uint32_t * fpga_version, size_t size);
int get_gpi_voltage(int * fd, uint8_t gpi_num, uint32_t * gpi_voltage, size_t size);
int get_led_status(int * fd, uint8_t led_num, uint8_t *brightness, uint8_t *red, uint8_t *green, uint8_t *blue);
int set_led_status(int * fd, uint8_t led_num, uint8_t brightness, uint8_t red, uint8_t green, uint8_t blue);
int get_power_on_threshold_cfg(int * fd, uint16_t *wiggle_count, uint16_t *wig_cnt_sample_period, uint16_t *ignition_threshold);
int set_power_on_threshold_cfg(int * fd, uint16_t wiggle_count, uint16_t wig_cnt_sample_period, uint16_t ignition_threshold);
int get_power_on_reason(int * fd, uint8_t *power_on_reason);
int set_device_power_off(int * fd, uint8_t wait_time);

