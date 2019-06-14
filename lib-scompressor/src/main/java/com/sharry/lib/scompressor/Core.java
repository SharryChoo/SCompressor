package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
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

    private static final String SUFFIX_JPEG = ".jpg";
    private static final String UNSUSPECTED_FILE_PREFIX = "SCompressor_";
    private static final int INVALIDATE = -1;

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
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if (request.destWidth == Request.INVALIDATE || request.destHeight == Request.INVALIDATE) {
            if (request.isSupportDownSample) {
                options.inSampleSize = Core.calculateSampleSize(inputFilePath);
            } else {
                Log.i(TAG, "Cannot support auto down sample");
            }
        } else {
            Core.calculateSampleSize(inputFilePath, request.destWidth, request.destHeight);
        }
        Bitmap downSampleBitmap = BitmapFactory.decodeFile(inputFilePath, options);
        // 2.2 Try to rotate Bitmap
        downSampleBitmap = Core.rotateBitmap(downSampleBitmap,
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
        int compressStatus = nativeCompress(downSampleBitmap, request.quality, outputFile.getAbsolutePath());
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

    static File createUnsuspectedFile() throws IOException {
        File tempFile = new File(
                Preconditions.checkNotNull(SCompressor.usableDir, "If U not set output path, " +
                        "Please invoke SCompressor.init config an usable directory."),
                UNSUSPECTED_FILE_PREFIX + System.currentTimeMillis() + SUFFIX_JPEG
        );
        if (tempFile.exists()) {
            tempFile.delete();
        }
        tempFile.createNewFile();
        return tempFile;
    }

    private static int calculateSampleSize(String filePath) {
        return calculateSampleSize(filePath, INVALIDATE, INVALIDATE);
    }

    private static int calculateSampleSize(String filePath, int destWidth, int destHeight) {
        int[] dimensions = getDimensions(filePath);
        return (destWidth == INVALIDATE || destHeight == INVALIDATE) ?
                calculateSampleSize(dimensions[0], dimensions[1]) :
                calculateSampleSize(dimensions[0], dimensions[1], destWidth, destHeight);
    }

    private static int calculateSampleSize(int srcWidth, int srcHeight) {
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

    private static int calculateSampleSize(int srcWidth, int srcHeight, int destWidth, int destHeight) {
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
            powerOfTwoSampleSize++;
        }
        return powerOfTwoSampleSize;
    }

    private static int readImageRotateAngle(String imagePath) throws IOException {
        int degree = 0;
        ExifInterface exifInterface = new ExifInterface(imagePath);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            default:
                break;
        }
        return degree;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        if (angle == 0) {
            return bitmap;
        }
        // Build rotate matrix
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // return rotated Bitmap
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int[] getDimensions(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false;
        return new int[]{options.outWidth, options.outHeight};
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

    ////////////////// native method /////////////////////

    static {
        System.loadLibrary("scompressor");
    }

    private static native int nativeCompress(Bitmap bitmap, int quality, String destPath);

}
