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
//getUserData();  // Retrieve existing user data when the activity starts
    }

    private void initButtonClick() {
        // Set click listener for the "Update" button.
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(binding.phoneNumberEt2.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Please input username", Toast.LENGTH_SHORT).show();
                } else {
                    doUpdate();
                }

                if (TextUtils.isEmpty(binding.phoneNumberEt3.getText().toString())) {
                    binding.phoneNumberEt3.setText("Hey there! I'm using Chatify");
                }
                doUpdate();
            }
        });

        binding.Change1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pick Image (placeholder for future functionality)
            }
        });
    }

    private void doUpdate() {
        progressDialog.setMessage("Updating...");
        progressDialog.show();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String userID = firebaseUser.getUid();

            // Retrieve and validate phone number
            String phoneNumber = binding.phoneNumberEt4.getText().toString().trim();

            if (TextUtils.isEmpty(phoneNumber)) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Please enter your phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Append +91 if not already included
            if (!phoneNumber.startsWith("+91")) {
                phoneNumber = "+91" + phoneNumber;
            }

            // Create the user object
            Users users = new Users(
                    userID,
                    binding.phoneNumberEt2.getText().toString(),  // Username
                    phoneNumber,  // Store phone number with +91
                    "", "", firebaseUser.getEmail(), "", "", "", binding.phoneNumberEt3.getText().toString()  // Bio and other data
            );

            // Store the user data in Firestore
            firebaseFirestore.collection("Users")
                    .document(userID)
                    .set(users)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Update Success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

//    private void getUserData() {
//        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (firebaseUser != null) {
//            String userID = firebaseUser.getUid();
//
//            firebaseFirestore.collection("Users")
//                    .document(userID)
//                    .get()
//                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(DocumentSnapshot documentSnapshot) {
//                            if (documentSnapshot.exists()) {
//                                String phoneNumber = documentSnapshot.getString("phoneNumber");
//
//                                // Ensure the displayed number starts with +91
//                                if (phoneNumber != null && !phoneNumber.startsWith("+91")) {
//                                    phoneNumber = "+91" + phoneNumber;
//                                }
//
//                                binding.phoneNumberEt4.setText(phoneNumber);
//                            }
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(SetUserInfoActivity.this, "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }
//    }
}
