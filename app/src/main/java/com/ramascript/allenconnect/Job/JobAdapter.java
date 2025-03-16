package com.ramascript.allenconnect.Job;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvJobsBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.viewHolder> {

    Context context;
    ArrayList<JobModel> list;

    public JobAdapter(Context context, ArrayList<JobModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_jobs,parent,false);

        return new viewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        JobModel jobModel = list.get(position);
//        holder.companyLogo.setImageResource(jobModel.getCompanyLogo());
        Picasso.get()
                .load(jobModel.getLogoImgPath())
                .placeholder(R.drawable.ic_avatar)
                .into(holder.companyLogo);
        holder.companyName.setText(jobModel.getCompanyName());
        holder.jobTitle.setText(jobModel.getJobTitle());

        holder.binding.jobViewDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, JobDetailActivity.class);
                intent.putExtra("jobID",jobModel.getJobID());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        RvJobsBinding binding;

        ImageView companyLogo;
        TextView jobTitle,companyName;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = RvJobsBinding.bind(itemView);

            companyLogo = binding.compnayLogo;
            jobTitle = binding.jobTitle;
            companyName = binding.companyName;
        }
    }
}
