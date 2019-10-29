package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Dimension;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileDescriptor;

import static androidx.annotation.Dimension.PX;
import static com.sharry.lib.scompressor.SCompressor.TAG;


/**
 * The options associated with Picture Compress.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/29 13:03
 */
public class Request<InputType, OutputType> {

    public static final int INVALIDATE = -1;

    /**
     * Input source associated with this compress task.
     */
    final InputSource<InputType> inputSource;

    /**
     * Compress output source associated with this compress task.
     */
    final Class<OutputType> outputType;

    /**
     * Compress quality.
     */
    final int quality;

    /**
     * Compress out desire width.
     */
    @Dimension(unit = PX)
    final int destWidth;

    /**
     * Compress out desire height
     */
    @Dimension(unit = PX)
    final int destHeight;

    /**
     * Set up desire file length after compressed.
     * <p>
     * Unit is byte.
     */
    final int desireOutputFileLength;

    /**
     * Control auto down sample or not.
     * <p>
     * default is true.
     */
    final boolean isAutoDownsample;

    /**
     * Control open arithmetic coding or not.
     * <p>
     * if true will use arithmetic coding, false use Huffman.
     */
    final boolean isArithmeticCoding;

    private Request(
            InputSource<InputType> inputSource,
            Class<OutputType> outputType,
            int quality,
            int destWidth,
            int destHeight,
            int desireOutputFileLength,
            boolean isAutoDownsample,
            boolean isArithmeticCoding) {
        this.inputSource = inputSource;
        this.outputType = outputType;
        this.quality = quality;
        this.destWidth = destWidth;
        this.destHeight = destHeight;
        this.desireOutputFileLength = desireOutputFileLength;
        this.isAutoDownsample = isAutoDownsample;
        this.isArithmeticCoding = isArithmeticCoding;
    }

    @Override
    public String toString() {
        return "SCompressor Request{" +
                "inputSource = " + inputSource.getType().getSimpleName() +
                ", outputType = " + outputType.toString() +
                ", quality = " + quality +
                ", isAutoDownsample = " + isAutoDownsample +
                ", destWidth = " + destWidth +
                ", destHeight = " + destHeight +
                '}';
    }

    /**
     * The builder associated with Request
     *
     * @param <InputType>
     * @param <OutputType>
     */
    public static class Builder<InputType, OutputType> {

        private static final int DEFAULT_QUALITY = 75;
        private static final Class DEFAULT_OUTPUT_TYPE = File.class;
        private static final int INVALIDATE = -1;

        private InputSource inputSource;
        private Class outputType = DEFAULT_OUTPUT_TYPE;
        private int quality = DEFAULT_QUALITY;
        private int desireWidth = INVALIDATE;
        private int desireHeight = INVALIDATE;
        private int desireOutputFileLength = INVALIDATE;
        private boolean isAutoDownSample = true;
        private boolean isArithmeticCoding = false;

        Builder() {

        }

        private Builder(InputSource inputSource,
                        Class outputSource,
                        int quality,
                        int desireWidth,
                        int desireHeight,
                        int desireOutputFileLength,
                        boolean isAutoDownSample,
                        boolean isArithmeticCoding) {
            this.inputSource = inputSource;
            this.outputType = outputSource;
            this.quality = quality;
            this.desireWidth = desireWidth;
            this.desireHeight = desireHeight;
            this.desireOutputFileLength = desireOutputFileLength;
            this.isAutoDownSample = isAutoDownSample;
            this.isArithmeticCoding = isArithmeticCoding;
        }

        /**
         * Set source image file path associated with this compress task.
         */
        public Builder<String, OutputType> setInputPath(@NonNull final String srcPath) {
            Preconditions.checkNotNull(srcPath);
            return setInputSource(new InputSource<String>() {
                @NonNull
                @Override
                public Class<String> getType() {
                    return String.class;
                }

                @NonNull
                @Override
                public String getSource() {
                    return srcPath;
                }
            });
        }

        /**
         * Set source image bitmap associated with this compress task.
         */
        public Builder<Bitmap, OutputType> setInputBitmap(@NonNull final Bitmap srcBitmap) {
            Preconditions.checkNotNull(srcBitmap);
            return setInputSource(new InputSource<Bitmap>() {
                @NonNull
                @Override
                public Class<Bitmap> getType() {
                    return Bitmap.class;
                }

                @NonNull
                @Override
                public Bitmap getSource() {
                    return srcBitmap;
                }
            });
        }

        /**
         * Set source image fd associated with this compress task
         */
        public Builder<FileDescriptor, OutputType> setInputFileDescriptor(@NonNull final FileDescriptor fd) {
            Preconditions.checkNotNull(fd);
            return setInputSource(new InputSource<FileDescriptor>() {
                @NonNull
                @Override
                public Class<FileDescriptor> getType() {
                    return FileDescriptor.class;
                }

                @NonNull
                @Override
                public FileDescriptor getSource() {
                    return fd;
                }
            });
        }

        /**
         * Set u custom input source.
         *
         * @param inputSource desc input source.
         */
        public <NewInputType> Builder<NewInputType, OutputType> setInputSource(
                @NonNull InputSource<NewInputType> inputSource) {
            this.inputSource = inputSource;
            return new Builder<>(
                    inputSource,
                    outputType,
                    quality,
                    desireWidth,
                    desireHeight,
                    desireOutputFileLength,
                    isAutoDownSample,
                    isArithmeticCoding
            );
        }

        /**
         * Set desire output quality when compressing start.
         *
         * @param quality range [0, 100]
         */
        public Builder<InputType, OutputType> setQuality(@IntRange(from = 0, to = 100) int quality) {
            this.quality = Preconditions.checkRange(quality, 0, 100);
            return this;
        }

        /**
         * Set desire output image width and height when compress completed.
         */
        public Builder<InputType, OutputType> setDesireSize(int desireWidth, int desireHeight) {
            this.desireWidth = desireWidth;
            this.desireHeight = desireHeight;
            return this;
        }

        /**
         * Set up support auto down sample or not.
         */
        public Builder<InputType, OutputType> setAutoDownsample(boolean autoDownSample) {
            isAutoDownSample = autoDownSample;
            return this;
        }

        /**
         * Set up support arithmetic coding or not.
         * <p>
         * Arithmetic coding will get performance compression ratio, but have some compatibility questions.
         */
        public Builder<InputType, OutputType> setArithmeticCoding(boolean isArithmeticCoding) {
            this.isArithmeticCoding = isArithmeticCoding;
            return this;
        }

        /**
         * Set up desire file length after compressed.
         * <p>
         * Unit is byte.
         */
        public Builder<InputType, OutputType> setDesireLength(int desireOutputFileLength) {
            this.desireOutputFileLength = desireOutputFileLength;
            return this;
        }

        /**
         * Convert output type to Bitmap
         */
        public Builder<InputType, Bitmap> asBitmap() {
            return as(Bitmap.class);
        }

        /**
         * Convert output type to byte array.
         */
        public Builder<InputType, byte[]> asByteArray() {
            return as(byte[].class);
        }

        /**
         * Convert output type to Uri.
         */
        public Builder<InputType, Uri> asUri() {
            return as(Uri.class);
        }

        /**
         * Convert output type to File path.
         */
        public Builder<InputType, String> asFilePath() {
            return as(String.class);
        }

        /**
         * Convert output type to File.
         */
        public Builder<InputType, File> asFile() {
            return as(File.class);
        }

        /**
         * Set u custom output source.
         *
         * @param outputType desc output data source.
         */
        public <NewOutputType> Builder<InputType, NewOutputType> as(Class<NewOutputType> outputType) {
            Preconditions.checkNotNull(outputType);
            this.outputType = outputType;
            return new Builder<>(
                    inputSource,
                    outputType,
                    quality,
                    desireWidth,
                    desireHeight,
                    desireOutputFileLength,
                    isAutoDownSample,
                    isArithmeticCoding
            );
        }

        /**
         * Execute async task with lambda callback.
         *
         * @param lambdaCallback the lambda callback related on async task.
         */
        public void asyncCall(@NonNull final ICompressorCallbackLambda<OutputType> lambdaCallback) {
            Preconditions.checkNotNull(lambdaCallback);
            asyncCall(new ICompressorCallback<OutputType>() {
                @Override
                public void onSuccess(@NonNull OutputType compressedData) {
                    lambdaCallback.onComplete(true, compressedData);
                }

                @Override
                public void onFailed(@NonNull Throwable e) {
                    Log.e(TAG, e.getMessage(), e);
                    lambdaCallback.onComplete(false, null);
                }
            });
        }

        /**
         * Execute async task with callback.
         *
         * @param callback the callback related on async task.
         */
        public void asyncCall(@NonNull ICompressorCallback<OutputType> callback) {
            Preconditions.checkNotNull(callback);
            Preconditions.checkNotNull(inputSource);
            SCompressor.asyncCall(
                    new Request<InputType, OutputType>(
                            inputSource,
                            outputType,
                            quality,
                            desireWidth,
                            desireHeight,
                            desireOutputFileLength,
                            isAutoDownSample,
                            isArithmeticCoding
                    ),
                    callback
            );
        }

        /**
         * Execute sync task.
         */
        @Nullable
        public OutputType syncCall() {
            Preconditions.checkNotNull(inputSource);
            return SCompressor.syncCall(
                    new Request<InputType, OutputType>(
                            inputSource,
                            outputType,
                            quality,
                            desireWidth,
                            desireHeight,
                            desireOutputFileLength,
                            isAutoDownSample,
                            isArithmeticCoding
                    )
            );
        }
    }

}
