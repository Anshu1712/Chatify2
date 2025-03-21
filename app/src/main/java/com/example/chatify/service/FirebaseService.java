package com.example.chatify.service;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseService {

    private Context context;

    public FirebaseService(Context context) {
        this.context = context;
    }

    public void upLoadImageToFireBaseStorage(Uri uri, OnCallBack onCallBack) {
        StorageReference riversRef = FirebaseStorage.getInstance().getReference()
                .child("ImageChats/" + System.currentTimeMillis() + "." + getFileExtention(uri));
        riversRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                Uri downloadUrl = uriTask.getResult();

                final String sdownload_Url = String.valueOf(downloadUrl);


                onCallBack.onUploadSuccess(sdownload_Url);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCallBack.onUploadFailed(e);
            }
        });
    }

    private String getFileExtention(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public interface OnCallBack {
        void onUploadSuccess(String imageUrl);

        void onUploadFailed(Exception e);

    }

}
