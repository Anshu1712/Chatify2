package com.example.chatify.chat;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
import com.example.chatify.R;
import com.example.chatify.adapters.ChatsAdapter;
import com.example.chatify.databinding.ActivityChatBinding;
import com.example.chatify.dialog.DialogReviewSendImage;
import com.example.chatify.interfaces.OnReadChatCallBack;
import com.example.chatify.managers.ChatService;
import com.example.chatify.model.chat.Chats;
import com.example.chatify.profile.UserProfileActivity;
import com.example.chatify.service.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_CORD_PERMISSION = 332;
    private static final int IMAGE_GALLERY_REQUEST = 111;
    private static final int PERMISSION_CODE = 101;
    private ActivityChatBinding binding;
    private String receiverID;
    private String senderID;
    private String audioPath, userProfile, userName, userPhone, UserBio;
    private ChatsAdapter adapter;
    private List<Chats> list = new ArrayList<>();
    private boolean isActionShown = false;
    DatabaseReference lastSeenRef, seenStatus;
    String key1, key2;
    private final android.os.Handler typingHandler = new android.os.Handler();
    private Runnable typingTimeout = () -> lastSeenRef.child(senderID).setValue("Online");
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private ChatService chatService;
    private String saveTime;
    private Uri imageUri;
    private MediaRecorder mediaRecorder;
    Chats chats; // Declare at the top


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        lastSeenRef = database.getReference("online");
        seenStatus = database.getReference("seenStatus");

        Calendar time1 = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveTime = currentTime.format(time1.getTime());

        // Get Chats object from Intent (make sure you passed it correctly)
        chats = (Chats) getIntent().getSerializableExtra("online");

        // Optional: Log for debugging
        if (chats == null) {
            Log.e("ChatActivity", "Chats object is null in onCreate()");
        }

        onlineUser();
        initialize();
        initBtnClick();
        readChats();
        getData();

    }

    private void initialize() {

        senderID = FirebaseAuth.getInstance().getUid();  // get sender ID
        Intent intent = getIntent();
        userName = intent.getStringExtra("username");
        receiverID = intent.getStringExtra("userID");
        userProfile = intent.getStringExtra("imageProfile");
        userPhone = intent.getStringExtra("userPhone");
        UserBio = intent.getStringExtra("bio");

        chatService = new ChatService(this, receiverID);


        // Set username and profile image
        if (receiverID != null) {
            binding.userName.setText(userName);
            Glide.with(this).load(userProfile).into(binding.imageProfile);
        }


        binding.etd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                typingHandler.removeCallbacks(typingTimeout);
                if (TextUtils.isEmpty(s.toString().trim())) {
                    lastSeenRef.child(senderID).setValue("Online");
                    binding.sentBtn.setVisibility(View.INVISIBLE);
                    binding.recordButton.setVisibility(View.VISIBLE);
                } else {
                    lastSeenRef.child(senderID).setValue("is typing...");
                    binding.sentBtn.setVisibility(View.VISIBLE);
                    binding.recordButton.setVisibility(View.INVISIBLE);
                    typingHandler.postDelayed(typingTimeout, 2000); // Reset to online after 2s
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Optional
            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatsAdapter(list, this);
        binding.recyclerView.setAdapter(adapter);

        setUpRecordButton();

        binding.recordButton.setRecordView(binding.recordView);
        binding.recordView.setSlideToCancelTextColor(Color.parseColor("#ff0000"));
        binding.recordView.setRecordButtonGrowingAnimationEnabled(true);

        initializeZego();

        binding.video.setOnClickListener((v) -> {
            binding.video.setIsVideoCall(true);
            binding.video.setResourceID("zego_chat_9");  // don't touch it !!!!!!!!
            binding.video.setInvitees(Collections.singletonList(new ZegoUIKitUser(receiverID)));
        });
    }

    private void getData() {
        lastSeenRef.child(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userStatus = snapshot.getValue(String.class);
                    binding.lastSeenTv.setText(userStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting user status", error.toException());
            }
        });
    }

    private void onlineUser() {
        if (chats != null && chats.sender != null) {
            String senderId = chats.sender;

            // Your existing logic here...
            Log.d("ChatActivity", "Sender ID: " + senderId);
            // Example Firebase status update logic (if any)
        } else {
            Log.e("ChatActivity", "Chats object or sender is null in onlineUser()");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        lastSeenRef.child(senderID).onDisconnect()
                .setValue("Last seen " + new SimpleDateFormat("hh:mm a").format(Calendar.getInstance().getTime()));

    }

    @Override
    protected void onStop() {
        super.onStop();
        String currentTime = new SimpleDateFormat("hh:mm a").format(Calendar.getInstance().getTime());
        lastSeenRef.child(senderID).setValue("Last seen " + currentTime);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        lastSeenRef.child(senderID).setValue("Last seen " + saveTime);
    }


    private void setUpRecordButton() {
        binding.recordButton.setRecordView(binding.recordView);
        binding.recordView.setSlideToCancelTextColor(Color.RED);
        binding.recordView.setRecordButtonGrowingAnimationEnabled(true);

        binding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                if (checkPermissions()) {
                    hideChatControls();
                    lastSeenRef.child(senderID).setValue("is recording...");
                    startRecording();
                    vibrate(100);
                } else {
                    requestPermissions();
                }
            }


            @Override
            public void onCancel() {
                stopRecording(false);
                lastSeenRef.child(senderID).setValue("Online");

            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                showChatControls();
                stopRecording(true);
                lastSeenRef.child(senderID).setValue("Online");

            }

            @Override
            public void onLessThanSecond() {
                showChatControls();
            }

            @Override
            public void onLock() {
            }

        });

        binding.recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                showChatControls();
            }
        });
    }

    private void hideChatControls() {
        binding.emoji.setVisibility(View.INVISIBLE);
        binding.etd.setVisibility(View.INVISIBLE);
        binding.attachment.setVisibility(View.INVISIBLE);
        binding.camera.setVisibility(View.INVISIBLE);
    }

    private void showChatControls() {
        binding.emoji.setVisibility(View.VISIBLE);
        binding.etd.setVisibility(View.VISIBLE);
        binding.attachment.setVisibility(View.VISIBLE);
        binding.camera.setVisibility(View.VISIBLE);
    }


    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d",
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    private void readChats() {
        chatService.readChatData(new OnReadChatCallBack() {
            @Override
            public void onReadSuccess(List<Chats> chatList) {
                adapter.setList(chatList);
                adapter.notifyDataSetChanged(); // Ensure the adapter updates properly

                if (chatList.size() > 0) {
                    binding.recyclerView.smoothScrollToPosition(chatList.size() - 1);
                }
            }

            @Override
            public void onReadFailed() {
                Log.d(TAG, "onReadFailed: Failed to read chat messages");
            }
        });
    }

    private void initBtnClick() {
        binding.sentBtn.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(binding.etd.getText().toString()) && receiverID != null) {
                // Sending the text message
                chatService.sendTextMsg(binding.etd.getText().toString());
                incrementMessageCount(FirebaseAuth.getInstance().getCurrentUser().getUid(), receiverID);
                // Clear the input field after sending
                binding.etd.setText("");
            } else {
                Log.e(TAG, "Receiver ID is null or message text is empty");
            }
        });


        binding.backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatActivity.this,
                        UserProfileActivity.class).putExtra("userID",
                                receiverID).putExtra("userProfile", userProfile).
                        putExtra("username", userName).putExtra("userPhone", userPhone)
                        .putExtra("bio", UserBio));
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isActionShown) {
                    binding.layoutAction.setVisibility(View.GONE);
                    isActionShown = false;
                } else {
                    binding.layoutAction.setVisibility(View.VISIBLE);
                    isActionShown = true;
                }
            }
        });

        binding.gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select image"), IMAGE_GALLERY_REQUEST);
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
    }


    private void startRecording() {
        String fileName = UUID.randomUUID().toString() + "_audio.m4a";
        File audioFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName);

        audioPath = audioFile.getAbsolutePath();
        mediaRecorder = new MediaRecorder();

        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioPath);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            Log.e(TAG, "Recording failed", e);
        }
    }

    private void stopRecording(boolean send) {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            if (send) {
                chatService.sendVoice(audioPath);
            }
        }
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping media recorder: " + e.getMessage());
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void vibrate(long milliseconds) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(milliseconds);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                reviewImage(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void reviewImage(Bitmap bitmap) {
        new DialogReviewSendImage(ChatActivity.this, bitmap).show(new DialogReviewSendImage.OnCallBack() {
            @Override
            public void onButtonSendClick() {
                if (imageUri != null) {
                    final ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
                    progressDialog.setMessage("Sending Image");
                    progressDialog.show();
                    binding.layoutAction.setVisibility(View.GONE);
                    isActionShown = false;

                    new FirebaseService(ChatActivity.this).upLoadImageToFireBaseStorage(imageUri, new FirebaseService.OnCallBack() {
                        @Override
                        public void onUploadSuccess(String imageUrl) {
                            chatService.setImage(imageUrl);
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onUploadFailed(Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    private void incrementMessageCount(String senderId, String receiverId) {
        DatabaseReference countRef = FirebaseDatabase.getInstance()
                .getReference("MessageCount")
                .child(senderId)
                .child(receiverId)
                .child("count");

        countRef.get().addOnSuccessListener(snapshot -> {
            long currentCount = 0;
            if (snapshot.exists()) {
                currentCount = snapshot.getValue(Long.class);
            }
            countRef.setValue(currentCount + 1); // Increment the message count
        }).addOnFailureListener(e -> {
            Log.e("CountError", "Failed to read or update count: " + e.getMessage());
        });
    }

    void initializeZego() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            long appID = 852249356;
            String appSign = "daa0a07354ee30c79fd1380c8f21fac1ad3b8e08470cc3236000c7b38987b838";

            String userID = firebaseUser.getUid();
            String username = userID;

            ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

            Log.e("ZegoInit", "Firebase user is not null.");
            ZegoUIKitPrebuiltCallService.init(getApplication(), appID, appSign, userID, username, callInvitationConfig);
        } else {
            Log.e("ZegoInit", "Firebase user is null. Please log in first.");
        }
    }

}
