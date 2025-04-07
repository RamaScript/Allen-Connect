package com.ramascript.allenconnect.features.report;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ReportDetailsBottomSheet extends BottomSheetDialogFragment {

    private String postId;
    private ArrayList<ReportModel> reports;

    private TextView reportCountTextView;
    private CircleImageView userProfileImageView;
    private TextView userNameTextView;
    private TextView userTypeTextView;
    private TextView postCaptionTextView;
    private RoundedImageView postImageView;
    private TextView postTimeTextView;
    private RecyclerView reportsRecyclerView;
    private AppCompatButton deletePostButton;

    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private OnPostDeletedListener onPostDeletedListener;

    public interface OnPostDeletedListener {
        void onPostDeleted(String postId);
    }

    public static ReportDetailsBottomSheet newInstance(String postId, ArrayList<ReportModel> reports) {
        ReportDetailsBottomSheet fragment = new ReportDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putSerializable("reports", reports);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnPostDeletedListener(OnPostDeletedListener listener) {
        this.onPostDeletedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            reports = (ArrayList<ReportModel>) getArguments().getSerializable("reports");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_report_details, container, false);

        // Initialize views
        reportCountTextView = view.findViewById(R.id.reportCountTextView);
        userProfileImageView = view.findViewById(R.id.userProfileImageView);
        userNameTextView = view.findViewById(R.id.userNameTextView);
        userTypeTextView = view.findViewById(R.id.userTypeTextView);
        postCaptionTextView = view.findViewById(R.id.postCaptionTextView);
        postImageView = view.findViewById(R.id.postImageView);
        postTimeTextView = view.findViewById(R.id.postTimeTextView);
        reportsRecyclerView = view.findViewById(R.id.reportsRecyclerView);
        deletePostButton = view.findViewById(R.id.deletePostButton);

        // Set up UI
        if (reports != null) {
            reportCountTextView.setText(reports.size() + " Reports");

            // Check reports for missing reporterIds
            int missingReporterIds = 0;
            for (ReportModel report : reports) {
                if (report.getReporterId() == null || report.getReporterId().isEmpty()) {
                    missingReporterIds++;
                    android.util.Log.e("ReportDetailsBottomSheet",
                            "Found report with missing reporterId: " + report.getReportId());
                }
            }

            if (missingReporterIds > 0) {
                android.util.Log.e("ReportDetailsBottomSheet",
                        missingReporterIds + " reports out of " + reports.size() + " have missing reporterIds");
            }

            // Set up reports recycler view with our custom adapter
            reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            // Create a direct instance of our own ReportDetailsAdapter instead of using the
            // nested one
            ReportDetailsAdapter adapter = new ReportDetailsAdapter(getContext(), reports);
            reportsRecyclerView.setAdapter(adapter);
        }

        // Load post details
        loadPostDetails();

        // Set up delete button
        deletePostButton.setOnClickListener(v -> showDeleteConfirmation());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Post a delayed action to ensure the view is fully rendered
        view.post(() -> {
            if (isAdded() && getContext() != null && reports != null && reportsRecyclerView != null) {
                // Refresh the adapter after view is fully created
                ReportDetailsAdapter adapter = (ReportDetailsAdapter) reportsRecyclerView.getAdapter();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void loadPostDetails() {
        database.getReference().child("Posts").child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            postModel post = snapshot.getValue(postModel.class);
                            if (post != null) {
                                // Set caption
                                if (post.getPostCaption() != null && !post.getPostCaption().isEmpty()) {
                                    postCaptionTextView.setText(post.getPostCaption());
                                    postCaptionTextView.setVisibility(View.VISIBLE);
                                } else {
                                    postCaptionTextView.setVisibility(View.GONE);
                                }

                                // Set image
                                if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
                                    Picasso.get()
                                            .load(post.getPostImage())
                                            .placeholder(R.drawable.ic_post_placeholder)
                                            .into(postImageView);
                                    postImageView.setVisibility(View.VISIBLE);
                                } else {
                                    postImageView.setVisibility(View.GONE);
                                }

                                // Set time
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a",
                                        Locale.getDefault());
                                postTimeTextView.setText(sdf.format(new Date(post.getPostedAt())));

                                // Load user info
                                loadUserInfo(post.getPostedBy());
                            }
                        } else {
                            showDeletedPostMessage();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load post: " + error.getMessage(),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void loadUserInfo(String userId) {
        database.getReference().child("Users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userModel user = snapshot.getValue(userModel.class);
                            if (user != null) {
                                userNameTextView.setText(user.getName());

                                // Set user type and additional info
                                if ("Student".equals(user.getUserType())) {
                                    userTypeTextView.setText(
                                            String.format("Student • %s (%s year)", user.getCourse(), user.getYear()));
                                } else if ("Alumni".equals(user.getUserType())) {
                                    userTypeTextView.setText(
                                            String.format("Alumni • %s at %s", user.getJobRole(), user.getCompany()));
                                } else if ("Professor".equals(user.getUserType())) {
                                    userTypeTextView.setText("Professor at AGOI");
                                }

                                // Load profile image
                                if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                                    Picasso.get()
                                            .load(user.getProfilePhoto())
                                            .placeholder(R.drawable.ic_avatar)
                                            .into(userProfileImageView);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load user info: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showDeletedPostMessage() {
        postCaptionTextView.setText("This post has been deleted");
        postCaptionTextView.setVisibility(View.VISIBLE);
        postImageView.setVisibility(View.GONE);
        postTimeTextView.setVisibility(View.GONE);
        userNameTextView.setText("Deleted User");
        userTypeTextView.setText("Unknown");
        userProfileImageView.setImageResource(R.drawable.ic_avatar);
        deletePostButton.setEnabled(false);
    }

    private void showDeleteConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(
                requireContext());
        builder.setTitle("Delete Post");
        builder.setMessage("Are you sure you want to delete this post?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deletePost();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deletePost() {
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
                                            deletePostFromDatabase();
                                        });
                            } else {
                                // No image to delete, proceed with post deletion
                                deletePostFromDatabase();
                            }
                        } else {
                            if (isAdded() && getContext() != null) {
                                Toast.makeText(getContext(), "Post not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Failed to delete post: " + error.getMessage(),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void deletePostFromDatabase() {
        // Delete reports for this post
        database.getReference().child("Reports").child(postId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // Delete post from database
                    database.getReference().child("Posts").child(postId)
                            .removeValue()
                            .addOnSuccessListener(unused2 -> {
                                // Check if the fragment is still attached before showing toast
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT)
                                            .show();
                                }

                                // Notify listener that post was deleted
                                if (onPostDeletedListener != null) {
                                    onPostDeletedListener.onPostDeleted(postId);
                                }

                                // Check if fragment is attached before calling dismiss
                                if (isAdded()) {
                                    dismiss();
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Check if the fragment is still attached before showing toast
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(getContext(), "Failed to delete post: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    // Check if the fragment is still attached before showing toast
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Failed to delete reports: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    // Create our own standalone ReportDetailsAdapter class
    private static class ReportDetailsAdapter extends RecyclerView.Adapter<ReportDetailsAdapter.ReportViewHolder> {

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

            android.util.Log.d("ReportDetailsAdapter", "Binding view for report at position " + position);

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

            // Load reporter profile image
            String reporterId = report.getReporterId();
            android.util.Log.d("ReportDetailsAdapter", "Reporter ID: " +
                    (reporterId != null ? reporterId : "null") + " for position " + position);

            if (reporterId != null && !reporterId.isEmpty()) {
                // Debug log
                android.util.Log.d("ReportDetailsAdapter", "Loading image for reporter ID: " + reporterId);

                // Define a final reference to the view holder position to use in the callback
                final int holderPosition = position;

                database.getReference().child("Users").child(reporterId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Check if data exists
                                if (dataSnapshot.exists()) {
                                    userModel user = dataSnapshot.getValue(userModel.class);
                                    if (user != null) {
                                        android.util.Log.d("ReportDetailsAdapter",
                                                "Found user for reporter ID: " + reporterId +
                                                        ", name: " + user.getName() +
                                                        ", photo: "
                                                        + (user.getProfilePhoto() != null ? user.getProfilePhoto()
                                                                : "null"));

                                        // Use the profile photo if available
                                        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                                            try {
                                                // Check if our position is still valid (view hasn't been recycled)
                                                if (holderPosition == holder.getAdapterPosition()
                                                        && holder.reporterProfileImageView != null) {
                                                    // Use Picasso to load the image with forced cache refresh
                                                    Picasso.get()
                                                            .load(user.getProfilePhoto())
                                                            .networkPolicy(com.squareup.picasso.NetworkPolicy.NO_CACHE)
                                                            .memoryPolicy(com.squareup.picasso.MemoryPolicy.NO_CACHE)
                                                            .placeholder(R.drawable.ic_avatar)
                                                            .error(R.drawable.ic_avatar)
                                                            .into(holder.reporterProfileImageView,
                                                                    new com.squareup.picasso.Callback() {
                                                                        @Override
                                                                        public void onSuccess() {
                                                                            android.util.Log.d("ReportDetailsAdapter",
                                                                                    "★★★ SUCCESS! Image loaded for reporter ID: "
                                                                                            + reporterId);
                                                                        }

                                                                        @Override
                                                                        public void onError(Exception e) {
                                                                            android.util.Log.e("ReportDetailsAdapter",
                                                                                    "✖ ERROR! Failed to load image for reporter ID: "
                                                                                            +
                                                                                            reporterId + ", error: "
                                                                                            + e.getMessage());
                                                                        }
                                                                    });
                                                } else {
                                                    android.util.Log.w("ReportDetailsAdapter",
                                                            "View recycled for position " + holderPosition);
                                                }
                                            } catch (Exception e) {
                                                android.util.Log.e("ReportDetailsAdapter",
                                                        "Exception loading image for reporter ID: " + reporterId, e);
                                            }
                                        } else {
                                            android.util.Log.d("ReportDetailsAdapter",
                                                    "No profile photo for reporter ID: " + reporterId);
                                        }
                                    } else {
                                        android.util.Log.e("ReportDetailsAdapter",
                                                "User object is null for reporter ID: " + reporterId);
                                    }
                                } else {
                                    android.util.Log.e("ReportDetailsAdapter",
                                            "No user data found for reporter ID: " + reporterId);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                android.util.Log.e("ReportDetailsAdapter",
                                        "Firebase error for reporter ID: " + reporterId +
                                                ", error: " + databaseError.getMessage());
                            }
                        });
            } else {
                android.util.Log.e("ReportDetailsAdapter", "Reporter ID is null or empty at position " + position);
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
            }
        }
    }
}