package com.ramascript.allenconnect.features.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.features.user.userModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReportPostBottomSheet extends BottomSheetDialogFragment {

    private EditText reportReasonEditText;
    private AppCompatButton submitButton;
    private AppCompatButton cancelButton;
    private String postId;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    public static ReportPostBottomSheet newInstance(String postId) {
        ReportPostBottomSheet fragment = new ReportPostBottomSheet();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_report_post, container, false);

        reportReasonEditText = view.findViewById(R.id.reportReasonEditText);
        submitButton = view.findViewById(R.id.submitReportButton);
        cancelButton = view.findViewById(R.id.cancelReportButton);

        submitButton.setOnClickListener(v -> submitReport());
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void submitReport() {
        String reason = reportReasonEditText.getText().toString().trim();

        if (reason.isEmpty()) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Please provide a reason for reporting", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (postId == null || auth.getCurrentUser() == null) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Error: Cannot submit report", Toast.LENGTH_SHORT).show();
            }
            dismiss();
            return;
        }

        // Show a loading indicator or disable submit button
        if (submitButton != null) {
            submitButton.setEnabled(false);
            submitButton.setText("Submitting...");
        }

        // First get current user information
        database.getReference().child("Users").child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userModel user = snapshot.getValue(userModel.class);
                            if (user != null) {
                                // Add detailed logging
                                android.util.Log.d("ReportPostBottomSheet", "User data retrieved: " +
                                        "ID=" + (user.getID() != null ? user.getID() : "null") +
                                        ", Name=" + (user.getName() != null ? user.getName() : "null") +
                                        ", Type=" + (user.getUserType() != null ? user.getUserType() : "null"));

                                // Add this code to ensure user ID is set
                                if (user.getID() == null || user.getID().isEmpty()) {
                                    String uid = auth.getCurrentUser().getUid();
                                    android.util.Log.w("ReportPostBottomSheet",
                                            "User ID is missing, setting it from auth UID: " + uid);
                                    user.setID(uid);
                                }

                                saveReport(user, reason);
                            } else {
                                android.util.Log.e("ReportPostBottomSheet", "User data not available");

                                // Re-enable the button
                                if (submitButton != null) {
                                    submitButton.setEnabled(true);
                                    submitButton.setText("Submit");
                                }

                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(getContext(), "Error: User data not available", Toast.LENGTH_SHORT)
                                            .show();
                                }
                                dismiss();
                            }
                        } else {
                            android.util.Log.e("ReportPostBottomSheet", "User not found");

                            // Re-enable the button
                            if (submitButton != null) {
                                submitButton.setEnabled(true);
                                submitButton.setText("Submit");
                            }

                            if (isAdded() && getContext() != null) {
                                Toast.makeText(getContext(), "Error: User not found", Toast.LENGTH_SHORT).show();
                            }
                            dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("ReportPostBottomSheet", "Database error: " + error.getMessage());

                        // Re-enable the button
                        if (submitButton != null) {
                            submitButton.setEnabled(true);
                            submitButton.setText("Submit");
                        }

                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        dismiss();
                    }
                });
    }

    private void saveReport(userModel user, String reason) {
        // Double check that user is valid before proceeding
        if (user == null) {
            android.util.Log.e("ReportPostBottomSheet", "User object is null");
            handleError("Invalid user data (null user)");
            return;
        }

        // Double check that user ID is valid
        if (user.getID() == null || user.getID().isEmpty()) {
            // Last attempt to set ID from auth if available
            if (auth.getCurrentUser() != null) {
                String uid = auth.getCurrentUser().getUid();
                android.util.Log.w("ReportPostBottomSheet",
                        "User ID still missing, final attempt to set from auth: " + uid);
                user.setID(uid);
            } else {
                android.util.Log.e("ReportPostBottomSheet", "User ID is missing and auth current user is null");
                handleError("Cannot determine user ID");
                return;
            }
        }

        // Check other required fields
        if (user.getName() == null || user.getName().isEmpty()) {
            android.util.Log.w("ReportPostBottomSheet", "User name is missing, using 'Unknown User'");
            user.setName("Unknown User");
        }

        if (user.getUserType() == null || user.getUserType().isEmpty()) {
            android.util.Log.w("ReportPostBottomSheet", "User type is missing, using 'Unknown'");
            user.setUserType("Unknown");
        }

        android.util.Log.d("ReportPostBottomSheet", "Checking if post exists: " + postId);

        // First check if the post still exists
        database.getReference().child("Posts").child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Post exists, proceed with saving report

                            // Validate user ID one last time
                            if (user.getID() == null || user.getID().isEmpty()) {
                                android.util.Log.e("ReportPostBottomSheet",
                                        "Invalid user data detected after all attempts.");
                                handleError("Invalid user data");
                                return;
                            }

                            // Log for debugging
                            android.util.Log.d("ReportPostBottomSheet", "Saving report for post: " + postId +
                                    " by user: " + user.getID() + ", name: " + user.getName());

                            // Create report data
                            Map<String, Object> reportData = new HashMap<>();
                            reportData.put("postId", postId);
                            reportData.put("reporterId", user.getID());
                            reportData.put("reporterName", user.getName());
                            reportData.put("reporterType", user.getUserType());
                            reportData.put("reason", reason);
                            reportData.put("timestamp", new Date().getTime());

                            android.util.Log.d("ReportPostBottomSheet",
                                    "Report data created: " + reportData.toString());

                            // Create a unique report ID
                            String reportId = database.getReference().child("Reports").child(postId).push().getKey();

                            if (reportId != null) {
                                // Use a final variable to close over the reportId for use in lambda
                                final String finalReportId = reportId;

                                // Save report under the post ID in Reports node
                                database.getReference().child("Reports").child(postId).child(reportId)
                                        .setValue(reportData)
                                        .addOnSuccessListener(unused -> {
                                            // Update post to mark it as reported
                                            DatabaseReference postRef = database.getReference().child("Posts")
                                                    .child(postId);
                                            Map<String, Object> updateData = new HashMap<>();
                                            updateData.put("isReported", true);
                                            postRef.updateChildren(updateData);

                                            // Log success
                                            android.util.Log.d("ReportPostBottomSheet",
                                                    "Report saved successfully with ID: " + finalReportId);

                                            // Re-enable the button
                                            if (submitButton != null) {
                                                submitButton.setEnabled(true);
                                                submitButton.setText("Submit");
                                            }

                                            if (isAdded() && getContext() != null) {
                                                Toast.makeText(getContext(), "Report submitted successfully",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            android.util.Log.e("ReportPostBottomSheet",
                                                    "Failed to save report: " + e.getMessage());
                                            handleError("Failed to save report: " + e.getMessage());
                                        });
                            } else {
                                android.util.Log.e("ReportPostBottomSheet", "Failed to generate report ID");
                                handleError("Error creating report");
                            }
                        } else {
                            // Post doesn't exist
                            android.util.Log.e("ReportPostBottomSheet", "Post doesn't exist: " + postId);
                            handleError("This post no longer exists");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("ReportPostBottomSheet",
                                "Database operation cancelled: " + error.getMessage());
                        handleError("Error: " + error.getMessage());
                    }
                });
    }

    // Helper method to handle errors consistently
    private void handleError(String errorMessage) {
        // Re-enable the button
        if (submitButton != null) {
            submitButton.setEnabled(true);
            submitButton.setText("Submit");
        }

        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }
}