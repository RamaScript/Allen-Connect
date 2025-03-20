package com.ramascript.allenconnect.features.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class chatTabViewPagerAdapter extends FragmentPagerAdapter {

    public chatTabViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return new chatsFragment();
            case 1: return new groupChatFragment();
            default: return new chatsFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position==0){
            title = "Chats";
        }if (position==1){
            title = "Groups";
        }
        return title;
    }
}
