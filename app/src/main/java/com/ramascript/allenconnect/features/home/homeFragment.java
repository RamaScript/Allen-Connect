package com.ramascript.allenconnect.features.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ramascript.allenconnect.features.post.postAdapter;
import com.ramascript.allenconnect.features.chat.chatActivity;
import com.ramascript.allenconnect.features.notification.notificationFragment;
import com.ramascript.allenconnect.features.post.postModel;
import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentHomeBinding;
import com.ramascript.allenconnect.base.mainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class homeFragment extends Fragment {

    FragmentHomeBinding binding;
    ArrayList<postModel> postList;
    ArrayList<postModel> displayedPostList;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    Uri selectedImageUri;
    ProgressDialog dialog;
    private static int instanceCounter = 0;
    private final int instanceId;
    private postAdapter postAdapter;
    private boolean isInitialPostLoad = true;
    private ValueEventListener postsListener;
    private ValueEventListener userDataListener;
    private ShimmerFrameLayout shimmerFrameLayout;

    public homeFragment() {
        // Required empty public constructor
        instanceId = ++instanceCounter;
        System.out.println("homeFragment instance #" + instanceId + " created");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("homeFragment onCreate called with savedInstanceState: " +
                (savedInstanceState == null ? "null" : "not null"));
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Keep Posts data synced for offline access
        try {
            database.getReference().child("Posts").keepSynced(true);

            // Also keep current user data synced
            if (auth.getCurrentUser() != null) {
                database.getReference().child("Users").child(auth.getCurrentUser().getUid()).keepSynced(true);
            }
        } catch (Exception e) {
            System.out.println("Error setting keepSynced: " + e.getMessage());
        }

        // Reset initial post load flag when fragment is created
        isInitialPostLoad = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("homeFragment onCreateView called");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize progress dialog
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Creating Post");
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);

        // Make RecyclerView initially hidden
        binding.dashBoardRV.setVisibility(View.GONE);

        // Initialize shimmer layout
        shimmerFrameLayout = view.findViewById(R.id.shimmerFrameLayout);
        if (shimmerFrameLayout == null) {
            // If direct access doesn't work, try through the included layout
            View shimmerView = view.findViewById(R.id.shimmerLayout);
            if (shimmerView != null) {
                shimmerFrameLayout = shimmerView.findViewById(R.id.shimmerFrameLayout);
            }
        }
        startShimmer();

        // Set click listener for hamburger menu
        binding.sideNavMenu.setOnClickListener(v -> {
            if (getActivity() instanceof mainActivity) {
                ((mainActivity) getActivity()).toggleDrawer();
            }
        });

        // Load user profile image and details
        if (auth.getCurrentUser() != null) {
            userDataListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Add null check for binding here to prevent crashes
                    if (binding == null || !isAdded() || getContext() == null) {
                        return; // Fragment is detached or being destroyed
                    }

                    if (snapshot.exists()) {
                        userModel userModel = snapshot
                                .getValue(com.ramascript.allenconnect.features.user.userModel.class);
                        if (userModel != null) {
                            if (getContext() != null && userModel.getProfilePhoto() != null) {
                                Glide.with(getContext())
                                        .load(userModel.getProfilePhoto())
                                        .placeholder(R.drawable.ic_avatar)
                                        .into(binding.userProfileImage);
                            }
                            binding.name.setText(userModel.getName());

                            // Set the text based on user type
                            if ("Student".equals(userModel.getUserType())) {
                                binding.title.setText(String.format("%s (%s year)", userModel.getCourse(),
                                        userModel.getYear()));
                            } else if ("Alumni".equals(userModel.getUserType())) {
                                binding.title.setText(userModel.getJobRole() + " at " + userModel.getCompany());
                            } else if ("Professor".equals(userModel.getUserType())) {
                                binding.title.setText("Professor at AGOI");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };

            database.getReference().child("Users").child(auth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(userDataListener);
        }

        // Set click listeners for notification and chat
        binding.notificationHomeIV.setOnClickListener(v -> {
            if (getActivity() instanceof mainActivity) {
                Fragment notificationFragment = new notificationFragment();
                ((mainActivity) getActivity()).loadFragment(notificationFragment, false);
            }
        });

        binding.chatHomeIV.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), chatActivity.class));
        });

        // Handle image selection
        binding.addImgbtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 10);
        });

        // Handle post creation
        binding.caption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String caption = binding.caption.getText().toString();
                if (!caption.isEmpty() || selectedImageUri != null) {
                    enablePostButton();
                } else {
                    disablePostButton();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Setup RecyclerView for posts
        postList = new ArrayList<>();
        displayedPostList = new ArrayList<>();
        postAdapter = new postAdapter(displayedPostList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.dashBoardRV.setLayoutManager(linearLayoutManager);
        binding.dashBoardRV.setAdapter(postAdapter);

        // Add this code to properly handle the scroll behavior
        binding.nestedScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // When scrolling down, hide the bottom navigation
                if (scrollY > oldScrollY) {
                    if (getActivity() != null && getActivity() instanceof mainActivity) {
                        ((mainActivity) getActivity()).hideBottomNavigation();
                    }
                }
                // When scrolling up, show the bottom navigation
                else if (scrollY < oldScrollY) {
                    if (getActivity() != null && getActivity() instanceof mainActivity) {
                        ((mainActivity) getActivity()).showBottomNavigation();
                    }
                }
            }
        });

        // Load posts
        loadPosts();

        // Add post button click listener
        binding.postBtn.setOnClickListener(v -> {
            if (!binding.caption.getText().toString().trim().isEmpty() || selectedImageUri != null) {
                createPost();
            } else {
                Toast.makeText(getContext(), "Please add some content to post", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("homeFragment onViewCreated called");

        // Rest of your code...
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            binding.postImage.setImageURI(selectedImageUri);
            binding.postImage.setVisibility(View.VISIBLE);
            enablePostButton();
        }
    }

    private void enablePostButton() {
        binding.postBtn.setVisibility(View.VISIBLE);
    }

    private void disablePostButton() {
        binding.postBtn.setVisibility(View.GONE);
    }

    private void createPost() {
        dialog.show();

        String caption = binding.caption.getText().toString().trim();
        postModel post = new postModel();
        post.setPostedBy(auth.getUid());
        post.setPostedAt(new Date().getTime());

        if (!caption.isEmpty()) {
            post.setPostCaption(caption);
        }

        if (selectedImageUri != null) {
            StorageReference reference = storage.getReference()
                    .child("Posts")
                    .child(auth.getUid())
                    .child(new Date().getTime() + "");

            reference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    post.setPostImage(uri.toString());
                    savePostToDatabase(post);
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            savePostToDatabase(post);
        }
    }

    private void savePostToDatabase(postModel post) {
        database.getReference()
                .child("Posts")
                .push()
                .setValue(post)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Posted Successfully", Toast.LENGTH_SHORT).show();
                    clearPostForm();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Error creating post", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearPostForm() {
        binding.caption.setText("");
        binding.postImage.setVisibility(View.GONE);
        selectedImageUri = null;
        disablePostButton();

        // Clear focus from EditText
        binding.caption.clearFocus();

        // Hide keyboard
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity()
                    .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void loadPosts() {
        // Cancel any existing listener to avoid duplicates
        if (postsListener != null) {
            database.getReference().child("Posts").removeEventListener(postsListener);
        }

        // Show shimmer effect before loading
        showShimmerEffect();

        // Record the start time to ensure minimum shimmer display duration
        long startTime = System.currentTimeMillis();

        // Reference to Posts node
        final DatabaseReference postsRef = database.getReference().child("Posts");

        // Make sure it's synced for offline access
        postsRef.keepSynced(true);

        postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) {
                    return;
                }

                if (isInitialPostLoad) {
                    // For initial load, clear and reload all posts
                    postList.clear();
                    displayedPostList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        postModel postModel = dataSnapshot
                                .getValue(com.ramascript.allenconnect.features.post.postModel.class);
                        if (postModel != null) {
                            postModel.setPostId(dataSnapshot.getKey());
                            postList.add(postModel);
                            displayedPostList.add(postModel);
                        }
                    }

                    // Sort posts by time (newest first)
                    Collections.reverse(displayedPostList);

                    // Update UI
                    postAdapter.notifyDataSetChanged();

                    // Calculate elapsed time
                    long elapsed = System.currentTimeMillis() - startTime;
                    // Ensure shimmer displays for at least 1 second (1000ms)
                    long minimumShimmerTime = 1000;

                    if (elapsed < minimumShimmerTime) {
                        // Delay hiding shimmer to ensure it's visible long enough to be noticed
                        new android.os.Handler().postDelayed(() -> {
                            if (binding != null) {
                                // Hide shimmer effect and show RecyclerView
                                hideShimmerEffect();
                                binding.progressBar.setVisibility(View.GONE);
                            }
                        }, minimumShimmerTime - elapsed);
                    } else {
                        // Hide shimmer effect and show RecyclerView immediately if enough time has
                        // passed
                        hideShimmerEffect();
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    // Mark initial load as complete
                    isInitialPostLoad = false;
                } else {
                    // For subsequent updates, update incrementally to avoid jitter
                    // Check for new posts or updated like/comment counts
                    boolean hasChanges = false;

                    // First, check if we need to add any new posts
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String postId = dataSnapshot.getKey();
                        boolean found = false;

                        // Look for this post in our existing list
                        for (postModel existingPost : postList) {
                            if (existingPost.getPostId().equals(postId)) {
                                found = true;

                                // Update like and comment counts
                                Integer likeCount = dataSnapshot.child("postLikes").getValue(Integer.class);
                                Integer commentCount = dataSnapshot.child("commentCount").getValue(Integer.class);

                                if (likeCount != null && likeCount != existingPost.getPostLikes()) {
                                    existingPost.setPostLikes(likeCount);
                                    hasChanges = true;
                                }

                                if (commentCount != null && commentCount != existingPost.getCommentCount()) {
                                    existingPost.setCommentCount(commentCount);
                                    hasChanges = true;
                                }

                                break;
                            }
                        }

                        // If post not found, it's new - add it
                        if (!found) {
                            postModel newPost = dataSnapshot.getValue(postModel.class);
                            if (newPost != null) {
                                newPost.setPostId(postId);
                                postList.add(0, newPost); // Add to beginning of list
                                displayedPostList.add(0, newPost);
                                hasChanges = true;
                            }
                        }
                    }

                    // If we detected changes, update the adapter
                    if (hasChanges) {
                        postAdapter.notifyDataSetChanged();
                    }

                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Check if fragment is still attached and binding is not null
                if (!isAdded() || binding == null) {
                    return;
                }
                hideShimmerEffect();
                binding.progressBar.setVisibility(View.GONE);
            }
        };

        // Add listener to database
        postsRef.addValueEventListener(postsListener);
    }

    private void startShimmer() {
        if (binding == null)
            return;

        // Hide the actual post creation card
        binding.createPostCard.setVisibility(View.GONE);

        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            binding.dashBoardRV.setVisibility(View.GONE);
        } else {
            // Fallback to included layout visibility if shimmerFrameLayout is not
            // accessible
            View shimmerView = binding.getRoot().findViewById(R.id.shimmerLayout);
            if (shimmerView != null) {
                shimmerView.setVisibility(View.VISIBLE);
            }
            binding.dashBoardRV.setVisibility(View.GONE);
        }
    }

    private void stopShimmer() {
        if (binding == null)
            return;

        // Show the actual post creation card
        binding.createPostCard.setVisibility(View.VISIBLE);

        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            binding.dashBoardRV.setVisibility(View.VISIBLE);
        } else {
            // Fallback to included layout visibility if shimmerFrameLayout is not
            // accessible
            View shimmerView = binding.getRoot().findViewById(R.id.shimmerLayout);
            if (shimmerView != null) {
                shimmerView.setVisibility(View.GONE);
            }
            binding.dashBoardRV.setVisibility(View.VISIBLE);
        }
    }

    // Update the showShimmerEffect and hideShimmerEffect methods
    private void showShimmerEffect() {
        startShimmer();
    }

    private void hideShimmerEffect() {
        stopShimmer();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Reset initial load flag when resuming to ensure data reloads
        isInitialPostLoad = true;

        // Reload posts when coming back from other fragments
        if (database != null) {
            loadPosts();
        }

        // Start shimmer effect in resume if posts are still loading
        if (isInitialPostLoad && binding != null) {
            if (shimmerFrameLayout != null) {
                if (shimmerFrameLayout.getVisibility() == View.VISIBLE) {
                    shimmerFrameLayout.startShimmer();
                    binding.createPostCard.setVisibility(View.GONE);
                }
            } else {
                View shimmerView = binding.getRoot().findViewById(R.id.shimmerLayout);
                if (shimmerView != null && shimmerView.getVisibility() == View.VISIBLE) {
                    ShimmerFrameLayout frameLayout = shimmerView.findViewById(R.id.shimmerFrameLayout);
                    if (frameLayout != null) {
                        frameLayout.startShimmer();
                        binding.createPostCard.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop shimmer effect in pause
        if (binding != null) {
            if (shimmerFrameLayout != null) {
                shimmerFrameLayout.stopShimmer();
            } else {
                View shimmerView = binding.getRoot().findViewById(R.id.shimmerLayout);
                if (shimmerView != null) {
                    ShimmerFrameLayout frameLayout = shimmerView.findViewById(R.id.shimmerFrameLayout);
                    if (frameLayout != null) {
                        frameLayout.stopShimmer();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        System.out.println("homeFragment onDestroyView called");

        // Stop shimmer animation
        hideShimmerEffect();

        // Clean up listeners
        if (postsListener != null && database != null) {
            database.getReference().child("Posts").removeEventListener(postsListener);
            postsListener = null;
        }

        // Clean up user data listener if it exists
        if (userDataListener != null && database != null && auth != null && auth.getCurrentUser() != null) {
            database.getReference().child("Users").child(auth.getCurrentUser().getUid())
                    .removeEventListener(userDataListener);
            userDataListener = null;
        }

        // Clear binding
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("homeFragment instance #" + instanceId + " destroyed");
    }
}
