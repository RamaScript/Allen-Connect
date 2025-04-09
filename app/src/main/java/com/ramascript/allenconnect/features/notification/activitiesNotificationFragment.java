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

public class activitiesNotificationFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<notificationModel> list;
    notificationAdapter adapter;

    public activitiesNotificationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities_notification, container, false);

        recyclerView = view.findViewById(R.id.notificationRV);
        list = new ArrayList<>();
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Ramanand</b> mentioned u in comment", "Just Now"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Raju</b> mentioned u in Story", "kal "));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>shadow </b>mentioned u in comment", "parso"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>yashraj </b>mentioned u in comment", " 1 oct"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>arvind</b> mentioned u in comment", "1 oct"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>aman </b>mentioned u in comment", "1 oct"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Vikram</b> liked your post", "2 hrs ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Priya</b> commented on your photo", "3 hrs ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Ananya</b> shared your post", "5 hrs ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Rajesh</b> replied to your comment", "Yesterday"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Neha</b> tagged you in a post", "Yesterday"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Amit</b> mentioned you in a group", "2 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Shreya</b> reacted to your story", "3 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Varun</b> commented on your photo", "4 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Meera</b> invited you to an event", "5 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Sanjay</b> mentioned you in a comment", "1 week ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Pooja</b> started following you", "1 week ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Karthik</b> liked your comment", "2 weeks ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Divya</b> shared your profile", "2 weeks ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Rahul</b> sent you a message", "3 weeks ago"));

        adapter = new notificationAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }
}