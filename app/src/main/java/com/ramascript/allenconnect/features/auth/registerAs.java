package com.ramascript.allenconnect.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ramascript.allenconnect.R;

public class registerAs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_as);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout studentRegisterLL = findViewById(R.id.studentRegisterLL);
        LinearLayout alumniRegisterLL = findViewById(R.id.alumniRegisterLL);
        LinearLayout professorRegisterLL = findViewById(R.id.professorRegisterLL);

        studentRegisterLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(registerAs.this, register.class);
                i.putExtra("userType", "Student");
                startActivity(i);
                finish();
            }
        });

        alumniRegisterLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(registerAs.this, register.class);
                i.putExtra("userType", "Alumni");
                startActivity(i);
                finish();
            }
        });

        professorRegisterLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(registerAs.this, register.class);
                i.putExtra("userType", "Professor");
                startActivity(i);
                finish();
            }
        });

        TextView alreadyHaveAcTV;
        alreadyHaveAcTV = findViewById(R.id.alreadyHaveAcTV);

        alreadyHaveAcTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iReg = new Intent(registerAs.this, loginAs.class);
                startActivity(iReg);
                finish();
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(registerAs.this, loginAs.class);
        startActivity(intent);
        finish(); // Close the current activity
    }
}