package com.sharry.lib.scompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

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
    static final List<InputAdapter> INPUT_ADAPTERS = new ArrayList<>();
    static final List<OutputAdapter> OUTPUT_ADAPTERS = new ArrayList<>();

    static Context sContext;
    static String sAuthority;
    static ByteArrayPool sArrayPool;

    /**
     * Init usable Dir, helper generate temp file.
     */
    public static void init(@NonNull Context context, String authority) {
        Preconditions.checkNotNull(context, "Please ensure context non null!");
        Preconditions.checkNotNull(authority, "Please ensure authority non null!");
        sContext = context.getApplicationContext();
        sAuthority = authority;
        sArrayPool = new LruByteArrayPool();
        // add default input adapters.
        addInputAdapter(new InputFilePathAdapter());
        addInputAdapter(new InputUriAdapter());
        // add default output adapters.
        addOutputAdapter(new OutputBitmapAdapter());
        addOutputAdapter(new OutputFilePathAdapter());
        addOutputAdapter(new OutputByteArrayAdapter());
        addOutputAdapter(new OutputUriAdapter());
        addOutputAdapter(new OutputFileAdapter());
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

    public static void replaceArrayPool(ByteArrayPool arrayPool) {
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
    public static <InputType> Request.Builder<InputType, File> with(final InputType inputSource) {
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
            if (BuildConfig.DEBUG) {
                throwable.printStackTrace();
            }
        }
        return output;
    }

}
