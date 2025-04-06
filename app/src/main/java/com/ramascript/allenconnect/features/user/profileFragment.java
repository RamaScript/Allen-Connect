package com.ramascript.allenconnect.features.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ramascript.allenconnect.features.auth.deleteAccountActivity;

import com.ramascript.allenconnect.features.auth.loginAs;
import com.ramascript.allenconnect.utils.baseFragment;
import com.ramascript.allenconnect.features.about.meetDevsActivity;
import com.ramascript.allenconnect.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.ramascript.allenconnect.databinding.FragmentProfileBinding;
import com.ramascript.allenconnect.features.post.postModel;
import com.ramascript.allenconnect.features.post.postAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;

public class profileFragment extends baseFragment {

    ArrayList<followerModel> list;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    FragmentProfileBinding binding; // Declare the binding
    ProgressDialog dialog;
    private ViewPager2 viewPager;
    private userModel currentUser;
    private String userId; // ID of user to display
    private boolean isCurrentUserProfile = true; // Flag to check if it's the current user's profile
    private ShimmerFrameLayout profileShimmerLayout;

    public profileFragment() {
        // Required empty public constructor
    }

    // New constructor to accept a userId parameter
    public static profileFragment newInstance(String userId) {
        profileFragment fragment = new profileFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);

        // Reset state variables
        fragment.isCurrentUserProfile = false; // Will be rechecked in onCreate
        fragment.currentUser = null;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        // Enable disk persistence for offline data
        try {
            // Note: persistence should be enabled only once in the Application class
            // We don't need to enable it here as it's already done in allenConnectApp

            // Keep user data synced for offline access
            if (getArguments() != null && getArguments().containsKey("userId")) {
                String id = getArguments().getString("userId");
                if (id != null) {
                    database.getReference().child("Users").child(id).keepSynced(true);
                }
            }
        } catch (Exception e) {
            Log.e("profileFragment", "Error with Firebase persistence: " + e.getMessage());
        }

        dialog = new ProgressDialog(getContext());

        // Check if userId was passed
        if (getArguments() != null && getArguments().containsKey("userId")) {
            userId = getArguments().getString("userId");
            isCurrentUserProfile = userId.equals(auth.getCurrentUser().getUid());
        } else {
            // Default to current user
            userId = auth.getCurrentUser().getUid();
            isCurrentUserProfile = true;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        setupProgressDialog();
        setupShimmer();
        setupViewPager();
        loadUserData();
        setupClickListeners();

        // Set up back button with proper back navigation
        binding.backButton.setOnClickListener(v -> {
            // Use proper back navigation to restore previous fragment state
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Only show menu button for current user's profile
        if (!isCurrentUserProfile) {
            binding.profileSettingsMenuBtn.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    private void setupProgressDialog() {
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Changing Profile Picture");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    private void setupShimmer() {
        profileShimmerLayout = binding.profileShimmerLayout;
        startShimmer();
    }

    private void startShimmer() {
        if (profileShimmerLayout != null && binding != null) {
            profileShimmerLayout.setVisibility(View.VISIBLE);
            profileShimmerLayout.startShimmer();
            binding.profileContentContainer.setVisibility(View.GONE);
        }
    }

    private void stopShimmer() {
        if (profileShimmerLayout != null && binding != null) {
            profileShimmerLayout.stopShimmer();
            profileShimmerLayout.setVisibility(View.GONE);
            binding.profileContentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setupViewPager() {
        viewPager = binding.viewPager;

        // Important: Remove default padding from ViewPager2
        if (viewPager.getChildAt(0) instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
            recyclerView.setPadding(0, 0, 0, 0);
            recyclerView.setClipToPadding(false);
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

            // Remove any item decorations
            for (int i = 0; i < recyclerView.getItemDecorationCount(); i++) {
                recyclerView.removeItemDecorationAt(i);
            }
        }

        ProfilePagerAdapter pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Posts");
                            break;
                        case 1:
                            tab.setText("Details");
                            break;
                    }
                }).attach();

        // Add page change callback to handle height and scroll behavior
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) { // Details tab
                    viewPager.post(() -> {
                        // Get the current fragment
                        Fragment fragment = getChildFragmentManager()
                                .findFragmentByTag("f" + position);
                        if (fragment != null && fragment.getView() != null) {
                            // Measure the details content
                            fragment.getView().measure(
                                    View.MeasureSpec.makeMeasureSpec(viewPager.getWidth(), View.MeasureSpec.EXACTLY),
                                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                            // Set the height to wrap the content
                            ViewGroup.LayoutParams params = viewPager.getLayoutParams();
                            params.height = fragment.getView().getMeasuredHeight();
                            viewPager.setLayoutParams(params);
                        }
                    });

                    // Make sure bottom navigation is visible for Details tab
                    if (getActivity() != null) {
                        View bottomNav = getActivity().findViewById(com.ramascript.allenconnect.R.id.bottomNavView);
                        if (bottomNav != null && bottomNav.getTranslationY() > 0) {
                            bottomNav.animate().translationY(0).setDuration(200).start();
                        }
                    }
                } else { // Posts tab
                    ViewGroup.LayoutParams params = viewPager.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    viewPager.setLayoutParams(params);
                }
            }
        });

        // Add scroll listener to the NestedScrollView
        if (binding.profileNestedScroll != null) {
            binding.profileNestedScroll.setOnScrollChangeListener(
                    (androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX,
                            oldScrollY) -> {
                        if (getActivity() == null)
                            return;

                        // Get the bottom navigation view
                        View bottomNav = getActivity().findViewById(com.ramascript.allenconnect.R.id.bottomNavView);
                        if (bottomNav == null)
                            return;

                        // When scrolling down, hide the bottom navigation
                        if (scrollY > oldScrollY + 10) {
                            if (bottomNav.getTranslationY() == 0) {
                                bottomNav.animate()
                                        .translationY(bottomNav.getHeight() + 100)
                                        .setDuration(200)
                                        .start();
                            }
                        }
                        // When scrolling up, show the bottom navigation
                        else if (oldScrollY > scrollY + 10) {
                            if (bottomNav.getTranslationY() > 0) {
                                bottomNav.animate()
                                        .translationY(0)
                                        .setDuration(200)
                                        .start();
                            }
                        }
                    });
        }

        // Disable swipe
        viewPager.setUserInputEnabled(false);
    }

    private void loadUserData() {
        Log.d("profileFragment", "Loading user data for uid: " + userId);

        // Show shimmer effect while loading
        startShimmer();

        // Keep data synced offline
        DatabaseReference userRef = database.getReference().child("Users").child(userId);
        userRef.keepSynced(true);

        // First load the user data
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if fragment is still attached and binding exists
                if (!isAdded() || binding == null) {
                    Log.e("profileFragment", "Fragment not attached or binding is null");
                    return;
                }

                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(userModel.class);
                    if (currentUser != null) {
                        // Always use the snapshot key as the user ID
                        String userId = snapshot.getKey();
                        Log.d("profileFragment", "User data loaded successfully with ID: " + userId);

                        // Hide shimmer once data is loaded
                        stopShimmer();

                        updateUIWithUserData(currentUser, userId);

                        // Load counts after user data is loaded
                        loadFollowersCount(userId);
                        loadPostsCount(userId);
                        loadFollowingCount(userId);

                        // Notify adapter about user data change
                        if (viewPager != null && viewPager.getAdapter() != null) {
                            viewPager.getAdapter().notifyDataSetChanged();
                        }
                    } else {
                        Log.e("profileFragment", "User data is null");
                        stopShimmer(); // Stop shimmer on error
                        showOfflineMessage();
                    }
                } else {
                    Log.e("profileFragment", "Snapshot doesn't exist");
                    stopShimmer(); // Stop shimmer on error
                    showOfflineMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("profileFragment", "Error loading user data: " + error.getMessage());
                // Check if fragment is still attached and binding exists
                if (isAdded() && binding != null) {
                    stopShimmer(); // Stop shimmer on error
                    showOfflineMessage();
                }
            }
        });
    }

    private void updateUIWithUserData(userModel user, String userId) {
        if (user == null || userId == null) {
            Log.e("profileFragment", "User object or userId is null");
            return;
        }

        // Profile Image
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
            Picasso.get()
                    .load(user.getProfilePhoto())
                    .placeholder(R.drawable.ic_avatar)
                    .into(binding.profileImage);
        }

        // Name and Bio
        binding.name.setText(user.getName());

        // Set username/profession based on user type
        String professionText = "";
        String bioText = "";

        switch (user.getUserType()) {
            case "Student":
                professionText = "@" + user.getCRN().toLowerCase();
                bioText = String.format("%s • %s Year\n%s Student at Allen Business School",
                        user.getCourse(), user.getYear(), user.getCourse());
                break;
            case "Professor":
                professionText = "Professor at Allen Business School";
                bioText = String.format("Professor at Allen Business School");
                break;
            case "Alumni":
                professionText = String.format("%s at %s", user.getJobRole(), user.getCompany());
                bioText = String.format("Allen Business School Alumni\n%s, %s",
                        user.getCourse(), user.getPassingYear());
                break;
        }

        binding.professionTV.setText(professionText);
        binding.BioTV.setText(bioText);

        // Handle button visibility based on whether this is current user's profile
        if (isCurrentUserProfile) {
            // Hide buttons for own profile
            binding.followButton.setVisibility(View.GONE);
            binding.messageButton.setVisibility(View.GONE);
            binding.profileSettingsMenuBtn.setVisibility(View.VISIBLE);
        } else {
            // Show buttons for other users' profiles
            binding.followButton.setVisibility(View.VISIBLE);
            binding.messageButton.setVisibility(View.VISIBLE);
            binding.profileSettingsMenuBtn.setVisibility(View.GONE);

            // Set message button click listener
            binding.messageButton.setOnClickListener(v -> {
                // Open chat detail
                Intent intent = new Intent(getActivity(),
                        com.ramascript.allenconnect.features.chat.chatDetailActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("profilePic", user.getProfilePhoto());
                intent.putExtra("userName", user.getName());
                startActivity(intent);
            });

            // Set follow button appearance based on following status
            checkFollowingStatus(userId);
        }
    }

    private void checkFollowingStatus(String profileUserId) {
        String currentUserId = auth.getCurrentUser().getUid();

        database.getReference()
                .child("Users")
                .child(profileUserId)
                .child("Followers")
                .child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding == null)
                            return;

                        if (snapshot.exists()) {
                            // Already following this user
                            binding.followButton.setText("Following");
                            binding.followButton.setBackgroundResource(android.R.color.transparent);
                            binding.followButton.setTextColor(getResources().getColor(R.color.textColor));

                            // Set click listener for unfollow
                            binding.followButton.setOnClickListener(v -> {
                                unfollowUser(profileUserId);
                            });
                        } else {
                            // Not following yet
                            binding.followButton.setText("Follow");
                            binding.followButton.setBackgroundResource(R.drawable.gradient_btn);
                            binding.followButton.setTextColor(getResources().getColor(R.color.white));

                            // Set click listener for follow
                            binding.followButton.setOnClickListener(v -> {
                                followUser(profileUserId);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("profileFragment", "Error checking follow status: " + error.getMessage());
                    }
                });
    }

    private void followUser(String profileUserId) {
        String currentUserId = auth.getCurrentUser().getUid();
        long currentTime = System.currentTimeMillis();

        // Create follower model
        followerModel follower = new followerModel();
        follower.setFollowedBy(currentUserId);
        follower.setFollowedAt(currentTime);

        // Create following model
        followingModel following = new followingModel();
        following.setFollowingTo(profileUserId);
        following.setFollowingAt(currentTime);

        // Disable button during operation
        binding.followButton.setEnabled(false);

        // Update UI immediately
        binding.followButton.setText("Following");
        binding.followButton.setBackgroundResource(android.R.color.transparent);
        binding.followButton.setTextColor(getResources().getColor(R.color.textColor));

        // Add to follower list of profile user
        database.getReference()
                .child("Users")
                .child(profileUserId)
                .child("Followers")
                .child(currentUserId)
                .setValue(follower)
                .addOnSuccessListener(unused -> {
                    // Add to following list of current user
                    database.getReference()
                            .child("Users")
                            .child(currentUserId)
                            .child("Following")
                            .child(profileUserId)
                            .setValue(following)
                            .addOnSuccessListener(unused1 -> {
                                // Update follower count
                                database.getReference()
                                        .child("Users")
                                        .child(profileUserId)
                                        .child("followersCount")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                int count = 0;
                                                if (snapshot.exists()) {
                                                    count = snapshot.getValue(Integer.class);
                                                }

                                                database.getReference()
                                                        .child("Users")
                                                        .child(profileUserId)
                                                        .child("followersCount")
                                                        .setValue(count + 1)
                                                        .addOnSuccessListener(unused2 -> {
                                                            // Re-enable button
                                                            binding.followButton.setEnabled(true);

                                                            // Update click listener for unfollow
                                                            binding.followButton.setOnClickListener(v -> {
                                                                unfollowUser(profileUserId);
                                                            });

                                                            Toast.makeText(getContext(),
                                                                    "You followed " + currentUser.getName(),
                                                                    Toast.LENGTH_SHORT).show();
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                binding.followButton.setEnabled(true);
                                                Toast.makeText(getContext(), "Error: " + error.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    binding.followButton.setEnabled(true);
                    binding.followButton.setText("Follow");
                    binding.followButton.setBackgroundResource(R.drawable.gradient_btn);
                    binding.followButton.setTextColor(getResources().getColor(R.color.white));

                    Toast.makeText(getContext(), "Failed to follow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void unfollowUser(String profileUserId) {
        String currentUserId = auth.getCurrentUser().getUid();

        // Disable button during operation
        binding.followButton.setEnabled(false);

        // Update UI immediately
        binding.followButton.setText("Follow");
        binding.followButton.setBackgroundResource(R.drawable.gradient_btn);
        binding.followButton.setTextColor(getResources().getColor(R.color.white));

        // Remove from follower list of profile user
        database.getReference()
                .child("Users")
                .child(profileUserId)
                .child("Followers")
                .child(currentUserId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // Remove from following list of current user
                    database.getReference()
                            .child("Users")
                            .child(currentUserId)
                            .child("Following")
                            .child(profileUserId)
                            .removeValue()
                            .addOnSuccessListener(unused1 -> {
                                // Update follower count
                                database.getReference()
                                        .child("Users")
                                        .child(profileUserId)
                                        .child("followersCount")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                int count = 0;
                                                if (snapshot.exists()) {
                                                    count = snapshot.getValue(Integer.class);
                                                }

                                                if (count > 0) {
                                                    database.getReference()
                                                            .child("Users")
                                                            .child(profileUserId)
                                                            .child("followersCount")
                                                            .setValue(count - 1)
                                                            .addOnSuccessListener(unused2 -> {
                                                                // Re-enable button
                                                                binding.followButton.setEnabled(true);

                                                                // Update click listener for follow
                                                                binding.followButton.setOnClickListener(v -> {
                                                                    followUser(profileUserId);
                                                                });

                                                                Toast.makeText(getContext(),
                                                                        "You unfollowed " + currentUser.getName(),
                                                                        Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                binding.followButton.setEnabled(true);
                                                Toast.makeText(getContext(), "Error: " + error.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    binding.followButton.setEnabled(true);
                    binding.followButton.setText("Following");
                    binding.followButton.setBackgroundResource(android.R.color.transparent);
                    binding.followButton.setTextColor(getResources().getColor(R.color.textColor));

                    Toast.makeText(getContext(), "Failed to unfollow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFollowersCount(String uid) {
        Log.d("profileFragment", "Loading followers count for uid: " + uid);

        DatabaseReference followersRef = database.getReference().child("Users").child(uid).child("Followers");
        followersRef.keepSynced(true);

        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null)
                    return;

                int count = (int) snapshot.getChildrenCount(); // Count child nodes
                Log.d("profileFragment", "Followers count from DB: " + count);

                binding.followersCountTV.setText(String.valueOf(count)); // Update UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("profileFragment", "Error loading followers count: " + error.getMessage());
                if (isAdded() && binding != null) {
                    binding.followersCountTV.setText("0");
                }
            }
        });
    }

    private void loadFollowingCount(String uid) {
        Log.d("profileFragment", "Loading following count for uid: " + uid);

        DatabaseReference followingRef = database.getReference().child("Users").child(uid).child("Following");
        followingRef.keepSynced(true);

        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null)
                    return;

                int count = (int) snapshot.getChildrenCount(); // Count child nodes
                Log.d("profileFragment", "Following count from DB: " + count);

                binding.followingCountTV.setText(String.valueOf(count)); // Update UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("profileFragment", "Error loading following count: " + error.getMessage());
                if (isAdded() && binding != null) {
                    binding.followingCountTV.setText("0");
                }
            }
        });
    }

    private void loadPostsCount(String uid) {
        DatabaseReference postsRef = database.getReference().child("Posts");
        postsRef.keepSynced(true);

        postsRef.orderByChild("postedBy").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null)
                    return;

                long count = snapshot.getChildrenCount();
                binding.postsCountTV.setText(String.valueOf(count));

                // Update posts count in database
                database.getReference().child("Users").child(uid).child("postsCount").setValue(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("profileFragment", "Error loading posts count: " + error.getMessage());
                if (isAdded() && binding != null) {
                    binding.postsCountTV.setText("0");
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.profileSettingsMenuBtn.setOnClickListener(v -> showProfileMenu(v));

        // Add click listeners for followers and following counts
        binding.followersCountTV.setOnClickListener(v -> showFollowersList());

        // For the followers label
        View followersLabel = ((View) binding.followersCountTV.getParent()).findViewById(
                R.id.followersLabel);
        if (followersLabel instanceof LinearLayout) {
            followersLabel.setOnClickListener(v -> showFollowersList());
        }

        binding.followingCountTV.setOnClickListener(v -> showFollowingList());

        // For the following label
        View followingLabel = ((View) binding.followingCountTV.getParent()).findViewById(
                R.id.followingLabel);
        if (followingLabel instanceof LinearLayout) {
            followingLabel.setOnClickListener(v -> showFollowingList());
        }
    }

    private void showFollowersList() {
        if (userId == null || userId.isEmpty()) {
            Log.e("profileFragment", "Cannot show followers list: userId is null or empty");
            Toast.makeText(getContext(), "Unable to load followers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show followers list with tabbed interface
        UserListFragment fragment = UserListFragment.newInstance(userId, "followers");

        try {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e("profileFragment", "Error showing followers list: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading followers", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFollowingList() {
        if (userId == null || userId.isEmpty()) {
            Log.e("profileFragment", "Cannot show following list: userId is null or empty");
            Toast.makeText(getContext(), "Unable to load following", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show following list with tabbed interface, starting on the following tab
        UserListFragment fragment = UserListFragment.newInstance(userId, "following");

        try {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e("profileFragment", "Error showing following list: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading following", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks if this fragment is displaying the current user's profile
     * 
     * @return true if this is the current user's profile, false otherwise
     */
    public boolean isCurrentUserProfile() {
        return isCurrentUserProfile;
    }

    private void showProfileMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_profile) {
                startActivity(new Intent(getContext(), editProfileActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_change_profile_picture) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 11);
                return true;
            } else if (item.getItemId() == R.id.action_developers) {
                startActivity(new Intent(getContext(), meetDevsActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_delete_account) {
                startActivity(new Intent(getContext(), deleteAccountActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                auth.signOut();
                startActivity(new Intent(getActivity(), loginAs.class));
                requireActivity().finish();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            Uri uri = data.getData();
            binding.profileImage.setImageURI(uri); // Using binding

            // Show the dialog after the image is selected
            dialog.show();

            final StorageReference reference = storage.getReference().child("profile_pictures")
                    .child(FirebaseAuth.getInstance().getUid());

            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), "Profile photo changed", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Users")
                                    .child(auth.getUid()).child("profilePhoto").setValue(uri.toString());
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (profileShimmerLayout != null && profileShimmerLayout.getVisibility() == View.VISIBLE && binding != null) {
            profileShimmerLayout.startShimmer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (profileShimmerLayout != null && binding != null) {
            profileShimmerLayout.stopShimmer();
        }
    }

    // ViewPager2 Adapter
    private class ProfilePagerAdapter extends FragmentStateAdapter {
        public ProfilePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    PostsFragment postsFragment = new PostsFragment();
                    // Pass the userId to the posts fragment
                    Bundle args = new Bundle();
                    args.putString("userId", userId);
                    postsFragment.setArguments(args);
                    return postsFragment;
                case 1:
                    DetailsFragment detailsFragment = new DetailsFragment();
                    // Pass the user data to details fragment
                    if (currentUser != null) {
                        Bundle detailsArgs = new Bundle();
                        detailsArgs.putString("userType", currentUser.getUserType());
                        detailsFragment.setArguments(detailsArgs);
                    }
                    return detailsFragment;
                default:
                    return new PostsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Two tabs: Posts and Details
        }

        // Add for better handling of fragment recreation
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean containsItem(long itemId) {
            return itemId < getItemCount();
        }
    }

    // Posts Fragment
    public static class PostsFragment extends Fragment {
        private RecyclerView postsRecyclerView;
        private View emptyStateView;
        private ArrayList<postModel> postList;
        private com.ramascript.allenconnect.features.post.postAdapter postAdapter;
        private FirebaseAuth auth;
        private FirebaseDatabase database;
        private String userId;
        private boolean isCurrentUserProfile;
        private int lastScrollDy = 0;
        private boolean isNavBarHidden = false;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_posts, container, false);

            // Ensure no top padding is applied
            view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), view.getPaddingBottom());

            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();

            // Get userId from arguments
            if (getArguments() != null) {
                userId = getArguments().getString("userId");
            } else {
                // Default to current user if not specified
                userId = auth.getCurrentUser().getUid();
            }

            // Check if viewing own profile
            isCurrentUserProfile = userId.equals(auth.getCurrentUser().getUid());

            // Initialize views
            postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
            postsRecyclerView.setNestedScrollingEnabled(true);
            postsRecyclerView.setClipToPadding(false);
            postsRecyclerView.setPadding(postsRecyclerView.getPaddingLeft(), 0,
                    postsRecyclerView.getPaddingRight(), postsRecyclerView.getPaddingBottom());

            emptyStateView = view.findViewById(R.id.emptyStateView);

            // Change empty state text for other users' profiles
            if (!isCurrentUserProfile) {
                TextView emptyTitle = emptyStateView.findViewById(R.id.emptyTitleText);
                TextView emptyDesc = emptyStateView.findViewById(R.id.emptyDescText);

                if (emptyTitle != null) {
                    emptyTitle.setText("No Posts Yet");
                }

                if (emptyDesc != null) {
                    emptyDesc.setText("This user hasn't shared any posts yet");
                }
            }

            // Initialize data
            postList = new ArrayList<>();

            // Setup RecyclerView
            setupRecyclerView();

            // Load posts
            loadUserPosts();

            return view;
        }

        private void setupRecyclerView() {
            postAdapter = new postAdapter(postList, requireContext());
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);

            // Clear any existing item decorations
            while (postsRecyclerView.getItemDecorationCount() > 0) {
                postsRecyclerView.removeItemDecorationAt(0);
            }

            // Reset padding to ensure no unwanted spacing
            postsRecyclerView.setPadding(8, 0, 8, 80);

            // Set fixed layout parameters
            postsRecyclerView.setLayoutManager(layoutManager);
            postsRecyclerView.setAdapter(postAdapter);

            // Add direct scroll listener with threshold
            postsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                private static final int SCROLL_THRESHOLD = 8;
                private boolean scrollingUp = false;
                private boolean scrollingDown = false;

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (getActivity() == null)
                        return;

                    // Direct manipulation of bottom navigation view
                    View bottomNav = getActivity().findViewById(R.id.bottomNavView);
                    if (bottomNav == null)
                        return;

                    // Accumulate scroll direction for smoother detection
                    lastScrollDy += dy;

                    // Reset if direction changes
                    if ((lastScrollDy > 0 && dy < 0) || (lastScrollDy < 0 && dy > 0)) {
                        lastScrollDy = dy;
                    }

                    // Use threshold to avoid jitter
                    if (lastScrollDy > SCROLL_THRESHOLD && !isNavBarHidden) {
                        // Scrolling down - hide nav bar
                        isNavBarHidden = true;
                        bottomNav.animate()
                                .translationY(bottomNav.getHeight() + 100)
                                .setDuration(200)
                                .start();
                        lastScrollDy = 0;
                    } else if (lastScrollDy < -SCROLL_THRESHOLD && isNavBarHidden) {
                        // Scrolling up - show nav bar
                        isNavBarHidden = false;
                        bottomNav.animate()
                                .translationY(0)
                                .setDuration(200)
                                .start();
                        lastScrollDy = 0;
                    }
                }

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    // When scrolling stops
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && getActivity() != null) {
                        // Reset accumulated scroll
                        lastScrollDy = 0;

                        // Show bottom nav when at top of list
                        if (!recyclerView.canScrollVertically(-1)) {
                            View bottomNav = getActivity().findViewById(R.id.bottomNavView);
                            if (bottomNav != null && bottomNav.getTranslationY() > 0) {
                                isNavBarHidden = false;
                                bottomNav.animate()
                                        .translationY(0)
                                        .setDuration(200)
                                        .start();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();

            // Show bottom navigation when resuming the fragment
            if (getActivity() != null) {
                View bottomNav = getActivity().findViewById(R.id.bottomNavView);
                if (bottomNav != null && bottomNav.getTranslationY() > 0) {
                    isNavBarHidden = false;
                    bottomNav.animate()
                            .translationY(0)
                            .setDuration(200)
                            .start();
                }
            }

            // Reload posts data when returning to ensure fresh data
            if (userId != null) {
                loadUserPosts();
            }
        }

        private void loadUserPosts() {
            if (userId == null)
                return;

            DatabaseReference postsRef = database.getReference().child("Posts");
            postsRef.keepSynced(true);

            postsRef.orderByChild("postedBy").equalTo(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded() || postsRecyclerView == null) {
                        return; // Fragment not attached or views destroyed
                    }

                    postList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        try {
                            postModel post = new postModel();
                            post.setPostId(dataSnapshot.getKey());
                            post.setPostImage(dataSnapshot.child("postImage").getValue(String.class));
                            post.setPostCaption(dataSnapshot.child("postCaption").getValue(String.class));
                            post.setPostedBy(dataSnapshot.child("postedBy").getValue(String.class));

                            if (dataSnapshot.child("postedAt").exists()) {
                                Long postedAt = dataSnapshot.child("postedAt").getValue(Long.class);
                                if (postedAt != null) {
                                    post.setPostedAt(postedAt);
                                }
                            }

                            if (dataSnapshot.child("postLikes").exists()) {
                                Long postLikes = dataSnapshot.child("postLikes").getValue(Long.class);
                                post.setPostLikes(postLikes != null ? postLikes.intValue() : 0);
                            }

                            // Handle comment count
                            if (dataSnapshot.child("commentCount").exists()) {
                                Long commentCount = dataSnapshot.child("commentCount").getValue(Long.class);
                                post.setCommentCount(commentCount != null ? commentCount.intValue() : 0);
                            } else {
                                // If commentCount doesn't exist, count the comments manually
                                int count = (int) dataSnapshot.child("comments").getChildrenCount();
                                post.setCommentCount(count);

                                // Update the commentCount in database
                                if (isAdded()) {
                                    database.getReference()
                                            .child("Posts")
                                            .child(post.getPostId())
                                            .child("commentCount")
                                            .setValue(count);
                                }
                            }

                            postList.add(post);
                        } catch (Exception e) {
                            Log.e("PostsFragment", "Error parsing post: " + e.getMessage());
                        }
                    }
                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("PostsFragment", "Error loading posts: " + error.getMessage());
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Error loading posts", Toast.LENGTH_SHORT).show();
                    }
                    updateUI(); // Still call updateUI to handle empty state
                }
            });
        }

        private void updateUI() {
            if (!isAdded() || emptyStateView == null || postsRecyclerView == null) {
                return; // Fragment not attached or views destroyed
            }

            if (postList.isEmpty()) {
                emptyStateView.setVisibility(View.VISIBLE);
                postsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyStateView.setVisibility(View.GONE);
                postsRecyclerView.setVisibility(View.VISIBLE);
                if (postAdapter != null) {
                    postAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            // Clean up resources
            postList.clear();
            postsRecyclerView.setAdapter(null);
        }
    }

    // Details Fragment
    public static class DetailsFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.view_user_details, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Get parent fragment
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof profileFragment) {
                profileFragment parent = (profileFragment) parentFragment;
                showUserDetails(view, parent.currentUser);
            }
        }

        private void showUserDetails(View view, userModel user) {
            if (user == null) {
                Log.e("profileFragment", "User is null in details fragment");
                return;
            }

            // Find common TextViews for all user types
            TextView nameTV = view.findViewById(R.id.nameTV);
            TextView emailTV = view.findViewById(R.id.emailTV);
            TextView phoneTV = view.findViewById(R.id.phoneTV);

            // Set common fields
            nameTV.setText(user.getName());
            emailTV.setText(user.getEmail());
            phoneTV.setText(user.getPhoneNo());

            View studentFields = view.findViewById(R.id.studentFields);
            View professorFields = view.findViewById(R.id.professorFields);
            View alumniFields = view.findViewById(R.id.alumniFields);

            // Hide all fields first
            studentFields.setVisibility(View.GONE);
            professorFields.setVisibility(View.GONE);
            alumniFields.setVisibility(View.GONE);

            // Show fields based on user type
            switch (user.getUserType()) {
                case "Student":
                    studentFields.setVisibility(View.VISIBLE);
                    TextView crnTV = view.findViewById(R.id.crnTV);
                    TextView courseTV = view.findViewById(R.id.courseTV);
                    TextView yearTV = view.findViewById(R.id.yearTV);

                    crnTV.setText(user.getCRN());
                    courseTV.setText(user.getCourse());
                    yearTV.setText(user.getYear());
                    break;

                case "Professor":
                    professorFields.setVisibility(View.VISIBLE);
                    TextView departmentTV = view.findViewById(R.id.departmentTV);
                    departmentTV.setText(user.getCourse()); // Using course field for department
                    break;

                case "Alumni":
                    alumniFields.setVisibility(View.VISIBLE);
                    TextView alumniCourseTV = view.findViewById(R.id.alumniCourseTV);
                    TextView companyTV = view.findViewById(R.id.companyTV);
                    TextView jobRoleTV = view.findViewById(R.id.jobRoleTV);
                    TextView passingYearTV = view.findViewById(R.id.passingYearTV);

                    alumniCourseTV.setText(user.getCourse());
                    companyTV.setText(user.getCompany());
                    jobRoleTV.setText(user.getJobRole());
                    passingYearTV.setText(user.getPassingYear());
                    break;
            }
        }
    }

    // Add a method to show offline message
    private void showOfflineMessage() {
        if (binding != null) {
            // Show offline error message in shimmer layout
            View errorMessage = binding.getRoot().findViewById(R.id.shimmer_error_message);
            if (errorMessage != null) {
                errorMessage.setVisibility(View.VISIBLE);
            }

            // Check if we have cached data and display it
            if (currentUser != null) {
                stopShimmer();
                updateUIWithUserData(currentUser, userId);
            } else {
                // If no cached data, show a toast message
                Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}