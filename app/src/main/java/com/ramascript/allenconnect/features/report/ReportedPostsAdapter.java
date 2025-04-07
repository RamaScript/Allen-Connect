package com.ramascript.allenconnect.features.report;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.makeramen.roundedimageview.RoundedImageView;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.features.post.postModel;
import com.ramascript.allenconnect.features.user.userModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReportedPostsAdapter extends RecyclerView.Adapter<ReportedPostsAdapter.ViewHolder> {

    private final Context context;
    private final Map<String, ArrayList<ReportModel>> reportedPostsMap;
    private final ArrayList<String> postIds;
    private final FirebaseDatabase database;
    private final FirebaseStorage storage;

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

        // Set the number of reports in the badge
        holder.reportCountBadge.setText(String.valueOf(reports.size()));

        // Load post details
        loadPostDetails(holder, postId);

        // Set up click listener for the card
        holder.itemView.setOnClickListener(v -> {
            showReportDetailsBottomSheet(postId, reports);
        });
    }

    private void loadPostDetails(ViewHolder holder, String postId) {
        database.getReference().child("Posts").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                            Picasso.get().load(post.getPostImage()).placeholder(R.drawable.ic_post_placeholder)
                                    .into(holder.postImageView);
                            holder.postImageView.setVisibility(View.VISIBLE);
                        } else {
                            holder.postImageView.setVisibility(View.GONE);
                        }

                        // Set time
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
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
                if (context != null) {
                    Toast.makeText(context, "Failed to load post: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
        database.getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userModel user = snapshot.getValue(userModel.class);
                    if (user != null) {
                        holder.userNameTextView.setText(user.getName());

                        // Set user type and additional info
                        if ("Student".equals(user.getUserType())) {
                            holder.userTypeTextView
                                    .setText(String.format("Student • %s (%s year)", user.getCourse(), user.getYear()));
                        } else if ("Alumni".equals(user.getUserType())) {
                            holder.userTypeTextView
                                    .setText(String.format("Alumni • %s at %s", user.getJobRole(), user.getCompany()));
                        } else if ("Professor".equals(user.getUserType())) {
                            holder.userTypeTextView.setText("Professor at AGOI");
                        }

                        // Load profile image
                        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                            Picasso.get().load(user.getProfilePhoto()).placeholder(R.drawable.ic_avatar)
                                    .into(holder.userProfileImageView);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (context != null) {
                    Toast.makeText(context, "Failed to load user info: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void showReportDetailsBottomSheet(String postId, ArrayList<ReportModel> reports) {
        ReportDetailsBottomSheet bottomSheet = ReportDetailsBottomSheet.newInstance(postId, reports);
        bottomSheet.setOnPostDeletedListener(deletedPostId -> {
            // Update adapter data
            int position = postIds.indexOf(deletedPostId);
            if (position != -1) {
                reportedPostsMap.remove(deletedPostId);
                postIds.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
            }
        });

        if (context instanceof androidx.fragment.app.FragmentActivity) {
            bottomSheet.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(),
                    "ReportDetailsBottomSheet");
        }
    }

    @Override
    public int getItemCount() {
        return postIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userProfileImageView;
        RoundedImageView postImageView;
        TextView userNameTextView, userTypeTextView, postCaptionTextView, postTimeTextView, reportCountBadge,
                reportCountTextView;
        RecyclerView reportsRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            userTypeTextView = itemView.findViewById(R.id.userTypeTextView);
            postCaptionTextView = itemView.findViewById(R.id.postCaptionTextView);
            postImageView = itemView.findViewById(R.id.postImageView);
            postTimeTextView = itemView.findViewById(R.id.postTimeTextView);
            reportCountBadge = itemView.findViewById(R.id.reportCountBadge);
            reportCountTextView = itemView.findViewById(R.id.reportCountTextView);
            reportsRecyclerView = itemView.findViewById(R.id.reportsRecyclerView);
        }
    }

    // Inner adapter class for report details
    static class ReportDetailsAdapter extends RecyclerView.Adapter<ReportDetailsAdapter.ReportViewHolder> {

        private final Context context;
        private final ArrayList<ReportModel> reports;
        private final FirebaseDatabase database;

        public ReportDetailsAdapter(Context context, ArrayList<ReportModel> reports) {
            this.context = context;
            this.reports = reports;
            this.database = FirebaseDatabase.getInstance();
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

            // Ensure the profile image view is visible and has a placeholder
            holder.reporterProfileImageView.setVisibility(View.VISIBLE);
            holder.reporterProfileImageView.setImageResource(R.drawable.ic_avatar);

            // Reporter name and type - use fallbacks if data is missing
            String reporterName = report.getReporterName();
            if (reporterName == null || reporterName.isEmpty()) {
                reporterName = "Unknown Reporter";
            }
            holder.reporterNameTextView.setText(reporterName);

            // Format the reporter type better
            String formattedType = report.getReporterType();
            if (formattedType == null || formattedType.isEmpty()) {
                formattedType = "Unknown";
            }

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
            String reason = report.getReason();
            if (reason == null || reason.isEmpty()) {
                reason = "No reason provided";
            }
            holder.reportReasonTextView.setText(reason);

            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
            holder.reportTimeTextView.setText(sdf.format(new Date(report.getTimestamp())));

            // Load reporter profile image only if reporterId is valid
            String reporterId = report.getReporterId();
            if (reporterId != null && !reporterId.isEmpty()) {
                loadReporterProfileImage(holder, reporterId);
            } else {
                android.util.Log.e("ReportDetailsAdapter", "Reporter ID is null or empty, skipping profile image load");
                // Set a default image
                holder.reporterProfileImageView.setImageResource(R.drawable.ic_avatar);
            }
        }

        private void loadReporterProfileImage(ReportViewHolder holder, String reporterId) {
            if (reporterId != null && !reporterId.isEmpty()) {
                android.util.Log.d("ReportDetailsAdapter", "Loading profile for reporter ID: " + reporterId);

                database.getReference().child("Users").child(reporterId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    userModel user = snapshot.getValue(userModel.class);
                                    if (user != null) {
                                        android.util.Log.d("ReportDetailsAdapter", "User found: " + user.getName());

                                        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                                            android.util.Log.d("ReportDetailsAdapter",
                                                    "Loading profile image: " + user.getProfilePhoto());

                                            // Make sure we have a valid image view before loading the image
                                            if (holder.reporterProfileImageView != null) {
                                                // Load profile image
                                                Picasso.get().load(user.getProfilePhoto())
                                                        .placeholder(R.drawable.ic_avatar)
                                                        .into(holder.reporterProfileImageView,
                                                                new com.squareup.picasso.Callback() {
                                                                    @Override
                                                                    public void onSuccess() {
                                                                        android.util.Log.d("ReportDetailsAdapter",
                                                                                "Image loaded successfully");
                                                                    }

                                                                    @Override
                                                                    public void onError(Exception e) {
                                                                        android.util.Log.e("ReportDetailsAdapter",
                                                                                "Error loading image: "
                                                                                        + e.getMessage());
                                                                    }
                                                                });
                                            } else {
                                                android.util.Log.e("ReportDetailsAdapter",
                                                        "Reporter profile image view is null");
                                            }
                                        } else {
                                            android.util.Log.d("ReportDetailsAdapter", "User has no profile photo");
                                        }
                                    } else {
                                        android.util.Log.d("ReportDetailsAdapter", "User object is null");
                                    }
                                } else {
                                    android.util.Log.d("ReportDetailsAdapter", "Reporter snapshot doesn't exist");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                android.util.Log.e("ReportDetailsAdapter", "Error loading user: " + error.getMessage());
                            }
                        });
            } else {
                android.util.Log.e("ReportDetailsAdapter", "Reporter ID is null or empty");
            }
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        static class ReportViewHolder extends RecyclerView.ViewHolder {
            TextView reporterNameTextView, reporterTypeTextView, reportReasonTextView, reportTimeTextView;
            de.hdodenhof.circleimageview.CircleImageView reporterProfileImageView;

            public ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                reporterProfileImageView = itemView.findViewById(R.id.reporterProfileImageView);
                reporterNameTextView = itemView.findViewById(R.id.reporterNameTextView);
                reporterTypeTextView = itemView.findViewById(R.id.reporterTypeTextView);
                reportReasonTextView = itemView.findViewById(R.id.reportReasonTextView);
                reportTimeTextView = itemView.findViewById(R.id.reportTimeTextView);

                // Debug check if profile image view is null
                if (reporterProfileImageView == null) {
                    android.util.Log.e("ReportViewHolder", "reporterProfileImageView is null");
                } else {
                    android.util.Log.d("ReportViewHolder", "reporterProfileImageView is found");
                }
            }
        }
    }
}