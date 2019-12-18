package com.sharry.lib.scompressor;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Adapter bitmap 2 temp file.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputUriAdapter implements InputAdapter<Uri> {

    @Override
    public InputStream adapt(Context context, String authority, @NonNull InputSource<Uri> inputSource) throws Throwable {
        // write bitmap 2 temp file.
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(inputSource.getSource(), "r");
        if (pfd == null) {
            return null;
        }
        FileDescriptor fd = pfd.getFileDescriptor();
        if (fd == null) {
            return null;
        }
        return new FileInputStream(fd);
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return Uri.class.isAssignableFrom(adaptedType);
    }

}
