package com.ramascript.allenconnect.features.report;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView reportsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;

    private FirebaseDatabase database;
    private ReportedPostsAdapter adapter;
    private Map<String, ArrayList<ReportModel>> reportedPostsMap;
    private ArrayList<String> postIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        reportsRecyclerView = findViewById(R.id.reportsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        // Set up toolbar with custom back button
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Setup back button click listener
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                onBackPressed();
            });
        }

        // Set up RecyclerView
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportedPostsMap = new HashMap<>();
        postIds = new ArrayList<>();
        adapter = new ReportedPostsAdapter(this, reportedPostsMap, postIds);
        reportsRecyclerView.setAdapter(adapter);

        // Load reported posts
        loadReportedPosts();
    }

    private void loadReportedPosts() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);

        DatabaseReference reportsRef = database.getReference().child("Reports");
        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportedPostsMap.clear();
                postIds.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showEmptyState();
                    return;
                }

                final int[] pendingPosts = { (int) snapshot.getChildrenCount() };
                android.util.Log.d("ReportsActivity", "Found " + snapshot.getChildrenCount() + " posts with reports");

                // Iterate through all postIds
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    ArrayList<ReportModel> reports = new ArrayList<>();
                    android.util.Log.d("ReportsActivity",
                            "Processing post: " + postId + " with " + postSnapshot.getChildrenCount() + " reports");

                    // Iterate through all reports for this post
                    for (DataSnapshot reportSnapshot : postSnapshot.getChildren()) {
                        ReportModel report = reportSnapshot.getValue(ReportModel.class);
                        if (report != null) {
                            report.setReportId(reportSnapshot.getKey());
                            report.setPostId(postId);

                            // Debug logs for report data
                            android.util.Log.d("ReportsActivity", "Report data: " +
                                    "reportId=" + report.getReportId() +
                                    ", postId=" + report.getPostId() +
                                    ", reporterId=" + report.getReporterId() +
                                    ", reporterName=" + report.getReporterName());

                            // Check if reporterId is null/empty
                            if (report.getReporterId() == null || report.getReporterId().isEmpty()) {
                                android.util.Log.e("ReportsActivity", "ReporterId is null or empty in the database");
                                // Try to get reporterId from raw snapshot data
                                Object reporterIdObj = reportSnapshot.child("reporterId").getValue();
                                if (reporterIdObj != null) {
                                    String reporterId = reporterIdObj.toString();
                                    android.util.Log.d("ReportsActivity",
                                            "Fixed reporterId from snapshot: " + reporterId);
                                    report.setReporterId(reporterId);
                                }
                            }

                            reports.add(report);
                        } else {
                            android.util.Log.e("ReportsActivity", "Failed to parse report from snapshot");
                        }
                    }

                    if (!reports.isEmpty()) {
                        reportedPostsMap.put(postId, reports);
                        if (!postIds.contains(postId)) {
                            postIds.add(postId);
                        }
                    }

                    pendingPosts[0]--;

                    // When all posts are processed, update the UI
                    if (pendingPosts[0] <= 0) {
                        updateUI();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ReportsActivity.this,
                        "Error loading reports: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (reportedPostsMap.isEmpty()) {
            showEmptyState();
        } else {
            progressBar.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.GONE);
            adapter = new ReportedPostsAdapter(this, reportedPostsMap, postIds);
            reportsRecyclerView.setAdapter(adapter);
        }
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText("No reported posts found");
    }
}