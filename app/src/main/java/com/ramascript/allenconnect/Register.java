package com.ramascript.allenconnect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.databinding.ActivityRegisterBinding;

public class Register extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Register.this, RegisterAs.class);
                startActivity(i);
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        String userType = getIntent().getStringExtra("userType");
        final StorageReference reference = storage.getReference().child("profile_pictures").child("ic_avatar.png");

        if (userType.equals("Student")) {
            binding.studentCV.setVisibility(View.VISIBLE);
            binding.alumniCV.setVisibility(View.GONE);
            binding.professorCV.setVisibility(View.GONE);

            binding.studentRegisterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = binding.studentEmailET.getText().toString().trim();
                    String password = binding.studentPasswordET.getText().toString().trim();
                    String name = binding.nameET.getText().toString().trim();
                    String crn = binding.crnET.getText().toString().trim();
                    String year = binding.yearET.getText().toString().trim();
                    String course = crn.substring(2, 5);
                    String phone = binding.studentPhoneNoET.getText().toString().trim();

                    // Validate input fields
                    boolean isValid = true;

                    //in this block i am cheaking all the values. kaam to nhi hai kuch is {block} ka bas code ko minimize kar saku is liye laga diya hai
                    {
                        // Name validation
                        if (name.isEmpty()) {
                            binding.nameET.setError("Name is required");
                            isValid = false;
                        }

                        // CRN validation
                        if (crn.isEmpty()) {
                            binding.crnET.setError("CRN is required");
                            isValid = false;
                        }

                        // Year validation
                        if (year.isEmpty()) {
                            binding.yearET.setError("Year is required");
                            isValid = false;
                        }

                        // Course validation
                        if (course.isEmpty()) {
                            isValid = false;
                        }

                        // Phone validation (10 digits check)
                        if (phone.isEmpty()) {
                            binding.studentPhoneNoET.setError("Phone number is required");
                            isValid = false;
                        } else if (!phone.matches("^\\d{10}$")) {  // Regex for exactly 10 digits
                            binding.studentPhoneNoET.setError("Please enter exactly 10 digits");
                            isValid = false;
                        }

                        // Email validation
                        if (email.isEmpty()) {
                            binding.studentEmailET.setError("Email is required");
                            isValid = false;
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            binding.studentEmailET.setError("Please enter a valid email address");
                            isValid = false;
                        }

                        // Password validation
                        if (password.isEmpty()) {
                            binding.studentPasswordET.setError("Password is required");
                            isValid = false;
                        } else if (password.length() < 8) {
                            binding.studentPasswordET.setError("Password should be at least 8 characters");
                            isValid = false;
                        }
                    }

                    // Proceed with registration if all inputs are valid
                    if (isValid) {
                        // Show the ProgressBar when the registration process starts
                        binding.studentProgressBar.setVisibility(View.VISIBLE);
                        binding.studentRegisterBtn.setVisibility(View.GONE);

                        // Start the Firebase registration process
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Hide the ProgressBar once the registration is complete
                                binding.studentProgressBar.setVisibility(View.GONE);
                                binding.studentRegisterBtn.setVisibility(View.VISIBLE);

                                if (task.isSuccessful()) {
                                    // Create a UserModel object and store the user data in Firebase Realtime Database
                                    UserModel userModel = new UserModel(crn, email, phone, password, name, course, year, reference.getPath(), userType);

                                    // Get the user's unique Firebase ID
                                    String id = task.getResult().getUser().getUid();

                                    // Store the user data in Firebase Realtime Database under "Users"
                                    database.getReference().child("Users").child(id).setValue(userModel);

                                    binding.greenBoxTV.setText("Your " + userType + " account has been created!!!....");
                                    binding.greenBoxTV.setVisibility(View.VISIBLE);
                                    binding.errorBoxTV.setVisibility(View.GONE);

                                    // Notify the user that the account has been created and redirect them to the login screen
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent i = new Intent(Register.this, MainActivity.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }, 500);
                                } else if (task.getException() != null) {
                                    String errorMessage = task.getException().getLocalizedMessage();
                                    binding.errorBoxTV.setVisibility(View.VISIBLE);
                                    binding.errorBoxTV.setText(errorMessage);
                                }
                            }
                        });
                    }
                }
            });
        }
        else if (userType.equals("Alumni")) {
            binding.alumniCV.setVisibility(View.VISIBLE);
            binding.professorCV.setVisibility(View.GONE);
            binding.studentCV.setVisibility(View.GONE);

            binding.alumniRegisterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = binding.alumniEmailET.getText().toString().trim();
                    String password = binding.alumniPasswordET.getText().toString().trim();
                    String name = binding.alumniNameET.getText().toString().trim();
                    String course = binding.alumniCourseET.getText().toString().trim();
                    String passingYear = binding.alumniPassingYearET.getText().toString().trim();
                    String company = binding.alumniCompanyET.getText().toString().trim();
                    String jobRole = binding.alumniJobRoleET.getText().toString().trim();
                    String phone = binding.alumniPhoneNoET.getText().toString().trim();

                    // Validate input fields
                    boolean isValid = true;

                    //in this block i am cheaking all the values. kaam to nhi hai kuch is {block} ka bas code ko minimize kar saku is liye laga diya hai
                    {
                        // Name validation
                        if (name.isEmpty()) {
                            binding.alumniNameET.setError("Name is required");
                            isValid = false;
                        }

                        // Course validation
                        if (course.isEmpty()) {
                            binding.alumniCourseET.setError("Course is required");
                            isValid = false;
                        }

                        // Passing Year validation (should be a 4-digit number)
                        if (passingYear.isEmpty()) {
                            binding.alumniPassingYearET.setError("Passing year is required");
                            isValid = false;
                        } else if (!passingYear.matches("^\\d{4}$")) {
                            binding.alumniPassingYearET.setError("Enter a valid 4-digit year");
                            isValid = false;
                        }

                        // Company validation
                        if (company.isEmpty()) {
                            binding.alumniCompanyET.setError("Company name is required");
                            isValid = false;
                        }

                        // Job Role validation
                        if (jobRole.isEmpty()) {
                            binding.alumniJobRoleET.setError("Job role is required");
                            isValid = false;
                        }

                        // Phone validation (10 digits check)
                        if (phone.isEmpty()) {
                            binding.alumniPhoneNoET.setError("Phone number is required");
                            isValid = false;
                        } else if (!phone.matches("^\\d{10}$")) {  // Regex for exactly 10 digits
                            binding.alumniPhoneNoET.setError("Please enter exactly 10 digits");
                            isValid = false;
                        }

                        // Email validation
                        if (email.isEmpty()) {
                            binding.alumniEmailET.setError("Email is required");
                            isValid = false;
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            binding.alumniEmailET.setError("Please enter a valid email address");
                            isValid = false;
                        }

                        // Password validation
                        if (password.isEmpty()) {
                            binding.alumniPasswordET.setError("Password is required");
                            isValid = false;
                        } else if (password.length() < 8) {
                            binding.alumniPasswordET.setError("Password should be at least 8 characters");
                            isValid = false;
                        }
                    }

                    // Proceed with registration if all inputs are valid
                    if (isValid) {
                        // Show the ProgressBar when the registration process starts
                        binding.alumniProgressBar.setVisibility(View.VISIBLE);
                        binding.alumniRegisterBtn.setVisibility(View.GONE);

                        // Start the Firebase registration process
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Hide the ProgressBar once the registration is complete
                                binding.alumniProgressBar.setVisibility(View.GONE);
                                binding.alumniRegisterBtn.setVisibility(View.VISIBLE);

                                if (task.isSuccessful()) {
                                    // Create a UserModel object and store the user data in Firebase Realtime Database
                                    UserModel userModel = new UserModel(email,phone,password,name,course,passingYear,company,jobRole,reference.getPath(),userType);

                                    // Get the user's unique Firebase ID
                                    String id = task.getResult().getUser().getUid();

                                    // Store the user data in Firebase Realtime Database under "Users"
                                    database.getReference().child("Users").child(id).setValue(userModel);

                                    binding.greenBoxTV.setText("Your " + userType + " account has been created!!!....");
                                    binding.greenBoxTV.setVisibility(View.VISIBLE);
                                    binding.errorBoxTV.setVisibility(View.GONE);

                                    // Notify the user that the account has been created and redirect them to the login screen
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent i = new Intent(Register.this, MainActivity.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }, 1000);
                                } else if (task.getException() != null) {
                                    String errorMessage = task.getException().getLocalizedMessage();
                                    binding.errorBoxTV.setVisibility(View.VISIBLE);
                                    binding.errorBoxTV.setText(errorMessage);
                                }
                            }
                        });
                    }
                }
            });
        }
        else if (userType.equals("Professor")) {
            binding.professorCV.setVisibility(View.VISIBLE);
            binding.alumniCV.setVisibility(View.GONE);
            binding.studentCV.setVisibility(View.GONE);

            binding.professorRegisterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = binding.professorEmailET.getText().toString().trim();
                    String password = binding.professorPasswordET.getText().toString().trim();
                    String name = binding.professorNameET.getText().toString().trim();
                    String phone = binding.professorPhoneNoET.getText().toString().trim();

                    // Validate input fields
                    boolean isValid = true;

                    //in this block i am cheaking all the values. kaam to nhi hai kuch is {block} ka bas code ko minimize kar saku is liye laga diya hai
                    {
                        // Name validation
                        if (name.isEmpty()) {
                            binding.alumniNameET.setError("Name is required");
                            isValid = false;
                        }

                        // Phone validation (10 digits check)
                        if (phone.isEmpty()) {
                            binding.alumniPhoneNoET.setError("Phone number is required");
                            isValid = false;
                        } else if (!phone.matches("^\\d{10}$")) {  // Regex for exactly 10 digits
                            binding.alumniPhoneNoET.setError("Please enter exactly 10 digits");
                            isValid = false;
                        }

                        // Email validation
                        if (email.isEmpty()) {
                            binding.alumniEmailET.setError("Email is required");
                            isValid = false;
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            binding.alumniEmailET.setError("Please enter a valid email address");
                            isValid = false;
                        }

                        // Password validation
                        if (password.isEmpty()) {
                            binding.alumniPasswordET.setError("Password is required");
                            isValid = false;
                        } else if (password.length() < 8) {
                            binding.alumniPasswordET.setError("Password should be at least 8 characters");
                            isValid = false;
                        }
                    }

                    // Proceed with registration if all inputs are valid
                    if (isValid) {
                        // Show the ProgressBar when the registration process starts
                        binding.professorProgressBar.setVisibility(View.VISIBLE);
                        binding.professorRegisterBtn.setVisibility(View.GONE);

                        // Start the Firebase registration process
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Hide the ProgressBar once the registration is complete
                                binding.professorProgressBar.setVisibility(View.GONE);
                                binding.professorRegisterBtn.setVisibility(View.VISIBLE);

                                if (task.isSuccessful()) {
                                    // Create a UserModel object and store the user data in Firebase Realtime Database
                                    UserModel userModel = new UserModel(name,phone,email,password,reference.getPath(),userType);

                                    // Get the user's unique Firebase ID
                                    String id = task.getResult().getUser().getUid();

                                    // Store the user data in Firebase Realtime Database under "Users"
                                    database.getReference().child("Users").child(id).setValue(userModel);

                                    binding.greenBoxTV.setText("Your " + userType + " account has been created!!!....");
                                    binding.greenBoxTV.setVisibility(View.VISIBLE);
                                    binding.errorBoxTV.setVisibility(View.GONE);

                                    // Notify the user that the account has been created and redirect them to the login screen
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent i = new Intent(Register.this, MainActivity.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }, 1000);
                                } else if (task.getException() != null) {
                                    String errorMessage = task.getException().getLocalizedMessage();
                                    binding.errorBoxTV.setVisibility(View.VISIBLE);
                                    binding.errorBoxTV.setText(errorMessage);
                                }
                            }
                        });
                    }
                }
            });
        } else {
            binding.professorCV.setVisibility(View.GONE);
            binding.alumniCV.setVisibility(View.GONE);
            binding.studentCV.setVisibility(View.GONE);
        }
    }
}