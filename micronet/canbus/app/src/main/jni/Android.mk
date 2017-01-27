LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := canbus
LOCAL_SRC_FILES := FlexCANCanbusInterfaceBridge.cpp
include $(BUILD_SHARED_LIBRARY)

