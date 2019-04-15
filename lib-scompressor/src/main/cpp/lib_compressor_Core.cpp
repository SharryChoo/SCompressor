#include <jni.h>
#include <malloc.h>
#include <android/bitmap.h>
#include <android/log.h>
#include "libjpegturbo_utils.h"

#define TAG "Core_native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

typedef unsigned char uchar;

extern "C"
JNIEXPORT jint JNICALL
Java_com_sharry_libscompressor_Core_nativeCompress(JNIEnv *env, jclass type, jobject bitmap,
                                                   jint quality, jstring destPath_) {
    // 1. 获取 bitmap 信息
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bitmap, &info);
    int cols = info.width;
    int rows = info.height;
    int format = info.format;
    LOGI("Bitmap width is %d, height is %d", cols, rows);
    // 若不为 ARGB8888, 则不给予压缩
    if (format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Unsupported Bitmap channels, Please ensure channels is ARGB_8888.");
        return false;
    }
    // 2. 解析数据
    LOGI("Parse bitmap pixels");
    // 锁定画布
    uchar *pixels = NULL;
    AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels);
    // 创建存储数组
    uchar *data = (uchar *) malloc(static_cast<size_t>(cols * rows * 3));
    uchar *data_header_pointer = data;// 临时保存 data 的首地址, 用于后续释放内存
    uchar r, g, b;
    int row = 0, col = 0, pixel;
    for (row = 0; row < rows; ++row) {
        for (col = 0; col < cols; ++col) {
            // 获取二维数组的每一个像素信息首地址
            pixel = *((int *) pixels);
            // ...                                              // 忽略 A 通道值
            r = static_cast<uchar>((pixel & 0x00FF0000) >> 16); // 获取 R 通道值
            g = static_cast<uchar>((pixel & 0x0000FF00) >> 8);  // 获取 G 通道值
            b = static_cast<uchar>((pixel & 0x000000FF));       // 获取 B 通道值
            *data = b;
            *(data + 1) = g;
            *(data + 2) = r;
            data += 3;
            pixels += 4;
        }
    }
    // 解锁画布
    AndroidBitmap_unlockPixels(env, bitmap);
    // 3. 使用 libjpeg 进行图片质量压缩
    LOGI("Lib jpeg turbo do compress");
    char *output_filename = (char *) (env)->GetStringUTFChars(destPath_, 0);
    int result = LibJpegTurboUtils::write_JPEG_file(data_header_pointer, rows, cols,
                                                    output_filename,
                                                    quality);
    // 4. 释放资源
    LOGI("Release memory");
    free((void *) data_header_pointer);
    env->ReleaseStringUTFChars(destPath_, output_filename);
    return result;
}