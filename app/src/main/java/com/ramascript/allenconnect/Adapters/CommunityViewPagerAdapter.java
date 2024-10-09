package com.ramascript.allenconnect.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ramascript.allenconnect.Fragments.ActivityNotificationFragment;
import com.ramascript.allenconnect.Fragments.CommunityAlumniFragment;
import com.ramascript.allenconnect.Fragments.CommunityProfessorFragment;
import com.ramascript.allenconnect.Fragments.CommunityStudentFragment;
import com.ramascript.allenconnect.Fragments.EventsNotificationFragment;
import com.ramascript.allenconnect.Fragments.RequestsNotificationFragment;

public class CommunityViewPagerAdapter extends FragmentPagerAdapter {
    public CommunityViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return new CommunityStudentFragment();
            case 1: return new CommunityProfessorFragment();
            case 2: return new CommunityAlumniFragment();
            default: return new CommunityStudentFragment();
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
            title = "Student";
        } else if (position == 1){
            title = "Professor";
        } else if (position == 2){
            title = "Alumni";
        }
        return title;
    }
}
