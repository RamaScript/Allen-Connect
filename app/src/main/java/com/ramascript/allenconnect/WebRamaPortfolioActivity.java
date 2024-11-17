package com.ramascript.allenconnect;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ramascript.allenconnect.databinding.ActivityWebRamaPortfolioBinding;

public class WebRamaPortfolioActivity extends AppCompatActivity {

    ActivityWebRamaPortfolioBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWebRamaPortfolioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //get portfolio link from Meet Devs Activity Intent
        String portfolioLink = getIntent().getStringExtra("PortfolioLink");

        // Set up the WebView
        WebView webView = binding.webview;
        webView.setWebViewClient(new WebViewClient()); // Keeps navigation within the WebView
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // Update progress bar visibility
                if (newProgress == 100) {
                    // Page fully loaded, hide the progress bar
                    binding.progressBar.setVisibility(View.GONE);
                    binding.linear.setVisibility(View.GONE);
                } else {
                    // Page is loading, show the progress bar
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript if needed

        // Load a URL in the WebView
        webView.setWebViewClient(new WebViewClient()); // Keeps navigation within the WebView
        webView.loadUrl(portfolioLink);
    }
}