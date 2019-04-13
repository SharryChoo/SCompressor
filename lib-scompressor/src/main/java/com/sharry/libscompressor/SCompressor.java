package com.sharry.libscompressor;

import android.content.Context;
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
    public static void init(@NonNull Context context) {
        Preconditions.checkNotNull(context, "Please ensure context non null!");
        mUsableDir = context.getCacheDir();
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
    static <InputType, OutputType> void asyncCall(Request<InputType, OutputType> request) {
        Preconditions.checkNotNull(request.callback, "Please ensure Request.callback non null!");
        CompressDispatcher.asyncDispatcher(request);
    }

    /**
     * Execute sync task.
     *
     * @return an instance of target output data.
     */
    @Nullable
    static <InputType, OutputType> OutputType syncCall(Request<InputType, OutputType> request) {
        Preconditions.checkNotNull(request.inputSource, "Please ensure Request.inputSource non null!");
        return CompressDispatcher.syncDispatcher(request);
    }
}
