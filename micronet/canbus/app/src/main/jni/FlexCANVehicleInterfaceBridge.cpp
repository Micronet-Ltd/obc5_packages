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

/**
 * Configure CAN Interface
 * Begin with initialising everything
 * 1. Start working with hardware filters. Get Filter, Mask and Ids and store them in a structure
 * 2. Derive the TTY port that needs to be configured
 * 3. Get the flow control codes
 * 4. Create a new interface based on the port number
 * 5. Set the right variables - file descriptors
 * */
JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANVehicleInterfaceBridge_configureCanInterface(JNIEnv *env, jobject instance, jboolean listeningModeEnable, jint bitrate, jboolean termination,jobjectArray  hardwarefilter, jint port_number,jobjectArray flowControl){
    // /Initialisation
    static char *port=NULL;
    int ttyport_number=0;
    int i=0,f=0,m=0,fmt=0;
    int total_masks = 0, total_filters = 0, total_filter_mask_types=0;
    struct FLEXCAN_Flow_Control flowControlMessageArray[8];
    struct FLEXCAN_filter_mask filter_array[24];
    int numfilter = 0;
    int numFlowControlMessages = 0;
    int x =0 , flowMesgCount = 0;

    if (hardwarefilter != NULL){
        numfilter = env->GetArrayLength (hardwarefilter);
    }

    if (flowControl != NULL){
        numFlowControlMessages = env->GetArrayLength (flowControl);
    }

    LOGD("Flow Control Messages in JNI = %d", numFlowControlMessages);

    jclass clazz = env->FindClass("com/micronet/canbus/FlexCANVehicleInterfaceBridge");

    //Get Filters and Masks from the Object
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
                //throwException(env, "Hardware Filter: %s tried to pass array index of filter_mask_type", "err");
            }
            filter_array[i].filter_mask_type[fmt] = filterMaskTypeInts[fmt];
            total_filter_mask_types++;
        }
    }

    //The maximum number of filters that can be added are 24; Otherwise the MCU overwrites the existing filters.
    if (total_filters > 24){
        char str_filters [20];
        snprintf(str_filters, sizeof(str_filters), "%d", i);
        throwException(env, "Hardware Filter: Too many filter ids (%s). Max allowed - 24", str_filters);
    }

    //The maximum number of masks that can be set are 16.
    if (total_masks > 16){
        char str_masks [20];
        snprintf(str_masks, sizeof(str_masks), "%d", total_masks);
        throwException(env, "Hardware Filter: Too many mask ids (%s). Max allowed - 16", str_masks);
    }

    //Set the correct port number
    if(port_number == 2 || port_number == 3){
        port = getPortName(port_number);
    } else{
        throwException(env, "Entered an incorrect port number: %d ", (const char *) ttyport_number);

    }

    //Get Flow control codes
    if(flowControl != NULL){

        for(x = 0; x < numFlowControlMessages; x++){

            jobject  flowElement = env->GetObjectArrayElement(flowControl, x);
            jclass flowClass = env->GetObjectClass(flowElement);

            jmethodID methodSearchId = env->GetMethodID(flowClass, "getSearchId", "()I");
            uint32_t searchId = (uint32_t) env->CallIntMethod(flowElement,methodSearchId);
            flowControlMessageArray[x].search_id = searchId;

            jmethodID methodResponseId = env->GetMethodID(flowClass, "getResponseId", "()I");
            uint32_t responseId = (uint32_t) env->CallIntMethod(flowElement,methodResponseId);
            flowControlMessageArray[x].response_id = responseId;

            jmethodID methodMesgType = env->GetMethodID(flowClass, "getFlowMessageType", "()I");
            uint8_t mesgType = (uint8_t) env->CallIntMethod(flowElement,methodMesgType);
            flowControlMessageArray[x].flow_msg_type = mesgType;

            jmethodID methodDataResponseLength = env->GetMethodID(flowClass, "getFlowDataLength", "()I");
            uint8_t mesgLength = (uint8_t) env->CallIntMethod(flowElement,methodDataResponseLength);
            flowControlMessageArray[x].flow_msg_data_length = mesgLength;

            jmethodID methodResponseDataBytes=env->GetMethodID(flowClass,"getDataBytes","()[B");
            jbyteArray responseDataBytes=(jbyteArray)env->CallObjectMethod(flowElement, methodResponseDataBytes);
            jbyte *bufferPtr = env->GetByteArrayElements(responseDataBytes,NULL);
            jsize lengthOfR=env->GetArrayLength(responseDataBytes);

            for(int i = 0; i < flowControlMessageArray[x].flow_msg_data_length; i ++){
                flowControlMessageArray[i].response_data_bytes[i]= (uint8_t) bufferPtr[i];
                LOGD("Response data bytes from Java: %d ----> Response data bytes stored: %d", (uint8_t) bufferPtr[i],flowControlMessageArray[i].response_data_bytes[i]);
            }

            //TODO: Set Codes
            flowMesgCount ++;
            LOGD("Flow Message %d stored", flowMesgCount);
        }

        if(flowMesgCount > 8){
            char str [20];
            snprintf(str, sizeof(str), "%d", i);
            throwException(env, "Flow Messages: Too many flow codes (%s) set. Max allowed - 8", str);
        }
    }

    //Create a interface
    if (port_number==2) {
        jint fdCanPort1 = FlexCAN_startup(listeningModeEnable, bitrate, termination, filter_array, numfilter, port, flowControlMessageArray, numFlowControlMessages);
        jfieldID fd_id;
        fd_id = env->GetFieldID(clazz, "fdCanPort1", "I");
        env->SetIntField(instance, fd_id, fdCanPort1);
        if (fdCanPort1 < 0){
          return SYSTEM_ERROR;
        }
    }
    else if (port_number==3){
        jint fdCanPort2 = FlexCAN_startup(listeningModeEnable, bitrate, termination, filter_array, numfilter, port, flowControlMessageArray, numFlowControlMessages);
        jfieldID fd_id;
        fd_id = env->GetFieldID(clazz, "fdCanPort2", "I");
        env->SetIntField(instance, fd_id, fdCanPort2);
        if (fdCanPort2 < 0){
          return SYSTEM_ERROR;
        }
    }

    return 0;
    error:
    return SYSTEM_ERROR;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANVehicleInterfaceBridge_removeCAN1Interface(JNIEnv *env, jobject instance) {

    closeCAN1Thread();
    if (closePort(CAN1_TTY_NUMBER) == -1) {
        return -1;
        LOGD("Couldn't close CAN1 successfully ");
    }

    return 0;
    error:
    return SYSTEM_ERROR;

}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANVehicleInterfaceBridge_removeCAN2Interface(JNIEnv *env, jobject instance) {

    closeCAN2Thread();
    if(closePort(CAN2_TTY_NUMBER) == -1) {
        return -1;
        LOGD("Couldn't close CAN2 successfully ");
    }

    return 0;
    error:
    return SYSTEM_ERROR;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANVehicleInterfaceBridge_createJ1708Interface(JNIEnv *env, jobject instance){

    LOGD("Creating a J1708 Interface");
//    jfieldID fd_read_id;
//    jfieldID fd_write_id;

//    jint fdJ1708Read = FlexCAN_j1708_startup(J1708_TTY_READ);
//    jint fdJ1708Write = getFd(J1708_TTY_WRITE_NUMBER);

      jfieldID fd_1708;
      jint fdJ1708 = FlexCAN_j1708_startup(J1708_TTY);

      jclass clazz = env->FindClass("com/micronet/canbus/FlexCANVehicleInterfaceBridge");

//    fd_read_id = env->GetFieldID(clazz, "fdJ1708Read", "I");
//    env->SetIntField(instance, fd_read_id, fdJ1708Read);

//    fd_write_id = env->GetFieldID(clazz, "fdJ1708Write", "I");
//    env->SetIntField(instance, fd_write_id, fdJ1708Write);

      fd_1708 = env->GetFieldID(clazz, "fdJ1708", "I");
      env->SetIntField(instance, fd_1708, fdJ1708);

    return 0;

    error:
    return SYSTEM_ERROR;
}

JNIEXPORT jint JNICALL Java_com_micronet_canbus_FlexCANVehicleInterfaceBridge_removeJ1708Interface(JNIEnv *env, jobject instance){

    //LOGD("Entered removeJ1708Interface: Begin deinit()!!");
    closeJ1708Thread();
    //LOGD("Closing J1708 file descriptors");
    if (closePort(J1708_TTY_NUMBER) == -1) {
        return -1;
        LOGD("Couldn't close J1708_READ successfully ");
    }
   /* if (closePort(J1708_TTY_READ_NUMBER) == -1) {
        return -1;
        LOGD("Couldn't close J1708_READ successfully ");
    }
    if (closePort(J1708_TTY_WRITE_NUMBER) == -1) {
        return -1;
        LOGD("Couldn't close J1708_READ successfully ");
    }*/
    LOGD("Removing J1708 Interface");
    return 0;
    error:
    return SYSTEM_ERROR;
}



