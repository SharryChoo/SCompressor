package com.sharry.scompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.sharry.libscompressor.CompressCallback;
import com.sharry.libscompressor.SCompressor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Example of a call to a native method
        final ImageView iv = findViewById(R.id.sample_image);
        // 设置可用的目录
        SCompressor.initUsableDirectory(getCacheDir());
        Bitmap origin = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper);
        iv.setImageBitmap(origin);
        // 进行图片压缩
        SCompressor.create()
                .setSrcBitmap(origin)
                .setDesireSize(500, 1000)
                .asBitmap()
                .commit(new CompressCallback<Bitmap>() {
                    @Override
                    public void onCompressComplete(@NonNull Bitmap compressedData) {
                        iv.setImageBitmap(compressedData);
                    }
                });
    }

}
