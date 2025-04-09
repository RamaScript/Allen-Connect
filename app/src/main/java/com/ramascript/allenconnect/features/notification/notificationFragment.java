package com.ramascript.allenconnect.features.notification;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.ramascript.allenconnect.base.mainActivity;
import com.ramascript.allenconnect.databinding.FragmentNotificationBinding;
import com.ramascript.allenconnect.features.home.homeFragment;
import com.ramascript.allenconnect.R;

public class notificationFragment extends Fragment {

    ViewPager viewPager;
    TabLayout tabLayout;
    FragmentNotificationBinding binding;

    public notificationFragment() {
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
        binding = FragmentNotificationBinding.inflate(inflater, container, false);

        binding.backBtnIV.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.menuBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), binding.menuBtn);
            popupMenu.getMenu().add("Notification Settings");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Notification Settings")) {
                    Toast.makeText(getContext(), "Feature is in development phase", Toast.LENGTH_SHORT).show();
                }
                return true;
            });

            popupMenu.show();
        });

        binding.viewPager.setAdapter(new notificationViewPagerAdapter(getChildFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        return binding.getRoot();
    }
}