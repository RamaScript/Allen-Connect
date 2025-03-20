package com.ramascript.allenconnect.features.allenBot;

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

public class allenBotActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 123;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allen_bot);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up back button functionality
        ImageView backBtn = findViewById(R.id.backBtnIV);
        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(allenBotActivity.this, mainActivity.class);
            startActivity(i);
        });


        webView = findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true); // Enable local storage
        webView.getSettings().setAllowContentAccess(true); // Allow content access
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false); // Allow autoplay for media (TTS)

        webView.loadUrl("https://9020-2401-4900-5f79-f330-b953-6d9d-5e33-e4fc.ngrok-free.app/");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        // Handle media permissions (microphone/camera)
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(allenBotActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            request.grant(request.getResources());
                        } else {
                            ActivityCompat.requestPermissions(allenBotActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
                        }
                    } else {
                        request.grant(request.getResources());
                    }
                });
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}