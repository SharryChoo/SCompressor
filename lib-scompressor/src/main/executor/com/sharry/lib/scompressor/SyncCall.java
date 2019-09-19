package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;

import static com.sharry.lib.scompressor.Core.calculateSampleSize;
import static com.sharry.lib.scompressor.Core.createUnsuspectedFile;
import static com.sharry.lib.scompressor.Core.nativeCompress;
import static com.sharry.lib.scompressor.SCompressor.TAG;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-09-19 14:13
 */
final class SyncCall {

    private static final String UNSUSPECTED_FILE_PREFIX = "SCompressor_";

    static <InputType, OutputType> OutputType execute(Request<InputType, OutputType> request) throws Throwable {
        // 1. validate.
        DataSource<InputType> inputSource = request.inputSource;
        DataSource<OutputType> outputSource = request.outputSource;
        if (inputSource.getSource() == null) {
            throw new NullPointerException();
        }
        Log.i(TAG, request.toString());
        // 2. Down sample
        Bitmap downsampledBitmap;
        if (Bitmap.class.getSimpleName().equals(request.inputSource.getType().getName())) {
            downsampledBitmap = handleBitmapInputType(request);
        } else {
            downsampledBitmap = handleOtherInputType(request);
        }
        // 3. Quality compress
        File outputFile;
        if (String.class.equals(outputSource.getType()) && outputSource.getSource() != null) {
            outputFile = new File((String) outputSource.getSource());
        } else {
            outputFile = createUnsuspectedFile();
        }
        qualityCompress(request, downsampledBitmap, outputFile);
        // 4. Adapter 2 target type.
        Log.i(TAG, "Output file is: " + outputFile.getAbsolutePath());
        Log.i(TAG, "Output file length is " + outputFile.length() / 1024 + "kb");
        return findOutputAdapter(outputSource.getType()).adapt(outputFile);
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
        String inputFilePath = findInputWriter(request.inputSource.getType())
                .writeToDisk(request.inputSource);
        // 2. Do compress.
        // 2.1 Nearest Neighbour down sampling compress
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (request.destWidth == Request.INVALIDATE || request.destHeight == Request.INVALIDATE) {
            if (request.isAutoDownsample) {
                options.inSampleSize = calculateSampleSize(inputFilePath);
            } else {
                Log.i(TAG, "Do not need down sample");
            }
        } else {
            options.inSampleSize = calculateSampleSize(inputFilePath,
                    request.destWidth, request.destHeight);
        }
        Bitmap downsampledBitmap = BitmapFactory.decodeFile(inputFilePath, options);
        // 2.2 Try to rotate Bitmap
        downsampledBitmap = Core.rotateBitmap(downsampledBitmap,
                Core.readImageRotateAngle(inputFilePath));
        // If the file is unsuspected file, then delete it.
        if (inputFilePath.startsWith(UNSUSPECTED_FILE_PREFIX)) {
            File tempFile = new File(inputFilePath);
            tempFile.delete();
        }
        return downsampledBitmap;
    }

    private static <Input> InputWriter<Input> findInputWriter(Class<Input> inputType) {
        InputWriter<Input> writer = null;
        for (InputWriter inputWriter : SCompressor.INPUT_ADAPTERS) {
            if (inputWriter.isWriter(inputType)) {
                writer = inputWriter;
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
