LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_SRC_FILES:= mctl.c iosocket.c api.c
LOCAL_MODULE := mctl
LOCAL_MODULE_TAGS := optional
LOCAL_CFLAGS += -std=c99 -DUSE_THREADS -fpermissive
#LOCAL_STATIC_LIBRARIES := libcutils libc
LOCAL_SHARED_LIBRARIES := libcutils libc
include $(BUILD_EXECUTABLE)
