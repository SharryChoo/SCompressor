package com.sharry.lib.scompressor;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Adapter compressed file path 2 byte array.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 15:10
 */
public class OutputByteArrayAdapter implements OutputAdapter<byte[]> {

    @Override
    public byte[] adapt(Context context, String authority, @NonNull File compressedFile) {
        return getBytes(compressedFile);
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(byte[].class.getName());
    }

    private static byte[] getBytes(File compressedFile) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(compressedFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}
