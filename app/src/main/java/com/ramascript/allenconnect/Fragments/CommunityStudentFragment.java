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
import com.ramascript.allenconnect.databinding.FragmentCommunityStudentBinding;

import java.util.ArrayList;

public class CommunityStudentFragment extends Fragment {

    FragmentCommunityStudentBinding binding;
    ArrayList<UserModel> list;
    ArrayList<UserModel> filteredList;
    UserAdapter adapter;

    FirebaseAuth auth;
    FirebaseDatabase database;

    public CommunityStudentFragment() {
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
        // Inflate the layout for this fragment
        binding = FragmentCommunityStudentBinding.inflate(inflater, container, false);

        list = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new UserAdapter(getContext(), filteredList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvStudent.setLayoutManager(layoutManager); // Using binding
        binding.rvStudent.setAdapter(adapter); // Using binding

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
                    UserModel model = dataSnapshot.getValue(UserModel.class);
                    if (model != null) {
                        model.setID(dataSnapshot.getKey());
                        if (!dataSnapshot.getKey().equals(auth.getUid()) && "Student".equals(model.getUserType())) {
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
            for (UserModel user : list) {
                if (user.getName().toLowerCase().contains(query)) {
                    filteredList.add(user);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}