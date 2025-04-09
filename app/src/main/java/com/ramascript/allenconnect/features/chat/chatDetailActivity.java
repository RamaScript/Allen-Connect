package com.ramascript.allenconnect.features.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.base.mainActivity;
import com.ramascript.allenconnect.databinding.ActivityChatDetailBinding;
import com.ramascript.allenconnect.features.user.userModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class chatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private ArrayList<chatMsgModel> messageList;
    private chatMsgAdapter adapter;
    private String receiverId;
    private String senderRoom;
    private String receiverRoom;
    private ValueEventListener messageListener;
    private ValueEventListener onlineStatusListener;
    private ValueEventListener incomingCallListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private long lastMessageReadTimestamp = 0;
    private boolean isActivityActive = false;
    private String receiverName;
    private String receiverProfilePic;

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
            adapter = new chatMsgAdapter(messageList, this);

            // Get receiver details from intent
            receiverId = getIntent().getStringExtra("userId");
            receiverName = getIntent().getStringExtra("userName");
            receiverProfilePic = getIntent().getStringExtra("profilePic");

            if (receiverId == null) {
                finish();
                return;
            }

            senderRoom = auth.getUid() + receiverId;
            receiverRoom = receiverId + auth.getUid();

            // Set receiver details
            if (binding != null) {
                binding.userName.setText(receiverName);
                if (receiverProfilePic != null && !receiverProfilePic.isEmpty()) {
                    Picasso.get()
                            .load(receiverProfilePic)
                            .placeholder(R.drawable.ic_avatar)
                            .into(binding.profileImage);
                }

                // Add click listener to navigate to user profile
                binding.chatUserLL.setOnClickListener(v -> {
                    if (receiverId != null) {
                        navigateToUserProfile(receiverId);
                    }
                });

                // Add video call button
                binding.videoCallBtn.setVisibility(View.VISIBLE);
                binding.videoCallBtn.setOnClickListener(v -> initiateVideoCall());

                // Add click listener for menu button
                binding.menuBtn.setOnClickListener(v -> showPopupMenu());

                // Check if user is deleted immediately
                database.getReference()
                        .child("Users")
                        .child(receiverId)
                        .child("isDeleted")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (binding == null || isFinishing() || isDestroyed())
                                    return;

                                if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                                    handleDeletedAccountUI();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Continue with normal flow if error occurs
                            }
                        });

                // Setup RecyclerView with optimized settings
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setStackFromEnd(true);
                binding.chatRecyclerView.setLayoutManager(layoutManager);
                binding.chatRecyclerView.setAdapter(adapter);
                binding.chatRecyclerView.setHasFixedSize(true);
                binding.chatRecyclerView.setItemViewCacheSize(20);

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
            listenForIncomingCalls();
        } catch (Exception e) {
            Log.e("chatDetailActivity", "Error in onCreate: " + e.getMessage());
            finish();
        }
    }

    private void initiateVideoCall() {
        // Check if user is online first
        database.getReference()
                .child("Users")
                .child(receiverId)
                .child("online")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                            // User is online, start video call
                            startVideoCallActivity(true);
                        } else {
                            // User is offline, show message
                            Toast.makeText(chatDetailActivity.this,
                                    "User is offline. Try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(chatDetailActivity.this,
                                "Could not check user status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startVideoCallActivity(boolean isInitiator) {
        Intent intent = new Intent(chatDetailActivity.this, VideoCallActivity.class);
        intent.putExtra("userId", receiverId);
        intent.putExtra("userName", receiverName);
        intent.putExtra("profilePic", receiverProfilePic);
        intent.putExtra("isCallInitiator", isInitiator);
        startActivity(intent);
    }

    private void listenForIncomingCalls() {
        // Listen for incoming call requests
        DatabaseReference callRef = database.getReference()
                .child("calls")
                .child(auth.getUid() + "_" + receiverId);

        incomingCallListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && "offer".equals(snapshot.child("type").getValue(String.class))) {
                    // We've received a call from the current chat partner
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);
                    long currentTime = new Date().getTime();

                    // Only handle calls that are recent (within last 30 seconds)
                    if ((currentTime - timestamp) < 30000) {
                        startVideoCallActivity(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("chatDetailActivity", "Error listening for calls: " + error.getMessage());
            }
        };

        callRef.addValueEventListener(incomingCallListener);
    }

    private void navigateToUserProfile(String userId) {
        try {
            // Create a completely new approach with a dedicated flag
            Intent intent = new Intent(chatDetailActivity.this, mainActivity.class);

            // Use NEW_TASK flag to create a new task and CLEAR_TASK to clear existing tasks
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Add special flags to indicate direct profile loading
            intent.putExtra("directProfileLoad", true);
            intent.putExtra("openFragment", "profileFragment");
            intent.putExtra("userId", userId);

            Log.d("chatDetailActivity", "Opening profile with direct loading for user ID: " + userId);
            startActivity(intent);
            finish(); // Finish this activity
        } catch (Exception e) {
            Log.e("chatDetailActivity", "Error opening profile: " + e.getMessage());
            Toast.makeText(this, "Could not open profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage() {
        if (binding == null || isFinishing() || isDestroyed())
            return;

        String messageText = binding.msgEt.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Clear input immediately for better UX
            binding.msgEt.setText("");

            backgroundExecutor.execute(() -> {
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
                                // Add to receiver's room
                                database.getReference()
                                        .child("chats")
                                        .child(receiverRoom)
                                        .child(messageId)
                                        .setValue(messageObj);
                            });
                }
            });
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
                    backgroundExecutor.execute(() -> {
                        ArrayList<chatMsgModel> tempList = new ArrayList<>();
                        boolean hasUnreadMessages = false;

                        // Process all messages
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            String uId = messageSnapshot.child("uId").getValue(String.class);
                            String message = messageSnapshot.child("message").getValue(String.class);
                            long timestamp = 0;
                            if (messageSnapshot.child("timestamp").getValue() != null) {
                                timestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                            }
                            Boolean isRead = messageSnapshot.child("read").getValue(Boolean.class);

                            // If this is a message from the other user and not read yet
                            if (uId != null && !uId.equals(auth.getUid()) && (isRead == null || !isRead)) {
                                hasUnreadMessages = true;

                                // If activity is active, mark as read immediately in the database
                                if (isActivityActive) {
                                    messageSnapshot.getRef().child("read").setValue(true);
                                    isRead = true;
                                }
                            }

                            if (message != null && uId != null) {
                                chatMsgModel model = new chatMsgModel(uId, message, timestamp,
                                        isRead != null ? isRead : false);
                                tempList.add(model);
                            }
                        }

                        // If we found unread messages, trigger the markMessagesAsRead method
                        // to ensure unread counts are updated everywhere
                        final boolean needToMarkAsRead = hasUnreadMessages && isActivityActive;

                        mainHandler.post(() -> {
                            messageList.clear();
                            messageList.addAll(tempList);
                            adapter.notifyDataSetChanged();

                            if (messageList.size() > 0 && binding.chatRecyclerView != null) {
                                binding.chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                            }

                            // Force sync with the lastReadTime update if needed
                            if (needToMarkAsRead) {
                                markMessagesAsRead();
                            }
                        });
                    });
                } catch (Exception e) {
                    Log.e("chatDetailActivity", "Error in message listener: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(chatDetailActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
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
        if (binding == null || receiverId == null)
            return;

        onlineStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (binding == null || isFinishing() || isDestroyed())
                        return;

                    if (snapshot.exists()) {
                        Boolean online = snapshot.child("online").getValue(Boolean.class);
                        if (online != null && online) {
                            // User is online
                            binding.onlineStatus.setText("Online now");
                            binding.onlineStatus.setVisibility(View.VISIBLE);
                        } else {
                            // User is offline, hide "Online Now"
                            binding.onlineStatus.setVisibility(View.GONE);

                            // Get last seen timestamp
                            Long lastSeen = snapshot.child("lastSeen").getValue(Long.class);
                            if (lastSeen != null) {
                                String lastSeenText = "Last seen "
                                        + userModel.getFormattedLastSeen(lastSeen);
                                // Update onlineStatus to show last seen instead
                                binding.onlineStatus.setText(lastSeenText);
                                binding.onlineStatus.setVisibility(View.VISIBLE);
                            } else {
                                binding.onlineStatus.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        binding.onlineStatus.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("chatDetailActivity", "Error in online status: " + e.getMessage());
                    binding.onlineStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("chatDetailActivity", "Online status monitoring cancelled");
                binding.onlineStatus.setVisibility(View.GONE);
            }
        };

        database.getReference()
                .child("Users")
                .child(receiverId)
                .addValueEventListener(onlineStatusListener);
    }

    private void handleDeletedAccountUI() {
        if (binding == null || isFinishing() || isDestroyed())
            return;

        // Hide online status
        binding.onlineStatus.setVisibility(View.GONE);

        // Hide call buttons for deleted users
        binding.videoCallBtn.setVisibility(View.GONE);
        binding.audioCallBtn.setVisibility(View.GONE);

        // Hide message input field and send button
        binding.linear.setVisibility(View.GONE);

        // Show deleted account message
        binding.userDeletedMsg.setVisibility(View.VISIBLE);
        binding.userDeletedMsg.setText("This account has been deleted. You cannot send messages to it.");
    }

    private void markMessagesAsRead() {
        if (receiverId == null || auth == null || auth.getUid() == null || isFinishing() || isDestroyed()
                || database == null) {
            return;
        }

        try {
            Log.d("chatDetailActivity", "Starting to mark messages as read");

            // Reference to chat rooms
            String myId = auth.getUid();

            // We need to check both possible room structures
            String room1 = myId + receiverId; // senderRoom format
            String room2 = receiverId + myId; // receiverRoom format

            // First check room1
            markMessagesAsReadInRoom(room1);

            // Then check room2
            markMessagesAsReadInRoom(room2);

            // Force immediate update notification to all listeners
            DatabaseReference ref = database.getReference().child("chats").child(room2);
            HashMap<String, Object> forceUpdate = new HashMap<>();
            forceUpdate.put("lastReadTime", System.currentTimeMillis());
            ref.updateChildren(forceUpdate).addOnCompleteListener(task -> {
                Log.d("chatDetailActivity", "Forced update to trigger chat listeners");
            });

        } catch (Exception e) {
            Log.e("chatDetailActivity", "Error marking messages as read: " + e.getMessage(), e);
        }
    }

    private void markMessagesAsReadInRoom(String roomId) {
        // Get the chat room
        database.getReference().child("chats").child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            // Skip if room doesn't exist
                            if (!snapshot.exists()) {
                                return;
                            }

                            Log.d("chatDetailActivity", "Checking room: " + roomId);

                            // Determine if we have a messages subnode or direct messages
                            DataSnapshot messagesNode;
                            if (snapshot.hasChild("messages")) {
                                messagesNode = snapshot.child("messages");
                                Log.d("chatDetailActivity",
                                        "Found messages subnode with " + messagesNode.getChildrenCount() + " messages");
                            } else {
                                // Just use the direct children
                                messagesNode = snapshot;
                                Log.d("chatDetailActivity",
                                        "Using direct room with " + messagesNode.getChildrenCount() + " children");
                            }

                            // Count how many messages we mark as read
                            int markedCount = 0;

                            // Process all messages
                            for (DataSnapshot messageSnapshot : messagesNode.getChildren()) {
                                String senderId = null;

                                // Check various possible sender ID fields
                                if (messageSnapshot.hasChild("uId")) {
                                    senderId = messageSnapshot.child("uId").getValue(String.class);
                                } else if (messageSnapshot.hasChild("senderId")) {
                                    senderId = messageSnapshot.child("senderId").getValue(String.class);
                                } else if (messageSnapshot.hasChild("from")) {
                                    senderId = messageSnapshot.child("from").getValue(String.class);
                                }

                                // Skip messages from the current user
                                if (senderId == null || senderId.equals(auth.getUid())) {
                                    continue;
                                }

                                // Check read status in various fields
                                Boolean isRead = null;
                                if (messageSnapshot.hasChild("read")) {
                                    isRead = messageSnapshot.child("read").getValue(Boolean.class);
                                } else if (messageSnapshot.hasChild("seen")) {
                                    isRead = messageSnapshot.child("seen").getValue(Boolean.class);
                                } else if (messageSnapshot.hasChild("isRead")) {
                                    isRead = messageSnapshot.child("isRead").getValue(Boolean.class);
                                }

                                // Mark unread messages as read
                                if (isRead == null || !isRead) {
                                    markedCount++;
                                    if (messageSnapshot.hasChild("read")) {
                                        messageSnapshot.getRef().child("read").setValue(true);
                                    } else if (messageSnapshot.hasChild("seen")) {
                                        messageSnapshot.getRef().child("seen").setValue(true);
                                    } else if (messageSnapshot.hasChild("isRead")) {
                                        messageSnapshot.getRef().child("isRead").setValue(true);
                                    } else {
                                        // If no read field exists, add it
                                        messageSnapshot.getRef().child("read").setValue(true);
                                    }
                                }
                            }

                            Log.d("chatDetailActivity",
                                    "Marked " + markedCount + " messages as read in room " + roomId);
                        } catch (Exception e) {
                            Log.e("chatDetailActivity", "Error processing room: " + e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("chatDetailActivity", "Database error while marking messages: " + error.getMessage());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        markMessagesAsRead();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            database.getReference().child("chats").child(senderRoom).removeEventListener(messageListener);
        }
        if (onlineStatusListener != null) {
            database.getReference().child("Users").child(receiverId).removeEventListener(onlineStatusListener);
        }
        if (incomingCallListener != null) {
            database.getReference().child("calls").child(auth.getUid() + "_" + receiverId)
                    .removeEventListener(incomingCallListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_call_history) {
            navigateToCallHistory();
            return true;
        } else if (id == R.id.menu_profile) {
            if (receiverId != null) {
                navigateToUserProfile(receiverId);
            }
            return true;
        } else if (id == R.id.menu_clear_chat) {
            clearChat();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToCallHistory() {
        Intent intent = new Intent(chatDetailActivity.this, CallHistoryActivity.class);
        intent.putExtra("userId", receiverId); // Optional - to filter calls with this user
        startActivity(intent);
    }

    private void clearChat() {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to clear all messages?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear messages from database
                    if (senderRoom != null) {
                        database.getReference().child("chats").child(senderRoom).child("messages").removeValue()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(chatDetailActivity.this, "Chat cleared", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, binding.menuBtn);
        popupMenu.getMenuInflater().inflate(R.menu.chat_detail_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_call_history) {
                navigateToCallHistory();
                return true;
            } else if (id == R.id.menu_profile) {
                if (receiverId != null) {
                    navigateToUserProfile(receiverId);
                }
                return true;
            } else if (id == R.id.menu_clear_chat) {
                clearChat();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }
}
