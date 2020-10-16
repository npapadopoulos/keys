package com.property.keys.tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class TaskExecutor {

    private static final String TAG = TaskExecutor.class.getSimpleName();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Executor executor = Executors.newCachedThreadPool();

    public void executeAsync(AsyncTask<Void, Void, Void> task) {
        try {
            executor.execute(new RunnableTask(handler, task));
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Failed to execute task: %s", task.getClass());
        }
    }

    public static class RunnableTask<R> implements Runnable {
        private final Handler handler;
        private final AsyncTask<Void, Void, Void> task;

        public RunnableTask(Handler handler, AsyncTask<Void, Void, Void> task) {
            this.handler = handler;
            this.task = task;
        }

        @Override
        public void run() {
            try {
                handler.post(new RunnableTaskForHandler(task));
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "Failed to run  task: %s", task.getClass());
            }
        }
    }

    public static class RunnableTaskForHandler implements Runnable {

        private AsyncTask<Void, Void, Void> task;

        public RunnableTaskForHandler(AsyncTask<Void, Void, Void> task) {
            this.task = task;
        }

        @Override
        public void run() {
            task.execute();
        }
    }
}
