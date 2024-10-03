package com.ramascript.allenconnect;

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

public class RegisterAs extends AppCompatActivity {

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
                Intent i = new Intent(RegisterAs.this, Register.class);
                i.putExtra("userType", "Student");
                startActivity(i);
            }
        });

        alumniRegisterLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterAs.this, Register.class);
                i.putExtra("userType", "Alumni");
                startActivity(i);
            }
        });

        professorRegisterLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterAs.this, Register.class);
                i.putExtra("userType", "Professor");
                startActivity(i);
            }
        });

        TextView alreadyHaveAcTV;
        alreadyHaveAcTV = findViewById(R.id.alreadyHaveAcTV);

        alreadyHaveAcTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iReg = new Intent(RegisterAs.this, LoginAs.class);
                startActivity(iReg);
                finish();
            }
        });
    }
}