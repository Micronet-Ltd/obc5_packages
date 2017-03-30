LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

$(warning $(LOCAL_SRC_FILES))
# Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14
# LOCAL_JAVA_LIBRARIES := qcnvitems qcom.fmradio qcrilhook
# Add By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14
LOCAL_JAVA_LIBRARIES := qcom.fmradio
# telephony-msim

LOCAL_JNI_SHARED_LIBRARIES := libqcomfm_jni

LOCAL_PACKAGE_NAME := QrtFactoryKit
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := disabled

#ifeq ($(strip $(TARGET_QRT_PROJECT)), X820B01) 
#		LOCAL_MANIFEST_FILE := qrt/X820B01/AndroidManifest.xml
#endif

#ifeq ($(strip $(TARGET_QRT_PROJECT)), X820C) 
#		LOCAL_MANIFEST_FILE := qrt/X820C/AndroidManifest.xml
#		LOCAL_SRC_FILES := $(subst src/com/qrt/factory/activity/TouchPanelKey.java, qrt/X820C/src/com/qrt/factory/activity/TouchPanelKey.java, $(LOCAL_SRC_FILES))
#endif

#X820H change source by wangwenlong
#ifeq ($(strip $(TARGET_QRT_PROJECT)), X820H) 
#		LOCAL_SRC_FILES := $(subst src/com/qrt/factory/activity/WiFi.java, qrt/X820H/src/com/qrt/factory/activity/WiFi.java, $(LOCAL_SRC_FILES))
#		LOCAL_SRC_FILES := $(subst src/com/qrt/factory/ControlCenter.java, qrt/X820H/src/com/qrt/factory/ControlCenter.java, $(LOCAL_SRC_FILES))
#	LOCAL_SRC_FILES := $(subst src/com/qrt/factory/TestSettings.java, qrt/X820H/src/com/qrt/factory/TestSettings.java, $(LOCAL_SRC_FILES))
#endif

include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))
