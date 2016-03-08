LOCAL_PATH:= $(call my-dir)
    include $(CLEAR_VARS)
    LOCAL_MODULE_TAGS := optional
    LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 locSDK
    LOCAL_JAVA_LIBRARIES := telephony-common
    LOCAL_SRC_FILES := $(call all-subdir-java-files)
    LOCAL_CERTIFICATE := platform
    LOCAL_PACKAGE_NAME := SuperCompass
    #LOCAL_DEX_PREOPT := false
    LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)
				