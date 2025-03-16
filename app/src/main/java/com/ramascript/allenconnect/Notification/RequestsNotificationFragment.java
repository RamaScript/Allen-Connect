package com.ramascript.allenconnect.Notification;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramascript.allenconnect.R;

import java.util.ArrayList;

public class RequestsNotificationFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<NotificationModel> list;
    NotificationAdapter adapter;


    public RequestsNotificationFragment() {
        // Required empty public constructor
    }


    public static RequestsNotificationFragment newInstance(String param1, String param2) {
        RequestsNotificationFragment fragment = new RequestsNotificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests_notification, container, false);

        recyclerView = view.findViewById(R.id.requestNotificationRV);
        list = new ArrayList<>();
        list.add(new NotificationModel(R.drawable.ic_avatar, "<b>Ramanand</b> Sent you a request", "Just Now"));
        list.add(new NotificationModel(R.drawable.ic_avatar, "<b>Raju</b> Sent you a request ", "kal "));
        list.add(new NotificationModel(R.drawable.ic_avatar, "<b>shadow </b> Sent you a request", "parso"));
        list.add(new NotificationModel(R.drawable.ic_avatar, "<b>yashraj </b> Sent you a request", " 1 oct"));
        list.add(new NotificationModel(R.drawable.ic_avatar, "<b>arvind</b> Sent you a request", "1 oct"));
        list.add(new NotificationModel(R.drawable.ic_avatar, "<b>aman </b> Sent you a request", "1 oct"));

        adapter = new NotificationAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }
}