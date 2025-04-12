package com.example.chatify.Status;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    Bitmap bitmap;
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
        uid = user.getUid();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            imageUri = bundle.getString("u");
            Picasso.get().load(imageUri).into(imageView);
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
            }
        });
    }

    private void saveStatus() {
        pb.setVisibility(View.VISIBLE);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference = storage.getReference("statusimages");

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

                    String key = statusRef.push().getKey();
                    statusRef.child(uid).child(key).setValue(model);


                    model.setDelete(String.valueOf(System.currentTimeMillis()));
                    model.setImage(downloadUri.toString());
                    model.setCaption(captionEt.getText().toString().trim());

                    model.setUid(uid);
                    model.setTime(saveDate + " " + saveTime);

                    lastStatus.child(uid).setValue(model);

                    pb.setVisibility(View.GONE);
                    Toast.makeText(imageActivity.this, "Status Uploaded", Toast.LENGTH_SHORT).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(imageActivity.this, Fragment_Status.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 1000);
                }
            }
        });
    }
}