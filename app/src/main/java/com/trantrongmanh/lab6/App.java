package com.trantrongmanh.lab6;

import android.app.Application;

import com.google.firebase.FirebaseApp;

/**
 * Custom Application class for app-level initialization.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
