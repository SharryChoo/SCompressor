//
// Created by Sharry on 2019-12-20.
// Copy from Google.
//

#ifndef SCOMPRESSOR_GIFDECODER_H
#define SCOMPRESSOR_GIFDECODER_H

#include "giflib/core/gif_lib.h"
#include "utils/color.h"

class GifDecoder {

public:
    GifDecoder(char *fileName);

    ~GifDecoder();

    long drawFrame(int frameNr, Color8888 *outputPtr, int outputPixelStride, int previousFrameNr);

    bool getPreservedFrame(int frameIndex) const { return mPreservedFrames[frameIndex]; }

    int getRestoringFrame(int frameIndex) const { return mRestoringFrames[frameIndex]; }

    void savePreserveBuffer(Color8888 *outputPtr, int outputPixelStride, int frameNr);

    void restorePreserveBuffer(Color8888 *outputPtr, int outputPixelStride);

private:
    GifFileType *mGif;

    int mLoopCount;

    // array of bool per frame - if true, frame data is used by a later DISPOSE_PREVIOUS frame
    bool *mPreservedFrames;

    // array of ints per frame - if >= 0, points to the index of the preserve that frame needs
    int *mRestoringFrames;

    Color8888 *mPreserveBuffer;

    int mPreserveBufferFrame;

    Color8888 mBgColor;

};


typedef struct GifInfo {
    int total_time;     //总时间
    int current_frame;  //当前帧
    int frame_duration; //每一帧时间
    int total_frame;    //总帧数
} GifInfo;

#endif //SCOMPRESSOR_GIFDECODER_H
