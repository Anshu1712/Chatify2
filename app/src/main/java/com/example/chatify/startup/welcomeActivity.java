package com.example.chatify.startup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatify.MainActivity;
import com.example.chatify.R;
import com.example.chatify.auth.Login;
import com.google.android.material.button.MaterialButton;

public class welcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        MaterialButton btnAgree = findViewById(R.id.button);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(welcomeActivity.this, Login.class));
            }
        });
    }
}