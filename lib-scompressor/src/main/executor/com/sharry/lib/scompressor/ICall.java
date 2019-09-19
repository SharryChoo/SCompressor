package com.sharry.lib.scompressor;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-09-19 14:13
 */
public interface ICall<InputType, OutputType> {

    OutputType execute(Request<InputType, OutputType> request) throws Throwable;

}
