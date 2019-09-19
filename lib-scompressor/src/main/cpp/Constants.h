//
// Created by Sharry on 2019-09-19.
//

#ifndef SCOMPRESSOR_CONSTANTS_H
#define SCOMPRESSOR_CONSTANTS_H

#include <android/log.h>

#define TAG "SCompressor_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

typedef unsigned char uchar;

#define ABGR8888_MASK_RED 0x000000FF
#define ABGR8888_MASK_GREEN 0x0000FF00
#define ABGR8888_MASK_BLUE 0x00FF0000

#define RGB565_MASK_RED 0xF800
#define RGB565_MASK_GREEN 0x07E0
#define RGB565_MASK_BLUE 0x001F

#endif //SCOMPRESSOR_CONSTANTS_H
