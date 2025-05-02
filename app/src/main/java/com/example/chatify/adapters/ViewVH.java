package com.example.chatify.adapters;

import android.app.Application;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.squareup.picasso.Picasso;

public class ViewVH extends RecyclerView.ViewHolder {

    ImageView iv;
    public TextView nameTv;
    TextView timeTv;

    public ViewVH(@NonNull View itemView) {
        super(itemView);
    }

    public void setUser(Application application, String name, String url, String time, String uid) {
        iv = itemView.findViewById(R.id.ivviewbs_item);
        timeTv = itemView.findViewById(R.id.timetvviewbs_item);
        nameTv = itemView.findViewById(R.id.nametvviewbs_item);

        nameTv.setText(name);
        timeTv.setText(time);
        Picasso.get().load(url).into(iv);
    }
}
