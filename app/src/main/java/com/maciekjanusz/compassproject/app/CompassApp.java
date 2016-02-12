package com.maciekjanusz.compassproject.app;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Application extension for initializing LeakCanary
 */
public class CompassApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);
    }
}
