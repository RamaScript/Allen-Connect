package com.ramascript.allenconnect.Chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.User.UserModel;
import com.ramascript.allenconnect.databinding.ItemOnlineUserBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OnlineUsersAdapter extends RecyclerView.Adapter<OnlineUsersAdapter.ViewHolder> {

    private final ArrayList<UserModel> onlineUsers;
    private final Context context;

    public OnlineUsersAdapter(ArrayList<UserModel> onlineUsers, Context context) {
        this.onlineUsers = onlineUsers;
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
        UserModel user = onlineUsers.get(position);

        // Set user image
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
            Picasso.get()
                    .load(user.getProfilePhoto())
                    .placeholder(R.drawable.ic_avatar)
                    .into(holder.binding.userImage);
        } else {
            holder.binding.userImage.setImageResource(R.drawable.ic_avatar);
        }

        // Set user name with max length constraint
        String displayName = user.getName();
        if (displayName != null) {
            if (displayName.length() > 10) {
                displayName = displayName.substring(0, 7) + "...";
            }
            holder.binding.userName.setText(displayName);
        }

        // Online indicator is always visible for online users
        holder.binding.onlineIndicator.setVisibility(View.VISIBLE);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("userId", user.getID());
            intent.putExtra("profilePicture", user.getProfilePhoto());
            intent.putExtra("userName", user.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return onlineUsers.size();
    }

    // Add new online users to the list
    public void updateOnlineUsers(ArrayList<UserModel> newOnlineUsers) {
        this.onlineUsers.clear();
        this.onlineUsers.addAll(newOnlineUsers);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemOnlineUserBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemOnlineUserBinding.bind(itemView);
        }
    }
}