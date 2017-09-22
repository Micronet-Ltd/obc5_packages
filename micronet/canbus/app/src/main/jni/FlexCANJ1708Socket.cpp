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

    LOGD("Entered registerCallback1708");
    g_canbus.g_listenerObject_J1708 = (jobject) env->NewGlobalRef(listenerObj);
    jclass j1708ListenerClass = env->GetObjectClass(listenerObj);
    if (j1708ListenerClass == NULL) {
        LOGE("!!!!!!!!!!!!!!!! FlexCANJ1708Socket error - J1708 Port !!!!!!!!!!!!!!!!");
    }

    g_canbus.g_onPacketReceiveJ1708 = env->GetMethodID(j1708ListenerClass, "onPacketReceiveJ1708Port", "(Lcom/micronet/canbus/J1708Frame;)V");
    if (g_canbus.g_onPacketReceiveJ1708 == NULL) {
        LOGE("!!!!!!!!!!!!!!!! g_onPacketReceive J1708 error !!!!!!!!!!!!!!!!");
    }
    LOGD("Leaving registerCallback1708");
    return 0;

}
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANJ1708Socket_sendJ1708(JNIEnv *env, jobject instance, jint socket, jobject frame){

    int data_sent;
    char id_str[64];
    int id, type;
    int priority;

    jclass cls = env->GetObjectClass(frame);
    jmethodID methodId = env->GetMethodID(cls, "getData", "()[B");
    jbyteArray data = (jbyteArray)env->CallObjectMethod(frame, methodId);

    methodId = env->GetMethodID(cls, "getId", "()I");
    id = env->CallIntMethod(frame, methodId);

    methodId = env->GetMethodID(cls, "getPriority", "()I");
    priority = env->CallIntMethod(frame, methodId);

    jbyte* bufferPtr = env->GetByteArrayElements(data, NULL);
    jsize lengthOfArray = env->GetArrayLength(data);

    qb_send_j1708_packet(id, (BYTE*) bufferPtr, (BYTE)priority, lengthOfArray);

    env->ReleaseByteArrayElements(data, bufferPtr, JNI_ABORT);

    return 0;
}
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANJ1708Socket_closeSocketJ1708(JNIEnv *env, jobject instance){
    ///TODO : Any check?
    LOGD("Closing the J1708 Socket !! ");
    env->DeleteGlobalRef(g_canbus.g_listenerObject_J1708);
    g_canbus.g_listenerObject_J1708 = NULL;
    g_canbus.g_onPacketReceiveJ1708 = NULL;
    return 0;
}

