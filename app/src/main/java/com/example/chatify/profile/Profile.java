package com.example.chatify.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.chatify.MainActivity;
import com.example.chatify.R;
import com.example.chatify.adapters.showImage;
import com.example.chatify.databinding.ActivityProfileBinding;
import com.example.chatify.startup.SplachActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Profile extends AppCompatActivity {

    private static final int IMAGE_GALLERY_REQUEST = 111;
    private ActivityProfileBinding binding;
    private FirebaseUser firebaseUser;
    private ImageView backButton1;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firestore;
    private Toolbar toolbar;
    private BottomSheetDialog bottomSheetDialog, bsDialogEditName, bsDialog;
    private Uri imageUri;

    private String imageProfile;
    private String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomSheetDialog = new BottomSheetDialog(this);

        // Initialize the back button (ImageView) from the layout
        backButton1 = binding.backArrow;

        // Initialize the toolbar from the layout, assuming there's a Toolbar in the layout
        toolbar = binding.toolbar4;

        // Set the toolbar as the action bar (top navigation bar)
        setSupportActionBar(toolbar);

        fetchUserProfileData();
        backButton1.setOnClickListener(v -> navigateBackToMainActivity());

        // Initialize FirebaseAuth and Firestore instances to interact with Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);

        // If the user is authenticated, fetch their information from Firestore
        if (firebaseUser != null) {
            getUserInfo();  // Fetch and display user info from Firestore.
        }

        initActionClick();
    }

    private void initActionClick() {
        binding.fabCamera.setOnClickListener(v -> showBottomSheetPickPhoto());

        binding.nameEdit.setOnClickListener(view -> showButtomSheetEditName());

        binding.bioEdit.setOnClickListener(view -> showBioBottomSheet());

        binding.button5.setOnClickListener(view -> showDialogSignOut());

    }

    private void fetchUserProfileData() {
        if (firebaseUser != null) {
            String userID = firebaseUser.getUid();  // Get user ID from FirebaseAuth.

            // Fetch user data from Firestore
            firestore.collection("Users")
                    .document(userID)  // Get the document for the current user.
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the phone number from Firestore
                            String phoneNumber = documentSnapshot.getString("userPhone");

                            // Display the phone number in the TextView (or any other UI element)
                            binding.phone.setText(phoneNumber != null ? phoneNumber : "No phone number set");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showBottomSheetPickPhoto() {
        bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.botton_sheet, null);
        bottomSheetDialog.setContentView(view);

        view.findViewById(R.id.gallery).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openGallery();
        });

        view.findViewById(R.id.camera1).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            checkCameraPermission();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Objects.requireNonNull(bottomSheetDialog.getWindow())
                    .addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        bottomSheetDialog.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 221);
        } else {
            openCamera();
        }
    }


    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "IMG_" + timeStamp;
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        photoFile
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 440);
            }
        }
    }


    @SuppressLint("MissingInflatedId")
    private void showBioBottomSheet() {
        if (bsDialog == null) {
            bsDialog = new BottomSheetDialog(this);
        }
        View view = getLayoutInflater().inflate(R.layout.biobottomsheet, null);
        ((View) view.findViewById(R.id.button7)).setOnClickListener(view1 -> bsDialog.dismiss());

        EditText edBio = view.findViewById(R.id.phoneNumberEt6);

        ((View) view.findViewById(R.id.button8)).setOnClickListener(view12 -> {
            if (TextUtils.isEmpty(edBio.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Bio Section Can't be empty", Toast.LENGTH_SHORT).show();
            } else {
                updateBio(edBio.getText().toString());
                bsDialog.dismiss();
            }
        });
        bsDialog = new BottomSheetDialog(this);
        bsDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Objects.requireNonNull(bsDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        bsDialog.setOnDismissListener(dialog -> bsDialogEditName = null);
        bsDialog.show();
    }

    private void showButtomSheetEditName() {
        if (bottomSheetDialog == null) {
            bottomSheetDialog = new BottomSheetDialog(this);
        }
        View view = getLayoutInflater().inflate(R.layout.botton_sheet_edit_name, null);

        ((View) view.findViewById(R.id.button3)).setOnClickListener(view1 -> bsDialogEditName.dismiss());

        EditText edUserName = view.findViewById(R.id.phoneNumberEt4);

        ((View) view.findViewById(R.id.button4)).setOnClickListener(view12 -> {
            if (TextUtils.isEmpty(edUserName.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Name Section Can't be empty", Toast.LENGTH_SHORT).show();
            } else {
                updateName(edUserName.getText().toString());
                bsDialogEditName.dismiss();
            }
        });

        bsDialogEditName = new BottomSheetDialog(this);
        bsDialogEditName.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Objects.requireNonNull(bsDialogEditName.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        bsDialogEditName.setOnDismissListener(dialog -> bsDialogEditName = null);
        bsDialogEditName.show();
    }

    private void getUserInfo() {
        firestore.collection("Users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Check if document exists
                    if (documentSnapshot.exists()) {
                        // Retrieve user info from Firestore
                        userName = documentSnapshot.getString("username");      // 🔧 CHANGED
                        imageProfile = documentSnapshot.getString("imageProfile"); // 🔧 CHANGED

                        String userPhone = documentSnapshot.getString("userPhone");
                        String userBio = documentSnapshot.getString("bio");
                        String userEmail = documentSnapshot.getString("email");

                        Log.d("ProfileActivity", "User phone: " + userPhone);  // Debugging to check phone number

                        // Handle case if phone number is null or empty
                        if (userPhone == null || userPhone.isEmpty()) {
                            binding.phone.setText("Phone number not available");
                        } else {
                            binding.phone.setText(userPhone); // Display phone number
                        }

                        // Set the values to the UI components
                        binding.nameProfile.setText(userName);  // Setting username
                        binding.bio.setText(userBio);          // Setting bio
                        binding.email.setText(userEmail);      // Setting email
                        Glide.with(Profile.this).load(imageProfile).into(binding.Change);

                    } else {
                        // Handle case where user data does not exist in Firestore
                        showError("User data not found");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure when fetching data
                    showError("Failed to load user info");
                });

        binding.Change.setOnClickListener(v -> {
            // 🔧 CHANGED: Use class-level variables, not local
            if (imageProfile != null && userName != null) { // 🔧 CHANGED
                Intent intent = new Intent(Profile.this, showImage.class);
                intent.putExtra("IMAGE", imageProfile); // 🔧 CHANGED
                intent.putExtra("name", userName);      // 🔧 CHANGED
                startActivity(intent);
            } else {
                Toast.makeText(Profile.this, "User data not available", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Helper method to show error message
    private void showError(String message) {
        Toast.makeText(Profile.this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateName(String name) {
        // Update the user name in Firestore
        firestore.collection("Users").document(firebaseUser.getUid())
                .update("username", name)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Show success message
                        Toast.makeText(Profile.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                        binding.nameProfile.setText(name);
                        userName = name;
                    } else {
                        // Show error message
                        showError("Failed to update username");
                    }
                });
    }

    private void updateBio(String bio) {
        // Update the user bio in Firestore
        firestore.collection("Users").document(firebaseUser.getUid())
                .update("bio", bio)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Show success message
                        Toast.makeText(Profile.this, "Bio updated successfully", Toast.LENGTH_SHORT).show();
                        binding.bio.setText(bio);
                    } else {
                        // Show error message
                        showError("Failed to update bio");
                    }
                });
    }

    private void showDialogSignOut() {
        // Create sign-out confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to sign out?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> signOut())
                .setNegativeButton("No", (dialog, id) -> dialog.cancel())
                .create()
                .show();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Profile.this, SplachActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish();
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select image"), IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadToFirebase();
        }

        if (requestCode == 440 && resultCode == RESULT_OK) {
            if (imageUri != null) {
                uploadToFirebase();
            }
        }
    }

    private String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadToFirebase() {
        if (imageUri != null) {
            progressDialog.setMessage("Please Wait");
            progressDialog.show();

            StorageReference reference = FirebaseStorage.getInstance().getReference()
                    .child("ImageProfile/" + System.currentTimeMillis() + "." + getFileExtention(imageUri));

            reference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        if (downloadUri != null) {
                            String sdownload_url = downloadUri.toString();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("imageProfile", sdownload_url);

                            firestore.collection("Users").document(firebaseUser.getUid())
                                    .update(hashMap)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(getApplicationContext(), "Upload Successfully", Toast.LENGTH_SHORT).show();
                                        getUserInfo();
                                        progressDialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getApplicationContext(), "Firestore Update Failed", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to Get Download URL", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        }
    }

    private void navigateBackToMainActivity() {
        // Navigate back to the main activity (or wherever you need)
        startActivity(new Intent(Profile.this, MainActivity.class));
        finish();
    }

}
