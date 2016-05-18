#include <stdint.h>
#include <stdbool.h>
typedef struct
{
	uint8_t * data;
	size_t data_alloc;
	size_t data_len;

	bool escape_flag;
	bool data_ready;
	bool in_frame;
} frame_t;

void frame_reset(frame_t * frame);
void frame_setbuffer(frame_t * frame, uint8_t * buffer, size_t len);
int frame_process_buffer(frame_t * frame, uint8_t * buffer, size_t len);

int frame_encode(uint8_t * s, uint8_t * d, int len);
bool frame_data_ready(frame_t * frame);

