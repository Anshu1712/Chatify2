package com.example.chatify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.model.chat.Chats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chats> list;
    private Context context;
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private FirebaseUser firebaseUser;

    public ChatsAdapter(List<Chats> list, Context context) {
        this.list = list;
        this.context = context;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // ✅ Initialize once
    }

    public void setList(List<Chats> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolderLeft(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolderRight(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chats chat = list.get(position);

        if (holder instanceof ViewHolderLeft) {
            ((ViewHolderLeft) holder).textMessage.setText(chat.getTextMessage());
        } else if (holder instanceof ViewHolderRight) {
            ((ViewHolderRight) holder).textMessage.setText(chat.getTextMessage());
        }
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0; // ✅ Prevent potential NullPointerException
    }

    @Override
    public int getItemViewType(int position) {
        if (firebaseUser != null && list.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT; // ✅ Sender's message (right side)
        } else {
            return MSG_TYPE_LEFT;  // ✅ Receiver's message (left side)
        }
    }

    // ✅ Separate ViewHolder for Left Messages (Received)
    public static class ViewHolderLeft extends RecyclerView.ViewHolder {
        TextView textMessage;

        public ViewHolderLeft(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.txtLift); // ✅ Ensure this ID is correct in chat_item_left.xml
        }
    }

    // ✅ Separate ViewHolder for Right Messages (Sent)
    public static class ViewHolderRight extends RecyclerView.ViewHolder {
        TextView textMessage;

        public ViewHolderRight(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.txtRight); // ✅ Ensure this ID is correct in chat_item_right.xml
        }
    }
}