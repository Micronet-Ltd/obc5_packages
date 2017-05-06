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

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env, jobject instance, jboolean listeningModeEnable, jint bitrate, jboolean termination, jobjectArray  hardwarefilter, int port_number) {

    char *port;
    struct FLEXCAN_filter_mask filter_array[24];
    int numfilter = env->GetArrayLength (hardwarefilter);
    int i=0,f=0,m=0,fmt=0;
    int total_masks=0;
    int total_filters = 0;
    int total_filter_mask_types=0;

    for (i = 0; i < numfilter; i++) {
        jobject element = env->GetObjectArrayElement(hardwarefilter, i);

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
        jmethodID methodFilterType = env->GetMethodID(cls, "getFilterMaskType", "()[I");
        jintArray FilterType = (jintArray)env->CallObjectMethod(element, methodFilterType);
        jint* filterMaskTypeInts = env->GetIntArrayElements(FilterType, NULL);
        jsize lengthOfFilterMaskTypeArray = env->GetArrayLength(FilterType);

        //Saving Filter ids
        filter_array[i].filter_count = lengthOfArray;
        for (f = 0; f < lengthOfArray; f++) {
            filter_array[i].filter_id[f] = ints[f];
            total_filters++;
        }

        // Saving the mask ids
        filter_array[i].mask_count = lengthOfMaskArray;
        for (m = 0; m < lengthOfMaskArray; m++) {
            filter_array[i].mask_id[m] = maskInts[m];
            total_masks++;
        }

        //Saving Filter and Mask types
        filter_array[i].filter_mask_type_count = lengthOfFilterMaskTypeArray;
        for (fmt= 0; fmt < lengthOfFilterMaskTypeArray; fmt++) {
			if (fmt < MAX_QB_CAN_FILTERS)
			{
            	filter_array[i].filter_mask_type[fmt] = filterMaskTypeInts[fmt];
            	total_filter_mask_types++;
			}
			else
			{
        		throwException(env, "Hardware Filter: %s tried to pass array index of filter_mask_type", "err");
			}
            filter_array[i].filter_mask_type[fmt] = filterMaskTypeInts[fmt];
            total_filter_mask_types++;
        }
    }

    if (total_filters > 24){
        char str_filters [20];
        snprintf(str_filters, sizeof(str_filters), "%d", i);
        throwException(env, "Hardware Filter: Too many filter ids (%s). Max allowed - 24", str_filters);
    }

    if (total_masks > 16){
        char str_masks [20];
        snprintf(str_masks, sizeof(str_masks), "%d", total_masks);
        throwException(env, "Hardware Filter: Too many mask ids (%s). Max allowed - 16", str_masks);
    }
    int ttyport_number= port_number;
    if (ttyport_number==2){port= CAN1_TTY;}
    else if (ttyport_number==3){port= CAN2_TTY;}

    jint fd = FlexCAN_startup(listeningModeEnable, bitrate, termination, filter_array, numfilter,port);
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



