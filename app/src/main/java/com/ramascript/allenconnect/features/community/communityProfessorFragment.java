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
import com.ramascript.allenconnect.databinding.FragmentCommunityProfessorBinding;

import java.util.ArrayList;

public class communityProfessorFragment extends Fragment {

    FragmentCommunityProfessorBinding binding;
    ArrayList<userModel> list;
    ArrayList<userModel> filteredList;
    communityUserAdapter adapter;
    ValueEventListener usersListener;

    FirebaseAuth auth;
    FirebaseDatabase database;
    private boolean isInitialLoad = true;

    public communityProfessorFragment() {
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
        binding = FragmentCommunityProfessorBinding.inflate(inflater, container, false);

        list = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new communityUserAdapter(getContext(), filteredList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvProfessor.setLayoutManager(layoutManager);
        binding.rvProfessor.setHasFixedSize(true);
        binding.rvProfessor.setAdapter(adapter);

        loadUsers();

        return binding.getRoot();
    }

    private void loadUsers() {
        if (isInitialLoad && binding != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.rvProfessor.setVisibility(View.GONE);
        }

        if (usersListener != null) {
            database.getReference().child("Users").removeEventListener(usersListener);
        }

        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null)
                    return;

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

                            if (!dataSnapshot.getKey().equals(auth.getUid())
                                    && "Professor".equals(model.getUserType())) {
                                list.add(model);
                                filteredList.add(model);
                            }
                        }
                    }

                    binding.progressBar.setVisibility(View.GONE);

                    // Check if we have data to show
                    if (filteredList.isEmpty()) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                        binding.rvProfessor.setVisibility(View.GONE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                        binding.rvProfessor.setVisibility(View.VISIBLE);
                    }

                    adapter.notifyDataSetChanged();
                    isInitialLoad = false;
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        userModel existingUser = list.get(i);
                        DataSnapshot userSnapshot = snapshot.child(existingUser.getID());
                        if (userSnapshot.exists()) {
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
                    binding.rvProfessor.setVisibility(View.VISIBLE);
                }
            }
        };

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

        if (filteredList.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (usersListener != null) {
            database.getReference().child("Users").removeEventListener(usersListener);
            usersListener = null;
        }

        binding = null;
    }
}