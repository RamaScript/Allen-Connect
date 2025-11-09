package com.ramascript.allenconnect.base;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.features.about.meetDevsActivity;
import com.ramascript.allenconnect.features.bot.allenBotActivity;
import com.ramascript.allenconnect.features.auth.loginAs;
import com.ramascript.allenconnect.features.home.homeFragment;
import com.ramascript.allenconnect.features.job.jobPostFragment;
import com.ramascript.allenconnect.features.job.jobsFragment;
import com.ramascript.allenconnect.features.community.communityFragment;
import com.ramascript.allenconnect.features.post.postFragment;
import com.ramascript.allenconnect.features.user.profileFragment;
import com.ramascript.allenconnect.databinding.ActivityMainBinding;
import com.ramascript.allenconnect.features.chat.IncomingCallService;
import com.ramascript.allenconnect.base.allenConnectApp;

public class mainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    private int currentFragmentId = R.id.navigation_home; // Track the current fragment
    private boolean initialFragmentLoaded = false;
    private DrawerLayout drawerLayout;
    private boolean isDarkModeEnabled = false;

    // Navigation drawer item views
    private LinearLayout navMyProfileContainer;
    private LinearLayout navSettingsContainer;
    private LinearLayout navHelpContainer;
    private LinearLayout navDevelopersContainer;
    private SwitchCompat themeSwitch;
    private LinearLayout navSignOutContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("mainActivity onCreate called with savedInstanceState: " +
                (savedInstanceState == null ? "null" : "not null"));

        // Start the incoming call service to handle background calls
        startService(new Intent(this, IncomingCallService.class));

        // Check for direct profile loading first - must be done before UI inflation
        Intent intent = getIntent();
        boolean isDirectProfileLoad = intent != null && intent.getBooleanExtra("directProfileLoad", false);

        if (isDirectProfileLoad) {
            Log.d("mainActivity", "Direct profile loading detected");
        }

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

        // Initialize drawer layout
        drawerLayout = binding.drawerLayout;
        setupNavigationDrawer();

        binding.botIV.setOnClickListener(v -> {
            Intent i = new Intent(mainActivity.this, allenBotActivity.class);
            startActivity(i);
        });

        // Setup bottom navigation listener
        setupBottomNavigation();

        // Process intent before loading any default fragments
        if (savedInstanceState == null) {
            if (processIntent(getIntent())) {
                // Intent was processed, don't load any default fragment
                Log.d("mainActivity", "Intent processed successfully, skipping default fragment load");
            } else {
                // No specific fragment requested, load home fragment
                Log.d("mainActivity", "No specific fragment requested, loading home fragment");
                currentFragmentId = R.id.navigation_home;
                loadFragment(new homeFragment(), true);
            }
        } else {
            System.out.println("Skipping initial fragment load: savedInstanceState=" +
                    (savedInstanceState != null) + ", initialFragmentLoaded=" + initialFragmentLoaded);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // First check if drawer is open
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }

                FragmentManager fragmentManager = getSupportFragmentManager();

                // If there are fragments in the back stack, pop the last one
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                    // Update bottom navigation to match the current fragment
                    Fragment currentFragment = fragmentManager.findFragmentById(R.id.container);
                    if (currentFragment instanceof homeFragment) {
                        currentFragmentId = R.id.navigation_home;
                        binding.bottomNavView.setSelectedItemId(R.id.navigation_home);
                    } else if (currentFragment instanceof communityFragment) {
                        currentFragmentId = R.id.navigation_community;
                        binding.bottomNavView.setSelectedItemId(R.id.navigation_community);
                    } else if (currentFragment instanceof profileFragment) {
                        currentFragmentId = R.id.navigation_profile;
                        binding.bottomNavView.setSelectedItemId(R.id.navigation_profile);
                    }
                } else {
                    // If no fragments are in the back stack, show exit dialog
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

    private void setupNavigationDrawer() {
        // Find navigation drawer items
        View navView = findViewById(R.id.nav_drawer_content);

        navMyProfileContainer = navView.findViewById(R.id.nav_my_profile_container);
        navSettingsContainer = navView.findViewById(R.id.nav_settings_container);
        navHelpContainer = navView.findViewById(R.id.nav_help_container);
        navDevelopersContainer = navView.findViewById(R.id.nav_developers_container);
        themeSwitch = navView.findViewById(R.id.theme_switch);
        navSignOutContainer = navView.findViewById(R.id.nav_sign_out_container);

        // Set click listeners for navigation items
        navMyProfileContainer.setOnClickListener(v -> {
            // Navigate to user profile
            currentFragmentId = R.id.navigation_profile;
            loadFragment(new profileFragment(), false);
            binding.bottomNavView.setSelectedItemId(R.id.navigation_profile);
            closeDrawer();
        });

        navSettingsContainer.setOnClickListener(v -> {
            // Handle settings click
            Toast.makeText(this, "Settings Coming Soon", Toast.LENGTH_SHORT).show();
            closeDrawer();
        });

        navHelpContainer.setOnClickListener(v -> {
            // Handle help click
            Toast.makeText(this, "Help & Support Coming Soon", Toast.LENGTH_SHORT).show();
            closeDrawer();
        });

        navDevelopersContainer.setOnClickListener(v -> {
            // Navigate to developers screen
            startActivity(new Intent(mainActivity.this, meetDevsActivity.class));
            closeDrawer();
        });

        // Set theme switch listener
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleDarkMode(isChecked);
        });

        navSignOutContainer.setOnClickListener(v -> {
            // Show sign out confirmation dialog
            new AlertDialog.Builder(this)
                    .setMessage("Do you want to sign out?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Set user online status to false before logging out
                        ((allenConnectApp) getApplication()).updateOnlineStatus(false);
                        // Sign out from Firebase
                        if (auth != null) {
                            auth.signOut();
                            // Redirect to login or appropriate screen
                            startActivity(new Intent(mainActivity.this, loginAs.class));
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            closeDrawer();
        });
    }

    private void toggleDarkMode(boolean isDarkMode) {
        isDarkModeEnabled = isDarkMode;
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(this, "Dark Mode Enabled", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Light Mode Enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    // This method will be called from the homeFragment
    public void toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void setupBottomNavigation() {
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
    }

    /**
     * Process intent to determine which fragment to load.
     * 
     * @param intent The intent to process
     * @return true if a specific fragment was requested, false otherwise
     */
    private boolean processIntent(Intent intent) {
        // Check if intent contains fragment navigation instruction
        if (intent != null && intent.getStringExtra("openFragment") != null) {
            String fragmentToOpen = intent.getStringExtra("openFragment");
            Log.d("mainActivity", "Processing intent with openFragment: " + fragmentToOpen);

            // Direct profile loading takes highest priority
            boolean isDirectProfileLoad = intent.getBooleanExtra("directProfileLoad", false);
            if (isDirectProfileLoad && fragmentToOpen.equals("profileFragment")) {
                // Load profile fragment directly without animations
                currentFragmentId = R.id.navigation_profile;
                String userId = intent.getStringExtra("userId");

                if (userId != null && !userId.isEmpty()) {
                    Fragment fragment = profileFragment.newInstance(userId);
                    loadProfileDirectly(fragment);
                } else {
                    loadProfileDirectly(new profileFragment());
                }

                // Update bottom navigation
                binding.bottomNavView.setSelectedItemId(R.id.navigation_profile);
                return true;
            }

            // Normal navigation flow
            if (fragmentToOpen.equals("jobsFragment")) {
                currentFragmentId = R.id.navigation_job;
                loadFragment(new jobsFragment(), true);
                binding.bottomNavView.setSelectedItemId(R.id.navigation_job);
                return true;
            } else if (fragmentToOpen.equals("profileFragment")) {
                currentFragmentId = R.id.navigation_profile;

                // Directly get userId from intent extras if available
                String userId = intent.getStringExtra("userId");
                Fragment fragment;

                // Before creating the fragment, check for null/empty userId
                if (userId != null && !userId.isEmpty()) {
                    // Create profile fragment with userId
                    Log.d("mainActivity", "Loading profile for user ID: " + userId);
                    fragment = profileFragment.newInstance(userId);
                } else {
                    // Default profile fragment (current user)
                    Log.d("mainActivity", "Loading current user profile");
                    fragment = new profileFragment();
                }

                loadFragment(fragment, true);
                binding.bottomNavView.setSelectedItemId(R.id.navigation_profile);
                return true;
            }
        }

        // No specific fragment requested
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("mainActivity", "onNewIntent called with action: " + intent.getAction());

        // Set the new intent
        setIntent(intent);

        // Check for direct profile loading
        boolean isDirectProfileLoad = intent.getBooleanExtra("directProfileLoad", false);
        if (isDirectProfileLoad && intent.getStringExtra("openFragment") != null) {
            Log.d("mainActivity", "Direct profile loading in onNewIntent");
            String userId = intent.getStringExtra("userId");

            if (userId != null && !userId.isEmpty()) {
                Fragment fragment = profileFragment.newInstance(userId);
                loadProfileDirectly(fragment);
            } else {
                loadProfileDirectly(new profileFragment());
            }

            // Update bottom navigation
            binding.bottomNavView.setSelectedItemId(R.id.navigation_profile);
        } else {
            // Process the new intent without loading default fragments
            processIntent(intent);
        }
    }

    private void handlePostNavigation() {
        // Get user type from SharedPreferences
        String userType = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userType", "Student"); // Default to Student if not found

        if ("Student".equals(userType)) {
            loadFragment(new postFragment(), false);
        } else {
            // Create and show custom dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_post_options, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            // Set click listeners
            dialogView.findViewById(R.id.createPostOption).setOnClickListener(v -> {
                loadFragment(new postFragment(), false);
                dialog.dismiss();
            });

            dialogView.findViewById(R.id.postJobOption).setOnClickListener(v -> {
                loadFragment(new jobPostFragment(), false);
                dialog.dismiss();
            });

            dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> {
                dialog.dismiss();
                // Restore the previous bottom navigation selection
                binding.bottomNavView.setSelectedItemId(currentFragmentId);
            });

            dialog.setOnCancelListener(dialogInterface -> {
                // Restore the previous bottom navigation selection
                binding.bottomNavView.setSelectedItemId(currentFragmentId);
            });

            dialog.show();
        }
    }

    public void loadFragment(Fragment fragment, boolean isAppInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace the current fragment
        transaction.replace(R.id.container, fragment);

        // Add to back stack if it's not the initial fragment
        if (!isAppInitialized) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    public void navigateToFragment(Fragment fragment, int navigationId) {
        currentFragmentId = navigationId;
        loadFragment(fragment, false);
        binding.bottomNavView.setSelectedItemId(navigationId);
    }

    public void hideBottomNavigation() {
        if (binding.bottomNavView.isShown()) {
            binding.bottomNavView.animate()
                    .translationY(binding.bottomNavView.getHeight() + 240)
                    .setDuration(120)
                    .start();
        }
    }

    public void showBottomNavigation() {
        if (binding.bottomNavView.getTranslationY() > 0) {
            binding.bottomNavView.animate()
                    .translationY(0)
                    .setDuration(120)
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

    /**
     * Load profile fragment directly without animations
     */
    private void loadProfileDirectly(Fragment fragment) {
        if (fragment == null)
            return;

        // Hide post options
        // Use a transaction without animation
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE); // No animation
        transaction.replace(R.id.container, fragment);
        transaction.commitNow(); // Use commitNow for immediate execution

        Log.d("mainActivity", "Profile loaded directly with suppressAnimation");
    }

    // Online status updates are now handled by allenConnectApp
}