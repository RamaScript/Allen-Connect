package com.ramascript.allenconnect.features.user;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvFollowersSampleBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class followerAdapter extends RecyclerView.Adapter<followerAdapter.viewHolder> {

    ArrayList<followerModel> list;
    Context context;

    public followerAdapter(ArrayList<followerModel> list, Context context) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_followers_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        followerModel model = list.get(position);

        if (model.getFollowedBy() != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(model.getFollowedBy())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                userModel userModel = snapshot.getValue(com.ramascript.allenconnect.features.user.userModel.class);
                                if (userModel != null && userModel.getProfilePhoto() != null) {
                                    Picasso.get()
                                            .load(userModel.getProfilePhoto())
                                            .placeholder(R.drawable.ic_avatar)
                                            .error(R.drawable.ic_avatar)
                                            .into(holder.binding.friendImg);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("followerAdapter", "Error loading follower data: " + error.getMessage());
                        }
                    });
        }

        // Add click listener to view follower's profile
        holder.binding.friendImg.setOnClickListener(v -> {
            // You can add navigation to follower's profile here if needed
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        RvFollowersSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RvFollowersSampleBinding.bind(itemView);
        }
    }
}
