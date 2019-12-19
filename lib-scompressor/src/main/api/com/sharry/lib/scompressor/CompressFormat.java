package com.sharry.lib.scompressor;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-12-18 15:07
 */
public enum CompressFormat {

    /**
     * JPEG type without alpha.
     */
    JPEG(false, ".jpg"),

    /**
     * PNG type with alpha.
     */
    PNG(true, ".png"),

    /**
     * WebP type with alpha.
     */
    WEBP(true, ".webp");

    private final boolean hasAlpha;
    private final String suffix;

    CompressFormat(boolean hasAlpha, String suffix) {
        this.hasAlpha = hasAlpha;
        this.suffix = suffix;
    }

    public boolean hasAlpha() {
        return hasAlpha;
    }

    public String suffix() {
        return suffix;
    }

}
