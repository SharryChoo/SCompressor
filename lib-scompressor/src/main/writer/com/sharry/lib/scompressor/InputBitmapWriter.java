package com.sharry.lib.scompressor;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Adapter bitmap 2 temp file.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputBitmapWriter implements InputWriter<Bitmap> {

    @Override
    public String writeToDisk(@NonNull DataSource<Bitmap> inputSource) throws Throwable {
        // write bitmap 2 temp file.
        File tempFile = Core.createUnsuspectedFile();
        FileOutputStream fos = new FileOutputStream(tempFile);
        Bitmap bitmap = inputSource.getSource();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } else {
            throw new NullPointerException("Please ensure input source not null!");
        }
        return tempFile.getAbsolutePath();
    }

    @Override
    public boolean isWriter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(Bitmap.class.getName());
    }

}
