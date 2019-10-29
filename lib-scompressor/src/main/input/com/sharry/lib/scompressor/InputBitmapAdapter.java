package com.sharry.lib.scompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

/**
 * Adapter bitmap 2 temp file.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputBitmapAdapter implements InputAdapter<Bitmap> {

    @Override
    public FileDescriptor adapt(Context context, String authority, @NonNull InputSource<Bitmap> inputSource) throws Throwable {
        // Create temp uri
        File internalFile = FileUtil.createTempFile(context);
        Uri tempUri = FileUtil.getUriFromFile(context, authority, internalFile);
        // 将不支持的 Bitmap 先压缩到文件中
        FileDescriptor writeFd = context.getContentResolver().openFileDescriptor(tempUri, "w").getFileDescriptor();
        FileOutputStream fos = new FileOutputStream(writeFd);
        Bitmap bitmap = inputSource.getSource();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } else {
            throw new NullPointerException("Please ensure input source not null!");
        }
        // 返回读文件描述符
        return context.getContentResolver().openFileDescriptor(tempUri, "r").getFileDescriptor();
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(Bitmap.class.getName());
    }

}
