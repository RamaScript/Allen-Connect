package com.ramascript.allenconnect.features.comment;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvCommentSampleBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class commentAdapter extends RecyclerView.Adapter<commentAdapter.viewHolder> {

    Context context;
    ArrayList<commentModel> list;

    public commentAdapter(Context context, ArrayList<commentModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_comment_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        commentModel commentModel = list.get(position);

        String timeAgo = TimeAgo.using(commentModel.getCommentedAt());
        holder.binding.time.setText(timeAgo);

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(commentModel.getCommentedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userModel userModel = snapshot.getValue(com.ramascript.allenconnect.features.user.userModel.class);
                        if (userModel != null && userModel.getProfilePhoto() != null) {
                            Picasso.get()
                                    .load(userModel.getProfilePhoto())
                                    .placeholder(R.drawable.ic_avatar)
                                    .into(holder.binding.profileImage);
                        } else {
                            holder.binding.profileImage.setImageResource(R.drawable.ic_avatar);
                        }
                        holder.binding.commenterName
                                .setText(Html.fromHtml("<b>" + userModel.getName() + "</b>" + "  "));
                        holder.binding.commentBody.setText(commentModel.getCommentBody());
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

        RvCommentSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RvCommentSampleBinding.bind(itemView);

        }
    }
}
