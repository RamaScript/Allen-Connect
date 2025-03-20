package com.ramascript.allenconnect.features.community;

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
import com.ramascript.allenconnect.features.user.userAdapter;
import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.databinding.FragmentCommunityAlumniBinding;

import java.util.ArrayList;

public class communityAlumniFragment extends Fragment {

    FragmentCommunityAlumniBinding binding;
    ArrayList<userModel> list;
    ArrayList<userModel> filteredList;
    userAdapter adapter;

    FirebaseAuth auth;
    FirebaseDatabase database;

    public communityAlumniFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityAlumniBinding.inflate(inflater, container, false);

        list = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new userAdapter(getContext(), filteredList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvAlumni.setLayoutManager(layoutManager);
        binding.rvAlumni.setAdapter(adapter);

        loadUsers();

        return binding.getRoot();
    }

    private void loadUsers() {
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                filteredList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    userModel model = dataSnapshot.getValue(userModel.class);
                    if (model != null) {
                        model.setID(dataSnapshot.getKey());
                        if (!dataSnapshot.getKey().equals(auth.getUid()) && "Alumni".equals(model.getUserType())) {
                            list.add(model);
                            filteredList.add(model);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void filterUsers(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(list);
        } else {
            query = query.toLowerCase();
            for (userModel user : list) {
                if (user.getName().toLowerCase().contains(query)) {
                    filteredList.add(user);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}