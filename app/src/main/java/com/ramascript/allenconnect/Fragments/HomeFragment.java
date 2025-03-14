package com.ramascript.allenconnect.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.Adapters.PostAdapter;
import com.ramascript.allenconnect.Chat.Chat;
import com.ramascript.allenconnect.Models.PostModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    ArrayList<PostModel> postList;
    FirebaseDatabase database;
    FirebaseAuth auth;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Show progress bar while loading posts
        binding.progressBar.setVisibility(View.VISIBLE);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Load user profile image
        if (auth.getCurrentUser() != null) {
            database.getReference().child("Users").child(auth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.hasChild("profilePhoto")) {
                                String profileImage = snapshot.child("profilePhoto").getValue(String.class);
                                if (getContext() != null && profileImage != null) {
                                    Glide.with(getContext())
                                            .load(profileImage)
                                            .placeholder(R.drawable.ic_avatar)
                                            .into(binding.userProfileImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }

        // Set click listeners for notification and chat
        binding.notificationHomeIV.setOnClickListener(v -> {
            Fragment notificationFragment = new NotificationFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, notificationFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.chatHomeIV.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Chat.class));
            requireActivity().finish();
        });

        // Set click listener for post creation
        binding.createPostPrompt.setOnClickListener(v -> {
            // TODO: Navigate to post creation activity
            // Intent intent = new Intent(getContext(), CreatePostActivity.class);
            // startActivity(intent);
        });

        // Setup RecyclerView for posts
        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.dashBoardRV.setLayoutManager(linearLayoutManager);
        binding.dashBoardRV.setAdapter(postAdapter);

        // Add scroll listener to handle bottom navigation visibility
        binding.dashBoardRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int scrolledDistance = 0;
            private boolean controlsVisible = true;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (scrolledDistance > 10 && controlsVisible) {
                    // Hide bottom navigation and create post card
                    binding.createPostCard.animate().translationY(-binding.createPostCard.getHeight()).setDuration(200);
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -10 && !controlsVisible) {
                    // Show bottom navigation and create post card
                    binding.createPostCard.animate().translationY(0).setDuration(200);
                    controlsVisible = true;
                    scrolledDistance = 0;
                }

                if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                    scrolledDistance += dy;
                }
            }
        });

        // Load posts
        database.getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PostModel postModel = dataSnapshot.getValue(PostModel.class);
                    if (postModel != null) {
                        postModel.setPostID(dataSnapshot.getKey());
                        postList.add(postModel);
                    }
                }
                Collections.reverse(postList);
                postAdapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        // Handle back button
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        new AlertDialog.Builder(requireContext())
                                .setMessage("Do you want to leave the app?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", (dialog, id) -> requireActivity().finish())
                                .setNegativeButton("No", null)
                                .show();
                    }
                });

        return view;
    }
}
