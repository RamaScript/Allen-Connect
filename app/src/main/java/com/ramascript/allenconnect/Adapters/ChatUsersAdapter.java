package com.ramascript.allenconnect.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.Chat.ChatDetailActivity;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvChatsBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatUsersAdapter extends RecyclerView.Adapter<ChatUsersAdapter.viewHolder> implements Filterable {

    private ArrayList<UserModel> list;
    private ArrayList<UserModel> listFull;
    private Context context;
    private FilterCallback filterCallback;

    public interface FilterCallback {
        void onFilterComplete(int size);
    }

    public ChatUsersAdapter(ArrayList<UserModel> list, Context context, FilterCallback callback) {
        this.list = list;
        this.listFull = new ArrayList<>(list);
        this.context = context;
        this.filterCallback = callback;
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

        // Set profile image
        if (userModel.getProfilePhoto() != null) {
            Picasso.get()
                    .load(userModel.getProfilePhoto())
                    .placeholder(R.drawable.ic_avatar)
                    .into(holder.binding.profileImage);
        } else {
            holder.binding.profileImage.setImageResource(R.drawable.ic_avatar);
        }

        // Set user name
        holder.binding.userName.setText(userModel.getName());

        // Check if there's a last message
        if (userModel.getLastMsg() != null && !userModel.getLastMsg().isEmpty()) {
            holder.binding.lastMessage.setText(userModel.getLastMsg());

            // Show timestamp if available
            if (userModel.getLastMessageTime() != null) {
                holder.binding.msgTime.setText(UserModel.getTimeAgo(userModel.getLastMessageTime()));
                holder.binding.msgTime.setVisibility(View.VISIBLE);
            } else {
                holder.binding.msgTime.setVisibility(View.GONE);
            }
        } else {
            // If no chat history, show course and year
            String courseInfo = "";
            if (userModel.getCourse() != null && !userModel.getCourse().isEmpty()) {
                courseInfo = userModel.getCourse();
                if (userModel.getYear() != null && !userModel.getYear().isEmpty()) {
                    courseInfo += " • " + userModel.getYear() + " Year";
                }
            }
            holder.binding.lastMessage.setText(courseInfo);
            holder.binding.msgTime.setVisibility(View.GONE);
        }

        // Set unread count
        if (userModel.getUnreadCount() > 0) {
            holder.binding.unreadCount.setVisibility(View.VISIBLE);
            holder.binding.unreadCount.setText(String.valueOf(userModel.getUnreadCount()));
        } else {
            holder.binding.unreadCount.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("userId", userModel.getID());
            intent.putExtra("profilePicture", userModel.getProfilePhoto());
            intent.putExtra("userName", userModel.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<UserModel> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(listFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (UserModel user : listFull) {
                        // First check for names starting with the search pattern
                        if (user.getName().toLowerCase().startsWith(filterPattern)) {
                            filteredList.add(user);
                        }
                    }

                    // Then add items that contain the search pattern but don't start with it
                    for (UserModel user : listFull) {
                        if (!user.getName().toLowerCase().startsWith(filterPattern) &&
                                user.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(user);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list.clear();
                // noinspection unchecked
                list.addAll((ArrayList<UserModel>) results.values);
                notifyDataSetChanged();
                if (filterCallback != null) {
                    filterCallback.onFilterComplete(list.size());
                }
            }
        };
    }

    public void updateList(ArrayList<UserModel> newList) {
        this.listFull = new ArrayList<>(newList);
        this.list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        RvChatsBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RvChatsBinding.bind(itemView);
        }
    }
}
