package com.ramascript.allenconnect.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityEditProfileBinding;
import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri selectedImage;
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Load current user data
        loadUserData();

        // Set up click listeners
        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        binding.saveButton.setOnClickListener(v -> saveUserData());

        // Set up back button
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel user = snapshot.getValue(UserModel.class);
                            if (user != null) {
                                // Load profile image
                                if (user.getProfilePhoto() != null) {
                                    Picasso.get().load(user.getProfilePhoto())
                                            .placeholder(R.drawable.ic_avatar)
                                            .into(binding.profileImage);
                                }

                                // Set basic info
                                binding.nameET.setText(user.getName());

                                // Show/hide fields based on user type
                                if ("Student".equals(user.getUserType())) {
                                    binding.studentFields.setVisibility(View.VISIBLE);
                                    binding.alumniFields.setVisibility(View.GONE);
                                    binding.courseET.setText(user.getCourse());
                                    binding.yearET.setText(user.getYear());
                                } else if ("Alumni".equals(user.getUserType())) {
                                    binding.studentFields.setVisibility(View.GONE);
                                    binding.alumniFields.setVisibility(View.VISIBLE);
                                    binding.companyET.setText(user.getCompany());
                                    binding.jobRoleET.setText(user.getJobRole());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProfileActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData() {
        String name = binding.nameET.getText().toString();
        if (name.isEmpty()) {
            binding.nameET.setError("Name cannot be empty");
            return;
        }

        // Show progress
        binding.saveButton.setEnabled(false);
        binding.saveButton.setText("Saving...");

        // If new image is selected, upload it first
        if (selectedImage != null) {
            StorageReference reference = storage.getReference().child("profile_photos")
                    .child(auth.getUid());

            reference.putFile(selectedImage).addOnSuccessListener(taskSnapshot -> {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateUserData(name, uri.toString());
                });
            });
        } else {
            updateUserData(name, null);
        }
    }

    private void updateUserData(String name, String profilePhotoUrl) {
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            user.setName(name);
                            if (profilePhotoUrl != null) {
                                user.setProfilePhoto(profilePhotoUrl);
                            }

                            // Update based on user type
                            if ("Student".equals(user.getUserType())) {
                                user.setCourse(binding.courseET.getText().toString());
                                user.setYear(binding.yearET.getText().toString());
                            } else if ("Alumni".equals(user.getUserType())) {
                                user.setCompany(binding.companyET.getText().toString());
                                user.setJobRole(binding.jobRoleET.getText().toString());
                            }

                            // Save to database
                            database.getReference().child("Users").child(auth.getUid())
                                    .setValue(user).addOnSuccessListener(unused -> {
                                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                        binding.saveButton.setEnabled(true);
                        binding.saveButton.setText("Save Changes");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImage = data.getData();
            binding.profileImage.setImageURI(selectedImage);
        }
    }
}