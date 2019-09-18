package com.sharry.scompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.sharry.lib.album.ILoaderEngine;
import com.sharry.lib.album.MediaMeta;
import com.sharry.lib.album.PickerCallback;
import com.sharry.lib.album.PickerConfig;
import com.sharry.lib.album.PickerManager;
import com.sharry.lib.scompressor.Core;
import com.sharry.lib.scompressor.SCompressor;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView ivSample;
    private Button btnPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SCompressor.init(this);
        initViews();
    }

    private void initViews() {
        ivSample = findViewById(R.id.sample_image);
        btnPicker = findViewById(R.id.btn_picker);
        btnPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickerManager.with(v.getContext())
                        .setPickerConfig(
                                PickerConfig.Builder()
                                        .setThreshold(1)
                                        .build()
                        )
                        .setLoaderEngine(new ILoaderEngine() {
                            @Override
                            public void loadPicture(@NonNull Context context, @NonNull String s, @NonNull ImageView imageView) {
                                Glide.with(context).load(s).into(imageView);
                            }

                            @Override
                            public void loadGif(@NonNull Context context, @NonNull String s, @NonNull ImageView imageView) {

                            }

                            @Override
                            public void loadVideoThumbnails(@NonNull Context context, @NonNull String s, @Nullable String s1, @NonNull ImageView imageView) {

                            }
                        })
                        .start(new PickerCallback() {
                            @Override
                            public void onPickedComplete(@NonNull ArrayList<MediaMeta> arrayList) {
                                doCompress(arrayList.get(0).getPath());
                            }
                        });
            }
        });
    }

    private void doCompress(String url) {
        Bitmap bitmap = BitmapFactory.decodeFile(url);
        Log.e("TAG", "origin file length is " + new File(url).length() / 1024 + "kb");
        long startTime = System.currentTimeMillis();
        File file = new File(getCacheDir(), startTime + ".jpg");
        // libjpeg-turbo .
        Core.nativeCompress(bitmap, 60, file.getAbsolutePath());
        // Android system.
        // bitmap.compress(Bitmap.CompressFormat.JPEG, 60, new FileOutputStream(file));
        long endTime = System.currentTimeMillis();
        Log.e("TAG", "file length is " + (file.length() / 1024) + "kb, cost time is " + (endTime - startTime) + "ms");
        // show img.
        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        ivSample.setImageBitmap(bitmap);
    }

}
