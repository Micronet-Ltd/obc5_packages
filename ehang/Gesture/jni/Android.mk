LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CERTIFICATE := platform

LOCAL_MODULE    := libGesture
LOCAL_SRC_FILES := Gesture.c

include $(BUILD_SHARED_LIBRARY)
