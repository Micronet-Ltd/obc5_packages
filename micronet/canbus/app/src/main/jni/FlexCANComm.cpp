//
// Created by eemaan.siddiqi on 3/3/2017.
//

#include <unistd.h>
#include <sys/prctl.h>
#include <pthread.h>

#include "canbus.h"
#include "FlexCANComm.h"

static int fd_CAN1;
static int fd_CAN2;
static int fd_J1708;

static pthread_t thread__port1;
static pthread_t thread__port2;
static bool quit_port1 = false;
static bool quit_port2 = false;

int serial_set_nonblocking(int fd)
{
    int flags;

    if(-1 == (flags = fcntl(fd, F_GETFL))) {
        LOGE("%s:%d fnctl: %s\n", __FILE__, __LINE__, strerror(errno));
        return -1;
    }

    if(-1 == fcntl(fd, F_SETFL, flags | O_NONBLOCK))
    {
        LOGE("%s:%d: fcntl: %s\n", __FILE__, __LINE__, strerror(errno));
        return -1;
    }
    return 0;
}

int serial_init(char *portName)
{
    DD("opening port: '%s'\n", portName);

    //Initialising CAN1_TTY
    if(strcmp(portName, CAN1_TTY)==0){
        if ((fd_CAN1 = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
            perror(portName);
            exit(EXIT_FAILURE);
        }
            serial_set_nonblocking(fd_CAN1);
            DD("opened port: '%s', fd=%d", CAN1_TTY, fd_CAN1);
            initTerminalInterface(fd_CAN1);
            return fd_CAN1;
        }

        //Initialising CAN2_TTY
    else if(strcmp(portName,CAN2_TTY)== 0){
        if ((fd_CAN2 = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
            perror(portName);
            exit(EXIT_FAILURE);
        }
            serial_set_nonblocking(fd_CAN2);
            DD("opened port: '%s', fd=%d", CAN2_TTY, fd_CAN2);
            initTerminalInterface(fd_CAN2);
        return fd_CAN2;
    }

    //Initialising J1708
    else if(strcmp(portName,J1708_TTY)== 0){
        if ((fd_J1708 = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
            perror(portName);
            exit(EXIT_FAILURE);
        }
        serial_set_nonblocking(fd_J1708);
        DD("opened port: '%s', fd=%d", CAN1_TTY, fd_J1708);
        initTerminalInterface(fd_J1708);
        return fd_J1708;
    }

}

int initTerminalInterface(int fd) {
    if (isatty(fd)) {
        struct termios ios;

        tcgetattr(fd, &ios);
        bzero(&ios, sizeof(ios));

        cfmakeraw(&ios);
        cfsetospeed(&ios, B115200);
        cfsetispeed(&ios, B115200);
        ios.c_cflag = (B115200 | CS8 | CLOCAL | CREAD) & ~(CRTSCTS | CSTOPB | PARENB);
        ios.c_iflag = 0;
        ios.c_oflag = 0;
        ios.c_lflag = 0;        /* disable ECHO, ICANON, etc... */
        ios.c_cc[VTIME] = 10;   /* unit: 1/10 second. */
        ios.c_cc[VMIN] = 1;     /* minimal characters for reading */

        tcsetattr(fd, TCSANOW, &ios);
        tcflush(fd, TCIOFLUSH);
    } else {
        ERR("Error isatty(fd): invalid terminal %s\n", strerror(errno));
        close(fd);
        exit(EXIT_FAILURE);
    }

    return 0;
}

int closeCAN(int close_fd) {
    // first always close1939Port1 the CAN module (bug #250)
    // http://192.168.1.234/redmine/issues/250
    char buf[256];
    sprintf(buf, "C\r");
    if (-1 == write(close_fd, buf, strlen(buf))) {
        ERR("Error write1939Port1 %s command\n", buf);
        return -1;
    }
    LOGD("Closed can channel ");
    return 0;
}

int setFd(int portNumber){
    if(portNumber==CAN1_TTY_NUMBER){
            return fd_CAN1;
    }
    else if(portNumber==CAN2_TTY_NUMBER){
            return fd_CAN2;
    }
    else if(portNumber==J1708_TTY_NUMBER){
            return fd_J1708;
    }
}

int closePort(int port_number){
    int closeFd;
    closeFd=setFd(port_number);
    if (closeCAN(closeFd) == -1) {

        return -1;
    }
    return 0;
}

//Can send the entire message including the CAN OK RESPONSE
int sendMessage(int fd_port, const char * message) {
    char buf[256]={0};
    sprintf(buf, "%s", message);
    printf("Send %s\n", buf);
    int check=strlen(buf);
    LOGD("Check value of the string - %d",check);
    if (-1 == write(fd_port, buf, strlen(buf))) {
        ERR("Error write1939Port1 %s command\n", buf );
        return -1;
    }
    return 0;
}

void setFlowControlMessage(char type,char *searchID,char *responseID, int dataLength, BYTE* dataBytes, int port_fd){

    char *flowControlMessage = new char[36];
    memset(flowControlMessage,'\0',sizeof(char));
    int i = 0, j = 0, k=0, l=0;
    int standardMessageLength=0;
    int extendedMessageLength=0;
    uint8_t tmp1=0;

    flowControlMessage[i++]='M';

    if(type=='T') {
        flowControlMessage[i++]='F';
        //Add search ID
        for (i = 2; i<=9; i++) {
            flowControlMessage[i] = searchID[j];
            j++;
        }
        //Add response ID
        for(i=10; i<=17; i++){
            flowControlMessage[i]=responseID[k];
            k++;
        }
        //Add data length
        flowControlMessage[i++]=dataLength + '0';

        //Add response data bytes
        for(i=19;i<((2*dataLength)+18);i++){
            for (int ind=0; ind <dataLength; ind++){
                tmp1 = (dataBytes[ind] >> 4) & 0xF;
                if (tmp1 > 9)
                    flowControlMessage[i] = tmp1 - 10 + 'A';
                else
                    flowControlMessage[i] = tmp1 + '0';
                i++;
                tmp1 = dataBytes[ind] & 0xF;
                if (tmp1 > 9)
                    flowControlMessage[i] = tmp1 - 10 + 'A';
                else
                    flowControlMessage[i] = tmp1 + '0';
                i++;
            }
        }
        i--;
        //Add CAN_OK_RESPONSE character
        flowControlMessage[i++]=CAN_OK_RESPONSE;
        extendedMessageLength=i;
        flowControlMessage[i]= 0;
    }

    else if (type=='t'){
        flowControlMessage[i++]='f';

        //Add search ID
        for (i = 2; i<=4; i++) {
            flowControlMessage[i] = searchID[j];
            j++;
        }
        //Add response ID
        for(i=5; i<=7; i++){
            flowControlMessage[i]=responseID[k];
            k++;
        }
        //Add data length
        flowControlMessage[i++]= dataLength + '0';

        //Add response data bytes
        for(i=9;i<((2*dataLength)+8);i++){
            for (int ind=0; ind < dataLength; ind++){
                tmp1 = (dataBytes[ind] >> 4) & 0xF;
                if (tmp1 > 9)
                    flowControlMessage[i] = tmp1 - 10 + 'A';
                else
                    flowControlMessage[i] = tmp1 + '0';
                i++;
                tmp1 = dataBytes[ind] & 0xF;
                if (tmp1 > 9)
                    flowControlMessage[i] = tmp1 - 10 + 'A';
                else
                    flowControlMessage[i] = tmp1 + '0';
                i++;
            }
        }
        i--;
        //Add CAN_OK_RESPONSE Character
        flowControlMessage[i++]=CAN_OK_RESPONSE;
        standardMessageLength=i;
        flowControlMessage[i]= 0;
    }

    //Check for valid extended and standard flow command based on its length
    if((flowControlMessage[1]=='F' &&  flowControlMessage[extendedMessageLength-1]==CAN_OK_RESPONSE) || (flowControlMessage[1]=='f' &&  flowControlMessage[standardMessageLength-1]==CAN_OK_RESPONSE)){
        if (-1 == sendMessage(port_fd, flowControlMessage)) {
            LOGE("!!!!Error sending flow message: %s for Flow code: !!!!", searchID);
        }
        LOGD("Flow message SET: FlowMessage- %s", flowControlMessage);
    }
    else LOGD("Error: Flow control  message not set successfully!!! Message: %s, Extended Message size=%d or StandardMessageSize=%d", flowControlMessage, extendedMessageLength,standardMessageLength);
}


int setMasks(char *mask, char type, int port_fd) {
    char maskString[16];
    char maskCommand[16];
    char standardFormat[5]={'0','0','0','0','0'};
    int maskLength;
    int i = 0, j = 0, x=0;

    if(mask!=NULL) {
        maskString[i] = 'm';
        i++;
        maskString[i++] = type;

        if ((type == 'T') || (type == 'R')) {
            for (i = 2; i < 10; i++) {
                maskString[i] = mask[j];
                j++;
            }
        } else if ((type == 't') || (type == 'r')) {
            for (i = 2; i < 7; i++) {
                maskString[i] = standardFormat[x];
            }
            for (i = 7; i < 10; i++) {
                maskString[i] = mask[j];
                j++;
            }
        }
        maskString[i++] = '\r';
        maskLength = i;
        maskString[i++]= 0;

        //send Mask string
        memcpy(maskCommand, maskString,maskLength);
        if (-1 == sendMessage(port_fd, maskString)) {
            LOGE("!!!!Error sending Mask message: %s for Filter: !!!!", maskString);
        }
        LOGD("Mask set SET %s", mask);
    }
    else LOGE("!!!MASK NOT SET - NULL/Empty MASK PASSED!!!");
	return 0;
}

int setFilters(char *filter, char type, int port_fd) {
    char filterCommand[16]={0};
    char filterString[16]={0};
    char standardFormat[5]={'0','0','0','0','0'};
    int filters_length;
    int i = 0, j = 0, x=0;

    if(filter!=NULL) {
        filterString[i++] = 'M';
        filterString[i++] = type;

        if ((type == 'T') || (type == 'R')) {
            for (i = 2; i < 10; i++) {
                filterString[i] = filter[j];
                j++;
            }
        } else if ((type == 't') || (type == 'r')) {
            for (i = 2; i < 7; i++) {
                filterString[i] =standardFormat[x];
                x++;
            }
            for (i = 7; i < 10; i++) {
                filterString[i] = filter[j];
                j++;
            }
        }
        filterString[i++] = '\r';
        filters_length = i;
        filterString[i++]= 0;

        memcpy(filterCommand, filterString,filters_length);
        if (-1 == sendMessage(port_fd, filterCommand)) {
            LOGE("!!!!Error sending Filter message: %s for Filter: !!!!", filterString);
        }
        LOGD("Filter SET: Filter- %s", filter);
    }
    else LOGE("!!!! NULL FILTER PASSED !!!");
	return 0;
}

int setBitrate(int fd, int speed) {
    int baud;
    switch(speed) {
        case 10000:
            baud = 0;
            break;
        case 20000:
            baud = 1;
            break;
        case 33330:
            baud = 2;
            break;
        case 50000:
            baud = 3;
            break;
        case 100000:
            baud = 4;
            break;
        case 125000:
            baud = 5;
            break;
        case 250000:
            baud = 6;
            break;
        case 500000:
            baud = 7;
            break;
        case 800000:
            baud = 8;
            break;
        case 1000000:
            baud = 9;
            break;
        default:
            baud = 6;
            //J1939 Specifies 250K as default baud rate
            break;
    }
    char buf[256];
/*    sprintf(buf, "C\rS%d\r", baud);*/
    sprintf(buf, "S%d\r", baud);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write1939Port1 %s command\n", buf);
        return -1;
    }
    return 0;
}

int openCANandSetTermination(int fd, bool term) {
    int termination = term ? 1 : 0;

    char buf[256];
    sprintf(buf, "O%d\r", termination);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write1939Port1 %s command\n", buf);
        return -1;
    }
    LOGD("Opened can channel");
    return 0;
}

int setListeningMode(int fd, bool term) {
    int termination = term ? 1 : 0;

    char buf[256];
    sprintf(buf, "L%d\r", termination);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write1939Port1 %s command\n", buf);
        return -1;
    }
    return 0;
}

int sendReadStatusCommand(int fd) {
    char buf[256];
    sprintf(buf, "F\r");
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write1939Port1 %s command\n", buf);
        return -1;
    }
    return 0;
}

int serial_start_monitor_thread_can_port1()
{
    quit_port1 = false;
    int r = pthread_create(&thread__port1, NULL, monitor_data_thread_port1, 0);
    if( r != 0 ){
        error_message("thread__port1 has failed to be created");
        thread__port1 = 0;
        return -1;
    }
    return 0;
}

//TODO : check this function
int serial_deinit_thread_port1() {
/*    LOGD("Entered serial_deinit_thread_port1()");
    if (thread__port1) {
        int retval, *retvalp;
        // cancel out readPort1 threads
        quit_port1 = 1;
//        retvalp = &retval;
        LOGD("Begin cancelling the threads");
        pthread_join(thread__port1,NULL);
        LOGD("cancel out the threads");
        return retval;
    }
    LOGD("Failed to enter the if(thread__port1)");
    return 0;*/
    LOGD("Entered serial_deinit_thread_port1()");
    if (thread__port1) {
        int retval, *retvalp;
        // cancel out readPort1 threads
        quit_port1 = 1;
        /*retvalp = &retval;*/
        LOGD("Begin cancelling the threads");
        pthread_join(thread__port1,NULL/* (void **) &retvalp*/);
        LOGD("cancel out the threads");
        return retval;
    }
    LOGD("Failed to enter the if(thread__port1)");
    return 0;
}

int serial_start_monitor_thread_can_port2() {
    quit_port2 = false;
    int r = pthread_create(&thread__port2, NULL, monitor_data_thread_can_port2, 0);
    if (r != 0) {
        error_message("thread__port2 has failed to be created");
        thread__port2 = 0;
        return -1;
    }
    return 0;
}

int serial_deinit_thread_port2() {
    LOGD("Entered serial_deinit_thread_port1()");
    if (thread__port2) {
        int retval, *retvalp;
        // cancel out readPort1 threads
        quit_port2 = 1;
        /*retvalp = &retval;*/
        LOGD("Begin cancelling the threads");
        pthread_join(thread__port2,NULL/* (void **) &retvalp*/);
        LOGD("cancel out the threads");
        return retval;
    }
    LOGD("Failed to enter the if(thread__port1)");
    return 0;
}

int parseHex(uint8_t * asciiString, int len, uint8_t * hexValue) {
    *hexValue = 0;
    if (0 == len) {
        return -1;
    }
    while (len--) {
        if (*asciiString == 0) return -1;
        *hexValue <<= 4;
        if ((*asciiString >= '0') && (*asciiString <= '9')) {
            *hexValue += *asciiString - '0';
        } else if ((*asciiString >= 'A') && (*asciiString <= 'F')) {
            *hexValue += *asciiString - 'A' + 10;
        } else if ((*asciiString >= 'a') && (*asciiString <= 'f')) {
            *hexValue += *asciiString - 'a' + 10;
        } else return -1;
        if(!(len%2))
        {
            hexValue++;
        }
        asciiString++;
    }
    return 0;
}

int wait_for_data(int port_fd)
{
    int r;
    fd_set fds;

    // use a timeout to prevent thread__port1 or thread__port2 from staying alive after interface is closed
    timeval delay = {0, 500000};

    do {
        FD_ZERO(&fds);
        FD_SET(port_fd, &fds);
        r= select(port_fd+1, &fds, NULL, NULL, &delay);
    } while (-1 == r && errno == EINTR);

    if(-1 == r)
    {
        LOGE("%s:%d select: %s\n", __FILE__, __LINE__, strerror(errno));
        return -1;
    }
    else if(r > 0)
    {
        if(r != 1)
            LOGE("select did not return 1, returned %d\n", r);

        if(FD_ISSET(port_fd, &fds))
        {
            return 0;
        }
        else
            LOGE("select returned, bu no fd, r = %d\n", r);

    }
    else {
        // don't process when r==0 to reduce overhead
    }
    return -1;
}

void sendCanbusFramePort1(uint32_t frameId, int type, int length, BYTE* data ){
    // Attach thread__port1
    JNIEnv *env;
    jint rs = g_canbus.g_vm->AttachCurrentThread(&env, &g_canbus.args);
    if (rs != JNI_OK) {
        error_message("j1939rxd failed to attach from CAN1_TTY!!!!");
    }

    // construct object
    jclass canbusFramePort1Class = g_canbus.canbusFramePort1Class;
    jmethodID canbusFrameConstructor = env->GetMethodID(canbusFramePort1Class, "<init>", "(I[B)V");

    jfieldID typeField = env->GetFieldID(canbusFramePort1Class, "mType", "Lcom/micronet/canbus/CanbusFrameType;");

    // Storing the actual data of canbus frame into byte array
    jbyteArray data_l = env->NewByteArray(length);
    env->SetByteArrayRegion(data_l, 0, length, (jbyte *) data);

    jobject frameObj = env->NewObject(canbusFramePort1Class, canbusFrameConstructor, frameId, data_l);
    env->SetObjectField(frameObj, typeField, type == STANDARD ? g_canbus.type_s : g_canbus.type_e);

    // on rare occasion, frame is received before socket is initialized
    if (g_canbus.g_listenerObject_Can1 != NULL && g_canbus.g_onPacketReceive1939Port1 != NULL) {
        env->CallVoidMethod(g_canbus.g_listenerObject_Can1, g_canbus.g_onPacketReceive1939Port1, frameObj);
    }
    LOGD("######### PORT 1 ########## Message pushed to the java layer successfully");
    env->DeleteLocalRef(frameObj);
    env->DeleteLocalRef(data_l);
    g_canbus.g_vm->DetachCurrentThread();
}

void sendCanbusFramePort2(uint32_t frameId, int type, int length, BYTE* data){
//TODO: Add
    // Attach thread__port2

    JNIEnv *env;
    jint rs = g_canbus.g_vm->AttachCurrentThread(&env, &g_canbus.args);
    if (rs != JNI_OK) {
        error_message("j1939rxd failed to attach from CAN2_TTY!!!!");
    }

    // construct object
    jclass canbusFramePort2Class = g_canbus.canbusFramePort2Class;
    jmethodID canbusFrameConstructor = env->GetMethodID(canbusFramePort2Class, "<init>", "(I[B)V");

    jfieldID typeField = env->GetFieldID(canbusFramePort2Class, "mType", "Lcom/micronet/canbus/CanbusFrameType;");

    // Storing the actual data of canbus frame into byte array
    jbyteArray data_l = env->NewByteArray(length);
    env->SetByteArrayRegion(data_l, 0, length, (jbyte *) data);

    jobject frameObj = env->NewObject(canbusFramePort2Class, canbusFrameConstructor, frameId, data_l);
    env->SetObjectField(frameObj, typeField, type == STANDARD ? g_canbus.type_s : g_canbus.type_e);

    // on rare occasion, frame is received before socket is initialized
    if (g_canbus.g_listenerObject_Can2 != NULL && g_canbus.g_onPacketReceive1939Port2 != NULL) {
        env->CallVoidMethod(g_canbus.g_listenerObject_Can2, g_canbus.g_onPacketReceive1939Port2, frameObj);
    }
    LOGD("######### PORT 2 ########## Message pushed to the java layer successfully");
    env->DeleteLocalRef(frameObj);
    env->DeleteLocalRef(data_l);
    g_canbus.g_vm->DetachCurrentThread();
}

//TODO: Add sendJ1708()


void j1939rxd(BYTE *rxd, int portNumber) {
    char chars[100];
    J1939_ID mId;
    uint32_t frameId;
    int noerr = 1;
    int type;
    int idStart = 1;
    uint8_t dLength;
    int length;
    uint8_t id[8]; //maximum size of an Identifer is 8 bytes Long
    uint8_t *pId=id;
    uint8_t hexId[8]={0};
    int dataStartPos;
    uint8_t canData[16];
    BYTE hexData[8]={0};

    // Convert rxd (frame) into CanbusFramePort1 object
    if (*rxd == 't') {
            type=STANDARD;
            dataStartPos=5;
            memcpy(pId, (const void *) (rxd + idStart), 3);
            parseHex(id,3, hexId);
            mId.dwVal=(uint32_t)((hexId[0]<<8)| (hexId[1]<<0));
            frameId=mId.dwVal;
            dLength = *(rxd + 4) - '0';
            memcpy(canData, (const void *) (rxd + dataStartPos), dLength*2);
            parseHex(canData,dLength*2,hexData);
    }

    else if (*rxd == 'T') {
            type=EXTENDED;
            dataStartPos=10;
            memcpy(pId, (const void *) (rxd + idStart), 8);
            parseHex(id,8, hexId);
            mId.dwVal=(uint32_t)((hexId[0]<<24)| (hexId[1]<<16)|(hexId[2]<<8) | (hexId[3]<<0));
            frameId=mId.dwVal;
            dLength = *(rxd + 9) - '0';
            memcpy(canData, (const void *) (rxd + dataStartPos), dLength*2);
            parseHex(canData,dLength*2,hexData);
    }

    length = dLength; // save data length into an integer called 'length'

    if(portNumber==CAN1_TTY_NUMBER){
            sendCanbusFramePort1(frameId, type, length, hexData);
    }
    else if (portNumber==CAN2_TTY_NUMBER){
            sendCanbusFramePort2(frameId, type, length, hexData);
    }
}

static void *monitor_data_thread_port1(void *param)
{
    uint8_t data[8 * 1024];
    uint8_t *pdata = data;
	unsigned char * thread_char = (unsigned char *)(void *)(&thread__port1);

    prctl(PR_SET_NAME, "monitor_thread_port1", 0, 0, 0);
    LOGD("monitor_thread_port1 started");

    LOGD("thread__port1=%02x",(unsigned char)*thread_char);

    while (!quit_port1){
        // sanity check to kill stale readPort1 thread__port1
         if(thread__port1 != pthread_self()) {
             //TODO: Delete
             LOGD("read thread stale, thread=%d, pthread_self=%d", thread__port1, pthread_self());
             //TODO: Uncomment the following
            /* LOGD("readPort1 thread__port1 stale, thread__port1=%02x", (unsigned char)*thread_char);*/
             break;
         } //if statement was commented out
        if(fd_CAN1<0){break;}

        if (!wait_for_data(fd_CAN1)) {
            int readData;
            uint8_t *pend = NULL;
            readData = read(fd_CAN1, pdata, sizeof(data) - (pdata - data)); //Returns the number of bytes readPort1

            if (0 == readData) {
                quit_port1 = true;
                LOGD("quit1=%d", quit_port1);
                break;
            }
            if (-1 == readData) {
                if (EAGAIN == errno)
                    continue;
                LOGE("%s:%d readPort1: %s\n", __func__, __LINE__, strerror(errno));
                abort();
            }
            /*To check validity of a packet:
             * /r- Data is correct
             * BEL - Data is incorrect */

            int i, packetCount = 0,j = 0, start = 0, packetLength=0;
            //To identify the message type and store each valid message in the process buffer in data[]
            for (i = j; i <= readData; i++) {
                uint8_t idata = data[i];
                if (idata == '\r') {
                    continue;
                }
                int carriageReturn = 0;
                //For an extended CAN frame
                if (data[i] == 'T'){//T0x54
                    start = i;
                    uint8_t dataLength = (data[i + 9] - '0'); // get the actual value
                    if (dataLength == 0 || dataLength <= 8) {      // validating if the dataLength is in an actual number range (0-8)
                        carriageReturn = i + 14 + 2 * dataLength;
                        i = carriageReturn;
                        if (data[carriageReturn] == 13) {
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("### PORT 1 ### One complete extended packet received, Data Length- %d", dataLength);
                            j = carriageReturn;
                        }
                        else {
                            LOGD("### PORT 1 ### Error:Incomplete packet");
                        }
                    }
                    else LOGD("### PORT 1 ### Error:Invalid data length! ");
                }
                //For standard can frame
                if (data[i] == 't') {//T=0x74
                    start = i;
                    uint8_t dataLength = (data[i + 4] - '0');
                    if (dataLength == 0 || dataLength <= 8) {
                        carriageReturn = i + 9 + 2 * dataLength;
                        i = carriageReturn;
                        if (data[carriageReturn] == 13){
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("### PORT 1 ### One complete standard packet received, Data Length- %d", dataLength);
                            j = carriageReturn;
                        }
                        else
                            LOGD("### PORT 1 ### Error: Incomplete packet");
                    }
                    else LOGD("### PORT 1 ### Error:Invalid data length! ");
                }

                // extract one packet and convert into a byte array
                uint8_t frame[31]; //31 is the maximum size of an extended packet
                uint8_t * pFrame=frame;
                memcpy(frame, (const void *) (pdata+start), packetLength);
                LOGD("######### PORT 1 ########## FRAME = %s", frame);
                if ((frame[0] == 't' && frame[packetLength-1] == '\r') || (frame[0] == 'T' && frame[packetLength-1] == '\r')) {
                    j1939rxd(frame, CAN1_TTY_NUMBER);
                }
                else LOGD("######### PORT 1 ########## Incomplete packet received: Frame not sent to j1939rxd()");
            }
        }
    }

}

static void *monitor_data_thread_can_port2(void *param) {

    uint8_t data[8 * 1024];
    uint8_t *pdata = data;
    unsigned char * thread_char = (unsigned char *)(void *)(&thread__port2);

    prctl(PR_SET_NAME, "monitor_thread_port2", 0, 0, 0);
    LOGD("monitor_thread_port2 started");

    LOGD("thread__port2=%02x",(unsigned char)*thread_char);

    while (!quit_port2) {
        // sanity check to kill stale readPort2 thread__port2
        if(thread__port2 != pthread_self()) {
            LOGD("readPort2 thread__port2 stale, thread__port2=%02x", (unsigned char)*thread_char);
            break;
        } //if statement was commented out
        if(fd_CAN2<0){break;}

        if (!wait_for_data(fd_CAN2)) {
            int readData;
            uint8_t *pend = NULL;
            readData = read(fd_CAN2, pdata, sizeof(data) - (pdata - data)); //Returns the number of bytes readPort1

            if (0 == readData) {
                quit_port2 = true;
                LOGD("quit2=%d", quit_port2);
                break;
            }
            if (-1 == readData) {
                if (EAGAIN == errno)
                    continue;
                LOGE("%s:%d readPort2: %s\n", __func__, __LINE__, strerror(errno));
                abort();
            }
            /*To check validity of a packet:
             * /r- Data is correct
             * BEL - Data is incorrect */

            int i, packetCount = 0,j = 0, start = 0, packetLength=0;
            //To identify the message type and store each valid message in the process buffer in data[]
            for (i = j; i <= readData; i++) {
                uint8_t idata = data[i];
                if (idata == '\r') {
                    continue;
                }
                int carriageReturn = 0;
                //For an extended CAN frame
                if (data[i] == 'T'){//T0x54
                    start = i;
                    uint8_t dataLength = (data[i + 9] - '0'); // get the actual value
                    if (dataLength == 0 || dataLength <= 8) {      // validating if the dataLength is in an actual number range (0-8)
                        carriageReturn = i + 14 + 2 * dataLength;
                        i = carriageReturn;
                        if (data[carriageReturn] == 13) {
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("### PORT 2 ### One complete extended packet received, Data Length- %d", dataLength);
                            j = carriageReturn;
                        }
                        else {
                            LOGD("### PORT 2 ### Error:Incomplete packet");
                        }
                    }
                    else LOGD("Error:Invalid data length! ");
                }
                //For standard can frame
                if (data[i] == 't') {//T=0x74
                    start = i;
                    uint8_t dataLength = (data[i + 4] - '0');
                    if (dataLength == 0 || dataLength <= 8) {
                        carriageReturn = i + 9 + 2 * dataLength;
                        i = carriageReturn;
                        if (data[carriageReturn] == 13){
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("### PORT 2 ### One complete standard packet received, Data Length- %d", dataLength);
                            j = carriageReturn;
                        }
                        else
                            LOGD("### PORT 2 ### Error: Incomplete packet");
                    }
                    else LOGD("Error:Invalid data length! ");
                }

                // extract one packet and convert into a byte array
                uint8_t frame[31]; //31 is the maximum size of an extended packet
                uint8_t * pFrame=frame;
                memcpy(frame, (const void *) (pdata+start), packetLength);
                LOGD("######### PORT 2 ########## FRAME = %s", frame);
                if ((frame[0] == 't' && frame[packetLength-1] == '\r') || (frame[0] == 'T' && frame[packetLength-1] == '\r')  /*(frame[0] == 'r' && frame[5] == '\r')*/) {
                    j1939rxd(frame,CAN2_TTY_NUMBER);
                }
                else LOGD("######### PORT 2 ########## Incomplete packet received: Frame not sent to j1939rxd()");
            }
        }
    }
    return 0;
}


int closeInterfaceCAN1() {
    LOGD("Entered the close1939Port1()! ");
    return serial_deinit_thread_port1();
}

int closeInterfaceCAN2() {
    LOGD("Entered the close1939Port2()! ");
    return serial_deinit_thread_port2();
}

int serial_send_data(BYTE *mydata, DWORD bytes_to_write, int fd) {
    DWORD numwr = 0;
    if(fd!=-1){
        numwr = write(fd, mydata, bytes_to_write);
        LOGD("Frame sent sucessfully!! frame =%s fd=%d", mydata,fd);
        //TODO: this may not be an error
        if(numwr != bytes_to_write ){
            return -1;
        }
        return 0;
    }
    else return -1 ;
}
