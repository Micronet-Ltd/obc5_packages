ifeq ($(EH_PRODUCT_NAME),Q10)
else
ifeq ($(EH_PRODUCT_NAME),Q8)
else
LOCAL_PATH:= $(call my-dir)
    include $(CLEAR_VARS)
    LOCAL_MODULE_TAGS := optional
    LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 locSDK
    LOCAL_JAVA_LIBRARIES := telephony-common
    LOCAL_SRC_FILES := $(call all-subdir-java-files)
    LOCAL_CERTIFICATE := platform
    LOCAL_PACKAGE_NAME := SOS
    LOCAL_DEX_PREOPT := false
    LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)
   
include $(CLEAR_VARS)
	LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	locSDK:libs/locSDK_6.03.jar 
	LOCAL_C_FLAG := -dontwarn
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libs/liblocSDK6.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)
endif
endif