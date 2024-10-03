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

public class Chat extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewPager = findViewById(R.id.viewPagerChat);
//        viewPager.setAdapter(new NotificationViewPagerAdapter(getChildFragmentManager()));
        viewPager.setAdapter(new chatTabViewpagerAdapter(getSupportFragmentManager()));
        tabLayout = findViewById(R.id.tabLayoutChat);
        tabLayout.setupWithViewPager(viewPager);
    }
}