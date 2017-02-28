#include <string.h> 
#include <jni.h>
#include <android/log.h>
#include <errno.h>
#include <fcntl.h>
#include <linux/input.h>
#include <hardware/hardware.h>
#include <utils/Timers.h>
#define LOG_TAG "jni"

#include <hardware/hardware.h>
#include <utils/Timers.h>
#include "mmi_module.h"
#include "sensors_extension.h"
//#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

//#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define DEVICE_NAME "/dev/input/event1"

extern "C" { 
int fd;

/**
* Defined case run in mmi mode,this mode support UI.
*
*/
static const char *subcmd_calibration = "calibration";

static const char *extra_cmd_list[] = {
    subcmd_calibration,
};

static struct sensors_poll_device_1_ext_t *device = NULL;
static struct sensor_t const *sensor_list = NULL;
static int dev_count = 0;
static int cur_sensor_type = 0;
static char cur_module_name[32];
static int calibration_result = FAILED;
static mutex_locker g_mutex;

static int sensor_enable(int sensor_type, int delay, bool enable) {
    int err = FAILED;

    if(sensor_list == NULL || sensor_type <= 0) {
        ALOGE("Invalid sensor number %d passed to initSensor", sensor_type);
        return FAILED;
    }

    for(int i = 0; i < dev_count; i++) {
        if(sensor_list[i].type == sensor_type) {

            if(enable)
                device->setDelay((sensors_poll_device_t *) device, sensor_list[i].handle, ms2ns(delay));

            err = device->activate((sensors_poll_device_t *) device, sensor_list[i].handle, enable);
            if(err != SUCCESS) {
                ALOGE("activate() for '%s'failed (%s)\n", sensor_list[i].name, strerror(-err));
            }
            break;
        }
    }

    return err;
}

static int do_calibration(int sensor_type) {
    int i = 0;
    int ret = FAILED;
    bool found = false;
    struct cal_cmd_t para;

    memset(&para, 0, sizeof(cal_cmd_t));
    for(i = 0; i < dev_count; i++) {
        if(sensor_list[i].type == sensor_type) {
            switch (sensor_list[i].type) {
            case SENSOR_TYPE_ACCELEROMETER:
                found = true;
                para.axis = 3;
                para.save = 1;
                para.apply_now = 1;
                break;
            case SENSOR_TYPE_PROXIMITY:
                found = true;
                para.axis = 2;
                para.save = 1;
                para.apply_now = 1;
                break;
            default:
                break;
            }
            break;
        }
    }

    if(found && !sensor_enable(sensor_type, 0, false)) {
        ret =
            device->calibrate(reinterpret_cast < struct sensors_poll_device_1_ext_t *>(device),
                              sensor_list[i].handle, &para);
		
    }
	ALOGE("do_calibration %d /n", ret);
    return ret;
}



static int32_t module_run_calibration(int type) {
    int ret = FAILED;

    mutex_locker::autolock _L(g_mutex);
    if(type == SENSOR_TYPE_PROXIMITY){
    	cur_sensor_type = SENSOR_TYPE_PROXIMITY;
    }else{
    	cur_sensor_type = SENSOR_TYPE_ACCELEROMETER;
    }
    ALOGE("module_run_calibration  = %d /n", SENSOR_TYPE_ACCELEROMETER);
    ALOGE("module_run_calibration  = %d /n", SENSOR_TYPE_PROXIMITY);
    calibration_result = do_calibration(cur_sensor_type);

    sensor_enable(cur_sensor_type, 200, true);

    return calibration_result;
}




static int32_t module_init() {
    ALOGI("%s start ", __FUNCTION__);
    struct sensors_module_t *hal_mod = NULL;
    int err = FAILED;
    int i = 0;

    err = hw_get_module(SENSORS_HARDWARE_MODULE_ID, (hw_module_t const **) &hal_mod);
    if(err != 0) {
        ALOGE("FFBM SENSOR: hw_get_module() failed (%s)\n", strerror(-err));
        return FAILED;
    }

    err = sensors_open_ext(&hal_mod->common, &device);
    if(err != 0) {
        ALOGE("FFBM SENSOR: sensors_open_ext() failed (%s)\n", strerror(-err));
        return FAILED;
    }

    dev_count = hal_mod->get_sensors_list(hal_mod, &sensor_list);
    for(i = 0; i < dev_count; i++) {
        ALOGI("FFBM SENSOR: Deactivating all sensor after open,current index: %d", i);
        err = device->activate((sensors_poll_device_t *) device, sensor_list[i].handle, 0);
        if(err != SUCCESS) {
            ALOGE("FFBM SENSOR: deactivate() for '%s'failed (%s)\n", sensor_list[i].name, strerror(-err));
            sensors_close_ext(device);
            return FAILED;
        }
    }

    return SUCCESS;
}

static int32_t module_deinit() {
    ALOGI("%s start.", __FUNCTION__);
 

    return SUCCESS;
}

static int32_t module_stop() {
    ALOGI("%s start.", __FUNCTION__);

    sensor_enable(cur_sensor_type, 0, false);

    return SUCCESS;
}

// JNIEXPORT jint JNICALL
//
//Java_com_qrt_factory_calibrate_CalibrateServerManager_Opentp( JNIEnv* env,jobject thiz)
//
//{
//	system("echo 1 > /sys/ft5x46_tp_gesture/tp_gesture_enable");
//	return 0;
//}


 JNIEXPORT jint JNICALL

Java_com_qrt_factory_calibrate_CalibrateServerManager_SensorCalibrate( JNIEnv* env,jobject thiz,jint type)

{
	jint ret = -1;
	module_init();
	ret = module_run_calibration(type);
	return ret;
}
}


