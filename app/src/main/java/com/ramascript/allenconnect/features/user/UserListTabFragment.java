package com.ramascript.allenconnect.features.user;

import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new communityUserAdapter(getContext(), userList);
        recyclerView.setAdapter(adapter);

        // Load data
        loadUserList();
    }

    private void loadUserList() {
        if (userId == null || userId.isEmpty()) {
            Log.e("UserListTabFragment", "Cannot load user list: userId is null or empty");
            updateUI(true);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Determine the path based on listType
        String path = "Users/" + userId + "/" +
                (listType != null && listType.equals("followers") ? "Followers" : "Following");

        Log.d("UserListTabFragment", "Loading user list from path: " + path);

        database.getReference().child(path)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                            updateUI(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UserListTabFragment", "Error loading user list: " + error.getMessage());
                        progressBar.setVisibility(View.GONE);
                        updateUI(true);
                    }
                });
    }

    private void loadUserDetails(ArrayList<String> userIds) {
        if (userIds.isEmpty()) {
            updateUI(true);
            progressBar.setVisibility(View.GONE);
            return;
        }

        final int[] loadedCount = { 0 };
        final int totalToLoad = userIds.size();

        for (String userId : userIds) {
            database.getReference().child("Users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                                updateUI(userList.isEmpty());
                                progressBar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loadedCount[0]++;
                            Log.e("UserListTabFragment", "Error loading user: " + error.getMessage());

                            // Check if all users are loaded
                            if (loadedCount[0] >= totalToLoad) {
                                updateUI(userList.isEmpty());
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private void updateUI(boolean isEmpty) {
        if (getContext() == null)
            return;

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
}