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

    private final static int DEFAULT_SAMPLE_WIDTH = 1080;
    private final static int DEFAULT_SAMPLE_HEIGHT = 1920;

    // ///////////////////////////////// Down sampler //////////////////////////////////////

    static int calculateAutoSampleSize(int sourceWidth, int sourceHeight) {
        return calculateSampleSize(sourceWidth, sourceHeight, DEFAULT_SAMPLE_WIDTH, DEFAULT_SAMPLE_HEIGHT);
    }

    static int calculateSampleSize(int sourceWidth, int sourceHeight, int requestedWidth, int requestedHeight) {
        // Fetch source long side and short side.
        int sourceLongSide = Math.max(sourceWidth, sourceHeight);
        int sourceShortSide = Math.min(sourceWidth, sourceHeight);
        // Fetch requested long side and short side.
        int requestLongSide = Math.max(requestedWidth, requestedHeight);
        int requestShortSide = Math.min(requestedWidth, requestedHeight);
        if (sourceLongSide < requestLongSide && sourceShortSide < requestShortSide) {
            return 1;
        }
        // 1. Calculate exact factor.
        float exactScaleFactor = Math.min(requestLongSide / (float) sourceLongSide,
                requestShortSide / (float) sourceShortSide);

        // 2. Calculate int scale factor.
        int outShortSide = (int) (exactScaleFactor * sourceShortSide + 0.5f); // round to int
        int outLongSide = (int) (exactScaleFactor * sourceLongSide + 0.5f);   // round to int
        // Math.min will large than requestSize.
        int scaleFactor = Math.min(sourceShortSide / outShortSide, sourceLongSide / outLongSide);

        // 3. Calculate sample size.(convert scaleFactor to 2 power.)
        return Math.max(1, Integer.highestOneBit(scaleFactor));
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
