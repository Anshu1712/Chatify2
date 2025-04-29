package com.example.chatify.display;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

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
