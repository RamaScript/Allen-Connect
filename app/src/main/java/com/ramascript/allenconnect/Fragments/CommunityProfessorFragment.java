package com.ramascript.allenconnect.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.ramascript.allenconnect.Adapters.UserAdapter;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentCommunityAlumniBinding;
import com.ramascript.allenconnect.databinding.FragmentCommunityProfessorBinding;
import com.ramascript.allenconnect.databinding.FragmentCommunityStudentBinding;

import java.util.ArrayList;

public class CommunityProfessorFragment extends Fragment {

    FragmentCommunityProfessorBinding binding;
    ArrayList<UserModel> list;

    FirebaseAuth auth;
    FirebaseDatabase database;


    public CommunityProfessorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommunityProfessorBinding.inflate(inflater, container, false);

        list = new ArrayList<>();

        UserAdapter adapter = new UserAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvProfessor.setLayoutManager(layoutManager);  // Using binding
        binding.rvProfessor.setAdapter(adapter);  // Using binding

        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel model = dataSnapshot.getValue(UserModel.class);
                    model.setID(dataSnapshot.getKey());
                    if (!dataSnapshot.getKey().equals(auth.getUid()) && "Professor".equals(model.getUserType())) {
                        list.add(model);
                    }
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