//
// Created by eemaan.siddiqi on 2/24/2017.
//
#define LOG_TAG "Canbus"
#include <jni.h>
#include <pthread.h>
#include <semaphore.h>
#include <android/log.h>
#include <string>
#include "canbus.h"
#include "FlexCANcomm.h"

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
/*char parseASCII(uint8_t *hexData, int length, char *stringValue){
    int index=0;
    *stringValue=0;
    uint8_t tmp1;
    if (length & 1){
        LOGE("Received Invalid frame for Parsing: odd length");
        return -1;
    }
    if (length==0){
        LOGE("Received Invalid frame for Parsing: Null ");
        return -1;
    }

    for (index = 0; index < length; index++) {
        tmp1=(hexData[index]>>4) &0xF;
        if(tmp1>9)
            *stringValue=tmp1-10+'A';
        else
            *stringValue=tmp1+'0';
        stringValue++;
        tmp1=(hexData[index]) & (0xF);
        if (tmp1>9)
            *stringValue==tmp1-10+'A';
        else
            *stringValue=tmp1+'0';
        }
    return 0;
}*/

int32_t ParseCanMessToString(int msg_type, int id, int data_len, BYTE * data, uint8_t * pDestBuff){
//t ParseCanMessToString(pFLEXCAN_queue_element_t pCanMess, const uint8_t *pDestBuff) {
    uint8_t   tmp, tmp1, ind, *pmsg_str, curr_msg_len = 0;

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

    for (ind = 0; ind < data_len; ind++) {
        tmp1 = (data[ind]>>4) & 0xF;
        if (tmp1 > 9 )
            *pmsg_str = tmp1 - 10 + 'A';
        else
            *pmsg_str = tmp1 + '0';

        pmsg_str++;
        curr_msg_len++;
        tmp1 = data[ind] & 0xF;
        if (tmp1 > 9 )
            *pmsg_str = tmp1 - 10 + 'A';
        else
            *pmsg_str = tmp1 + '0';

        pmsg_str++;
        curr_msg_len++;
    }

    //Message time stamp
 /*   pmsg_str += 3;
    for (ind = 0; ind < 4; ind++) {
        tmp1 = (uint8_t)((pCanMess->msg_buff.cs >> (ind<<2)) & 0xF);
        if (tmp1 > 9 )
            *pmsg_str = tmp1 - 10 + 'A';
        else
            *pmsg_str = tmp1 + '0';

        pmsg_str--;
    }
    pmsg_str += 5;
    curr_msg_len += 5;
*/
    //Add CAN_OK_RESPONCE character
    *pmsg_str = CAN_OK_RESPONCE;

    return (int32_t)curr_msg_len;
}

/*static void FlexCAN_send_message(BYTE type, uint8_t *data,*//* unsigned char command,*//* int len)
{

    BYTE CanMessagetoASCIIString[MAX_PACKET_SIZE];
    int index=0,i=0;
    int packetLength=strlen((const char *) data);
    BYTE *packetInAscii;

    parseASCII(data, packetLength, (char *) packetInAscii);

    if(type==EXTENDED){
        CanMessagetoASCIIString[index++]='T';
    }
    else if(type==STANDARD){
        CanMessagetoASCIIString[index++]='t';
    }
    else if (type==EXTENDED_REMOTE){
        CanMessagetoASCIIString[index++]='R';
    }
    else if (type==STANDARD_REMOTE){
        CanMessagetoASCIIString[index++]='r';
    }
    for(int i=0; i < packetLength; i++) {
            CanMessagetoASCIIString[index + i] = packetInAscii[i];
    }
    int total_len=index+1;
    //  TODO: Send  CanMessagetoASCIIString to the port
    if( serial_send_data(CanMessagetoASCIIString, total_len)){
        error_message("!!!!!!!!!!!!!!! Couldn't send FLEXCAN CAN message !!!!!!!!!!!!!!!!!");
        return;
    }
}*/

void FlexCAN_send_can_packet(BYTE type, DWORD id, int data_len, BYTE *data) {
    int index = 0, i = 0;
/*    int dataSize = sizeof(data);
    int idSize = sizeof(id);*/
    uint8_t canPacketToTx[MAX_PACKET_SIZE] = {0};
    int msgLength = 0;

    msgLength = ParseCanMessToString(type, id, data_len, data, canPacketToTx);

    if( serial_send_data(canPacketToTx, msgLength)){
        error_message("!!!!!!!!!!!!!!! Couldn't send FLEXCAN CAN message !!!!!!!!!!!!!!!!!");
        return;
    }

/*    if (((type == EXTENDED)) || ((type == STANDARD)) || ((type == EXTENDED_REMOTE)) || ((type == STANDARD_REMOTE)) || ((0 >= data_len <= 8)) || (((idSize == CAN_MSG_ID_SIZE_STD)) || ((idSize == CAN_MSG_ID_SIZE_EXT))) || ((dataSize = 2 * data_len))){
        //canPacketToTx[index++] = type; //CAN packet type (1=extended, 0=standard)
      *//*  canPacketToTx[index++] = (BYTE) ((id >> 0) & 0xff);*//*
        canPacketToTx[index++] = (BYTE) ((id >> 8) & 0xff);
        canPacketToTx[index++] = (BYTE) ((id >> 0) & 0xff);

        if ((type == EXTENDED)||(type == EXTENDED_REMOTE) ) {
            canPacketToTx[index++] = (BYTE) ((id >> 16) & 0xff);
            canPacketToTx[index++] = (BYTE) ((id >> 24) & 0xff);
        }
        canPacketToTx[index++] =data_len;

        if((type=EXTENDED)||(type=STANDARD)) {
            for (i = 0; i < data_len; i++) {
                canPacketToTx[index + i] = data[i];
            }
        }
        canPacketToTx[index+1]=0xD;
        index += i+1;

        FlexCAN_send_message(type, canPacketToTx, index);
    }

        else LOGD("Invalid Argument (Type,id,data_len,data):  \n" + type, id, data_len, data);
*/
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
