package com.ramascript.allenconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ramascript.allenconnect.Adapters.NotificationViewPagerAdapter;
import com.ramascript.allenconnect.Adapters.chatTabViewpagerAdapter;
import com.ramascript.allenconnect.databinding.ActivityChatBinding;

public class Chat extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;

    ActivityChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



//        viewPager = findViewById(R.id.viewPagerChat);
        binding.viewPagerChat.setAdapter(new chatTabViewpagerAdapter(getSupportFragmentManager()));
//        tabLayout = findViewById(R.id.tabLayoutChat);
        binding.tabLayoutChat.setupWithViewPager(binding.viewPagerChat);
    }
}