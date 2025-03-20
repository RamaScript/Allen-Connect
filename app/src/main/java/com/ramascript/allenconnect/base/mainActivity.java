package com.ramascript.allenconnect.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.features.allenBot.allenBotActivity;
import com.ramascript.allenconnect.features.home.homeFragment;
import com.ramascript.allenconnect.features.job.jobPostFragment;
import com.ramascript.allenconnect.features.job.jobsFragment;
import com.ramascript.allenconnect.features.community.communityFragment;
import com.ramascript.allenconnect.features.post.postFragment;
import com.ramascript.allenconnect.features.user.profileFragment;
import com.ramascript.allenconnect.databinding.ActivityMainBinding;

public class mainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    private int currentFragmentId = R.id.navigation_home; // Track the current fragment
    private boolean initialFragmentLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("mainActivity onCreate called with savedInstanceState: " +
                (savedInstanceState == null ? "null" : "not null"));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Clear any existing back stack when app starts
        clearBackStack();

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Force the app to always use light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.botIV.setOnClickListener(v -> {
            Intent i = new Intent(mainActivity.this, allenBotActivity.class);
            startActivity(i);
        });

        binding.bottomNavView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            System.out.println("Bottom navigation selected: " + itemId +
                    ", current fragment ID: " + currentFragmentId);

            // Only process if this is a different fragment than the current one
            if (itemId != currentFragmentId) {
                currentFragmentId = itemId; // Update current fragment ID

                if (itemId == R.id.navigation_home) {
                    loadFragment(new homeFragment(), false);
                } else if (itemId == R.id.navigation_community) {
                    loadFragment(new communityFragment(), false);
                } else if (itemId == R.id.navigation_post) {
                    handlePostNavigation();
                } else if (itemId == R.id.navigation_job) {
                    loadFragment(new jobsFragment(), false);
                } else if (itemId == R.id.navigation_profile) {
                    loadFragment(new profileFragment(), false);
                }
            } else {
                System.out.println("Skipping navigation - already on this fragment");
            }
            return true;
        });

        // Only load the initial fragment if this is a fresh start
        if (savedInstanceState == null) {
            // Check if intent contains fragment navigation instruction
            if (getIntent().getStringExtra("openFragment") != null) {
                String fragmentToOpen = getIntent().getStringExtra("openFragment");

                if (fragmentToOpen.equals("jobsFragment")) {
                    currentFragmentId = R.id.navigation_job;
                    loadFragment(new jobsFragment(), true);
                    binding.bottomNavView.setSelectedItemId(R.id.navigation_job);
                } else if (fragmentToOpen.equals("profileFragment")) {
                    currentFragmentId = R.id.navigation_profile;
                    loadFragment(new profileFragment(), true);
                    binding.bottomNavView.setSelectedItemId(R.id.navigation_profile);
                }
            } else {
                // Default fragment when no intent specifies a fragment
                currentFragmentId = R.id.navigation_home;
                loadFragment(new homeFragment(), true);
            }
        } else {
            System.out.println("Skipping initial fragment load: savedInstanceState=" +
                    (savedInstanceState != null) + ", initialFragmentLoaded=" + initialFragmentLoaded);
        }

        // Add this at the end of onCreate to handle back button globally
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If we're not on the home fragment, navigate to home
                if (currentFragmentId != R.id.navigation_home) {
                    System.out.println("Back pressed: Navigating to homeFragment");
                    navigateToFragment(new homeFragment(), R.id.navigation_home);
                } else {
                    // If we're already on home, show exit dialog
                    System.out.println("Back pressed: Already on homeFragment, showing exit dialog");
                    new AlertDialog.Builder(mainActivity.this)
                            .setMessage("Do you want to leave the app?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", (dialog, id) -> finish())
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("mainActivity onNewIntent called");
        // Set the new intent
        setIntent(intent);
        // But don't process it again if we're already initialized
    }

    private void handlePostNavigation() {
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String userType = snapshot.child("userType").getValue(String.class);

                            if ("Student".equals(userType)) {
                                loadFragment(new postFragment(), false);
                            } else {
                                binding.postOptions.setVisibility(View.VISIBLE);

                                binding.createPost.setOnClickListener(v -> {
                                    loadFragment(new postFragment(), false);
                                });

                                binding.JobPost.setOnClickListener(v -> {
                                    loadFragment(new jobPostFragment(), false);
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void loadFragment(Fragment fragment, boolean isAppInitialized) {
        System.out.println("loadFragment called with: " + fragment.getClass().getSimpleName() +
                ", isAppInitialized=" + isAppInitialized);

        // By default making sure ki postoption wala jo card hai wo invisible rahe
        binding.postOptions.setVisibility(View.GONE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Always use replace, never add to back stack
        transaction.replace(R.id.container, fragment);

        // Never add to back stack - this is crucial
        // transaction.addToBackStack(null); - REMOVE THIS if it exists

        transaction.commit();
    }

    // Also update this method to handle fragment navigation from other places
    public void navigateToFragment(Fragment fragment, int navigationId) {
        if (navigationId != currentFragmentId) {
            currentFragmentId = navigationId;
            loadFragment(fragment, false);
            updateBottomNavSelection(navigationId);
        }
    }

    public void updateBottomNavSelection(int itemId) {
        binding.bottomNavView.setSelectedItemId(itemId);
    }

    public void hideBottomNavigation() {
        if (binding.bottomNavView.isShown()) {
            binding.bottomNavView.animate()
                    .translationY(binding.bottomNavView.getHeight() + 16)
                    .setDuration(200)
                    .start();
        }
    }

    public void showBottomNavigation() {
        if (binding.bottomNavView.getTranslationY() > 0) {
            binding.bottomNavView.animate()
                    .translationY(0)
                    .setDuration(200)
                    .start();
        }
    }

    public int getCurrentFragmentId() {
        return currentFragmentId;
    }

    // Add this method to clear the back stack
    private void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
    }

    // Online status updates are now handled by allenConnectApp
}