package com.example.chatify.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatify.R;
import com.example.chatify.adapters.ChatListAdapter;
import com.example.chatify.databinding.FragmentChatBinding;
import com.example.chatify.model.ChatListModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class chat extends Fragment {
    private static final String TAG = "Fragment_Chat";

    // Declare Firebase instances and UI elements
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private FirebaseFirestore firestore;
    private Handler handler = new Handler();
    private ChatListAdapter adapter;
    private List<ChatListModel> list;
    private ArrayList<String> allUserID;
    private FragmentChatBinding binding;

    public chat() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false);

        // Initialize list and allUserID
        list = new ArrayList<>();
        allUserID = new ArrayList<>();

        // Set the layout manager for the RecyclerView. The LinearLayoutManager arranges the items in a vertical list.
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatListAdapter(list, getContext());
        binding.recyclerView.setAdapter(adapter);

        // Initialize Firebase instances
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        // Start fetching chat list data if user is logged in
        if (firebaseUser != null) {
            getChatList();
        }

        // Return the root view of the fragment. This will be displayed on the screen.
        return binding.getRoot();
    }

    // Method to get chat list from Firebase Realtime Database
    private void getChatList() {
        // Hide progress and show invite friends section if there are no chats
        binding.progressCircular.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE); // Hide chat list
        binding.inInvite.setVisibility(View.GONE); // Show invite friends section

        reference.child("ChatList").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        list.clear();
                        allUserID.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String userID = Objects.requireNonNull(snapshot.child("chatid").getValue()).toString();
                            Log.d(TAG, "onDataChange: userid" + userID);

                            allUserID.add(userID);
                        }

                        // If no chat data is found, show the invite section and hide progress
                        if (allUserID.isEmpty()) {
                            binding.recyclerView.setVisibility(View.GONE); // Hide chat list
                            binding.progressCircular.setVisibility(View.VISIBLE); // Show invite friends section
                        } else {
                            getUserInfo();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: " + error.getMessage());
                    }
                });
    }

    // Method to fetch user info for all users in the chat list
    private void getUserInfo() {
        binding.progressCircular.setVisibility(View.VISIBLE); // Show progress while fetching user data
        binding.recyclerView.setVisibility(View.VISIBLE); // Show the RecyclerView once user data is fetched
        binding.inInvite.setVisibility(View.GONE); // Hide invite section once chat data exists

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (String userId : allUserID) {
                    firestore.collection("Users").document(userId).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Log.d(TAG, "onSuccess: add " + documentSnapshot.getString("username"));
                                    try {
                                        // Create a new ChatListModel object for each user
                                        ChatListModel chat = new ChatListModel(
                                                documentSnapshot.getString("userID"),
                                                documentSnapshot.getString("username"),
                                                "This is description..",
                                                getCurrentTime(),
                                                documentSnapshot.getString("imageProfile"),
                                                documentSnapshot.getString("userPhone"),
                                                documentSnapshot.getString("bio"),
                                                documentSnapshot.getString("unSeen"),
                                                documentSnapshot.getString("0"),
                                                documentSnapshot.getString("lastM")
                                        );
                                        list.add(chat);
                                    } catch (Exception e) {
                                        Log.d(TAG, "onSuccess: " + e.getMessage());
                                    }
                                    // Notify the adapter that new data has been added
                                    if (adapter != null) {
                                        adapter.notifyItemInserted(list.size() - 1);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                }
                            });
                }

                // After data is loaded, hide progress and show chat list
                binding.progressCircular.setVisibility(View.GONE); // Hide progress
            }
        });
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }



    // Method to handle invite friend button click
    public void onInviteFriendClicked(View view) {
        // Handle the logic for inviting a friend, such as opening a contact picker
        Log.d(TAG, "Invite Friend button clicked");
    }
}
