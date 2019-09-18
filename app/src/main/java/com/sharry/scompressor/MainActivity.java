package com.sharry.scompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button mBtnPicker;
    private ImageView mIvSkiaCompressed;
    private ImageView mIvScompressorCompressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SCompressor.init(this);
        initViews();

    }

    private void initViews() {
        mBtnPicker = findViewById(R.id.btn_picker);
        mIvSkiaCompressed = findViewById(R.id.iv_skia_compressed);
        mIvScompressorCompressed = findViewById(R.id.iv_scompressor_compressed);
        mBtnPicker = findViewById(R.id.btn_picker);
        mBtnPicker.setOnClickListener(new View.OnClickListener() {
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
                                doCompress(arrayList.get(0));
                            }
                        });
            }
        });
    }

    private final String usableDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SCompressor";

    private void doCompress(MediaMeta mediaMeta) {
        File dir = new File(usableDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Log.e(TAG, "Origin file length is " + new File(mediaMeta.getPath()).length() / 1024 + "kb");
        Bitmap bitmap = BitmapFactory.decodeFile(mediaMeta.getPath());
        // SCompressor 压缩
        File destFile = new File(dir, "SCompressor_" + System.currentTimeMillis() + ".jpg");
        performCompressBySCompressor(bitmap, destFile);
        bitmap = BitmapFactory.decodeFile(destFile.getAbsolutePath());
        mIvScompressorCompressed.setImageBitmap(bitmap);
        // Skia 压缩
        destFile = new File(dir, "Skia_" + System.currentTimeMillis() + ".jpg");
        performCompressByAndroidSkia(bitmap, destFile);
        bitmap = BitmapFactory.decodeFile(destFile.getAbsolutePath());
        mIvSkiaCompressed.setImageBitmap(bitmap);
    }

    private void performCompressByAndroidSkia(Bitmap bitmap, File file) {
        long startTime = System.currentTimeMillis();
        // Android system.
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            // ignore.
        }
        long endTime = System.currentTimeMillis();
        Log.e(
                TAG,
                "Skia compressed file length is " + (file.length() / 1024) + "kb, " +
                        "cost time is " + (endTime - startTime) + "ms"
        );
    }

    private void performCompressBySCompressor(Bitmap bitmap, File file) {
        long startTime = System.currentTimeMillis();
        Core.nativeCompress(bitmap, 10, file.getAbsolutePath());
        long endTime = System.currentTimeMillis();
        Log.e(
                TAG,
                "SCompressor compressed file length is " + (file.length() / 1024) + "kb, " +
                        "cost time is " + (endTime - startTime) + "ms"
        );
    }

}
