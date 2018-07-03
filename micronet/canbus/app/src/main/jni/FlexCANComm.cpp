
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
//static int fd_J1708_READ;
//static int fd_J1708_WRITE;
static int fd_J1708;

static pthread_t thread__port1;
static pthread_t thread__port2;
static pthread_t thread__port1708;

static bool quit_port1 = false;
static bool quit_port2 = false;
static bool quit_port1708 = false;

static int maxJ1708PacketSize = 24;

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

char* getPortName(int portNumber){
    if (portNumber==2){
        return const_cast<char *>(CAN1_TTY);
    }
    else if (portNumber ==3){
        return const_cast<char *>(CAN2_TTY);}
    else {
        return const_cast<char *>("-1");
    }
}

int serial_init(char *portName)
{
    DD("opening port: '%s'\n", portName);

    //Initialising CAN1_TTY
    if(strcmp(portName, CAN1_TTY)==0){
        if ((fd_CAN1 = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
            perror(portName);
			DD("failed to open %s, error: %s", portName, strerror(errno));
            return -1;
            //exit(EXIT_FAILURE);
        }
            serial_set_nonblocking(fd_CAN1);
            DD("opened port: '%s', fd=%d", CAN1_TTY, fd_CAN1);
            initTerminalInterface(fd_CAN1, B115200, 1);
            return fd_CAN1;
        }

        //Initialising CAN2_TTY
    else if(strcmp(portName,CAN2_TTY)== 0){
        if ((fd_CAN2 = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
            perror(portName);
			DD("failed to open %s, error: %s", portName, strerror(errno));
            return -1;
            //exit(EXIT_FAILURE);
        }
            serial_set_nonblocking(fd_CAN2);
            DD("opened port: '%s', fd=%d", CAN2_TTY, fd_CAN2);
            initTerminalInterface(fd_CAN2, B115200,1);
        return fd_CAN2;
    }

    //Initialising J1708_READ
    /*else if(strcmp(portName,J1708_TTY_READ)== 0){
        if ((fd_J1708_READ = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
            perror(portName);
			DD("failed to open %s, error: %s", portName, strerror(errno));
            return -1;
            //exit(EXIT_FAILURE);
        }
        serial_set_nonblocking(fd_J1708_READ);
        DD("opened port: '%s', fd=%d", J1708_TTY_READ, fd_J1708_READ);
        initTerminalInterface(fd_J1708_READ, B9600);
        return fd_J1708_READ;
    }
        //Initialising J1708_WRITE
    else if(strcmp(portName,J1708_TTY_WRITE)== 0){
        if ((fd_J1708_WRITE = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
            perror(portName);
			DD("failed to open %s, error: %s", portName, strerror(errno));
            return -1;
            //exit(EXIT_FAILURE);
        }
        serial_set_nonblocking(fd_J1708_WRITE);
        DD("opened port: '%s', fd=%d", J1708_TTY_WRITE, fd_J1708_WRITE);
        initTerminalInterface(fd_J1708_WRITE, B9600);
        return fd_J1708_WRITE;
    }*/

    else if(strcmp(portName,J1708_TTY)== 0){
        if ((fd_J1708 = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
            perror(portName);
            return -1;
            //exit(EXIT_FAILURE);
        }
        serial_set_nonblocking(fd_J1708);
        DD("opened port: '%s', fd=%d", J1708_TTY, fd_J1708);
        initTerminalInterface(fd_J1708, B9600,maxJ1708PacketSize);
        return fd_J1708;
    }

    else return -1;
}

int initTerminalInterface(int fd, speed_t interfaceBaud, uint8_t readMinChar) {
    if (isatty(fd)) {
        struct termios ios;

        tcgetattr(fd, &ios);
        bzero(&ios, sizeof(ios));

        cfmakeraw(&ios);
        cfsetospeed(&ios, interfaceBaud);
        cfsetispeed(&ios, interfaceBaud);
        ios.c_cflag = (interfaceBaud | CS8 | CLOCAL | CREAD) & ~(CRTSCTS | CSTOPB | PARENB);
        ios.c_iflag = 0;
        ios.c_oflag = 0;
        ios.c_lflag = 0;        /* disable ECHO, ICANON, etc... */
        ios.c_cc[VTIME] = 10;   /* unit: 1/10 second. */
        ios.c_cc[VMIN] = readMinChar;     /* minimal characters for reading */

        tcsetattr(fd, TCSANOW, &ios);
        tcflush(fd, TCIOFLUSH);
    }
    else {
        ERR("Error isatty(fd): invalid terminal %s\n", strerror(errno));
        close(fd);
        return -1;
        //exit(EXIT_FAILURE);
    }

    return 0;
}

int closeTerminalInterface(int port){
    LOGD("Entered closeTerminalInterface!!");
    if (port == CAN1_TTY_NUMBER){
        close(fd_CAN1);
        fd_CAN1=-1;
		return  0; 
    }
    else if(port == CAN2_TTY_NUMBER){
        close(fd_CAN2);
        fd_CAN2=-1;
		return 0; 
    }
    else if(port == J1708_TTY_NUMBER ){
        close(fd_J1708);
        fd_J1708 = -1;
        return 0;
    }
   /* else if(port == J1708_TTY_READ_NUMBER){
        close(fd_J1708_READ);
        fd_J1708_READ = -1;
		return 0; 
	}
    else if(port == J1708_TTY_WRITE_NUMBER){
        close(fd_J1708_WRITE);
        fd_J1708_WRITE = -1;
        return 0;
    }*/
	else return -1; 
	
}

int closeCAN(int close_fd) {
    // first always close the CAN module (bug #250)
    // http://192.168.1.234/redmine/issues/250
    char buf[256];
    sprintf(buf, "C\r");
    if (-1 == write(close_fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
		close(close_fd);
        return -1;
    }
    LOGD("Closed CAN channel ");
    return 0;
}

/**
 * Returns the file descriptor associated with the tty port
 **/
int getFd(int portNumber){
    if(portNumber == CAN1_TTY_NUMBER){
            return fd_CAN1;
    }
    else if(portNumber == CAN2_TTY_NUMBER){
            return fd_CAN2;
    }
    else if(portNumber == J1708_TTY_NUMBER){
        return fd_J1708;
    }
    /*else if(portNumber == J1708_TTY_READ_NUMBER){
            return fd_J1708_READ;
    }
    else if(portNumber == J1708_TTY_WRITE_NUMBER){
        return fd_J1708_WRITE;
    }*/
	else return -1; 
}
/**
 * closePort()
 * Disables the tty port and de-allocates the file descriptor associated with it.
 **/
int closePort(int portNumber){
    LOGD("Entered closePort!!");
    int closeFd = getFd(portNumber);

    if(portNumber == CAN1_TTY_NUMBER || portNumber == CAN2_TTY_NUMBER){
        if (closeCAN(closeFd) == -1) {
            //Couldn't close CAN channel
            return -1;
        }
    }
    /*else if(portNumber == J1708_TTY_READ_NUMBER || portNumber == J1708_TTY_WRITE_NUMBER){
        if(getFd(CAN1_TTY_NUMBER) <= 0){
            //Set CAN1_1708 power enabled to 0
            //Temp solution below for closing CAN1
            LOGD("Entered closePort!! 1708 condition!");
            if (closeCAN(closeFd) == -1) {
                //Couldn't close CAN channel
                return -1;
            }
        }
        else{
            LOGE("Cannot close J1708, CAN1 is being used");
        }
    }*/

    else if(portNumber == J1708_TTY_NUMBER){
        if(getFd(CAN1_TTY_NUMBER) <= 0){
            //Set CAN1_1708 power enabled to 0
            //Temp solution below for closing CAN1
            LOGD("Entered closePort!! 1708 condition!");
            if (closeCAN(closeFd) == -1) {
                //Couldn't close CAN channel
                return -1;
            }
        }
        else{
            LOGE("Cannot close J1708, CAN1 is being used");
        }
    }

    closeTerminalInterface(portNumber);
    LOGD("Leaving closePort!!");
    return 0;
}

/**
 * sendMessage(fileDescriptorToSendTo, Message)
 * This function writes the entire message to the port associated with the file descriptor.
 * */
int sendMessage(int fd_port, const char * message) {
    char buf[256]={0};
    sprintf(buf, "%s", message);
    printf("Send %s\n", buf);
//  int check=strlen(buf);
//  LOGD("Check value of the string - %d",check);
    if (-1 == write(fd_port, buf, strlen(buf))) {
        ERR("Error: %s command coudln't be written! \n", buf );
        return -1;
    }
    return 0;
}
/**
 * setFlowControlMessage(FlowMessageType, RequestId, ResponseId, NumberOfResponseDataBytes, ResponseDataBytes, fd)
 * Constructs a Flow control message and sends teh flow control command to the CAN_TTY ports.
 * The user can set up to 8 flow codes for each can instance.
 * */
void setFlowControlMessage(char type,char *searchID,char *responseID, int dataLength, BYTE* dataBytes, int port_fd){

    char *flowControlMessage = new char[36];
    memset(flowControlMessage,'\0',sizeof(char));
    int i = 0, j = 0, k=0, l=0;
    int standardMessageLength=0, extendedMessageLength=0;
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

    if(type == 't'){
        LOGD("Start printing Message; Length = %d", standardMessageLength );
        for(i = 0; i < standardMessageLength; i++){
            LOGD("flowControlMessage[%d] = %c", i, flowControlMessage[i] );
        }
    }
    else {
        LOGD("Start printing Message; Length = %d", extendedMessageLength );
        for(i = 0; i < extendedMessageLength; i++){
            LOGD("flowControlMessage[%d] = %c", i, flowControlMessage[i] );
        }
    }

        //Check for valid extended and standard flow command based on its length
    if((flowControlMessage[1]=='F' &&  flowControlMessage[extendedMessageLength-1]==CAN_OK_RESPONSE) || (flowControlMessage[1]=='f' &&  flowControlMessage[standardMessageLength-1]==CAN_OK_RESPONSE)){
            if (-1 == sendMessage(port_fd, flowControlMessage)) {
                LOGE("!!!!Error configuring flow message: %s for Flow code: !!!!", searchID);
            }
        LOGD("Flow message SET: %s", flowControlMessage);
    } else LOGE("Error: Flow control command coundn't be sent! Message: %s, Extended Message size=%d or StandardMessageSize=%d", flowControlMessage, extendedMessageLength,standardMessageLength);
}

/**
 * setMasks(mask, maskType, fd)
 * Builds a mask command to set acceptable masks for receive.
 * The masks are stated as global per CAN instance.
 * A user can set up to 16 Masks per can instance.
 * */
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
        maskString[i++] = CAN_OK_RESPONSE;
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

/**
 * setFilters(filter, filterType, fd)
 * Builds a filter command to set acceptable filters/codes for receive.
 * The filters are stated as global per CAN instance.
 * A user can set up to 24 Filters per can instance.
 * */
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
        filterString[i++] = CAN_OK_RESPONSE;
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
    sprintf(buf, "S%d\r", baud);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error: Write Failed! Command - %s \n", buf);
        return -1;
    }
    return 0;
}

int openCANandSetTerm(int fd, bool term) {
    int termination = term ? 1 : 0;

    char buf[256];
    sprintf(buf, "O%d\r", termination);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error: Write Failed! Command - %s \n", buf);
        return -1;
    }
    LOGD("Opened CAN channel with termination set to = %d ",termination);
    return 0;
}

int setListeningModeandTerm(int fd, bool term) {
    int termination = term ? 1 : 0;
    char buf[256];
    sprintf(buf, "L%d\r", termination);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error: Couldn't Open Channel in Listening Mode: Write Failed! Command - %s \n", buf);
        return -1;
    }
    return 0;
}

int sendReadStatusCommand(int fd) {
    char buf[256];
    sprintf(buf, "F\r");
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error: Read Status Flag Config Failed: Write Failed! Command - %s \n", buf);
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
    if (thread__port1) {
        quit_port1 = 1;
        pthread_join(thread__port1,NULL);
        LOGD("CAN1 Read Thread Joined!");
    }
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
    if (thread__port2) {
        quit_port2 = 1;
        pthread_join(thread__port2,NULL);
        LOGD("CAN1 Read Thread Joined!");
    }
    return 0;
}

int serial_start_monitor_thread_j1708()
{
    quit_port1708 = false;
    int r = pthread_create(&thread__port1708, NULL, monitor_data_thread_port1708, 0);
    if( r != 0 ){
        error_message("thread__port1708 has failed to be created");
        thread__port1708 = 0;
        return -1;
    }
    return 0;
}

int serial_deinit_thread_j1708() {
    LOGD("Entered serial_deinit_thread_port1708()");
    if (thread__port1708) {
        int retval;
        quit_port1708 = 1;
        pthread_join(thread__port1708,NULL);
        return retval;
    }
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

int waitForData(int port_fd)
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
        LOGE("%s:%d Select: %s\n", __FILE__, __LINE__, strerror(errno));
        return -1;
    }
    else if(r > 0)
    {
        if(r != 1)
            LOGE("Select did not return 1, returned %d\n", r);

        if(FD_ISSET(port_fd, &fds))
        {
            return 0;
        }
        else
            LOGE("Select returned, bu no fd, r = %d\n", r);

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
    //LOGD("CAN1 Message: Successfully pushed the frame to the Java queue!");
    env->DeleteLocalRef(frameObj);
    env->DeleteLocalRef(data_l);
    g_canbus.g_vm->DetachCurrentThread();
}

void sendCanbusFramePort2(uint32_t frameId, int type, int length, BYTE* data){

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
    //LOGD("CAN2 Message: Successfully pushed the frame to the Java queue!");
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
    uint8_t id[8];
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

    // save data length into an integer called 'length'
    length = dLength;

    if(portNumber==CAN1_TTY_NUMBER){
            sendCanbusFramePort1(frameId, type, length, hexData);
    }
    else if (portNumber==CAN2_TTY_NUMBER){
            sendCanbusFramePort2(frameId, type, length, hexData);
    }
}

/**
 * MCU A.2.8.0 Implementation
 * <Byte1 = packet length> <J1708 Packet which includes MID, PID, Checksum> <0x0 padding up to 24 bytes>
 *  Note: the read packet is always 24 bytes long and includes the packet length.
 * J1708 Raw Frame Format
 * Maximum Size -> 21 Bytes
 * |MID|Data1-Data19|Checksum
 */
void j1708rxd( BYTE *rxd, BYTE length)
{
    int mid;
    int checksum;
    int verifyChecksum = 0;

    if(length < 2)
    {
        error_message("Debugging Info - Error: Bad 1708 Message, Message size is less than two");
        return;
    }

    mid = rxd[0]; // Note: 111 is factory test mid
    //TODO: Validate checksum then send the frame!
    if(verifyJ1708Checksum(mid, reinterpret_cast<uint8_t *>(*rxd + 1), length)){
        JNIEnv *env;
        jint rs = g_canbus.g_vm->AttachCurrentThread(&env, &g_canbus.args);

        if(rs != JNI_OK) {
            error_message("Debugging Info - Error: j1708rxd failed to attach!");
        }

        jclass j1708FrameClass = g_canbus.j1708FrameClass;
        jmethodID j1708FrameConstructor = env->GetMethodID(j1708FrameClass, "<init>", "(I[B)V");
        // if length-1 == 0, this will be empty byte array. Must not be NULL.
        jbyteArray data_l = env->NewByteArray(length - 1);
        env->SetByteArrayRegion(data_l, 0, length-1, (jbyte*)rxd + 1);
        jobject frameObj = env->NewObject(j1708FrameClass, j1708FrameConstructor, mid, data_l);
        // on rare occasion, frame is received before socket is initialized
        if(g_canbus.g_listenerObject_J1708 != NULL && g_canbus.g_onPacketReceiveJ1708 != NULL) {
            env->CallVoidMethod(g_canbus.g_listenerObject_J1708, g_canbus.g_onPacketReceiveJ1708, frameObj);
        }
        LOGD("1708 Message: Successfully pushed the frame to the Java queue!");
        env->DeleteLocalRef(frameObj);
        env->DeleteLocalRef(data_l);

        g_canbus.g_vm->DetachCurrentThread();
    }
    else{
        LOGE("J1708 Frame Parsing Failed ! ");
    }
}



int parseCANFrame(int start, int packetLength, uint8_t *pdata, int portNumber){
    //31 is the maximum size of an extended packet
    uint8_t frame[31];
    memcpy(frame, (const void *) (pdata+start), packetLength);
    LOGD("CAN Frame on ttyACM%d = %s", portNumber,frame);
    if ((frame[0] == 't' && frame[packetLength-1] == CAN_OK_RESPONSE) || (frame[0] == 'T' && frame[packetLength-1] == CAN_OK_RESPONSE)) {
        j1939rxd(frame, portNumber);
        return 0;
    }
    else
        return -1;
        LOGD("CAN PORT 1 ERROR: Incomplete packet received - Frame not sent to j1939rxd()");
}

/**
 * MCU A.2.8.0 Implementation
 * <Byte1 = packet length> <J1708 Packet which includes MID, PID, Checksum> <0x0 padding up to 24 bytes>
 *  Note: the read packet is always 24 bytes long and includes the packet length.
 * J1708 Raw Frame Format
 * Maximum Size -> 21 Bytes
 * |MID|Data1-Data19|Checksum
 */
int parseJ1708Frame(int start, int packetLength, uint8_t *pdata){

    uint8_t frame[packetLength];
    memcpy(frame, (const void *) (pdata+start), packetLength);
    //LOGD("J1708 Frame on ttyACM4 = %d, packetLength = %d",frame[0], packetLength);
    if(packetLength > 2){
        j1708rxd(frame, packetLength);
        return 0; 
	}
    else{
        LOGD("J1708 ERROR: Incomplete packet received - Frame not sent to j1708rxd()");
        return -1;
    }
}



static void *monitor_data_thread_port1(void *param) {

    uint8_t data[8 * 1024];
    uint8_t *pdata = data;
	unsigned char * thread_char = (unsigned char *)(void *)(&thread__port1);
    int i, packetCount = 0,j = 0, start = 0, packetLength=0, carriageReturn = 0;
    int errorResponseCount=0;
    int readCount=0;

    prctl(PR_SET_NAME, "monitor_thread_port1", 0, 0, 0);
    LOGD("monitor_thread_port1 started");
   	LOGD("thread_port1=%02x",(unsigned char)*thread_char);

    while (!quit_port1){
        // sanity check to kill stale readPort1 thread__port1
         if(thread__port1 != pthread_self()) {
             LOGD("readPort1 thread__port1 stale, thread__port1=%02x", (unsigned char)*thread_char);
             break;
         }

        if(fd_CAN1<0){
            break;
        }

        if (!waitForData(fd_CAN1)) {
            int readData;
            uint8_t *pend = NULL;

            //Returns the number of bytes read on CAN1
            readData = read(fd_CAN1, pdata, sizeof(data) - (pdata - data));
            //readData = read(fd_CAN1, pdata, 31);
            readCount++;
            if(readData>0){LOGD("READ COUNT ON PORT1 %d Number of characters=%d", readCount, readData);}

            if (0 == readData) {
                quit_port1 = true;
				close(fd_CAN1);
                LOGD("quit1=%d", quit_port1);
                break;
            }

            if (-1 == readData) {
                if (EAGAIN == errno)
                    continue;
                LOGE("%s:%d readPort1: %s\n", __func__, __LINE__, strerror(errno));
                abort();
            }

            //To identify the message type and store each valid message in the process buffer in data[]
            for (i = 0; i <= readData; i++) {
                carriageReturn = 0;
                start=0;
                packetLength=0;

                if (data[i] == CAN_OK_RESPONSE) {
                    continue;
                }

                else if (data[i] == CAN_ERROR_RESPONSE) {
                    errorResponseCount++;
                    LOGE("CAN1 Message: CAN ERROR RESPONSE Received!! Count-%d", errorResponseCount);
                    continue;
                }

                //For an extended CAN frame
                else if (data[i] == 'T'){//T0x54
                    start = i;
                    uint8_t dataLength = (data[i + 9] - '0');
                    // Validating if the dataLength is in an actual number range (0-8)
                    if (dataLength == 0 || dataLength <= 8) {
                        carriageReturn = i + 14 + 2 * dataLength;
                        if (data[carriageReturn] == 13) {
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("CAN1 Message: One complete extended packet received! DataLength=%d, Start=%d CarriageReturn=%d", dataLength, start, carriageReturn);
                            // Extract one packet and convert into a byte array
                            //parseCANFrame(start, packetLength, pdata, CAN1_TTY_NUMBER);
                            if(0== parseCANFrame(start, packetLength, pdata, CAN1_TTY_NUMBER)){
                                LOGD("CAN1 Packet count =%d", packetCount);
                            }
                            //Incrementing i to read the first character after the frame that was sent
                            i = carriageReturn;
                            continue;
                        }
                        else
                            LOGE("CAN1 Error:Incomplete packet!");
                    }
                    else LOGE("CAN1 Error: Recived frame with an Invalid Data Length! DataLength = %d", dataLength);
                }

                //For standard can frame
                else if (data[i] == 't'){//T=0x74
                    start = i;
                    uint8_t dataLength = (data[i + 4] - '0');
                    if (dataLength == 0 || dataLength <= 8) {
                        carriageReturn = i + 9 + 2 * dataLength;
                        if (data[carriageReturn] == 13){
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("CAN1 Message: One complete extended packet received! DataLength=%d, Start=%d CarriageReturn=%d", dataLength, start, carriageReturn);
                            // extract one packet and convert into a byte array
//                            parseCANFrame(start, packetLength, pdata, CAN1_TTY_NUMBER);
                            if(0== parseCANFrame(start, packetLength, pdata, CAN1_TTY_NUMBER)){
                                LOGD("CAN1 Packet count =%d", packetCount);
                            }
                            //Incrementing i to read the first character after the frame that was sent
                            i = carriageReturn;
                            continue;
                        }
                        else LOGE("CAN1 Error:Incomplete packet  ");
                    }
                    else LOGE("CAN1 Error: Received frame with an Invalid Data Length! DataLength=%d", dataLength);
                }
            }
        }
    }
    return 0;
}

static void *monitor_data_thread_can_port2(void *param) {

    uint8_t data[8 * 1024];
    uint8_t *pdata = data;

    unsigned char * thread_char = (unsigned char *)(void *)(&thread__port2);
    int i, packetCount = 0,j = 0, start = 0, packetLength=0, carriageReturn = 0;
    int errorResponseCount=0;

    prctl(PR_SET_NAME, "monitor_thread_port2", 0, 0, 0);
    LOGD("monitor_thread_port2 started");
    LOGD("thread__port2=%02x",(unsigned char)*thread_char);


    while (!quit_port2){
        // sanity check to kill stale readPort2 thread__port2
        if(thread__port2 != pthread_self()) {
            LOGD("readPort2 thread__port2 stale, thread__port2=%02x", (unsigned char)*thread_char);
            break;
        }

        if(fd_CAN2 < 0){break;}

        if (!waitForData(fd_CAN2)){
            int readData;
            uint8_t *pend = NULL;
            //Returns the number of bytes readPort1
            readData = read(fd_CAN2, pdata, sizeof(data) - (pdata - data));

            if (0 == readData) {
                quit_port2 = true;
				close(fd_CAN2);
                LOGD("quit2=%d", quit_port2);
                break;
            }
            if (-1 == readData) {
                if (EAGAIN == errno)
                    continue;
                LOGE("%s:%d readPort2: %s\n", __func__, __LINE__, strerror(errno));
                abort();
            }

            //To identify the message type and store each valid message in the process buffer in data[]
            for (i = 0; i <= readData; i++) {
                carriageReturn=0;
                start=0;
                packetLength=0;

                if (data[i] == CAN_OK_RESPONSE) {
                    continue;
                }
                else if (data[i] == CAN_ERROR_RESPONSE) {
                    errorResponseCount++;
                    LOGD("CAN2 Message: CAN ERROR RESPONSE Recieved!! Count=%d" , errorResponseCount);
                    continue;
                }

                //For an extended CAN frame
                else if (data[i] == 'T'){//T0x54
                    start = i;
                    uint8_t dataLength = (data[i + 9] - '0');
                    // validating if the dataLength is in an actual number range (0-8)
                    if (dataLength == 0 || dataLength <= 8) {
                        carriageReturn = i + 14 + 2 * dataLength;
                        if (data[carriageReturn] == 13) {
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("CAN2 Message: One complete extended packet received! DataLength=%d, Start=%d CarriageReturn=%d", dataLength, start, carriageReturn);
                            // extract one packet and convert into a byte array
                            if(0== parseCANFrame(start, packetLength, pdata, CAN2_TTY_NUMBER)){
                                LOGD("CAN2 Packet count =%d", packetCount);
                                }
                            //Incrementing i to read the first character after the frame that was sent
                            i = carriageReturn;
                            continue;
                        }
                        else LOGE("CAN2 Error: Incomplete packet! data[carriage return]=%d", data[carriageReturn]);
                    }
                    else LOGE("CAN2 Error: Recived frame with an Invalid data length! DataLength=%d", dataLength);
                }

                //For standard can frame
                else if (data[i] == 't') {//T=0x74
                    start = i;
                    uint8_t dataLength = (data[i + 4] - '0');
                    if (dataLength == 0 || dataLength <= 8) {
                        carriageReturn = i + 9 + 2 * dataLength;
                        if (data[carriageReturn] == 13){
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("CAN2 Message: One complete extended packet received! DataLength=%d, Start=%d CarriageReturn=%d", dataLength, start, carriageReturn);
                            // extract one packet and convert into a byte array
                            if(0== parseCANFrame(start, packetLength, pdata, CAN2_TTY_NUMBER)){
                                LOGD("CAN2 Packet count =%d", packetCount);
                            }
                            //Incrementing i to read the first character after the frame that was sent
                            i = carriageReturn;
                            continue;
                        }
                        else {
                            LOGE("CAN2 Error: Incomplete packet");
                        }
                    }
                    else LOGE("CAN2 Error: Recived frame with an Invalid data length! DataLength=%d", dataLength);
                }
            }
        }
    }
    return 0;
}

static void *monitor_data_thread_port1708(void *param) {
    uint8_t data[8 * 1024];
    uint8_t *pdata = data;
    unsigned char * thread_char = (unsigned char *)(void *)(&thread__port1708);
    int i, packetCount = 0,j = 0, startOfPacket = 0, packetLength=0, endOfPacket = 0;
    int errorResponseCount=0;
    int readCount=0;

    prctl(PR_SET_NAME, "monitor_thread_port1708", 0, 0, 0);
    LOGD("monitor_thread_port1708 started");
    LOGD("thread_port1708=%02x",(unsigned char)*thread_char);

    while (!quit_port1708){

        // sanity check to kill stale readPort1708 thread__port1708
        if(thread__port1708 != pthread_self()) {
            LOGD("readPort1 thread__port1708 stale, thread__port1708 = %02x", (unsigned char)*thread_char);
            break;
        }

        if(fd_J1708 < 0){
            break;
        }

        if (!waitForData(fd_J1708)) {

            int readData;
            uint8_t *pend = NULL;

            //Returns the number of bytes read on J1708
            readData = read(fd_J1708, pdata,maxJ1708PacketSize);
            readCount++;

            if(readData > 0){
                LOGD("Read Count on J1708 Port = %d, Number of characters = %d", readCount, readData);
            }

            if (0 == readData) {
                quit_port1708 = true;
                LOGD(" quit1708 = %d", quit_port1708);
                break;
            }

            if (-1 == readData) {
                if (EAGAIN == errno)
                    LOGD(" Data Read = -1");
                    continue;
                LOGE("%s:%d readPort1708: %s\n", __func__, __LINE__, strerror(errno));
                abort();
            }

            startOfPacket = 0;

            //TODO: Change after MCU Implementation
            if(readData == maxJ1708PacketSize) {
                //Process each 24 byte packet
                packetLength = data[startOfPacket];
                    parseJ1708Frame(1, packetLength, pdata);
            }
        }
    }
    return 0 ;
}


int closeCAN1Thread() {
    LOGD("Entered the closeCAN1Thread()! ");
    return serial_deinit_thread_port1();
}

int closeCAN2Thread() {
    LOGD("Entered the closeCAN2Thread()! ");
    return serial_deinit_thread_port2();
}

int closeJ1708Thread() {
    LOGD("Entered the closeJ1708Thread()! ");
    return serial_deinit_thread_j1708();
}

int serial_send_data(BYTE *mydata, DWORD bytes_to_write, int fd) {
    DWORD numwr = 0;
    if(fd!=-1){
        numwr = write(fd, mydata, bytes_to_write);
        LOGD("Frame sent successfully!! Number of bytes=%d, frame=%s, fd=%d",bytes_to_write, mydata,fd);
        //TODO: this may not be an error
        if(numwr != bytes_to_write ){
            return -1;
        }
        return 0;
    }
    else return -1 ;
}
