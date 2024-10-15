package com.ramascript.allenconnect.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.Models.FollowerModel;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvFollowersSampleBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.viewHolder>{

    ArrayList<FollowerModel> list;
    Context context;

    public FollowerAdapter(ArrayList<FollowerModel> list, Context context ) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_followers_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        FollowerModel model = list.get(position);
        FirebaseDatabase.getInstance().getReference().child("Users")
            .child(model.getFollowedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                Picasso.get()
                        .load(userModel.getProfilePhoto())
                        .placeholder(R.drawable.ic_avatar)
                        .into(holder.binding.friendImg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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
