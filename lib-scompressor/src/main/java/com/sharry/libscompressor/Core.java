package com.sharry.libscompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The core algorithm associated with picture compress.
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/11/2019 4:53 PM
 */
final class Core {

    private static final String TAG = Core.class.getSimpleName();
    private static final int INVALIDATE = -1;

    static <InputType, OutputType> OutputType execute(Request<InputType, OutputType> request) throws IOException {
        // compress
        DataSource<InputType> inputSource = request.inputSource;
        DataSource<OutputType> outputSource = request.outputSource;
        if (inputSource.getSource() == null) {
            throw new NullPointerException();
        }
        Log.i(TAG, request.toString());
        int compressStatus;
        File outputFile = String.class.equals(outputSource.getType()) ?
                new File((String) outputSource.getSource()) : createDefaultOutputFile();
        if (Bitmap.class.equals(inputSource.getType())) {
            compressStatus = compress(
                    (Bitmap) inputSource.getSource(),
                    request.quality,
                    request.destWidth,
                    request.destHeight,
                    outputFile.getAbsolutePath()
            );
        } else if (String.class.equals(request.inputSource.getType())) {
            compressStatus = compress(
                    (String) inputSource.getSource(),
                    request.quality,
                    request.destWidth,
                    request.destHeight,
                    outputFile.getAbsolutePath()
            );
        } else {
            throw new UnsupportedOperationException("Cannot compress input source like " + request.inputSource);
        }
        if (compressStatus == 0) {
            Log.e(TAG, "Compress failed.");
            return null;
        }
        Log.i(TAG, "->> output file is: " + outputFile.getAbsolutePath());
        Log.i(TAG, "->> Output file length is " + outputFile.length() / 1024 + "kb");
        // do Transform.
        OutputType result;
        if (byte[].class.equals(outputSource.getType())) {
            // Convert 2 byte array.
            result = (OutputType) getBytes(outputFile);
        } else if (Bitmap.class.equals(outputSource.getType())) {
            // Convert 2 bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            result = (OutputType) BitmapFactory.decodeFile(outputFile.getAbsolutePath(), options);
        } else if (String.class.equals(outputSource.getType())) {
            // Return file path.
            return (OutputType) outputFile.getAbsolutePath();
        } else {
            throw new UnsupportedOperationException();
        }
        // If output type not File path, do delete at here.
        outputFile.delete();
        return result;
    }

    private static File createDefaultOutputFile() throws IOException {
        File tempFile = new File(
                Preconditions.checkNotNull(SCompressor.mUsableDir, "If U not set output path, " +
                        "Please invoke SCompressor.init config an usable directory."),
                System.currentTimeMillis() + ".jpg"
        );
        if (tempFile.exists()) {
            tempFile.delete();
        }
        tempFile.createNewFile();
        return tempFile;
    }

    private static byte[] getBytes(File target) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(target);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    private static int compress(
            String inputPath,
            int quality,
            int destWidth,
            int destHeight,
            String outputPath) {
        // 1. Nearest Neighbour Resampling Compress
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = (destWidth == INVALIDATE || destHeight == INVALIDATE) ? calculateSampleSize(inputPath)
                : calculateSampleSize(inputPath, destWidth, destHeight);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(inputPath, options);
        // 2. Try to rotate Bitmap
        bitmap = rotateBitmap(bitmap, readImageRotateAngle(inputPath));
        // 3. Quality Compress
        return nativeCompress(bitmap, quality, outputPath);
    }

    private static int compress(
            Bitmap inputBitmap,
            int quality,
            int destWidth,
            int destHeight,
            String outputPath) {
        // 1. Bilinear Resampling
        int sampleSize = (destWidth == INVALIDATE || destHeight == INVALIDATE) ?
                calculateSampleSize(inputBitmap.getWidth(), inputBitmap.getHeight()) :
                calculateSampleSize(inputBitmap.getWidth(), inputBitmap.getHeight(), destWidth, destHeight);
        Bitmap compress = Bitmap.createScaledBitmap(inputBitmap, inputBitmap.getWidth() / sampleSize,
                inputBitmap.getHeight() / sampleSize, true);
        // 2. Quality Compress
        return nativeCompress(compress, quality, outputPath);
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

    private static int readImageRotateAngle(String imagePath) {
        int degree = 0;
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(imagePath);
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
        } catch (IOException e) {
            Log.e(TAG, "readImageRotateAngle failed.", e);
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

    static {
        System.loadLibrary("lib-compressor");
    }

    private static native int nativeCompress(Bitmap bitmap, int quality, String destPath);

}
