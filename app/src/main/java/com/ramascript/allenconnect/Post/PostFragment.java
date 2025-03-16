package com.ramascript.allenconnect.Post;

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
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ramascript.allenconnect.Home.HomeFragment;
import com.ramascript.allenconnect.Utils.BaseFragment;
import com.ramascript.allenconnect.MainActivity;
import com.ramascript.allenconnect.User.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentPostBinding;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class PostFragment extends BaseFragment {

    FragmentPostBinding binding;
    Uri uri;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(getContext());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentPostBinding.inflate(inflater, container, false);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Post Uploading");
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // here i am fetching user data from db and showing in post fragment
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            Picasso.get().load(userModel.getProfilePhoto()).placeholder(R.drawable.ic_avatar)
                                    .into(binding.profileImage);
                            binding.name.setText(userModel.getName());

                            // Set the text based on user type
                            if ("Student".equals(userModel.getUserType())) {
                                binding.title.setText(
                                        String.format("%s (%s year)", userModel.getCourse(), userModel.getYear()));
                            } else if ("Alumni".equals(userModel.getUserType())) {
                                binding.title.setText(userModel.getJobRole() + " at " + userModel.getCompany());
                            } else if ("Professor".equals(userModel.getUserType())) {
                                binding.title.setText("Professor at AGOI");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.caption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String caption = binding.caption.getText().toString();
                if (!caption.isEmpty()) {
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btnbg));
                    binding.postBtn.setEnabled(true);
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.white_my));
                } else {
                    binding.postBtn
                            .setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.border_black));
                    binding.postBtn.setEnabled(false);
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.addImgbtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 10);
        });

        binding.postBtn.setOnClickListener(v -> {
            // Show loading dialog
            dialog.show();

            // Get caption
            String caption = binding.caption.getText().toString().trim();

            // Check if both caption and image are empty
            if (caption.isEmpty() && uri == null) {
                dialog.dismiss();
                Toast.makeText(getContext(), "Please provide a caption or select an image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Initialize post model
            PostModel postModel = new PostModel();
            postModel.setPostedBy(auth.getUid());
            postModel.setPostedAt(new Date().getTime());

            // Set caption if it exists
            if (!caption.isEmpty()) {
                postModel.setPostCaption(caption);
            }

            // Handle image upload if URI is not null
            if (uri != null) {
                final StorageReference reference = storage.getReference()
                        .child("Posts")
                        .child(auth.getUid())
                        .child(new Date().getTime() + " ");

                reference.putFile(uri).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            // Set image URL
                            postModel.setPostImage(downloadUri.toString());

                            // Save post to database
                            savePostToDatabase(postModel);
                        })
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                        }))
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // If no image, directly save the post
                savePostToDatabase(postModel);
            }
        });

        return binding.getRoot();
    }

    private void savePostToDatabase(PostModel postModel) {
        database.getReference().child("Posts").push()
                .setValue(postModel)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Posted Successfully", Toast.LENGTH_SHORT).show();

                    // Replace PostFragment with HomeFragment
                    if (getActivity() != null && getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToFragment(new HomeFragment(), R.id.navigation_home);
                    }
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Failed to save post", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if data is null
        if (data != null && data.getData() != null) {
            uri = data.getData();
            binding.postImage.setImageURI(uri);
            binding.postImage.setVisibility(View.VISIBLE);

            binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btnbg));
            binding.postBtn.setEnabled(true);
            binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.white_my));
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

}