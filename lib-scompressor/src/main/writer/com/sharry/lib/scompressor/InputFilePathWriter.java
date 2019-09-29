package com.sharry.lib.scompressor;

import androidx.annotation.NonNull;

/**
 * Adapter image file path 2 file path.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-12 14:29
 */
public class InputFilePathWriter implements InputWriter<String> {

    @Override
    public String writeToDisk(@NonNull DataSource<String> inputSource) throws Throwable {
        return inputSource.getSource();
    }

    @Override
    public boolean isWriter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(String.class.getName());
    }

}
