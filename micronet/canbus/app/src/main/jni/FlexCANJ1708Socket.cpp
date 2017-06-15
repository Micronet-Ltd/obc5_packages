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

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANJ1708Socket_registerCallbackJ1708Port(JNIEnv *env, jobject obj, jobject listenerObj){

    g_canbus.g_listenerObject_J1708 = (jobject) env->NewGlobalRef(listenerObj);
    jclass canbusListenerClass = env->GetObjectClass(listenerObj);
    if (canbusListenerClass == NULL) {
        LOGE("!!!!!!!!!!!!!!!! canbusSocketClass error - J708 Port !!!!!!!!!!!!!!!!");
    }

    g_canbus.g_onPacketReceiveJ1708 = env->GetMethodID(canbusListenerClass, "onPacketReceiveJ1708Port", "(Lcom/micronet/canbus/J1708Frame;)V");
    if (g_canbus.g_onPacketReceiveJ1708 == NULL) {
        LOGE("!!!!!!!!!!!!!!!! g_onPacketReceiveJ1708 error !!!!!!!!!!!!!!!!");
    }

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

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusSocket_closeSocketJ1708(JNIEnv *env, jobject instance) {
    ///TODO : Any check?

    env->DeleteGlobalRef(g_canbus.g_listenerObject_J1708);
    g_canbus.g_listenerObject_J1708 = NULL;
    g_canbus.g_onPacketReceiveJ1708 = NULL;
    return 0;
}

