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
    final CompressFormat withoutAlpha;

    /**
     * Control compressed file type.
     */
    final CompressFormat withAlpha;

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
                "inputSource=" + inputSource.getType() + ", \n" +
                "outputType=" + outputType + ", \n" +
                "requestedQuality=" + requestedQuality + ", \n" +
                "requestedWidth=" + requestedWidth + ", \n" +
                "requestedHeight=" + requestedHeight + ", \n" +
                "requestedLength=" + requestedLength + ", \n" +
                "isAutoDownsample=" + isAutoDownsample + ", \n" +
                "isArithmeticCoding=" + isArithmeticCoding + ", \n" +
                "withoutAlpha=" + withoutAlpha.name() + ", \n" +
                "withAlpha=" + withAlpha.name() + ", \n" +
                '}';
    }

    /**
     * The builder associated with Request
     *
     * @param <InputType>
     * @param <OutputType>
     */
    @SuppressWarnings("all")
    public static class Builder<InputType, OutputType> {

        private static final int DEFAULT_QUALITY = 75;
        private static final Class DEFAULT_OUTPUT_TYPE = File.class;
        private static final int INVALIDATE = -1;

        private final InputSource<InputType> inputSource;
        private Class<OutputType> outputType = DEFAULT_OUTPUT_TYPE;
        private int requestedQuality = DEFAULT_QUALITY;
        private int requestedWidth = INVALIDATE;
        private int requestedHeight = INVALIDATE;
        private int requestedLength = INVALIDATE;
        private boolean isAutoDownsample = true;
        private boolean isArithmeticCoding = false;
        private CompressFormat withoutAlpha = CompressFormat.JPEG;
        private CompressFormat withAlpha = CompressFormat.PNG;

        public Builder(@NonNull InputSource<InputType> newInputSource) {
            Preconditions.checkNotNull(newInputSource);
            this.inputSource = newInputSource;
        }

        <NewInputType, NewOutputType> Builder newBuilder(InputSource<NewInputType> newInputSource,
                                                         Class<NewOutputType> newOutputType) {
            Builder result = new Builder(newInputSource);
            result.outputType = newOutputType;
            result.requestedQuality = requestedQuality;
            result.requestedWidth = requestedWidth;
            result.requestedHeight = requestedHeight;
            result.requestedLength = requestedLength;
            result.isAutoDownsample = isAutoDownsample;
            result.isArithmeticCoding = isArithmeticCoding;
            result.withoutAlpha = withoutAlpha;
            result.withAlpha = withAlpha;
            return result;
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
            isAutoDownsample = autoDownSample;
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
         * @param newOutputType desc output data source.
         */
        public <NewOutputType> Builder<InputType, NewOutputType> as(Class<NewOutputType> newOutputType) {
            Preconditions.checkNotNull(newOutputType);
            return newBuilder(inputSource, newOutputType);
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
                    lambdaCallback.onComplete(compressedData);
                }

                @Override
                public void onFailed(@NonNull Throwable e) {
                    Log.e(TAG, e.getMessage(), e);
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
                    new Request<>(
                            inputSource,
                            outputType,
                            requestedQuality,
                            requestedWidth,
                            requestedHeight,
                            requestedLength,
                            isAutoDownsample,
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
                    new Request<>(
                            inputSource,
                            outputType,
                            requestedQuality,
                            requestedWidth,
                            requestedHeight,
                            requestedLength,
                            isAutoDownsample,
                            isArithmeticCoding,
                            withoutAlpha,
                            withAlpha
                    )
            );
        }
    }

}
