LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := Sensor_calibrate.cpp

LOCAL_MODULE := libSensor_calibrate
LOCAL_MODULE_TAGS := optional
LOCAL_CFLAGS := -Wall
LOCAL_C_INCLUDES := vendor/qcom/proprietary/fastmmi/libmmi \
                    external/connectivity/stlport/stlport

LOCAL_SHARED_LIBRARIES := libcutils libutils libmmi libhardware

LOCAL_C_INCLUDES += $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr/include
LOCAL_C_INCLUDES += hardware/qcom/sensors
ifeq ($(TARGET_COMPILE_WITH_MSM_KERNEL),true)
LOCAL_ADDITIONAL_DEPENDENCIES := $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr
endif
LOCAL_CERTIFICATE := platform
include $(BUILD_SHARED_LIBRARY)