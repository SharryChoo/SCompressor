#include <jni.h>
#include "GifEncoder.h"
#include "GifDecoder.h"
#include "Constants.h"

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
    GifDecoder *decoder = NULL;
    GifFileType *originGif = NULL;
    GifInfo *originInfo = NULL;
    GifEncoder *encoder = NULL;

    /*
     do decode.
     */
    decoder = new GifDecoder(inputFileName);

    /*
     do encode.
     */
//    encoder = new GifEncoder(outputFileName);
//    encoder->start(originGif->SWidth, originGif->SHeight, 256, 0,
//                   originGif->SColorMap, originInfo->frame_duration);
//    for (int i = 0; i < originInfo->total_frame; i++) {
//        // TODO put frames.
//        encoder->put();
//    }
//    encoder->finish();
    /*
     release
     */
    release:
    if (encoder) {
        delete encoder;
    }
    if (originGif) {
        delete originGif;
    }
    if (decoder) {
        delete decoder;
    }
    env->ReleaseStringUTFChars(input_file, inputFileName);
    env->ReleaseStringUTFChars(output_file, outputFileName);
}