package com.ramascript.allenconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginAs extends AppCompatActivity {

    FirebaseUser currentUser;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_as);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout StudentLoginLL = findViewById(R.id.studentLoginLL);
        LinearLayout AlumniLoginLL = findViewById(R.id.alumniLoginLL);
        LinearLayout ProfessorLoginLL = findViewById(R.id.professorLoginLL);

        StudentLoginLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginAs.this, Login.class);
                i.putExtra("userType", "Student");
                startActivity(i);
            }
        });

        AlumniLoginLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginAs.this, Login.class);
                i.putExtra("userType", "Alumni");
                startActivity(i);
            }
        });

        ProfessorLoginLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginAs.this, Login.class);
                i.putExtra("userType", "Professor");
                startActivity(i);
            }
        });

        TextView dontHaveAcTV;
        dontHaveAcTV = findViewById(R.id.dontHaveAcTV);

        dontHaveAcTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iReg = new Intent(LoginAs.this, RegisterAs.class);
                startActivity(iReg);
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser != null){
            Intent i = new Intent(LoginAs.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }
}