package com.ramascript.allenconnect.features.onBoarding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ramascript.allenconnect.base.mainActivity;
import com.ramascript.allenconnect.R;

public class splash extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Set Edge-to-Edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Disable Dark Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = auth.getCurrentUser();

            // Check if user is logged in
            Intent intent;
            if (currentUser != null) {
                // User is logged in, navigate to mainActivity
                intent = new Intent(splash.this, mainActivity.class);
            } else {
                // User not logged in, navigate to loginAs activity
//                intent = new Intent(splash.this, loginAs.class);
                intent = new Intent(splash.this, onboardingActivity.class);
            }
            startActivity(intent);

            // Finish splash activity
            finish();
        }, 2000);
    }
}
