LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_SRC_FILES:= frame.c main.c
LOCAL_MODULE := testframe
LOCAL_MODULE_TAGS := eng
LOCAL_CFLAGS += -std=c99 -DUSE_THREADS
#LOCAL_STATIC_LIBRARIES := libcutils libc
LOCAL_SHARED_LIBRARIES := libcutils libc
include $(BUILD_EXECUTABLE)
