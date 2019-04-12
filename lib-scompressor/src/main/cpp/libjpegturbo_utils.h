//
// Created by Sharry on 2019/4/12.
// Utils associated with libjpeg-turbo
// Details please see https://raw.githubusercontent.com/libjpeg-turbo/libjpeg-turbo/master/example.txt
//

#ifndef SCOMPRESSOR_LIBJPEG_UTILS_H
#define SCOMPRESSOR_LIBJPEG_UTILS_H

extern "C" {
#include "jpeglib.h"
}

class LibJpegTurboUtils {

public:

    /**
     * Encode buffer 2 jpeg.
     *
     * @param image_buffer  buffer of BGR data.
     * @param image_height  pixels
     * @param image_width   pixels
     * @param filename      nonnull
     * @param quality       range of [0, 100]
     *
     * @return 1 is success, 0 is failed.
     */
    static int
    write_JPEG_file(JSAMPLE *image_buffer, int image_height, int image_width, char *filename,
                    int quality);

    /**
     * Read data from jpeg file, not used.
     *
     * @return 1 is success, 0 is failed.
     */
    static int read_JPEG_file(char *filename);
};


#endif //SCOMPRESSOR_LIBJPEG_UTILS_H
