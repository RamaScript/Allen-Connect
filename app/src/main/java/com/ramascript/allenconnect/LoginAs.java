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

public class LoginAs extends AppCompatActivity {

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

        LinearLayout tutorLoginLL = findViewById(R.id.tutorLoginLL);
        LinearLayout parentLoginLL = findViewById(R.id.parentLoginLL);
        LinearLayout adminLoginLL = findViewById(R.id.adminLoginLL);

        tutorLoginLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginAs.this, Login.class);

                startActivity(i);
            }
        });

        parentLoginLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginAs.this, Login.class);
                
                startActivity(i);
            }
        });

        adminLoginLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginAs.this, Login.class);

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
    }
}