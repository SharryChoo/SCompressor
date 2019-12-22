package com.sharry.scompressor.gif;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.sharry.lib.gif.FrameSequenceDrawable;
import com.sharry.lib.gif.GifDecoder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Define a GIF Decoder.
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-12-22
 */
public class GifResourceDecoder implements ResourceDecoder<InputStream, FrameSequenceDrawable> {

    private final FrameSequenceDrawable.BitmapProvider mProvider;

    GifResourceDecoder(final BitmapPool bitmapPool) {
        this.mProvider = new FrameSequenceDrawable.BitmapProvider() {
            @Override
            public Bitmap acquireBitmap(int minWidth, int minHeight) {
                return bitmapPool.getDirty(minWidth, minHeight, Bitmap.Config.ARGB_8888);
            }

            @Override
            public void releaseBitmap(Bitmap bitmap) {
                bitmapPool.put(bitmap);
            }
        };
    }

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) throws IOException {
        return true;
    }

    /**
     * 将 GIF 的 InputStream 转为 GifDrawableResource
     */
    @Override
    public GifDrawableResource decode(@NonNull InputStream source, int width, int height, @NonNull Options options) throws IOException {
        GifDecoder decoder = GifDecoder.decodeStream(source);
        if (decoder == null) {
            return null;
        }
        FrameSequenceDrawable drawable = new FrameSequenceDrawable(decoder, mProvider);
//        drawable.setLoopCount(Integer.MAX_VALUE);
        return new GifDrawableResource(drawable);
    }

    /**
     * 创建一个用于加载 GIF 的 Glide 的 Resource
     *
     * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
     * @version 1.0
     * @since 2019-12-22
     */
    private static class GifDrawableResource extends DrawableResource<FrameSequenceDrawable> {

        private GifDrawableResource(FrameSequenceDrawable drawable) {
            super(drawable);
        }

        @NonNull
        @Override
        public Class<FrameSequenceDrawable> getResourceClass() {
            return FrameSequenceDrawable.class;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void recycle() {
            drawable.stop();
            drawable.destroy();
        }
    }

}
