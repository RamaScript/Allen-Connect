package com.ramascript.allenconnect.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramascript.allenconnect.Adapters.FriendAdapter;
import com.ramascript.allenconnect.Adapters.JobAdapter;
import com.ramascript.allenconnect.Models.FriendModel;
import com.ramascript.allenconnect.Models.JobModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentJobsBinding;

import java.util.ArrayList;

public class JobsFragment extends Fragment {


    private FragmentJobsBinding binding;
    ArrayList<JobModel> list;

    public JobsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Step 2: Inflate the layout using the binding
        binding = FragmentJobsBinding.inflate(inflater, container, false);

        list = new ArrayList<>();
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("Allenhouse","WP developer",R.drawable.allen_logo));
        list.add(new JobModel("infosys","Software Developer",R.drawable.p1));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));
        list.add(new JobModel("TCS","Software Developer",R.drawable.p6));

        JobAdapter adapter = new JobAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.jobsRV.setLayoutManager(layoutManager);  // Using binding
        binding.jobsRV.setAdapter(adapter);  // Using binding

        return binding.getRoot();
    }
}
