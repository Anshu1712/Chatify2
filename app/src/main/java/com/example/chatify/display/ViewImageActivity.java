package com.example.chatify.display;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.example.chatify.common.Common;
import com.example.chatify.databinding.ActivityViewImageBinding;

public class ViewImageActivity extends AppCompatActivity {
    private ActivityViewImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imageView.setImageBitmap(Common.IMAGE_BITMAP);

        // Handle back button click
        binding.backArrow.setOnClickListener(v -> onBackPressed());
    }
}
