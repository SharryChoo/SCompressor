package com.sharry.lib.scompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.annotation.NonNull;

import java.io.File;

/**
 * Adapter compressed file path 2 bitmap.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 15:10
 */
public class OutputBitmapAdapter implements OutputAdapter<Bitmap> {

    @Override
    public Bitmap adapt(Context context, String authority, @NonNull File compressedFile) {
        // Convert 2 bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                Bitmap.Config.HARDWARE : Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(compressedFile.getAbsolutePath(), options);
        compressedFile.delete();
        return bitmap;
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(Bitmap.class.getName());
    }
}
