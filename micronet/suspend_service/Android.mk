LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_SRC_FILES := AWakeLock.cpp
#LOCAL_MODULE := libspsrvs
LOCAL_MODULE_TAGS := optional

LOCAL_CFLAGS := -D_ANDROID_
#LOCAL_CFLAGS += -std=c99 -DUSE_THREADS -D__ANDROID__
#LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_CFLAGS += -std=c++1y

#LOCAL_SHARED_LIBRARIES := \
	libbinder \
	libcutils \
	liblog \
	libutils \
	libpowermanager

#LOCAL_STATIC_LIBRARIES := libcutils liblog libutils 
#LOCAL_C_INCLUDES += $(JNI_H_INCLUDE):
LOCAL_PROPRIETARY_MODULE := true
#LOCAL_MODULE_OWNER := qcom
#include $(BUILD_SHARED_LIBRARY)
#####################################
#include $(CLEAR_VARS)

LOCAL_MODULE := suspend_service
LOCAL_SRC_FILES := AWakeLock.cpp suspend_service.cpp
LOCAL_SHARED_LIBRARIES := \
	libbinder \
	libhardware \
	libhardware_legacy \
	libcutils \
	liblog \
	libutils \
	libpowermanager

include $(BUILD_EXECUTABLE)
