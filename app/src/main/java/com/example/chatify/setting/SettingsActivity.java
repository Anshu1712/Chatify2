package com.example.chatify.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.example.chatify.MainActivity;
import com.example.chatify.R;
import com.example.chatify.databinding.ActivitySettingsBinding;
import com.example.chatify.profile.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;  // View binding object
    private FirebaseUser firebaseUser;  // Firebase authenticated user
    private FirebaseFirestore firestore;  // Firestore instance

    private ImageView backButton1;  // Back button
    private Toolbar toolbar;  // Toolbar for settings
    private LinearLayout layout;  // Profile section layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Get authenticated user

        if (firebaseUser != null) {
            getInfo();  // Fetch user data if logged in
        }

        // Initialize views using binding
        backButton1 = binding.backArrow;
        layout = binding.profileLiner;
        toolbar = binding.toolbar4;

        setSupportActionBar(toolbar); // Set toolbar as action bar

        // Handle back button click
        backButton1.setOnClickListener(v -> navigateBackToMainActivity());

        // Handle profile layout click
        layout.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateBackToMainActivity();
    }

    private void navigateBackToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close SettingsActivity
    }

    private void getInfo() {
        firestore.collection("Users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("username");
                            String userBio = documentSnapshot.getString("bio");
                            String imageProfile = documentSnapshot.getString("imageProfile");

                            binding.nameTxt.setText(userName != null ? userName : "Unknown");
                            binding.bioTxt.setText(userBio != null ? userBio : "No bio available");

                            Glide.with(SettingsActivity.this)
                                    .load(imageProfile)
                                    .placeholder(R.drawable.user) // Show default if null
                                    .error(R.drawable.user) // Show default if error
                                    .into(binding.imageProfile);
                        } else {
                            Log.d("Get Data", "No user document found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Get Data", "Error fetching user data: " + e.getMessage(), e);
                    }
                });
    }
}
