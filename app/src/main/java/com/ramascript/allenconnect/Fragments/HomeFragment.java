package com.ramascript.allenconnect.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ramascript.allenconnect.Adapters.DashboardAdapter;
import com.ramascript.allenconnect.Adapters.StoryAdapter;
import com.ramascript.allenconnect.Chat;
import com.ramascript.allenconnect.Models.DashBoardModel;
import com.ramascript.allenconnect.Models.StoryModel;
import com.ramascript.allenconnect.R;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView storyRv, dashboardRv;
    ArrayList<StoryModel> list;
    ArrayList<DashBoardModel> dashBoardList;


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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ImageView notificationHomeIV = view.findViewById(R.id.notificationHomeIV);
        notificationHomeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    // he chatgpt mujhe yaha ka code do taki mai apne notification fragmnet me ja saku
                // Create an instance of the NotificationFragment
                Fragment notificationFragment = new NotificationFragment();

                // Get the FragmentTransaction object and replace the HomeFragment with NotificationFragment
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

                // Replace the fragment container with the new fragment and add it to the backstack
                transaction.replace(R.id.container, notificationFragment); // Replace 'fragment_container' with your actual container ID
                transaction.addToBackStack(null); // Adds this transaction to the backstack so the user can navigate back
                transaction.commit(); // Commit the transaction
            }
        });

        ImageView chatHomeIV = view.findViewById(R.id.chatHomeIV);

        chatHomeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), Chat.class);
                startActivity(i);
            }
        });

        storyRv = view.findViewById(R.id.storyRV);
        list = new ArrayList<>();
        list.add(new StoryModel(R.drawable.p8, R.drawable.ic_live, R.drawable.p7, "Ramanand"));
        list.add(new StoryModel(R.drawable.p7, R.drawable.ic_live, R.drawable.p3, "Rajat"));
        list.add(new StoryModel(R.drawable.p6, R.drawable.ic_live, R.drawable.p3, "suraj"));
        list.add(new StoryModel(R.drawable.p7, R.drawable.ic_live, R.drawable.p8, "Sohan"));
        list.add(new StoryModel(R.drawable.p8, R.drawable.ic_live, R.drawable.p1, "hum"));
        list.add(new StoryModel(R.drawable.p8, R.drawable.ic_live, R.drawable.p1, "hum"));
        list.add(new StoryModel(R.drawable.p8, R.drawable.ic_live, R.drawable.p1, "hum"));

        StoryAdapter adapter = new StoryAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        storyRv.setLayoutManager(layoutManager);
        storyRv.setNestedScrollingEnabled(false);
        storyRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        dashboardRv = view.findViewById(R.id.dashBoardRV);
        dashBoardList = new ArrayList<>();

        dashBoardList.add(new DashBoardModel(R.drawable.p7, R.drawable.p5, R.drawable.ic_bookmark,
                "Ramanand", "Student CS", "370", "310", "518"));
        dashBoardList.add(new DashBoardModel(R.drawable.p8, R.drawable.p1, R.drawable.ic_bookmark,
                "Abhinav", "Developer", "370", "310", "518"));
        dashBoardList.add(new DashBoardModel(R.drawable.p1, R.drawable.p3, R.drawable.ic_bookmark,
                "Suraj", "Engineer", "100", "90", "150"));
        dashBoardList.add(new DashBoardModel(R.drawable.p2, R.drawable.p6, R.drawable.ic_bookmark,
                "Sohan", "Designer", "200", "180", "250"));

        DashboardAdapter dashboardAdapter = new DashboardAdapter(dashBoardList,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        dashboardRv.setLayoutManager(linearLayoutManager);
        dashboardRv.setNestedScrollingEnabled(false);
        dashboardRv.setAdapter(dashboardAdapter);


        return view;
    }
}