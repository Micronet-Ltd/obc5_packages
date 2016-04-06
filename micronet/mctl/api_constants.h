
/// DO NOT add to this file if not found in iodriver source tree

// Socket Command type
#define MCTRL_MSTR	0x00
#define MCTRL_MRAW	0x01
#define MCTRL_MAPI	0x02

// type of API command
#define MAPI_WRITE_RQ 0
#define MAPI_READ_RQ  1

// MAPI Commands
#define MAPI_GET_MCU_FW_VERSION		0x00
#define MAPI_GET_FPGA_VERSION		0x01
#define MAPI_GET_GPI_THRESHOLD		0x02
#define MAPI_SET_GPI_THRESHOLD		0x03
#define MAPI_GET_ADC_OR_GPI_INPUT_VOLTAGE	0x04
#define MAPI_GET_LED_STATUS			0x05
#define MAPI_SET_LED_STATUS			0x06
#define MAPI_GET_POWER_ON_THRESHOLD	0x07
#define MAPI_SET_POWER_ON_THRESHOLD	0x08
#define MAPI_GET_POWER_ON_REASON	0x09
#define MAPI_SET_DEVICE_POWER_OFF	0x0A
#define MAPI_GET_RTC_DATE_TIME		0x0B
#define MAPI_SET_RTC_DATE_TIME		0x0C
#define MAPI_SET_GPI_UPDATE_ALL_VALUES 0x0D


//#define MAPI_CONFIG_SWC			0x05
//#define MAPI_CONFIG_J1708		0x06
//#define MAPI_CAN0_FILTERS		0x07
//#define MAPI_CAN1_FILTERS		0x08
//#define MAPI_RESYNC				0x09
//#define MAPI_GET_ADC			0x0a
//#define MAPI_GET_CONFIG_GPIO	0x0b
//#define MAPI_SET_CONFIG_GPIO	0x0c
//#define MAPI_EXTENDED			0xf0
//#define MAPI_RESERVED			0xff
