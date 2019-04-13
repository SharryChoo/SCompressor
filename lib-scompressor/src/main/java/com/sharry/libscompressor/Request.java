package com.sharry.libscompressor;

import android.graphics.Bitmap;
import android.support.annotation.Dimension;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.support.annotation.Dimension.PX;


/**
 * The options associated with Picture Compress.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/29 13:03
 */
public class Request<InputType, OutputType> {

    /**
     * Input source associated with this compress task.
     */
    final DataSource<InputType> inputSource;

    /**
     * Compress output source associated with this compress task.
     */
    final DataSource<OutputType> outputSource;

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
     * CompressCallback
     */
    final CompressCallback<OutputType> callback;

    private Request(
            DataSource<InputType> inputSource,
            DataSource<OutputType> outputSource,
            int quality,
            int destWidth,
            int destHeight,
            CompressCallback<OutputType> callback) {
        this.inputSource = inputSource;
        this.outputSource = outputSource;
        this.quality = quality;
        this.destWidth = destWidth;
        this.destHeight = destHeight;
        this.callback = callback;
    }

    @Override
    public String toString() {
        return "Request{" +
                "inputSourceType = " + inputSource.getType().getSimpleName() +
                ", outputSourceType = " + outputSource.getType().getSimpleName() +
                ", quality = " + quality +
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

        private static final int DEFAULT_QUALITY = 90;
        private static final int INVALIDATE = -1;

        private DataSource inputSource;
        private DataSource outputSource;
        private int quality = DEFAULT_QUALITY;
        private int desireWidth = INVALIDATE;
        private int desireHeight = INVALIDATE;

        Builder() {
        }

        private Builder(DataSource inputSource,
                        DataSource outputSource,
                        int quality,
                        int desireWidth,
                        int desireHeight) {
            this.inputSource = inputSource;
            this.outputSource = outputSource;
            this.quality = quality;
            this.desireWidth = desireWidth;
            this.desireHeight = desireHeight;
        }

        /**
         * Set source image file path associated with this compress task.
         */
        public Builder<String, OutputType> setSrcPath(@NonNull final String srcPath) {
            Preconditions.checkNotNull(srcPath);
            this.inputSource = new DataSource<String>() {
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
            };
            return asInput(String.class);
        }

        /**
         * Set source image bitmap associated with this compress task.
         */
        public Builder<Bitmap, OutputType> setSrcBitmap(@NonNull final Bitmap srcBitmap) {
            Preconditions.checkNotNull(srcBitmap);
            this.inputSource = new DataSource<Bitmap>() {
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
            };
            return asInput(Bitmap.class);
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
         * Set desire output quality when compressing start.
         *
         * @param quality range [0, 100]
         */
        public Builder<InputType, OutputType> setQuality(@IntRange(from = 0, to = 100) int quality) {
            this.quality = quality;
            return this;
        }

        /**
         * Set dest output file path associated with this compress request.
         * <p>
         * Output type will changed to String
         *
         * @param outputPath absolute file path.
         */
        public Builder<InputType, String> setOutputPath(@NonNull final String outputPath) {
            Preconditions.checkNotEmpty(outputPath);
            return asString(outputPath);
        }

        /**
         * Convert output type to Bitmap
         */
        public Builder<InputType, Bitmap> asBitmap() {
            this.outputSource = new DataSource<Bitmap>() {
                @NonNull
                @Override
                public Class<Bitmap> getType() {
                    return Bitmap.class;
                }

                @Nullable
                @Override
                public Bitmap getSource() {
                    return null;
                }
            };
            return asOutput(Bitmap.class);
        }

        /**
         * Convert output type to byte array.
         */
        public Builder<InputType, byte[]> asByteArray() {
            this.outputSource = new DataSource<byte[]>() {
                @NonNull
                @Override
                public Class<byte[]> getType() {
                    return byte[].class;
                }

                @Nullable
                @Override
                public byte[] getSource() {
                    return null;
                }
            };
            return asOutput(byte[].class);
        }

        /**
         * Execute async task.
         *
         * @param callback the callback when compress complete.
         */
        public void commit(@NonNull CompressCallback<OutputType> callback) {
            Preconditions.checkNotNull(callback);
            SCompressor.asyncCall(
                    new Request<InputType, OutputType>(
                            inputSource,
                            outputSource,
                            quality,
                            desireWidth,
                            desireHeight,
                            callback
                    )
            );
        }

        /**
         * Execute sync task.
         */
        @Nullable
        public OutputType execute() {
            return SCompressor.syncCall(
                    new Request<InputType, OutputType>(
                            inputSource,
                            outputSource,
                            quality,
                            desireWidth,
                            desireHeight,
                            null
                    )
            );
        }

        /**
         * Convert output type to string
         */
        private Builder<InputType, String> asString(final String destPath) {
            this.outputSource = new DataSource<String>() {

                @NonNull
                @Override
                public Class<String> getType() {
                    return String.class;
                }

                @Override
                public String getSource() {
                    return destPath;
                }

            };
            return asOutput(String.class);
        }

        /**
         * Convert Builder to target input type.
         *
         * @param inputClass target input type.
         */
        private <NewInputType> Builder<NewInputType, OutputType> asInput(Class<NewInputType> inputClass) {
            Log.v(Builder.class.getSimpleName(), "Builder input type convert to " + inputClass);
            return new Builder<>(
                    inputSource,
                    outputSource,
                    quality,
                    desireWidth,
                    desireHeight
            );
        }

        /**
         * Convert Builder to target output type.
         *
         * @param outputClass target output type.
         */
        private <NewOutputType> Builder<InputType, NewOutputType> asOutput(Class<NewOutputType> outputClass) {
            Log.v(Builder.class.getSimpleName(), "Builder output type convert to " + outputClass);
            return new Builder<>(
                    inputSource,
                    outputSource,
                    quality,
                    desireWidth,
                    desireHeight
            );
        }
    }

}
