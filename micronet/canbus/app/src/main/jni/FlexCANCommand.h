#ifndef FLEX_CAN_COMMAND_H
#define FLEX_CAN_COMMAND_H
//Re-ordered the arguments

int FlexCAN_startup(bool listeningModeEnable, int bitrate, int termination, FLEXCAN_filter_mask* filter_array, int numfilter, char *portName,FLEXCAN_Flow_Control* flow_array ,int numFlowControlMessages);
int FlexCAN_j1708_startup(char *portName);
void FlexCAN_send_can_packet(BYTE type,DWORD id, int data_len, BYTE* data, int portNumber);
void FlexCAN_send_j1708_packet(DWORD id, BYTE *data, BYTE priority, int dataLength);

#endif /*FLEX_CAN_COMMAND_H*/