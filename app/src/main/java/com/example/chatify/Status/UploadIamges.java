package com.example.chatify.Status;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UploadIamges extends AppCompatActivity {


    ImageView imageView;
    ImageView openCameraTv;
    private String currentPhotoPath;
    String uid;
    EditText captionEt;
    FloatingActionButton sendBtn;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference statusRef, lastStatus;
    StatusModel model;
    Bitmap bitmap;
    ProgressBar pb;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_iamges);

        model = new StatusModel();
        imageView = findViewById(R.id.iv_ss);
        openCameraTv = findViewById(R.id.textView4);
        captionEt = findViewById(R.id.caption_ss);
        sendBtn = findViewById(R.id.btn_send);
        pb = findViewById(R.id.pb_ss);
        statusRef = database.getReference("Status");
        lastStatus = database.getReference("Laststatus");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();


        openCameraTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fileName = "photo";
                File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try {

                    File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                    currentPhotoPath = imageFile.getAbsolutePath();
                    Uri imageUri = FileProvider.getUriForFile(UploadIamges.this, ".provider", imageFile);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, 1);
                } catch (Exception e) {
                    Toast.makeText(UploadIamges.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
            }
        });
    }

    private void saveStatus() {
        pb.setVisibility(View.VISIBLE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference = storage.getReference("statusimages");

        final StorageReference reference = storageReference.child(System.currentTimeMillis() + ".jpg");

        UploadTask uploadTask = reference.putBytes(data);
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
                    Toast.makeText(UploadIamges.this, "Status Uploaded", Toast.LENGTH_SHORT).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(UploadIamges.this, Fragment_Status.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 1000);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            imageView.setImageBitmap(bitmap);
        }
    }
}