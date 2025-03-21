package com.example.chatify.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatify.R;
import com.example.chatify.model.Users;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class emailverification extends AppCompatActivity {


    private EditText email;
    private MaterialButton registerButton, resendButton;
    private FirebaseFirestore firestore;
    private EditText password;
    private EditText number1, number2;
    private FirebaseAuth auth;
    private long lastSentTime = 0;
    private static final long COOLDOWN_TIME = 30000; // 30 seconds cooldown
    private CountDownTimer countDownTimer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emailverification);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Bind views
        registerButton = findViewById(R.id.verify1);
        resendButton = findViewById(R.id.resendVerificationEmailButton);
        email = findViewById(R.id.email);
        password = findViewById(R.id.passcode);


        // Set click listener for the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = email.getText().toString();
                String pass = password.getText().toString();
                registerUser(emailAddress, pass);
            }
        });

        // Set click listener for the resend verification email button
        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationEmail();
            }
        });
    }

    // Register user with email and password
    private void registerUser(String emailAddress, String password) {
        // Check if the email or password fields are empty
        if (emailAddress.isEmpty()) {
            Toast.makeText(emailverification.this, "Please enter an email address", Toast.LENGTH_SHORT).show();
            return; // Stop execution if the email is empty
        }
        if (password.isEmpty()) {
            Toast.makeText(emailverification.this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return; // Stop execution if the password is empty
        }

        // Proceed with registration if both fields are filled
        auth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Send verification email if registration is successful
                        sendVerificationEmail();
                    } else {
                        Toast.makeText(emailverification.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Send the email verification link to the user's email
    private void sendVerificationEmail() {
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Change button text to "Please Verify"
                        registerButton.setText("Please Verify");
                        registerButton.setEnabled(false); // Disable button until verified

                        // Enable the resend button and start the countdown timer
                        resendButton.setEnabled(true);
                        startResendCooldown();

                        // Show a message to let the user know that the email is sent
                        Toast.makeText(emailverification.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();

                        // Add user data to Firestore (no setup yet, user is still unverified)
                        firestore.collection("Users").document(auth.getCurrentUser().getUid())
                                .set(new Users(auth.getCurrentUser().getUid(), "name", "", "", "", "", "", "", "", ""))
                                .addOnSuccessListener(unused -> {
                                });
                    } else {
                        Toast.makeText(emailverification.this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Resend the email verification link
    private void resendVerificationEmail() {
        if (auth.getCurrentUser() != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSentTime < COOLDOWN_TIME) {
                long timeLeft = (COOLDOWN_TIME - (currentTime - lastSentTime)) / 1000;
                Toast.makeText(emailverification.this, "Please wait " + timeLeft + " seconds before resending.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send the email verification again
            auth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Show a message to let the user know that the email was resent
                            Toast.makeText(emailverification.this, "Verification email resent. Please check your inbox.", Toast.LENGTH_SHORT).show();

                            // Update the last sent time and start the cooldown
                            lastSentTime = System.currentTimeMillis();
                            startResendCooldown();
                        } else {
                            // Handle failure to resend the email
                            Toast.makeText(emailverification.this, "Failed to resend verification email. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Start the countdown timer for the resend cooldown
    private void startResendCooldown() {
        resendButton.setEnabled(false); // Disable the button for 60 seconds

        countDownTimer = new CountDownTimer(COOLDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                resendButton.setText("Resend in " + secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                resendButton.setEnabled(true); // Enable the button after cooldown
                resendButton.setText("Resend Verification Email");
            }
        }.start();
    }

    // Method to check if the email has been verified
    private void checkEmailVerification() {
        if (auth.getCurrentUser() != null) {
            FirebaseAuth.getInstance().getCurrentUser().reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // If the email is verified, enable the button and change text
                            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                registerButton.setText("Verified");
                                registerButton.setEnabled(true); // Enable the button
                                // Now user can proceed to SetupUserActivity
                                navigateToSetupUserActivity();
                            } else {
                                // Email not verified yet, show message to check inbox
                                Toast.makeText(emailverification.this, "Please verify your email by clicking the link sent to your inbox.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // Handle error if reloading user info fails
                            Toast.makeText(emailverification.this, "Error checking verification status. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If the user is null, handle appropriately
            Toast.makeText(emailverification.this, "No user found. Please register first.", Toast.LENGTH_SHORT).show();
        }
    }

    // Navigate to SetupUserActivity and finish emailVerification activity
    private void navigateToSetupUserActivity() {
        Intent intent = new Intent(emailverification.this, SetUserInfoActivity.class);
        startActivity(intent);
        finish(); // Finish emailVerification activity to prevent going back to it
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check email verification status when the activity resumes
        checkEmailVerification();
    }

    // Override the onBackPressed() method to navigate to the login page
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(emailverification.this, Login.class); // Change LoginActivity to your login activity
        startActivity(intent);
        finish(); // Close the current activity
    }
}