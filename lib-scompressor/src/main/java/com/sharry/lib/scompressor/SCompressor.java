package com.sharry.lib.scompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The facade of picture compressor.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/27 22:14
 */
public class SCompressor {

    static final String TAG = SCompressor.class.getSimpleName();
    static final List<InputAdapter> INPUT_ADAPTERS = new ArrayList<>();
    static final List<OutputAdapter> OUTPUT_ADAPTERS = new ArrayList<>();

    static {
        // add default input adapters.
        INPUT_ADAPTERS.add(new InputFilePathAdapter());
        INPUT_ADAPTERS.add(new InputBitmapAdapter());
        // add default output adapters.
        OUTPUT_ADAPTERS.add(new OutputBitmapAdapter());
        OUTPUT_ADAPTERS.add(new OutputFilePathAdapter());
        OUTPUT_ADAPTERS.add(new OutputByteArrayAdapter());
    }

    static File usableDir;

    /**
     * Init usable Dir, helper generate temp file.
     */
    public static void init(@NonNull Context context) {
        Preconditions.checkNotNull(context, "Please ensure context non null!");
        usableDir = context.getCacheDir();
    }

    /**
     * Add u custom input source adapter from here.
     */
    public static void addInputAdapter(@NonNull InputAdapter adapter) {
        Preconditions.checkNotNull(adapter);
        INPUT_ADAPTERS.add(adapter);
    }

    /**
     * Add u custom output source adapter from here.
     */
    public static void addOutputAdapter(@NonNull OutputAdapter adapter) {
        Preconditions.checkNotNull(adapter);
        OUTPUT_ADAPTERS.add(adapter);
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
