LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_SRC_FILES:= iodriver.c control.c accel.c util.c tty.c queue.c frame.c
LOCAL_MODULE := iodriver
LOCAL_MODULE_TAGS := eng
LOCAL_CFLAGS += -std=c99 -DUSE_THREADS
#LOCAL_STATIC_LIBRARIES := libcutils libc
LOCAL_SHARED_LIBRARIES := libcutils libc liblog libutils
LOCAL_C_INCLUDES += $(JNI_H_INCLUDE):
include $(BUILD_EXECUTABLE)
