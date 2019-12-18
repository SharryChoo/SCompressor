package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Dimension;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

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
     * Compress requestedQuality.
     */
    final int requestedQuality;

    /**
     * Compress out desire width.
     */
    @Dimension(unit = PX)
    final int requestedWidth;

    /**
     * Compress out desire height
     */
    @Dimension(unit = PX)
    final int requestedHeight;

    /**
     * Set up desire file length after compressed.
     * <p>
     * Unit is byte.
     */
    final int requestedLength;

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

    /**
     * Control compressed file type.
     */
    CompressFormat withoutAlpha;

    /**
     * Control compressed file type.
     */
    CompressFormat withAlpha;

    private Request(
            InputSource<InputType> inputSource,
            Class<OutputType> outputType,
            int requestedQuality,
            int requestedWidth,
            int requestedHeight,
            int requestedLength,
            boolean isAutoDownsample,
            boolean isArithmeticCoding,
            CompressFormat withoutAlpha,
            CompressFormat withAlpha) {
        this.inputSource = inputSource;
        this.outputType = outputType;
        this.requestedQuality = requestedQuality;
        this.requestedWidth = requestedWidth;
        this.requestedHeight = requestedHeight;
        this.requestedLength = requestedLength;
        this.isAutoDownsample = isAutoDownsample;
        this.isArithmeticCoding = isArithmeticCoding;
        this.withoutAlpha = withoutAlpha;
        this.withAlpha = withAlpha;
    }

    @Override
    public String toString() {
        return "Request{" + "\n" +
                "inputSource=" + inputSource.getType().getName() + "\n" +
                ", outputType=" + outputType + "\n" +
                ", requestedQuality=" + requestedQuality + "\n" +
                ", requestedWidth=" + requestedWidth + "\n" +
                ", requestedHeight=" + requestedHeight + "\n" +
                ", requestedLength=" + requestedLength + "\n" +
                ", isAutoDownsample=" + isAutoDownsample + "\n" +
                ", isArithmeticCoding=" + isArithmeticCoding + "\n" +
                ", withoutAlpha=" + withoutAlpha.name() + "\n" +
                ", withAlpha=" + withAlpha.name() + "\n" +
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
        private int requestedQuality = DEFAULT_QUALITY;
        private int requestedWidth = INVALIDATE;
        private int requestedHeight = INVALIDATE;
        private int requestedLength = INVALIDATE;
        private boolean isAutoDownSample = true;
        private boolean isArithmeticCoding = false;
        private CompressFormat withoutAlpha = CompressFormat.JPEG;
        private CompressFormat withAlpha = CompressFormat.PNG;

        Builder() {

        }

        private Builder(InputSource inputSource,
                        Class outputSource,
                        int requestedQuality,
                        int requestedWidth,
                        int requestedHeight,
                        int requestedLength,
                        boolean isAutoDownSample,
                        boolean isArithmeticCoding,
                        CompressFormat withoutAlpha,
                        CompressFormat withAlpha) {
            this.inputSource = inputSource;
            this.outputType = outputSource;
            this.requestedQuality = requestedQuality;
            this.requestedWidth = requestedWidth;
            this.requestedHeight = requestedHeight;
            this.requestedLength = requestedLength;
            this.isAutoDownSample = isAutoDownSample;
            this.isArithmeticCoding = isArithmeticCoding;
            this.withoutAlpha = withoutAlpha;
            this.withAlpha = withAlpha;
        }

        /**
         * Set u custom input source.
         *
         * @param inputSource desc input source. Current support
         *                    Origin Bitmap {@link Bitmap},
         *                    File Path {@link String},
         *                    File Uri {@link Uri}
         */
        public <NewInputType> Builder<NewInputType, OutputType> setInputSource(final NewInputType inputSource) {
            this.inputSource = new InputSource() {
                @NonNull
                @Override
                public Class getType() {
                    return inputSource.getClass();
                }

                @Nullable
                @Override
                public Object getSource() {
                    return inputSource;
                }
            };
            return new Builder<>(
                    this.inputSource,
                    outputType,
                    requestedQuality,
                    requestedWidth,
                    requestedHeight,
                    requestedLength,
                    isAutoDownSample,
                    isArithmeticCoding,
                    withoutAlpha,
                    withAlpha
            );
        }

        /**
         * Set desire output requestedQuality when compressing start.
         *
         * @param quality range [0, 100]
         */
        public Builder<InputType, OutputType> setQuality(@IntRange(from = 0, to = 100) int quality) {
            this.requestedQuality = Preconditions.checkRange(quality, 0, 100);
            return this;
        }

        /**
         * Set desire output image width and height when compress completed.
         */
        public Builder<InputType, OutputType> setDesireSize(int desireWidth, int desireHeight) {
            this.requestedWidth = desireWidth;
            this.requestedHeight = desireHeight;
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
            this.requestedLength = desireOutputFileLength;
            return this;
        }

        /**
         * Set up output image type.
         *
         * @param withoutAlpha u can choose {@link CompressFormat#JPEG}, {@link CompressFormat#PNG}, {@link CompressFormat#WEBP}
         * @param withAlpha    u can choose {@link CompressFormat#PNG}, {@link CompressFormat#WEBP}
         */
        public Builder<InputType, OutputType> setCompressFormat(@NonNull CompressFormat withoutAlpha,
                                                                @NonNull CompressFormat withAlpha) {
            Preconditions.checkNotNull(withoutAlpha);
            Preconditions.checkNotNull(withAlpha);
            if (withAlpha == CompressFormat.JPEG) {
                Log.w(TAG, "Image type JPEG will lost alpha channel.");
            }
            this.withoutAlpha = withoutAlpha;
            this.withAlpha = withAlpha;
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
                    requestedQuality,
                    requestedWidth,
                    requestedHeight,
                    requestedLength,
                    isAutoDownSample,
                    isArithmeticCoding,
                    withoutAlpha,
                    withAlpha
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
                            requestedQuality,
                            requestedWidth,
                            requestedHeight,
                            requestedLength,
                            isAutoDownSample,
                            isArithmeticCoding,
                            withoutAlpha,
                            withAlpha
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
                            requestedQuality,
                            requestedWidth,
                            requestedHeight,
                            requestedLength,
                            isAutoDownSample,
                            isArithmeticCoding,
                            withoutAlpha,
                            withAlpha
                    )
            );
        }
    }

}
