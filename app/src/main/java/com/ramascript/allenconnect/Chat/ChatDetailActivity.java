package com.ramascript.allenconnect.Chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    private ValueEventListener messageListener;
    private ValueEventListener onlineStatusListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
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

            if (receiverId == null) {
                finish();
                return;
            }

            senderRoom = auth.getUid() + receiverId;
            receiverRoom = receiverId + auth.getUid();

            // Set receiver details
            if (binding != null) {
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
                        .addOnLayoutChangeListener(
                                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                                    if (bottom < oldBottom) {
                                        binding.chatRecyclerView.postDelayed(() -> {
                                            if (messageList != null && messageList.size() > 0 && binding != null
                                                    && !isFinishing()) {
                                                binding.chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                                            }
                                        }, 100);
                                    }
                                });

                binding.backBtnIV.setOnClickListener(v -> finish());

                binding.send.setOnClickListener(v -> {
                    sendMessage();
                });
            }

            // Setup listeners
            setupMessageListener();
            monitorOnlineStatus();
            markMessagesAsRead();
        } catch (Exception e) {
            Log.e("ChatDetailActivity", "Error in onCreate: " + e.getMessage());
            finish();
        }
    }

    private void sendMessage() {
        if (binding == null || isFinishing() || isDestroyed())
            return;

        String messageText = binding.msgEt.getText().toString().trim();
        if (!messageText.isEmpty()) {
            long timestamp = new Date().getTime();

            HashMap<String, Object> messageObj = new HashMap<>();
            messageObj.put("uId", auth.getUid());
            messageObj.put("message", messageText);
            messageObj.put("timestamp", timestamp);
            messageObj.put("read", false);

            String messageId = database.getReference().child("chats").child(senderRoom).push().getKey();

            if (messageId != null) {
                // Add to sender's room
                database.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child(messageId)
                        .setValue(messageObj)
                        .addOnSuccessListener(unused -> {
                            if (binding == null || isFinishing() || isDestroyed())
                                return;

                            // Add to receiver's room
                            database.getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child(messageId)
                                    .setValue(messageObj)
                                    .addOnSuccessListener(unused1 -> {
                                        if (binding != null && !isFinishing()) {
                                            binding.msgEt.setText("");
                                        }
                                    });
                        });
            }
        }
    }

    private void setupMessageListener() {
        if (messageListener != null) {
            return; // Already set up
        }

        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (messageList == null || binding == null || isFinishing() || isDestroyed())
                    return;

                try {
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

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    if (messageList.size() > 0 && binding.chatRecyclerView != null) {
                        binding.chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                    }

                    // Mark messages as read
                    markMessagesAsRead();
                } catch (Exception e) {
                    Log.e("ChatDetailActivity", "Error in message listener: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(ChatDetailActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (database != null && senderRoom != null) {
            database.getReference()
                    .child("chats")
                    .child(senderRoom)
                    .addValueEventListener(messageListener);
        }
    }

    private void monitorOnlineStatus() {
        if (onlineStatusListener != null || receiverId == null || database == null) {
            return; // Already set up or prerequisites not met
        }

        onlineStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null || isFinishing() || isDestroyed())
                    return;

                try {
                    if (snapshot.exists()) {
                        // Check online status
                        Boolean isOnline = snapshot.child("online").getValue(Boolean.class);

                        // If online, show the "Online Now" text with color_primary
                        if (isOnline != null && isOnline) {
                            binding.onlineStatus.setVisibility(View.VISIBLE);
                            binding.lastSeenText.setVisibility(View.GONE); // Hide lastSeenText when online
                        } else {
                            // User is offline, hide "Online Now" and show last seen time
                            binding.onlineStatus.setVisibility(View.GONE);

                            // Get last seen timestamp
                            Long lastSeen = snapshot.child("lastSeen").getValue(Long.class);
                            if (lastSeen != null) {
                                String lastSeenText = "Last seen "
                                        + com.ramascript.allenconnect.User.UserModel.getFormattedLastSeen(lastSeen);
                                binding.lastSeenText.setText(lastSeenText);
                                binding.lastSeenText.setVisibility(View.VISIBLE);
                            } else {
                                binding.lastSeenText.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        binding.onlineStatus.setVisibility(View.GONE);
                        binding.lastSeenText.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("ChatDetailActivity", "Error in online status: " + e.getMessage());
                    binding.onlineStatus.setVisibility(View.GONE);
                    binding.lastSeenText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("ChatDetailActivity", "Online status monitoring cancelled");
                binding.onlineStatus.setVisibility(View.GONE);
                binding.lastSeenText.setVisibility(View.GONE);
            }
        };

        database.getReference()
                .child("Users")
                .child(receiverId)
                .addValueEventListener(onlineStatusListener);
    }

    private void markMessagesAsRead() {
        if (receiverId == null || auth == null || auth.getUid() == null || isFinishing() || isDestroyed()
                || database == null)
            return;

        try {
            String senderRoom = auth.getUid() + receiverId;

            // Get all messages in the current chat room
            database.getReference().child("chats").child(senderRoom).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (isFinishing() || isDestroyed() || database == null)
                                return;

                            try {
                                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                    // Only mark messages from the other user
                                    String senderId = messageSnapshot.child("uId").getValue(String.class);
                                    Boolean isRead = messageSnapshot.child("read").getValue(Boolean.class);

                                    // If message is from receiver and not marked as read
                                    if (senderId != null && senderId.equals(receiverId)
                                            && (isRead == null || !isRead)) {
                                        // Mark as read in both rooms
                                        String messageId = messageSnapshot.getKey();
                                        if (messageId != null && database != null) {
                                            database.getReference().child("chats").child(senderRoom)
                                                    .child(messageId).child("read").setValue(true);
                                            database.getReference().child("chats").child(receiverRoom)
                                                    .child(messageId).child("read").setValue(true);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("ChatDetailActivity", "Error marking messages as read: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w("ChatDetailActivity", "Failed to mark messages as read: " + error.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e("ChatDetailActivity", "Error initiating mark messages as read: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove Firebase listeners to prevent callbacks after activity is destroyed
        if (senderRoom != null && database != null) {
            database.getReference().child("chats").child(senderRoom).removeEventListener(messageListener);
        }

        if (receiverId != null && database != null) {
            database.getReference().child("Users").child(receiverId).child("online")
                    .removeEventListener(onlineStatusListener);
        }

        // Clean up references
        if (messageList != null) {
            messageList.clear();
            messageList = null;
        }
        adapter = null;
        binding = null;
        database = null;
        auth = null;
    }
}
