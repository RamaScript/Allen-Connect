package com.ramascript.allenconnect.utils;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ramascript.allenconnect.base.mainActivity;
import com.ramascript.allenconnect.features.home.homeFragment;
import com.ramascript.allenconnect.R;

public class baseFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the back button if it exists
        ImageView backButton = view.findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Navigate to homeFragment
                if (getActivity() instanceof mainActivity) {
                    mainActivity mainActivity = (com.ramascript.allenconnect.base.mainActivity) getActivity();
                    mainActivity.navigateToFragment(new homeFragment(), R.id.navigation_home);
                }
            });
        }
    }
}