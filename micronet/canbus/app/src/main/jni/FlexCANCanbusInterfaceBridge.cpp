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

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_createInterface(JNIEnv *env, jobject instance, jboolean listeningModeEnable, jint bitrate, jboolean termination, jobjectArray  hardwarefilter, int port_number,jobjectArray flowControl) {

    char *port=NULL;
    int ttyport_number=0;

    struct FLEXCAN_filter_mask filter_array[24];
    int numfilter = env->GetArrayLength (hardwarefilter);
    int i=0,f=0,m=0,fmt=0;
    int total_masks=0;
    int total_filters = 0;
    int total_filter_mask_types=0;

    struct FLEXCAN_Flow_Control flowControlMessageArray[8];
    int numFlowControlMessages;

    for (i = 0; i < numfilter; i++){

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
			if (fmt < MAX_FLEXCAN_CAN_FILTERS)
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
    ttyport_number= port_number;

    if (ttyport_number==2){port= CAN1_TTY;}
    else if (ttyport_number==3){port= CAN2_TTY;}
    else throwException(env, "Entered an incorrect port number: %d ", (const char *) ttyport_number);


    if(flowControl!=NULL) {
        struct FLEXCAN_Flow_Control flowControlMessageArray[8];
        int numFlowControlMessages = env->GetArrayLength(flowControl);
        int totalSearchIds = 0;
        int totalResponseIds = 0;
        int totalIdTypes = 0;
        int totalIdDataLengths = 0;
        int totalResponseDatabytes1 = 0;
        int totalResponseDatabytes2 = 0;
        int totalResponseDatabytes3 = 0;
        int totalResponseDatabytes4 = 0;
        int totalResponseDatabytes5 = 0;
        int totalResponseDatabytes6 = 0;
        int totalResponseDatabytes7 = 0;
        int totalResponseDatabytes8 = 0;


        int j = 0;
        int sids, rids, idtypes, datalengths, databytes1, databytes2,databytes3,databytes4,databytes5,databytes6,databytes7,databytes8;

        for (j = 0; j < numFlowControlMessages; j++) {

            jobject flowElement = env->GetObjectArrayElement(flowControl, j);

            jclass flowClass = env->GetObjectClass(flowElement);

            //get search ids array
            jmethodID methodSearchId = env->GetMethodID(flowClass, "getSearchIds", "()[I");
            jintArray searchIds = (jintArray) env->CallObjectMethod(flowElement, methodSearchId);
            jint *intsSearchIds = env->GetIntArrayElements(searchIds, NULL);
            jsize lengthOfSearchIdArray = env->GetArrayLength(searchIds);

            //get response ids array
            jmethodID methodResponseId = env->GetMethodID(flowClass, "getResponseIds", "()[I");
            jintArray responseIds = (jintArray) env->CallObjectMethod(flowElement,methodResponseId);
            jint *intsResponseIds = env->GetIntArrayElements(responseIds, NULL);
            jsize lengthOfResponseIdArray = env->GetArrayLength(responseIds);

            //Get search and response Ids type array
            jmethodID methodIdType = env->GetMethodID(flowClass, "getIdType", "()[I");
            jintArray idType = (jintArray) env->CallObjectMethod(flowElement, methodIdType);
            jint *idTypeInts = env->GetIntArrayElements(idType, NULL);
            jsize lengthOfIdTypeArray = env->GetArrayLength(idType);

            //Get response Ids data length array
            jmethodID methodIdDataLength = env->GetMethodID(flowClass, "getFlowDataLength", "()[I");
            jintArray idDataLength = (jintArray) env->CallObjectMethod(flowElement, methodIdDataLength);
            jint *idDataLengthInts = env->GetIntArrayElements(idDataLength, NULL);
            jsize lengthOfIdDataLengthArray = env->GetArrayLength(idDataLength);

            //TODO: Fix me
       /*     jmethodID methodResponseDataBytes1=env->GetMethodID(flowClass,"getDataBytes","()[[B");
            jbyteArray responseDataBytes1=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes1);
            jsize lengthOfResponseArray1=env->GetArrayLength(responseDataBytes1);
            LOGD("Response Data bytes=%d", responseDataBytes1[0]);*/

            jmethodID methodResponseDataBytes1=env->GetMethodID(flowClass,"getDataBytes1","()[B");
            jbyteArray responseDataBytes1=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes1);
            jbyte *bufferPtr1=env->GetByteArrayElements(responseDataBytes1,NULL);
            jsize lengthOfResponseArray1=env->GetArrayLength(responseDataBytes1);

            jmethodID methodResponseDataBytes2=env->GetMethodID(flowClass,"getDataBytes2","()[B");
            jbyteArray responseDataBytes2=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes2);
            jbyte *bufferPtr2=env->GetByteArrayElements(responseDataBytes2,NULL);
            jsize lengthOfResponseArray2=env->GetArrayLength(responseDataBytes2);

            jmethodID methodResponseDataBytes3=env->GetMethodID(flowClass,"getDataBytes3","()[B");
            jbyteArray responseDataBytes3=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes3);
            jbyte *bufferPtr3=env->GetByteArrayElements(responseDataBytes3,NULL);
            jsize lengthOfResponseArray3=env->GetArrayLength(responseDataBytes3);

            jmethodID methodResponseDataBytes4=env->GetMethodID(flowClass,"getDataBytes4","()[B");
            jbyteArray responseDataBytes4=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes4);
            jbyte *bufferPtr4=env->GetByteArrayElements(responseDataBytes4,NULL);
            jsize lengthOfResponseArray4=env->GetArrayLength(responseDataBytes4);

            jmethodID methodResponseDataBytes5=env->GetMethodID(flowClass,"getDataBytes5","()[B");
            jbyteArray responseDataBytes5=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes5);
            jbyte *bufferPtr5=env->GetByteArrayElements(responseDataBytes5,NULL);
            jsize lengthOfResponseArray5=env->GetArrayLength(responseDataBytes5);

            jmethodID methodResponseDataBytes6=env->GetMethodID(flowClass,"getDataBytes6","()[B");
            jbyteArray responseDataBytes6=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes6);
            jbyte *bufferPtr6=env->GetByteArrayElements(responseDataBytes6,NULL);
            jsize lengthOfResponseArray6=env->GetArrayLength(responseDataBytes6);

            jmethodID methodResponseDataBytes7=env->GetMethodID(flowClass,"getDataBytes7","()[B");
            jbyteArray responseDataBytes7=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes7);
            jbyte *bufferPtr7=env->GetByteArrayElements(responseDataBytes7,NULL);
            jsize lengthOfResponseArray7=env->GetArrayLength(responseDataBytes7);

            jmethodID methodResponseDataBytes8=env->GetMethodID(flowClass,"getDataBytes8","()[B");
            jbyteArray responseDataBytes8=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes8);
            jbyte *bufferPtr8=env->GetByteArrayElements(responseDataBytes8,NULL);
            jsize lengthOfResponseArray8=env->GetArrayLength(responseDataBytes8);

            //Saving search ids
            flowControlMessageArray[j].search_id_count = lengthOfSearchIdArray;
            for (sids = 0; sids < lengthOfSearchIdArray; sids++) {
                flowControlMessageArray[j].search_id[sids] = intsSearchIds[sids];
                totalSearchIds++;
                LOGD("Search id stored: %d and total search ids set=%d", intsSearchIds[sids], totalSearchIds);
            }
            // Saving response ids
            flowControlMessageArray[j].response_id_count = lengthOfResponseIdArray;
            for (rids = 0; rids < lengthOfResponseIdArray; rids++) {
                flowControlMessageArray[j].response_id[rids] = intsResponseIds[rids];
                totalResponseIds++;
                LOGD("Response id stored: %d and total Response ids set=%d", intsResponseIds[rids], totalResponseIds);
            }
            //Saving Id types
            flowControlMessageArray[j].flow_msg_type_count = lengthOfIdTypeArray;
            for (idtypes = 0; idtypes < lengthOfIdTypeArray; idtypes++) {
                flowControlMessageArray[j].flow_msg_type[idtypes] = idTypeInts[idtypes];
                totalIdTypes++;
                LOGD("ID type stored: %d and total id types set=%d", idTypeInts[idtypes], totalIdTypes);
            }
            //Saving Id data lengths
            flowControlMessageArray[j].flow_msg_data_length_count = lengthOfIdDataLengthArray;
            for (datalengths = 0; datalengths < lengthOfIdDataLengthArray; datalengths++) {
                flowControlMessageArray[j].flow_msg_data_length[datalengths] = idDataLengthInts[datalengths];
                totalIdDataLengths++;
                LOGD("ID Data lengths stored: %d and total datalengths saved=%d", idDataLengthInts[datalengths], totalIdDataLengths);
            }
            //Saving response data bytes
            flowControlMessageArray[j].response_databytes1_count = lengthOfResponseArray1;
            for (databytes1 = 0; databytes1 < lengthOfResponseArray1; databytes1++) {
                flowControlMessageArray[j].response_data_bytes1[databytes1]= (uint8_t) bufferPtr1[databytes1];
                totalResponseDatabytes1++;
                LOGD("Response data bytes stored: %d and total databytes1 saved=%d", (uint8_t) bufferPtr1[databytes1], totalResponseDatabytes1);
            }

            flowControlMessageArray[j].response_databytes2_count = lengthOfResponseArray2;
            for (databytes2 = 0; databytes2 < lengthOfResponseArray2; databytes2++) {
                flowControlMessageArray[j].response_data_bytes2[databytes2]= (uint8_t) bufferPtr2[databytes2];
                totalResponseDatabytes2++;
                LOGD("Response data bytes stored: %d and total databytes2 saved=%d", (uint8_t) bufferPtr2[databytes2], totalResponseDatabytes2);
            }

            flowControlMessageArray[j].response_databytes3_count = lengthOfResponseArray3;
            for (databytes3 = 0; databytes3 < lengthOfResponseArray3; databytes3++) {
                flowControlMessageArray[j].response_data_bytes3[databytes3]= (uint8_t) bufferPtr3[databytes3];
                totalResponseDatabytes3++;
                LOGD("Response data bytes stored: %d and total databytes3 saved=%d", (uint8_t) bufferPtr3[databytes3], totalResponseDatabytes3);
                LOGD("response_databytes_3 array: %d", flowControlMessageArray[j].response_data_bytes3[databytes3]);
            }

            flowControlMessageArray[j].response_databytes4_count = lengthOfResponseArray4;
            for (databytes4 = 0; databytes4 < lengthOfResponseArray4; databytes4++) {
                flowControlMessageArray[j].response_data_bytes4[databytes4]= (uint8_t) bufferPtr4[databytes4];
                totalResponseDatabytes4++;
                LOGD("Response data bytes stored: %d and total databytes4 saved=%d", (uint8_t) bufferPtr4[databytes4], totalResponseDatabytes4);
            }

            flowControlMessageArray[j].response_databytes5_count = lengthOfResponseArray5;
            for (databytes5 = 0; databytes5 < lengthOfResponseArray5; databytes5++) {
                flowControlMessageArray[j].response_data_bytes5[databytes5]= (uint8_t) bufferPtr5[databytes5];
                totalResponseDatabytes5++;
                LOGD("Response data bytes stored: %d and total databytes5 saved=%d", (uint8_t) bufferPtr5[databytes5], totalResponseDatabytes5);
            }

            flowControlMessageArray[j].response_databytes6_count = lengthOfResponseArray6;
            for (databytes6 = 0; databytes6 < lengthOfResponseArray6; databytes6++) {
                flowControlMessageArray[j].response_data_bytes6[databytes6]= (uint8_t) bufferPtr6[databytes6];
                totalResponseDatabytes6++;
                LOGD("Response data bytes stored: %d and total databytes6 saved=%d", (uint8_t) bufferPtr6[databytes6], totalResponseDatabytes6);
            }

            flowControlMessageArray[j].response_databytes7_count = lengthOfResponseArray7;
            for (databytes7 = 0; databytes7 < lengthOfResponseArray7; databytes7++) {
                flowControlMessageArray[j].response_data_bytes7[databytes7]= (uint8_t) bufferPtr7[databytes7];
                totalResponseDatabytes7++;
                LOGD("Response data bytes stored: %d and total databytes7 saved=%d", (uint8_t) bufferPtr7[databytes7], totalResponseDatabytes7);
            }

            flowControlMessageArray[j].response_databytes8_count = lengthOfResponseArray8;
            for (databytes8 = 0; databytes8 < lengthOfResponseArray8; databytes8++) {
                flowControlMessageArray[j].response_data_bytes8[databytes8]= (uint8_t) bufferPtr8[databytes8];
                totalResponseDatabytes8++;
                LOGD("Response data bytes stored: %d and total databytes8 saved=%d", (uint8_t) bufferPtr8[databytes8], totalResponseDatabytes8);
            }
        }

        if (totalSearchIds > 8 || totalResponseIds > 8 || totalIdTypes > 8 || totalIdDataLengths > 8) {
            char str_flow_message[60];

            snprintf(str_flow_message, sizeof(str_flow_message),"[SearchIds:%d, ResponseIds:%d, TotalIdTypes:%d, TotalIdDataLength:%d]", totalSearchIds, totalResponseIds, totalIdTypes, totalIdDataLengths);
            throwException(env, "FlowControlMessage Error: Received too many arguments (%s). Max allowed - 8", str_flow_message);
        }
    }


    jclass clazz = env->FindClass("com/micronet/canbus/FlexCANCanbusInterfaceBridge");

    if (port_number==2) {
        jint fd = FlexCAN_startup(listeningModeEnable, bitrate, termination, filter_array, numfilter, port, flowControlMessageArray, numFlowControlMessages);
        jfieldID fd_id;
        fd_id = env->GetFieldID(clazz, "fd_can_port1", "I");
        env->SetIntField(instance, fd_id, fd);
    }

    else if (port_number==3){
        jint fd = FlexCAN_startup(listeningModeEnable, bitrate, termination, filter_array, numfilter, port, flowControlMessageArray, numFlowControlMessages);
        jfieldID fd_id;
        fd_id = env->GetFieldID(clazz, "fd_can_port2", "I");
        env->SetIntField(instance, fd_id, fd);
    }

    //TODO : Add and else if for 1708
    return 0;

    error:
    return SYSTEM_ERROR;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANCanbusInterfaceBridge_removeInterface(JNIEnv *env, jobject instance) {

    // TODO close1939Port1 Canbus
    int false_fd;
    qb_close();

    return closeCAN(false_fd);
}



