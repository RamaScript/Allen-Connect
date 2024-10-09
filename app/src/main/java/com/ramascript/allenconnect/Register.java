package com.ramascript.allenconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ramascript.allenconnect.Models.StudentUserModel;
import com.ramascript.allenconnect.databinding.ActivityRegisterBinding;

public class Register extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;

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

        if (userType.equals("Student")){
            AppCompatButton registerBtn = findViewById(R.id.studentRegisterBtn);
            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = binding.studentEmailET.getText().toString();
                    String password = binding.studentPasswordET.getText().toString();
                    auth.createUserWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                StudentUserModel studentUserModel = new StudentUserModel( binding.crnET.getText().toString(),
                                        email, binding.studentPhoneNoET.getText().toString(),
                                        binding.studentUsernameET.getText().toString(), password);

                                String id = task.getResult().getUser().getUid();
                                database.getReference().child("Users").child("Students").child(id).setValue(studentUserModel);
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