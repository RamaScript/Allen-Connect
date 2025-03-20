package com.ramascript.allenconnect.features.auth;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.base.mainActivity;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityLoginBinding;

public class login extends AppCompatActivity {

    FirebaseAuth auth;

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(v -> {
            Intent i = new Intent(login.this, loginAs.class);
            startActivity(i);
            finish();
        });

        String userType = getIntent().getStringExtra("userType");
        binding.loginTitleTV.setText(String.format("%s login", userType));

        auth = FirebaseAuth.getInstance();

        binding.loginBtn.setOnClickListener(v -> {
            String email = binding.emailET.getText().toString().trim();
            String password = binding.passwordET.getText().toString().trim();

            // Check if email or password fields are empty
            if (email.isEmpty() || password.isEmpty()) {
                binding.errorBoxTV.setVisibility(View.VISIBLE);
                binding.errorBoxTV.setText("Please fill in all fields.");
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.errorBoxTV.setVisibility(View.VISIBLE);
                binding.errorBoxTV.setText("Please enter a valid email.");
            } else {
                binding.errorBoxTV.setVisibility(View.GONE);  // Hide error message
                binding.loginBtn.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.VISIBLE);  // Show ProgressBar

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);  // Hide ProgressBar after login attempt
                    binding.loginBtn.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Access the user's data in the database to check usertype
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                            userRef.child("userType").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String dbUserType = snapshot.getValue(String.class);
                                    assert userType != null;
                                    if (userType.equals(dbUserType)) {
                                        Toast.makeText(login.this, "login Successful", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(login.this, mainActivity.class);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        auth.signOut();
                                        binding.errorBoxTV.setVisibility(View.VISIBLE);
                                        binding.errorBoxTV.setText(String.format("Access denied. Only %s can log in here.", userType));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    binding.errorBoxTV.setVisibility(View.VISIBLE);
                                    binding.errorBoxTV.setText("Database error. Please try again later.");
                                }
                            });
                        }
                    } else {
                        // Handle different types of errors from Firebase
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            binding.errorBoxTV.setVisibility(View.VISIBLE);
                            binding.errorBoxTV.setText("Email or password is incorrect. Please try again.");
                        } else {
                            binding.errorBoxTV.setVisibility(View.VISIBLE);
                            binding.errorBoxTV.setText("login failed. Please try again.");
                        }
                    }
                });
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(login.this, loginAs.class);
        startActivity(intent);
        finish(); // Close the current activity
    }

}