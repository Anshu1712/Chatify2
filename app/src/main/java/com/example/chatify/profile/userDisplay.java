package com.example.chatify.profile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.example.chatify.adapters.showImage;
import com.example.chatify.model.ChatListModel;

import java.util.Objects;

public class userDisplay {

    private final Context context;

    public userDisplay(Context context, ChatListModel chatListModel) {
        this.context = context;
        initialize(chatListModel);
    }

    private void initialize(ChatListModel chatListModel) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_user_display);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);

        ImageView btnChat = dialog.findViewById(R.id.btnChat);
        ImageView btnVoice = dialog.findViewById(R.id.btnCall);
        ImageView btnVideo = dialog.findViewById(R.id.btnVCall);
        ImageView btnOther = dialog.findViewById(R.id.btnOther);
        ImageView profile = dialog.findViewById(R.id.profile_image);
        TextView nameTv = dialog.findViewById(R.id.tv_name);

        nameTv.setText(chatListModel.getUserName());
        Glide.with(context).load(chatListModel.getUrlProfile()).into(profile);

        // Show dialog
        dialog.show();

        // Open full image view when profile clicked
        profile.setOnClickListener(v -> {
            Intent intent = new Intent(context, showImage.class);
            intent.putExtra("IMAGE", chatListModel.getUrlProfile());
            intent.putExtra("name", chatListModel.getUserName());
            context.startActivity(intent);
            dialog.dismiss();
        });
    }
}
