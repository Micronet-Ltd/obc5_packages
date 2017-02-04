LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := canbus
LOCAL_SRC_FILES :=	canbus.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

