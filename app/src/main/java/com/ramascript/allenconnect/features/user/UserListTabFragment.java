package com.ramascript.allenconnect.features.user;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.features.community.communityUserAdapter;

import java.util.ArrayList;

public class UserListTabFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String ARG_LIST_TYPE = "listType"; // "followers" or "following"

    private String userId;
    private String listType;

    private RecyclerView recyclerView;
    private View emptyView;
    private ProgressBar progressBar;
    private View shimmerView;
    private ShimmerFrameLayout shimmerFrameLayout;
    private DatabaseReference userRef;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ArrayList<userModel> userList;
    private communityUserAdapter adapter;

    public UserListTabFragment() {
        // Required empty public constructor
    }

    public static UserListTabFragment newInstance(String userId, String listType) {
        UserListTabFragment fragment = new UserListTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_LIST_TYPE, listType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID, "");
            listType = getArguments().getString(ARG_LIST_TYPE, "followers");
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userList = new ArrayList<>();

        // Keep data synced for offline capability
        try {
            // Determine the path based on listType
            String path = "Users/" + userId + "/" +
                    (listType != null && listType.equals("followers") ? "Followers" : "Following");
            database.getReference().child(path).keepSynced(true);
            database.getReference().child("Users").keepSynced(true);
        } catch (Exception e) {
            Log.e("UserListTabFragment", "Error setting keepSynced: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.usersRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
        shimmerView = view.findViewById(R.id.shimmerLayout);

        if (shimmerView != null) {
            shimmerFrameLayout = shimmerView.findViewById(R.id.shimmerFrameLayout);
        }

        startShimmer();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new communityUserAdapter(getContext(), userList);
        recyclerView.setAdapter(adapter);

        // Load data
        loadUserList();
    }

    private void startShimmer() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.startShimmer();
            if (shimmerView != null) {
                shimmerView.setVisibility(View.VISIBLE);
            }
        }
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void stopShimmer() {
        if (!isAdded())
            return;

        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
        }

        if (shimmerView != null) {
            shimmerView.setVisibility(View.GONE);
        }

        // Don't set RecyclerView visible here - let updateUI handle visibility
        // instead based on whether we have data
    }

    private void loadUserList() {
        if (userId == null || userId.isEmpty()) {
            Log.e("UserListTabFragment", "Cannot load user list: userId is null or empty");
            stopShimmer();
            updateUI(true);
            return;
        }

        // Determine the path based on listType
        String path = "Users/" + userId + "/" +
                (listType != null && listType.equals("followers") ? "Followers" : "Following");

        Log.d("UserListTabFragment", "Loading user list from path: " + path);

        userRef = database.getReference().child(path);
        userRef.keepSynced(true);

        // Add a timeout to ensure shimmer doesn't show indefinitely
        new Handler().postDelayed(() -> {
            if (isAdded() && shimmerView != null && shimmerView.getVisibility() == View.VISIBLE) {
                Log.d("UserListTabFragment", "Applying timeout for shimmer effect");
                stopShimmer();
                updateUI(userList.isEmpty());
            }
        }, 8000); // 8 second timeout

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) {
                    return; // Fragment not attached
                }

                userList.clear();

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    // First collect all user IDs
                    ArrayList<String> userIds = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String id = dataSnapshot.getKey();
                        if (id != null && !id.isEmpty()) {
                            userIds.add(id);
                            Log.d("UserListTabFragment", "Added user ID: " + id);
                        }
                    }

                    // Then load user details for each ID
                    loadUserDetails(userIds);
                } else {
                    Log.d("UserListTabFragment", "No users found at path: " + path);
                    stopShimmer();
                    updateUI(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded())
                    return;

                Log.e("UserListTabFragment", "Error loading user list: " + error.getMessage());
                stopShimmer();
                updateUI(true);
            }
        });
    }

    private void loadUserDetails(ArrayList<String> userIds) {
        if (userIds.isEmpty() || !isAdded()) {
            stopShimmer();
            updateUI(true);
            return;
        }

        final int[] loadedCount = { 0 };
        final int totalToLoad = userIds.size();

        // Set a reasonable timeout to ensure shimmer stops even if Firebase is slow
        Handler timeoutHandler = new Handler();
        Runnable timeoutRunnable = () -> {
            if (isAdded() && loadedCount[0] < totalToLoad) {
                Log.d("UserListTabFragment", "Timeout reached while loading users, showing what we have");
                stopShimmer();
                updateUI(userList.isEmpty());
            }
        };

        // Set a 5-second timeout
        timeoutHandler.postDelayed(timeoutRunnable, 5000);

        for (String userId : userIds) {
            DatabaseReference userDetailsRef = database.getReference().child("Users").child(userId);
            userDetailsRef.keepSynced(true);

            userDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded())
                        return;

                    loadedCount[0]++;

                    if (snapshot.exists()) {
                        userModel user = snapshot.getValue(userModel.class);
                        if (user != null) {
                            // Set the ID for the user (important for adapter)
                            user.setID(snapshot.getKey());

                            // Skip if account is deleted
                            Boolean isDeleted = user.isDeleted();
                            if (isDeleted == null || !isDeleted) {
                                userList.add(user);
                            }
                        }
                    }

                    // Check if all users are loaded
                    if (loadedCount[0] >= totalToLoad) {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        stopShimmer();
                        updateUI(userList.isEmpty());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (!isAdded())
                        return;

                    loadedCount[0]++;
                    Log.e("UserListTabFragment", "Error loading user: " + error.getMessage());

                    // Check if all users are loaded
                    if (loadedCount[0] >= totalToLoad) {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        stopShimmer();
                        updateUI(userList.isEmpty());
                    }
                }
            });
        }
    }

    private void updateUI(boolean isEmpty) {
        if (!isAdded() || getContext() == null)
            return;

        // Always stop shimmer when updating UI
        stopShimmer();

        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);

            // Set empty state text based on list type
            TextView emptyTitle = emptyView.findViewById(R.id.emptyTitleText);
            TextView emptyDesc = emptyView.findViewById(R.id.emptyDescText);
            ImageView emptyImage = emptyView.findViewById(R.id.emptyStateImage);

            if (listType.equals("followers")) {
                emptyTitle.setText("No Followers Yet");
                emptyDesc.setText("When people follow you, they'll appear here");

                if (emptyImage != null) {
                    emptyImage.setImageResource(R.drawable.ic_profile_empty);
                    emptyImage.setColorFilter(getContext().getResources().getColor(R.color.gray),
                            android.graphics.PorterDuff.Mode.SRC_IN);
                }
            } else {
                emptyTitle.setText("Not Following Anyone");
                emptyDesc.setText("When you follow people, they'll appear here");

                if (emptyImage != null) {
                    emptyImage.setImageResource(R.drawable.ic_profile_empty);
                    emptyImage.setColorFilter(getContext().getResources().getColor(R.color.gray),
                            android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shimmerFrameLayout != null && shimmerView != null && shimmerView.getVisibility() == View.VISIBLE) {
            shimmerFrameLayout.startShimmer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
        }
    }
}