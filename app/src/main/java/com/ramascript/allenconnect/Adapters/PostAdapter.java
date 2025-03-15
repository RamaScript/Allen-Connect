package com.ramascript.allenconnect.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    ArrayList<PostModel> list;
    Context context;

    public PostAdapter(ArrayList<PostModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostModel post = list.get(position);

        // Load post image
        if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
            Picasso.get()
                    .load(post.getPostImage())
                    .placeholder(R.drawable.ic_post_placeholder)
                    .into(holder.postImage);
            holder.postImage.setVisibility(View.VISIBLE);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        // Set description
        if (post.getPostCaption() != null && !post.getPostCaption().isEmpty()) {
            holder.description.setText(post.getPostCaption());
            holder.description.setVisibility(View.VISIBLE);
        } else {
            holder.description.setVisibility(View.GONE);
        }

        // Set likes and comments count
        holder.likes.setText(String.valueOf(post.getPostLikes()));
        holder.comments.setText(String.valueOf(post.getCommentCount()));

        // Add click listener for comment
        holder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("postedBy", post.getPostedBy());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

        // Set time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.time.setText(sdf.format(new Date(post.getPostedAt())));

        // Load user info
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(post.getPostedBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel != null && holder.profile != null && holder.name != null) {
                            Picasso.get()
                                    .load(userModel.getProfilePhoto())
                                    .placeholder(R.drawable.ic_avatar)
                                    .into(holder.profile);
                            holder.name.setText(userModel.getName());

                            // Set the text based on user type with null check
                            if (holder.about != null) {
                                if ("Student".equals(userModel.getUserType())) {
                                    holder.about.setText(String.format("%s (%s year)",
                                            userModel.getCourse(),
                                            userModel.getYear()));
                                } else if ("Alumni".equals(userModel.getUserType())) {
                                    holder.about.setText(String.format("%s at %s",
                                            userModel.getJobRole(),
                                            userModel.getCompany()));
                                } else if ("Professor".equals(userModel.getUserType())) {
                                    holder.about.setText("Professor at AGOI");
                                }
                                holder.about.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Load like status
        FirebaseDatabase.getInstance().getReference()
                .child("Posts")
                .child(post.getPostId())
                .child("Likes")
                .child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User has already liked the post - show filled heart
                            holder.like.setImageResource(R.drawable.ic_heart_filled);

                            // Add click listener for unlike
                            holder.like.setOnClickListener(v -> {
                                // Remove like from database
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Posts")
                                        .child(post.getPostId())
                                        .child("Likes")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .removeValue()
                                        .addOnSuccessListener(unused -> {
                                            // When unliking, DECREASE the like count
                                            int newLikeCount = post.getPostLikes() - 1;
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Posts")
                                                    .child(post.getPostId())
                                                    .child("postLikes")
                                                    .setValue(newLikeCount);
                                            post.setPostLikes(newLikeCount); // Update local model
                                            holder.likes.setText(String.valueOf(newLikeCount)); // Update UI count
                                        });
                            });
                        } else {
                            // User hasn't liked the post - show empty heart
                            holder.like.setImageResource(R.drawable.ic_heart);

                            // Add click listener for like
                            holder.like.setOnClickListener(v -> {
                                // Add like to database
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Posts")
                                        .child(post.getPostId())
                                        .child("Likes")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .setValue(true)
                                        .addOnSuccessListener(unused -> {
                                            // When liking, INCREASE the like count
                                            int newLikeCount = post.getPostLikes() + 1;
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Posts")
                                                    .child(post.getPostId())
                                                    .child("postLikes")
                                                    .setValue(newLikeCount);
                                            post.setPostLikes(newLikeCount); // Update local model
                                            holder.likes.setText(String.valueOf(newLikeCount)); // Update UI count
                                        });
                            });
                        }
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile, postImage, like, comment, share;
        TextView name, time, likes, comments, description, about;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profileImage);
            postImage = itemView.findViewById(R.id.postImage);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            share = itemView.findViewById(R.id.share);
            name = itemView.findViewById(R.id.userName);
            time = itemView.findViewById(R.id.time);
            likes = itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.comments);
            description = itemView.findViewById(R.id.description);
            about = itemView.findViewById(R.id.about);
        }
    }
}
