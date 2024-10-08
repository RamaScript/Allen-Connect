package com.ramascript.allenconnect.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ramascript.allenconnect.Fragments.ChatsFragment;
import com.ramascript.allenconnect.Fragments.GroupChatFragment;

public class chatTabViewpagerAdapter extends FragmentPagerAdapter {

    public chatTabViewpagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return new ChatsFragment();
            case 1: return new GroupChatFragment();
            default: return new ChatsFragment();
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
