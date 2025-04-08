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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityAllenBotBinding;

import android.content.SharedPreferences;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import android.util.Log;

public class allenBotActivity extends AppCompatActivity {

    private ActivityAllenBotBinding binding;
    private BotChatAdapter chatAdapter;
    private SharedPreferences userPrefs;
    private DatabaseReference chatDatabase;
    private String currentUserId;
    private boolean isLoadingMessages = false;
    private BotGeminiService geminiService;

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

        // Initialize SharedPreferences
        userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Get current user ID
        currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Please log in to use chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        chatDatabase = FirebaseDatabase.getInstance().getReference().child("bot_chats").child(currentUserId);

        // Initialize Gemini service
        geminiService = new BotGeminiService(this);

        // Set up RecyclerView
        chatAdapter = new BotChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // To display messages from bottom
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        // Set up back button
        binding.backButton.setOnClickListener(v -> finish());

        // Check if user is professor and show/hide upload button
        String userType = userPrefs.getString("userType", "");
        binding.uploadBtn.setVisibility(userType.equals("Professor") ? View.VISIBLE : View.GONE);

        // Set up send button
        binding.sendButton.setOnClickListener(v -> {
            String message = binding.messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });

        // Set up upload button (for professors only)
        binding.uploadBtn.setOnClickListener(v -> {
            openUploadActivity();
        });

        // Load messages
        loadChatMessages();
    }

    private String getCurrentUserId() {
        // Try to get the current user from Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }

        // Try to get from SharedPreferences
        String userId = userPrefs.getString("userId", null);

        if (userId != null && !userId.equals("unknown")) {
            return userId;
        }

        return null;
    }

    private void loadChatMessages() {
        isLoadingMessages = true;
        binding.progressBar.setVisibility(View.VISIBLE);

        chatDatabase.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BotChatMessage> messages = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BotChatMessage message = snapshot.getValue(BotChatMessage.class);
                    if (message != null) {
                        message.setMessageId(snapshot.getKey());
                        messages.add(message);
                    }
                }

                // Sort messages by timestamp
                Collections.sort(messages, Comparator.comparingLong(BotChatMessage::getTimestamp));

                // Update adapter with loaded messages
                chatAdapter.setMessages(messages);

                // Scroll to bottom if we're not loading initial messages
                if (!isLoadingMessages && !messages.isEmpty()) {
                    binding.chatRecyclerView.postDelayed(
                            () -> binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1), 200);
                } else if (!messages.isEmpty()) {
                    binding.chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                }

                isLoadingMessages = false;
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(allenBotActivity.this,
                        "Error loading messages: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                isLoadingMessages = false;
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void sendMessage(String text) {
        // Disable input field and show loading indicator to prevent multiple sends
        binding.messageInput.setEnabled(false);
        binding.sendButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Create user message
        BotChatMessage userMessage = new BotChatMessage(text, true);
        userMessage.setUserId(currentUserId);

        // Save user message to Firebase
        String userMessageId = UUID.randomUUID().toString();
        chatDatabase.child(userMessageId).setValue(userMessage)
                .addOnSuccessListener(aVoid -> {
                    // Clear input
                    binding.messageInput.setText("");

                    // Generate response using Gemini AI
                    geminiService.getResponseWithDocumentContext(currentUserId, text,
                            new BotGeminiService.ResponseCallback() {
                                @Override
                                public void onResponseReceived(String response) {
                                    // Create bot response
                                    BotChatMessage botResponse = new BotChatMessage(response, false);
                                    botResponse.setUserId("bot");
                                    botResponse.setTimestamp(System.currentTimeMillis());

                                    // Save bot response to Firebase
                                    String botMessageId = UUID.randomUUID().toString();
                                    chatDatabase.child(botMessageId).setValue(botResponse)
                                            .addOnSuccessListener(aVoid2 -> {
                                                // Re-enable input field
                                                runOnUiThread(() -> {
                                                    binding.messageInput.setEnabled(true);
                                                    binding.sendButton.setEnabled(true);
                                                    binding.progressBar.setVisibility(View.GONE);
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(allenBotActivity.this,
                                                        "Failed to save bot response",
                                                        Toast.LENGTH_SHORT).show();

                                                binding.messageInput.setEnabled(true);
                                                binding.sendButton.setEnabled(true);
                                                binding.progressBar.setVisibility(View.GONE);
                                            });
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    // Handle error
                                    BotChatMessage errorResponse = new BotChatMessage(
                                            "Sorry, I'm having trouble responding right now.", false);
                                    errorResponse.setUserId("bot");
                                    errorResponse.setTimestamp(System.currentTimeMillis());

                                    // Save error response to Firebase
                                    String botMessageId = UUID.randomUUID().toString();
                                    chatDatabase.child(botMessageId).setValue(errorResponse);

                                    // Log the error to logcat but don't show it to the user
                                    Log.e("BotChat", "API Error: " + errorMessage);

                                    // If there's a 404 error with the model, try listing available models
                                    if (errorMessage.contains("404") && errorMessage.contains("models")) {
                                        geminiService.listAvailableModels(new BotGeminiService.ResponseCallback() {
                                            @Override
                                            public void onResponseReceived(String response) {
                                                // Log the available models but don't show to user
                                                Log.d("BotChat", "Available models: " + response);
                                            }

                                            @Override
                                            public void onError(String listError) {
                                                Log.e("BotChat", "Failed to list models: " + listError);
                                            }
                                        });
                                    }

                                    // Re-enable input field
                                    binding.messageInput.setEnabled(true);
                                    binding.sendButton.setEnabled(true);
                                    binding.progressBar.setVisibility(View.GONE);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(allenBotActivity.this,
                            "Failed to send message",
                            Toast.LENGTH_SHORT).show();

                    binding.messageInput.setEnabled(true);
                    binding.sendButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                });
    }

    private void openUploadActivity() {
        Intent intent = new Intent(this, BotUploadActivity.class);
        startActivity(intent);
    }
}