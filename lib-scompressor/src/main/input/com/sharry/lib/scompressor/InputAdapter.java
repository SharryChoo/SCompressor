package com.sharry.lib.scompressor;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;

/**
 * Adapter {@link InputSource} to {@link FileDescriptor}
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:20
 */
public interface InputAdapter<InputType> {

    /**
     * Adapter input data 2 picture path.
     *
     * @param context     Android Application Context
     * @param authority   FileProvider Authority
     * @param inputSource the compress input.
     * @return file path.
     */
    FileDescriptor adapt(Context context, String authority, @NonNull InputSource<InputType> inputSource) throws Throwable;

    /**
     * Is can adapter 2 target type.
     *
     * @param adaptedType target adapter type.
     * @return is can adapter 2 target type.
     */
    boolean isAdapter(@NonNull Class adaptedType);

}
