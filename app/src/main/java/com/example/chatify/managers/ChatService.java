package com.example.chatify.managers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chatify.interfaces.OnReadChatCallBack;
import com.example.chatify.model.chat.Chats;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatService {

    private Context context;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private String receiverID;

    public ChatService(Context context, String receiverID) {
        this.context = context;
        this.receiverID = receiverID;
    }

    public void readChatData(OnReadChatCallBack onCallBack) {
        reference.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Chats> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chats chats = snapshot.getValue(Chats.class);
                    if (chats != null &&
                            ((chats.getSender().equals(firebaseUser.getUid()) && chats.getReceiver().equals(receiverID)) ||
                                    (chats.getSender().equals(receiverID) && chats.getReceiver().equals(firebaseUser.getUid())))) {
                        list.add(chats);
                    }
                }
                // Send data to ChatActivity
                onCallBack.onReadSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onCallBack.onReadFailed();
                Log.e("ChatError", "Database Error: " + error.getMessage());
            }
        });
    }

    public void sendTextMsg(String text) {

        Chats chats = new Chats(getCurrentDate(), text, "", "TEXT", firebaseUser.getUid(), receiverID,"");

        reference.child("Chats").push().setValue(chats)
                .addOnSuccessListener(unused -> Log.d("Send", "Message Sent"))
                .addOnFailureListener(e -> Log.d("Send", "Message Failed: " + e.getMessage()));

        DatabaseReference chatRef1 = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(receiverID);
        chatRef1.child("chatid").setValue(receiverID);

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(receiverID)
                .child(firebaseUser.getUid());
        chatRef2.child("chatid").setValue(firebaseUser.getUid());
    }

    public void setImage(String imageUri) {
        Chats chats = new Chats(getCurrentDate(), "", imageUri, "IMAGE", firebaseUser.getUid(), receiverID, "");

        reference.child("Chats").push().setValue(chats)
                .addOnSuccessListener(unused -> Log.d("Send", "Message Sent"))
                .addOnFailureListener(e -> Log.d("Send", "Message Failed: " + e.getMessage()));

        DatabaseReference chatRef1 = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(receiverID);
        chatRef1.child("chatid").setValue(receiverID);

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(receiverID)
                .child(firebaseUser.getUid());
        chatRef2.child("chatid").setValue(firebaseUser.getUid());
    }

    public String getCurrentDate() {
        String timestamp = new SimpleDateFormat("dd-MM-yyyy, hh:mm a").format(Calendar.getInstance().getTime());

        return timestamp;
    }

    public void sendVoice(String audioPath) {
        Uri uriAudio = Uri.fromFile(new File(audioPath));
        final StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("Chats/Voice/" + System.currentTimeMillis());
        audioRef.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot audioSnapshot) {
                Task<Uri> uriTask = audioSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                Uri downloadUrl = uriTask.getResult();
                String voiceUrl = String.valueOf(downloadUrl);

                Chats chats = new Chats(getCurrentDate(), "", voiceUrl, "VOICE", firebaseUser.getUid(), receiverID,"");

                reference.child("Chats").push().setValue(chats)
                        .addOnSuccessListener(unused -> Log.d("Send", "Message Sent"))
                        .addOnFailureListener(e -> Log.d("Send", "Message Failed: " + e.getMessage()));

                DatabaseReference chatRef1 = FirebaseDatabase.getInstance()
                        .getReference("ChatList")
                        .child(firebaseUser.getUid())
                        .child(receiverID);
                chatRef1.child("chatid").setValue(receiverID);

                DatabaseReference chatRef2 = FirebaseDatabase.getInstance()
                        .getReference("ChatList")
                        .child(receiverID)
                        .child(firebaseUser.getUid());
                chatRef2.child("chatid").setValue(firebaseUser.getUid());
            }
        });
    }
}
