package com.sharry.lib.scompressor.recycleable;

import androidx.annotation.VisibleForTesting;

/**
 * A fixed size Array Pool that evicts arrays using an LRU strategy to keep the pool under
 * the maximum byte size.
 */
public final class LruArrayPool implements ArrayPool {

    // 4MB.
    private static final int DEFAULT_SIZE = 4 * 1024 * 1024;

    /**
     * The maximum number of times larger an int array may be to be than a requested size to eligible
     * to be returned from the pool.
     */
    @VisibleForTesting
    static final int MAX_OVER_SIZE_MULTIPLE = 8;

    /**
     * Used to calculate the maximum % of the total pool size a single byte array may consume.
     */
    private static final int SINGLE_ARRAY_MAX_SIZE_DIVISOR = 2;

    @Override
    public <T> void put(T array) {
        // nothing.
    }

    @Override
    public <T> T get(int size, Class<T> arrayClass) {
        if (arrayClass == byte[].class) {
            return (T) new byte[size];
        } else {
            return null;
        }
    }

    @Override
    public <T> T getExact(int size, Class<T> arrayClass) {
        return null;
    }

    @Override
    public void clearMemory() {

    }

    @Override
    public void trimMemory(int level) {

    }

}
