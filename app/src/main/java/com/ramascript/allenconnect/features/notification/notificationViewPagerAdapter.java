package com.ramascript.allenconnect.features.notification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class notificationViewPagerAdapter extends FragmentPagerAdapter {
    public notificationViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return new activityNotificationFragment();
            case 1: return new requestsNotificationFragment();
            case 2: return new eventsNotificationFragment();
            default: return new activityNotificationFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0){
            title = "Activity";
        } else if (position == 1){
            title = "Requests";
        } else if (position == 2){
            title = "Events";
        }
        return title;
    }

}