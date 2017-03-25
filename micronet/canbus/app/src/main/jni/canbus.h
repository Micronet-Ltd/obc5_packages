//
// Created by eemaan.siddiqi on 2/4/2017.
//
#define LOG_TAG "Canbus"
#include <jni.h>
#include <pthread.h>

#ifndef CAN_BUS_H
#define CAN_BUS_H


#define invalid_arg "Invalid_Argument"
#define CANBUS_JNI_VER "20170220.000"

#define MAX_SIZE 20
#define RECEIVE_BUFFER_SIZE 8388608

#define STANDARD 0
#define EXTENDED 1
#define STANDARD_REMOTE 2
#define EXTENDED_REMOTE 3

#define error_message ERR
#define ERR(...) LOGE(__VA_ARGS__)
#define  DD(...)   LOGD(__VA_ARGS__)

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define CAN1_TTY    "/dev/ttyACM2"
#define CAN2_TTY    "/dev/ttyACM3"
#define J1708_TTY   "/dev/ttyACM4"

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
    jobject type_e_r;
    jobject type_s_r;
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
JNIEXPORT jint JNICALL Java_com_micronet_canbus_CanbusInterface_getImplId(JNIEnv * env, jclass cls);//added

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env, jobject instance, jboolean listeningModeEnable, jint bitrate, jboolean termination);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_removeInterface(JNIEnv *env, jobject instance);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_setHardwareFilter(JNIEnv *env, jobject obj, jobjectArray hardwareFilters);
//Socket JNI
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_send(JNIEnv *env, jobject instance, jint socket, jobject frame);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_sendJ1708(JNIEnv *env, jobject instance, jint socket, jobject frame);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_registerCallback(JNIEnv *env, jobject instance, jobject listener);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_closeSocket(JNIEnv *env, jobject instance);
JNIEXPORT jstring JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_jniGetMCUVersion(JNIEnv *env, jclass type);


/*// TODO remove if interface can only be set when opening CAN
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_setInterfaceBitrate(JNIEnv *env,jobject instance, jint bitrate);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_enableListeningMode(JNIEnv *env, jobject instance, jboolean enable);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_setTermination(JNIEnv *env, jobject instance, jboolean enabled);*/
};
#endif /* CAN_BUS_H */
