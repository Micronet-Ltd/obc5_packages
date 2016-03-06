LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := bouncycastle conscrypt telephony-common ims-common
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 android-support-v13 jsr305 libsecurespaces

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        src/com/android/settings/EventLogTags.logtags \
        src/com/android/cabl/ICABLService.aidl

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_SRC_FILES += \
        src/com/android/location/XT/IXTSrv.aidl \
        src/com/android/location/XT/IXTSrvCb.aidl \
        src/com/android/display/IPPService.aidl
LOCAL_PACKAGE_NAME := Settings
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include frameworks/opt/setupwizard/navigationbar/common.mk

include $(BUILD_PACKAGE)

# the graphite changes to Settings require the SecureSpaces SDK.  If
# we cannot build the SDK from source, then we look for a prebuilt
# version in vendor/graphiteplus
ifeq ($(wildcard packages/apps/SpacesManager),)
include $(CLEAR_VARS)
LIB_SS_PATH := vendor/graphiteplus/proprietary/libsecurespaces/securespaces-sdk.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
        libsecurespaces:../../../$(LIB_SS_PATH)
include $(BUILD_MULTI_PREBUILT)
endif

# Use the following include to make our test apk.
ifeq (,$(ONE_SHOT_MAKEFILE))
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
