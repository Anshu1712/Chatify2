package com.example.chatify.startup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatify.MainActivity;
import com.example.chatify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplachActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splach);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    startActivity(new Intent(SplachActivity.this, MainActivity.class));
                    finish();
                }
            }, 4000);
        } else {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    startActivity(new Intent(SplachActivity.this, welcomeActivity.class));
                    finish();
                }
            }, 4000);
        }
    }
}