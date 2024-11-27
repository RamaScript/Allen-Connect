package com.ramascript.allenconnect.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.Chat.ChatDetailActivity;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvChatsBinding;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

public class ChatUsersAdapter extends RecyclerView.Adapter<ChatUsersAdapter.viewHolder> {

    ArrayList<UserModel> list;
    Context context;

    public ChatUsersAdapter(ArrayList<UserModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_chats, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        UserModel userModel = list.get(position);
        Picasso.get()
            .load(userModel.getProfilePhoto())
            .placeholder(R.drawable.ic_avatar)
            .into(holder.binding.profileImage);
        holder.binding.userName.setText(userModel.getName());

        String lastMsg = userModel.getLastMsg();
        if (lastMsg == null) {
            // Set the text based on user type
            if ("Student".equals(userModel.getUserType())) {
                holder.binding.lastMessage.setText(userModel.getCourse() + " (" + userModel.getYear() + " year)");
            } else if ("Alumni".equals(userModel.getUserType())) {
                holder.binding.lastMessage.setText(userModel.getJobRole() + " at " + userModel.getCompany());
            } else if ("Professor".equals(userModel.getUserType())) {
                holder.binding.lastMessage.setText("Professor at AGOI");
            }
        } else {
            holder.binding.lastMessage.setText(userModel.getLastMsg());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ab mai sare data ko main activity se chat activity me bhej rha hhuuuu
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId", userModel.getID());
                intent.putExtra("profilePicture", userModel.getProfilePhoto());
                intent.putExtra("userName", userModel.getName());
                context.startActivity(intent);
                // Finish the current activity
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        RvChatsBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = RvChatsBinding.bind(itemView);
        }
    }
}
