//
// Created by Sharry Choo on 2019-12-20.
//

#ifndef SCOMPRESSOR_GIFENCODER_H
#define SCOMPRESSOR_GIFENCODER_H

#include "giflib/core/gif_lib.h"

/**
 * How to Encode.
 *
 * https://blog.csdn.net/yehuohan/article/details/52166678
 */
class GifEncoder {

public:
    GifEncoder(char *file_name);

    void start(const int Width,
               const int Height,
               const int ColorRes,
               const int BackGround,
               const ColorMapObject *ColorMap, int duration);

    void put();

    void finish();

private :
    GifFileType *outputGif;
};


#endif //SCOMPRESSOR_GIFENCODER_H
