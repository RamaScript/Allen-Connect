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

        LinearLayout tutorRegisterLL = findViewById(R.id.tutorRegisterLL);
        LinearLayout parentRegisterLL = findViewById(R.id.parentRegisterLL);
        LinearLayout adminRegisterLL = findViewById(R.id.adminRegisterLL);

        tutorRegisterLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterAs.this, Register.class);

                startActivity(i);
            }
        });

        parentRegisterLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterAs.this, Register.class);

                startActivity(i);
            }
        });

        adminRegisterLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterAs.this, Register.class);

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