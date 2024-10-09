package com.ramascript.allenconnect.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramascript.allenconnect.Adapters.CommStudentAdapter;
import com.ramascript.allenconnect.Adapters.JobAdapter;
import com.ramascript.allenconnect.Models.CommStudentModel;
import com.ramascript.allenconnect.Models.JobModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentCommunityStudentBinding;
import com.ramascript.allenconnect.databinding.FragmentJobsBinding;

import java.util.ArrayList;

public class CommunityStudentFragment extends Fragment {

    FragmentCommunityStudentBinding binding;
    ArrayList<CommStudentModel> list;

    public CommunityStudentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCommunityStudentBinding.inflate(inflater, container, false);

        list = new ArrayList<>();
        list.add(new CommStudentModel("Ramanand Kumar","BCA","3rd Year",R.drawable.p6));
        list.add(new CommStudentModel("Rajat Kumar","BCA","2nd Year",R.drawable.p1));
        list.add(new CommStudentModel("Mohan Kumar","BBA","1st Year",R.drawable.p2));
        list.add(new CommStudentModel("Ramanand Kumar","BCA","3rd Year",R.drawable.p3));
        list.add(new CommStudentModel("Yashraj Kumar","MBA","2nd Year",R.drawable.p6));
        list.add(new CommStudentModel("Ramanand Kumar","BCA","3rd Year",R.drawable.p5));

        CommStudentAdapter adapter = new CommStudentAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvStudent.setLayoutManager(layoutManager);  // Using binding
        binding.rvStudent.setAdapter(adapter);  // Using binding

        return binding.getRoot();
    }
}