package com.ramascript.allenconnect.features.post;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.features.comment.commentActivity;
import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class postAdapter extends RecyclerView.Adapter<postAdapter.ViewHolder> {

    ArrayList<postModel> list;
    Context context;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public postAdapter(ArrayList<postModel> list, Context context) {
        this.list = list;
        this.context = context;
        // Enable stable IDs to improve RecyclerView performance
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        // Use the post ID hash as a stable identifier
        return list.get(position).getPostId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        // Return position as the view type to help RecyclerView differentiate items
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        postModel post = list.get(position);

        // Load post image only if it's not already loaded with the same URL
        if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
            // Set a tag to track the image URL being loaded
            String currentUrl = (String) holder.postImage.getTag();
            if (currentUrl == null || !currentUrl.equals(post.getPostImage())) {
                holder.postImage.setTag(post.getPostImage());
                Picasso.get()
                        .load(post.getPostImage())
                        .placeholder(R.drawable.ic_post_placeholder)
                        .into(holder.postImage);
            }
            holder.postImage.setVisibility(View.VISIBLE);
        } else {
            holder.postImage.setVisibility(View.GONE);
            holder.postImage.setTag(null);
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
            Intent intent = new Intent(context, commentActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("postedBy", post.getPostedBy());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

        // Set time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        holder.time.setText(sdf.format(new Date(post.getPostedAt())));

        // Set a tag to track which post ID we're loading data for
        holder.itemView.setTag(post.getPostId());
        String postId = post.getPostId();

        // Load user info
        loadUserInfo(holder, post.getPostedBy(), postId);

        // Load like status
        loadLikeStatus(holder, post);

        // Add click listener to open the user's profile
        holder.postUserLL.setOnClickListener(v -> {
            navigateToProfile(post.getPostedBy());
        });

        // Set up menu button click listener
        if (holder.postMenu != null) {
            // Show menu only if post belongs to current user or is admin
            boolean isCurrentUserPost = auth.getCurrentUser() != null &&
                    auth.getCurrentUser().getUid().equals(post.getPostedBy());

            if (isCurrentUserPost) {
                holder.postMenu.setVisibility(View.VISIBLE);
                holder.postMenu.setOnClickListener(v -> {
                    showPostMenu(holder, post);
                });
            } else {
                holder.postMenu.setVisibility(View.GONE);
            }
        }
    }

    private void showPostMenu(ViewHolder holder, postModel post) {
        // Create popup menu
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(context, holder.postMenu);
        popupMenu.inflate(R.menu.post_menu);

        // Only show delete option for posts made by current user
        popupMenu.getMenu().findItem(R.id.action_delete_post).setVisible(
                auth.getCurrentUser() != null &&
                        auth.getCurrentUser().getUid().equals(post.getPostedBy()));

        // Set up menu item click listener
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_delete_post) {
                deletePost(post);
                return true;
            }
            return false;
        });

        // Show the menu
        popupMenu.show();
    }

    private void deletePost(postModel post) {
        // Show confirmation dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Delete Post");
        builder.setMessage("Are you sure you want to delete this post?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // First delete any post image from storage
            if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
                com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl(post.getPostImage())
                        .delete()
                        .addOnCompleteListener(task -> {
                            // Proceed with deleting post data regardless of image deletion success
                            deletePostData(post);
                        });
            } else {
                // No image to delete, just delete post data
                deletePostData(post);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deletePostData(postModel post) {
        // Delete the post from database
        database.getReference()
                .child("Posts")
                .child(post.getPostId())
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // Find position of post in list
                    int position = -1;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getPostId().equals(post.getPostId())) {
                            position = i;
                            break;
                        }
                    }

                    // Remove from list and notify adapter
                    if (position != -1) {
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());

                        // Show success message
                        android.widget.Toast.makeText(context, "Post deleted successfully",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Show error message
                    android.widget.Toast.makeText(context, "Failed to delete post: " + e.getMessage(),
                            android.widget.Toast.LENGTH_SHORT).show();
                });
    }

    // Method to navigate to the profile fragment
    private void navigateToProfile(String userId) {
        if (context instanceof androidx.fragment.app.FragmentActivity) {
            androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) context;

            // Create new profile fragment instance with the user ID
            com.ramascript.allenconnect.features.user.profileFragment profileFragment = com.ramascript.allenconnect.features.user.profileFragment
                    .newInstance(userId);

            // Replace the current fragment with the profile fragment
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, profileFragment)
                    .addToBackStack(null) // Use null for default back stack behavior
                    .commit();
        }
    }

    private void loadUserInfo(ViewHolder holder, String userId, String postId) {
        // Skip loading if this view is no longer for the same post
        if (!postId.equals(holder.itemView.getTag())) {
            return;
        }

        database.getReference()
                .child("Users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Skip updating if the view is recycled for another post
                        if (!postId.equals(holder.itemView.getTag())) {
                            return;
                        }

                        userModel userModel = snapshot
                                .getValue(com.ramascript.allenconnect.features.user.userModel.class);
                        if (userModel != null && holder.profile != null && holder.name != null) {
                            // Use tag to avoid redundant image loads
                            String currentProfileUrl = (String) holder.profile.getTag();
                            if (currentProfileUrl == null || !currentProfileUrl.equals(userModel.getProfilePhoto())) {
                                holder.profile.setTag(userModel.getProfilePhoto());
                                Picasso.get()
                                        .load(userModel.getProfilePhoto())
                                        .placeholder(R.drawable.ic_avatar)
                                        .into(holder.profile);
                            }
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
    }

    private void loadLikeStatus(ViewHolder holder, postModel post) {
        String postId = post.getPostId();
        // Skip loading if this view is no longer for the same post
        if (!postId.equals(holder.itemView.getTag())) {
            return;
        }

        database.getReference()
                .child("Posts")
                .child(postId)
                .child("Likes")
                .child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Skip updating if the view is recycled for another post
                        if (!postId.equals(holder.itemView.getTag())) {
                            return;
                        }

                        if (snapshot.exists()) {
                            // User has already liked the post - show filled heart
                            holder.like.setImageResource(R.drawable.ic_heart_filled);

                            // Add click listener for unlike
                            holder.like.setOnClickListener(v -> {
                                // Remove like from database
                                database.getReference()
                                        .child("Posts")
                                        .child(post.getPostId())
                                        .child("Likes")
                                        .child(auth.getUid())
                                        .removeValue()
                                        .addOnSuccessListener(unused -> {
                                            // When unliking, DECREASE the like count
                                            int newLikeCount = post.getPostLikes() - 1;

                                            // Update the UI directly without reloading all data
                                            holder.like.setImageResource(R.drawable.ic_heart);
                                            holder.likes.setText(String.valueOf(newLikeCount));

                                            // Update the model
                                            post.setPostLikes(newLikeCount);

                                            // Add click listener for like
                                            holder.like.setOnClickListener(likeView -> {
                                                likePost(holder, post);
                                            });

                                            // Update the database (no listener to avoid reloading)
                                            database.getReference()
                                                    .child("Posts")
                                                    .child(post.getPostId())
                                                    .child("postLikes")
                                                    .setValue(newLikeCount);
                                        });
                            });
                        } else {
                            // User has not liked the post - show outline heart
                            holder.like.setImageResource(R.drawable.ic_heart);

                            // Add click listener for like
                            holder.like.setOnClickListener(v -> {
                                likePost(holder, post);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void likePost(ViewHolder holder, postModel post) {
        // Add like to database
        database.getReference()
                .child("Posts")
                .child(post.getPostId())
                .child("Likes")
                .child(auth.getUid())
                .setValue(true)
                .addOnSuccessListener(unused -> {
                    // When liking, INCREASE the like count
                    int newLikeCount = post.getPostLikes() + 1;

                    // Update the UI directly without reloading all data
                    holder.like.setImageResource(R.drawable.ic_heart_filled);
                    holder.likes.setText(String.valueOf(newLikeCount));

                    // Update the model
                    post.setPostLikes(newLikeCount);

                    // Add click listener for unlike
                    holder.like.setOnClickListener(unlikeView -> {
                        // Remove like from database
                        database.getReference()
                                .child("Posts")
                                .child(post.getPostId())
                                .child("Likes")
                                .child(auth.getUid())
                                .removeValue()
                                .addOnSuccessListener(removeUnused -> {
                                    // When unliking, DECREASE the like count
                                    int newerLikeCount = post.getPostLikes() - 1;

                                    // Update the UI directly without reloading all data
                                    holder.like.setImageResource(R.drawable.ic_heart);
                                    holder.likes.setText(String.valueOf(newerLikeCount));

                                    // Update the model
                                    post.setPostLikes(newerLikeCount);

                                    // Add click listener for like
                                    holder.like.setOnClickListener(likeAgainView -> {
                                        likePost(holder, post);
                                    });

                                    // Update the database (no listener to avoid reloading)
                                    database.getReference()
                                            .child("Posts")
                                            .child(post.getPostId())
                                            .child("postLikes")
                                            .setValue(newerLikeCount);
                                });
                    });

                    // Update the database (no listener to avoid reloading)
                    database.getReference()
                            .child("Posts")
                            .child(post.getPostId())
                            .child("postLikes")
                            .setValue(newLikeCount);
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile, postImage, like, comment, share, postMenu;
        TextView name, time, likes, comments, description, about;

        LinearLayout postUserLL;

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
            postUserLL = itemView.findViewById(R.id.postUserLL);
            postMenu = itemView.findViewById(R.id.postMenu);
        }
    }
}
