#include <jni.h>
#include "GifEncoder.h"

typedef struct GifInfo {
    //总时间
    int total_time;
    //当前帧
    int current_frame;
    //每一帧时间
    int frame_duration;
    //总帧数
    int total_frame;
} GifInfo;

/**
 * Office source
 * http://giflib.sourceforge.net/gif_lib.html#idm73
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_sharry_lib_gif_GifHelper_nativeDownsampler(JNIEnv *env, jclass clazz, jstring input_file,
                                                    jstring output_file, jint sample_size) {
    char *inputFileName = (char *) (env)->GetStringUTFChars(input_file, NULL);
    char *outputFileName = (char *) (env)->GetStringUTFChars(output_file, NULL);
    /*
     do decode.
     */
    int errorStatus;
    GifFileType *originGif = DGifOpenFileName(inputFileName, &errorStatus);
    DGifSlurp(originGif);
    //初始化一下GifBean保存gif的信息,为GifBean开劈一个内存空间
    GifInfo *originGifInfo = new GifInfo();
    originGifInfo->frame_duration = 0;
    originGifInfo->current_frame = 0;
    originGifInfo->total_frame = 0;
    originGifInfo->total_time = 0;
    originGif->UserData = originGifInfo;//这里是设置一上tag，相当于给view设置一个tag
    // 给 gifbean 成员变量赋值，得到当前播放时间的总时长；
    int i, j, frame_delay;
    // 这个是系统中保存 gif 图片个数的结构体
    SavedImage *frame = NULL;
    // 这个保存 gif 延时代码块的结构体
    ExtensionBlock *ext = NULL;
    //遍历拿到每一帧时间，总时间，延迟时间
    for (i = 0; i < originGif->ImageCount; i++) {
        //拿到每个一个图片相关信息
        frame = &originGif->SavedImages[i];
        for (j = 0; j < frame->ExtensionBlockCount; j++) {
            // 找到含有延迟时间的代码块
            if (frame->ExtensionBlocks[j].Function == GRAPHICS_EXT_FUNC_CODE) {
                ext = &(frame->ExtensionBlocks[j]);
            }
        }
        //拿到延迟时间
        if (ext) {
            //延迟时间1-->10ms
            frame_delay = 10 * (ext->Bytes[2] << 8 | ext->Bytes[1]);//拿到延迟时间
            //拿到总时间
            originGifInfo->total_time += frame_delay;
        }
    }
    //每一帧时间,拿总时间除总帧数
    originGifInfo->frame_duration = originGifInfo->total_time / originGif->ImageCount;

    /*
     do encode.
     */
    GifEncoder *encoder = new GifEncoder(outputFileName);
    encoder->start(originGif->SWidth, originGif->SHeight, 256, 0,
                   originGif->SColorMap, originGifInfo->frame_duration);
    // TODO put frames.
    encoder->put();
    encoder->finish();

    /*
     release
     */
    delete originGifInfo;
    delete encoder;
    env->ReleaseStringUTFChars(input_file, inputFileName);
    env->ReleaseStringUTFChars(output_file, outputFileName);
}