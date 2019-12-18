package com.sharry.lib.scompressor;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.InputStream;

/**
 * Adapter image file path 2 file path.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputFilePathAdapter implements InputAdapter<String> {

    @Override
    public InputStream adapt(Context context, String authority, @NonNull InputSource<String> inputSource) throws Throwable {
        Uri uri = FileUtil.getUriFromFile(context, authority, new File(inputSource.getSource()));
        return context.getContentResolver().openInputStream(uri);
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(String.class.getName());
    }

}
