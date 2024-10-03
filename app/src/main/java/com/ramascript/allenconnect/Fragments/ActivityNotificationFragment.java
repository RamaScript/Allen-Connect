package com.ramascript.allenconnect.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramascript.allenconnect.Adapters.NotificationAdapter;
import com.ramascript.allenconnect.Models.NotificationModel;
import com.ramascript.allenconnect.R;

import java.util.ArrayList;


public class ActivityNotificationFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<NotificationModel> list;
    NotificationAdapter adapter;

    public ActivityNotificationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity_notification, container, false);

        recyclerView = view.findViewById(R.id.notificationRV);
        list = new ArrayList<>();
        list.add(new NotificationModel(R.drawable.p3, "<b>Ramanand</b> mentioned u in comment", "Just Now"));
        list.add(new NotificationModel(R.drawable.p1, "<b>Raju</b> mentioned u in Story", "kal "));
        list.add(new NotificationModel(R.drawable.p2, "<b>shadow </b>mentioned u in comment", "parso"));
        list.add(new NotificationModel(R.drawable.p5, "<b>yashraj </b>mentioned u in comment", " 1 oct"));
        list.add(new NotificationModel(R.drawable.p6, "<b>arvind</b> mentioned u in comment", "1 oct"));
        list.add(new NotificationModel(R.drawable.p7, "<b>aman </b>mentioned u in comment", "1 oct"));

        adapter = new NotificationAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }
}