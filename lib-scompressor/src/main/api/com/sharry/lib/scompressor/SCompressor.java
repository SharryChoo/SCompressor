package com.sharry.lib.scompressor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sharry.lib.BuildConfig;
import com.sharry.lib.scompressor.recycleable.ByteArrayPool;
import com.sharry.lib.scompressor.recycleable.LruByteArrayPool;

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

    @SuppressLint("StaticFieldLeak")
    static Context sContext;
    static String sAuthority;
    static ByteArrayPool sArrayPool;
    static List<InputAdapter> sInputAdapters;
    static List<OutputAdapter> sOutputAdapters;
    static boolean sIsDebug = BuildConfig.DEBUG;
    private static boolean sHasInit = false;

    /**
     * Init usable Dir, helper generate temp file.
     */
    public static synchronized void init(@NonNull Context context, String authority) {
        Preconditions.checkNotNull(context, "Please ensure context non null!");
        Preconditions.checkNotNull(authority, "Please ensure authority non null!");
        sContext = context.getApplicationContext();
        sAuthority = authority;
        sArrayPool = new LruByteArrayPool();
        // add default input adapters.
        sInputAdapters = new ArrayList<>();
        sInputAdapters.add(new InputFilePathAdapter());
        sInputAdapters.add(new InputFileUriAdapter());
        // add default output adapters.
        sOutputAdapters = new ArrayList<>();
        sOutputAdapters.add(new OutputBitmapAdapter());
        sOutputAdapters.add(new OutputFilePathAdapter());
        sOutputAdapters.add(new OutputByteArrayAdapter());
        sOutputAdapters.add(new OutputUriAdapter());
        sOutputAdapters.add(new OutputFileAdapter());
        // init completed.
        sHasInit = true;
    }

    public static void setDebug(boolean isDebug) {
        sIsDebug = isDebug;
    }

    /**
     * Add u custom input source adapter from here.
     */
    public static void addInputAdapter(@NonNull InputAdapter adapter) {
        if (!sHasInit) {
            throw new IllegalStateException("Please init first");
        }
        Preconditions.checkNotNull(adapter);
        sInputAdapters.add(adapter);
    }

    /**
     * Add u custom output source adapter from here.
     */
    public static void addOutputAdapter(@NonNull OutputAdapter adapter) {
        if (!sHasInit) {
            throw new IllegalStateException("Please init first");
        }
        Preconditions.checkNotNull(adapter);
        sOutputAdapters.add(adapter);
    }

    public static void replaceArrayPool(ByteArrayPool arrayPool) {
        if (!sHasInit) {
            throw new IllegalStateException("Please init first");
        }
        sArrayPool = arrayPool;
    }

    /**
     * Get an instance of Request.Builder
     * Set u custom input source.
     *
     * @param inputSource desc input source. Current support
     *                    Origin Bitmap {@link Bitmap},
     *                    File Path {@link String},
     *                    File Uri {@link Uri}
     */
    @NonNull
    public static <InputType> Request.Builder<InputType, File> with(@NonNull final InputType inputSource) {
        if (!sHasInit) {
            throw new IllegalStateException("Please init first");
        }
        Preconditions.checkNotNull(inputSource);
        return new Request.Builder<>(new InputSource<InputType>() {
            @NonNull
            @Override
            public Class<InputType> getType() {
                return (Class<InputType>) inputSource.getClass();
            }

            @NonNull
            @Override
            public InputType getSource() {
                return inputSource;
            }
        });
    }

    /**
     * Execute async task.
     */
    static <InputType, OutputType> void asyncCall(Request<InputType, OutputType> request,
                                                  ICompressorCallback<OutputType> callback) {
        if (!sHasInit) {
            throw new IllegalStateException("Please init first");
        }
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
        if (!sHasInit) {
            throw new IllegalStateException("Please init first");
        }
        Preconditions.checkNotNull(request.inputSource, "Please ensure Request.inputSource non null!");
        OutputType output = null;
        try {
            output = SyncCaller.execute(request);
        } catch (Throwable throwable) {
            if (sIsDebug) {
                Log.e(TAG, throwable.getMessage(), throwable);
            }
        }
        return output;
    }

}
