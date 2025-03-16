package com.ramascript.allenconnect.About;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ramascript.allenconnect.MainActivity;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityMeetDevsBinding;

public class MeetDevsActivity extends AppCompatActivity {

    ActivityMeetDevsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMeetDevsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetDevsActivity.this, MainActivity.class);
                intent.putExtra("openFragment", "ProfileFragment"); // Adding extra info to specify fragment
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        binding.portfolioRamaIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetDevsActivity.this, WebRamaPortfolioActivity.class);
                intent.putExtra("PortfolioLink", "https://ramascript.github.io");
                startActivity(intent);
            }
        });

        binding.portfolioYashIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetDevsActivity.this, WebRamaPortfolioActivity.class);
                intent.putExtra("PortfolioLink", "https://yashraj63929.github.io/portfolio/");
                startActivity(intent);
            }
        });

        binding.portfolioPriyanshiIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetDevsActivity.this, WebRamaPortfolioActivity.class);
                intent.putExtra("PortfolioLink", "https://shadowpriyanshi.github.io/");
                startActivity(intent);
            }
        });
    }
}