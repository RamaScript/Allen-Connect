package com.ramascript.allenconnect.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.ramascript.allenconnect.Adapters.CommunityViewPagerAdapter;
import com.ramascript.allenconnect.databinding.FragmentCommunityBinding;

public class CommunityFragment extends BaseFragment {

    FragmentCommunityBinding binding;
    CommunityViewPagerAdapter adapter;
    private CommunityStudentFragment studentFragment;
    private CommunityAlumniFragment alumniFragment;
    private CommunityProfessorFragment professorFragment;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCommunityBinding.inflate(inflater, container, false);

        // Initialize fragments
        studentFragment = new CommunityStudentFragment();
        alumniFragment = new CommunityAlumniFragment();
        professorFragment = new CommunityProfessorFragment();

        // Setup ViewPager with adapter
        adapter = new CommunityViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(studentFragment, "Students");
        adapter.addFragment(alumniFragment, "Alumni");
        adapter.addFragment(professorFragment, "Professors");

        binding.viewPager.setAdapter(adapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        // Setup search functionality
        setupSearch();

        return binding.getRoot();
    }

    private void setupSearch() {
        // Setup clear button click
        binding.clearSearchBtn.setOnClickListener(v -> {
            binding.searchEt.setText("");
            binding.clearSearchBtn.setVisibility(View.GONE);
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
                int currentPosition = binding.viewPager.getCurrentItem();
                filterUsers(s.toString(), currentPosition);
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
                // When tab changes, update search results for the new tab
                String query = binding.searchEt.getText().toString();
                if (!query.isEmpty()) {
                    filterUsers(query, position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void filterUsers(String query, int tabPosition) {
        switch (tabPosition) {
            case 0: // Students tab
                if (studentFragment != null) {
                    studentFragment.filterUsers(query);
                }
                break;
            case 1: // Alumni tab
                if (alumniFragment != null) {
                    alumniFragment.filterUsers(query);
                }
                break;
            case 2: // Professors tab
                if (professorFragment != null) {
                    professorFragment.filterUsers(query);
                }
                break;
        }
    }
}