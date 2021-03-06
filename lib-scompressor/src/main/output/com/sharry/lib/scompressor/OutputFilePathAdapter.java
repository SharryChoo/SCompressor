package com.sharry.lib.scompressor;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * Adapter compressed file path 2 file path.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 15:10
 */
public class OutputFilePathAdapter implements OutputAdapter<String> {
    @Override
    public String adapt(Context context, String authority, @NonNull File compressedFile) {
        return compressedFile.getAbsolutePath();
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(String.class.getName());
    }
}
