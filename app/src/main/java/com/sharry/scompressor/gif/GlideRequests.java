package com.sharry.scompressor.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.manager.Lifecycle;
import com.bumptech.glide.manager.RequestManagerTreeNode;
import com.bumptech.glide.request.RequestOptions;
import com.sharry.lib.gif.FrameSequenceDrawable;

import java.io.File;
import java.net.URL;

/**
 * Includes all additions from methods in {@link com.bumptech.glide.annotation.GlideExtension}s
 * annotated with {@link com.bumptech.glide.annotation.GlideType}
 *
 * <p>Generated code, do not modify
 */
@SuppressWarnings("deprecation")
public class GlideRequests extends RequestManager {
    public GlideRequests(@NonNull Glide glide, @NonNull Lifecycle lifecycle,
                         @NonNull RequestManagerTreeNode treeNode, @NonNull Context context) {
        super(glide, lifecycle, treeNode, context);
    }

    @Override
    @CheckResult
    @NonNull
    public <ResourceType> GlideRequest<ResourceType> as(@NonNull Class<ResourceType> resourceClass) {
        return new GlideRequest<>(glide, this, resourceClass, context);
    }

    /**
     * @see GifExtensions#asGif2(RequestBuilder)
     */
    @NonNull
    @CheckResult
    public GlideRequest<FrameSequenceDrawable> asGif2() {
        GlideRequest<FrameSequenceDrawable> requestBuilder = this.as(FrameSequenceDrawable.class);
        GifExtensions.asGif2(requestBuilder);
        return requestBuilder;
    }

    @Override
    @NonNull
    public GlideRequests applyDefaultRequestOptions(@androidx.annotation.NonNull RequestOptions requestOptions) {
        return (GlideRequests) super.applyDefaultRequestOptions(requestOptions);
    }

    @Override
    @NonNull
    public GlideRequests setDefaultRequestOptions(@androidx.annotation.NonNull RequestOptions requestOptions) {
        return (GlideRequests) super.setDefaultRequestOptions(requestOptions);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Bitmap> asBitmap() {
        return (GlideRequest<Bitmap>) super.asBitmap();
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<GifDrawable> asGif() {
        return (GlideRequest<GifDrawable>) super.asGif();
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> asDrawable() {
        return (GlideRequest<Drawable>) super.asDrawable();
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> load(@Nullable Bitmap bitmap) {
        return (GlideRequest<Drawable>) super.load(bitmap);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> load(@Nullable Drawable drawable) {
        return (GlideRequest<Drawable>) super.load(drawable);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> load(@Nullable String string) {
        return (GlideRequest<Drawable>) super.load(string);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> load(@Nullable Uri uri) {
        return (GlideRequest<Drawable>) super.load(uri);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> load(@Nullable File file) {
        return (GlideRequest<Drawable>) super.load(file);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> load(@Nullable Integer resourceId) {
        return (GlideRequest<Drawable>) super.load(resourceId);
    }

    @Override
    @Deprecated
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> load(@Nullable URL url) {
        return (GlideRequest<Drawable>) super.load(url);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> load(@Nullable byte[] model) {
        return (GlideRequest<Drawable>) super.load(model);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<Drawable> load(@Nullable Object model) {
        return (GlideRequest<Drawable>) super.load(model);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<File> downloadOnly() {
        return (GlideRequest<File>) super.downloadOnly();
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<File> download(@Nullable Object model) {
        return (GlideRequest<File>) super.download(model);
    }

    @Override
    @androidx.annotation.NonNull
    @androidx.annotation.CheckResult
    public GlideRequest<File> asFile() {
        return (GlideRequest<File>) super.asFile();
    }

    @Override
    protected void setRequestOptions(@NonNull RequestOptions toSet) {
        if (toSet instanceof com.sharry.scompressor.gif.GlideOptions) {
            super.setRequestOptions(toSet);
        } else {
            super.setRequestOptions(new com.sharry.scompressor.gif.GlideOptions().apply(toSet));
        }
    }
}
