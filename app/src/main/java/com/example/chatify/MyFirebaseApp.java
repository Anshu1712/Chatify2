package com.example.chatify;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable persistence once and early
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Optional: keep a common node synced (e.g., users)
        FirebaseDatabase.getInstance().getReference("Users").keepSynced(true);
    }
}
