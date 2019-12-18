package com.sharry.lib.scompressor;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Compress runnable.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/30  16:31
 */
final class AsyncCaller {

    private static final String TAG = AsyncCaller.class.getSimpleName();

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    static <InputType, OutputType> void execute(final Request<InputType, OutputType> request,
                                                final ICompressorCallback<OutputType> callback) {
        InstanceHolder.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final OutputType data = SyncCaller.execute(request);
                    MAIN_HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(data);
                        }
                    });
                } catch (final Throwable throwable) {
                    MAIN_HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailed(throwable);
                        }
                    });
                }
            }
        });
    }

    /**
     * The holder associated with with CompressExecutorPool instance.
     */
    private static final class InstanceHolder {

        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int INIT_THREAD_COUNT = CPU_COUNT + 1;
        private static final int MAX_THREAD_COUNT = INIT_THREAD_COUNT;
        private static final long SURPLUS_THREAD_LIFE = 30L;

        private static final ThreadPoolExecutor INSTANCE = new ThreadPoolExecutor(
                INIT_THREAD_COUNT,
                MAX_THREAD_COUNT,
                SURPLUS_THREAD_LIFE,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(64),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        Thread thread = new Thread(r, TAG);
                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.setDaemon(false);
                        return thread;
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.e(TAG, "Task rejected, too many task!");
                    }
                }
        );
    }

}
