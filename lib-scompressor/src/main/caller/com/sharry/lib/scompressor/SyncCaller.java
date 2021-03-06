package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sharry.lib.BuildConfig;
import com.sharry.lib.scompressor.recycleable.RecyclableBufferedInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.sharry.lib.scompressor.Core.calculateSampleSize;
import static com.sharry.lib.scompressor.SCompressor.TAG;
import static com.sharry.lib.scompressor.SCompressor.sArrayPool;
import static com.sharry.lib.scompressor.SCompressor.sIsDebug;

/**
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-19 14:13
 */
@SuppressWarnings("all")
final class SyncCaller {

    static <InputType, OutputType> OutputType execute(Request<InputType, OutputType> request) throws Throwable {
        if (sIsDebug) {
            Log.e(TAG, request.toString());
        }
        // 1. Do down sample
        Bitmap downsampledBitmap = doDownsampler(request);
        if (downsampledBitmap == null) {
            return null;
        }
        // 2. If output type is bitmap, return directly.
        if (Bitmap.class.getName().equals(request.outputType.getName())) {
            return (OutputType) downsampledBitmap;
        }
        // 3. Do requestedQuality compress
        File compressedFile = doQualityCompress(request, downsampledBitmap);
        // 4. Adapter 2 target type.
        if (sIsDebug) {
            Log.e(TAG, "Compressed File is: " + compressedFile.getAbsolutePath());
            Log.e(TAG, "Compressed File length is " + compressedFile.length() / 1024 + "kb");
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
        if (sIsDebug) {
            Log.e(TAG, "Origin bitmap size is [" + originBitmap.getWidth() + ", " + originBitmap.getHeight() + "]");
        }
        int sampleSize;
        if (request.requestedWidth == Request.INVALIDATE || request.requestedHeight == Request.INVALIDATE) {
            if (request.isAutoDownsample) {
                sampleSize = Core.calculateAutoSampleSize(originBitmap.getWidth(), originBitmap.getHeight());
            } else {
                sampleSize = 1;
            }
        } else {
            sampleSize = calculateSampleSize(originBitmap.getWidth(), originBitmap.getHeight(),
                    request.requestedWidth, request.requestedHeight);
        }
        Bitmap downsampled;
        if (sampleSize == 1) {
            // Do not need down sample.
            downsampled = originBitmap;
        } else {
            // Use bilinear filtering.
            downsampled = Bitmap.createScaledBitmap(originBitmap, originBitmap.getWidth() / sampleSize,
                    originBitmap.getHeight() / sampleSize, true);
        }
        if (sIsDebug) {
            Log.e(TAG, "Downsampled bitmap size is [" + downsampled.getWidth() + ", " + downsampled.getHeight() + "]");
        }
        return downsampled;
    }

    /**
     * Thanks for Glide.
     * <p>
     * 5MB. This is the max image header size we can handle, we preallocate a much smaller buffer but
     * will resize up to this amount if necessary.
     */
    private static final int HEADER_MARK_POSITION = 5 * 1024 * 1024;

    /**
     * Thanks for Glide.
     * <p>
     * 10MB. This is the max image header size we can handle, we preallocate a much smaller buffer
     * but will resize up to this amount if necessary.
     */
    private static final int DECODE_MARK_POSITION = 10 * 1024 * 1024;

    private static <OutputType, InputType> Bitmap handleOtherInputType(Request<InputType, OutputType> request) throws Throwable {
        // 1. Adapter inputSource 2 FileDescriptor.
        InputStream is = findInputAdapter(request.inputSource.getType())
                .adapt(SCompressor.sContext, SCompressor.sAuthority, request.inputSource);
        // 2. with input stream for this fd.
        if (!is.markSupported()) {
            is = new RecyclableBufferedInputStream(is, sArrayPool);
        }
        // 3. Ensure color channel.
        BitmapFactory.Options options = new BitmapFactory.Options();
        is.mark(HEADER_MARK_POSITION);     // mark start.
        options.inPreferredConfig = ImageUtil.hasAlpha(is) ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        is.reset();                        // reset
        // 4. Ensure sample size.
        options.inJustDecodeBounds = true;
        is.mark(DECODE_MARK_POSITION);     // mark start.
        BitmapFactory.decodeStream(is, null, options);
        is.reset();                        // reset
        options.inJustDecodeBounds = false;
        if (sIsDebug) {
            Log.e(TAG, "Origin bitmap size is [" + options.outWidth + ", " + options.outHeight + "]");
        }
        // calculate sample size.
        if (request.requestedWidth == Request.INVALIDATE || request.requestedHeight == Request.INVALIDATE) {
            if (request.isAutoDownsample) {
                options.inSampleSize = Core.calculateAutoSampleSize(options.outWidth, options.outHeight);
            } else {
                if (sIsDebug) {
                    Log.i(TAG, "Do not need down sample");
                }
            }
        } else {
            options.inSampleSize = Core.calculateSampleSize(options.outWidth, options.outHeight,
                    request.requestedWidth, request.requestedHeight);
        }
        // 5. Do Neighbour down sampling compress
        Bitmap downsampled = BitmapFactory.decodeStream(is, null, options);
        if (sIsDebug) {
            Log.e(TAG, "Downsampled bitmap size is [" + downsampled.getWidth() + ", " + downsampled.getHeight() + "]");
        }
        // close the stream.
        is.close();
        return downsampled;
    }

    private static <InputType> InputAdapter<InputType> findInputAdapter(Class<InputType> inputType) {
        InputAdapter<InputType> adapter = null;
        for (InputAdapter current : SCompressor.sInputAdapters) {
            if (current.isAdapter(inputType)) {
                adapter = current;
            }
        }
        if (adapter == null) {
            throw new UnsupportedOperationException("Cannot find an adapter that can convert "
                    + inputType.getName() + " to pre requestedQuality compressed bitmap");
        }
        return adapter;
    }

    // ///////////////////////////////////////////// Quality Compress ///////////////////////////////////////////////////////////

    private static <OutputType, InputType> File doQualityCompress(Request<InputType, OutputType> request,
                                                                  Bitmap downsampledBitmap) throws Throwable {
        boolean hasAlpha = downsampledBitmap.hasAlpha();
        File compressedFile = FileUtil.createOutputFile(
                SCompressor.sContext,
                hasAlpha ? request.withAlpha.suffix() : request.withoutAlpha.suffix()
        );
        compressByFormat(
                downsampledBitmap,
                hasAlpha ? request.withAlpha : request.withoutAlpha,
                request.requestedQuality,
                request.isArithmeticCoding,
                request.requestedLength,
                compressedFile
        );
        return compressedFile;
    }

    private static void compressByFormat(Bitmap sourceBitmap,
                                         CompressFormat format,
                                         int requestedQuality,
                                         boolean isArithmeticCoding,
                                         int requestedLength,
                                         File compressedFile) throws IOException {
        switch (format) {
            case WEBP:
                Core.compressWebp(sourceBitmap, requestedQuality, requestedLength, compressedFile);
            case PNG:
                Core.compressPng(sourceBitmap, requestedQuality, requestedLength, compressedFile);
                break;
            case JPEG:
            default:
                Core.compressJpeg(sourceBitmap, requestedQuality, isArithmeticCoding,
                        requestedLength, compressedFile);
                break;
        }
    }

    private static <Target> OutputAdapter<Target> findOutputAdapter(Class<Target> targetType) {
        OutputAdapter<Target> adapter = null;
        for (OutputAdapter current : SCompressor.sOutputAdapters) {
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
