LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := suspend_service
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := EXECUTABLES

LOCAL_CFLAGS += -std=c++1y


LOCAL_PROPRIETARY_MODULE := true

LOCAL_SRC_FILES := AWakeLock.cpp suspend_service.cpp
LOCAL_SHARED_LIBRARIES := \
	libbinder \
	libhardware \
	libhardware_legacy \
	libcutils \
	liblog \
	libutils \
	libpowermanager

LOCAL_MODULE_PATH := $(TARGET_OUT_EXECUTABLES)

include $(BUILD_EXECUTABLE)
