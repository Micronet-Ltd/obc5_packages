//
// Created by eemaan.siddiqi on 3/3/2017.
//

#include <unistd.h>
#include <sys/prctl.h>
#include <pthread.h>

#include "canbus.h"
#include "FlexCANComm.h"

static int fd=-1; //serial port file descriptor (handle)
static pthread_t thread;
static bool quit = false;

int serial_set_nonblocking(int fd) {
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

int serial_init(char *portName){

    char *tty=portName;
    DD("opening port: '%s'\n", portName);

    if ((fd = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
        perror(portName);
        exit(EXIT_FAILURE);
    }

    serial_set_nonblocking(fd);

    DD("opened port: '%s', fd=%d",CAN1_TTY, fd);

    initTerminalInterface(fd);

    return fd;
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

int closeCAN(int false_fd) { //was fd
    // first always close the CAN module (bug #250)
    // http://192.168.1.234/redmine/issues/250
    char buf[256];
    sprintf(buf, "C\r");
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
        return -1;
    }
    LOGD("Closing can channel ");
    return 0;
}

//Can send the entire message including the CAN OK RESPONSE
int sendMessage(int fd, const char * message) {
    char buf[256]={0};
    sprintf(buf, "%s", message);
    printf("Send %s\n", buf);
    int check=strlen(buf);
    LOGD("ChecK value of the string - %d",check);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf );
        return -1;
    }
    return 0;
}

void setFlowControlMessage(char type,char *searchID,char *responseID, char dataLength, char *dataBytes){
    //TODO: Test if its working properly.
    char flowControlMessage[36];
    int i = 0, j = 0, k=0, l=0;
    int messageLength=0;
    flowControlMessage[i++]='M';
    if(type='t'){
        flowControlMessage[i++]='f';
        for (i = 2; i<=4; i++) {
            flowControlMessage[i] = searchID[j];
            j++;
        }
        for(i=5; i<=7; i++){
            flowControlMessage[i]=responseID[k];
            k++;
        }
        flowControlMessage[i++]=dataLength;
        for(i=9;i<=24;i++){
            flowControlMessage[i] = searchID[l];
            l++;
        }
        flowControlMessage[i++]='\r';
        messageLength=i;
    }
    else if(type='T') {
        flowControlMessage[i++]='F';
        for (i = 2; i<=9; i++) {
            flowControlMessage[i] = searchID[j];
            j++;
        }
        for(i=10; i<=17; i++){
            flowControlMessage[i]=responseID[k];
            k++;
        }
        flowControlMessage[i++]=dataLength;
        for(i=19;i<=34;i++){
            flowControlMessage[i] = searchID[l];
            l++;
        }
        flowControlMessage[i++]='\r';
        messageLength=i;
    }
    sendMessage(fd,flowControlMessage);
}

int setMasks(char *mask, char type) {
    char maskString[16];
    char maskCommand[16];
    char maskString1[16]={'m','T','0','0','0','0','F','E','F','1','\r'}; //Compliler issue
    int maskLength;
    int i = 0, j = 0;

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
                maskString[i] = 0;
            }
            for (i = 7; i < 10; i++) {
                maskString[i] = mask[j];
                j++;
            }
        }
        maskString[i++] = '\r';
        maskLength = i;

        //send Mask string
        memcpy(maskCommand, maskString,maskLength);
        if (-1 == sendMessage(fd, maskString)) {
            LOGE("!!!!Error sending Mask message: %s for Filter: !!!!", maskString);
        }
        LOGD("Mask set SET %s", mask);
    }
    else LOGE("!!!MASK NOT SET - NULL/Empty MASK PASSED!!!");
	return 0;
}

int setFilters(char *filter, char type) { char maskString1[16]={'m','T','0','0','0','0','F','E','F','1','\r'}; //Compliler issue
    char filterCommand[16]={0};
    char filterString[16]={0};
    int filters_length;
    int i = 0, j = 0;

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
                filterString[i] = 0;
            }for (i = 7; i < 10; i++) {
                filterString[i] = filter[j];
                j++;
            }
        }
        filterString[i++] = '\r';
        filters_length = i;
        memcpy(filterCommand, filterString,filters_length);
        if (-1 == sendMessage(fd, filterCommand)) {
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
        ERR("Error write %s command\n", buf);
        return -1;
    }
    return 0;
}

int openCANandSetTermination(int fd, bool term) {
    int termination = term ? 1 : 0;

    char buf[256];
    sprintf(buf, "O%d\r", termination);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
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
        ERR("Error write %s command\n", buf);
        return -1;
    }
    return 0;
}

int sendReadStatusCommand(int fd) {
    char buf[256];
    sprintf(buf, "F\r");
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
        return -1;
    }
    return 0;
}

int serial_start_monitor_thread()
{
    quit = false;
    int r = pthread_create(&thread, NULL, monitor_data_thread, 0);
    if( r != 0 ){
        error_message("thread has failed to be created");
        thread = 0;
        return -1;
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

int wait_for_data()
{
    int r;
    fd_set fds;

    // use a timeout to prevent thread from staying alive after interface is closed
    timeval delay = {0, 500000};

    do {
        FD_ZERO(&fds);
        FD_SET(fd, &fds);
        r= select(fd+1, &fds, NULL, NULL, &delay);
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

        if(FD_ISSET(fd, &fds))
        {
            return 0;
        }
        else
            LOGE("select returned, bu no fd, r = %d\n", r);

    }
    else { // don't process when r==0 to reduce overhead
    }

    return -1;
}

void j1939rxd(BYTE *rxd) {
    char chars[100];
    J1939_ID mId;
    int noerr = 1;
    int type;
    int idStart = 1;
    uint8_t dLength;
    uint8_t id[8]; //maximum size of an Identifer is 8 bytes Longs
    uint8_t *pId=id;
    uint8_t hexId[8]={0};
    int dataStartPos;
    uint8_t canData[16];
    BYTE hexData[8]={0};

    // Convert rxd (frame) into CanbusFrame object
    if (*rxd == 't') {
        type=STANDARD;
        dataStartPos=5;
        memcpy(pId, (const void *) (rxd + idStart), 3);
        parseHex(id,3, hexId);
        mId.dwVal=(uint32_t)((hexId[0]<<8)| (hexId[1]<<0));
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
        dLength = *(rxd + 9) - '0';
        memcpy(canData, (const void *) (rxd + dataStartPos), dLength*2);
        parseHex(canData,dLength*2,hexData);
    }
    int length = dLength; // save data length into an integer called 'length'

    // Attach thread
    JNIEnv *env;
    jint rs = g_canbus.g_vm->AttachCurrentThread(&env, &g_canbus.args);
    if (rs != JNI_OK) {
        error_message("j1939rxd failed to attach!");
    }

    // construct object
    jclass canbusFrameClass = g_canbus.canbusFrameClass;
    jmethodID canbusFrameConstructor = env->GetMethodID(canbusFrameClass, "<init>", "(I[B)V");

    jfieldID typeField = env->GetFieldID(canbusFrameClass, "mType", "Lcom/micronet/canbus/CanbusFrameType;");

    // Storing the actual data of canbus frame into byte array
    jbyteArray data_l = env->NewByteArray(length);
    //env->SetByteArrayRegion(data_l, 0, length, (jbyte *) hexData[0]);
    env->SetByteArrayRegion(data_l, 0, length, (jbyte *) hexData);

    jobject frameObj = env->NewObject(canbusFrameClass, canbusFrameConstructor, mId.dwVal, data_l);
    env->SetObjectField(frameObj, typeField, type == STANDARD ? g_canbus.type_s : g_canbus.type_e);

    // on rare occasion, frame is received before socket is initialized
    if (g_canbus.g_listenerObject != NULL && g_canbus.g_onPacketReceive != NULL) {
        env->CallVoidMethod(g_canbus.g_listenerObject, g_canbus.g_onPacketReceive, frameObj);
    }
    LOGD("Message pushed to the java layer successfully");
    env->DeleteLocalRef(frameObj);
    env->DeleteLocalRef(data_l);
    g_canbus.g_vm->DetachCurrentThread();
//    }
}

static void *monitor_data_thread(void *param) {
    uint8_t data[8 * 1024];

    uint8_t *pdata = data;
	unsigned char * thread_char = (unsigned char *)(void *)(&thread);

    prctl(PR_SET_NAME, "monitor_thread", 0, 0, 0);
    LOGD("monitor_thread started");
    LOGD("thread=%02x",(unsigned char)*thread_char);
    int quit = 0;
    while (!quit) {
        // sanity check to kill stale read thread
         if(thread != pthread_self()) {
             LOGD("read thread stale, thread=%02x", (unsigned char)*thread_char);
             break;
         } //if statement was commented out
        if(fd<0){break;}

        if (!wait_for_data()) {
            int readData;
            uint8_t *pend = NULL;
            readData = read(fd, pdata, sizeof(data) - (pdata - data)); //Returns the number of bytes read

            if (0 == readData) {
                quit = true;
                LOGD("quit1=%d", quit);
                break;
            }
            if (-1 == readData) {
                if (EAGAIN == errno)
                    continue;
                LOGE("%s:%d read: %s\n", __func__, __LINE__, strerror(errno));
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
                            LOGD("One complete extended packet received, Data Length- %d", dataLength);
                            j = carriageReturn;
                        }
                        else {
                            LOGD("Error:Incomplete packet");
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
                            LOGD("One complete standard packet received, Data Length- %d", dataLength);
                            j = carriageReturn;
                        }
                        else
                            LOGD("Incomplete packet");
                    }
                    else LOGD("Error:Invalid data length! ");
                }

                // extract one packet and convert into a byte array
                uint8_t frame[31]; //31 is the maximum size of an extended packet
                uint8_t * pFrame=frame;
                memcpy(frame, (const void *) (pdata+start), packetLength);
                if ((frame[0] == 't' && frame[carriageReturn] == '\r') || (frame[0] == 'T' && frame[carriageReturn] == '\r')  /*(frame[0] == 'r' && frame[5] == '\r')*/) {
                    j1939rxd(frame);
                }
                else LOGD("Incomplete packet received: Frame not sent to j1939rxd()");
            }
        }
    }
    return 0;
}

int serial_deinit() {
    LOGD("Entered serial_deinit()");
    if (thread) {
        LOGD("Entered if(thread)");
        int retval, *retvalp;
        // cancel out read threads
        quit = 1;
        /*retvalp = &retval;*/
        LOGD("Begin cancelling the threads");
        pthread_join(thread,NULL/* (void **) &retvalp*/);
        LOGD("cancel out the threads");
        return retval;
    }
    LOGD("Failed to enter the if(thread)");
    return 0;
}

int qb_close() {
    LOGD("Entered the close()! ");
    return serial_deinit();
}


int serial_send_data(BYTE *mydata, DWORD bytes_to_write) {
    DWORD numwr = 0;

    numwr = write(fd, mydata, bytes_to_write);
    //TODO: this may not be an error
    if( numwr != bytes_to_write ){
        return -1;
    }
    return 0;
}
