package com.example.chatify.contacts;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatify.R;
import com.example.chatify.adapters.ContactAdapter;
import com.example.chatify.databinding.ActivityAddContactBinding;
import com.example.chatify.model.Users;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddContact extends AppCompatActivity {

    private static final String TAG = "AddContact";
    private ActivityAddContactBinding binding;
    private List<Users> list = new ArrayList<>();
    private ContactAdapter adapter;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_contact);

        binding.contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            getContactList();
        }
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getContactList() {
        firestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                    String userID = snapshots.getString("userID");
                    String userName = snapshots.getString("username");
                    String imageUrl = snapshots.getString("imageProfile");
                    String desc = snapshots.getString("bio");

                    Users user = new Users();
                    user.setUserID(userID);
                    user.setUsername(userName);
                    user.setBio(desc);
                    user.setImageProfile(imageUrl);

                    if (userID != null && !userID.equals(firebaseUser.getUid())) {
                        list.add(user);
                    }
                }
                adapter = new ContactAdapter(list, AddContact.this);
                binding.contactsRecyclerView.setAdapter(adapter);
            }
        });
    }
}