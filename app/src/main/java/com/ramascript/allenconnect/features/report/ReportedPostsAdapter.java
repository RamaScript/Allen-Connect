package com.ramascript.allenconnect.features.report;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.features.post.postModel;
import com.ramascript.allenconnect.features.user.userModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ReportedPostsAdapter extends RecyclerView.Adapter<ReportedPostsAdapter.ViewHolder> {

    private Context context;
    private Map<String, ArrayList<ReportModel>> reportedPostsMap;
    private ArrayList<String> postIds;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    public ReportedPostsAdapter(Context context, Map<String, ArrayList<ReportModel>> reportedPostsMap,
            ArrayList<String> postIds) {
        this.context = context;
        this.reportedPostsMap = reportedPostsMap;
        this.postIds = postIds;
        this.database = FirebaseDatabase.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reported_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String postId = postIds.get(position);
        ArrayList<ReportModel> reports = reportedPostsMap.get(postId);

        if (reports == null || reports.isEmpty()) {
            return;
        }

        // Set the number of reports
        holder.reportCountTextView.setText(String.format("%d Reports", reports.size()));

        // Load post details
        loadPostDetails(holder, postId);

        // Set up reports adapter
        ReportDetailsAdapter reportDetailsAdapter = new ReportDetailsAdapter(context, reports);
        holder.reportsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.reportsRecyclerView.setAdapter(reportDetailsAdapter);

        // Set up delete button
        holder.deletePostButton.setOnClickListener(v -> {
            showDeleteConfirmation(postId);
        });
    }

    private void loadPostDetails(ViewHolder holder, String postId) {
        database.getReference().child("Posts").child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            postModel post = snapshot.getValue(postModel.class);
                            if (post != null) {
                                post.setPostId(snapshot.getKey());

                                // Set caption
                                if (post.getPostCaption() != null && !post.getPostCaption().isEmpty()) {
                                    holder.postCaptionTextView.setText(post.getPostCaption());
                                    holder.postCaptionTextView.setVisibility(View.VISIBLE);
                                } else {
                                    holder.postCaptionTextView.setVisibility(View.GONE);
                                }

                                // Set image
                                if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
                                    Picasso.get()
                                            .load(post.getPostImage())
                                            .placeholder(R.drawable.ic_post_placeholder)
                                            .into(holder.postImageView);
                                    holder.postImageView.setVisibility(View.VISIBLE);
                                } else {
                                    holder.postImageView.setVisibility(View.GONE);
                                }

                                // Set time
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a",
                                        Locale.getDefault());
                                holder.postTimeTextView.setText(sdf.format(new Date(post.getPostedAt())));

                                // Load user info
                                loadUserInfo(holder, post.getPostedBy());
                            } else {
                                // Post data is null
                                showDeletedPostMessage(holder);
                            }
                        } else {
                            // Post doesn't exist
                            showDeletedPostMessage(holder);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to load post: " + error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void showDeletedPostMessage(ViewHolder holder) {
        holder.postCaptionTextView.setText("This post has been deleted");
        holder.postCaptionTextView.setVisibility(View.VISIBLE);
        holder.postImageView.setVisibility(View.GONE);
        holder.postTimeTextView.setVisibility(View.GONE);
        holder.userNameTextView.setText("Deleted User");
        holder.userTypeTextView.setText("Unknown");
        holder.userProfileImageView.setImageResource(R.drawable.ic_avatar);
    }

    private void loadUserInfo(ViewHolder holder, String userId) {
        database.getReference().child("Users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userModel user = snapshot.getValue(userModel.class);
                            if (user != null) {
                                holder.userNameTextView.setText(user.getName());

                                // Set user type and additional info
                                if ("Student".equals(user.getUserType())) {
                                    holder.userTypeTextView.setText(String.format("Student • %s (%s year)",
                                            user.getCourse(), user.getYear()));
                                } else if ("Alumni".equals(user.getUserType())) {
                                    holder.userTypeTextView.setText(String.format("Alumni • %s at %s",
                                            user.getJobRole(), user.getCompany()));
                                } else if ("Professor".equals(user.getUserType())) {
                                    holder.userTypeTextView.setText("Professor at AGOI");
                                }

                                // Load profile image
                                if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                                    Picasso.get()
                                            .load(user.getProfilePhoto())
                                            .placeholder(R.drawable.ic_avatar)
                                            .into(holder.userProfileImageView);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to load user info: " + error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void showDeleteConfirmation(String postId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Post");
        builder.setMessage("Are you sure you want to delete this post?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deletePost(postId);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deletePost(String postId) {
        // First get the post to check if it has an image to delete
        database.getReference().child("Posts").child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            postModel post = snapshot.getValue(postModel.class);
                            if (post != null && post.getPostImage() != null && !post.getPostImage().isEmpty()) {
                                // Delete the image from storage
                                storage.getReferenceFromUrl(post.getPostImage())
                                        .delete()
                                        .addOnCompleteListener(task -> {
                                            // Continue with deleting post data
                                            deletePostFromDatabase(postId);
                                        });
                            } else {
                                // No image to delete, proceed with post deletion
                                deletePostFromDatabase(postId);
                            }
                        } else {
                            Toast.makeText(context, "Post not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to delete post: " + error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void deletePostFromDatabase(String postId) {
        // Delete reports for this post
        database.getReference().child("Reports").child(postId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // Delete post from database
                    database.getReference().child("Posts").child(postId)
                            .removeValue()
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();

                                // Update adapter data
                                int position = postIds.indexOf(postId);
                                if (position != -1) {
                                    reportedPostsMap.remove(postId);
                                    postIds.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, getItemCount());
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete post: " + e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return postIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImageView, postImageView;
        TextView userNameTextView, userTypeTextView, postCaptionTextView, postTimeTextView, reportCountTextView;
        Button deletePostButton;
        RecyclerView reportsRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            userTypeTextView = itemView.findViewById(R.id.userTypeTextView);
            postCaptionTextView = itemView.findViewById(R.id.postCaptionTextView);
            postImageView = itemView.findViewById(R.id.postImageView);
            postTimeTextView = itemView.findViewById(R.id.postTimeTextView);
            reportCountTextView = itemView.findViewById(R.id.reportCountTextView);
            deletePostButton = itemView.findViewById(R.id.deletePostButton);
            reportsRecyclerView = itemView.findViewById(R.id.reportsRecyclerView);
        }
    }

    // Inner adapter class for report details
    static class ReportDetailsAdapter extends RecyclerView.Adapter<ReportDetailsAdapter.ReportViewHolder> {

        private Context context;
        private ArrayList<ReportModel> reports;

        public ReportDetailsAdapter(Context context, ArrayList<ReportModel> reports) {
            this.context = context;
            this.reports = reports;
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_report_detail, parent, false);
            return new ReportViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            ReportModel report = reports.get(position);

            // Reporter name and type
            holder.reporterNameTextView.setText(report.getReporterName());

            // Format the reporter type better
            String formattedType = report.getReporterType();
            if ("Student".equals(formattedType)) {
                holder.reporterTypeTextView.setText("Student");
            } else if ("Alumni".equals(formattedType)) {
                holder.reporterTypeTextView.setText("Alumni");
            } else if ("Professor".equals(formattedType)) {
                holder.reporterTypeTextView.setText("Professor");
            } else {
                holder.reporterTypeTextView.setText(formattedType);
            }

            // Report reason
            holder.reportReasonTextView.setText(report.getReason());

            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
            holder.reportTimeTextView.setText(sdf.format(new Date(report.getTimestamp())));
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        static class ReportViewHolder extends RecyclerView.ViewHolder {
            TextView reporterNameTextView, reporterTypeTextView, reportReasonTextView, reportTimeTextView;

            public ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                reporterNameTextView = itemView.findViewById(R.id.reporterNameTextView);
                reporterTypeTextView = itemView.findViewById(R.id.reporterTypeTextView);
                reportReasonTextView = itemView.findViewById(R.id.reportReasonTextView);
                reportTimeTextView = itemView.findViewById(R.id.reportTimeTextView);
            }
        }
    }
}