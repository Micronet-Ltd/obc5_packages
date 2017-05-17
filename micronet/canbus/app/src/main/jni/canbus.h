//
// Created by eemaan.siddiqi on 2/4/2017.
//
#define LOG_TAG "Canbus"
#include <jni.h>
#include <pthread.h>

#ifndef CAN_BUS_H
#define CAN_BUS_H


#define invalid_arg "Invalid_Argument"
#define CANBUS_JNI_VER "20170509.000"

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

#define SINGLE_WIRE_TTY "/dev/ttyACM1"
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


#define CAN_OK_RESPONSE 	0x0D
#define CAN_ERROR_RESPONSE	0x07
#define FLOW_CONTROL_ARR_SIZE 0x8
#define FLOW_CONTROL_INVALID_POS 0xFF
#define FLOW_CONTROL_INVALID_ID 0x0
#define CAN_MSG_ID_SIZE_STD 3
#define CAN_MSG_ID_SIZE_EXT 8

#define MAX_FLEXCAN_CAN_FILTERS 24
#define MAX_FlexCAN_Flowcontrol_CAN 8

struct FLEXCAN_filter_mask {
    __u32 mask_id[MAX_FLEXCAN_CAN_FILTERS];
    __u8 mask_count;

    __u8 filter_mask_type[MAX_FLEXCAN_CAN_FILTERS];
    __u8 filter_mask_type_count;

    __u32 filter_id[MAX_FLEXCAN_CAN_FILTERS];
    __u8 filter_count;

};

struct FLEXCAN_Flow_Control{
    //TODO: ADD STRUCTURE
    __u32 search_id[MAX_FlexCAN_Flowcontrol_CAN];
    __u8 search_id_count;
    __u32 response_id[MAX_FlexCAN_Flowcontrol_CAN];
    __u8 response_id_count;
    __u8 flow_msg_type[MAX_FlexCAN_Flowcontrol_CAN];
    __u8 flow_msg_type_count;
    __u8 flow_msg_data_length[MAX_FlexCAN_Flowcontrol_CAN];
    __u8 flow_msg_data_length_count;
    //TODO change jbyteArray to __u8 response_data_bytes[MAX_FlexCAN_Flowcontrol_CAN]
    jbyteArray response_data_bytes[MAX_FlexCAN_Flowcontrol_CAN];//__u32 response_data_bytes[MAX_FlexCAN_Flowcontrol_CAN][MAX_FlexCAN_Flowcontrol_CAN];
    __u8 response_data_bytes_count;
};

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

JNIEXPORT jint JNICALL
Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env, jobject instance, jboolean listeningModeEnable, jint bitrate, jboolean termination, jobjectArray  hardwarefilter, int port_number,jobjectArray flowControl);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_removeInterface(JNIEnv *env, jobject instance);

//Socket JNI
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_send(JNIEnv *env, jobject instance, jint socket, jobject frame);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_sendJ1708(JNIEnv *env, jobject instance, jint socket, jobject frame);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_registerCallback(JNIEnv *env, jobject instance, jobject listener);
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_closeSocket(JNIEnv *env, jobject instance);

};
#endif /* CAN_BUS_H */
