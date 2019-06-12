package com.sharry.lib.scompressor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

/**
 * Compress runnable.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/30  16:31
 */
class AsyncCall<InputType, OutputType> implements Runnable {

    private static final String TAG = AsyncCall.class.getSimpleName();

    private final Request<InputType, OutputType> mRequest;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            OutputType outputData = (OutputType) msg.obj;
            if (outputData != null) {
                mRequest.callback.onCompressComplete(outputData);
            } else {
                Log.e(TAG, "Compress failed.");
            }
        }
    };

    AsyncCall(Request<InputType, OutputType> request) {
        this.mRequest = request;
    }

    @Override
    public void run() {
        try {
            Message message = Message.obtain();
            message.obj = Core.execute(mRequest);
            mHandler.sendMessage(message);
        } catch (IOException e) {
            Log.e(TAG, "Image compress failed.", e);
        }
    }

}
