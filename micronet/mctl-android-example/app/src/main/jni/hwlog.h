#ifndef HWLOG_H
#define HWLOG_H

#ifdef UNIT_TEST
#include <stdio.h>
#define LOGI(...)	printf(__VA_ARGS__)
#ifdef DEBUG
#define LOGD(...)	printf(__VA_ARGS__)
#else
#define LOGD(...)
#endif
#define LOGE(...)	printf(__VA_ARGS__)

#else
#include <android/log.h>

//#define DEBUG 1

#define LOG_TAG "HardwareLib"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#ifdef DEBUG
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
#define LOGD(...)
#endif
#endif
#endif