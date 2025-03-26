package com.ramascript.allenconnect.features.job;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.ramascript.allenconnect.base.mainActivity;
import com.ramascript.allenconnect.features.chat.chatDetailActivity;
import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityJobDetailBinding;
import com.squareup.picasso.Picasso;

public class jobDetailActivity extends AppCompatActivity {

    private ActivityJobDetailBinding binding;
    private FirebaseDatabase database;
    private userModel user;
    private jobModel job;
    private String jobId;

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
        jobId = getIntent().getStringExtra("jobID");

        binding.backBtnIV.setOnClickListener(v -> {
            Intent intent = new Intent(jobDetailActivity.this, mainActivity.class);
            intent.putExtra("openFragment", "jobsFragment");
            startActivity(intent);
            finish();
        });

        // Disable chat button initially
        binding.chat.setEnabled(false);
        loadJobDetails();
    }

    private void loadJobDetails() {
        if (jobId == null) return;

        database.getReference().child("Jobs").child(jobId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) return;

                    job = snapshot.getValue(jobModel.class);
                    if (job == null) return;

                    updateUI(job);
                    loadJobPoster(job.getJobPostedBy());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
    }

    private void updateUI(jobModel job) {

            Picasso.get()
                .load(job.getLogoImgPath())
                .placeholder(R.drawable.ic_avatar)
                .into(binding.companyLogo);

            setTextWithHtml(binding.jobTitle, "Designation:", job.getJobTitle());
            setTextWithHtml(binding.companyName, "Company Name:", job.getCompanyName());
            setTextWithHtml(binding.jobType, "Job Type:", job.getJobType());
            setTextWithHtml(binding.experienceLevel, "Experience Required:", job.getExperienceRequired());
            setTextWithHtml(binding.courseEligibility, "Courses Eligible:", String.join(", ", job.getCoursesEligible()));
            setTextWithHtml(binding.skillsRequired, "Skills Required:", String.join(", ", job.getSkillsRequired()));
            setTextWithHtml(binding.salary, "Salary:", job.getSalary());
            setTextWithHtml(binding.contactEmail, "Contact Email:", job.getContactEmail());
            setTextWithHtml(binding.contactPhone, "Contact Phone:", job.getContactPhone());
            setTextWithHtml(binding.jobDescription, "Job Description:", job.getJobDescription());
            setTextWithHtml(binding.applicationDeadline, "Application Deadline:", job.getApplicationDeadline());

    }

    private void loadJobPoster(String userId) {
        if (userId == null) return;

        database.getReference().child("Users").child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) return;

                    user = snapshot.getValue(userModel.class);
                    if (user == null) return;

                    updateJobPosterUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
    }

    private void updateJobPosterUI() {
            Picasso.get()
                .load(user.getProfilePhoto())
                .placeholder(R.drawable.ic_avatar)
                .into(binding.jobPosterProfilePic);

            binding.jobPosterName.setText(Html.fromHtml(user.getName()));

            String jobPosterTitle;
            if ("Alumni".equals(user.getUserType())) {
                jobPosterTitle = "<b>" + user.getJobRole() + "</b> at <b>" +
                    user.getCompany() + "</b><br><i>Alumni AllenHouse</i> - " +
                    user.getPassingYear() + " Batch.";
            } else if ("Professor".equals(user.getUserType())) {
                jobPosterTitle = "<b>Prof.</b> at Allenhouse";
            } else {
                jobPosterTitle = "";
            }
            binding.jobPosterTitle.setText(Html.fromHtml(jobPosterTitle));

            // Enable chat button now that user data is available
            binding.chat.setEnabled(true);
            binding.chat.setOnClickListener(v -> openChat());
    }

    private void openChat() {
        if (user == null) return;
        Log.d("ChatDebug", "User ID: " + job.getJobPostedBy());
        Log.d("ChatDebug", "User Profile: " + user.getProfilePhoto());
        Log.d("ChatDebug", "User Name: " + user.getName());

        Toast.makeText(this, "clicked on chat", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(jobDetailActivity.this, chatDetailActivity.class);
        intent.putExtra("userId", job.getJobPostedBy());
        intent.putExtra("profilePic", user.getProfilePhoto());
        intent.putExtra("userName", user.getName());
        startActivity(intent);
        Toast.makeText(this, "started", Toast.LENGTH_SHORT).show();
    }

    private void setTextWithHtml(View view, String label, String value) {
        if (value == null || value.trim().isEmpty()) return;
        if (view instanceof android.widget.TextView) {
            ((android.widget.TextView) view).setText(Html.fromHtml("<b>" + label + "</b> " + value));
        }
    }
}
