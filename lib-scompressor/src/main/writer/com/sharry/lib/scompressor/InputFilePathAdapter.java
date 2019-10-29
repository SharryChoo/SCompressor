package com.sharry.lib.scompressor;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileDescriptor;

/**
 * Adapter image file path 2 file path.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputFilePathAdapter implements FileDescriptorAdapter<String> {

    @Override
    public FileDescriptor adapt(Context context, String authority, @NonNull InputSource<String> inputSource) throws Throwable {
        Uri uri = FileUtil.getUriFromFile(context, authority, new File(inputSource.getSource()));
        // 返回读文件描述符
        return context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();
    }

    @Override
    public boolean isWriter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(String.class.getName());
    }

}
