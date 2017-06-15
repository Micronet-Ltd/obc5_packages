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

char getCharType(uint32_t type){
    char typeChar=0;
    if(type==0){typeChar='t';}
    else if(type==1){typeChar='T';}
    return typeChar;
}

void setFilterAndMasks(FLEXCAN_filter_mask *filter_array, int numfilter, int port_fd){
    int i=0;
    uint32_t filterId=0;
    char filterIdString[MAX_MASK_FILTER_SIZE]={0};
    int filterSetCount=0;
    int maskSetCount=0;

    uint8_t filterMaskType=0;
    char filterMaskTypeChar=0;

    uint32_t maskId=0;
    char maskIdString[MAX_MASK_FILTER_SIZE]={0};

    struct FLEXCAN_filter_mask tmp_filter={ .mask_id = {0},
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
            filterMaskTypeChar=getCharType(filterMaskType);

            filterId=tmp_filter.filter_id[index];
            if(filterMaskTypeChar=='T'){
                sprintf ( (char*)filterIdString, "%08x", filterId);
            } else if(filterMaskTypeChar=='t'){
                sprintf ((char*)filterIdString, "%03x", filterId);
            }
            if(filterSetCount<tmp_filter.filter_count || filterSetCount<=24) {
                setFilters(filterIdString, filterMaskTypeChar,port_fd);
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
                setMasks(maskIdString, filterMaskTypeChar,port_fd);
                maskSetCount++;
                usleep(5000);
                LOGD("Mask Set, No of masks set = %d", maskSetCount);
            }
        }filterMaskTypeChar=0;
}

void configureFlowControl(FLEXCAN_Flow_Control *configuration_array, int numfilter, int port_fd){

   struct FLEXCAN_Flow_Control tmp_flow_control={
           .search_id = {0},
           .search_id_count = 0,
           .response_id = {0},
           .response_id_count = 0,
           .flow_msg_type = {0},
           .flow_msg_type_count = 0,
           .flow_msg_data_length = {0},
           .flow_msg_data_length_count = 0,
           .response_data_bytes1 = {0},
           .response_data_bytes2 = {0},
           .response_data_bytes3 = {0},
           .response_data_bytes4 = {0},
           .response_data_bytes5 = {0},
           .response_data_bytes6 = {0},
           .response_data_bytes7 = {0},
           .response_data_bytes8 = {0},
           .response_databytes1_count = 0,
           .response_databytes2_count = 0,
           .response_databytes3_count = 0,
           .response_databytes4_count = 0,
           .response_databytes5_count = 0,
           .response_databytes6_count = 0,
           .response_databytes7_count = 0,
           .response_databytes8_count = 0
   };

    int i=0, j=0;
    int flowCodeSetCount=0;

    uint8_t flowMessageType=0;
    char flowMessageTypeChar=0;

    uint32_t searchId=0;
    char *searchIdString = new char[MAX_FlexCAN_Flowcontrol_CAN];

    uint32_t responseId=0;
    char *responseIdString = new char[MAX_FlexCAN_Flowcontrol_CAN];

    int dataLength=0;

    BYTE dataBytes[8]={0,0,0,0,0,0,0,0};

    tmp_flow_control = *configuration_array;

    for(int index=0; index<tmp_flow_control.search_id_count;index++){
        BYTE firstElement = dataBytes[0];
        //Retrieving message type
        flowMessageType=tmp_flow_control.flow_msg_type[index];
        flowMessageTypeChar= getCharType(flowMessageType);


        //Retrieving response ids
        responseId=tmp_flow_control.response_id[index];
        if(flowMessageTypeChar=='T'){
            sprintf ( (char*)responseIdString, "%08x", responseId);
        } else if(flowMessageTypeChar=='t'){
            sprintf ((char*)responseIdString, "%03x", responseId);
        }

        //Retrieving search ids
        searchId=tmp_flow_control.search_id[index];
        if(flowMessageTypeChar=='T'){
            sprintf ((char*)searchIdString, "%08x", searchId);
        } else if(flowMessageTypeChar=='t'){
            sprintf ((char*)searchIdString, "%03x", searchId);
        }

        //Retrieving the dataLength
        dataLength=tmp_flow_control.flow_msg_data_length[index];

      //Retrieving data bytes
        if(index==0){
            for(int i=0; i<dataLength; i++){
                dataBytes[i]= tmp_flow_control.response_data_bytes1[j++];
            }
            j=0;
        }
        else if(index==1){
            for(int i=0; i<dataLength; i++){
                dataBytes[i]= tmp_flow_control.response_data_bytes2[j++];
            }
            j=0;
        }
        else if(index==2){
            for(int i=0; i<dataLength; i++){
                dataBytes[i]= tmp_flow_control.response_data_bytes3[j++];
            }
            j=0;
        }
        else if(index==3){
            for(int i=0; i<dataLength; i++){
                dataBytes[i]= tmp_flow_control.response_data_bytes4[j++];
            }
            j=0;
        }
        else if(index==4){
            for(int i=0; i<dataLength; i++){
                dataBytes[i]= tmp_flow_control.response_data_bytes5[j++];
            }
            j=0;
        }

        else if(index==5){
            for(int i=0; i<dataLength; i++){
                dataBytes[i]= tmp_flow_control.response_data_bytes6[j++];
            }
            j=0;
        }
        else if(index==6){
            for(int i=0; i<dataLength; i++){
                dataBytes[i]= tmp_flow_control.response_data_bytes7[j++];
            }
            j=0;
        }
        else if(index==7){
            for(int i=0; i<dataLength; i++){
                dataBytes[i]= tmp_flow_control.response_data_bytes8[j++];
            }
            j=0;
        }

        if((flowCodeSetCount <= tmp_flow_control.flow_msg_type_count) && (flowCodeSetCount<=8)) {
            setFlowControlMessage(flowMessageTypeChar, searchIdString, responseIdString, dataLength,dataBytes, port_fd);
            memset(dataBytes,0, sizeof dataBytes);
            usleep(5000);
            flowCodeSetCount++;
            LOGD("Flow control message set, No of filters set = %d", flowCodeSetCount);
        }
    }
}

int FlexCAN_j1708_startup(){
    return -1;
}


int FlexCAN_startup(bool listeningModeEnable, int bitrate, int termination, FLEXCAN_filter_mask* filter_array,int numfilter, char *portName, FLEXCAN_Flow_Control* flexcan_flow_control,int numOfFlowMessages)
{
    int i=0, ret=-1;
    char *port = portName;
    int fd=-1;

    fd = ret = serial_init(port);

    if (initTerminalInterface(fd) == -1) {
        return -1;
    }

    /* first always close1939Port1 the CAN module (bug #250)
     http://192.168.1.234/redmine/issues/250
     */
    if (closeCAN(fd) == -1) {
        return -1;
    }
   /*
    *The firmware has a 20ms delay after closing the port.
    */
    usleep(100000);

    setFilterAndMasks(filter_array, numfilter,fd);

    configureFlowControl(flexcan_flow_control, numOfFlowMessages,fd);

    if(strcmp(portName, CAN1_TTY)==0){
        if(serial_start_monitor_thread_can_port1())
        {
            LOGE("unable to start serial monitor thread__port1\n");
            return -1;
        }
    }
    else if(strcmp(portName, CAN2_TTY)==0){
        if(serial_start_monitor_thread_can_port2())
        {
            LOGE("unable to start serial monitor thread__port2\n");
            return -1;
        }
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

void FlexCAN_send_can_packet(BYTE type, DWORD id, int data_len, BYTE *data, int portNumber) {
    int index = 0, i = 0;
    int fd=-1;
    uint8_t canPacketToTx[MAX_PACKET_SIZE] = {0};
    int msgLength = 0;
    fd=setFd(portNumber);

    msgLength = ParseCanMessToString(type, id, data_len, data, canPacketToTx);

    if( serial_send_data(canPacketToTx, msgLength, fd)){
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
