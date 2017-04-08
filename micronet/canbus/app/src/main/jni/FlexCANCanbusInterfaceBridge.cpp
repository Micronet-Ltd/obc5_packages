//
// Created by eemaan.siddiqi on 3/6/2017.
//

#include <jni.h>
#define LOG_TAG "Canbus"

#include "canbus.h"
#include "FlexCANComm.h"
#include "FlexCANCommand.h"

#define SYSTEM_ERROR -1

struct FLEXCAN_filter_mask g_filter_mask;

static void throwException(JNIEnv *env, const char *message, const char* add) {
    char msg[128];
    sprintf(msg, message, add);
    jclass cls = env->FindClass("java/lang/IllegalArgumentException");
    if (cls == 0) {
        return;
    }
    env->ThrowNew(cls, msg);
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env, jobject instance, jboolean listeningModeEnable, jint bitrate, jboolean termination, jobjectArray  hardwarefilter) {

    jint fd = FlexCAN_startup(listeningModeEnable, bitrate, termination,hardwarefilter);
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
     int i=0,f=0,m=0,mt=0,ft=0;
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
         jmethodID methodMaskId = env->GetMethodID(cls, "getMask", "()[I");
         jintArray masks = (jintArray)env->CallObjectMethod(element, methodMaskId);
         jint* maskInts = env->GetIntArrayElements(masks, NULL);
         jsize lengthOfMaskArray = env->GetArrayLength(masks);

         //Get filter and Mask type array
         jmethodID methodFilterType = env->GetMethodID(cls, "getFilterType", "()[I");
         jintArray FilterType = (jintArray)env->CallObjectMethod(element, methodFilterType);
         jint* filterTypeInts = env->GetIntArrayElements(FilterType, NULL);
         jsize lengthOfFilterTypeArray = env->GetArrayLength(FilterType);


         filter_array[i].filter_count = lengthOfArray;
         for (f = 0; f < lengthOfArray; f++) {
             filter_array[i].filter_id[f] = ints[f];
             total_filters++;
         }

         filter_array[i].filter_type_count = lengthOfFilterTypeArray;
         for (ft= 0; ft < lengthOfFilterTypeArray; ft++) {
             filter_array[i].filter_type[ft] = filterTypeInts[ft];
             total_filter_types++;
         }

         // Saving the mask ids
         filter_array[i].mask_count = lengthOfMaskArray;
         for (m = 0; m < lengthOfMaskArray; m++) {
             filter_array[i].mask_id[m] = ints[m];
             total_masks++;
         }
         //Setting Mask Types for (14 masks)
         for (mt = 0; mt < lengthOfMaskArray; mt++) {
             filter_array[i].mask_type[mt] = ints[mt];
             total_mask_types++;
         }

     }

     if (total_filters > 24){
         char str_filters [20];
         snprintf(str_filters, sizeof(str_filters), "%d", i);
         throwException(env, "Hardware Filter: Too many filter ids (%s). Max allowed - 24", str_filters);
     }

    if (total_masks > 14){
        char str_masks [20];
        snprintf(str_masks, sizeof(str_masks), "%d", total_masks);
        throwException(env, "Hardware Filter: Too many mask ids (%s). Max allowed - 14", str_masks);
    }

    Flex_CAN_filter_list(filter_array, numfilter);


     return 0;
}


