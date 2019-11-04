package com.example.computer_networking_1;

import android.app.Application;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class ArchLifecycleApp extends Application implements LifecycleObserver {
    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded(){

    }
}
