package com.sharry.lib.scompressor;

import android.support.annotation.Nullable;

/**
 * 图片压缩任务的回调
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/27 22:14
 */
public interface CompressCallbackLambda<Output> {

    /**
     * Called when compress task complete
     *
     * @param isSuccess      if true is success, false is failed.
     * @param compressedData isSuccess is true it will nonnull, false is null.
     */
    void onCompressComplete(boolean isSuccess, @Nullable Output compressedData);

}
