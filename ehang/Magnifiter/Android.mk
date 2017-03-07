ifeq ($(EH_PRODUCT_NAME),Q10)
else
ifeq ($(EH_PRODUCT_NAME),Q8)
else
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := Magnifiter
LOCAL_CERTIFICATE := platform
# don't apply dalvik preoptimization to ease development
#LOCAL_DEX_PREOPT := false

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
endif
endif