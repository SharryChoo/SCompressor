package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.sharry.lib.scompressor.SCompressor.TAG;

/**
 * The core algorithm associated with picture compress.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/11/2019 4:53 PM
 */
final class Core {

    // ///////////////////////////////// Down sampler //////////////////////////////////////

    static int calculateAutoSampleSize(int sourceWidth, int sourceHeight) {
        sourceWidth = sourceWidth % 2 == 1 ? sourceWidth + 1 : sourceWidth;
        sourceHeight = sourceHeight % 2 == 1 ? sourceHeight + 1 : sourceHeight;

        int longSide = Math.max(sourceWidth, sourceHeight);
        int shortSide = Math.min(sourceWidth, sourceHeight);

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

    static int calculateSampleSize(int sourceWidth, int sourceHeight, int requestedWidth, int requestedHeight) {
        if (sourceWidth < requestedWidth && sourceHeight < requestedHeight) {
            return 1;
        }
        // 1. Calculate exact factor.
        double exactScaleFactor = Math.max((float) requestedWidth / (float) sourceWidth,
                (float) requestedHeight / (float) sourceHeight);
        // 2. Calculate scale factor.
        int outWidth = (int) Math.round(exactScaleFactor * sourceWidth);
        int outHeight = (int) Math.round(exactScaleFactor * sourceHeight);
        int widthScaleFactor = sourceWidth / outWidth;
        int heightScaleFactor = sourceHeight / outHeight;
        int scaleFactor = Math.max(widthScaleFactor, heightScaleFactor);
        // 3. Calculate sample size.(convert scaleFactor to 2 power.)
        int powerOfTwoSampleSize = Math.max(1, Integer.highestOneBit(scaleFactor));
        if (powerOfTwoSampleSize < (1.f / exactScaleFactor)) {
            powerOfTwoSampleSize = powerOfTwoSampleSize << 1;
        }
        // 4. Adjust weird picture.
        final float totalPixels = sourceWidth * sourceHeight;
        final float desirePixels = requestedWidth * requestedHeight << 1;
        while (totalPixels / (powerOfTwoSampleSize * powerOfTwoSampleSize) > desirePixels) {
            powerOfTwoSampleSize = powerOfTwoSampleSize << 1;
        }
        return powerOfTwoSampleSize;
    }
    // ///////////////////////////////// Quality compress //////////////////////////////////////

    static void compressJpeg(Bitmap sourceBitmap, int requestedQuality, boolean isArithmeticCoding,
                             int requestedLength, File compressedFile) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isArithmeticCoding) {
            compressBySkia(sourceBitmap, requestedQuality, requestedLength, compressedFile, Bitmap.CompressFormat.JPEG);
        } else {
            compressByLibjpegTurbo(sourceBitmap, requestedQuality, isArithmeticCoding, requestedLength, compressedFile);
        }
    }

    static void compressPng(Bitmap sourceBitmap, int requestedQuality,
                            int requestedLength, File compressedFile) throws IOException {
        compressBySkia(sourceBitmap, requestedQuality, requestedLength, compressedFile, Bitmap.CompressFormat.PNG);
    }

    static void compressWebp(Bitmap sourceBitmap, int requestedQuality,
                             int requestedLength, File compressedFile) throws IOException {
        compressBySkia(sourceBitmap, requestedQuality, requestedLength, compressedFile, Bitmap.CompressFormat.WEBP);
    }

    static void compressBySkia(Bitmap sourceBitmap, int quality,
                               int requestedLength, File compressedFile,
                               Bitmap.CompressFormat compressFormat) throws IOException {
        do {
            FileOutputStream fos = new FileOutputStream(compressedFile);
            sourceBitmap.compress(compressFormat, quality, fos);
            fos.flush();
            fos.close();
            quality -= 10;
        } while (
                requestedLength != Request.INVALIDATE &&
                        compressedFile.length() > requestedLength &&
                        quality > 0
        );
    }

    private static void compressByLibjpegTurbo(Bitmap sourceBitmap, int requestedQuality,
                                               boolean isArithmeticCoding,
                                               int requestedLength, File compressedFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // if bitmap config is Hardware, we need convert 2 other type ensure we can fetch bitmap pixels.
            if (sourceBitmap.getConfig() == Bitmap.Config.HARDWARE) {
                sourceBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
        }
        do {
            int compressStatus = nativeCompress(sourceBitmap, requestedQuality,
                    compressedFile.getAbsolutePath(), isArithmeticCoding);
            // Verify compress result.
            if (compressStatus == 0) {
                Log.e(TAG, "Compress failed.");
                throw new RuntimeException("Native requestedQuality compress failed.");
            }
            requestedQuality -= 10;
        } while (requestedLength != Request.INVALIDATE &&
                compressedFile.length() > requestedLength &&
                requestedQuality > 0);
    }

    // ///////////////////////////////// Native method //////////////////////////////////////

    static {
        System.loadLibrary("scompressor");
    }

    static native int nativeCompress(Bitmap bitmap, int quality, String destPath, boolean isArithmeticCoding);

}
