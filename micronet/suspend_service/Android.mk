LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:= suspend_service.c
LOCAL_MODULE := suspend_service
LOCAL_MODULE_TAGS := optional
LOCAL_CFLAGS += -std=c99 -DUSE_THREADS -D__ANDROID__
LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_STATIC_LIBRARIES := libcutils libc liblog libutils
#LOCAL_SHARED_LIBRARIES := libcutils libc liblog libutils
LOCAL_C_INCLUDES += $(JNI_H_INCLUDE):
include $(BUILD_EXECUTABLE)
