package com.sharry.lib.scompressor;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
public final class SCompressor {

    static final String TAG = SCompressor.class.getSimpleName();
    static final List<InputWriter> INPUT_WRITERS = new ArrayList<>();
    static final List<OutputAdapter> OUTPUT_ADAPTERS = new ArrayList<>();

    static {
        // add default input adapters.
        INPUT_WRITERS.add(new InputFilePathWriter());
        INPUT_WRITERS.add(new InputBitmapWriter());
        // add default output adapters.
        OUTPUT_ADAPTERS.add(new OutputBitmapAdapter());
        OUTPUT_ADAPTERS.add(new OutputFilePathAdapter());
        OUTPUT_ADAPTERS.add(new OutputByteArrayAdapter());
    }

    static File sUsableDir;

    /**
     * Init usable Dir, helper generate temp file.
     */
    public static void init(@NonNull Context context) {
        Preconditions.checkNotNull(context, "Please ensure context non null!");
        sUsableDir = context.getCacheDir();
    }

    /**
     * Add u custom input source adapter from here.
     */
    public static void addInputAdapter(@NonNull InputWriter adapter) {
        Preconditions.checkNotNull(adapter);
        INPUT_WRITERS.add(adapter);
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
    static <InputType, OutputType> void asyncCall(Request<InputType, OutputType> request,
                                                  ICompressorCallback<OutputType> callback) {
        Preconditions.checkNotNull(callback, "Please ensure Request.callback non null!");
        AsyncCaller.execute(request, callback);
    }

    /**
     * Execute sync task.
     *
     * @return an instance of target output data.
     */
    @Nullable
    static <InputType, OutputType> OutputType syncCall(Request<InputType, OutputType> request) {
        Preconditions.checkNotNull(request.inputSource, "Please ensure Request.inputSource non null!");
        OutputType output = null;
        try {
            output = SyncCaller.execute(request);
        } catch (Throwable throwable) {
            // ignore.
        }
        return output;
    }

}
