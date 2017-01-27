LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
JNI_SRC_PATH := $(LOCAL_PATH)/src/main/jni

LOCAL_MODULE := canbus
LOCAL_SRC_FILES := $(JNI_SRC_PATH)/FlexCANCanbusInterfaceBridge.cpp \
                   $(JNI_SRC_PATH)/canbus.cpp
include $(BUILD_SHARED_LIBRARY)

