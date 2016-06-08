// Possibly safest max in linux (see TLPI 57.3)
// This is usually page size or less
#define SOCK_MAX_MSG 4*1024

typedef enum
{
	SYNC_INFO		= 0,
	COMM_WRITE_REQ 	= 1,
	COMM_READ_REQ	= 2,
	COMM_READ_RESP 	= 3,
	PING_REQ		= 4,
	PING_RESP		= 5,
	GPIO_INT_STATUS	= 6,
} packet_type_enum;

struct control_thread_context
{
	bool running;
	char name[PATH_MAX];
	fd_set fds;
	int mcu_fd;
	int gpio_fd;
	int sock_fd;
	frame_t frame;
	struct sockaddr_un * sock_resp_addr;
	bool rtc_req;
	uint8_t rtc_init_val[8];

	uint8_t seq;

	uint8_t mcu_fw_version[4]; // TODO: query the MCU for this
	bool received_fw_version;

	int ping_sent;
	int pong_recv;
    int vled_fd;
};

void * control_proc(void * cntx);

// '#' will be replaced with '\0'
#define UD_NAMESPACE "#micronet_control"

void * control_proc(void * cntx);
