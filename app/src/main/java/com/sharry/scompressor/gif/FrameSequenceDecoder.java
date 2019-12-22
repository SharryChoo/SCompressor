package com.sharry.scompressor.gif;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.sharry.lib.gif.FrameSequenceDrawable;
import com.sharry.lib.gif.GifDecoder;

import java.io.IOException;
import java.io.InputStream;

public class FrameSequenceDecoder implements ResourceDecoder<InputStream, FrameSequenceDrawable> {

    private BitmapPool bitmapPool;

    public FrameSequenceDecoder(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public FrameSequenceDrawableResource decode(@NonNull InputStream source, int width, int height, @NonNull Options options) throws IOException {
        GifDecoder fs = GifDecoder.decodeStream(source);
        FrameSequenceDrawable drawable = new FrameSequenceDrawable(fs, mProvider);
        return new FrameSequenceDrawableResource(drawable);
    }

    // This provider is entirely unnecessary, just here to validate the acquire/release process
    private class CheckingProvider implements FrameSequenceDrawable.BitmapProvider {

        @Override
        public Bitmap acquireBitmap(int minWidth, int minHeight) {
            return bitmapPool.getDirty(minWidth, minHeight, Bitmap.Config.ARGB_8888);
        }

        @Override
        public void releaseBitmap(Bitmap bitmap) {
            bitmapPool.put(bitmap);
        }
    }

    private CheckingProvider mProvider = new CheckingProvider();

}
