package com.ramascript.allenconnect.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.Adapters.JobAdapter;
import com.ramascript.allenconnect.Adapters.UserAdapter;
import com.ramascript.allenconnect.Models.JobModel;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentJobsBinding;

import java.util.ArrayList;

public class JobsFragment extends BaseFragment {

    private FragmentJobsBinding binding;
    ArrayList<JobModel> list;

    FirebaseAuth auth;
    FirebaseDatabase database;

    public JobsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Step 2: Inflate the layout using the binding
        binding = FragmentJobsBinding.inflate(inflater, container, false);

        list = new ArrayList<>();

        JobAdapter adapter = new JobAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.jobsRV.setLayoutManager(layoutManager); // Using binding
        binding.jobsRV.setAdapter(adapter); // Using binding

        database.getReference().child("Jobs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    JobModel model = dataSnapshot.getValue(JobModel.class);
                    model.setJobID(dataSnapshot.getKey());
                    list.add(model);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }
}
