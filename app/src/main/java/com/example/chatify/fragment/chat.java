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
    private void getUserInfo() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.inInvite.setVisibility(View.GONE);

        list.clear(); // Clear old data
        adapter.notifyDataSetChanged();

        for (String userId : allUserID) {
            firestore.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Step 1: Fetch last message
                            reference.child("Chats").orderByChild("timestamp")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String lastMessage = "";
                                            for (DataSnapshot snap : snapshot.getChildren()) {
                                                String sender = snap.child("sender").getValue(String.class);
                                                String receiver = snap.child("receiver").getValue(String.class);

                                                if ((sender != null && receiver != null) &&
                                                        ((sender.equals(firebaseUser.getUid()) && receiver.equals(userId)) ||
                                                                (receiver.equals(firebaseUser.getUid()) && sender.equals(userId)))) {
                                                    lastMessage = snap.child("message").getValue(String.class);
                                                }
                                            }

                                            // Step 2: Create ChatListModel with dummy count (will update in real-time)
                                            ChatListModel chat = new ChatListModel(
                                                    userId,
                                                    documentSnapshot.getString("username"),
                                                    "",
                                                    getCurrentTime(),
                                                    documentSnapshot.getString("imageProfile"),
                                                    documentSnapshot.getString("userPhone"),
                                                    documentSnapshot.getString("bio"),
                                                    documentSnapshot.getString("unSeen"),
                                                    0, // Initial count
                                                    lastMessage
                                            );

                                            list.add(chat);
                                            int index = list.size() - 1;
                                            adapter.notifyItemInserted(index);

                                            // Step 3: Attach real-time listener to message count
                                            reference.child("MessageCount")
                                                    .child(userId)
                                                    .child(firebaseUser.getUid())
                                                    .child("count")
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            int count = dataSnapshot.exists() ? dataSnapshot.getValue(Integer.class) : 0;

                                                            // Update model and notify adapter
                                                            chat.setmCount(count);
                                                            adapter.notifyItemChanged(index);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Log.e(TAG, "Message count listener error: " + error.getMessage());
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "Chat fetch error: " + error.getMessage());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> Log.d(TAG, "User fetch failed: " + e.getMessage()));
        }

        binding.progressCircular.setVisibility(View.GONE);
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
