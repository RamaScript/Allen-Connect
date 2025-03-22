package com.ramascript.allenconnect.features.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvChatsBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class chatUsersAdapter extends RecyclerView.Adapter<chatUsersAdapter.viewHolder> implements Filterable {

    private ArrayList<userModel> list;
    private ArrayList<userModel> listFull;
    private Context context;
    private FilterCallback filterCallback;

    public interface FilterCallback {
        void onFilterComplete(int size);
    }

    public chatUsersAdapter(ArrayList<userModel> list, Context context, FilterCallback callback) {
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
        userModel userModel = list.get(position);

        // Check if account is deleted
        boolean isDeleted = userModel.isDeleted() != null && userModel.isDeleted();

        // Set profile image
        if (isDeleted) {
            // Use placeholder for deleted users
            holder.binding.profileImage.setImageResource(R.drawable.ic_avatar);
        } else if (userModel.getProfilePhoto() != null) {
            Picasso.get()
                    .load(userModel.getProfilePhoto())
                    .placeholder(R.drawable.ic_avatar)
                    .into(holder.binding.profileImage);
        } else {
            holder.binding.profileImage.setImageResource(R.drawable.ic_avatar);
        }

        // Set user name - show "Deleted User" for deleted accounts
        holder.binding.userName.setText(userModel.getName());

        // Check if there's a last message
        if (userModel.getLastMsg() != null && !userModel.getLastMsg().isEmpty()) {
            holder.binding.lastMessage.setText(userModel.getLastMsg());

            // Show timestamp if available
            if (userModel.getLastMessageTime() != null) {
                holder.binding.msgTime.setText(
                        com.ramascript.allenconnect.features.user.userModel.getTimeAgo(userModel.getLastMessageTime()));
                holder.binding.msgTime.setVisibility(View.VISIBLE);
            } else {
                holder.binding.msgTime.setVisibility(View.GONE);
            }
        } else {
            // If no chat history and account is deleted, show "Account deleted"
            if (isDeleted) {
                holder.binding.lastMessage.setText("Account deleted");
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
            }
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
            Intent intent = new Intent(context, chatDetailActivity.class);
            intent.putExtra("userId", userModel.getID());
            intent.putExtra("profilePicture", isDeleted ? "" : userModel.getProfilePhoto());
            intent.putExtra("userName", userModel.getName() + " {Deleted}");
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
                ArrayList<userModel> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(listFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (userModel user : listFull) {
                        // First check for names starting with the search pattern
                        if (user.getName().toLowerCase().startsWith(filterPattern)) {
                            filteredList.add(user);
                        }
                    }

                    // Then add items that contain the search pattern but don't start with it
                    for (userModel user : listFull) {
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
                list.addAll((ArrayList<userModel>) results.values);
                notifyDataSetChanged();
                if (filterCallback != null) {
                    filterCallback.onFilterComplete(list.size());
                }
            }
        };
    }

    public void updateList(ArrayList<userModel> newList) {
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
