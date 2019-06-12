package com.sharry.lib.scompressor;

import android.support.annotation.NonNull;

/**
 * The adapter related on user input data.
 * <p>
 * Adapter input data 2 picture path.
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:20
 */
public interface InputAdapter<InputType> {

    /**
     * Adapter input data 2 picture path.
     *
     * @param request   the compress request.
     * @param inputData the input data.
     * @return pre quality compress quality compress.
     */
    String adapt(@NonNull Request request, @NonNull InputType inputData) throws Throwable;

    /**
     * Is can adapter 2 target type.
     *
     * @param adaptedType target adapter type.
     * @return is can adapter 2 target type.
     */
    boolean isAdapter(@NonNull Class adaptedType);

}
