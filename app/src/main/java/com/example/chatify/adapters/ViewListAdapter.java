package com.example.chatify.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.model.ViewListModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewListAdapter extends RecyclerView.Adapter<ViewListAdapter.ViewHolder> {
    private Context context;
    private List<ViewListModel> viewList;

    public ViewListAdapter(Context context, List<ViewListModel> viewList) {
        this.context = context;
        this.viewList = viewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewbs_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewListModel model = viewList.get(position);

        holder.name.setText(model.getName());

        Picasso.get().load(model.getUrl()).into(holder.circleImageView);
        holder.time.setText(model.getTime());

    }

    @Override
    public int getItemCount() {
        return viewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your ViewHolder logic goes here
        CircleImageView circleImageView;
        TextView name,time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.ivviewbs_item);
            name = itemView.findViewById(R.id.nametvviewbs_item);
            time = itemView.findViewById(R.id.timetvviewbs_item);
        }
    }
}
