package com.ramascript.allenconnect.features.community;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.databinding.FragmentCommunityStudentBinding;

import java.util.ArrayList;

public class communityStudentFragment extends Fragment {

    FragmentCommunityStudentBinding binding;
    ArrayList<userModel> list;
    ArrayList<userModel> filteredList;
    communityUserAdapter adapter;
    ValueEventListener usersListener;
    private ShimmerFrameLayout shimmerLayout;

    FirebaseAuth auth;
    FirebaseDatabase database;
    private boolean isInitialLoad = true;

    public communityStudentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Keep Users data synced for offline access
        try {
            database.getReference().child("Users").keepSynced(true);
        } catch (Exception e) {
            Log.e("communityStudentFragment", "Error setting keepSynced: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCommunityStudentBinding.inflate(inflater, container, false);

        list = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new communityUserAdapter(getContext(), filteredList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvStudent.setLayoutManager(layoutManager);
        binding.rvStudent.setHasFixedSize(true);
        binding.rvStudent.setAdapter(adapter);

        // Initialize shimmer
        shimmerLayout = binding.shimmerLayout;
        startShimmer();

        loadUsers();

        return binding.getRoot();
    }

    private void startShimmer() {
        if (shimmerLayout != null && binding != null) {
            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();
            binding.rvStudent.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    private void stopShimmer() {
        if (shimmerLayout != null && binding != null) {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
        }
    }

    private void loadUsers() {
        // Show loading state on initial load
        if (isInitialLoad && binding != null) {
            startShimmer();
        }

        // Remove previous listener if exists
        if (usersListener != null) {
            database.getReference().child("Users").removeEventListener(usersListener);
        }

        // Reference to the Users node
        final DatabaseReference usersRef = database.getReference().child("Users");

        // Enable disk persistence for this reference
        usersRef.keepSynced(true);

        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null)
                    return;

                // Only clear lists on initial load or filter changes
                if (isInitialLoad) {
                    list.clear();
                    filteredList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        userModel model = dataSnapshot.getValue(userModel.class);
                        if (model != null) {
                            model.setID(dataSnapshot.getKey());

                            // Skip deleted users
                            Boolean isDeleted = dataSnapshot.child("isDeleted").getValue(Boolean.class);
                            if (isDeleted != null && isDeleted) {
                                continue;
                            }

                            if (!dataSnapshot.getKey().equals(auth.getUid()) && "Student".equals(model.getUserType())) {
                                list.add(model);
                                filteredList.add(model);
                            }
                        }
                    }

                    // Hide loading state
                    stopShimmer();

                    // Check if we have data to show
                    if (filteredList.isEmpty()) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                        binding.rvStudent.setVisibility(View.GONE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                        binding.rvStudent.setVisibility(View.VISIBLE);
                    }

                    // Use more efficient notification method
                    adapter.notifyDataSetChanged();
                    isInitialLoad = false;
                } else {
                    // For subsequent updates, just update follow status data
                    // This prevents the list from refreshing entirely
                    for (int i = 0; i < list.size(); i++) {
                        userModel existingUser = list.get(i);
                        DataSnapshot userSnapshot = snapshot.child(existingUser.getID());
                        if (userSnapshot.exists()) {
                            // Update follower count if needed
                            Integer followersCount = userSnapshot.child("followersCount").getValue(Integer.class);
                            if (followersCount != null) {
                                existingUser.setFollowersCount(followersCount);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && binding != null) {
                    stopShimmer();
                    binding.rvStudent.setVisibility(View.VISIBLE);

                    // Show offline message if data is empty
                    if (list.isEmpty()) {
                        binding.emptyView.setText("Network error. Please check your connection.");
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        // Add the listener and keep data synced offline
        usersRef.addValueEventListener(usersListener);
    }

    public void filterUsers(String query) {
        if (binding == null)
            return;

        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(list);
        } else {
            query = query.toLowerCase();
            for (userModel user : list) {
                if (user.getName().toLowerCase().contains(query)) {
                    filteredList.add(user);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Show empty state if no results found
        if (filteredList.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove listeners when view is destroyed
        if (usersListener != null) {
            database.getReference().child("Users").removeEventListener(usersListener);
            usersListener = null;
        }

        stopShimmer();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start shimmer effect if it's showing
        if (shimmerLayout != null && shimmerLayout.getVisibility() == View.VISIBLE && binding != null) {
            shimmerLayout.startShimmer();
        }

        // Reset isInitialLoad flag when returning to this fragment
        isInitialLoad = true;

        // Reload users data when returning to this fragment from another fragment
        if (binding != null && list != null) {
            loadUsers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shimmerLayout != null && binding != null) {
            shimmerLayout.stopShimmer();
        }
    }
}