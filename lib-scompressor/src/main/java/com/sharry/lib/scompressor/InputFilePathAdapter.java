package com.sharry.lib.scompressor;

import android.support.annotation.NonNull;

/**
 * Adapter image file path 2 file path.
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputFilePathAdapter implements InputAdapter<String> {

    @Override
    public String adapt(@NonNull Request request, @NonNull String inputData) {
        return inputData;
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(String.class.getName());
    }

}
