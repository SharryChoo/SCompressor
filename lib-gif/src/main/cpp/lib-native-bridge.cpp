#include <jni.h>
#include <android/bitmap.h>
#include "GifDecoder.h"
#include "utils/log.h"
#include "stream/Stream.h"

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    if (JavaStream_OnLoad(env)) {
        ALOGE("Failed to load JavaStream");
        return -1;
    }

    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_sharry_lib_gif_GifDecoder_nativeDecodeFile(JNIEnv *env, jclass clazz, jstring file_path) {
    char *filePath = const_cast<char *>(env->GetStringUTFChars(file_path, NULL));
    GifDecoder *decoder = new GifDecoder(filePath);
    env->ReleaseStringUTFChars(file_path, filePath);
    return reinterpret_cast<jlong>(decoder);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_sharry_lib_gif_GifDecoder_nativeDecodeStream(JNIEnv *env, jclass clazz, jobject istream,
                                                      jbyteArray byteArray) {
    JavaInputStream stream(env, istream, byteArray);
    ALOGE("decodeStream");
    GifDecoder *decoder = new GifDecoder(&stream);
    return reinterpret_cast<jlong>(decoder);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_sharry_lib_gif_GifDecoder_nativeDecodeByteArray(JNIEnv *env, jclass clazz,
                                                         jbyteArray byteArray,
                                                         jint offset, jint length) {
    jbyte *bytes = reinterpret_cast<jbyte *>(env->GetPrimitiveArrayCritical(byteArray, NULL));
    if (bytes == NULL) {
        ALOGE("couldn't read array bytes");
        return -1;
    }
    MemoryStream stream(bytes + offset, length, NULL);
    GifDecoder *decoder = new GifDecoder(&stream);
    env->ReleasePrimitiveArrayCritical(byteArray, bytes, 0);
    return reinterpret_cast<jlong>(decoder);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_sharry_lib_gif_GifDecoder_nativeDecodeByteBuffer(JNIEnv *env, jclass clazz, jobject buf,
                                                          jint offset, jint limit) {
    jobject globalBuf = env->NewGlobalRef(buf);
    JavaVM *vm;
    env->GetJavaVM(&vm);
    MemoryStream stream(
            (reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(globalBuf))) + offset,
            limit,
            globalBuf);
    GifDecoder *decoder = new GifDecoder(&stream);
    return reinterpret_cast<jlong>(decoder);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_sharry_lib_gif_GifDecoder_nativeGetFrame(JNIEnv *env, jobject thiz, jlong decoder_handle,
                                                  jint frame_nr,
                                                  jobject bitmap, jint previous_frame_nr) {
    GifDecoder *decoder = reinterpret_cast<GifDecoder *>(decoder_handle);
    AndroidBitmapInfo info;
    void *pixels;
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    int pixelStride = info.stride >> 2;
    jlong delayMs = decoder->drawFrame(frame_nr, (Color8888 *) pixels, pixelStride,
                                       previous_frame_nr);
    AndroidBitmap_unlockPixels(env, bitmap);
    return delayMs;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_sharry_lib_gif_GifDecoder_nativeGetWidth(JNIEnv *env, jobject thiz, jlong native_ptr) {
    GifDecoder *decoder = reinterpret_cast<GifDecoder *>(native_ptr);
    return decoder->getWidth();
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_sharry_lib_gif_GifDecoder_nativeGetHeight(JNIEnv *env, jobject thiz, jlong native_ptr) {
    GifDecoder *decoder = reinterpret_cast<GifDecoder *>(native_ptr);
    return decoder->getHeight();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sharry_lib_gif_GifDecoder_nativeDestroy(JNIEnv *env, jobject thiz, jlong native_ptr) {
    GifDecoder *decoder = reinterpret_cast<GifDecoder *>(native_ptr);
    delete (decoder);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_sharry_lib_gif_GifDecoder_nativeGetFrameCount(JNIEnv *env, jobject thiz,
                                                       jlong native_ptr) {
    GifDecoder *decoder = reinterpret_cast<GifDecoder *>(native_ptr);
    return decoder->getFrameCount();
}
