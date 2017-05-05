LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= libcanbus

LOCAL_SRC_FILES := \
				canbus.cpp \
				CanbusInterface.cpp \
				FlexCANCanbusInterfaceBridge.cpp \
				FlexCANCanbusSocket.cpp \
				FlexCANCommand.cpp \
				FlexCANComm.cpp \

LOCAL_SHARED_LIBRARIES := \
	        libnativehelper \
			libcutils \
			libutils \
			liblog

LOCAL_CPPFLAGS := -O3 -g -Werror -DSO_RXQ_OVFL=40 -DPF_CAN=29 -DAF_CAN=PF_CAN
LOCAL_CFLAGS += -Wno-unused-parameter -Wno-int-to-pointer-cast -Wno-write-strings -Wno-maybe-uninitialized -Wno-uninitialized 

LOCAL_C_INCLUDES += $(JNI_H_INCLUDE)

#LOCAL_STATIC_LIBRARIES := libcan
include $(BUILD_SHARED_LIBRARY)

