#include <jni.h>
#include <malloc.h>
#include <android/bitmap.h>
#include "libjpegturbo_utils.h"
#include "Constants.h"

void cvtABGR8888ToRGB888(void *src, uchar *dst, int rows, int cols);

void cvtRGB565ToRGB888(void *src, uchar *dst, int rows, int cols);

extern "C"
JNIEXPORT jint JNICALL
Java_com_sharry_lib_scompressor_Core_nativeCompress(JNIEnv *env, jclass type, jobject bitmap,
                                                    jint quality, jstring destPath_,
                                                    jboolean arith_code) {
    // 1. 获取 bitmap 信息
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bitmap, &info);
    int cols = info.width;
    int rows = info.height;
    int format = info.format;
    LOGI("Bitmap width is %d, height is %d", cols, rows);
    // 若非 ARGB_8888/RGB_565 则不给予压缩
    if (format != ANDROID_BITMAP_FORMAT_RGBA_8888 && format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOGE("Unsupported Bitmap channels, Please ensure channels is ARGB_8888, current is %d",
             format);
        return false;
    }

    // 2. 解析数据
    LOGI("Parse bitmap pixels");
    // 锁定画布
    void *pixels = NULL;
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    if (pixels == NULL) {
        LOGE("Fetch Bitmap data failed.");
        return false;
    }
    // 创建存储数组
    uchar *image_buffer = new uchar[cols * rows * 3];;
    if (format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        cvtABGR8888ToRGB888(pixels, image_buffer, rows, cols);
    } else {
        cvtRGB565ToRGB888(pixels, image_buffer, rows, cols);
    }
    // 解锁画布
    AndroidBitmap_unlockPixels(env, bitmap);

    // 3. 使用 libjpeg 进行图片质量压缩
    LOGI("libjpeg-turbo do compress");
    char *output_filename = (char *) (env)->GetStringUTFChars(destPath_, NULL);
    int result = LibJpegTurboUtils::write_JPEG_file(image_buffer, rows, cols,
                                                    output_filename, quality,
                                                    arith_code ? TRUE : FALSE);

    // 4. 释放资源
    LOGI("Release memory");
    delete[]image_buffer;
    env->ReleaseStringUTFChars(destPath_, output_filename);
    return result;
}

void cvtABGR8888ToRGB888(void *src, uchar *dst, int rows, int cols) {
    int *pixels = static_cast<int *>(src);
    uchar r, g, b;
    int row = 0, col = 0, pixel;
    for (row = 0; row < rows; ++row) {
        for (col = 0; col < cols; ++col) {
            // 获取像素值
            pixel = *pixels;
            // ...                                                       // 忽略 A 通道值
            b = static_cast<uchar>((pixel & ABGR8888_MASK_BLUE) >> 16);  // 获取 B 通道值
            g = static_cast<uchar>((pixel & ABGR8888_MASK_GREEN) >> 8);  // 获取 G 通道值
            r = static_cast<uchar>((pixel & ABGR8888_MASK_RED));         // 获取 R 通道值
            // 为 Data 填充数据
            *(dst++) = r;
            *(dst++) = g;
            *(dst++) = b;
            // 更新首地址
            pixels += 1;
        }
    }
}

void cvtRGB565ToRGB888(void *src, uchar *dst, int rows, int cols) {
    uint16_t *pixels = static_cast<uint16_t *>(src);
    uchar r, g, b;
    int row = 0, col = 0;
    uint16_t pixel;
    for (row = 0; row < rows; ++row) {
        for (col = 0; col < cols; ++col) {
            // 获取像素值
            pixel = *pixels;
            r = static_cast<uchar>((pixel & RGB565_MASK_RED) >> 11);   // 获取 R 通道值
            g = static_cast<uchar>((pixel & RGB565_MASK_GREEN) >> 5);  // 获取 G 通道值
            b = static_cast<uchar>((pixel & RGB565_MASK_BLUE));        // 获取 B 通道值
            // 填充到 dst 中
            *(dst++) = r << 3;
            *(dst++) = g << 2;
            *(dst++) = b << 3;
            // 更新指针首地址
            pixels += 1;
        }
    }
}
