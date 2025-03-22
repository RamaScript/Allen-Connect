package com.ramascript.allenconnect.features.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityDeleteAccountBinding;

import java.util.HashMap;

public class deleteAccountActivity extends AppCompatActivity {

    private ActivityDeleteAccountBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Deleting Account");
        progressDialog.setMessage("Please wait while we process your request...");
        progressDialog.setCancelable(false);

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup back button
        binding.backButton.setOnClickListener(v -> finish());

        // Setup cancel button
        binding.cancelButton.setOnClickListener(v -> finish());

        // Setup delete button
        binding.deleteButton.setOnClickListener(v -> verifyAndDeleteAccount());
    }

    private void verifyAndDeleteAccount() {
        // Hide any previous error messages
        binding.errorMessage.setVisibility(View.GONE);

        // Get password from input field
        String password = binding.passwordInput.getText().toString().trim();

        // Validate password
        if (password.isEmpty()) {
            binding.errorMessage.setText("Please enter your password");
            binding.errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        // Show progress dialog
        progressDialog.show();

        // Get current user
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, loginAs.class));
            finish();
            return;
        }

        // Re-authenticate user with their credentials
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User verified, proceed with soft delete
                        softDeleteAccount(user.getUid());
                    } else {
                        // Authentication failed
                        progressDialog.dismiss();
                        binding.errorMessage.setText("Incorrect password. Please try again.");
                        binding.errorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void softDeleteAccount(String userId) {
        // Create a map of values to update
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("isDeleted", true);
        updates.put("online", false);

        // Set isDeleted flag to true and online to false in the user's database record
        database.getReference()
                .child("Users")
                .child(userId)
                .updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                        finishDeletion();
                    } else {
                        // Failed to set isDeleted flag
                        progressDialog.dismiss();
                        Toast.makeText(deleteAccountActivity.this,
                                "Failed to delete account. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void finishDeletion() {
        // Sign out the user
        auth.signOut();

        // Dismiss progress dialog
        progressDialog.dismiss();

        // Show success message
        Toast.makeText(deleteAccountActivity.this,
                "Your account has been deleted successfully",
                Toast.LENGTH_LONG).show();

        // Redirect to login screen
        Intent intent = new Intent(deleteAccountActivity.this, loginAs.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}