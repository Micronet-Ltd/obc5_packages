//
// Created by eemaan.siddiqi on 2/4/2017.
//
#include <jni.h>


#define CANBUS_JNI_VER "20170220.000"

#define MAX_SIZE 20
#define RECEIVE_BUFFER_SIZE 8388608

#define STANDARD 0
#define EXTENDED 1

#define DWORD uint32_t
#define WORD uint16_t
#define BYTE uint8_t
#define BOOL int

#define TRUE 1
#define FALSE 0

#define ACK_OK 0

#define MAX_QB_CAN_FILTERS 25
/*struct qb_filter_mask {
    __u32 mask;
    __u8 count;
    __u8 is_extended;
    __u32 filter_id[MAX_QB_CAN_FILTERS];
};*/

struct canbus_globals
{
    jbyteArray data;
    jobject g_listenerObject;
    jobject type_s;
    jobject type_e;
    jfieldID sizeField;
    jfieldID typeField;

    JavaVM* g_vm;
    JavaVMAttachArgs args;
    jmethodID g_onPacketReceive;
    jmethodID g_onPacketReceiveJ1708;


    jclass canbusFrameClass;
    jclass j1708FrameClass;
};
extern struct canbus_globals g_canbus;


extern "C" {
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env, jobject instance, jboolean listeningModeEnable, jint bitrate, jboolean termination);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_removeInterface(JNIEnv *env, jobject instance);
//Socket JNI
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_send(JNIEnv *env, jobject instance, jint socket, jobject frame);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_sendJ1708(JNIEnv *env, jobject instance, jint socket, jobject frame);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_registerCallback(JNIEnv *env, jobject instance, jobject listener);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_closeSocket(JNIEnv *env, jobject instance);

// TODO remove if interface can only be set when opening CAN
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_setInterfaceBitrate(JNIEnv *env,jobject instance, jint bitrate);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_enableListeningMode(JNIEnv *env, jobject instance, jboolean enable);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_setTermination(JNIEnv *env, jobject instance, jboolean enabled);
};
