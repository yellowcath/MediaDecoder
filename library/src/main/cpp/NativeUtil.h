//
// Created by yellowcat on 2017/5/15.
//
#include <android/log.h>

#ifndef CODECPLAYER_NATIVEUTIL_H
#define CODECPLAYER_NATIVEUTIL_H

#define LOG_TAG "MediaDecoder"
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

void copyData(unsigned char* from, int len, unsigned char* to, int pixelStride, int rowStride, int width);

#endif //CODECPLAYER_NATIVEUTIL_H
