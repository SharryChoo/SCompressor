package com.sharry.lib.scompressor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The description of data.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/2/2019 11:04 AM
 */
interface DataSource<SourceType> {

    /**
     * Get generic SourceType class.
     *
     * @return a class of generic SourceType type.
     */
    @NonNull
    Class<SourceType> getType();

    /**
     * Get an input source.
     *
     * @return an instance of generic SourceType type(will be null).
     */
    @Nullable
    SourceType getSource();

}
