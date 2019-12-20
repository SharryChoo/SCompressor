//
// Created by Sharry Choo on 2019-12-20.
//

#include "GifEncoder.h"

GifEncoder::GifEncoder(char *file_name) {
    int error;
    outputGif = EGifOpenFileName(file_name, false, &error);
}

void GifEncoder::start(const int Width, const int Height, const int ColorRes, const int BackGround,
                       const ColorMapObject *ColorMap, int duration) {
    // 1. 逻辑屏幕标识符(Logical Screen Descriptor) 和 全局颜色列表
    EGifPutScreenDesc(outputGif, Width, Height, ColorRes, BackGround, ColorMap);
    // 2. 图形控制扩展(Graphic Control Extension)，此块可以设定动画速度和透明色
    GraphicsControlBlock gcb;
    gcb.DisposalMode = DISPOSE_DO_NOT;
    gcb.UserInputFlag = false;
    gcb.DelayTime = duration;
    gcb.UserInputFlag = NO_TRANSPARENT_COLOR;
    GifByteType gcbLen = 4;
    GifByteType gcbBytes[gcbLen];
    EGifGCBToExtension(&gcb, gcbBytes);
    EGifPutExtension(outputGif, GRAPHICS_EXT_FUNC_CODE, gcbLen, gcbBytes);
    GifByteType aeLen = 11;
    char *aeBytes = {"NETSCAPE2.0"};
    GifByteType aeSubLen = 3;
    GifByteType aeSubBytes[aeSubLen];
    aeSubBytes[0] = 0x01;
    aeSubBytes[1] = 0x00;   //byte[1]是低位，byte[2]是高位，组成一个无符号16位数，决定动画循环次数
    aeSubBytes[2] = 0x00;
    EGifPutExtensionLeader(outputGif, APPLICATION_EXT_FUNC_CODE);
    EGifPutExtensionBlock(outputGif, aeLen, aeBytes);
    EGifPutExtensionBlock(outputGif, aeSubLen, aeSubBytes);
    EGifPutExtensionTrailer(outputGif);
}

void GifEncoder::put() {

}

void GifEncoder::finish() {

}
