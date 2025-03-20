package com.ramascript.allenconnect.features.community;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ramascript.allenconnect.utils.baseFragment;
import com.ramascript.allenconnect.databinding.FragmentCommunityBinding;

public class communityFragment extends baseFragment {

    FragmentCommunityBinding binding;
    communityViewPagerAdapter adapter;
    private communityStudentFragment studentFragment;
    private communityAlumniFragment alumniFragment;
    private communityProfessorFragment professorFragment;
    private int currentTabPosition = 0;

    public communityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create fragments early to ensure they're properly initialized
        studentFragment = new communityStudentFragment();
        alumniFragment = new communityAlumniFragment();
        professorFragment = new communityProfessorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCommunityBinding.inflate(inflater, container, false);

        setupViewPager();
        setupTabLayout();
        setupSearch();

        return binding.getRoot();
    }

    private void setupViewPager() {
        // Setup ViewPager with adapter
        adapter = new communityViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(studentFragment, "Students");
        adapter.addFragment(alumniFragment, "Alumni");
        adapter.addFragment(professorFragment, "Professors");

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(3); // Keep all fragments in memory
    }

    private void setupTabLayout() {
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                binding.viewPager.setCurrentItem(currentTabPosition);

                // Apply any existing search filter to the newly selected tab
                if (binding.searchEt.getText().length() > 0) {
                    filterUsers(binding.searchEt.getText().toString(), currentTabPosition);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not used
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not used
            }
        });
    }

    private void setupSearch() {
        // Setup clear button click
        binding.clearSearchBtn.setOnClickListener(v -> {
            binding.searchEt.setText("");
            binding.clearSearchBtn.setVisibility(View.GONE);

            // Clear filters when search is cleared
            filterUsers("", currentTabPosition);
        });

        // Setup search functionality
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button based on text
                binding.clearSearchBtn.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // Get current fragment and filter users
                filterUsers(s.toString(), binding.viewPager.getCurrentItem());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listen for page changes to update search results
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentTabPosition = position;

                // When tab changes, update search results for the new tab
                String query = binding.searchEt.getText().toString();
                filterUsers(query, position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void filterUsers(String query, int tabPosition) {
        switch (tabPosition) {
            case 0: // Students tab
                if (studentFragment != null && studentFragment.isAdded()) {
                    studentFragment.filterUsers(query);
                }
                break;
            case 1: // Alumni tab
                if (alumniFragment != null && alumniFragment.isAdded()) {
                    alumniFragment.filterUsers(query);
                }
                break;
            case 2: // Professors tab
                if (professorFragment != null && professorFragment.isAdded()) {
                    professorFragment.filterUsers(query);
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentTabPosition", currentTabPosition);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            currentTabPosition = savedInstanceState.getInt("currentTabPosition", 0);
            if (binding != null) {
                binding.viewPager.setCurrentItem(currentTabPosition);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}