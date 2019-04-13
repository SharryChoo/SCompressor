package com.sharry.scompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sharry.libscompressor.CompressCallback;
import com.sharry.libscompressor.SCompressor;
import com.sharry.picturepicker.picker.manager.PickerCallback;
import com.sharry.picturepicker.picker.manager.PickerConfig;
import com.sharry.picturepicker.picker.manager.PicturePickerManager;
import com.sharry.picturepicker.support.loader.IPictureLoader;

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
                        .setPictureLoader(new IPictureLoader() {
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
                .setSrcPath(url)
                .setQuality(70)
                .asBitmap()
                .commit(new CompressCallback<Bitmap>() {
                    @Override
                    public void onCompressComplete(@NonNull Bitmap compressedData) {
                        ivSample.setImageBitmap(compressedData);
                    }
                });
    }

}
