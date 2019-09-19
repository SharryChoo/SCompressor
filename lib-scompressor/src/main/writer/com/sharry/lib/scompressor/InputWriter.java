package com.sharry.lib.scompressor;

import androidx.annotation.NonNull;

/**
 * The writer related on user input data.
 * <p>
 * write input data 2 disk.
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:20
 */
public interface InputWriter<InputType> {

    /**
     * Adapter input data 2 picture path.
     *
     * @param inputSource the compress input.
     * @return file path.
     */
    String writeToDisk(@NonNull DataSource<InputType> inputSource) throws Throwable;

    /**
     * Is can adapter 2 target type.
     *
     * @param adaptedType target adapter type.
     * @return is can adapter 2 target type.
     */
    boolean isWriter(@NonNull Class adaptedType);

}
