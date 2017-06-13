// Created by eemaan.siddiqi on 2/24/2017.
#define LOG_TAG "Canbus"
#include <jni.h>
#include "canbus.h"
#include "FlexCANCommand.h"
#include "FlexCANComm.h"

#include <stdio.h>
#include <android/log.h>

int  false_fd2;

static void throwException(JNIEnv *env, const char *message, const char* add)
{
    char msg[128];
    sprintf(msg, message, add);
    jclass cls = env->FindClass("java/lang/IllegalArgumentException");
    if (cls == 0) {
        return;
    }
    env->ThrowNew(cls, msg);
}

static int get_frame_type(JNIEnv *env, jobject object)
{
    int type;

    jclass cls = env->GetObjectClass(object);
    jmethodID methodId = env->GetMethodID(cls, "getType", "()Lcom/micronet/canbus/CanbusFrameType;");
    jobject o = env->CallObjectMethod(object, methodId);
    jclass typeclass = env->FindClass("com/micronet/canbus/CanbusFrameType");
    g_canbus.typeField = env->GetFieldID(typeclass, "mType", "I");
    type = env->GetIntField(o, g_canbus.typeField);

    return type;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_registerCallback(JNIEnv *env, jobject obj, jobject listenerObj)
{
    g_canbus.g_listenerObject = (jobject) env->NewGlobalRef(listenerObj);
    jclass canbusListenerClass = env->GetObjectClass(listenerObj);
    if (canbusListenerClass == NULL) {
        LOGE("!!!!!!!!!!!!!!!! canbusSocketClass error !!!!!!!!!!!!!!!!");
    }
    //TODO fix the onpacket stufff

    g_canbus.g_onPacketReceive1939Port1 = env->GetMethodID(canbusListenerClass, "onPacketReceive1939Port1", "(Lcom/micronet/canbus/CanbusFramePort1;)V");
    if (g_canbus.g_onPacketReceive1939Port1 == NULL) {
        LOGE("!!!!!!!!!!!!!!!! g_onPacketReceive1939Port1 error !!!!!!!!!!!!!!!!");
    }

    g_canbus.g_onPacketReceive1939Port2 = env->GetMethodID(canbusListenerClass, "onPacketReceive1939Port2", "(Lcom/micronet/canbus/CanbusFramePort2;)V");
    if (g_canbus.g_onPacketReceive1939Port2 == NULL) {
        LOGE("!!!!!!!!!!!!!!!! g_onPacketReceive1939Port1 error !!!!!!!!!!!!!!!!");
    }

    g_canbus.g_onPacketReceiveJ1708 = env->GetMethodID(canbusListenerClass, "onPacketReceiveJ1708Port", "(Lcom/micronet/canbus/J1708Frame;)V");
    if (g_canbus.g_onPacketReceiveJ1708 == NULL) {
        LOGE("!!!!!!!!!!!!!!!! g_onPacketReceiveJ1708 error !!!!!!!!!!!!!!!!");
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_sendJ1939Port1(JNIEnv *env, jobject obj, jint socket, jobject canbusFrameObj) {

    int data_sent;
    char id_str[64];
    int id, type;

    jclass cls = env->GetObjectClass(canbusFrameObj);
    jmethodID methodId = env->GetMethodID(cls, "getData", "()[B");
    jbyteArray data = (jbyteArray)env->CallObjectMethod(canbusFrameObj, methodId);

    methodId = env->GetMethodID(cls, "getId", "()I");
    id = env->CallIntMethod(canbusFrameObj, methodId);

    jbyte* bufferPtr = env->GetByteArrayElements(data, NULL);
    jsize lengthOfArray = env->GetArrayLength(data);
    type = get_frame_type(env, canbusFrameObj);

    FlexCAN_send_can_packet((BYTE)type,id,lengthOfArray, (BYTE*) bufferPtr);

    env->ReleaseByteArrayElements(data, bufferPtr, JNI_ABORT);

    return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_sendJ1939Port2(JNIEnv *env, jobject obj, jint socket, jobject canbusFrameObj) {

    int data_sent;
    char id_str[64];
    int id, type;

    jclass cls = env->GetObjectClass(canbusFrameObj);
    jmethodID methodId = env->GetMethodID(cls, "getData", "()[B");
    jbyteArray data = (jbyteArray)env->CallObjectMethod(canbusFrameObj, methodId);

    methodId = env->GetMethodID(cls, "getId", "()I");
    id = env->CallIntMethod(canbusFrameObj, methodId);

    jbyte* bufferPtr = env->GetByteArrayElements(data, NULL);
    jsize lengthOfArray = env->GetArrayLength(data);
    type = get_frame_type(env, canbusFrameObj);

    FlexCAN_send_can_packet((BYTE)type,id,lengthOfArray, (BYTE*) bufferPtr);

    env->ReleaseByteArrayElements(data, bufferPtr, JNI_ABORT);

    return 0;
}

JNIEXPORT jint Java_com_micronet_canbus_FlexCANCanbusSocket_sendJ1708(JNIEnv *env, jobject obj, jint socket, jobject j1708FrameObj){

    int data_sent;
    char id_str[64];
    int id, type;
    int priority;

    jclass cls = env->GetObjectClass(j1708FrameObj);
    jmethodID methodId = env->GetMethodID(cls, "getData", "()[B");
    jbyteArray data = (jbyteArray)env->CallObjectMethod(j1708FrameObj, methodId);

    methodId = env->GetMethodID(cls, "getId", "()I");
    id = env->CallIntMethod(j1708FrameObj, methodId);

    methodId = env->GetMethodID(cls, "getPriority", "()I");
    priority = env->CallIntMethod(j1708FrameObj, methodId);

    jbyte* bufferPtr = env->GetByteArrayElements(data, NULL);
    jsize lengthOfArray = env->GetArrayLength(data);

    //qb_send_j1708_packet(id, (BYTE*) bufferPtr, (BYTE)priority, lengthOfArray);

    env->ReleaseByteArrayElements(data, bufferPtr, JNI_ABORT);

    return 0;
}
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_closeSocketJ193Port1(JNIEnv *env, jobject instance) {

    if(closePort(CAN1_TTY_NUMBER) == -1) {
        return -1;
    }
    /*env->DeleteGlobalRef(g_canbus.g_listenerObject);
    g_canbus.g_listenerObject = NULL;*/
    g_canbus.g_onPacketReceive1939Port1 = NULL;
//    g_canbus.g_onPacketReceiveJ1708 = NULL;
	return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_closeSocketJ1939Port2(JNIEnv *env, jobject instance) {

    if(closePort(CAN2_TTY_NUMBER) == -1) {
        return -1;
    }
 /*   env->DeleteGlobalRef(g_canbus.g_listenerObject);
    g_canbus.g_listenerObject = NULL;
    g_canbus.g_onPacketReceive1939Port1 = NULL;
    g_canbus.g_onPacketReceiveJ1708 = NULL;*/
    g_canbus.g_onPacketReceive1939Port2 = NULL;
    return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_closeSocketJ1708(JNIEnv *env, jobject instance) {
//TODO : Pass fd_J1708 to closeCAN()

    if(closePort(J1708_TTY_NUMBER) == -1) {
        return -1;
    }

    //env->DeleteGlobalRef(g_canbus.g_listenerObject);
    //g_canbus.g_listenerObject = NULL;
    //g_canbus.g_onPacketReceive1939Port1 = NULL;
    g_canbus.g_onPacketReceiveJ1708 = NULL;
    return 0;
}

