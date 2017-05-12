#ifndef FLEX_CAN_COMMAND_H
#define FLEX_CAN_COMMAND_H
//Re-ordered the arguments


int FlexCAN_startup(bool listeningModeEnable, int bitrate, int termination, FLEXCAN_filter_mask* filter_array, int numfilter, char *portName,FLEXCAN_Flow_Control* flow_array ,int numFlowControlMessages);
void qb_send_j1708_packet(BYTE j1708,DWORD id, int data_len, BYTE* data);
void FlexCAN_send_message(BYTE type, uint8_t *data,/* unsigned char command,*/ int len);
void FlexCAN_send_can_packet(BYTE type,DWORD id, int data_len, BYTE* data ); //TODO: Check function name





#endif /*FLEX_CAN_COMMAND_H*/