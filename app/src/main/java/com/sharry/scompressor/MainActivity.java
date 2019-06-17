package com.sharry.scompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sharry.lib.scompressor.CompressCallbackLambda;
import com.sharry.lib.scompressor.SCompressor;
import com.sharry.picturepicker.facade.IPictureLoaderEngine;
import com.sharry.picturepicker.facade.PickerCallback;
import com.sharry.picturepicker.facade.PickerConfig;
import com.sharry.picturepicker.facade.PicturePickerManager;

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
                PicturePickerManager.with(v.getContext())
                        .setPickerConfig(
                                PickerConfig.Builder()
                                        .setThreshold(1)
                                        .build()
                        )
                        .setPictureLoader(new IPictureLoaderEngine() {
                            @Override
                            public void load(Context context, String uri, ImageView imageView) {
                                Glide.with(context).load(uri).into(imageView);
                            }
                        })
                        .start(new PickerCallback() {
                            @Override
                            public void onPickedComplete(ArrayList<String> userPickedSet) {
                                doCompress(userPickedSet.get(0));
                            }
                        });
            }
        });
    }

    private void doCompress(String url) {
        // 进行图片压缩
        SCompressor.create()
                .setInputPath(url)
                .setQuality(70)
                .setAutoDownSample(true)
                .setDesireSize(500, 1000)
                .asBitmap()
                .asyncCall(new CompressCallbackLambda<Bitmap>() {
                    @Override
                    public void onCompressComplete(boolean isSuccess, @Nullable Bitmap compressedData) {
                        if (isSuccess) {
                            ivSample.setImageBitmap(compressedData);
                        }
                    }
                });
    }

}
