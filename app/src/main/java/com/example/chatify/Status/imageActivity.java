package com.example.chatify.Status;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatify.MainActivity;
import com.example.chatify.R;
import com.example.chatify.fragment.Fragment_Status;
import com.example.chatify.model.StatusModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class imageActivity extends AppCompatActivity {

    ImageView imageView;
    String uid;
    EditText captionEt;
    FloatingActionButton sendBtn;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference statusRef, lastStatus;
    StatusModel model;
    String imageUri;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image);

        model = new StatusModel();
        imageView = findViewById(R.id.iv_ss_image);
        captionEt = findViewById(R.id.caption_ss_image);
        sendBtn = findViewById(R.id.btn_send_image);
        pb = findViewById(R.id.pb_ss_image);
        statusRef = database.getReference("Status");
        lastStatus = database.getReference("Laststatus");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        uid = user.getUid();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            imageUri = bundle.getString("u");

            if (imageUri != null && !imageUri.isEmpty()) {
                Picasso.get().load(imageUri).into(imageView);
            } else {
                Toast.makeText(this, "Image URI not received!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "No data received!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
            }
        });
    }

    private void saveStatus() {
        if (imageUri == null || imageUri.isEmpty()) {
            Toast.makeText(this, "Image URI is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        pb.setVisibility(View.VISIBLE);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("statusimages");

        final StorageReference reference = storageReference.child(System.currentTimeMillis() + ".jpg");

        UploadTask uploadTask = reference.putFile(Uri.parse(imageUri));
        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    Calendar callForDate = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM");
                    final String saveDate = currentDate.format(callForDate.getTime());

                    Calendar callForTime = Calendar.getInstance();
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:a");
                    final String saveTime = currentTime.format(callForTime.getTime());

                    model.setDelete(String.valueOf(System.currentTimeMillis()));
                    model.setImage(downloadUri.toString());
                    model.setCaption(captionEt.getText().toString().trim());
                    model.setUid(uid);
                    model.setTime(saveDate + " " + saveTime);

                    FirebaseAuth auth = FirebaseAuth.getInstance();

                    String uid = auth.getCurrentUser().getUid().toString();
                    FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String name = document.getString("username");
                                    Log.e("FireStore", "name : "+name);
                                    model.setName(name);
                                    String key = statusRef.push().getKey();
                                    statusRef.child(uid).child(key).setValue(model);
                                    lastStatus.child(uid).setValue(model);

                                    pb.setVisibility(View.GONE);
                                    Toast.makeText(imageActivity.this, "Status Uploaded you know", Toast.LENGTH_SHORT).show();


                                    // Go back to MainActivity and switch to Status tab
                                    new Handler().postDelayed(() -> {
                                        Intent intent = new Intent(imageActivity.this, MainActivity.class);
                                        intent.putExtra("goToStatus", true);
                                        startActivity(intent);
                                        finish();
                                    }, 1000);
                                } else {
                                    Log.e("FireStore", "document missing : "+true);
                                }
                            } else {
                                Log.e("FireStore", "failed");
                            }
                        }
                    });

                    //pb.setVisibility(View.GONE);
                    //Toast.makeText(imageActivity.this, "Status Uploaded", Toast.LENGTH_SHORT).show();

                } else {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(imageActivity.this, "Upload failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
