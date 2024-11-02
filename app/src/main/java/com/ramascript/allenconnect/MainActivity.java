package com.ramascript.allenconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ramascript.allenconnect.Fragments.HomeFragment;
import com.ramascript.allenconnect.Fragments.JobsFragment;
import com.ramascript.allenconnect.Fragments.CommunityFragment;
import com.ramascript.allenconnect.Fragments.PostFragment;
import com.ramascript.allenconnect.Fragments.ProfileFragment;
import com.ramascript.allenconnect.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
                    loadFragment(new PostFragment(), false);
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
        loadFragment(new HomeFragment(),true);
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (isAppInitialized){
            transaction.add(R.id.container, fragment);
        }else {
            transaction.replace(R.id.container, fragment);
        }
        transaction.commit();
    }
}