#define LOG_TAG "Canbus"

#include "canbus.h"
#include "can.h"
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <getopt.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <termios.h>
#include <linux/tty.h>
#include <android/log.h>
#include <pthread.h>
#include <sys/prctl.h>
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define ERR(...) LOGE(__VA_ARGS__)

#define  DD(...)   LOGD(__VA_ARGS__)

#define CAN1_TTY    "/dev/ttyACM2"
#define CAN2_TTY    "/dev/ttyACM3"
#define J1708_TTY   "/dev/ttyACM4"
static pthread_t thread;
static int fd=-1; //File Descriptor (Handle)

struct canbus_globals g_canbus;

static void throwRuntimeException(JNIEnv *env, const char *message)
{
    jclass clazz = env->FindClass("java/lang/RuntimeException");

    if(clazz)
    {
        env->ThrowNew(clazz, message);
    }

}


jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env;
    g_canbus.g_vm = vm;
    g_canbus.args.version = JNI_VERSION_1_6;
    g_canbus.args.name = "canbus_monitor_data_thread";
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    // Check JAR libs version
    {
        jclass infoClass = env->FindClass("com/micronet/canbus/Info");
        if(!infoClass)
        {
            LOGE("Canbus Library mismatch, JNI '%s'\n", CANBUS_JNI_VER);
            throwRuntimeException(env, "Canbus Library mismatch. Unable to load class com.micronet.canbus.Info");
            return -1;

        }
        jfieldID versionField = env->GetStaticFieldID(infoClass, "VERSION", "Ljava/lang/String;");
        jstring versionString = (jstring)env->GetStaticObjectField(infoClass, versionField);
        const char * versionCString = env->GetStringUTFChars(versionString, 0);

        if(strcmp(versionCString, CANBUS_JNI_VER))
        {
            LOGE("Canbus Library mismatch, JNI '%s' != JAR '%s'\n", CANBUS_JNI_VER, versionCString);
            throwRuntimeException(env, "Canbus Library mismatch. canbus_api.jar does not match libcanbus.so");
            env->ReleaseStringUTFChars(versionString, versionCString);
            return -1;
        }

        env->ReleaseStringUTFChars(versionString, versionCString);
    }


    // Initialization for QBridge Implementation
    jclass cls = env->FindClass("com/micronet/canbus/CanbusFrame");
    g_canbus.canbusFrameClass = (jclass)env->NewGlobalRef(cls);

    jclass j1708Class = env->FindClass("com/micronet/canbus/J1708Frame");
    g_canbus.j1708FrameClass = (jclass)env->NewGlobalRef(j1708Class);

    jclass clsCanbusFrameType = env->FindClass("com/micronet/canbus/CanbusFrameType");
    jfieldID typeStandardField = env->GetStaticFieldID(clsCanbusFrameType, "STANDARD", "Lcom/micronet/canbus/CanbusFrameType;");
    jfieldID typeExtendedField = env->GetStaticFieldID(clsCanbusFrameType, "EXTENDED", "Lcom/micronet/canbus/CanbusFrameType;");

    g_canbus.type_s = (jobject) env->NewGlobalRef(env->GetStaticObjectField(clsCanbusFrameType, typeStandardField));
    g_canbus.type_e = (jobject) env->NewGlobalRef(env->GetStaticObjectField(clsCanbusFrameType, typeExtendedField));
    // end QBridge


    return JNI_VERSION_1_6;
}


void j1939rxd(BYTE *rxd) {
    char chars[100];
    J1939_ID mId;
    int noerr = 1;
    int type = STANDARD;
    int start = 1;
    uint8_t dLength = *(rxd + 4) - '0';
    int frameCarriageReturn = 4 + 2 * dLength + 5;
    // process complete packets
        // TODO: Convert rxd (frame) into CanbusFrame object
        // determine whether frame is standard or extended and save to type
        // get the identifier and store into mId.dwVal
        uint8_t *id;
        if (*rxd == 't') {
            memcpy(id, (const void *) (rxd + start), 3);
            mId.dwVal = (uint32_t) id;
        }
        else if (*rxd == 'T') {
            memcpy(id, (const void *) (rxd + start), 8);
            mId.dwVal = (uint32_t) id;
        }

        int length = dLength; // save data length into an integer called 'length'



        // Attach thread
        JNIEnv *env;
        jint rs = g_canbus.g_vm->AttachCurrentThread(&env, &g_canbus.args);
        if (rs != JNI_OK) {
            // error_message("j1939rxd failed to attach!");
        }

        // construct object
        jclass canbusFrameClass = g_canbus.canbusFrameClass;
        jmethodID canbusFrameConstructor = env->GetMethodID(canbusFrameClass, "<init>", "(I[B)V");

        jfieldID typeField = env->GetFieldID(canbusFrameClass, "mType",
                                             "Lcom/micronet/canbus/CanbusFrameType;");

        // store the actual data of canbus frame into byte array
        jbyteArray data_l = env->NewByteArray(length);
        env->SetByteArrayRegion(data_l, 0, length,
                                (jbyte *) /* determine where data position is in rxd */ &rxd[0]);

        jobject frameObj = env->NewObject(canbusFrameClass, canbusFrameConstructor, mId.dwVal,
                                          data_l);
        env->SetObjectField(frameObj, typeField,
                            type == STANDARD ? g_canbus.type_s : g_canbus.type_e);

        // on rare occasion, frame is received before socket is initialized
        if (g_canbus.g_listenerObject != NULL && g_canbus.g_onPacketReceive != NULL) {
            env->CallVoidMethod(g_canbus.g_listenerObject, g_canbus.g_onPacketReceive, frameObj);
        }

        env->DeleteLocalRef(frameObj);
        env->DeleteLocalRef(data_l);
        g_canbus.g_vm->DetachCurrentThread();
//    }
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

int closeCAN(int fd) {
// first always close the CAN module (bug #250)
    // http://192.168.1.234/redmine/issues/250
    char buf[256];
    sprintf(buf, "C\r");
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
        return -1;
    }
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
    sprintf(buf, "C\rS%d\r", baud);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
        return -1;
    }
    return 0;
}

int setTermination(int fd, bool term) {
    int termination = term ? 1 : 0;

    char buf[256];
    sprintf(buf, "O%d\r", termination);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf);
        return -1;
    }
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

int sendMessage(int fd, const char * message) {
    char buf[256];
    sprintf(buf, "t%s\r", message);
    printf("Send %s\n", buf);
    if (-1 == write(fd, buf, strlen(buf))) {
        ERR("Error write %s command\n", buf );
        return -1;
    }
    return 0;
}
static void *monitor_data_thread(void *param) {
    uint8_t data[8 * 1024];
    uint8_t *pdata = data;


    prctl(PR_SET_NAME, "monitor_thread", 0, 0, 0);
    LOGD("monitor_thread started");
    LOGD("thread=%d", thread);
    int quit = 0;
    while (!quit) {
        // sanity check to kill stale read thread
        /* if(thread != pthread_self()) {
             LOGD("read thread stale, thread=%d, pthread_self=%d", thread, pthread_self());
             break;
         }*/

        if (!wait_for_data()) {
            int readData;
            uint8_t *pend = NULL;
            //Returns the number of bytes read
            readData = read(fd, pdata, sizeof(data) - (pdata - data));
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
            //TODO: Check validity (Packet)
            /*To check validity of a packet we need to look for '/r
             * /r- Data is correct
             * BEL - Data is incorect */

            int i, packetCount = 0,j = 0, start = 0, packetLength=0;
            //To identify the message type and store each valid message in the process buffer in data[]
            for (i = j; i <= readData; i++) {
                uint8_t idata = data[i]; //For debugging
                //To identify if the carriage return
                if (idata == '\r') {
                    continue;
                }
                int carriageReturn = 0;
                //For an extended message
                if (data[i] == 'T'){//T0x54
                    start = i;
                    uint8_t dataLength = (data[i + 9] - '0'); // get the actual value
                    // validate if the dataLength is in an actual number range (0-8)
                    if (dataLength == 0 || dataLength <= 8) {
                        carriageReturn = i + 14 + 2 * dataLength;
                        i = carriageReturn;
                        // uint valueAtCR=data[carriageReturn];
                        if (data[carriageReturn] == 13) {
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("One packet is complete, Data Length- %d", dataLength);
                            j = carriageReturn;
                        }
                        else {
                            LOGD("Error:Incomplete packet");
                        }
                    }
                    else LOGD("Error:Invalid data length! ");
                }
                //For standard message
                if (data[i] == 't') {//T=0x74
                    start = i;
                    uint8_t dataLength = (data[i + 4] - '0');
                    if (dataLength == 0 || dataLength <= 8) {
                        carriageReturn = i + 9 + 2 * dataLength;
                        i = carriageReturn;
                        if (data[carriageReturn] == 13){
                            packetCount++;
                            packetLength=carriageReturn-start +1;
                            LOGD("One packet is complete, Data Length- %d", dataLength);
                            j = carriageReturn;
                        }
                        else
                            LOGD("Incomplete packet");
                    }
                    else LOGD("Error:Invalid data length! ");
                }
                // TODO: extract the packet and convert into byte array
                uint8_t frame[31]; //31 is the maximum size of an extended packet
                uint8_t * pFrame=frame;
                memcpy(pFrame, (const void *) (pdata+start), packetLength);
                if (*pFrame == 't' && *(pFrame+carriageReturn) == '\r' || *pFrame == 'T' && *(pFrame+carriageReturn) == '\r') {
                j1939rxd(frame);
                }
                else LOGD("Incomplete packet received");
            }
        }
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env, jobject instance, jboolean listeningModeEnable, jint bitrate, jboolean termination) {
//    int fd;
    char *tty;
    DD("opening port: '%s'\n", CAN1_TTY);

    if ((fd = open(CAN1_TTY, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
        perror(tty);
        exit(EXIT_FAILURE);
    }

    int r = pthread_create(&thread, NULL, monitor_data_thread, 0);
    if (initTerminalInterface(fd) == -1) {
        return -1;
    }
    /* first always close the CAN module (bug #250)
     http://192.168.1.234/redmine/issues/250
     */
    if (closeCAN(fd) == -1) {
        return -1;
    }

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
        0	0	L0O0	O0
        0	1	L0O1	O1

     */
    if(listeningModeEnable && setListeningMode(fd, termination) == -1) { // enable listening mode and set the termination value
        return -1;
    } else if(setTermination(fd,termination ) == -1) { // set the termination value and (disable listening mode?) when opening CAN.
        // TODO: check if running "setTermination" disables listening mode
        return -1;
    }
/*    const char * mesg = "7003112233";
    if(sendMessage(fd, mesg) == -1) {
        return -1;
    }*/

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_removeInterface(JNIEnv *env,
                                                                      jobject instance) {
    // TODO close canbus

}

JNIEXPORT jint JNICALL
Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_setInterfaceBitrate(JNIEnv *env,
                                                                          jobject instance,
                                                                          jint bitrate) {

    // TODO remove if interface can only be set when opening CAN

}

JNIEXPORT jint JNICALL
Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_enableListeningMode(JNIEnv *env,
                                                                          jobject instance,
                                                                          jboolean enable) {

    // TODO remove if interface can only be set when opening CAN

}

JNIEXPORT jint JNICALL
Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_setTermination(JNIEnv *env, jobject instance,
                                                                     jboolean enabled) {

    // TODO remove if interface can only be set when opening CAN

}