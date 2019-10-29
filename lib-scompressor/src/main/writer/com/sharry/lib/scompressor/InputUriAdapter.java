package com.sharry.lib.scompressor;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;

/**
 * Adapter bitmap 2 temp file.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputUriAdapter implements FileDescriptorAdapter<Uri> {

    @Override
    public FileDescriptor adapt(Context context, String authority, @NonNull InputSource<Uri> inputSource) throws Throwable {
        // write bitmap 2 temp file.
        return context.getContentResolver().openFileDescriptor(inputSource.getSource(), "r").getFileDescriptor();
    }

    @Override
    public boolean isWriter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(Uri.class.getName());
    }

}
