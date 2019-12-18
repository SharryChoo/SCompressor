package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

/**
 * The core algorithm associated with picture compress.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/11/2019 4:53 PM
 */
final class Core {

    static int calculateAutoSampleSize(int srcWidth, int srcHeight) {
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide >= 1664 && longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }

    static int calculateSampleSize(int srcWidth, int srcHeight, int destWidth, int destHeight) {
        if (srcWidth < destWidth && srcHeight < destHeight) {
            return 1;
        }
        // 1. Calculate exact factor.
        double exactScaleFactor = Math.max((float) destWidth / (float) srcWidth,
                (float) destHeight / (float) srcHeight);
        // 2. Calculate scale factor.
        int outWidth = (int) Math.round(exactScaleFactor * srcWidth);
        int outHeight = (int) Math.round(exactScaleFactor * srcHeight);
        int widthScaleFactor = srcWidth / outWidth;
        int heightScaleFactor = srcHeight / outHeight;
        int scaleFactor = Math.max(widthScaleFactor, heightScaleFactor);
        // 3. Calculate sample size.(convert scaleFactor to 2 power.)
        int powerOfTwoSampleSize = Math.max(1, Integer.highestOneBit(scaleFactor));
        if (powerOfTwoSampleSize < (1.f / exactScaleFactor)) {
            powerOfTwoSampleSize = powerOfTwoSampleSize << 1;
        }
        // 4. Adjust weird picture.
        final float totalPixels = srcWidth * srcHeight;
        final float desirePixels = destWidth * destHeight << 1;
        while (totalPixels / (powerOfTwoSampleSize * powerOfTwoSampleSize) > desirePixels) {
            powerOfTwoSampleSize = powerOfTwoSampleSize << 1;
        }
        return powerOfTwoSampleSize;
    }

    ////////////////// native method /////////////////////

    static {
        System.loadLibrary("scompressor");
    }

    static native int nativeCompress(Bitmap bitmap, int quality, String destPath, boolean isArithmeticCoding);

}
