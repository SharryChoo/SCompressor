package com.sharry.lib.scompressor;

import androidx.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.sharry.lib.scompressor.SCompressor.TAG;

/**
 * 图片压缩分发器
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.5
 * @since 2018/8/27 22:14
 */
class CompressDispatcher extends ThreadPoolExecutor {

    /**
     * Sync dispatch a task.
     *
     * @return an instance of output data.
     */
    static <InputType, OutputType> OutputType syncDispatcher(Request<InputType, OutputType> request) {
        OutputType result = null;
        try {
            result = Core.execute(request);
        } catch (Throwable e) {
            Log.e(TAG, "Compress failed.", e);
        }
        return result;
    }

    /**
     * Async dispatch a task.
     */
    static <InputType, OutputType> void asyncDispatcher(Request<InputType, OutputType> request) {
        InstanceHolder.INSTANCE.execute(new AsyncCall<>(request));
    }

    private CompressDispatcher(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.e(TAG, "Task rejected, too many task!");
                    }
                });
    }

    /**
     * Handle exceptions when thread is completed.
     *
     * @param r the runnable that has completed
     * @param t the exception that caused termination, or null if
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (null == t && r instanceof Future<?>) {
            try {
                ((Future<?>) r).get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
            }
        }
        if (null != t) {
            Log.w(TAG, "Running task appeared exception! Thread [" +
                    Thread.currentThread().getName() + "], because [" + t.getMessage() + "]\n" +
                    t.getMessage());
        }
    }

    /**
     * The holder associated with create CompressExecutorPool instance.
     */
    private static final class InstanceHolder {

        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int INIT_THREAD_COUNT = CPU_COUNT + 1;
        private static final int MAX_THREAD_COUNT = INIT_THREAD_COUNT;
        private static final long SURPLUS_THREAD_LIFE = 30L;

        private static final CompressDispatcher INSTANCE = new CompressDispatcher(
                INIT_THREAD_COUNT,
                MAX_THREAD_COUNT,
                SURPLUS_THREAD_LIFE,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(64),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        Thread thread = new Thread(r, CompressDispatcher.class.getSimpleName());
                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.setDaemon(false);
                        return thread;
                    }
                }
        );
    }

}
