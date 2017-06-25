ifeq ($(EH_PRODUCT_NAME),Q10)
else
ifeq ($(EH_PRODUCT_NAME),Q8)
else
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_SRC_FILES := $(call all-subdir-java-files)


LOCAL_PACKAGE_NAME := PhoneAssistant
LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)
endif
endif