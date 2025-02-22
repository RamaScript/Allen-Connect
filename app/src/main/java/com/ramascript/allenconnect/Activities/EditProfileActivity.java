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

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

                                // Set common fields
                                binding.nameET.setText(user.getName());
                                binding.profileEmail.setText(user.getEmail());

                                // Hide all specific fields first
                                binding.studentFields.setVisibility(View.GONE);
                                binding.alumniFields.setVisibility(View.GONE);
                                binding.professorFields.setVisibility(View.GONE);

                                // Show fields based on user type
                                switch (user.getUserType()) {
                                    case "Student":
                                        binding.studentFields.setVisibility(View.VISIBLE);
                                        binding.courseET.setText(user.getCourse());
                                        binding.yearET.setText(user.getYear());
                                        break;

                                    case "Alumni":
                                        binding.alumniFields.setVisibility(View.VISIBLE);
                                        binding.companyET.setText(user.getCompany());
                                        binding.jobRoleET.setText(user.getJobRole());
                                        break;

                                    case "Professor":
                                        binding.professorFields.setVisibility(View.VISIBLE);
                                        binding.professorEmailET.setText(user.getEmail());
                                        binding.phoneNoET.setText(user.getPhoneNo());
                                        break;
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
        // Validate common fields
        String name = binding.nameET.getText().toString().trim();
        if (name.isEmpty()) {
            binding.nameET.setError("Name cannot be empty");
            return;
        }

        // Show progress
        binding.saveButton.setEnabled(false);
        binding.saveButton.setText("Saving...");

        // Validate type-specific fields
        UserModel currentUser = getCurrentUser();
        if (currentUser != null) {
            switch (currentUser.getUserType()) {
                case "Student":
                    String course = binding.courseET.getText().toString().trim();
                    String year = binding.yearET.getText().toString().trim();
                    if (course.isEmpty()) {
                        binding.courseET.setError("Course cannot be empty");
                        resetSaveButton();
                        return;
                    }
                    if (year.isEmpty()) {
                        binding.yearET.setError("Year cannot be empty");
                        resetSaveButton();
                        return;
                    }
                    break;

                case "Alumni":
                    String company = binding.companyET.getText().toString().trim();
                    String jobRole = binding.jobRoleET.getText().toString().trim();
                    if (company.isEmpty()) {
                        binding.companyET.setError("Company cannot be empty");
                        resetSaveButton();
                        return;
                    }
                    if (jobRole.isEmpty()) {
                        binding.jobRoleET.setError("Job Role cannot be empty");
                        resetSaveButton();
                        return;
                    }
                    break;

                case "Professor":
                    String email = binding.professorEmailET.getText().toString().trim();
                    String phone = binding.phoneNoET.getText().toString().trim();
                    if (email.isEmpty()) {
                        binding.professorEmailET.setError("Email cannot be empty");
                        resetSaveButton();
                        return;
                    }
                    if (phone.isEmpty()) {
                        binding.phoneNoET.setError("Phone number cannot be empty");
                        resetSaveButton();
                        return;
                    }
                    break;
            }
        }

        // If new image is selected, upload it first
        if (selectedImage != null) {
            StorageReference reference = storage.getReference().child("profile_photos")
                    .child(auth.getUid());

            reference.putFile(selectedImage).addOnSuccessListener(taskSnapshot -> {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateUserData(name, uri.toString());
                }).addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                resetSaveButton();
            });
        } else {
            updateUserData(name, null);
        }
    }

    private void resetSaveButton() {
        binding.saveButton.setEnabled(true);
        binding.saveButton.setText("Save Changes");
    }

    private UserModel getCurrentUser() {
        try {
            DatabaseReference userRef = database.getReference().child("Users").child(auth.getUid());
            DataSnapshot snapshot = Tasks.await(userRef.get());
            return snapshot.getValue(UserModel.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateUserData(String name, String profilePhotoUrl) {
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            // Update common fields
                            user.setName(name);
                            if (profilePhotoUrl != null) {
                                user.setProfilePhoto(profilePhotoUrl);
                            }

                            // Update specific fields based on user type
                            switch (user.getUserType()) {
                                case "Student":
                                    user.setCourse(binding.courseET.getText().toString());
                                    user.setYear(binding.yearET.getText().toString());
                                    break;

                                case "Alumni":
                                    user.setCompany(binding.companyET.getText().toString());
                                    user.setJobRole(binding.jobRoleET.getText().toString());
                                    break;

                                case "Professor":
                                    user.setEmail(binding.professorEmailET.getText().toString());
                                    user.setPhoneNo(binding.phoneNoET.getText().toString());
                                    break;
                            }

                            // Save to database
                            database.getReference().child("Users").child(auth.getUid())
                                    .setValue(user).addOnSuccessListener(unused -> {
                                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(EditProfileActivity.this,
                                                "Failed to update profile: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        resetSaveButton();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                        resetSaveButton();
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