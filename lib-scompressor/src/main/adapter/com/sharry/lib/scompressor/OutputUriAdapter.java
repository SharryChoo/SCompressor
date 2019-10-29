package com.sharry.lib.scompressor;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * Adapter compressed file path 2 file path.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 15:10
 */
public class OutputUriAdapter implements OutputAdapter<Uri> {

    @Override
    public Uri adapt(Context context, String authority, @NonNull File compressedFile) {
        return FileUtil.getUriFromFile(context, authority, compressedFile);
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(Uri.class.getName());
    }

}
