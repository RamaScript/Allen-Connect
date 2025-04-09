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

public class requestsNotificationFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<notificationModel> list;
    notificationAdapter adapter;

    public requestsNotificationFragment() {
        // Required empty public constructor
    }

    public static requestsNotificationFragment newInstance(String param1, String param2) {
        requestsNotificationFragment fragment = new requestsNotificationFragment();
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
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Ramanand</b> Sent you a request", "Just Now"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Raju</b> Sent you a request ", "kal "));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>shadow </b> Sent you a request", "parso"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>yashraj </b> Sent you a request", " 1 oct"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>arvind</b> Sent you a request", "1 oct"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>aman </b> Sent you a request", "1 oct"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Vikram</b> sent you a friend request", "1 hr ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Anjali</b> sent you a friend request", "2 hrs ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Arjun</b> sent you a friend request", "3 hrs ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Kavya</b> sent you a friend request", "5 hrs ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Mohan</b> sent you a friend request", "Yesterday"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Aisha</b> sent you a friend request", "Yesterday"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Rohan</b> sent you a friend request", "2 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Prerna</b> sent you a follow request", "2 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Nikhil</b> sent you a friend request", "3 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Shreya</b> sent you a friend request", "4 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Gaurav</b> sent you a follow request", "5 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Tanvi</b> sent you a friend request", "6 days ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Siddharth</b> sent you a friend request",
                "1 week ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Kirti</b> sent you a follow request", "1 week ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Vijay</b> sent you a friend request", "1 week ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Aditi</b> sent you a friend request", "2 weeks ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Kabir</b> sent you a friend request", "2 weeks ago"));
        list.add(
                new notificationModel(R.drawable.ic_avatar, "<b>Nandini</b> sent you a follow request", "2 weeks ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Rahul</b> sent you a friend request", "2 weeks ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Simran</b> sent you a friend request", "3 weeks ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Vivek</b> sent you a friend request", "3 weeks ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Neelam</b> sent you a follow request", "3 weeks ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Raj</b> sent you a friend request", "1 month ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Meena</b> sent you a friend request", "1 month ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Sachin</b> sent you a friend request", "1 month ago"));
        list.add(
                new notificationModel(R.drawable.ic_avatar, "<b>Deepika</b> sent you a follow request", "1 month ago"));
        list.add(new notificationModel(R.drawable.ic_avatar, "<b>Akshay</b> sent you a friend request", "1 month ago"));

        adapter = new notificationAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }
}