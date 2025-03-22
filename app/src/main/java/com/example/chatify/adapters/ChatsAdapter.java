package com.example.chatify.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.example.chatify.model.chat.Chats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chats> list;
    private Context context;
    private static final int MSG_TEXT_LEFT = 0;
    private static final int MSG_TEXT_RIGHT = 1;
    private static final int MSG_IMAGE_LEFT = 2;
    private static final int MSG_IMAGE_RIGHT = 3;
    private static final int MSG_VOICE_LEFT = 4;
    private static final int MSG_VOICE_RIGHT = 5;

    private FirebaseUser firebaseUser;
    private MediaPlayer mediaPlayer;
    private ImageButton lastButton;
    private Chronometer lastChronometer;
    private Handler handler = new Handler();

    public ChatsAdapter(List<Chats> list, Context context) {
        this.list = list;
        this.context = context;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.mediaPlayer = new MediaPlayer();
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
            case MSG_VOICE_LEFT:
                return new ViewHolderVoiceLeft(inflater.inflate(R.layout.chat_item_voice_left, parent, false));
            case MSG_VOICE_RIGHT:
                return new ViewHolderVoiceRight(inflater.inflate(R.layout.chat_item_voice_right, parent, false));
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
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .into(((ViewHolderImageLeft) holder).imageMessage);
        } else if (holder instanceof ViewHolderImageRight && chat.getType().equals("IMAGE")) {
            Glide.with(context)
                    .load(chat.getUrl())
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .into(((ViewHolderImageRight) holder).imageMessage);
        }

        // ✅ Handle Voice Message
        else if ((holder instanceof ViewHolderVoiceLeft || holder instanceof ViewHolderVoiceRight)
                && chat.getType().equals("VOICE")) {
            setupVoicePlayer(holder, chat.getUrl());
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
            if (chat.getType().equals("TEXT")) return MSG_TEXT_RIGHT;
            if (chat.getType().equals("IMAGE")) return MSG_IMAGE_RIGHT;
            if (chat.getType().equals("VOICE")) return MSG_VOICE_RIGHT;
        } else {
            if (chat.getType().equals("TEXT")) return MSG_TEXT_LEFT;
            if (chat.getType().equals("IMAGE")) return MSG_IMAGE_LEFT;
            if (chat.getType().equals("VOICE")) return MSG_VOICE_LEFT;
        }
        throw new IllegalArgumentException("Unknown message type");
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

    // ✅ ViewHolder for Voice (Left - Receiver)
    public static class ViewHolderVoiceLeft extends RecyclerView.ViewHolder {
        ImageButton playButton;
        Chronometer timer;

        public ViewHolderVoiceLeft(@NonNull View itemView) {
            super(itemView);
            playButton = itemView.findViewById(R.id.btn_play_voiceLeft);
            timer = itemView.findViewById(R.id.txt_voice_durationLeft);
        }
    }

    // ✅ ViewHolder for Voice (Right - Sender)
    public static class ViewHolderVoiceRight extends RecyclerView.ViewHolder {
        ImageButton playButton;
        Chronometer timer;

        public ViewHolderVoiceRight(@NonNull View itemView) {
            super(itemView);
            playButton = itemView.findViewById(R.id.btn_play_voice);
            timer = itemView.findViewById(R.id.txt_voice_duration);
        }
    }

    // ✅ MediaPlayer logic with Chronometer
    private void setupVoicePlayer(RecyclerView.ViewHolder holder, String url) {
        ImageButton playButton;
        Chronometer timer;

        if (holder instanceof ViewHolderVoiceLeft) {
            playButton = ((ViewHolderVoiceLeft) holder).playButton;
            timer = ((ViewHolderVoiceLeft) holder).timer;
        } else {
            playButton = ((ViewHolderVoiceRight) holder).playButton;
            timer = ((ViewHolderVoiceRight) holder).timer;
        }

        playButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                timer.stop();
                playButton.setImageResource(R.drawable.baseline_play_arrow_24);
            } else {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(context, Uri.parse(url));
                    mediaPlayer.prepare();

                    timer.setBase(SystemClock.elapsedRealtime());
                    timer.start();

                    mediaPlayer.start();
                    playButton.setImageResource(R.drawable.baseline_pause_24);

                    mediaPlayer.setOnCompletionListener(mp -> {
                        playButton.setImageResource(R.drawable.baseline_play_arrow_24);
                        timer.stop();
                        timer.setBase(SystemClock.elapsedRealtime());
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
