package com.ramascript.allenconnect.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramascript.allenconnect.R;

public class RequestsNotificationFragment extends Fragment {


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
        return inflater.inflate(R.layout.fragment_requests_notification, container, false);
    }
}