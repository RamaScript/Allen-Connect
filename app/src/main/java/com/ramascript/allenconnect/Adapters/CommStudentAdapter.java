package com.ramascript.allenconnect.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.Models.CommStudentModel;
import com.ramascript.allenconnect.Models.JobModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvCommStudentBinding;
import com.ramascript.allenconnect.databinding.RvJobsBinding;

import java.util.ArrayList;

public class CommStudentAdapter extends RecyclerView.Adapter<CommStudentAdapter.viewHolder>{

    Context context;
    ArrayList<CommStudentModel> list;

    public CommStudentAdapter(Context context, ArrayList<CommStudentModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_comm_student,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        CommStudentModel commStudentModel = list.get(position);

        holder.profilePic.setImageResource(commStudentModel.getProfilePic());
        holder.name.setText(commStudentModel.getName());
        holder.course.setText(commStudentModel.getCourse());
        holder.year.setText(" ("+commStudentModel.getYear()+")");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        RvCommStudentBinding binding;

        ImageView profilePic;
        TextView name,course,year;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RvCommStudentBinding.bind(itemView);
            profilePic = binding.studentPic;
            name = binding.name;
            course = binding.courseTV;
            year = binding.yearTV;
        }
    }
}
