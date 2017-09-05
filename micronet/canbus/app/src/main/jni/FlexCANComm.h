//
// Created by eemaan.siddiqi on 3/3/2017.
//
#include "can.h"

#ifndef CANBUS_FLEXCANCOMM_H
#define CANBUS_FLEXCANCOMM_H
static int CAN1_TTY_NUMBER= 2;
static int CAN2_TTY_NUMBER= 3;
static int J1708_TTY_NUMBER= 4;
#define MAX_MASK_FILTER_SIZE 11

int serial_init(char *name);

int initTerminalInterface(int fd);
int closeTerminalInterface(int port);
int closeCanPort(int portNumber);
int closeCAN(int close_fd);
int setBitrate(int fd, int speed);
int openCANandSetTermination(int fd, bool term);
int setListeningMode(int fd, bool term);
int sendReadStatusCommand(int fd);
int setFd(int portNumber);

int setMasks(char *mask, char type, int fd);
int setFilters( char *filter, char type, int fd);
void setFlowControlMessage(char type,char *searchID,char *responseID, int dataLength, BYTE* dataBytes, int fd );

int serial_start_monitor_thread_can_port1();
int serial_start_monitor_thread_can_port2();
int serial_send_data(unsigned char*, uint32_t bytesToWrite, int fd);
int sendMessage(int fd_port, const char * message);

int serial_start_monitor_thread_can_port1();
int serial_start_monitor_thread_can_port2();

static void *monitor_data_thread_port1(void *param);
static void *monitor_data_thread_can_port2(void *param);

int waitForData(int port_fd);
int parseHex(uint8_t * asciiString, int len, uint8_t * hexValue);

void sendCanbusFramePort1(uint32_t frameId, int type, int length, BYTE* data );
void sendCanbusFramePort2(uint32_t frameId, int type, int length, BYTE* data);

int closeInterfaceCAN1();
int closeInterfaceCAN2();
int serial_deinit_thread_port1();
int serial_deinit_thread_port2();
int serial_set_nonblocking(int fd);




#endif //CANBUS_FLEXCANCOMM_H
