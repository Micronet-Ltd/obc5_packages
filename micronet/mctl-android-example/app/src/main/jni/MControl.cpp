#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include "iosocket.h"
#include "api.h"

#ifdef __cplusplus
extern "C" {
#endif

#define RTC_STRING_SIZE 23

enum LED {
    GPS,
    VIOLATION
};

static pthread_mutex_t mutexlock;

JNIEXPORT jstring JNICALL
Java_com_micronet_mcontrol_MControl_jniGetMCUVersion(JNIEnv *env, jclass type) {
    uint8_t data[255];
    int result = 0;
    jstring jresult = NULL;

    int fd = iosocket_connect();
    if (fd != 0) {
        result = get_mcu_version(&fd, data, 4);
        snprintf((char *)data, sizeof(data), "%X.%X.%X.%X", data[0], data[1], data[2], data[3]);
        iosocket_disconnect(&fd);
        jresult = env->NewStringUTF((char *)data);
        return jresult;
    }

    return jresult;
}

JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniGetFPGAVersion(JNIEnv *env, jclass type) {

    uint32_t fpga_ver = 0;
    int result = 0;

    int fd = iosocket_connect();
    if (fd != 0) {
        result = get_fpga_version(&fd, &fpga_ver, 4);
        //LOGI("result: %d, FPGA Version: %X", result, fpga_ver);

        iosocket_disconnect(&fd);

        return fpga_ver;
    }

    return fpga_ver;

}

JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniGetADCorGPIVoltage(JNIEnv *env, jclass type, jint gpi_num) {
    uint32_t voltage = 0;
    int result = 0;

    int fd = iosocket_connect();
    if (fd != 0) {
        result = get_adc_or_gpi_voltage(&fd, gpi_num, &voltage, sizeof(voltage));
        //LOGI("result: %d, FPGA Version: %X", result, fpga_ver);

        iosocket_disconnect(&fd);
    }

    return voltage;
}

JNIEXPORT jintArray JNICALL
Java_com_micronet_mcontrol_MControl_jniGetLEDStatus(JNIEnv *env, jobject instance, jint led_num) {
    int size = 4;
    jintArray newArray = env->NewIntArray(size);

    int result = 0;

    int fd = iosocket_connect();
    if (fd != 0) {
        jint tmp[3];
        uint8_t brightness, red, green, blue;
        result = get_led_status(&fd, led_num, &brightness, &red, &green, &blue);
        iosocket_disconnect(&fd);
        tmp[0] = red;
        tmp[1] = green;
        tmp[2] = blue;
        tmp[3] = brightness;
        // copy jint[] to jintarray
        env->SetIntArrayRegion(newArray, 0, size, tmp);
    }

    return newArray;
}

JNIEXPORT void JNICALL
Java_com_micronet_mcontrol_MControl_jniSetLEDValue(JNIEnv *env, jclass type, jint led, jint rgb) {

    int result = 0;

    uint8_t brightness = 0xFF;
    uint8_t red = (rgb & 0xFF0000) >> 16;
    uint8_t green = (rgb & 0x00FF00) >> 8;
    uint8_t blue = rgb & 0x0000FF;

    pthread_mutex_lock(&mutexlock);
    int fd = iosocket_connect();
    if (fd != 0) {
        result = set_led_status(&fd, led, brightness, red, green, blue);
        //LOGI("LED: %d, red: 0x%02X, green: 0x%02X, blue: 0x%02X, result: %d", led, red, green, blue, result);

        iosocket_disconnect(&fd);
    }
    pthread_mutex_unlock(&mutexlock);

}

JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniGetPowerOnThresholdCfg(JNIEnv *env, jobject instance) {

    // TODO

}

JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniSetPowerOnThresholdCfg(JNIEnv *env, jobject instance) {

    // TODO

}

JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniGetPowerOnReason(JNIEnv *env, jobject instance) {

    // TODO

}

JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniSetDevicePowerOff(JNIEnv *env, jobject instance) {

    // TODO

}

JNIEXPORT jstring JNICALL
Java_com_micronet_mcontrol_MControl_jniGetRTCDateTime(JNIEnv *env, jobject instance) {
    char dt_str[RTC_STRING_SIZE] = "2016-03-29 19:09:06.58";
    int result = 0;
    jstring jresult = NULL;

    int fd = iosocket_connect();

    if (fd != 0) {
        result = get_rtc_date_time(&fd, dt_str);
        iosocket_disconnect(&fd);
        jresult = env->NewStringUTF(dt_str);
    }

    return jresult;
}

/**
 *  Expected dt_str format: year-month-day hour:min:sec.deciseconds
 * 					  Ex : 2016-03-29 19:09:06.58
 * 	Returns result, 0 is bad
 */
JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniSetRTCDateTime(JNIEnv *env, jobject instance, jstring time) {

    char * dt_str = (char *)env->GetStringUTFChars(time, JNI_FALSE);

    int result = 0;
    int fd = iosocket_connect();

    if (fd != 0) {
        result = set_rtc_date_time(&fd, dt_str);
        iosocket_disconnect(&fd);
    }

    return result;
}

JNIEXPORT jintArray JNICALL
Java_com_micronet_mcontrol_MControl_jniGetRTCCalReg(JNIEnv *env, jobject instance) {
    uint8_t rtc_dig_cal, rtc_analog_cal;
    jintArray jarr = env->NewIntArray(2);
    jint *narr = env->GetIntArrayElements(jarr, NULL);

    int result = 0;
    int fd = iosocket_connect();

    if (fd != 0) {
        result = get_rtc_cal_reg(&fd, &rtc_dig_cal, &rtc_analog_cal);
        printf("get rtc cal registers, dig cal: %x analog cal: %x, ret = %d  \n", \
					rtc_dig_cal, rtc_analog_cal, result);
        iosocket_disconnect(&fd);
        narr[0] = rtc_dig_cal;
        narr[1] = rtc_analog_cal;
    }

    env->ReleaseIntArrayElements(jarr, narr, NULL);
    return jarr;
}

JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniSetRTCCalReg(JNIEnv *env, jobject instance) {

    // TODO

}

JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniGetRTCRegDBG(JNIEnv *env, jobject instance) {

    // TODO

}

JNIEXPORT jint JNICALL
Java_com_micronet_mcontrol_MControl_jniSetRTCRegDBG(JNIEnv *env, jobject instance) {

    // TODO

}

/**
 * returns false if RTC battery is bad or register could not be read.
 * returns true if RTC battery is good.
 */
JNIEXPORT jboolean JNICALL
Java_com_micronet_mcontrol_MControl_jniCheckRTCBattery(JNIEnv *env, jobject instance) {
    int result = 0;
    int fd = iosocket_connect();

    if (fd != 0) {
        result = check_rtc_battery(&fd);
        iosocket_disconnect(&fd);
    }

    // 0 indicates no response or bad RTC battery
    return result != 0;
}

#ifdef __cplusplus
}
#endif