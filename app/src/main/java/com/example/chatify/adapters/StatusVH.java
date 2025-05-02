package com.example.chatify.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class StatusVH extends RecyclerView.ViewHolder {

    public TextView nameTv;
    ImageView statusIv;
    TextView timeTv;
    String urlResult, deleteResult, timeResult;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference statusRef, lastStatus;
    LinearLayout ll_ss;

    public StatusVH(@NonNull View itemView) {
        super(itemView);


    }

    public void fetchStatus(FragmentActivity application,
                            String key1, String key2,
                            String key3, String lastM
            , String name, String url,
                            String uid) {

        statusRef = database.getReference("Status");
        lastStatus = database.getReference("Laststatus");

        ll_ss = itemView.findViewById(R.id.ll_status_item);
        statusIv = itemView.findViewById(R.id.image_profile_item);
        nameTv = itemView.findViewById(R.id.tv_name_item);
        timeTv = itemView.findViewById(R.id.tv_message_item);

//        lastStatus.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void  onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.hasChild(uid)) {
//
//                    lastStatus.child(uid).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.exists()) {
//                                urlResult = snapshot.child("image").getValue().toString();
//                                timeResult = snapshot.child("time").getValue().toString();
//                                deleteResult = snapshot.child("delete").getValue().toString();
//
//                                Picasso.get().load(url).into(statusIv);
//                                timeTv.setText(timeResult != null ? timeResult : "No time");
//                                statusIv.setPadding(0, 0, 0, 0);
//                                nameTv.setText(name);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                } else {
//                    ll_ss.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(uid)) {
                    statusRef.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                urlResult = dataSnapshot1.child("image").getValue().toString();
                                timeResult = dataSnapshot1.child("time").getValue().toString();
                                deleteResult = dataSnapshot1.child("delete").getValue().toString();

                                Picasso.get().load(urlResult).into(statusIv);
                                timeTv.setText(timeResult);
                                statusIv.setPadding(0, 0, 0, 0);
                                nameTv.setText(name);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
