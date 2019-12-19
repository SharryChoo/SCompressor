package com.sharry.lib.scompressor;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * The adapter related on user input data.
 * <p>
 * Adapter compressed file path 2 user desire output type.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:20
 */
public interface OutputAdapter<OutputType> {

    /**
     * Adapter compressed file path 2 output type.
     *
     * @param context        Android Application Context
     * @param authority      FileProvider Authority
     * @param compressedFile the compressed file path.
     * @return user desire type.
     */
    OutputType adapt(Context context, String authority, @NonNull File compressedFile);

    /**
     * Is can adapter 2 target type.
     *
     * @param adaptedType target adapter type.
     * @return is can adapter 2 target type.
     */
    boolean isAdapter(@NonNull Class adaptedType);

}
