package com.ramascript.allenconnect.Chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityChatDetailBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private ArrayList<ChatMsgModel> messageList;
    private ChatMsgAdapter adapter;
    private String receiverId;
    private String senderRoom;
    private String receiverRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set window flags for proper keyboard handling
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        messageList = new ArrayList<>();
        adapter = new ChatMsgAdapter(messageList, this);

        // Get receiver details from intent
        receiverId = getIntent().getStringExtra("userId");
        String receiverName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePicture");

        senderRoom = auth.getUid() + receiverId;
        receiverRoom = receiverId + auth.getUid();

        // Set receiver details
        binding.userName.setText(receiverName);
        if (profilePic != null && !profilePic.isEmpty()) {
            Picasso.get()
                    .load(profilePic)
                    .placeholder(R.drawable.ic_avatar)
                    .into(binding.profileImage);
        }

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        binding.chatRecyclerView.setAdapter(adapter);
        binding.chatRecyclerView.setHasFixedSize(false);

        // Add scroll listener to handle new messages
        binding.chatRecyclerView
                .addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if (bottom < oldBottom) {
                        binding.chatRecyclerView.postDelayed(() -> {
                            if (messageList.size() > 0) {
                                binding.chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }, 100);
                    }
                });

        // Load messages
        database.getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            String uId = messageSnapshot.child("uId").getValue(String.class);
                            String message = messageSnapshot.child("message").getValue(String.class);
                            long timestamp = 0;
                            if (messageSnapshot.child("timestamp").getValue() != null) {
                                timestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                            }

                            if (message != null && uId != null) {
                                ChatMsgModel model = new ChatMsgModel(uId, message, timestamp);
                                messageList.add(model);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (messageList.size() > 0) {
                            binding.chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatDetailActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });

        binding.backBtnIV.setOnClickListener(v -> finish());

        binding.send.setOnClickListener(v -> {
            String messageText = binding.msgEt.getText().toString().trim();
            if (!messageText.isEmpty()) {
                long timestamp = new Date().getTime();

                HashMap<String, Object> messageObj = new HashMap<>();
                messageObj.put("uId", auth.getUid());
                messageObj.put("message", messageText);
                messageObj.put("timestamp", timestamp);

                String messageId = database.getReference().child("chats").child(senderRoom).push().getKey();

                if (messageId != null) {
                    // Add to sender's room
                    database.getReference()
                            .child("chats")
                            .child(senderRoom)
                            .child(messageId)
                            .setValue(messageObj)
                            .addOnSuccessListener(unused -> {
                                // Add to receiver's room
                                database.getReference()
                                        .child("chats")
                                        .child(receiverRoom)
                                        .child(messageId)
                                        .setValue(messageObj)
                                        .addOnSuccessListener(unused1 -> {
                                            binding.msgEt.setText("");
                                        });
                            });
                }
            }
        });

        binding.msgEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.send.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        monitorOnlineStatus();
    }

    private void monitorOnlineStatus() {
        database.getReference().child("Users").child(receiverId).child("online")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            boolean isOnline = snapshot.getValue(Boolean.class) != null &&
                                    snapshot.getValue(Boolean.class);
                            binding.onlineStatus.setVisibility(isOnline ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageList != null) {
            messageList.clear();
            messageList = null;
        }
        adapter = null;
        binding = null;
    }
}
