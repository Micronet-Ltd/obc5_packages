#ifndef FLEX_CAN_COMMAND_H
#define FLEX_CAN_COMMAND_H
//Re-ordered the arguments

void qb_send_j1708_packet(BYTE j1708,DWORD id, int data_len, BYTE* data);
void FlexCAN_send_can_packet(BYTE type,DWORD id, int data_len, BYTE* data ); //TODO: Check function name


#endif /*FLEX_CAN_COMMAND_H*/