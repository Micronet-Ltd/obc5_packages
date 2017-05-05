#define LOG_TAG "Canbus"
#include "canbus.h"
#include "can.h"

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


    // Initialization for FlexCAN Implementation
    jclass cls = env->FindClass("com/micronet/canbus/CanbusFrame");
    g_canbus.canbusFrameClass = (jclass)env->NewGlobalRef(cls);

    jclass j1708Class = env->FindClass("com/micronet/canbus/J1708Frame");
    g_canbus.j1708FrameClass = (jclass)env->NewGlobalRef(j1708Class);

    jclass clsCanbusFrameType = env->FindClass("com/micronet/canbus/CanbusFrameType");
    jfieldID typeStandardField = env->GetStaticFieldID(clsCanbusFrameType, "STANDARD", "Lcom/micronet/canbus/CanbusFrameType;");
    jfieldID typeExtendedField = env->GetStaticFieldID(clsCanbusFrameType, "EXTENDED", "Lcom/micronet/canbus/CanbusFrameType;");
    jfieldID typeExtendedRemoteField = env->GetStaticFieldID(clsCanbusFrameType, "EXTENDED_REMOTE", "Lcom/micronet/canbus/CanbusFrameType;");
    jfieldID typeStandardRemoteField = env->GetStaticFieldID(clsCanbusFrameType, "STANDARD_REMOTE", "Lcom/micronet/canbus/CanbusFrameType;");


    g_canbus.type_s = (jobject) env->NewGlobalRef(env->GetStaticObjectField(clsCanbusFrameType, typeStandardField));
    g_canbus.type_e = (jobject) env->NewGlobalRef(env->GetStaticObjectField(clsCanbusFrameType, typeExtendedField));
    g_canbus.type_s_r = (jobject) env->NewGlobalRef(env->GetStaticObjectField(clsCanbusFrameType, typeStandardRemoteField));
    g_canbus.type_e_r= (jobject) env->NewGlobalRef(env->GetStaticObjectField(clsCanbusFrameType, typeExtendedRemoteField));
    // end FlexCAN

    return JNI_VERSION_1_6;
}
