package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.sharry.lib.BuildConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import static com.sharry.lib.scompressor.Core.calculateSampleSize;
import static com.sharry.lib.scompressor.Core.nativeCompress;
import static com.sharry.lib.scompressor.SCompressor.TAG;

/**
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-19 14:13
 */
final class SyncCaller {

    static <InputType, OutputType> OutputType execute(Request<InputType, OutputType> request) throws Throwable {
        // 1. validate.
        InputSource<InputType> inputSource = request.inputSource;
        if (inputSource.getSource() == null) {
            throw new NullPointerException();
        }
        if (BuildConfig.DEBUG) {
            Log.e(TAG, request.toString());
        }
        // 2. Do down sample
        Bitmap downsampledBitmap = doDownsampler(request);
        if (downsampledBitmap == null) {
            return null;
        }
        // 3. If output type is bitmap, return directly.
        if (Bitmap.class.getName().equals(request.outputType.getName())) {
            return (OutputType) downsampledBitmap;
        }
        // 3. Do quality compress
        File compressedFile = doQualityCompress(request, downsampledBitmap);
        // 4. Adapter 2 target type.
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "Output file is: " + compressedFile.getAbsolutePath());
            Log.e(TAG, "Output file length is " + compressedFile.length() / 1024 + "kb");
        }
        return findOutputAdapter(request.outputType).adapt(SCompressor.sContext,
                SCompressor.sAuthority, compressedFile);
    }

    // ///////////////////////////////////////////// Downsampler ///////////////////////////////////////////////////////////

    private static <OutputType, InputType> Bitmap doDownsampler(Request<InputType, OutputType> request) throws Throwable {
        Bitmap downsampledBitmap;
        if (Bitmap.class.getName().equals(request.inputSource.getType().getName())) {
            downsampledBitmap = handleBitmapInputType(request);
        } else {
            downsampledBitmap = handleOtherInputType(request);
        }
        return downsampledBitmap;
    }

    private static <OutputType, InputType> Bitmap handleBitmapInputType(Request<InputType, OutputType> request) {
        Bitmap originBitmap = (Bitmap) request.inputSource.getSource();
        if (originBitmap == null) {
            throw new NullPointerException("Please ensure input source not null!");
        }
        int sampleSize;
        if (request.destWidth == Request.INVALIDATE || request.destHeight == Request.INVALIDATE) {
            if (request.isAutoDownsample) {
                sampleSize = Core.calculateAutoSampleSize(originBitmap.getWidth(), originBitmap.getHeight());
            } else {
                sampleSize = 1;
            }
        } else {
            sampleSize = calculateSampleSize(originBitmap.getWidth(), originBitmap.getHeight(),
                    request.destWidth, request.destHeight);
        }
        if (sampleSize == 1) {
            Log.i(TAG, "Do not need down sample");
            return originBitmap;
        }
        // Use bilinear filtering.
        return Bitmap.createScaledBitmap(originBitmap, originBitmap.getWidth() / sampleSize,
                originBitmap.getHeight() / sampleSize, true);
    }

    /**
     * 5MB. This is the max image header size we can handle, we preallocate a much smaller buffer but
     * will resize up to this amount if necessary.
     */
    private static final int MARK_POSITION = 5 * 1024 * 1024;

    private static <OutputType, InputType> Bitmap handleOtherInputType(
            Request<InputType, OutputType> request
    ) throws Throwable {
        // 1. Adapter inputSource 2 FileDescriptor.
        FileDescriptor fd = findInputAdapter(request.inputSource.getType())
                .adapt(SCompressor.sContext, SCompressor.sAuthority, request.inputSource);
        // 2. create input stream for this fd.
        InputStream is = new BufferedInputStream(new FileInputStream(fd));
        // mark start.
        is.mark(MARK_POSITION);
        // 3. Ensure color channel.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = ImageUtil.hasAlpha(is) ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        is.reset(); // back to start.
        // 4. Ensure sample size.
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        is.reset(); // back to start.
        options.inJustDecodeBounds = false;
        // calculate sample size.
        if (request.destWidth == Request.INVALIDATE || request.destHeight == Request.INVALIDATE) {
            if (request.isAutoDownsample) {
                options.inSampleSize = Core.calculateAutoSampleSize(options.outWidth, options.outHeight);
            } else {
                Log.i(TAG, "Do not need down sample");
            }
        } else {
            options.inSampleSize = Core.calculateSampleSize(options.outWidth, options.outHeight,
                    request.destWidth, request.destHeight);
        }
        Log.i(TAG, "options.inSampleSize is " + options.inSampleSize);
        // 5. Do Neighbour down sampling compress
        Bitmap result = BitmapFactory.decodeStream(is, null, options);
        is.close();
        return result;
    }

    private static <InputType> InputAdapter<InputType> findInputAdapter(Class<InputType> inputType) {
        InputAdapter<InputType> adapter = null;
        for (InputAdapter current : SCompressor.INPUT_ADAPTERS) {
            if (current.isAdapter(inputType)) {
                adapter = current;
            }
        }
        if (adapter == null) {
            throw new UnsupportedOperationException("Cannot find an adapter that can convert "
                    + inputType.getName() + " to pre quality compressed bitmap");
        }
        return adapter;
    }

    // ///////////////////////////////////////////// Quality Compress ///////////////////////////////////////////////////////////

    private static <OutputType, InputType> File doQualityCompress(Request<InputType, OutputType> request,
                                                                  Bitmap downsampledBitmap) throws Throwable {
        File compressedFile = FileUtil.createOutputFile(SCompressor.sContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !request.isArithmeticCoding) {
            skiaCompress(request, downsampledBitmap, compressedFile);
        } else {
            libjpegTurboCompress(request, downsampledBitmap, compressedFile);
        }
        return compressedFile;
    }

    private static <OutputType, InputType> void skiaCompress(Request<InputType, OutputType> request,
                                                             Bitmap downsampledBitmap,
                                                             File outputFile) throws Throwable {
        // 1. First compress.
        int quality = request.quality;
        do {
            FileOutputStream fos = new FileOutputStream(outputFile);
            downsampledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
            quality -= 10;
        } while (
                request.desireOutputFileLength != Request.INVALIDATE &&
                        outputFile.length() > request.desireOutputFileLength &&
                        quality > 0
        );
    }

    private static <OutputType, InputType> void libjpegTurboCompress(Request<InputType, OutputType> request,
                                                                     Bitmap downsampledBitmap, File outputFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // if bitmap config is Hardware, we need convert 2 other type ensure we can fetch bitmap pixels.
            if (downsampledBitmap.getConfig() == Bitmap.Config.HARDWARE) {
                downsampledBitmap = downsampledBitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
        }
        int quality = request.quality;
        do {
            int compressStatus = nativeCompress(downsampledBitmap, quality,
                    outputFile.getAbsolutePath(), request.isArithmeticCoding);
            // Verify compress result.
            if (compressStatus == 0) {
                Log.e(TAG, "Compress failed.");
                throw new RuntimeException("Native quality compress failed.");
            }
            quality -= 10;
        } while (
                request.desireOutputFileLength != Request.INVALIDATE &&
                        outputFile.length() > request.desireOutputFileLength &&
                        quality > 0
        );
    }

    private static <Target> OutputAdapter<Target> findOutputAdapter(Class<Target> targetType) {
        OutputAdapter<Target> adapter = null;
        for (OutputAdapter current : SCompressor.OUTPUT_ADAPTERS) {
            if (current.isAdapter(targetType)) {
                adapter = current;
            }
        }
        if (adapter == null) {
            throw new UnsupportedOperationException("Cannot find an adapter that can convert " +
                    "compressed file path to" + targetType.getName());
        }
        return adapter;
    }
}
