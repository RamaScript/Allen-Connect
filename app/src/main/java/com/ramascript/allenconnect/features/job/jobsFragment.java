package com.ramascript.allenconnect.features.job;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.utils.baseFragment;
import com.ramascript.allenconnect.databinding.FragmentJobsBinding;

import java.util.ArrayList;

public class jobsFragment extends baseFragment {

    private FragmentJobsBinding binding;
    ArrayList<jobModel> list;
    private jobAdapter adapter;
    private ShimmerFrameLayout shimmerLayout;
    private ValueEventListener jobsListener;

    FirebaseAuth auth;
    FirebaseDatabase database;

    public jobsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Keep Jobs data synced for offline access
        try {
            database.getReference().child("Jobs").keepSynced(true);
        } catch (Exception e) {
            Log.e("jobsFragment", "Error setting keepSynced: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Step 2: Inflate the layout using the binding
        binding = FragmentJobsBinding.inflate(inflater, container, false);

        list = new ArrayList<>();

        // Initialize shimmer layout
        shimmerLayout = binding.shimmerLayout;
        startShimmer();

        setupRecyclerView();
        loadJobs();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new jobAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.jobsRV.setLayoutManager(layoutManager);
        binding.jobsRV.setAdapter(adapter);
    }

    private void startShimmer() {
        if (shimmerLayout != null && binding != null) {
            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();
            binding.jobsRV.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    private void stopShimmer() {
        if (shimmerLayout != null && binding != null) {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
        }
    }

    private void loadJobs() {
        // Reference to the Jobs node
        final DatabaseReference jobsRef = database.getReference().child("Jobs");

        // Enable disk persistence for this reference
        jobsRef.keepSynced(true);

        // Remove previous listener if exists
        if (jobsListener != null) {
            jobsRef.removeEventListener(jobsListener);
        }

        jobsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) {
                    return; // Fragment not attached or binding is null
                }

                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        jobModel model = dataSnapshot.getValue(jobModel.class);
                        if (model != null) {
                            model.setJobID(dataSnapshot.getKey());
                            list.add(model);
                        }
                    } catch (Exception e) {
                        Log.e("jobsFragment", "Error parsing job: " + e.getMessage());
                    }
                }

                // Stop shimmer and update UI
                stopShimmer();
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && binding != null) {
                    stopShimmer();

                    // Show error/offline message if needed
                    if (list.isEmpty()) {
                        binding.emptyView.setText("Network error. Please check your connection.");
                        binding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        // If we have cached data, show it
                        binding.jobsRV.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        // Add the listener
        jobsRef.addValueEventListener(jobsListener);
    }

    private void updateUI() {
        if (!isAdded() || binding == null) {
            return; // Fragment not attached or binding is null
        }

        if (list.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.jobsRV.setVisibility(View.GONE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
            binding.jobsRV.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shimmerLayout != null && shimmerLayout.getVisibility() == View.VISIBLE && binding != null) {
            shimmerLayout.startShimmer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shimmerLayout != null && binding != null) {
            shimmerLayout.stopShimmer();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove listener when view is destroyed
        if (jobsListener != null && database != null) {
            database.getReference().child("Jobs").removeEventListener(jobsListener);
            jobsListener = null;
        }

        binding = null;
    }
}
