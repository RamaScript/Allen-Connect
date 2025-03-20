package com.ramascript.allenconnect.features.job;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ramascript.allenconnect.utils.baseFragment;
import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentJobPostBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.util.Patterns;

public class jobPostFragment extends baseFragment {

    FragmentJobPostBinding binding;
    Uri logoUri;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;

    public jobPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentJobPostBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Posting Job...");
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // here i am setting the profile pic and username and title of user from
        // database to the job post fragment
        database.getReference()
                .child("Users")
                .child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userModel userModel = snapshot.getValue(com.ramascript.allenconnect.features.user.userModel.class);
                            Picasso.get()
                                    .load(userModel.getProfilePhoto())
                                    .placeholder(R.drawable.ic_avatar)
                                    .into(binding.profileImage);
                            binding.name.setText(userModel.getName());

                            // Set the text based on user type
                            if ("Student".equals(userModel.getUserType())) {
                                binding.title.setText(userModel.getCourse() + " (" + userModel.getYear() + " year)");
                            } else if ("Alumni".equals(userModel.getUserType())) {
                                binding.title.setText(userModel.getJobRole() + " at " + userModel.getCompany());
                            } else if ("Professor".equals(userModel.getUserType())) {
                                binding.title.setText("Professor at AGOI");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // here i am Setting up the adapter for MultiAutoCompleteTextView
        // For Skills Required
        List<String> skills = Arrays.asList(
                // BCA Key Skills
                "Java", "Python", "C", "C++", "HTML", "CSS", "JavaScript", "SQL", "MySQL", "React", "Node.js",
                "Android Development", "Data Structures", "Algorithms", "Object-Oriented Programming", "Git/GitHub",
                "Linux",
                // BBA Key Skills
                "Project Management", "Business Communication", "Financial Analysis", "Digital Marketing",
                "Market Research", "Entrepreneurship", "Leadership", "Strategic Thinking", "Negotiation Skills",
                "Accounting Principles", "Business Planning", "Excel",
                // MBA Key Skills
                "Leadership", "Strategic Planning", "Project Management", "Financial Management", "Business Analytics",
                "Consulting", "Change Management", "Corporate Strategy", "Data Analytics", "Business Intelligence",
                "Market Research", "Negotiation Skills",
                // BTech Key Skills
                "Java", "C", "C++", "Python", "Data Structures", "Algorithms", "Object-Oriented Programming",
                "Web Development", "Mobile App Development", "Machine Learning", "Cloud Computing",
                "Database Management", "Network Security", "DevOps", "Agile Methodology", "Git",
                "Artificial Intelligence");
        setupMultiAutoCompleteTextView(binding.skillsRequired, skills);

        // For Courses Eligible
        List<String> courses = Arrays.asList("BCA", "BBA", "MBA", "B Tech");
        setupMultiAutoCompleteTextView(binding.coursesEligible, courses);

        // picking last date to apply
        binding.applicationDeadlinePickerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                    int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our text view.
                                binding.applicationDeadlineET
                                        .setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

        // logo upload wala kaam yaha ho rha hai
        binding.uploadLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 20);
            }
        });

        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jobTitle = binding.jobTitle.getText().toString().trim();
                String companyName = binding.companyName.getText().toString().trim();
                String jobType = binding.jobType.getSelectedItem().toString();
                String experienceRequired = binding.experienceRequired.getSelectedItem().toString();
                String skillsRequired = binding.skillsRequired.getText().toString().trim();
                String coursesEligible = binding.coursesEligible.getText().toString().trim();
                String contactEmail = binding.contactEmail.getText().toString().trim();
                String contactPhone = binding.contactPhone.getText().toString().trim();
                String jobDescription = binding.jobDescription.getText().toString().trim();
                String salary = binding.jobSalary.getText().toString().trim();
                String applicationDeadline = binding.applicationDeadlineET.getText().toString().trim();

                // Validation flag
                boolean isValid = true;

                // in this block i am validating all the fields
                {
                    // Validate jobTitle
                    if (jobTitle.isEmpty()) {
                        isValid = false;
                        binding.jobTitle.setError("Job title is required.");
                        binding.jobTitle.requestFocus();
                    }

                    // Validate companyName
                    if (companyName.isEmpty()) {
                        isValid = false;
                        binding.companyName.setError("Company name is required.");
                        binding.companyName.requestFocus();
                    }

                    // Validate jobType
                    if (jobType.equals("Select Job Type")) {
                        isValid = false;
                        Toast.makeText(getContext(), "Please select a valid Job Type.", Toast.LENGTH_SHORT).show();
                    }

                    // Validate experienceRequired
                    if (experienceRequired.equals("Select Experience Level")) {
                        isValid = false;
                        Toast.makeText(getContext(), "Please select the required experience.", Toast.LENGTH_SHORT)
                                .show();
                    }

                    // Validate skillsRequired
                    if (skillsRequired.isEmpty()) {
                        isValid = false;
                        binding.skillsRequired.setError("Skills required field cannot be empty.");
                        binding.skillsRequired.requestFocus();
                    }

                    // Validate coursesEligible
                    if (coursesEligible.isEmpty()) {
                        isValid = false;
                        binding.coursesEligible.setError("Eligible courses field cannot be empty.");
                        binding.coursesEligible.requestFocus();
                    }

                    // Validate contactEmail
                    if (contactEmail.isEmpty()) {
                        isValid = false;
                        binding.contactEmail.setError("Contact email is required.");
                        binding.contactEmail.requestFocus();
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(contactEmail).matches()) {
                        isValid = false;
                        binding.contactEmail.setError("Please enter a valid email address.");
                        binding.contactEmail.requestFocus();
                    }

                    // Validate contactPhone
                    if (!contactPhone.isEmpty() && !contactPhone.matches("\\d{10}")) { // Check only if not empty
                        isValid = false;
                        binding.contactPhone.setError("Please enter a valid 10-digit phone number.");
                        binding.contactPhone.requestFocus();
                    }

                    // Validate jobDescription
                    if (jobDescription.isEmpty()) {
                        isValid = false;
                        binding.jobDescription.setError("Job description is required.");
                        binding.jobDescription.requestFocus();
                    }

                    // Validate applicationDeadline
                    if (applicationDeadline.isEmpty()) {
                        isValid = false;
                        binding.applicationDeadlineET.setError("Application deadline is required.");
                        binding.applicationDeadlineET.requestFocus();
                    }
                }

                if (isValid) {
                    dialog.show();

                    // Prepare the jobModel object
                    jobModel jobModel = new jobModel();

                    jobModel.setJobPostedBy(auth.getUid());
                    jobModel.setJobPostedAt(new Date().getTime());

                    jobModel.setJobTitle(jobTitle);
                    jobModel.setCompanyName(companyName);
                    jobModel.setJobType(jobType);
                    jobModel.setExperienceRequired(experienceRequired);
                    jobModel.setSkillsRequired(Arrays.asList(skillsRequired.split(",")));
                    jobModel.setCoursesEligible(Arrays.asList(coursesEligible.split(",")));
                    jobModel.setSalary(salary);
                    jobModel.setJobDescription(jobDescription);
                    jobModel.setContactEmail(contactEmail);
                    jobModel.setContactPhone(contactPhone);
                    jobModel.setApplicationDeadline(applicationDeadline);

                    // Upload the logo image if selected
                    if (logoUri != null) {

                        final StorageReference logoReference = storage.getReference().child("JobLogos")
                                .child(auth.getUid())
                                .child(new Date().getTime() + "_logo");

                        logoReference.putFile(logoUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        logoReference.getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {

                                                        // Logo uploaded successfully, get the download URL
                                                        String logoUrl = uri.toString();

                                                        // Set image URL in job model
                                                        jobModel.setLogoImgPath(logoUrl);

                                                        // Save the job posting to Firebase database
                                                        database.getReference().child("Jobs").push().setValue(jobModel)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(getContext(),
                                                                                "Job posted successfully!",
                                                                                Toast.LENGTH_SHORT).show();
                                                                        // Optionally, navigate back or clear the fields
                                                                        clearFields();
                                                                    }
                                                                }).addOnFailureListener(e -> {
                                                                    dialog.dismiss();
                                                                    Toast.makeText(getContext(),
                                                                            "Failed to post job. Please try again.",
                                                                            Toast.LENGTH_SHORT).show();
                                                                });
                                                    }
                                                });
                                    }
                                });
                    } else {
                        // If no logo is uploaded, you can proceed with the poster image URL and set a
                        // default logo URL
                        jobModel.setLogoImgPath(String.valueOf(R.drawable.ic_avatar));

                        // Save job to the database without the logo image
                        database.getReference().child("Jobs").push().setValue(jobModel)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "Job posted successfully!", Toast.LENGTH_SHORT)
                                                .show();
                                        clearFields();
                                    }
                                }).addOnFailureListener(e -> {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), "Failed to post job. Please try again.",
                                            Toast.LENGTH_SHORT).show();
                                });
                    }

                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            if (requestCode == 20) { // Logo upload
                logoUri = data.getData(); // Assign to logoUri
                binding.uploadLogoTV.setVisibility(View.GONE);
                binding.uploadLogoIcIV.setVisibility(View.GONE);
                binding.uploadLogoIV.setVisibility(View.VISIBLE);
                binding.uploadLogoIV.setImageURI(logoUri);
            }
        }
    }

    private void setupMultiAutoCompleteTextView(MultiAutoCompleteTextView view, List<String> allItems) {
        // Copy of the original list to manage dropdown options
        List<String> availableItems = new ArrayList<>(allItems);

        // Adapter for MultiAutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line,
                availableItems);
        view.setAdapter(adapter);
        view.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        // Add a TextWatcher to dynamically update the dropdown
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Get the already selected items
                String[] selectedItems = s.toString().split(",\\s*"); // Split by comma and trim spaces

                // Update the list of available options
                availableItems.clear();
                for (String item : allItems) {
                    if (!Arrays.asList(selectedItems).contains(item)) { // Exclude already selected items
                        availableItems.add(item);
                    }
                }

                // Notify adapter about data change
                adapter.notifyDataSetChanged();

                // Show dropdown again if there are options left
                if (!availableItems.isEmpty()) {
                    view.showDropDown();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Show dropdown explicitly on click or focus
        view.setOnClickListener(v -> {
            if (!availableItems.isEmpty()) {
                view.showDropDown();
            }
        });

        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !availableItems.isEmpty()) {
                view.showDropDown();
            }
        });
    }

    private void clearFields() {
        binding.jobTitle.setText("");
        binding.companyName.setText("");
        binding.skillsRequired.setText("");
        binding.coursesEligible.setText("");
        binding.contactEmail.setText("");
        binding.contactPhone.setText("");
        binding.jobDescription.setText("");
        binding.jobSalary.setText("");
        binding.applicationDeadlineET.setText("");
        binding.uploadLogoIV.setImageURI(null);
        binding.uploadLogoTV.setVisibility(View.VISIBLE);
        binding.uploadLogoIcIV.setVisibility(View.VISIBLE);
        binding.uploadLogoIV.setVisibility(View.GONE);
    }
}
