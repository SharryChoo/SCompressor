package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.sharry.lib.BuildConfig;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

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
        // 2. Down sample
        Bitmap downsampledBitmap;
        if (Bitmap.class.getName().equals(inputSource.getType().getName())) {
            downsampledBitmap = handleBitmapInputType(request);
        } else {
            downsampledBitmap = handleOtherInputType(request);
        }
        // 3. Quality compress
        File outputFile = FileUtil.createOutputFile(SCompressor.sContext);
        qualityCompress(request, downsampledBitmap, outputFile);
        // 4. Adapter 2 target type.
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "Output file is: " + outputFile.getAbsolutePath());
            Log.e(TAG, "Output file length is " + outputFile.length() / 1024 + "kb");
        }
        return findOutputAdapter(request.outputType).adapt(SCompressor.sContext,
                SCompressor.sAuthority, outputFile);
    }

    private static <OutputType, InputType> Bitmap handleBitmapInputType(
            Request<InputType, OutputType> request) throws Throwable {
        Bitmap originBitmap = (Bitmap) request.inputSource.getSource();
        if (originBitmap == null) {
            throw new NullPointerException("Please ensure input source not null!");
        }
        if (originBitmap.getConfig() == Bitmap.Config.ARGB_8888 || originBitmap.getConfig() == Bitmap.Config.RGB_565) {
            int sampleSize;
            if (request.destWidth == Request.INVALIDATE || request.destHeight == Request.INVALIDATE) {
                if (request.isAutoDownsample) {
                    sampleSize = calculateSampleSize(originBitmap.getWidth(), originBitmap.getHeight());
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
            return Bitmap.createScaledBitmap(originBitmap, originBitmap.getWidth() / sampleSize,
                    originBitmap.getHeight() / sampleSize, true);
        } else {
            return handleOtherInputType(request);
        }
    }

    private static <OutputType, InputType> Bitmap handleOtherInputType(
            Request<InputType, OutputType> request
    ) throws Throwable {
        // 1. Adapter input data 2 input path.
        FileDescriptor fd = findInputWriter(request.inputSource.getType())
                .adapt(SCompressor.sContext, SCompressor.sAuthority, request.inputSource);
        // 2. Do compress.
        // 2.1 Nearest Neighbour down sampling compress
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (request.destWidth == Request.INVALIDATE || request.destHeight == Request.INVALIDATE) {
            if (request.isAutoDownsample) {
                options.inSampleSize = calculateSampleSize(fd);
            } else {
                Log.i(TAG, "Do not need down sample");
            }
        } else {
            options.inSampleSize = calculateSampleSize(fd,
                    request.destWidth, request.destHeight);
        }
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    private static <Input> FileDescriptorAdapter<Input> findInputWriter(Class<Input> inputType) {
        FileDescriptorAdapter<Input> writer = null;
        for (FileDescriptorAdapter fileDescriptorAdapter : SCompressor.INPUT_ADAPTERS) {
            if (fileDescriptorAdapter.isWriter(inputType)) {
                writer = fileDescriptorAdapter;
            }
        }
        if (writer == null) {
            throw new UnsupportedOperationException("Cannot find an adapter that can convert "
                    + inputType.getName() + " to pre quality compressed bitmap");
        }
        return writer;
    }

    private static <Target> OutputAdapter<Target> findOutputAdapter(Class<Target> targetType) {
        OutputAdapter<Target> adapter = null;
        for (OutputAdapter outputAdapter : SCompressor.OUTPUT_ADAPTERS) {
            if (outputAdapter.isAdapter(targetType)) {
                adapter = outputAdapter;
            }
        }
        if (adapter == null) {
            throw new UnsupportedOperationException("Cannot find an adapter that can convert " +
                    "compressed file path to" + targetType.getName());
        }
        return adapter;
    }

    private static <OutputType, InputType> void qualityCompress(Request<InputType, OutputType> request,
                                                                Bitmap downsampledBitmap, File outputFile) throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !request.isArithmeticCoding) {
            skiaCompress(request, downsampledBitmap, outputFile);
        } else {
            libjpegTurboCompress(request, downsampledBitmap, outputFile);
        }
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

}
