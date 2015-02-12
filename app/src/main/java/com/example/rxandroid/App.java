package com.example.rxandroid;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Dustin on 2/10/15.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
