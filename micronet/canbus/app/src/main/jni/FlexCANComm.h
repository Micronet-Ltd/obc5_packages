//
// Created by eemaan.siddiqi on 3/3/2017.
//
#include "can.h"

#ifndef CANBUS_FLEXCANCOMM_H
#define CANBUS_FLEXCANCOMM_H
#define MAX_MASK_FILTER_SIZE 11

int serial_init(char *name);

int initTerminalInterface(int fd);
int closeCAN(int close_fd);
int setBitrate(int fd, int speed);
int openCANandSetTermination(int fd, bool term);
int setListeningMode(int fd, bool term);
int sendReadStatusCommand(int fd);

int setMasks(char *mask, char type, int fd);
int setFilters( char *filter, char type, int fd);
void setFlowControlMessage(char type,char *searchID,char *responseID, int dataLength, BYTE* dataBytes, int fd );

int serial_start_monitor_thread();
int serial_send_data(unsigned char*, uint32_t);
int sendMessage(int fd_port, const char * message);

static void *monitor_data_thread(void *param);
int wait_for_data(int port_fd);
int parseHex(uint8_t * asciiString, int len, uint8_t * hexValue);


int qb_close();
int serial_deinit();
int serial_set_nonblocking(int fd);




#endif //CANBUS_FLEXCANCOMM_H
