package com.ramascript.allenconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.Features.AllenBot;
import com.ramascript.allenconnect.Fragments.HomeFragment;
import com.ramascript.allenconnect.Fragments.JobPostFragment;
import com.ramascript.allenconnect.Fragments.JobsFragment;
import com.ramascript.allenconnect.Fragments.CommunityFragment;
import com.ramascript.allenconnect.Fragments.PostFragment;
import com.ramascript.allenconnect.Fragments.ProfileFragment;
import com.ramascript.allenconnect.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Force the app to always use light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.botIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AllenBot.class);
                startActivity(i);
            }
        });

        binding.bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home){
                    loadFragment(new HomeFragment(), false);
                }
                else if (itemId == R.id.navigation_community) {
                    loadFragment(new CommunityFragment(), false);
                }
                else if (itemId == R.id.navigation_post) {

                    database.getReference().child("Users").child(auth.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String userType = snapshot.child("userType").getValue(String.class);

                                if (userType.equals("Student")) {
                                    loadFragment(new PostFragment(), false);
                                } else {
                                    binding.postOptions.setVisibility(View.VISIBLE);

                                    binding.createPost.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            loadFragment(new PostFragment(), false);
                                        }
                                    });

                                    binding.JobPost.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            loadFragment(new JobPostFragment(), false);
                                        }
                                    });

                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
                else if (itemId == R.id.navigation_job) {
                    loadFragment(new JobsFragment(), false);
                }
                else if (itemId == R.id.navigation_profile) {
                    loadFragment(new ProfileFragment(), false);
                }
                return true;
            }
        });
        // Check if intent contains fragment navigation instruction
        if (getIntent().getStringExtra("openFragment") != null) {
            String fragmentToOpen = getIntent().getStringExtra("openFragment");

            if (fragmentToOpen.equals("JobsFragment")) {
                loadFragment(new JobsFragment(), true); // Open JobsFragment
                binding.bottomNavView.setSelectedItemId(R.id.navigation_job); // Highlight correct tab
            } else if (fragmentToOpen.equals("ProfileFragment")) {
                loadFragment(new ProfileFragment(), true); // Open ProfileFragment
                binding.bottomNavView.setSelectedItemId(R.id.navigation_profile); // Highlight correct tab
            }
        }
        else {
            // Default fragment when no intent specifies a fragment
            loadFragment(new HomeFragment(), true);
        }
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized){

        //By default making sure ki postoption wala jo card hai wo invisible rahe
        binding.postOptions.setVisibility(View.GONE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (isAppInitialized){
            transaction.add(R.id.container, fragment);
        }else {
            transaction.replace(R.id.container, fragment);
        }
        transaction.commit();
    }
}