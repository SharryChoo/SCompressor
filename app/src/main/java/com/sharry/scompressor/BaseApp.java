package com.sharry.scompressor;

import android.app.Application;

import com.sharry.lib.scompressor.SCompressor;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-12-19 14:01
 */
public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SCompressor.init(this, "com.sharry.scompressor.FileProvider");
    }
}
