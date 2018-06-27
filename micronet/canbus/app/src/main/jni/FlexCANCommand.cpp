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
/**
 * Cofigures upto 24 filters and 16 masks for the Can ports
 * Filter and Mask Format:
 * ----------------------------------------------------------------------------------
 * Filters
 * M<t/T/r/R>iiiiiiii[CR]
 * iiiiiiii - Acceptable code for receive.
 * Accepted IDs Range in Hex = [0x00000000 - 0x1FFFFFFF]
 * T - Extended Filter
 * t - Standard Filter
 * R - Extended Remote Filter
 * r - Remote Standard Filter
 * ----------------------------------------------------------------------------------
 * Masks
 * m<t/T/r/R>iiiiiiii[CR]
 * iiiiiiii - Acceptable mask for receive.
 * Accepted IDs Range in Hex = [0x00000000 - 0x1FFFFFFF]
 * T - Extended Mask
 * t - Standard Mask
 * R - Extended Remote Mask
 * r - Remote Standard Mask
 * ---------------------------------------------------------------------------------
 *
 * */
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

        tmp_filter = *filter_array;

        for(int index=0; index < tmp_filter.filter_count; index++){

            filterMaskType = tmp_filter.filter_mask_type[index];
            filterMaskTypeChar = getCharType(filterMaskType);

            filterId = tmp_filter.filter_id[index];
            if(filterMaskTypeChar == 'T'){
                sprintf ((char*)filterIdString, "%08x", filterId);
            }
            else if(filterMaskTypeChar=='t'){
                sprintf ((char*)filterIdString, "%03x", filterId);
            }

            if(filterSetCount < tmp_filter.filter_count || filterSetCount <= 24){
                setFilters(filterIdString, filterMaskTypeChar,port_fd);
                usleep(5000);
                filterSetCount++;
                LOGD("Filter Set, No of filters set = %d", filterSetCount);
            }

            maskId = tmp_filter.mask_id[index];
            if(filterMaskTypeChar == 'T'){
                sprintf ( (char*)maskIdString, "%08x", maskId);
            }
            else if(filterMaskTypeChar=='t'){
                sprintf ((char*)maskIdString, "%03x", maskId);
            }
            if(maskSetCount<tmp_filter.mask_count && maskSetCount<=16){
                setMasks(maskIdString, filterMaskTypeChar,port_fd);
                maskSetCount++;
                usleep(5000);
                LOGD("Mask Set, No of masks set = %d", maskSetCount);
            }
        }
    filterMaskTypeChar=0;
}

void configureFlowControl( struct FLEXCAN_Flow_Control *configuration_array, int numFlowCodes, int port_fd){

    int count = 0;
    int flowCodeSetCount=0;
    char flowMessageTypeChar='\0';
    char *searchIdString = new char[MAX_FlexCAN_Flowcontrol_CAN];
    char *responseIdString = new char[MAX_FlexCAN_Flowcontrol_CAN];
    BYTE dataBytes[8]={0,0,0,0,0,0,0,0};

    for(count = 0; count < numFlowCodes; count++ ){
        struct FLEXCAN_Flow_Control tmp_flow_control={
                .search_id = {0},
                .response_id = {0},
                .flow_msg_type = {0},
                .flow_msg_data_length = {0},
                .response_data_bytes = {0},
        };

        tmp_flow_control = configuration_array[count];

        flowMessageTypeChar = getCharType(tmp_flow_control.flow_msg_type);

        if(flowMessageTypeChar=='T'){
            // Parse Search Id
            sprintf ((char*)searchIdString, "%08x", tmp_flow_control.search_id);
            //Parse Response Id
            sprintf ((char*)responseIdString, "%08x", tmp_flow_control.response_id);
        } else if(flowMessageTypeChar=='t'){
            // Parse Search Id
            sprintf ((char*)searchIdString, "%03x", tmp_flow_control.search_id);
            //Parse Response Id
            sprintf ((char*)responseIdString, "%03x", tmp_flow_control.response_id);
        }

        for(int j = 0; j < 8; j++){
            LOGD("Flow messgae %d, Sturcture dataBytes[%d] = %x", count , j, tmp_flow_control.response_data_bytes[j]);
        }

        //Workaround to fix compile time error
        dataBytes[0] = 0;
        for(int index = 0; index < tmp_flow_control.flow_msg_data_length; index++){
            LOGD("Flow message %d, Old data bytes %x to data bytes from structure = %x", index, dataBytes[index], tmp_flow_control.response_data_bytes[index] );
            dataBytes[index] = tmp_flow_control.response_data_bytes[index];
            LOGD("Flow messgae %d, dataBytes[%d] = %x", count , index, dataBytes[index]);
        }

        if((flowCodeSetCount <= numFlowCodes) && (flowCodeSetCount<=8)) {
            setFlowControlMessage(flowMessageTypeChar, searchIdString, responseIdString, tmp_flow_control.flow_msg_data_length,dataBytes, port_fd);
            memset(dataBytes,0, sizeof dataBytes);
            usleep(5000);
            flowCodeSetCount++;
            LOGD("Flow control message set, No of filters set = %d", flowCodeSetCount);
        }
    }
}
/**
 * Creates and configures a J1708 Interface for communication
 * J1708 and CAN1 have the same GPIO pin for power.
 * CAN1 has a higher priority over J1708.
 * Closing the interface for CAN1 would affect J1708.
 * */
int FlexCAN_j1708_startup(char *portName){

    int i=0, ret_read = -1;
    char *port = portName;
    int fd_read = -1, fd_write = -1;

    fd_read = ret_read = serial_init(J1708_TTY_READ);

    fd_write = serial_init(J1708_TTY_WRITE);

    //TODO: Check if CAN 1 is enabled, it isn't then enable power to the GPIO pin
    //If CAN1 is open, don't close the port continue reading. If its closed, open it.
    if(getFd(CAN1_TTY_NUMBER) == -1){
        system("mctl api 0213020001");
        //The firmware has a 20ms delay after opening the port.
        usleep(20000);
    }

    if(serial_start_monitor_thread_j1708()){
        LOGE("Unable to start serial monitor thread__port1708\n");
        return -1;
    }

    return ret_read;
}

/**
 * Creates and configures a Can Interface for communication
 * This configures the tty ports does all the following:
 *     a. Sets Filters and Masks, Max filters = 24, Max masks = 16.
 *     b. Set upto 8 flow control codes
 *     c. Sets the can baud rate
 *     d. Configures can termination [Enable/Disable]
 *     e. Configures the control mode [Listening?]
 *     f. Opens Can channel for communication
 */
int FlexCAN_startup(bool listeningModeEnable, int bitrate, int termination, FLEXCAN_filter_mask* filter_array,int numfilter, char *portName, FLEXCAN_Flow_Control flexcan_flow_control[],int numOfFlowMessages)
{
    int i=0, ret=-1;
    char *port = portName;
    int fd= -1;

    fd = ret = serial_init(port);

    if (initTerminalInterface(fd, B9600) == -1) {
        return -1;
    }

    /**
    * First always close1939Port the CAN module (bug #250)
    * http://192.168.1.234/redmine/issues/250/
    */
    if (closeCAN(fd) == -1) {
        return -1;
    }

    /**
     * The firmware has a 100ms delay after closing the port.
     */
    usleep(100000);

    /**
     * Configuring CAN Baud rate
     * The baud rate must be set after closing the CAN module.
     */
    if(setBitrate(fd, bitrate) == -1) {
        return -1;
    }

    /**
     * Configuring Filter List
     */
    setFilterAndMasks(filter_array, numfilter,fd);

    /**
     * The firmware has a 25ms delay between configuring filters and flow control codes
     */
    usleep(25000);

    /**
     * Configure Flow Control
     */
    configureFlowControl(flexcan_flow_control, numOfFlowMessages,fd);

    if(strcmp(portName, CAN1_TTY) == 0){
        if(serial_start_monitor_thread_can_port1())
        {
            LOGE("unable to start serial monitor thread__port1\n");
            return -1;
        }
    }
    else if(strcmp(portName, CAN2_TTY) == 0){
        if(serial_start_monitor_thread_can_port2())
        {
            LOGE("unable to start serial monitor thread__port2\n");
            return -1;
        }
    }

    /**
     * Open CAN interface for communication and enable/disable termination
     */
    if(listeningModeEnable){
        if(setListeningModeandTerm(fd, termination) == -1){
            return -1;
        }
    }
    else {
        if(openCANandSetTerm(fd, termination) == -1){
            return -1;
        }
    }

    /**
     * 100ms delay after CAN channel is opened.
     */
    usleep(100000);

   /**
    * TODO: Research more about me.
    * if(sendReadStatusCommand(fd) == -1) {
    *    return -1;
    * }
    */
    return ret;
}

/**
 * Converts the Canbus frame to a string build building a message that can be sent to the mcu.
 * Message Formats
 * -------------------------------------------------------------------------------------------
 * Extended Frame: TiiiiiiiiLdd..[CR]
 * iiiiiiii - Identifier,  IdRange = [0x00000000 - 0x1FFFFFFF]
 * L - Number of data bytes [length], Range = [0-8]
 * dd - Byte value in Hex, Max number of dd pairs = 8
 * Note: The number of dd pairs must match the number of dd pairs.
 * -------------------------------------------------------------------------------------------
 * Standard Frame: tiiildd..[CR]
 * iii - IdRange = [0x000 - 0x7FF]
 * L - Number of data bytes [length], Range = [0-8]
 * dd - Byte value in Hex, Max number of dd pairs = 8
 * Note: The number of dd pairs must match the number of dd pairs.
 * -------------------------------------------------------------------------------------------
 * Extended Remote: Riiiiiiii[CR]
 * iiiiiiii - Identifier,  IdRange = [0x00000000 - 0x1FFFFFFF]
 * -------------------------------------------------------------------------------------------
 * Standard Remote: riii[CR]
 * iii - IdRange = [0x000 - 0x7FF]
 * ------------------------------------------------------------------------------------------
 * This returns the length of the formatted string
 *
 */
int32_t ParseCanMessToString(int msg_type, int id, int data_len, BYTE * data, uint8_t * pDestBuff){

    uint8_t tmp1, ind, *pmsg_str, curr_msg_len = 0;

    if (NULL == data || NULL == pDestBuff) {
        LOGD("%s: Error wrong params\n", __func__);
        return -1;
    }

    pmsg_str = (uint8_t*)pDestBuff;

    //The first byte defines the message type
    // T - Extended CAN Frame
    // t - Standard CAN Frame
    // R - Remote Extended Frame
    // r - Standard Remote Frame

    if(msg_type==EXTENDED){
        *pmsg_str = 'T';
    }
    else if(msg_type==STANDARD){
        *pmsg_str = 't';
    }
    else if (msg_type==EXTENDED_REMOTE){
        *pmsg_str = 'R';
    }
    else if (msg_type==STANDARD_REMOTE){
        *pmsg_str = 'r';
    }

    pmsg_str++;
    curr_msg_len++;

    //Append the CAN Id to the string [Hex values as characters]
    if ((msg_type == EXTENDED) | (msg_type == EXTENDED_REMOTE)) {
        // Extended / Extended Remote Identifier
        sprintf ( (char*)pmsg_str, "%08x", id);
        pmsg_str += 8;
        curr_msg_len += 8;
    }
    else if ((msg_type == STANDARD) || (msg_type == STANDARD_REMOTE)){
        // Standard / Standard Remote Identifier
        sprintf ( (char*)pmsg_str, "%03x", id);
        pmsg_str += 3;
        curr_msg_len += 3;
    }

    //Append the Message length
    *pmsg_str = data_len + '0';
    pmsg_str++;
    curr_msg_len ++;

    // Append the data bytes to the frame
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

    //Add a CAN_OK_RESPONSE character
    *pmsg_str = CAN_OK_RESPONSE;

    curr_msg_len++;

    return (int32_t)curr_msg_len;
}

/**
 * Sends a Canbus frame to the Can ports.
 * */
void FlexCAN_send_can_packet(BYTE type, DWORD id, int data_len, BYTE *data, int portNumber) {
    int i = 0;
    int fd=-1;
    uint8_t canPacketToTx[MAX_PACKET_SIZE] = {0};
    int msgLength = 0;
    fd = getFd(portNumber);

    msgLength = ParseCanMessToString(type, id, data_len, data, canPacketToTx);

    if( serial_send_data(canPacketToTx, msgLength, fd)){
        error_message("!!!!!!!!!!!!!!! Couldn't send FLEXCAN CAN message !!!!!!!!!!!!!!!!!");
        return;
    }
}

int computeJ1708Checksum(int id, int priority, BYTE *dataBytes, int dataLength){
    int  sum = 0, checksum = 0;

    sum = sum + id + priority;
    for (int i = 0; i < dataLength; i++){
        sum = sum + dataBytes[i];
    }

    checksum = (255 - (sum % 256)) + 1 ;

    LOGD("Returned Checksum = % d", checksum);

    return checksum;
}

/**
 * Sends a J1708 frame to the 1708 port
 * */
void FlexCAN_send_j1708_packet(DWORD id, BYTE *data, BYTE priority, int dataLength)
{
    int index = 0, i = 0, fd_write = -1;
    unsigned char packet[dataLength+5]; //={'\0'};

    fd_write = getFd(J1708_TTY_WRITE_NUMBER);

    packet[index++] = 0x7E;
    packet[index++] = priority;
    packet[index++] = (BYTE)(id);

    for (i = 0; i < dataLength; i++){
        packet[index + i] = data[i];
    }
    index += i;

    packet[index++] = computeJ1708Checksum((BYTE) (id), priority, data, dataLength);
    packet[index++]=0x7E;

    if( serial_send_data(packet, dataLength + 5, fd_write)){
        error_message("!!!!!!!!!!!!!!! Couldn't transmit 1708 message !!!!!!!!!!!!!!!!!");
        return;
    }
}
