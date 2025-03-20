package com.ramascript.allenconnect.features.community;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

        loadUsers();

        return binding.getRoot();
    }

    private void loadUsers() {
        // Show loading state on initial load
        if (isInitialLoad && binding != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.rvStudent.setVisibility(View.GONE);
        }

        // Remove previous listener if exists
        if (usersListener != null) {
            database.getReference().child("Users").removeEventListener(usersListener);
        }

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
                            if (!dataSnapshot.getKey().equals(auth.getUid()) && "Student".equals(model.getUserType())) {
                                list.add(model);
                                filteredList.add(model);
                            }
                        }
                    }

                    // Hide loading state
                    binding.progressBar.setVisibility(View.GONE);

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
                if (binding != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.rvStudent.setVisibility(View.VISIBLE);
                }
            }
        };

        // Add the listener
        database.getReference().child("Users").addValueEventListener(usersListener);
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

        binding = null;
    }
}