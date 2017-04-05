//
// Created by eemaan.siddiqi on 3/6/2017.
//

#include <jni.h>
#define LOG_TAG "Canbus"

#include "canbus.h"
#include "FlexCANComm.h"
#include "FlexCANCommand.h"

#define SYSTEM_ERROR -1

static void throwException(JNIEnv *env, const char *message, const char* add) {
    char msg[128];
    sprintf(msg, message, add);
    jclass cls = env->FindClass("java/lang/IllegalArgumentException");
    if (cls == 0) {
        return;
    }
    env->ThrowNew(cls, msg);
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env, jobject instance, jboolean listeningModeEnable, jint bitrate, jboolean termination) {

    jint fd = FlexCAN_startup(listeningModeEnable, bitrate, termination);
    jfieldID fd_id;

    jclass clazz = env->FindClass("com/micronet/canbus/FlexCANCanbusInterfaceBridge");

    fd_id = env->GetFieldID(clazz, "fd", "I");
    env->SetIntField(instance, fd_id, fd);

    return 0;

    error:
    return SYSTEM_ERROR;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_removeInterface(JNIEnv *env, jobject instance) {

    // TODO close Canbus
    int false_fd;
    qb_close();

    return closeCAN(false_fd);
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_setHardwareFilter(JNIEnv *env, jobject obj, jobjectArray hardwareFilters) {

     struct FLEXCAN_filter_mask filter_array[24];
     int numfilter = env->GetArrayLength (hardwareFilters);
     int i,f, m, mt,ft;
     int total_masks=0;
     int total_filters = 0;
     int total_mask_types=0;
     int total_filter_types=0;

     for (i = 0; i < numfilter; i++) {
         jobject element = env->GetObjectArrayElement(hardwareFilters, i);

         //get filter ids array
         jclass cls = env->GetObjectClass(element);
         jmethodID methodId = env->GetMethodID(cls, "getIds", "()[I");
         jintArray ids = (jintArray)env->CallObjectMethod(element, methodId);
         jint* ints = env->GetIntArrayElements(ids, NULL);
         jsize lengthOfArray = env->GetArrayLength(ids);

         //get masks array
         //new
         jmethodID methodMaskId = env->GetMethodID(cls, "getMask", "()[I");
         jintArray masks = (jintArray)env->CallObjectMethod(element, methodMaskId);
         jint* maskInts = env->GetIntArrayElements(masks, NULL);
         jsize lengthOfMaskArray = env->GetArrayLength(masks);

         //get filter frame type
         methodId = env->GetMethodID(cls, "getFilterType", "()Lcom/micronet/canbus/CanbusFrameType;");
         jobject o = env->CallObjectMethod(element, methodId);
         cls = env->FindClass("com/micronet/canbus/CanbusFrameType");
         g_canbus.typeField = env->GetFieldID(cls, "mType", "I");
         int type = env->GetIntField(o, g_canbus.typeField);

/*
         //get Mask types
         jmethodID methodMaskType = env->GetMethodID(cls, "getMaskType", "()[Lcom/micronet/canbus/CanbusFrameType;");
         jobject o = env->CallObjectMethod(element, methodId);
         jintArray masksType = (jintArray)env->CallObjectMethod(element, methodMaskType);
         jint* maskTypeInts = env->GetIntArrayElements(masksType, NULL);
         jsize lengthOfMaskTypeArray = env->GetArrayLength(masksType);

         //Get filter type array
         jmethodID methodFilterType = env->GetMethodID(cls, "getFilterType", "()[I");
         jintArray FilterType = (jintArray)env->CallObjectMethod(element, methodFilterType);
         jint* filterTypeInts = env->GetIntArrayElements(masksType, NULL);
         jsize lengthOfFilterTypeArray = env->GetArrayLength(masksType);
*/


         filter_array[i].count = lengthOfArray;
         for (f = 0; f < lengthOfArray; f++) {
             filter_array[i].filter_id[f] = ints[f];
             total_filters++;
         }

      /*   filter_array[i].filter_type_count = lengthOfFilterTypeArray;
         for (ft= 0; ft < lengthOfFilterTypeArray; ft++) {
             filter_array[i].filter_type[ft] = filterTypeInts[ft];
             total_filter_types++;
         }*/

         filter_array[i].mask_count = lengthOfMaskArray;
         for (m = 0; m < lengthOfMaskArray; m++) {
             filter_array[i].mask_id[m] = maskInts[m];
             total_masks++;
         }


     }

     if (total_filters > 24){
         char str_filters [20];
         snprintf(str_filters, sizeof(str_filters), "%d", i);
         throwException(env, "Hardware Filter: Too many filter ids (%s). Max allowed - 24", str_filters);
     }

    if (total_masks > 24){
        char str_masks [20];
        snprintf(str_masks, sizeof(str_masks), "%d", i);
        throwException(env, "Hardware Filter: Too many mask ids (%s). Max allowed - 24", str_masks);
    }

    //check if the number of masks have same number of mask types


     return 0;
}


