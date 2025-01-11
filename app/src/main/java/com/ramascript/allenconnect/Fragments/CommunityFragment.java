package com.ramascript.allenconnect.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ramascript.allenconnect.Adapters.CommunityViewPagerAdapter;
import com.ramascript.allenconnect.databinding.FragmentCommunityBinding;

public class CommunityFragment extends Fragment {

    FragmentCommunityBinding binding;

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

        binding.viewPager.setAdapter(new CommunityViewPagerAdapter(getChildFragmentManager()));

        binding.tabLayout.setupWithViewPager(binding.viewPager);

        // Handle back button press
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show the confirmation dialog
                new AlertDialog.Builder(getContext()).setMessage("Do you want to leave the app?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Finish the activity, closing the app
                            requireActivity().finish();
                        }
                    }).setNegativeButton("No", null) // Just dismiss the dialog
                    .show();
            }
        });

        return binding.getRoot();
    }
}