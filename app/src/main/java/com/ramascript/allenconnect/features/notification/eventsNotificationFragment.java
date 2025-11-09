package com.ramascript.allenconnect.features.notification;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramascript.allenconnect.R;

import java.util.ArrayList;

public class eventsNotificationFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<notificationModel> list;
    notificationAdapter adapter;

    public eventsNotificationFragment() {
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
        View view = inflater.inflate(R.layout.fragment_events_notification, container, false);

        recyclerView = view.findViewById(R.id.eventNotificationRV);
        list = new ArrayList<>();

        list.add(new notificationModel(R.drawable.exuberance, "<b>Exuberance</b> college fest is coming on 12th April",
                "2 days ago"));
        list.add(new notificationModel(R.drawable.winter_gala, "<b>Winter Gala</b> is on 31st December", "3 days ago"));
        list.add(
                new notificationModel(R.drawable.csjm, "<b>University Exams</b> start on 10th May", "1 week ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Annual Cultural Fest</b> is scheduled for 15th March",
                "Yesterday"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Tech Summit</b> happening on 5th June", "5 days ago"));
        list.add(
                new notificationModel(R.drawable.ic_avatar, "<b>Sports Day</b> event on 20th February", "2 weeks ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Industry Visit</b> to InfoTech on 8th April",
                "4 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>College Farewell</b> party on 25th May", "1 day ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Hackathon</b> registrations open until 18th April",
                "3 hours ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Alumni Meet</b> scheduled for 22nd July",
                "1 week ago"));

        adapter = new notificationAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }
}