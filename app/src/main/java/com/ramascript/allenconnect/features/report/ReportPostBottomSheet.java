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
    private Button submitButton;
    private Button cancelButton;
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
            Toast.makeText(getContext(), "Please provide a reason for reporting", Toast.LENGTH_SHORT).show();
            return;
        }

        if (postId == null || auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Error: Cannot submit report", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // First get current user information
        database.getReference().child("Users").child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userModel user = snapshot.getValue(userModel.class);
                            if (user != null) {
                                saveReport(user, reason);
                            } else {
                                Toast.makeText(getContext(), "Error: User data not available", Toast.LENGTH_SHORT)
                                        .show();
                                dismiss();
                            }
                        } else {
                            Toast.makeText(getContext(), "Error: User not found", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
    }

    private void saveReport(userModel user, String reason) {
        // First check if the post still exists
        database.getReference().child("Posts").child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Post exists, proceed with saving report
                            // Create report data
                            Map<String, Object> reportData = new HashMap<>();
                            reportData.put("postId", postId);
                            reportData.put("reporterId", user.getID());
                            reportData.put("reporterName", user.getName());
                            reportData.put("reporterType", user.getUserType());
                            reportData.put("reason", reason);
                            reportData.put("timestamp", new Date().getTime());

                            // Create a unique report ID
                            String reportId = database.getReference().child("Reports").child(postId).push().getKey();

                            if (reportId != null) {
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

                                            Toast.makeText(getContext(), "Report submitted successfully",
                                                    Toast.LENGTH_SHORT).show();
                                            dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                                                    .show();
                                            dismiss();
                                        });
                            } else {
                                Toast.makeText(getContext(), "Error creating report", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        } else {
                            // Post doesn't exist
                            Toast.makeText(getContext(), "This post no longer exists", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
    }
}