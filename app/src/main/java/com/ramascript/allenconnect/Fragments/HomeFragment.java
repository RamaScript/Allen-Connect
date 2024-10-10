package com.ramascript.allenconnect.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.Adapters.PostAdapter;
import com.ramascript.allenconnect.Adapters.StoryAdapter;
import com.ramascript.allenconnect.Chat;
import com.ramascript.allenconnect.Models.PostModel;
import com.ramascript.allenconnect.Models.StoryModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentHomeBinding;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    // Step 1: Declare the binding variable
    private FragmentHomeBinding binding;

    ArrayList<StoryModel> list;
    ArrayList<PostModel> postList;

    FirebaseDatabase database;
    FirebaseAuth auth;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.notificationHomeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an instance of the NotificationFragment
                Fragment notificationFragment = new NotificationFragment();

                // Get the FragmentTransaction object and replace the HomeFragment with NotificationFragment
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

                // Replace the fragment container with the new fragment and add it to the backstack
                transaction.replace(R.id.container, notificationFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        binding.chatHomeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), Chat.class);
                startActivity(i);
            }
        });

        list = new ArrayList<>();
        list.add(new StoryModel(R.drawable.p8, R.drawable.ic_live, R.drawable.p7, "Ramanand"));
        list.add(new StoryModel(R.drawable.p7, R.drawable.ic_live, R.drawable.p3, "Rajat"));
        list.add(new StoryModel(R.drawable.p6, R.drawable.ic_live, R.drawable.p3, "suraj"));
        list.add(new StoryModel(R.drawable.p7, R.drawable.ic_live, R.drawable.p8, "Sohan"));
        list.add(new StoryModel(R.drawable.p8, R.drawable.ic_live, R.drawable.p1, "hum"));
        list.add(new StoryModel(R.drawable.p8, R.drawable.ic_live, R.drawable.p1, "hum"));
        list.add(new StoryModel(R.drawable.p8, R.drawable.ic_live, R.drawable.p1, "hum"));

        StoryAdapter adapter = new StoryAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.storyRV.setLayoutManager(layoutManager);
        binding.storyRV.setNestedScrollingEnabled(false);
        binding.storyRV.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        //dashboard recycler view
        postList = new ArrayList<>();

        PostAdapter postAdapter = new PostAdapter(postList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.dashBoardRV.setLayoutManager(linearLayoutManager);
        binding.dashBoardRV.addItemDecoration(new DividerItemDecoration(binding.dashBoardRV.getContext(), DividerItemDecoration.VERTICAL));
        binding.dashBoardRV.setNestedScrollingEnabled(false);
        binding.dashBoardRV.setAdapter(postAdapter);

        database.getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    PostModel postModel = dataSnapshot.getValue(PostModel.class);
                    postList.add(postModel);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

}
