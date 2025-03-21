package com.example.chatify.auth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.chatify.MainActivity;
import com.example.chatify.R;
import com.example.chatify.databinding.ActivitySetUserInfoBinding;
import com.example.chatify.model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SetUserInfoActivity extends AppCompatActivity {
    private ActivitySetUserInfoBinding binding;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_user_info);
        progressDialog = new ProgressDialog(this);
        initButtonClick();

    }

    private void initButtonClick() {
        // Set click listener for the "Update" button.
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                // Check if the username (input field) is empty.
                if (TextUtils.isEmpty(binding.phoneNumberEt2.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Please input username", Toast.LENGTH_SHORT).show();  // Show a toast if username is empty.
                } else {
                    doUpdate();  // Call the method to update the user information if username is valid.
                }
                if (TextUtils.isEmpty(binding.phoneNumberEt3.getText().toString())) {
                    binding.phoneNumberEt3.setText("Hey there I'M using chatify");
                    doUpdate();
                } else {
                    doUpdate();
                }
            }
        });
        binding.Change1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pickImage();
            }
        });
    }

    private void doUpdate() {
        progressDialog.setMessage("Updating...");
        progressDialog.show();

        // Get the instance of Firebase Firestore to store user data.
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        // Get the current logged-in user from Firebase Authentication.
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {  // Check if the user is authenticated.
            String userID = firebaseUser.getUid();  // Get the unique user ID from Firebase Authentication.

            // Get the phone number input by the user from the EditText.
            String phoneNumber = binding.phoneNumberEt4.getText().toString().trim();

            // Check if the phone number is empty
            if (TextUtils.isEmpty(phoneNumber)) {
                progressDialog.dismiss();  // Dismiss the progress dialog
                Toast.makeText(getApplicationContext(), "Please enter your phone number", Toast.LENGTH_SHORT).show();  // Show a toast message
                return;  // Exit the method early since phone number is required
            }

            // Create a new Users object to hold user data (including the phone number).
            Users users = new Users(
                    userID,
                    binding.phoneNumberEt2.getText().toString(),  // Username
                    phoneNumber,  // Phone number entered by the user
                    "", "", firebaseUser.getEmail(), "", "", "", binding.phoneNumberEt3.getText().toString() // Bio and other data
            );

            // Update the Firestore "Users" collection with the updated user data.
            firebaseFirestore.collection("Users")
                    .document(userID)  // Use the user ID as the document ID to store user data.
                    .set(users)  // Set the user object in the Firestore database.
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();  // Dismiss the progress dialog when the update is successful.
                            Toast.makeText(getApplicationContext(), "Update Success", Toast.LENGTH_SHORT).show();  // Show success message.
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));  // Navigate to MainActivity after successful update.
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();  // Dismiss the progress dialog if the update fails.
                            Toast.makeText(getApplicationContext(), "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();  // Show failure message.
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();  // Show error message if user is null.
            progressDialog.dismiss();  // Dismiss the progress dialog if user is not authenticated.
        }
    }

}