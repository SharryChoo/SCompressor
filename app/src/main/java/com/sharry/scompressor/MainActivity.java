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
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.sharry.lib.album.ILoaderEngine;
import com.sharry.lib.album.MediaMeta;
import com.sharry.lib.album.PickerCallback;
import com.sharry.lib.album.PickerConfig;
import com.sharry.lib.album.PickerManager;
import com.sharry.lib.scompressor.CompressFormat;
import com.sharry.lib.scompressor.ICompressorCallbackLambda;
import com.sharry.lib.scompressor.SCompressor;

import java.io.File;
import java.util.ArrayList;

/**
 * 测试页
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-20 13:37
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = SCompressor.class.getSimpleName();

    private Button mBtnPicker;
    private ImageView mIvCompressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mBtnPicker = findViewById(R.id.btn_picker);
        mIvCompressed = findViewById(R.id.iv_compressed);
        mBtnPicker = findViewById(R.id.btn_picker);
        mBtnPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickerManager.with(v.getContext())
                        .setPickerConfig(PickerConfig.Builder()
                                .setThreshold(1)
                                .isPickGif(true)
                                .build()
                        )
                        .setLoaderEngine(new ILoaderEngine() {
                            @Override
                            public void loadPicture(@NonNull Context context, @NonNull MediaMeta mediaMeta, @NonNull ImageView imageView) {
                                Glide.with(context).asBitmap().load(mediaMeta.getContentUri()).into(imageView);
                            }

                            @Override
                            public void loadGif(@NonNull Context context, @NonNull MediaMeta mediaMeta, @NonNull ImageView imageView) {
                                Glide.with(context).asGif().load(mediaMeta.getContentUri()).into(imageView);
                            }

                            @Override
                            public void loadVideoThumbnails(@NonNull Context context, @NonNull MediaMeta mediaMeta, @NonNull ImageView imageView) {
                                Glide.with(context).asBitmap().load(mediaMeta.getContentUri()).into(imageView);
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

    private void doCompress(MediaMeta mediaMeta) {
        // SCompressor 压缩
        final long startTime = System.currentTimeMillis();
        SCompressor.with(mediaMeta.getPath())
                // 使用自动降采样
                .setAutoDownsample(true)
                // 使用算术编码
                .setArithmeticCoding(true)
                // 压缩后期望的文件大小, 单位 byte
                .setDesireLength(500 * 1024)
                // 压缩质量
                .setQuality(50)
                // 设置压缩后文件输出类型
                .setCompressFormat(
                        // 非透明通道文件输出
                        CompressFormat.JPEG,
                        // 透明通道文件输出类型
                        CompressFormat.WEBP
                )
                // 转为目标类型
                .asFile()
                // 异步调用
                .asyncCall(new ICompressorCallbackLambda<File>() {
                    @Override
                    public void onComplete(@NonNull File compressedData) {
                        long endTime = System.currentTimeMillis();
                        Log.e(TAG, "cost time is " + (endTime - startTime) + "ms");
                        Bitmap compressedBitmap = BitmapFactory.decodeFile(compressedData.getAbsolutePath());
                        mIvCompressed.setImageBitmap(compressedBitmap);
                    }
                });
    }

}
