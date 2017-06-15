#define LOG_TAG "Canbus"
#include <jni.h>
#include "canbus.h"
JNIEXPORT jint JNICALL Java_com_micronet_canbus_J1708Interface_getImplId(JNIEnv *env, jclass cls)
{
    return 2;
    // If FlexCAN return 2
}
