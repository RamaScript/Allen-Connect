package com.ramascript.allenconnect.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.Models.DashBoardModel;
import com.ramascript.allenconnect.R;


import java.util.ArrayList;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.viewHolder> {

    ArrayList<DashBoardModel> list;
    Context context;

    public DashboardAdapter(ArrayList<DashBoardModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_dashboard_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        DashBoardModel model = list.get(position);

        holder.profile.setImageResource(model.getProfile());
        holder.postImg.setImageResource(model.getPostImg());
//        holder.save.setImageResource(model.getSave());
        holder.name.setText(model.getName());
        holder.about.setText(model.getAbout());
        holder.like.setText(model.getLike());
        holder.comment.setText(model.getComment());
        holder.share.setText(model.getShare());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        ImageView profile, postImg, save;
        TextView name, about, like, comment, share;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profile_image_post);
            postImg = itemView.findViewById(R.id.postImg);
            save = itemView.findViewById(R.id.saveImg);
            name = itemView.findViewById(R.id.userName);
            about = itemView.findViewById(R.id.about);
            like = itemView.findViewById(R.id.likeTV);
            comment = itemView.findViewById(R.id.commentTV);
            share = itemView.findViewById(R.id.shareTV);


        }
    }
}
