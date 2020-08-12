package com.property.keys.tasks;

import java.util.concurrent.Callable;

public interface AsyncTask extends Callable<Void> {
    void runInBackground();
}
