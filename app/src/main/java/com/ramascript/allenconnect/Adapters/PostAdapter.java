package com.ramascript.allenconnect.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.Features.CommentActivity;
import com.ramascript.allenconnect.Models.PostModel;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvPostSampleBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {

    ArrayList<PostModel> list;
    Context context;

    FirebaseAuth auth;
    FirebaseDatabase database;

    public PostAdapter(ArrayList<PostModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_post_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        PostModel model = list.get(position);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        String postImage = model.getPostImage();
        if (postImage == null || postImage.isEmpty()) {
            holder.binding.postImg.setVisibility(View.GONE); // Hide the image if not available
        } else {
            holder.binding.postImg.setVisibility(View.VISIBLE); // Show the image if available
            Picasso.get().load(postImage).placeholder(R.drawable.ic_post_placeholder).into(holder.binding.postImg);
        }

        holder.binding.commentTV.setText(model.getCommentCount() + "");
        holder.binding.likeTV.setText(model.getPostLikes() + "");

        String caption = model.getPostCaption();
        if (caption == null || caption.isEmpty()) {
            holder.binding.postcaption.setVisibility(View.GONE);
        } else {
            holder.binding.postcaption.setText(caption);
            holder.binding.postcaption.setVisibility(View.VISIBLE);
        }

        database.getReference().child("Users").child(model.getPostedBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        assert userModel != null;
                        Picasso.get().load(userModel.getProfilePhoto()).placeholder(R.drawable.ic_avatar)
                                .into(holder.binding.profileImagePost);
                        holder.binding.userName.setText(userModel.getName());

                        // Set the text based on user type
                        if ("Student".equals(userModel.getUserType())) {
                            holder.binding.about
                                    .setText(String.format("%s (%s year)", userModel.getCourse(), userModel.getYear()));
                        } else if ("Alumni".equals(userModel.getUserType())) {
                            holder.binding.about
                                    .setText(String.format("%s at %s", userModel.getJobRole(), userModel.getCompany()));
                        } else if ("Professor".equals(userModel.getUserType())) {
                            holder.binding.about.setText("Professor at AGOI");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Check if the current user has liked the post
        database.getReference()
                .child("Posts")
                .child(model.getPostID())
                .child("Likes")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User has already liked the post - show filled heart
                            holder.binding.likeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_filled, 0,
                                    0, 0);

                            // Add click listener for unlike
                            holder.binding.likeTV.setOnClickListener(v -> {
                                // Remove like from database
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Posts")
                                        .child(model.getPostID())
                                        .child("Likes")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .removeValue()
                                        .addOnSuccessListener(unused -> {
                                            // When unliking, DECREASE the like count
                                            int newLikeCount = model.getPostLikes() - 1;
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Posts")
                                                    .child(model.getPostID())
                                                    .child("postLikes")
                                                    .setValue(newLikeCount);
                                            model.setPostLikes(newLikeCount); // Update local model
                                            holder.binding.likeTV.setText(String.valueOf(newLikeCount)); // Update UI
                                                                                                         // count
                                        });
                            });
                        } else {
                            // User hasn't liked the post - show empty heart
                            holder.binding.likeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart, 0, 0, 0);

                            // Add click listener for like
                            holder.binding.likeTV.setOnClickListener(v -> {
                                // Add like to database
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Posts")
                                        .child(model.getPostID())
                                        .child("Likes")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .setValue(true)
                                        .addOnSuccessListener(unused -> {
                                            // When liking, INCREASE the like count
                                            int newLikeCount = model.getPostLikes() + 1;
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Posts")
                                                    .child(model.getPostID())
                                                    .child("postLikes")
                                                    .setValue(newLikeCount);
                                            model.setPostLikes(newLikeCount); // Update local model
                                            holder.binding.likeTV.setText(String.valueOf(newLikeCount)); // Update UI
                                                                                                         // count
                                        });
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Error: " + error.getMessage());
                    }
                });

        holder.binding.commentTV.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", model.getPostID());
            intent.putExtra("postedBy", model.getPostedBy());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        RvPostSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = RvPostSampleBinding.bind(itemView);

        }
    }
}
