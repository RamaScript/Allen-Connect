package com.ramascript.allenconnect.features.job;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.utils.baseFragment;
import com.ramascript.allenconnect.databinding.FragmentJobsBinding;

import java.util.ArrayList;

public class jobsFragment extends baseFragment {

    private FragmentJobsBinding binding;
    ArrayList<jobModel> list;

    FirebaseAuth auth;
    FirebaseDatabase database;

    public jobsFragment() {
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

        jobAdapter adapter = new jobAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.jobsRV.setLayoutManager(layoutManager); // Using binding
        binding.jobsRV.setAdapter(adapter); // Using binding

        database.getReference().child("Jobs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    jobModel model = dataSnapshot.getValue(jobModel.class);
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
