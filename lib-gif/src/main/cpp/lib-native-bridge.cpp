#include <jni.h>
#include <android/bitmap.h>
#include "GifDecoder.h"
#include "utils/log.h"
#include "stream/Stream.h"


////////////////////////////////////////////////////////////////////////////////
// JNILoader
////////////////////////////////////////////////////////////////////////////////

namespace gifdecoder {

    jlong nativeDecodeFile(JNIEnv *env, jclass, jstring file_path) {
        char *filePath = const_cast<char *>(env->GetStringUTFChars(file_path, NULL));
        GifDecoder *decoder = new GifDecoder(filePath);
        env->ReleaseStringUTFChars(file_path, filePath);
        return reinterpret_cast<jlong>(decoder);
    }

    jlong nativeDecodeStream(JNIEnv *env, jclass, jobject istream,
                             jbyteArray byteArray) {
        JavaInputStream stream(env, istream, byteArray);
        ALOGE("decodeStream");
        GifDecoder *decoder = new GifDecoder(&stream);
        return reinterpret_cast<jlong>(decoder);
    }

    jlong nativeDecodeByteArray(JNIEnv *env, jclass,
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

    jlong nativeDecodeByteBuffer(JNIEnv *env, jclass, jobject buf,
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

    jlong nativeGetFrame(JNIEnv *env, jobject, jlong decoder_handle,
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

    jint nativeGetWidth(JNIEnv *, jobject, jlong native_ptr) {
        GifDecoder *decoder = reinterpret_cast<GifDecoder *>(native_ptr);
        return decoder->getWidth();
    }

    jint nativeGetHeight(JNIEnv *, jobject, jlong native_ptr) {
        GifDecoder *decoder = reinterpret_cast<GifDecoder *>(native_ptr);
        return decoder->getHeight();
    }

    jint nativeGetFrameCount(JNIEnv *, jobject, jlong native_ptr) {
        GifDecoder *decoder = reinterpret_cast<GifDecoder *>(native_ptr);
        return decoder->getFrameCount();
    }

    void nativeDestroy(JNIEnv *, jobject, jlong native_ptr) {
        GifDecoder *decoder = reinterpret_cast<GifDecoder *>(native_ptr);
        delete (decoder);
    }

}

static JNINativeMethod gGifDecoderMethods[] = {
        // create method.
        {"nativeDecodeFile",       "(Ljava/lang/String;)J",           (void *) gifdecoder::nativeDecodeFile},
        {"nativeDecodeStream",     "(Ljava/io/InputStream;[B)J",      (void *) gifdecoder::nativeDecodeStream},
        {"nativeDecodeByteArray",  "([BII)J",                         (void *) gifdecoder::nativeDecodeByteArray},
        {"nativeDecodeByteBuffer", "(Ljava/nio/ByteBuffer;II)J",      (void *) gifdecoder::nativeDecodeByteBuffer},
        // other method.
        {"nativeGetFrame",         "(JILandroid/graphics/Bitmap;I)J", (void *) gifdecoder::nativeGetFrame},
        {"nativeGetWidth",         "(J)I",                            (void *) gifdecoder::nativeGetWidth},
        {"nativeGetHeight",        "(J)I",                            (void *) gifdecoder::nativeGetHeight},
        {"nativeGetFrameCount",    "(J)I",                            (void *) gifdecoder::nativeGetFrameCount},
        {"nativeDestroy",          "(J)V",                            (void *) gifdecoder::nativeDestroy},
};

jint GifDecoder_OnLoad(JNIEnv *env) {
    jclass jclsGifDecoder = env->FindClass("com/sharry/lib/gif/GifDecoder");
    jclsGifDecoder = reinterpret_cast<jclass>(env->NewGlobalRef(jclsGifDecoder));
    return env->RegisterNatives(
            jclsGifDecoder,
            gGifDecoderMethods,
            sizeof(gGifDecoderMethods) / sizeof(gGifDecoderMethods[0])
    );
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    if (JavaStream_OnLoad(env)) {
        ALOGE("Failed to load JavaStream");
        return -1;
    }
    if (GifDecoder_OnLoad(env)) {
        ALOGE("Failed to load GifDecoder");
        return -1;
    }
    return JNI_VERSION_1_6;
}


