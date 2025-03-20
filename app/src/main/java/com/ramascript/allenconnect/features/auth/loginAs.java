package com.ramascript.allenconnect.features.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ramascript.allenconnect.base.mainActivity;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityLoginAsBinding;

public class loginAs extends AppCompatActivity {

    private ActivityLoginAsBinding binding; // View Binding
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        // Initialize View Binding
        binding = ActivityLoginAsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Edge-to-edge configuration
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Set click listeners using View Binding
        setupLoginClickListener(binding.studentLoginLL, "Student");
        setupLoginClickListener(binding.alumniLoginLL, "Alumni");
        setupLoginClickListener(binding.professorLoginLL, "Professor");

        // Handle "Don't have an account" button
        binding.dontHaveAcTV.setOnClickListener(v -> {
            Intent iReg = new Intent(loginAs.this, registerAs.class);
            startActivity(iReg);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent i = new Intent(loginAs.this, mainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void setupLoginClickListener(android.view.View layout, String userType) {
        layout.setOnClickListener(v -> {
            Intent intent = new Intent(loginAs.this, login.class);
            intent.putExtra("userType", userType);
            startActivity(intent);
            finish();
        });
    }
}
