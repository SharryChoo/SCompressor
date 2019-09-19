package com.sharry.lib.scompressor;

import androidx.annotation.NonNull;

/**
 * 图片压缩任务的回调
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/27 22:14
 */
public interface ICompressorCallback<Output> {

    /**
     * Called when compress task complete
     *
     * @param compressedData result is true it will nonnull, false is null.
     */
    void onSuccess(@NonNull Output compressedData);

    /**
     * Called when compress failed
     */
    void onFailed(@NonNull Throwable e);
}
