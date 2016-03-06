#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <errno.h>
#include <fcntl.h>
#include <linux/input.h>

#define LOG_TAG "jni"

//#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

//#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define DEVICE_NAME "/dev/input/event1"

int fd;

jint

Java_com_yihang_gesture_service_ServerManager_GetGesture( JNIEnv* env,jobject thiz)
{

	int ret;
	int result;
	int len;
	struct input_event btn;
	fd = open (DEVICE_NAME, O_RDONLY);
	if (fd < 0)
	{
		//LOGI("open %s fail\n",DEVICE_NAME);
		return -1;
	}
	len = sizeof(struct input_event);
	memset(&btn, 0, len);
	ret = read(fd, &btn, len);
	if(ret<0||ret!=len)
	{
		//LOGI("Error %s read: %d", __FUNCTION__, errno);
		result = -1;
	}
	else
	{
		//LOGI("btn.code is  %d", btn.code);
		result = btn.code;
	}
	close(fd);
	return result;
}



jint

Java_com_yihang_gesture_service_ServerManager_Closetp( JNIEnv* env,jobject thiz)

{
	system("echo 0 > /sys/ft5x46_tp_gesture/tp_gesture_enable");
	return 0;
}

jint

Java_com_yihang_gesture_service_ServerManager_Opentp( JNIEnv* env,jobject thiz)

{
	system("echo 1 > /sys/ft5x46_tp_gesture/tp_gesture_enable");
	return 0;
}


