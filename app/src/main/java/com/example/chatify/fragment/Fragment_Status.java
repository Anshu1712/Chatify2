package com.example.chatify.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.example.chatify.Status.ShowStatus;
import com.example.chatify.Status.imageActivity;
import com.example.chatify.adapters.StatusVH;
import com.example.chatify.databinding.FragmentStatusBinding;
import com.example.chatify.model.ChatListModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class Fragment_Status extends Fragment implements View.OnClickListener {

    private FragmentStatusBinding binding;
    private Uri imageUri;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_GALLERY = 102;

    TextView tapToAddTv, myStatus;
    String uid, url, time, delete;

    private FirebaseDatabase database;
    private DatabaseReference statusRef, chatList;
    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;

    public Fragment_Status() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_status, container, false);

        database = FirebaseDatabase.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
            getProfile(uid);
        } else {
            showError("User not logged in");
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (uid == null) {
            showError("UID is null");
            return;
        }

        tapToAddTv = binding.tvMessage;
        myStatus = binding.tvName;
        statusRef = database.getReference("lastStatus");

        binding.statusRecycle.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.statusRecycle.setHasFixedSize(true);

        tapToAddTv.setOnClickListener(this);
        myStatus.setOnClickListener(this);
        statusRef.keepSynced(true);

        chatList = database.getReference("chat list").child(uid);

        checkPermission();
        fetchStatus();
    }

    private void checkPermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, REQUEST_STORAGE_PERMISSION);
        } else {
            fetchStatus();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchStatus();
            } else {
                Toast.makeText(getContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchStatus() {
        if (statusRef == null || uid == null) return;

        statusRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    url = snapshot.child("image").getValue().toString();
                    time = snapshot.child("time").getValue().toString();
                    delete = snapshot.child("delete").getValue().toString();

                    Picasso.get().load(url).into(binding.imageProfile);
                    tapToAddTv.setText(time != null ? time : "No time");
                    binding.imageProfile.setPadding(0, 0, 0, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to fetch status: " + error.getMessage());
            }
        });
    }

    private void getProfile(String userId) {
        firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String imageProfile = documentSnapshot.getString("imageProfile");
                    if (isAdded() && getContext() != null && imageProfile != null) {
                        Glide.with(getContext()).load(imageProfile).into(binding.imageProfile);
                    }
                })
                .addOnFailureListener(e -> showError("Failed to load user info"));
    }

    private void showError(String message) {
        Log.e("Fragment_Status", message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_message || v.getId() == R.id.tv_name) {
            openCameraBs();
        }
    }

    private void openCameraBs() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.camera_bs, null);
        bottomSheetDialog.setContentView(view);

        ImageView camera = view.findViewById(R.id.camera1);
        LinearLayout galleryLayout = view.findViewById(R.id.gallery1);

        camera.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openCamera();
        });

        galleryLayout.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openGallery();
        });

        bottomSheetDialog.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
                imageUri = data.getData();
                openImageActivity(imageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    Intent intent = new Intent(getActivity(), imageActivity.class);
                    intent.putExtra("bitmap", bitmap); // Make sure imageActivity can handle Bitmap
                    startActivity(intent);
                }
            }
        }
    }

    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ChatListModel> options =
                new FirebaseRecyclerOptions.Builder<ChatListModel>()
                        .setQuery(chatList, ChatListModel.class)
                        .build();

        FirebaseRecyclerAdapter<ChatListModel, StatusVH> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatListModel, StatusVH>(options) {
            @Override
            protected void onBindViewHolder(@NonNull StatusVH holder, int position, @NonNull ChatListModel model) {

                holder.fetchStatus(getActivity(), "", "", model.getDate(), model.getLastM(), model.getUserName(), model.getUrlProfile(), model.getUserID());

                String userId = getItem(position).getUserID();
                holder.nameTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ShowStatus.class);
                        intent.putExtra("uid", userId);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public StatusVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat, parent, false);
                return new StatusVH(view); // ✅ correct
            }
        };

    }

    private void openImageActivity(Uri uri) {
        Intent intent = new Intent(getActivity(), imageActivity.class);
        intent.putExtra("u", uri.toString());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
