package com.ramascript.allenconnect.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.Adapters.ChatUsersAdapter;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.databinding.FragmentChatsBinding;

import java.util.ArrayList;

public class ChatsFragment extends Fragment implements ChatUsersAdapter.FilterCallback {

    private FragmentChatsBinding binding;
    private ArrayList<UserModel> usersList;
    private FirebaseDatabase database;
    private ChatUsersAdapter adapter;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        usersList = new ArrayList<>();

        adapter = new ChatUsersAdapter(usersList, getContext(), this);
        binding.chatRecyclerView.setAdapter(adapter);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId != null) {
            database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    usersList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        String userId = dataSnapshot.getKey();

                        if (user != null && userId != null && !userId.equals(currentUserId)) {
                            user.setID(userId);
                            usersList.add(user);
                        }
                    }

                    adapter.updateList(usersList);
                    updateEmptyState();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ChatsFragment", "Database error: " + error.getMessage());
                    updateEmptyState();
                }
            });
        } else {
            updateEmptyState();
        }

        return binding.getRoot();
    }

    private void updateEmptyState() {
        if (binding != null) {
            binding.noResultsView.setVisibility(
                    usersList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onFilterComplete(int size) {
        if (binding != null) {
            binding.noResultsView.setVisibility(size == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}