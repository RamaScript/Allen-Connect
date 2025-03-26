package com.ramascript.allenconnect.features.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ramascript.allenconnect.R;

public class UserListFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String ARG_INITIAL_TAB = "initialTab"; // 0 for followers, 1 for following

    private String userId;
    private int initialTab;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView titleTextView;
    private ImageView backButton;

    public UserListFragment() {
        // Required empty public constructor
    }

    public static UserListFragment newInstance(String userId, String listType) {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putInt(ARG_INITIAL_TAB, "following".equals(listType) ? 1 : 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID, "");
            initialTab = getArguments().getInt(ARG_INITIAL_TAB, 0);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list_tabbed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        titleTextView = view.findViewById(R.id.titleTextView);
        backButton = view.findViewById(R.id.backButton);

        // Set title
        titleTextView.setText("Connections");

        // Set back button listener
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Setup ViewPager
        setupViewPager();
    }

    private void setupViewPager() {
        // Create adapter
        UserListPagerAdapter pagerAdapter = new UserListPagerAdapter(requireActivity());
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Followers");
                            break;
                        case 1:
                            tab.setText("Following");
                            break;
                    }
                }).attach();

        // Set initial tab
        viewPager.setCurrentItem(initialTab, false);
    }

    private class UserListPagerAdapter extends FragmentStateAdapter {

        public UserListPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return UserListTabFragment.newInstance(userId, "followers");
                case 1:
                    return UserListTabFragment.newInstance(userId, "following");
                default:
                    return UserListTabFragment.newInstance(userId, "followers");
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Followers and Following tabs
        }
    }
}