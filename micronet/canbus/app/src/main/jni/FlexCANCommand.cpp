//
// Created by eemaan.siddiqi on 2/24/2017.
//
#define LOG_TAG "Canbus"
#include <jni.h>

#include "canbus.h"
#include "FlexCANComm.h"
#include "FlexCANCommand.h"
#include <stdlib.h>
#include <stdio.h>

#define MAX_PACKET_SIZE 256

/*int numberOfFilters;
int count;

void Flex_CAN_filter_list(struct FLEXCAN_filter_mask* filter_array, int numfilter){

    numberOfFilters=numfilter;
    tmp_filter=filter_array[count];
}*/

char getFilterMaskType(uint32_t type ){
    char typeChar=0;
    if(type==0){typeChar='t';}
    else if(type==1){typeChar='T';}
    return typeChar;
}

void setFilterAndMasks(FLEXCAN_filter_mask *filter_array, int numfilter){
    int i=0;
    uint32_t filterId=0;
    char filterIdString[MAX_MASK_FILTER_SIZE]={0};
    int filterSetCount=0;
    int maskSetCount=0;

    uint8_t filterMaskType=0;
    char filterMaskTypeChar=0;

    uint32_t maskId=0;
    char maskIdString[MAX_MASK_FILTER_SIZE]={0};
    struct FLEXCAN_filter_mask tmp_filter={	.mask_id = {0},
											.mask_count = 0,
											.filter_mask_type = {0},
											.filter_mask_type_count = 0,
											.filter_id = {0},
											.filter_count = 0
   										   };

    LOGD("Start setting filters and masks (loop begins!) Numfilter: %d", numfilter);

        tmp_filter = *filter_array;

        for(int index=0; index<tmp_filter.filter_count;index++){
            LOGD("Entered Loop");

            filterMaskType=tmp_filter.filter_mask_type[index];
            filterMaskTypeChar=getFilterMaskType(filterMaskType);

            filterId=tmp_filter.filter_id[index];
            if(filterMaskTypeChar=='T'){
                sprintf ( (char*)filterIdString, "%08x", filterId);
            } else if(filterMaskTypeChar=='t'){
                sprintf ((char*)filterIdString, "%03x", filterId);
            }
            if(filterSetCount<tmp_filter.filter_count || filterSetCount<=24) {
                setFilters(filterIdString, filterMaskTypeChar);
               usleep(5000);
                filterSetCount++;
                LOGD("Filter Set, No of filters set = %d", filterSetCount);
            }

            maskId=tmp_filter.mask_id[index];
            if(filterMaskTypeChar=='T'){
                sprintf ( (char*)maskIdString, "%08x", maskId);
            } else if(filterMaskTypeChar=='t'){
                sprintf ((char*)maskIdString, "%03x", maskId);
            }
            if(maskSetCount<tmp_filter.mask_count && maskSetCount<=16){
                setMasks(maskIdString, filterMaskTypeChar);
                maskSetCount++;
                usleep(5000);
                LOGD("Mask Set, No of masks set = %d", maskSetCount);
            }
        }filterMaskTypeChar=0;
}

int FlexCAN_startup(bool listeningModeEnable, int bitrate, int termination, FLEXCAN_filter_mask* filter_array,int numfilter, char *portName)
{
    int i, ret;
    char *port = portName;
    int fd;

    fd = ret = serial_init(port);

    if (initTerminalInterface(fd) == -1) {
        return -1;
    }

    /* first always close the CAN module (bug #250)
     http://192.168.1.234/redmine/issues/250
     */
    if (closeCAN(fd) == -1) {
        return -1;
    }
   /*
    *The firmware has a 20ms delay after closing the port.
    * */
    usleep(100000);


    setFilterAndMasks(filter_array, numfilter);

    if(serial_start_monitor_thread())
    {
        LOGE("unable to start serial monitor thread\n");
        return -1;
    }

    if(setBitrate(fd, bitrate) == -1) {
        return -1;
    }

    if(sendReadStatusCommand(fd) == -1) {
        return -1;
    }
    /*
     * The firmware has a 20ms delay after opening the port.
     * */
    usleep(100000);

    if(listeningModeEnable && setListeningMode(fd, termination) == -1) { // enable listening mode and set the termination value
        return -1;
    } else if(openCANandSetTermination(fd, termination) == -1) { // set the termination value and (disable listening mode?) when opening CAN.
        // TODO: check if running "openCANandSetTermination" disables listening mode
        return -1;
    }
    return ret;
}

int32_t ParseCanMessToString(int msg_type, int id, int data_len, BYTE * data, uint8_t * pDestBuff){
    uint8_t tmp1, ind, *pmsg_str, curr_msg_len = 0;

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
    //Add CAN_OK_RESPONSE character
    *pmsg_str = CAN_OK_RESPONSE;

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
