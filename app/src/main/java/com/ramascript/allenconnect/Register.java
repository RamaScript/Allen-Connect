package com.ramascript.allenconnect;

import android.content.Intent;
import android.os.Bundle;
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

        String userType = getIntent().getStringExtra("userType");

        if(userType.equals("Student")){
            binding.studentCV.setVisibility(View.VISIBLE);
            binding.alumniCV.setVisibility(View.GONE);
            binding.professorCV.setVisibility(View.GONE);
        }
        else if (userType.equals("Alumni")) {
            binding.alumniCV.setVisibility(View.VISIBLE);
            binding.professorCV.setVisibility(View.GONE);
            binding.studentCV.setVisibility(View.GONE);
        }
        else if (userType.equals("Professor")) {
            binding.professorCV.setVisibility(View.VISIBLE);
            binding.alumniCV.setVisibility(View.GONE);
            binding.studentCV.setVisibility(View.GONE);
        }
        else {
            binding.professorCV.setVisibility(View.GONE);
            binding.alumniCV.setVisibility(View.GONE);
            binding.studentCV.setVisibility(View.GONE);
        }

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

        if (userType.equals("Student")){
            binding.studentRegisterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = binding.studentEmailET.getText().toString();
                    String password = binding.studentPasswordET.getText().toString();
                    final StorageReference reference = storage.getReference().child("profile_pictures").child("ic_avatar.png");
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                UserModel userModel = new UserModel(binding.crnET.getText().toString(),
                                                                                    email,
                                                                                    binding.studentPhoneNoET.getText().toString(),
                                                                                    binding.studentUsernameET.getText().toString(),
                                                                                    password,
                                                                                    binding.nameET.getText().toString(),
                                                                                    binding.courseET.getText().toString(),
                                                                                    binding.yearET.getText().toString(),
                                                                                    reference.getPath());

                                String id = task.getResult().getUser().getUid();
                                database.getReference().child("Users").child(id).setValue(userModel);
                                Toast.makeText(Register.this, "Your student account has been created", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(Register.this, MainActivity.class);
                                startActivity(i);
                                finish();
                            }else {
                                Toast.makeText(Register.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }

    }
}