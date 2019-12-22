package com.sharry.scompressor.gif;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.sharry.lib.gif.FrameSequenceDrawable;

/**
 * 创建一个用于加载 GIF 的 Glide 的 Resource
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-12-22
 */
public class GifDrawableResource extends DrawableResource<FrameSequenceDrawable> {

    public GifDrawableResource(FrameSequenceDrawable drawable) {
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
