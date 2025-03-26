package com.ramascript.allenconnect.features.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ItemOnlineUserBinding;
import com.ramascript.allenconnect.features.user.userModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class onlineUsersAdapter extends RecyclerView.Adapter<onlineUsersAdapter.ViewHolder> {

    ArrayList<userModel> list;
    Context context;

    public onlineUsersAdapter(ArrayList<userModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_online_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        userModel user = list.get(position);

        // Set user name - keep it simple
        if (user.getName() != null) {
            // Set name text
            holder.binding.userName.setText(user.getName());

            // Only make the name bold if this is the current user
            if (user.isCurrentUser()) {
                holder.binding.userName.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                holder.binding.userName.setTypeface(Typeface.DEFAULT);
            }
        }

        // Set profile image
        if (user.getProfilePhoto() != null) {
            Picasso.get()
                    .load(user.getProfilePhoto())
                    .placeholder(R.drawable.ic_avatar)
                    .into(holder.binding.profileImage);
        } else {
            holder.binding.profileImage.setImageResource(R.drawable.ic_avatar);
        }

        // Set click listener - only for other users (not current user)
        holder.itemView.setOnClickListener(v -> {
            if (!user.isCurrentUser() && user.getID() != null) {
                Intent intent = new Intent(context, chatDetailActivity.class);
                intent.putExtra("userId", user.getID());
                intent.putExtra("userName", user.getName());
                intent.putExtra("profilePic", user.getProfilePhoto());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemOnlineUserBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemOnlineUserBinding.bind(itemView);
        }
    }
}