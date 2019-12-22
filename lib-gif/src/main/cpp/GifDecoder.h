//
// Created by Sharry on 2019-12-20.
// Copy from Google.
//

#ifndef SCOMPRESSOR_GIFDECODER_H
#define SCOMPRESSOR_GIFDECODER_H

#include <jni.h>
#include "giflib/gif_lib.h"
#include "utils/color.h"
#include "stream/Stream.h"

class GifDecoder {

private:
    GifFileType *mGif;
    // array of bool per frame - if true, frame data is used by a later DISPOSE_PREVIOUS frame
    bool *mPreservedFrames = NULL;
    // array of ints per frame - if >= 0, points to the index of the preserve that frame needs
    int *mRestoringFrames = NULL;
    int mPreserveBufferFrame;
    int mLoopCount = 1;
    long mDurationMs = 0l;
    Color8888 *mPreserveBuffer = NULL;
    Color8888 mBgColor = TRANSPARENT;

public:

    GifDecoder(Stream *stream);

    GifDecoder(char *filePath);

    ~GifDecoder();

    int getWidth() { return mGif->SWidth; }

    int getHeight() { return mGif->SHeight; }

    bool isOpaque() {
        return (mBgColor & COLOR_8888_ALPHA_MASK) == COLOR_8888_ALPHA_MASK;
    }

    int getFrameCount() { return mGif->ImageCount; }

    int getLooperCount() {
        return mLoopCount;
    }

    long getDuration() {
        return mDurationMs;
    }

    long drawFrame(int frameNr, Color8888 *outputPtr, int outputPixelStride, int previousFrameNr);

private:
    void init();

    bool getPreservedFrame(int frameIndex) const { return mPreservedFrames[frameIndex]; }

    int getRestoringFrame(int frameIndex) const { return mRestoringFrames[frameIndex]; }

    void savePreserveBuffer(Color8888 *outputPtr, int outputPixelStride, int frameNr);

    void restorePreserveBuffer(Color8888 *outputPtr, int outputPixelStride);

};

jint GifDecoder_OnLoad(JNIEnv *env);

#endif //SCOMPRESSOR_GIFDECODER_H
