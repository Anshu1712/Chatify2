package com.example.chatify.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.example.chatify.chat.ChatActivity;
import com.example.chatify.model.Users;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Users> list;
    private Context context;

    public ContactAdapter(List<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_contact_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = list.get(position);
        holder.username.setText(user.getUsername());
        holder.desc.setText(user.getBio());

        Glide.with(context).load(user.getImageProfile()).into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ChatActivity.class)
                        .putExtra("userID", user.getUserID())
                        .putExtra("username", user.getUsername())
                        .putExtra("imageProfile", user.getImageProfile())
                        .putExtra("userPhone", user.getUserPhone())
                        .putExtra("bio", user.getBio())
                );
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView username, desc;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            imageView = itemView.findViewById(R.id.image_Profile1);
            username = itemView.findViewById(R.id.userName);
            desc = itemView.findViewById(R.id.userBio);
        }
    }
}
