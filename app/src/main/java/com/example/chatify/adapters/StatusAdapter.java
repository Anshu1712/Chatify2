package com.example.chatify.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.Status.ShowStatus;
import com.example.chatify.model.StatusModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    private Context context;
    private String admin_name;
    private List<StatusModel> list;

    private String ans;
    public StatusAdapter(Context context, List<StatusModel> list, String admin, String admin_image) {
        this.context = context;
        this.list = list;
        this.ans = admin_image;
        this.admin_name = admin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.status_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatusModel statusModel = list.get(position);
        String name = statusModel.getName();
        String time = statusModel.getTime();
        Long delete = statusModel.getDelete();
        String uid =statusModel.getUid();
        String caption = statusModel.getCaption();
        String image = statusModel.getImage();

        holder.instance.setText(statusModel.getTime());
        Picasso.get().load(statusModel.getImage()).into(holder.circleImageView);
        holder.name.setText(statusModel.getName());



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("status tag 44", "on click holder item: "+statusModel.getName());
                v.getContext().startActivity(new Intent(context, ShowStatus.class)
                        .putExtra("name",name)
                        .putExtra("admin",admin_name)
                        .putExtra("time",time)
                        .putExtra("delete", delete)
                        .putExtra("uid", uid)
                        .putExtra("caption", caption)
                                .putExtra("uns",ans)
                        .putExtra("image", image));

                Log.d("tanjiro", "onClick: ddd "+ ans);


            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView circleImageView;
        private TextView name, instance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.image_profile_item);
            name = itemView.findViewById(R.id.tv_name_item);
            instance = itemView.findViewById(R.id.tv_message_item);
        }
    }
}
