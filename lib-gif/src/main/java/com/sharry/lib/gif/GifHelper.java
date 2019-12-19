package com.sharry.lib.gif;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-12-19
 */
public class GifHelper {

    /**
     * 对 GIF 图进行降采样
     *
     * @param inputFile  输入文件
     * @param outputFile 输出文件
     * @param sampleSize 采样率(需要为 2 的幂次)
     */
    public static void downsampler(String inputFile, String outputFile, int sampleSize) {
        nativeDownsampler(inputFile, outputFile, sampleSize);
    }

    static native void nativeDownsampler(String inputFile, String outputFile, int sampleSize);

}
