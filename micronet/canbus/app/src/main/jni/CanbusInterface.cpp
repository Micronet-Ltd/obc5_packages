#define LOG_TAG "Canbus"
#include <jni.h>

JNIEXPORT jint JNICALL Java_com_micronet_canbus_CanbusInterface_getImplId(JNIEnv *env, jclass cls)
{
	return 2;
	// If FlexCAN return 2
}
