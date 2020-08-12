package com.property.keys.tasks;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskExecutor {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Executor executor = Executors.newCachedThreadPool();

    public void executeAsync(AsyncTask task) {
        try {
            task.call();
            executor.execute(new RunnableTask(handler, task));
        } catch (Exception e) {
        }
    }

    public static class RunnableTask<R> implements Runnable {
        private final Handler handler;
        private final AsyncTask task;

        public RunnableTask(Handler handler, AsyncTask task) {
            this.handler = handler;
            this.task = task;
        }

        @Override
        public void run() {
            try {
                handler.post(new RunnableTaskForHandler(task));
            } catch (Exception e) {
            }
        }
    }

    public static class RunnableTaskForHandler implements Runnable {

        private AsyncTask task;

        public RunnableTaskForHandler(AsyncTask task) {
            this.task = task;
        }

        @Override
        public void run() {
            task.runInBackground();
        }
    }
}
