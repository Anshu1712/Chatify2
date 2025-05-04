package com.example.chatify.adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.example.chatify.chat.ChatActivity;
import com.example.chatify.model.ChatListModel;
import com.example.chatify.profile.userDisplay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {

    private List<ChatListModel> list;
    private Context context;
    private DatabaseReference databaseReference;

    public ChatListAdapter(List<ChatListModel> list, Context context) {
        this.list = list;
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_chat, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChatListModel chatList = list.get(position);

        holder.tvName.setText(chatList.getUserName());
        holder.tvDate.setText(chatList.getDate());
        holder.tvMessage.setText(chatList.getDescription());

        if (chatList.getmCount() > 0) {
            holder.unreadCount.setVisibility(View.VISIBLE);
            holder.unreadCount.setText(String.valueOf(chatList.getmCount()));
        } else {
            holder.unreadCount.setVisibility(View.GONE);
        }

        Glide.with(context).load(chatList.getUrlProfile()).into(holder.profile);

        holder.itemView.setOnClickListener(view -> {
            // Reset message count when the chat is opened
            resetMessageCount(chatList.getUserID());

            context.startActivity(new Intent(context, ChatActivity.class)
                    .putExtra("userID", chatList.getUserID())
                    .putExtra("username", chatList.getUserName())
                    .putExtra("imageProfile", chatList.getUrlProfile())
                    .putExtra("userPhone", chatList.getUserPhone())
                    .putExtra("bio", chatList.getUserBio()));
        });

        holder.profile.setOnClickListener(v -> new userDisplay(context, chatList));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvMessage, tvDate, unreadCount;
        private CircleImageView profile;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.messageTime);
            tvName = itemView.findViewById(R.id.chatName);
            tvMessage = itemView.findViewById(R.id.lastMessage);
            unreadCount = itemView.findViewById(R.id.m_notv);
            profile = itemView.findViewById(R.id.image_profile);
        }
    }

    // Increment unread message count for a specific user
//    public void incrementMessageCount(String userId) {
//        for (int i = 0; i < list.size(); i++) {
//            ChatListModel model = list.get(i);
//            if (model.getUserID().equals(userId)) {
//                // Start the count from 1 if it's the first message after reset
//                int oldCount = model.getmCount();
//                int newCount = (oldCount == 0) ? 1 : oldCount + 1;  // If it's the first message after reset, start from 1
//
//                model.setmCount(newCount);
//
//                // Update the message count in Firebase
//                databaseReference.child("MessageCount")
//                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                        .child(userId)
//                        .child("count")
//                        .setValue(newCount);
//
//                // Notify the adapter to update the UI
//                notifyItemChanged(i);
//                break;
//            }
//        }
//    }


    // Reset unread message count to zero when chat is opened
    public void resetMessageCount(String userId) {
        for (int i = 0; i < list.size(); i++) {
            ChatListModel model = list.get(i);
            if (model.getUserID().equals(userId)) {
                // Reset the count to 0 in the model
                model.setmCount(0);

                // Update the message count to 0 in Firebase
                String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                databaseReference.child("MessageCount")
                        .child(currentUserUid)
                        .child(userId)
                        .child("count")
                        .setValue(0)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Message count reset successfully in Firebase"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error resetting message count: " + e.getMessage()));

                // Notify the adapter to refresh the UI
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void incrementMessageCount(String userId) {
        for (int i = 0; i < list.size(); i++) {
            ChatListModel model = list.get(i);
            if (model.getUserID().equals(userId)) {
                // Start the count from 1 if it's the first message after reset
                int oldCount = model.getmCount();
                int newCount = (oldCount == 0) ? 1 : oldCount + 1;  // If it's the first message after reset, start from 1

                // Set the new message count in the model
                model.setmCount(newCount);

                // Update the message count in Firebase
                String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                databaseReference.child("MessageCount")
                        .child(currentUserUid)
                        .child(userId)
                        .child("count")
                        .setValue(newCount)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Message count updated successfully in Firebase"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating message count: " + e.getMessage()));

                // Notify the adapter to refresh the UI
                notifyItemChanged(i);
                break;
            }
        }
    }


    // Method to update the message count when a new message arrives
    public void onNewMessageReceived(String userId) {
        incrementMessageCount(userId);
    }
}
