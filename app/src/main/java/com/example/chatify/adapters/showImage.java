package com.example.chatify.adapters;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.jsibbold.zoomage.ZoomageView;

public class showImage extends AppCompatActivity {

    ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        button = findViewById(R.id.backbtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ZoomageView zoomageView = findViewById(R.id.img_show);
        TextView name = findViewById(R.id.tv_name);
        TextView time = findViewById(R.id.tv_time);

        String userName = getIntent().getStringExtra("name");
        String mTime = getIntent().getStringExtra("time");
        String imageUri = getIntent().getStringExtra("IMAGE");

        name.setText(userName);
        time.setText(mTime);
        Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.person)
                .error(R.drawable.person)
                .into(zoomageView);
    }
}
