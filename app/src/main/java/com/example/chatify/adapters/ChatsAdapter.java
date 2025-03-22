package com.example.chatify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.example.chatify.model.chat.Chats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chats> list;
    private Context context;
    private static final int MSG_TEXT_LEFT = 0;
    private static final int MSG_TEXT_RIGHT = 1;
    private static final int MSG_IMAGE_LEFT = 2;
    private static final int MSG_IMAGE_RIGHT = 3;
    private FirebaseUser firebaseUser;

    public ChatsAdapter(List<Chats> list, Context context) {
        this.list = list;
        this.context = context;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void setList(List<Chats> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case MSG_TEXT_LEFT:
                return new ViewHolderTextLeft(inflater.inflate(R.layout.chat_item_left, parent, false));
            case MSG_TEXT_RIGHT:
                return new ViewHolderTextRight(inflater.inflate(R.layout.chat_item_right, parent, false));
            case MSG_IMAGE_LEFT:
                return new ViewHolderImageLeft(inflater.inflate(R.layout.chat_item_image_left, parent, false));
            case MSG_IMAGE_RIGHT:
                return new ViewHolderImageRight(inflater.inflate(R.layout.chat_item_image_right, parent, false));
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chats chat = list.get(position);

        if (chat == null) return;

        // ✅ Handle Text Message
        if (holder instanceof ViewHolderTextLeft && chat.getType().equals("TEXT")) {
            ((ViewHolderTextLeft) holder).textMessage.setText(chat.getTextMessage());
        } else if (holder instanceof ViewHolderTextRight && chat.getType().equals("TEXT")) {
            ((ViewHolderTextRight) holder).textMessage.setText(chat.getTextMessage());
        }

        // ✅ Handle Image Message
        else if (holder instanceof ViewHolderImageLeft && chat.getType().equals("IMAGE")) {
            Glide.with(context)
                    .load(chat.getUrl())
                    .placeholder(R.drawable.person) // ✅ Add placeholder for smooth loading
                    .error(R.drawable.person)             // ✅ Add error image
                    .into(((ViewHolderImageLeft) holder).imageMessage);
        } else if (holder instanceof ViewHolderImageRight && chat.getType().equals("IMAGE")) {
            Glide.with(context)
                    .load(chat.getUrl())
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .into(((ViewHolderImageRight) holder).imageMessage);
        }
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (list == null || list.get(position) == null) {
            throw new IllegalArgumentException("Unknown message type");
        }

        Chats chat = list.get(position);

        if (firebaseUser != null && chat.getSender().equals(firebaseUser.getUid())) {
            return chat.getType().equals("TEXT") ? MSG_TEXT_RIGHT : MSG_IMAGE_RIGHT;
        } else {
            return chat.getType().equals("TEXT") ? MSG_TEXT_LEFT : MSG_IMAGE_LEFT;
        }
    }

    // ✅ ViewHolder for Text (Left - Receiver)
    public static class ViewHolderTextLeft extends RecyclerView.ViewHolder {
        TextView textMessage;

        public ViewHolderTextLeft(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.txtLift);
        }
    }

    // ✅ ViewHolder for Text (Right - Sender)
    public static class ViewHolderTextRight extends RecyclerView.ViewHolder {
        TextView textMessage;

        public ViewHolderTextRight(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.txtRight);
        }
    }

    // ✅ ViewHolder for Image (Left - Receiver)
    public static class ViewHolderImageLeft extends RecyclerView.ViewHolder {
        ImageView imageMessage;

        public ViewHolderImageLeft(@NonNull View itemView) {
            super(itemView);
            imageMessage = itemView.findViewById(R.id.image_chatLeft);
        }
    }

    // ✅ ViewHolder for Image (Right - Sender)
    public static class ViewHolderImageRight extends RecyclerView.ViewHolder {
        ImageView imageMessage;

        public ViewHolderImageRight(@NonNull View itemView) {
            super(itemView);
            imageMessage = itemView.findViewById(R.id.image_chat);
        }
    }
}
