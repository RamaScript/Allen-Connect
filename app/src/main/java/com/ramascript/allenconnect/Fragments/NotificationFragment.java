package com.ramascript.allenconnect.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.google.android.material.tabs.TabLayout;
import com.ramascript.allenconnect.Adapters.NotificationViewPagerAdapter;
import com.ramascript.allenconnect.MainActivity;
import com.ramascript.allenconnect.R;

public class NotificationFragment extends Fragment {

    ViewPager viewPager;
    TabLayout tabLayout;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment\
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        ImageView backBtnIV = view.findViewById(R.id.backBtnIV);
        backBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), MainActivity.class);
                startActivity(i);
            }
        });

        ImageView notificationMenuIV = view.findViewById(R.id.notificationMenuIV);

        notificationMenuIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(getContext(), v);

                // Inflate the menu resource
                popupMenu.getMenuInflater().inflate(R.menu.notification_menu, popupMenu.getMenu());

                // Set a click listener for menu item clicks
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.action_settings){
                            // Handle settings click
                            // Add your logic for opening settings or handling the event
                            return true;
                        } else if (item.getItemId() == R.id.action_logout) {
//                            handle
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                // Show the popup menu
                popupMenu.show();
            }
        });

        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new NotificationViewPagerAdapter(getChildFragmentManager()));

        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);


        return view;
    }
}