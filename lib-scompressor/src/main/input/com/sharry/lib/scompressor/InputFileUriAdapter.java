package com.sharry.lib.scompressor;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.InputStream;

/**
 * Adapter uri 2 input stream.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputFileUriAdapter implements InputAdapter<Uri> {

    @Override
    public InputStream adapt(Context context, String authority, @NonNull InputSource<Uri> inputSource) throws Throwable {
        return context.getContentResolver().openInputStream(inputSource.getSource());
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return Uri.class.isAssignableFrom(adaptedType);
    }

}
