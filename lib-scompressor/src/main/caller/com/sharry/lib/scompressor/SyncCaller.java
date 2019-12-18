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

/**
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-19 14:13
 */
@SuppressWarnings("all")
final class SyncCaller {

    static <InputType, OutputType> OutputType execute(Request<InputType, OutputType> request) throws Throwable {
        if (BuildConfig.DEBUG) {
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
        if (sampleSize == 1) {
            Log.i(TAG, "Do not need down sample");
            return originBitmap;
        }
        // Use bilinear filtering.
        return Bitmap.createScaledBitmap(originBitmap, originBitmap.getWidth() / sampleSize,
                originBitmap.getHeight() / sampleSize, true);
    }

    /**
     * Thanks for Glide.
     * <p>
     * 5MB. This is the max image header size we can handle, we preallocate a much smaller buffer but
     * will resize up to this amount if necessary.
     */
    private static final int MARK_POSITION = 5 * 1024 * 1024;

    private static <OutputType, InputType> Bitmap handleOtherInputType(Request<InputType, OutputType> request) throws Throwable {
        // 1. Adapter inputSource 2 FileDescriptor.
        InputStream originIs = findInputAdapter(request.inputSource.getType())
                .adapt(SCompressor.sContext, SCompressor.sAuthority, request.inputSource);
        // 2. create input stream for this fd.
        InputStream wrapperIs = new RecyclableBufferedInputStream(originIs, sArrayPool);
        wrapperIs.mark(MARK_POSITION);  // mark start.
        // 3. Ensure color channel.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = ImageUtil.hasAlpha(wrapperIs) ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        wrapperIs.reset();             // back to start.
        // 4. Ensure sample size.
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(wrapperIs, null, options);
        wrapperIs.reset();              // back to start.
        options.inJustDecodeBounds = false;
        // calculate sample size.
        if (request.requestedWidth == Request.INVALIDATE || request.requestedHeight == Request.INVALIDATE) {
            if (request.isAutoDownsample) {
                options.inSampleSize = Core.calculateAutoSampleSize(options.outWidth, options.outHeight);
            } else {
                Log.i(TAG, "Do not need down sample");
            }
        } else {
            options.inSampleSize = Core.calculateSampleSize(options.outWidth, options.outHeight,
                    request.requestedWidth, request.requestedHeight);
        }
        Log.i(TAG, "options.inSampleSize is " + options.inSampleSize);
        // 5. Do Neighbour down sampling compress
        Bitmap result = BitmapFactory.decodeStream(wrapperIs, null, options);
        // close the stream.
        wrapperIs.close();
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
