package com.sharry.scompressor.gif;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.sharry.lib.gif.FrameSequenceDrawable;

import java.io.InputStream;

@GlideModule
public class GlideGifModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // 注册一个将 GIF 的 InputStream 转为  FrameSequenceDrawable 的 ResourceDecoder
        registry.prepend(
                Registry.BUCKET_GIF,
                InputStream.class, FrameSequenceDrawable.class,
                new GifResourceDecoder(glide.getBitmapPool())
        );
    }

}
