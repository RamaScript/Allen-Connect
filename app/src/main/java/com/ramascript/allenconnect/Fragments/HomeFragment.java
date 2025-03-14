package com.ramascript.allenconnect.Fragments;

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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ramascript.allenconnect.Adapters.PostAdapter;
import com.ramascript.allenconnect.Chat.Chat;
import com.ramascript.allenconnect.Models.PostModel;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    ArrayList<PostModel> postList;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    Uri selectedImageUri;
    ProgressDialog dialog;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize progress dialog
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Creating Post");
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);

        // Show progress bar while loading posts
        binding.progressBar.setVisibility(View.VISIBLE);

        // Load user profile image and details
        if (auth.getCurrentUser() != null) {
            database.getReference().child("Users").child(auth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserModel userModel = snapshot.getValue(UserModel.class);
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
        PostModel post = new PostModel();
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

    private void savePostToDatabase(PostModel post) {
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
}
