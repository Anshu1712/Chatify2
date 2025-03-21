package com.example.chatify.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatify.MainActivity;
import com.example.chatify.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class Login extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private MaterialButton signUpButton;
    private MaterialButton loginButton;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog; // Declare ProgressDialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        auth = FirebaseAuth.getInstance();

        // If the user is already signed in, redirect to MainActivity
        if (auth.getCurrentUser() != null) {
            navigateToMainActivity();
        }

        // Initialize UI components
        signUpButton = findViewById(R.id.singup);
        loginButton = findViewById(R.id.verify);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.passcode);

        // Initialize the ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false); // Prevent cancellation by tapping outside

        // Set listeners for buttons
        signUpButton.setOnClickListener(v -> navigateToEmailVerificationActivity());

        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if (isValidInput(email, password)) {
                signInUser(email, password);
            }
        });
    }

    // Validate email and password inputs
    private boolean isValidInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!email.contains("@")) {
            Toast.makeText(this, "Invalid email address.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Handle user sign-in
    private void signInUser(String email, String password) {
        // Show the progress dialog
        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Hide the progress dialog
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        navigateToMainActivity();
                    } else {
                        handleSignInError(task.getException());
                    }
                });
    }

    // Handle different sign-in errors
    private void handleSignInError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            String errorMessage = exception.getMessage();
            Toast.makeText(this, "Sign-in failed: " + errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "An unknown error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Navigate to the main activity
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Navigate to the email verification activity (sign-up flow)
    private void navigateToEmailVerificationActivity() {
        Intent intent = new Intent(this, emailverification.class);
        startActivity(intent);
        finish();
    }


}