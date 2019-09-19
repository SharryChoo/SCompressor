package com.sharry.lib.scompressor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
            if (msg.obj instanceof Throwable) {
                mRequest.callback.onCompressFailed((Throwable) msg.obj);
                return;
            }
            mRequest.callback.onCompressSuccess((OutputType) msg.obj);
        }
    };

    AsyncCall(Request<InputType, OutputType> request) {
        this.mRequest = request;
    }

    @Override
    public void run() {
        Message msg = Message.obtain();
        try {
            msg.obj = Core.execute(mRequest);
        } catch (Throwable e) {
            msg.obj = e;
        }
        mHandler.sendMessage(msg);
    }

}
