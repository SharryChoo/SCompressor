//
// Created by Sharry on 2019-12-20.
//

#include <malloc.h>
#include <string.h>
#include <android/bitmap.h>
#include "GifDecoder.h"
#include "utils/math.h"
#include "utils/log.h"

////////////////////////////////////////////////////////////////////////////////
// draw helpers
////////////////////////////////////////////////////////////////////////////////

static long getDelayMs(GraphicsControlBlock &gcb) {
    return gcb.DelayTime * 10;
}

static Color8888 gifColorToColor8888(const GifColorType &color) {
    return ARGB_TO_COLOR8888(0xff, color.Red, color.Green, color.Blue);
}

static bool willBeCleared(const GraphicsControlBlock &gcb) {
    return gcb.DisposalMode == DISPOSE_BACKGROUND || gcb.DisposalMode == DISPOSE_PREVIOUS;
}

// return true if area of 'target' is completely covers area of 'covered'
static bool checkIfCover(const GifImageDesc &target, const GifImageDesc &covered) {
    return target.Left <= covered.Left
           && covered.Left + covered.Width <= target.Left + target.Width
           && target.Top <= covered.Top
           && covered.Top + covered.Height <= target.Top + target.Height;
}

static void copyLine(Color8888 *dst, const unsigned char *src, const ColorMapObject *cmap,
                     int transparent, int width) {
    for (; width > 0; width--, src++, dst++) {
        if (*src != transparent && *src < cmap->ColorCount) {
            *dst = gifColorToColor8888(cmap->Colors[*src]);
        }
    }
}

static void setLineColor(Color8888 *dst, Color8888 color, int width) {
    for (; width > 0; width--, dst++) {
        *dst = color;
    }
}

static void getCopySize(const GifImageDesc &imageDesc, int maxWidth, int maxHeight,
                        GifWord &copyWidth, GifWord &copyHeight) {
    copyWidth = imageDesc.Width;
    if (imageDesc.Left + copyWidth > maxWidth) {
        copyWidth = maxWidth - imageDesc.Left;
    }
    copyHeight = imageDesc.Height;
    if (imageDesc.Top + copyHeight > maxHeight) {
        copyHeight = maxHeight - imageDesc.Top;
    }
}

static int streamReader(GifFileType *fileType, GifByteType *out, int size) {
    Stream *stream = (Stream *) fileType->UserData;
    return (int) stream->read(out, size);
}

////////////////////////////////////////////////////////////////////////////////
// GifDecoder Implementation
////////////////////////////////////////////////////////////////////////////////

GifDecoder::GifDecoder(char *filePath) {
    mGif = DGifOpenFileName(filePath, NULL);
    init();
}

GifDecoder::GifDecoder(Stream *stream) {
    mGif = DGifOpen(stream, streamReader, NULL);
    ALOGE("GifDecoder stream constructor");
    init();
}

void GifDecoder::init() {
    if (!mGif) {
        ALOGW("Gif load failed");
        return;
    }
    if (DGifSlurp(mGif) != GIF_OK) {
        ALOGW("Gif slurp failed");
        DGifCloseFile(mGif, NULL);
        mGif = NULL;
        return;
    }

    long durationMs = 0;
    int lastUnclearedFrame = -1;
    mPreservedFrames = new bool[mGif->ImageCount];
    mRestoringFrames = new int[mGif->ImageCount];

    GraphicsControlBlock gcb;
    for (int i = 0; i < mGif->ImageCount; i++) {
        const SavedImage &image = mGif->SavedImages[i];

        // find the loop extension pair
        for (int j = 0; (j + 1) < image.ExtensionBlockCount; j++) {
            ExtensionBlock *eb1 = image.ExtensionBlocks + j;
            ExtensionBlock *eb2 = image.ExtensionBlocks + j + 1;
            if (eb1->Function == APPLICATION_EXT_FUNC_CODE
                // look for "NETSCAPE2.0" app extension
                && eb1->ByteCount == 11
                && !memcmp((const char *) (eb1->Bytes), "NETSCAPE2.0", 11)
                // verify extension contents and get loop count
                && eb2->Function == CONTINUE_EXT_FUNC_CODE
                && eb2->ByteCount == 3
                && eb2->Bytes[0] == 1) {
                mLoopCount = (int) (eb2->Bytes[2] << 8) + (int) (eb2->Bytes[1]);
            }
        }

        DGifSavedExtensionToGCB(mGif, i, &gcb);

        // timing
        durationMs += getDelayMs(gcb);

        // preserve logic
        mPreservedFrames[i] = false;
        mRestoringFrames[i] = -1;
        if (gcb.DisposalMode == DISPOSE_PREVIOUS && lastUnclearedFrame >= 0) {
            mPreservedFrames[lastUnclearedFrame] = true;
            mRestoringFrames[i] = lastUnclearedFrame;
        }
        if (!willBeCleared(gcb)) {
            lastUnclearedFrame = i;
        }
    }

    // 打印 GIF 信息
    ALOGE("GifDecoder created with size [%d, %d], frames is %d, duration is %ld",
          mGif->SWidth, mGif->SHeight, mGif->ImageCount, durationMs);
//    for (int i = 0; i < mGif->ImageCount; i++) {
//        DGifSavedExtensionToGCB(mGif, i, &gcb);
//        LOGE("Frame %d - must preserve %d, restore point %d, trans color %d",
//             i, mPreservedFrames[i], mRestoringFrames[i], gcb.TransparentColor);
//    }

    // 解析 GIF 的背景色
    const ColorMapObject *cmap = mGif->SColorMap;
    if (cmap) {
        // calculate bg color
        GraphicsControlBlock gcb;
        DGifSavedExtensionToGCB(mGif, 0, &gcb);
        if (gcb.TransparentColor == NO_TRANSPARENT_COLOR
            && mGif->SBackGroundColor < cmap->ColorCount) {
            // 获取 GIF 的背景颜色
            mBgColor = gifColorToColor8888(cmap->Colors[mGif->SBackGroundColor]);
        }
    }
}

GifDecoder::~GifDecoder() {
    if (mGif) {
        DGifCloseFile(mGif, NULL);
    }
    delete[] mPreservedFrames;
    delete[] mRestoringFrames;
    ALOGE("GifDecoder release.");
}

long GifDecoder::drawFrame(int frameNr, Color8888 *outputPtr, int outputPixelStride,
                           int previousFrameNr) {
    GifFileType *gif = mGif;
#if GIF_DEBUG
    ALOGD("      drawFrame on %p nr %d on addr %p, previous frame nr %d",
          this, frameNr, outputPtr, previousFrameNr);
#endif

    const int height = mGif->SHeight;
    const int width = mGif->SWidth;

    GraphicsControlBlock gcb;

    int start = max(previousFrameNr + 1, 0);

    for (int i = max(start - 1, 0); i < frameNr; i++) {
        int neededPreservedFrame = getRestoringFrame(i);
        if (neededPreservedFrame >= 0 &&
            (mPreserveBufferFrame != neededPreservedFrame)) {
#if GIF_DEBUG
            ALOGD("frame %d needs frame %d preserved, but %d is currently, so drawing from scratch",
                    i, neededPreservedFrame, mPreserveBufferFrame);
#endif
            start = 0;
        }
    }

    for (int i = start; i <= frameNr; i++) {
        DGifSavedExtensionToGCB(gif, i, &gcb);
        const SavedImage &frame = gif->SavedImages[i];

#if GIF_DEBUG
        bool frameOpaque = gcb.TransparentColor == NO_TRANSPARENT_COLOR;
        ALOGD("producing frame %d, drawing frame %d (opaque %d, disp %d, del %d)",
                frameNr, i, frameOpaque, gcb.DisposalMode, gcb.DelayTime);
#endif
        if (i == 0) {
            // clear bitmap
            Color8888 bgColor = mBgColor;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    outputPtr[y * outputPixelStride + x] = bgColor;
                }
            }
        } else {
            GraphicsControlBlock prevGcb;
            DGifSavedExtensionToGCB(gif, i - 1, &prevGcb);
            const SavedImage &prevFrame = gif->SavedImages[i - 1];
            bool prevFrameDisposed = willBeCleared(prevGcb);

            bool newFrameOpaque = gcb.TransparentColor == NO_TRANSPARENT_COLOR;
            bool prevFrameCompletelyCovered = newFrameOpaque
                                              && checkIfCover(frame.ImageDesc,
                                                              prevFrame.ImageDesc);

            if (prevFrameDisposed && !prevFrameCompletelyCovered) {
                switch (prevGcb.DisposalMode) {
                    case DISPOSE_BACKGROUND: {
                        Color8888 *dst = outputPtr + prevFrame.ImageDesc.Left +
                                         prevFrame.ImageDesc.Top * outputPixelStride;

                        GifWord copyWidth, copyHeight;
                        getCopySize(prevFrame.ImageDesc, width, height, copyWidth,
                                    copyHeight);
                        for (; copyHeight > 0; copyHeight--) {
                            setLineColor(dst, TRANSPARENT, copyWidth);
                            dst += outputPixelStride;
                        }
                    }
                        break;
                    case DISPOSE_PREVIOUS: {
                        restorePreserveBuffer(outputPtr, outputPixelStride);
                    }
                        break;
                }
            }

            if (getPreservedFrame(i - 1)) {
                // currently drawn frame will be restored by a following DISPOSE_PREVIOUS draw, so
                // we preserve it
                savePreserveBuffer(outputPtr, outputPixelStride, i - 1);
            }
        }

        bool willBeCleared = gcb.DisposalMode == DISPOSE_BACKGROUND
                             || gcb.DisposalMode == DISPOSE_PREVIOUS;
        if (i == frameNr || !willBeCleared) {
            const ColorMapObject *cmap = gif->SColorMap;
            if (frame.ImageDesc.ColorMap) {
                cmap = frame.ImageDesc.ColorMap;
            }

            // If a cmap is missing, the frame can't be decoded, so we skip it.
            if (cmap) {
                const unsigned char *src = frame.RasterBits;
                Color8888 *dst =
                        outputPtr + frame.ImageDesc.Left +
                        frame.ImageDesc.Top * outputPixelStride;
                GifWord copyWidth, copyHeight;
                getCopySize(frame.ImageDesc, width, height, copyWidth, copyHeight);
                for (; copyHeight > 0; copyHeight--) {
                    copyLine(dst, src, cmap, gcb.TransparentColor, copyWidth);
                    src += frame.ImageDesc.Width;
                    dst += outputPixelStride;
                }
            }
        }
    }
    // return last frame's delay
    const int maxFrame = gif->ImageCount;
    const int lastFrame = (frameNr + maxFrame - 1) % maxFrame;
    DGifSavedExtensionToGCB(gif, lastFrame, &gcb);
    return getDelayMs(gcb);
}

void GifDecoder::restorePreserveBuffer(Color8888 *outputPtr, int outputPixelStride) {
    const int width = mGif->SWidth;
    const int height = mGif->SHeight;
    if (!mPreserveBuffer) {
        ALOGD("preserve buffer not allocated! ah!");
        return;
    }
    for (int y = 0; y < height; y++) {
        memcpy(outputPtr + outputPixelStride * y, mPreserveBuffer + width * y, width * 4);
    }
}

void GifDecoder::savePreserveBuffer(Color8888 *outputPtr, int outputPixelStride,
                                    int frameNr) {
    if (frameNr == mPreserveBufferFrame) return;

    mPreserveBufferFrame = frameNr;
    const int width = mGif->SWidth;
    const int height = mGif->SHeight;
    if (!mPreserveBuffer) {
        mPreserveBuffer = new Color8888[width * height];
    }
    for (int y = 0; y < height; y++) {
        memcpy(mPreserveBuffer + width * y,
               outputPtr + outputPixelStride * y,
               width * 4);
    }
}


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