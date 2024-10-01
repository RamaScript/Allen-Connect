package com.ramascript.allenconnect.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.ramascript.allenconnect.Adapters.FriendAdapter;
import com.ramascript.allenconnect.Models.FriendModel;
import com.ramascript.allenconnect.R;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<FriendModel> list;

    public ProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView notificationMenuIV = view.findViewById(R.id.profileSettingsMenuBtn);

        notificationMenuIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(getContext(), v);

                // Inflate the menu resource
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

                // Set a click listener for menu item clicks
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.action_settings){
                            // Handle settings click
                            // Add your logic for opening settings or handling the event
                            return true;
                        } else if (item.getItemId() == R.id.action_logout) {
//                            handle
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                // Show the popup menu
                popupMenu.show();
            }
        });

        recyclerView = view.findViewById(R.id.friendRV);

        list = new ArrayList<>();

        list.add(new FriendModel(R.drawable.p8));
        list.add(new FriendModel(R.drawable.p1));
        list.add(new FriendModel(R.drawable.p2));
        list.add(new FriendModel(R.drawable.p3));
        list.add(new FriendModel(R.drawable.p5));
        list.add(new FriendModel(R.drawable.p6));
        list.add(new FriendModel(R.drawable.p7));
        list.add(new FriendModel(R.drawable.p8));

        FriendAdapter adapter = new FriendAdapter(list,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }
}