package com.sharry.lib.scompressor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.InputStream;

import static com.sharry.lib.scompressor.SCompressor.TAG;
import static com.sharry.lib.scompressor.SCompressor.sIsDebug;

/**
 * Adapter image file path 2 input stream.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputFilePathAdapter implements InputAdapter<String> {

    @Override
    public InputStream adapt(Context context, String authority, @NonNull InputSource<String> inputSource) throws Throwable {
        File file = new File(inputSource.getSource());
        if (sIsDebug) {
            Log.e(TAG, "Origin file length is " + file.length() / 1024 + "kb");
        }
        Uri uri = FileUtil.getUriFromFile(context, authority, file);
        return context.getContentResolver().openInputStream(uri);
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(String.class.getName());
    }

}
