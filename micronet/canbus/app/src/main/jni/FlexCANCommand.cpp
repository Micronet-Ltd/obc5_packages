//
// Created by eemaan.siddiqi on 2/24/2017.
//
#define LOG_TAG "Canbus"
#include <jni.h>

#include "canbus.h"
#include "FlexCANcomm.h"
#include "FlexCANCommand.h"
#include <string>

#define CAN_OK_RESPONCE 	0x0D
#define CAN_ERROR_RESPONCE	0x07
#define FLOW_CONTROL_ARR_SIZE 0x8
#define FLOW_CONTROL_INVALID_POS 0xFF
#define FLOW_CONTROL_INVALID_ID 0x0
#define CAN_MSG_ID_SIZE_STD 3
#define CAN_MSG_ID_SIZE_EXT 8

#define COMMAND_J1708_PACKET 'D'
#define COMMAND_CAN_PACKET 'J'
#define MAX_PACKET_SIZE 256


static const char* qb_str_command(char command)
{
    switch (command)
    {
        case COMMAND_CAN_PACKET:
            return "COMMAND_CAN_PACKET";
        default:
            return "UNKNOWN COMMAND !!";
    }

}

int FlexCAN_startup(bool listeningModeEnable, int bitrate, int termination)
{
    int i, ret;
    char *port = NULL;
    int fd;

    fd = ret = serial_init(port);

    if(serial_start_monitor_thread())
    {
        LOGE("unable to start serial monitor thread\n");
        return -1;
    }
    if (initTerminalInterface(fd) == -1) {
        return -1;
    }
    /* first always close the CAN module (bug #250)
     http://192.168.1.234/redmine/issues/250
     */
    if (closeCAN(fd) == -1) {
        return -1;
    }

    //TODO: Add masks

    if(setBitrate(fd, bitrate) == -1) {
        return -1;
    }

    if(sendReadStatusCommand(fd) == -1) {
        return -1;
    }


    /*
     * Test listening mode:
     *  Open CAN in listening mode with termination true: 	./slcan_tty -l0 -f -s6 /dev/ttyACM2
     *  Try to send message. Verify that message is not received: ./slcan_tty -t7003112233 /dev/ttyACM2
     *  Close CAN: ./slcan_tty -c /dev/ttyACM2
     *
     *  Open CAN in termination mode true: ./slcan_tty -o1 -f –s6 /dev/ttyACM2
     *  Try to send message. Verity that the message is received by other device: ./slcan_tty -t7003112233 /dev/ttyACM2
     *  Close CAN: ./slcan_tty -c /dev/ttyACM2
     *
     *
     *  L	O	Extended LO	Actual Command
        1	0	L1O0	L0
        1	1	L1O1	L1
        0	0	L0O0	0
        0	1	L0O1	O1

     */
    if(listeningModeEnable && setListeningMode(fd, termination) == -1) { // enable listening mode and set the termination value
        return -1;
    } else if(openCANandSetTermination(fd, termination) == -1) { // set the termination value and (disable listening mode?) when opening CAN.
        // TODO: check if running "openCANandSetTermination" disables listening mode
        return -1;
    }
/*    const char * mesg = "7003112233";
    if(sendMessage(fd, mesg) == -1) {
        return -1;
    }*/

    return ret;
}

int32_t ParseCanMessToString(int msg_type, int id, int data_len, BYTE * data, uint8_t * pDestBuff){
    uint8_t tmp1, ind, *pmsg_str, curr_msg_len = 0;

    //TODO: deal with remote frames

    if (NULL == data || NULL == pDestBuff) {
        LOGD("%s: Error wrong params\n", __func__);
        return -1;
    }

    pmsg_str = (uint8_t*)pDestBuff;

    if(msg_type==EXTENDED){
        *pmsg_str='T';
    }
    else if(msg_type==STANDARD){
        *pmsg_str='t';
    }
    else if (msg_type==EXTENDED_REMOTE){
        *pmsg_str='R';
    }
    else if (msg_type==STANDARD_REMOTE){
        *pmsg_str='r';
    }
    pmsg_str++;
    curr_msg_len++;

    //set message ID;
    if ((msg_type==EXTENDED) | (msg_type==EXTENDED_REMOTE)) {
        //Extended
        sprintf ( (char*)pmsg_str, "%08x", id);
        pmsg_str += 8;
        curr_msg_len += 8;
    }
    else if ((msg_type == STANDARD) || (msg_type == STANDARD_REMOTE)){
        //Standard
        sprintf ( (char*)pmsg_str, "%03x", id);
        pmsg_str += 3;
        curr_msg_len += 3;
    }

    //Message length
    *pmsg_str = data_len + '0';
    pmsg_str++;
    curr_msg_len ++;

    if ((msg_type==EXTENDED) || (msg_type==STANDARD)) {
        for (ind = 0; ind < data_len; ind++) {
            tmp1 = (data[ind] >> 4) & 0xF;
            if (tmp1 > 9)
                *pmsg_str = tmp1 - 10 + 'A';
            else
                *pmsg_str = tmp1 + '0';

            pmsg_str++;
            curr_msg_len++;

            tmp1 = data[ind] & 0xF;
            if (tmp1 > 9)
                *pmsg_str = tmp1 - 10 + 'A';
            else
                *pmsg_str = tmp1 + '0';

            pmsg_str++;
            curr_msg_len++;
        }
    }
    //Add CAN_OK_RESPONCE character
    *pmsg_str = CAN_OK_RESPONCE;

    curr_msg_len++;

    return (int32_t)curr_msg_len;
}

void FlexCAN_send_can_packet(BYTE type, DWORD id, int data_len, BYTE *data) {
    int index = 0, i = 0;
    uint8_t canPacketToTx[MAX_PACKET_SIZE] = {0};
    int msgLength = 0;

    msgLength = ParseCanMessToString(type, id, data_len, data, canPacketToTx);

    if( serial_send_data(canPacketToTx, msgLength)){
        error_message("!!!!!!!!!!!!!!! Couldn't send FLEXCAN CAN message !!!!!!!!!!!!!!!!!");
        return;
    }
}

/*void qb_send_j1708_packet(DWORD id, BYTE* data, BYTE priority, int data_len)
{
    int index = 0, i = 0;
    unsigned char packet[MAX_PACKET_SIZE];

    packet[index++] = priority;
    packet[index++] = (BYTE)(id);

    for (i = 0; i < data_len; i++){
        packet[index + i] = data[i];
    }

    index += i;

    *//*FlexCAN_send_message(packet, COMMAND_J1708_PACKET, index);*//*
}*/
