package com.sharry.lib.scompressor.recycleable;

import android.util.Log;
import android.util.LruCache;

/**
 * A fixed size Array Pool that evicts arrays using an LRU strategy to keep the pool under
 * the maximum byte size.
 */
public final class LruByteArrayPool implements ByteArrayPool {

    // 4MB.
    private static final int DEFAULT_SIZE = 4 * 1024 * 1024;

    private final int mMaxSize;
    private LruCache<Integer, byte[]> mCache;

    public LruByteArrayPool() {
        this(DEFAULT_SIZE);
    }

    public LruByteArrayPool(int maxSize) {
        this.mMaxSize = maxSize;
        this.mCache = new LruCache<Integer, byte[]>(mMaxSize) {

            @Override
            protected int sizeOf(Integer key, byte[] value) {
                return key;
            }

            @Override
            protected byte[] create(Integer key) {
                return new byte[key];
            }
        };
    }

    @Override
    public void put(byte[] array) {
        mCache.put(array.length, array);
    }

    @Override
    public byte[] get(int size) {
        return mCache.get(size);
    }

}
