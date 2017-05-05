LOCAL_PATH:= $(call my-dir)

# The library
include $(CLEAR_VARS)
LOCAL_SRC_FILES += $(call all-java-files-under, java)
LOCAL_MODULE := canbus_api
LOCAL_MODULE_TAGS := optional
# Hack to make static library, instead of framework, and copy into framework path
LOCAL_IS_STATIC_JAVA_LIBRARY := true
LOCAL_DEX_PREOPT := false
include $(BUILD_JAVA_LIBRARY)

# Documentation
include $(CLEAR_VARS)
$LOCAL_SRC_FILES := $(call all-subdir-java-files) $(call all-subdir-html-files)
LOCAL_MODULE := canbus_api
LOCAL_DROIDDOC_OPTIONS := com.micronet.canbus
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_DROIDDOC_USE_STANDARD_DOCLET := true
include $(BUILD_DROIDDOC)

# JNI
include $(CLEAR_VARS)
include $(call all-makefiles-under,$(LOCAL_PATH))


