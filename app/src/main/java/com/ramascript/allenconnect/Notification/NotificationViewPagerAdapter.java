package com.ramascript.allenconnect.Notification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class NotificationViewPagerAdapter extends FragmentPagerAdapter {
    public NotificationViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return new ActivityNotificationFragment();
            case 1: return new RequestsNotificationFragment();
            case 2: return new EventsNotificationFragment();
            default: return new ActivityNotificationFragment();
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