/*
 Created by eemaan.siddiqi on 2/11/2017.
*/

#ifndef CAN_H
#define CAN_H

#include <stdint.h>
#include <errno.h>
#include <termios.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <sys/poll.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <linux/types.h>
#include <linux/netlink.h>
#include <android/log.h>

#define error_message ERR
#define DWORD uint32_t
#define WORD uint16_t
#define BYTE uint8_t
#define BOOL int

typedef union _J1939_ID {
    struct {
        DWORD src_addr:8;  //source address
        DWORD PS
                : 8;       //PDU specific (if PF is addressable, this contains the destination address
        DWORD PF
                : 8;       //PDU format 0-239 (PDU1) message is addressable, 240-255 (PDU2) message is broadcast
        DWORD dp: 1;       //data page
        DWORD reserved: 1; //unused/reserved bit
        DWORD priority: 3; //lower number is higher priority
        DWORD notused
                : 3;  //extended can ID is only 29 bits, so there are 3 unused bits in a 4 byte value
    } pgn_bits;
    DWORD dwVal;         //some times it is useful to access it all at once
}J1939_ID;
#endif //CAN_H