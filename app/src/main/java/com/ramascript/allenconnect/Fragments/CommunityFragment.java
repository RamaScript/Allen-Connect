package com.ramascript.allenconnect.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.ramascript.allenconnect.Adapters.CommunityViewPagerAdapter;
import com.ramascript.allenconnect.Adapters.NotificationViewPagerAdapter;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentCommunityBinding;
import com.ramascript.allenconnect.databinding.FragmentJobsBinding;

public class CommunityFragment extends Fragment {

    FragmentCommunityBinding binding;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCommunityBinding.inflate(inflater, container, false);

        binding.viewPager.setAdapter(new CommunityViewPagerAdapter(getChildFragmentManager()));

        binding.tabLayout.setupWithViewPager(binding.viewPager);
        return binding.getRoot();
    }
}