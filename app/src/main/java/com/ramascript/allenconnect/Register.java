package com.ramascript.allenconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String userType = getIntent().getStringExtra("userType");

        if(userType.equals("Student")){
            findViewById(R.id.studentCV).setVisibility(View.VISIBLE);
            findViewById(R.id.professorCV).setVisibility(View.GONE);
            findViewById(R.id.alumniCV).setVisibility(View.GONE);
        } else if (userType.equals("Alumni")) {
            findViewById(R.id.alumniCV).setVisibility(View.VISIBLE);
            findViewById(R.id.professorCV).setVisibility(View.GONE);
            findViewById(R.id.studentCV).setVisibility(View.GONE);
        } else if (userType.equals("Professor")) {
            findViewById(R.id.professorCV).setVisibility(View.VISIBLE);
            findViewById(R.id.alumniCV).setVisibility(View.GONE);
            findViewById(R.id.studentCV).setVisibility(View.GONE);
        }else {
            findViewById(R.id.studentCV).setVisibility(View.GONE);
            findViewById(R.id.professorCV).setVisibility(View.GONE);
            findViewById(R.id.alumniCV).setVisibility(View.GONE);
        }



        AppCompatButton backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Register.this, RegisterAs.class);
                startActivity(i);
                finish();
            }
        });

    }
}