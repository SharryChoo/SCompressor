package com.sharry.lib.scompressor;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Adapter bitmap 2 temp file.
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputBitmapAdapter implements InputAdapter<Bitmap> {

    @Override
    public String adapt(@NonNull Request request, @NonNull Bitmap inputData) throws Throwable {
        // write bitmap 2 temp file.
        File tempFile = Core.createUnsuspectedFile();
        FileOutputStream fos = new FileOutputStream(tempFile);
        inputData.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        return tempFile.getAbsolutePath();
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(Bitmap.class.getName());
    }

}
