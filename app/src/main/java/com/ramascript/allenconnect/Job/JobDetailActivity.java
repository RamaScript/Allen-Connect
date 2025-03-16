package com.ramascript.allenconnect.Job;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.MainActivity;
import com.ramascript.allenconnect.User.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityJobDetailBinding;
import com.squareup.picasso.Picasso;

public class JobDetailActivity extends AppCompatActivity {

    ActivityJobDetailBinding binding;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityJobDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();

        binding.backBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobDetailActivity.this, MainActivity.class);
                intent.putExtra("openFragment", "JobsFragment");
                startActivity(intent);
                finish();
            }
        });

        String jobId = getIntent().getStringExtra("jobID");

        database.getReference().child("Jobs").child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    JobModel jobModel = snapshot.getValue(JobModel.class);

                    // Populate views using JobModel and binding

                    Picasso.get()
                        .load(jobModel.getLogoImgPath())
                        .placeholder(R.drawable.ic_avatar)
                        .into(binding.companyLogo);

                    binding.jobTitle.setText(Html.fromHtml("<b>Designation:</b> " + jobModel.getJobTitle()));
                    binding.companyName.setText(Html.fromHtml("<b>Company Name:</b> " + jobModel.getCompanyName()));
                    binding.jobType.setText(Html.fromHtml("<b>Job Type:</b> " + jobModel.getJobType()));
                    binding.experienceLevel.setText(Html.fromHtml("<b>Experience Required:</b> " + jobModel.getExperienceRequired()));
                    binding.courseEligibility.setText(Html.fromHtml("<b>Courses Eligible:</b> " + String.join(", ", jobModel.getCoursesEligible())));
                    binding.skillsRequired.setText(Html.fromHtml("<b>Skills Required:</b> " + String.join(", ", jobModel.getSkillsRequired())));
                    binding.salary.setText(Html.fromHtml("<b>Salary:</b> " + jobModel.getSalary()));
                    binding.contactEmail.setText(Html.fromHtml("<b>Contact Email:</b> " + jobModel.getContactEmail()));
                    binding.contactPhone.setText(Html.fromHtml("<b>Contact Phone:</b> " + jobModel.getContactPhone()));
                    binding.jobDescription.setText(Html.fromHtml("<b>Job Description:</b> " + jobModel.getJobDescription()));
                    binding.applicationDeadline.setText(Html.fromHtml("<b>Application Deadline:</b> " + jobModel.getApplicationDeadline()));

                    database.getReference().child("Users").child(jobModel.getJobPostedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserModel userModel = snapshot.getValue(UserModel.class);

                                Picasso.get()
                                    .load(userModel.getProfilePhoto())
                                    .placeholder(R.drawable.ic_avatar)
                                    .into(binding.jobPosterProfilePic);

                                binding.jobPosterName.setText(Html.fromHtml(userModel.getName()));

                                if(userModel.getUserType().equals("Alumni")){
                                    binding.jobPosterTitle.setText(Html.fromHtml("<b>" + userModel.getJobRole() + "</b>" +
                                                                                    " at <b>" + userModel.getCompany() + "</b> " +
                                                                                    "<br><i>Alumni AllenHouse</i> - " +
                                                                                    userModel.getPassingYear() + " Batch."));
                                }
                                else if(userModel.getUserType().equals("Professor")){
                                    binding.jobPosterTitle.setText(Html.fromHtml("<b> Prof. </b> at Allenhouse"));
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}