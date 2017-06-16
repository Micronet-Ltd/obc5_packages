// Created by eemaan.siddiqi on 2/24/2017.
#define LOG_TAG "Canbus"
#include <jni.h>
#include "canbus.h"
#include "FlexCANCommand.h"
#include "FlexCANComm.h"

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

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_registerCallbackCanPort1(JNIEnv *env, jobject obj, jobject listenerObj)
{

    g_canbus.g_listenerObject_Can1 = (jobject) env->NewGlobalRef(listenerObj);
    jclass canbusListenerClass = env->GetObjectClass(listenerObj);
    if (canbusListenerClass == NULL) {
        LOGE("!!!!!!!!!!!!!!!! canbusSocketClass error !!!!!!!!!!!!!!!!");
    }

    g_canbus.g_onPacketReceive1939Port1 = env->GetMethodID(canbusListenerClass, "onPacketReceive1939Port1", "(Lcom/micronet/canbus/CanbusFramePort1;)V");
    if (g_canbus.g_onPacketReceive1939Port1 == NULL) {
        LOGE("!!!!!!!!!!!!!!!! g_onPacketReceive1939Port1 error !!!!!!!!!!!!!!!!");
    }

    return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_registerCallbackCanPort2(JNIEnv *env, jobject obj, jobject listenerObj)
{
    g_canbus.g_listenerObject_Can2 = (jobject) env->NewGlobalRef(listenerObj);
    jclass canbusListenerClass = env->GetObjectClass(listenerObj);
    if (canbusListenerClass == NULL) {
        LOGE("!!!!!!!!!!!!!!!! canbusSocketClass error - CAN Port 1 !!!!!!!!!!!!!!!!");
    }

       g_canbus.g_onPacketReceive1939Port2 = env->GetMethodID(canbusListenerClass, "onPacketReceive1939Port2", "(Lcom/micronet/canbus/CanbusFramePort2;)V");
       if (g_canbus.g_onPacketReceive1939Port2 == NULL) {
           LOGE("!!!!!!!!!!!!!!!! g_onPacketReceive1939Port1 error - CAN Port 2 !!!!!!!!!!!!!!!!");
       }

    return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_sendJ1939Port1(JNIEnv *env, jobject obj, jint socket, jobject canbusFrameObj) {

    int id, type;

    jclass cls = env->GetObjectClass(canbusFrameObj);
    jmethodID methodId = env->GetMethodID(cls, "getData", "()[B");
    jbyteArray data = (jbyteArray)env->CallObjectMethod(canbusFrameObj, methodId);

    methodId = env->GetMethodID(cls, "getId", "()I");
    id = env->CallIntMethod(canbusFrameObj, methodId);

    jbyte* bufferPtr = env->GetByteArrayElements(data, NULL);
    jsize lengthOfArray = env->GetArrayLength(data);
    type = get_frame_type(env, canbusFrameObj);

    FlexCAN_send_can_packet((BYTE)type,id,lengthOfArray, (BYTE*) bufferPtr, CAN1_TTY_NUMBER);

    env->ReleaseByteArrayElements(data, bufferPtr, JNI_ABORT);

    return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_sendJ1939Port2(JNIEnv *env, jobject obj, jint socket, jobject canbusFrameObj) {

    int id, type;

    jclass cls = env->GetObjectClass(canbusFrameObj);
    jmethodID methodId = env->GetMethodID(cls, "getData", "()[B");
    jbyteArray data = (jbyteArray)env->CallObjectMethod(canbusFrameObj, methodId);

    methodId = env->GetMethodID(cls, "getId", "()I");
    id = env->CallIntMethod(canbusFrameObj, methodId);

    jbyte* bufferPtr = env->GetByteArrayElements(data, NULL);
    jsize lengthOfArray = env->GetArrayLength(data);
    type = get_frame_type(env, canbusFrameObj);

    FlexCAN_send_can_packet((BYTE)type,id,lengthOfArray, (BYTE*) bufferPtr, CAN2_TTY_NUMBER);

    env->ReleaseByteArrayElements(data, bufferPtr, JNI_ABORT);

    return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_closeSocketJ1939Port1(JNIEnv *env, jobject instance){

    env->DeleteGlobalRef(g_canbus.g_listenerObject_Can1);
    g_canbus.g_listenerObject_Can1 = NULL;
    g_canbus.g_onPacketReceive1939Port1 = NULL;

	return 0;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_closeSocketJ1939Port2(JNIEnv *env, jobject instance) {

    env->DeleteGlobalRef(g_canbus.g_listenerObject_Can2);
    g_canbus.g_listenerObject_Can2 = NULL;
    g_canbus.g_onPacketReceive1939Port2 = NULL;
    return 0;
}

