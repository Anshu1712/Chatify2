package com.example.chatify.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.example.chatify.databinding.ActivityUserProfileBinding;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);
        // Get data from intent
        Intent intent = getIntent();
        String userName = intent.getStringExtra("username");
        String receiverID = intent.getStringExtra("userID");
        String userProfile = intent.getStringExtra("userProfile");
        String userNumber = intent.getStringExtra("userPhone");
        String userBio = intent.getStringExtra("bio");

        // Display the received information
        if (receiverID != null) {
            // Set username in the toolbar title
            binding.userNameTag.setTitle(userName);

            // Fetch and display the user's profile image
            if (userProfile != null && !userProfile.isEmpty()) {
                Glide.with(this).load(userProfile).into(binding.imageProfile);
            } else {
                binding.imageProfile.setImageResource(R.drawable.user); // Default image
            }


            // Display phone number
            binding.numberTxt.setText(userNumber != null && !userNumber.isEmpty() ? userNumber : "No phone number available");

            // Display bio
            binding.bioo.setText(userBio != null && !userBio.isEmpty() ? userBio : "No bio available");
            Log.d("UserProfileActivity", "User Bio Text: " + userBio);

        } else {
            Toast.makeText(this, "User data is missing!", Toast.LENGTH_SHORT).show();
        }

        // Initialize the toolbar
        initToolbar();
    }

    private void initToolbar() {
        // Set the back navigation icon on the toolbar
        binding.userNameTag.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
        setSupportActionBar(binding.userNameTag);

        // Enable the up navigation button (back arrow)
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Handle back button clicks
        binding.userNameTag.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for the toolbar
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item clicks from the toolbar
        if (item.getItemId() == android.R.id.home) {
            finish(); // Finish the activity when the back button is clicked
        } else {
            Toast.makeText(this, "Option selected: " + item.getItemId(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}