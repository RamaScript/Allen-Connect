package com.ramascript.allenconnect.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramascript.allenconnect.Adapters.JobAdapter;
import com.ramascript.allenconnect.Models.JobModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentJobsBinding;

import java.util.ArrayList;

public class JobsFragment extends Fragment {


    private FragmentJobsBinding binding;
    ArrayList<JobModel> list;

    public JobsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Step 2: Inflate the layout using the binding
        binding = FragmentJobsBinding.inflate(inflater, container, false);

        list = new ArrayList<>();
        list.add(new JobModel("TCS","Software Developer","https://logowik.com/content/uploads/images/tcs-tata-consultancy-services2792.logowik.com.webp"));
        list.add(new JobModel("W3villa","Web Designer","https://photo.isu.pub/w3villa/photo_large.jpg"));
        list.add(new JobModel("Infosys","Full stack Developer","https://i0.wp.com/logotaglines.com/wp-content/uploads/2016/08/Infosys-Logo-Tagline-Slogan-Founders.webp?fit=640,640&ssl=1"));
        list.add(new JobModel("IBM","App Developer","https://logowik.com/content/uploads/images/416_ibm.jpg"));
        list.add(new JobModel("Meta","Business Analyst","https://freelogopng.com/images/all_img/1664035876new-meta-logo.png"));
        list.add(new JobModel("Google","React Developer","https://t3.ftcdn.net/jpg/03/88/07/84/360_F_388078454_mKtbdXYF9cyQovCCTsjqI0gbfu7gCcSp.jpg"));


        JobAdapter adapter = new JobAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.jobsRV.setLayoutManager(layoutManager);  // Using binding
        binding.jobsRV.setAdapter(adapter);  // Using binding

        return binding.getRoot();
    }
}
