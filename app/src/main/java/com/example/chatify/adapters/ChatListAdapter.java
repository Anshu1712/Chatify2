package com.example.chatify.adapters;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {

    private List<ChatListModel> list;
    private Context context;

    public ChatListAdapter(List<ChatListModel> list, Context context) {
        this.list = list;
        this.context = context;
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
        Glide.with(context).load(chatList.getUrlProfile()).into(holder.profile);

        holder.itemView.setOnClickListener(view -> {
            context.startActivity(new Intent(context, ChatActivity.class)
                    .putExtra("userID", chatList.getUserID())
                    .putExtra("username", chatList.getUserName())
                    .putExtra("imageProfile", chatList.getUrlProfile())
                    .putExtra("userPhone", chatList.getUserPhone())
                    .putExtra("bio", chatList.getUserBio()));
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvMessage, tvDate;
        private CircleImageView profile;

        public Holder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.messageTime);
            tvName = itemView.findViewById(R.id.chatName);
            tvMessage = itemView.findViewById(R.id.lastMessage);
            profile = itemView.findViewById(R.id.image_profile);

        }
    }
}
