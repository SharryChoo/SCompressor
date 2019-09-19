package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;

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
        // compress
        DataSource<InputType> inputSource = request.inputSource;
        DataSource<OutputType> outputSource = request.outputSource;
        if (inputSource.getSource() == null) {
            throw new NullPointerException();
        }
        Log.i(TAG, request.toString());
        // 1. Adapter input data 2 input path.
        String inputFilePath = findInputAdapter(inputSource.getType())
                .adapt(request, inputSource.getSource());
        // 2. Do compress.
        // 2.1 Nearest Neighbour down sampling compress
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (request.destWidth == Request.INVALIDATE || request.destHeight == Request.INVALIDATE) {
            if (request.isAutoDownsample) {
                options.inSampleSize = Core.calculateSampleSize(inputFilePath);
            } else {
                Log.i(TAG, "Cannot support auto down sample");
            }
        } else {
            options.inSampleSize = Core.calculateSampleSize(inputFilePath, request.destWidth, request.destHeight);
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
        // 2.3 Quality compress
        File outputFile;
        if (String.class.equals(outputSource.getType()) && outputSource.getSource() != null) {
            outputFile = new File((String) outputSource.getSource());
        } else {
            outputFile = createUnsuspectedFile();
        }
        int compressStatus = nativeCompress(downsampledBitmap, request.quality,
                outputFile.getAbsolutePath(), request.isArithmeticCoding);
        // Verify compress result.
        if (compressStatus == 0) {
            Log.e(TAG, "Compress failed.");
            throw new RuntimeException("Native quality compress failed.");
        }
        // 3. Adapter 2 target type.
        Log.i(TAG, "Output file is: " + outputFile.getAbsolutePath());
        Log.i(TAG, "Output file length is " + outputFile.length() / 1024 + "kb");
        return findOutputAdapter(outputSource.getType()).adapt(outputFile);
    }

    private static <Input> InputAdapter<Input> findInputAdapter(Class<Input> inputType) {
        InputAdapter<Input> adapter = null;
        for (InputAdapter inputAdapter : SCompressor.INPUT_ADAPTERS) {
            if (inputAdapter.isAdapter(inputType)) {
                adapter = inputAdapter;
            }
        }
        if (adapter == null) {
            throw new UnsupportedOperationException("Cannot find an adapter that can convert "
                    + inputType.getName() + " to pre quality compressed bitmap");
        }
        return adapter;
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

}
