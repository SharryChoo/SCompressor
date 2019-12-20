package com.sharry.lib.gif;

import android.graphics.Bitmap;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Gif 解码工具类
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-12-20 14:23
 */
public final class GifDecoder {

    // /////////////////////////////////////////// Get instance //////////////////////////////////////////////////

    /**
     * Get an instance of GifDecoder
     *
     * @param filePath a gif file path.
     * @return an instance of GifDecoder
     */
    public static GifDecoder decodeFilePath(String filePath) {
        return new GifDecoder(nativeDecodeFile(filePath));
    }

    /**
     * Get an instance of GifDecoder
     *
     * @param stream a gif stream
     * @return an instance of GifDecoder
     */
    public static GifDecoder decodeStream(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException();
        }
        // use buffer pool
        byte[] tempStorage = new byte[16 * 1024];
        return new GifDecoder(nativeDecodeStream(stream, tempStorage));
    }

    /**
     * Get an instance of GifDecoder
     *
     * @param data a gif byte array.
     * @return an instance of GifDecoder
     */
    public static GifDecoder decodeByteArray(byte[] data) {
        return decodeByteArray(data, 0, data.length);
    }

    /**
     * Get an instance of GifDecoder
     *
     * @param data a gif byte array.
     * @return an instance of GifDecoder
     */
    public static GifDecoder decodeByteArray(byte[] data, int offset, int length) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        if (offset < 0 || length < 0 || (offset + length > data.length)) {
            throw new IllegalArgumentException("invalid offset/length parameters");
        }
        return new GifDecoder(nativeDecodeByteArray(data, offset, length));
    }

    /**
     * Get an instance of GifDecoder
     *
     * @param buffer a gif native buffer.
     * @return an instance of GifDecoder
     */
    public static GifDecoder decodeByteBuffer(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        if (!buffer.isDirect()) {
            if (buffer.hasArray()) {
                byte[] byteArray = buffer.array();
                return decodeByteArray(byteArray, buffer.position(), buffer.remaining());
            } else {
                throw new IllegalArgumentException("Cannot have non-direct ByteBuffer with no byte array");
            }
        }
        return new GifDecoder(nativeDecodeByteBuffer(buffer, buffer.position(), buffer.remaining()));
    }

    // /////////////////////////////////////////// Inner Method. //////////////////////////////////////////////////

    private long mNativePtr;

    private GifDecoder(long nativePtr) {
        mNativePtr = nativePtr;
    }

    public int getDefaultLoopCount() {
        return 1;
    }

    /**
     * Get Bitmap at require frame.
     *
     * @param frameNr         the frame that u wanted.
     * @param output          in and out args, will fill pixels at native.
     * @param previousFrameNr previous frame number, u can pass -1.
     * @return next frame duration. Unit is ms
     */
    public long getFrame(int frameNr, Bitmap output, int previousFrameNr) {
        return nativeGetFrame(mNativePtr, frameNr, output, previousFrameNr);
    }

    /**
     * Get gif width.
     *
     * @return gif width.
     */
    public int getWidth() {
        return nativeGetWidth(mNativePtr);
    }

    /**
     * Get gif height.
     *
     * @return gif height.
     */
    public int getHeight() {
        return nativeGetHeight(mNativePtr);
    }

    /**
     * Get gif frame count.
     *
     * @return gif frame count.
     */
    public int getFrameCount() {
        return nativeGetFrameCount(mNativePtr);
    }

    /**
     * Destroy resource.
     */
    public void destroy() {
        nativeDestroy(mNativePtr);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativePtr != 0) {
                nativeDestroy(mNativePtr);
                mNativePtr = 0;
            }
        } finally {
            super.finalize();
        }
    }

    // /////////////////////////////////////////// Native Method. //////////////////////////////////////////////////

    static {
        System.loadLibrary("gifkit");
    }

    private static native long nativeDecodeFile(String filePath);

    private static native long nativeDecodeStream(InputStream stream, byte[] tempStorage);

    private static native long nativeDecodeByteArray(byte[] data, int offset, int length);

    private static native long nativeDecodeByteBuffer(ByteBuffer buffer, int position, int remaining);

    private static native int nativeGetWidth(long nativePtr);

    private static native int nativeGetHeight(long nativePtr);

    private static native int nativeGetFrameCount(long mNativePtr);

    private static native long nativeGetFrame(long decoder, int frameNr, Bitmap output, int previousFrameNr);

    private static native void nativeDestroy(long nativePtr);

}
