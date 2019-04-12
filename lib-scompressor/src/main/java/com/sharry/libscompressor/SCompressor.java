package com.sharry.libscompressor;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * The facade of picture compressor.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/27 22:14
 */
public class SCompressor {

    static File mUsableDir;

    /**
     * Init usable Dir, helper generate temp file.
     */
    public static void initUsableDirectory(File cacheDir) {
        mUsableDir = cacheDir;
    }

    /**
     * Get an instance of Request.Builder
     */
    @NonNull
    public static Request.Builder<Bitmap, String> create() {
        return new Request.Builder<>();
    }

    /**
     * Execute async task.
     */
    static <InputType, OutputType> void asyncCall(@NonNull Request<InputType, OutputType> ops) {
        Preconditions.checkNotNull(ops);
        Preconditions.checkNotNull(ops.inputSource);
        Preconditions.checkNotNull(ops.callback);
        CompressDispatcher.asyncDispatcher(ops);
    }

    /**
     * Execute sync task.
     *
     * @return an instance of target output data.
     */
    @Nullable
    static <InputType, OutputType> OutputType syncCall(@NonNull Request<InputType, OutputType> ops) {
        Preconditions.checkNotNull(ops);
        Preconditions.checkNotNull(ops.inputSource);
        return CompressDispatcher.syncDispatcher(ops);
    }
}
