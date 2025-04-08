package com.ramascript.allenconnect.features.bot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ramascript.allenconnect.base.mainActivity;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityAllenBotBinding;
import com.ramascript.allenconnect.databinding.ActivityMainBinding;

public class allenBotActivity extends AppCompatActivity {

    ActivityAllenBotBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllenBotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up back button functionality
        binding.backButton.setOnClickListener(v -> {
            finish();
        });



    }
}