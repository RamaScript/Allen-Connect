package com.ramascript.allenconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AllenBot extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        WebView webView;

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_allen_bot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView backBtn = findViewById(R.id.backBtnIV);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AllenBot.this, MainActivity.class);
                startActivity(i);
            }
        });

        webView = findViewById(R.id.webview);

        // Enable JavaScript in WebView
        webView.getSettings().setJavaScriptEnabled(true);

        // Load the specified URL
        webView.loadUrl("https://a4a0-2402-8100-2609-35c1-8c6c-d006-65da-8913.ngrok-free.app/");

        // Override WebViewClient to handle opening links within the WebView
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
    }
}