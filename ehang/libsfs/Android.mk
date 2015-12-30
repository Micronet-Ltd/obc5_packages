
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_CFLAGS:= \
        -DAMSS_VERSION=$(AMSS_VERSION) \
        $(mmcamera_debug_defines) \
        $(mmcamera_debug_cflags)


LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../includes
LOCAL_C_INCLUDES += $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr/include \
                                     $(LOCAL_PATH)/../../../vendor/qcom/proprietary/securemsm/QSEEComAPI \
                                     $(TARGET_OUT_HEADERS)/common/inc
LOCAL_ADDITIONAL_DEPENDENCIES := $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr

LOCAL_SRC_FILES:= libsfs.c

LOCAL_MODULE           := libsfs
LOCAL_SHARED_LIBRARIES := libcutils \
                                              libc \
                                              libutils \
                                              libQSEEComAPI \

LOCAL_CERTIFICATE := platform

LOCAL_SHARED_LIBRARIES += liblog


LOCAL_MODULE_OWNER := qcom
LOCAL_PROPRIETARY_MODULE := true

include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)

#LOCAL_SRC_FILES:= \
#        test.c

#LOCAL_SHARED_LIBRARIES := \
#       libsfs

#LOCAL_CFLAGS := -DFEATURE_DSS_LINUX_ANDROID

#LOCAL_MODULE:= sfs_test
#LOCAL_MODULE_TAGS := optional eng

#include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
        rpmb_key.c

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../includes
LOCAL_C_INCLUDES += $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr/include \
                                     $(LOCAL_PATH)/../../../vendor/qcom/proprietary/securemsm/QSEEComAPI \
                                     $(TARGET_OUT_HEADERS)/common/inc
LOCAL_ADDITIONAL_DEPENDENCIES := $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr

LOCAL_SHARED_LIBRARIES := libcutils \
                                              libc \
                                              libutils \
                                              libQSEEComAPI \

LOCAL_CFLAGS := -DFEATURE_DSS_LINUX_ANDROID

LOCAL_MODULE:= rpmb_key
LOCAL_MODULE_TAGS := optional eng

include $(BUILD_EXECUTABLE)

