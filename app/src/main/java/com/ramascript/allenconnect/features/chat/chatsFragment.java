package com.ramascript.allenconnect.features.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.databinding.FragmentChatsBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class chatsFragment extends Fragment implements chatUsersAdapter.FilterCallback {

    private FragmentChatsBinding binding;
    private ArrayList<userModel> usersList;
    private ArrayList<userModel> filteredList;
    private ArrayList<userModel> onlineUsersList;
    private FirebaseDatabase database;
    private chatUsersAdapter adapter;
    private onlineUsersAdapter onlineUsersAdapter;
    private FirebaseAuth auth;
    private Map<String, Long> lastMessageTimes;
    private Map<String, String> lastMessages;
    private Map<String, Integer> unreadCounts;
    private static final String TAG = "chatsFragment";

    // Firebase listeners
    private ValueEventListener usersListener;
    private ValueEventListener onlineUsersListener;
    private ValueEventListener messagesListener;

    // Add this field to track if first load is complete
    private boolean isInitialLoadComplete = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        usersList = new ArrayList<>();
        filteredList = new ArrayList<>();
        onlineUsersList = new ArrayList<>();
        lastMessageTimes = new HashMap<>();
        lastMessages = new HashMap<>();
        unreadCounts = new HashMap<>();

        // Initialize adapters
        adapter = new chatUsersAdapter(filteredList, getContext(), this);
        binding.chatRecyclerView.setAdapter(adapter);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up online users recycler view
        onlineUsersAdapter = new onlineUsersAdapter(onlineUsersList, getContext());
        binding.onlineUsersRecyclerView.setAdapter(onlineUsersAdapter);
        binding.onlineUsersRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        // Show loading state initially
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.chatRecyclerView.setVisibility(View.VISIBLE);

        // Show a progress bar if we have one
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        // Initialize the users listener (but don't attach it yet)
        loadUsers();

        // First fetch message data to know which users have conversations
        // The fetchLastMessages method will then trigger the usersListener
        fetchLastMessages();

        // Monitor online users
        monitorOnlineUsers();

        return binding.getRoot();
    }

    private void loadUsers() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId != null) {
            usersListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding == null || !isAdded()) {
                        Log.d(TAG, "Binding is null or fragment not added in loadUsers callback");
                        return;
                    }

                    try {
                        usersList.clear();
                        filteredList.clear();

                        // Log for debugging
                        Log.d(TAG, "Loading users, we have " + lastMessages.size() + " conversations");
                        if (lastMessages.size() > 0) {
                            Log.d(TAG, "Conversation user IDs: " + lastMessages.keySet());
                        }

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            userModel user = dataSnapshot.getValue(userModel.class);
                            String userId = dataSnapshot.getKey();

                            if (user != null && userId != null && !userId.equals(currentUserId)) {
                                user.setID(userId);

                                // Add to full user list regardless
                                usersList.add(user);

                                // Check if this user has conversations
                                boolean hasConversation = lastMessages.containsKey(userId);

                                // Only add to filtered list if we have message history
                                if (hasConversation) {
                                    // Set last message data
                                    user.setLastMsg(lastMessages.get(userId));

                                    // Set last message time
                                    if (lastMessageTimes.containsKey(userId)) {
                                        user.setLastMessageTime(lastMessageTimes.get(userId));
                                    }

                                    // Set unread count - only if there are unread messages
                                    if (unreadCounts.containsKey(userId) && unreadCounts.get(userId) > 0) {
                                        user.setUnreadCount(unreadCounts.get(userId));
                                        Log.d(TAG, "User " + user.getName() + " has " + unreadCounts.get(userId)
                                                + " unread messages");
                                    } else {
                                        // Explicitly set to zero to clear any previous unread count
                                        user.setUnreadCount(0);
                                    }

                                    // Add to filtered list that will be displayed
                                    filteredList.add(user);

                                    Log.d(TAG, "Added user to chat list: " + user.getName());
                                }
                            }
                        }

                        // Log the final result
                        Log.d(TAG, "Final filtered list size: " + filteredList.size());

                        // Sort users by last message time (most recent first)
                        Collections.sort(filteredList, (u1, u2) -> {
                            Long time1 = u1.getLastMessageTime();
                            Long time2 = u2.getLastMessageTime();

                            if (time1 == null && time2 == null)
                                return 0;
                            if (time1 == null)
                                return 1;
                            if (time2 == null)
                                return -1;

                            return time2.compareTo(time1);
                        });

                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        updateEmptyState();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in loadUsers: " + e.getMessage(), e);
                        updateEmptyState();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                    if (binding != null && isAdded()) {
                        updateEmptyState();
                    }
                }
            };
        } else {
            updateEmptyState();
        }
    }

    private void monitorOnlineUsers() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId == null)
            return;

        onlineUsersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Early return if binding is null to prevent NPE
                if (binding == null || !isAdded()) {
                    Log.d(TAG, "Binding is null in monitorOnlineUsers callback, skipping UI update");
                    return;
                }

                try {
                    onlineUsersList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String userId = dataSnapshot.getKey();
                        if (userId != null && !userId.equals(currentUserId)) {
                            // Check if user is deleted
                            Boolean isDeleted = dataSnapshot.child("isDeleted").getValue(Boolean.class);
                            if (isDeleted != null && isDeleted) {
                                // Skip deleted users
                                continue;
                            }

                            // Check if user is online
                            Boolean isOnline = dataSnapshot.child("online").getValue(Boolean.class);
                            if (isOnline != null && isOnline) {
                                userModel user = dataSnapshot.getValue(userModel.class);
                                if (user != null) {
                                    user.setID(userId);
                                    onlineUsersList.add(user);
                                }
                            }
                        }
                    }

                    // Update online users recycler view visibility
                    if (onlineUsersList.size() > 0 && onlineUsersAdapter != null) {
                        binding.onlineUsersRecyclerView.setVisibility(View.VISIBLE);
                        onlineUsersAdapter.notifyDataSetChanged();
                    } else {
                        binding.onlineUsersRecyclerView.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in monitorOnlineUsers: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to monitor online users: " + error.getMessage());
            }
        };

        database.getReference().child("Users").addValueEventListener(onlineUsersListener);
    }

    private void fetchLastMessages() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId == null)
            return;

        // Add debug log
        Log.d(TAG, "Fetching messages for user: " + currentUserId);

        // Reference to the chats node
        DatabaseReference chatsRef = database.getReference().child("chats");

        // Get a list of all chat rooms for the current user
        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) {
                    Log.d(TAG, "Fragment not added in fetchLastMessages callback");
                    return;
                }

                try {
                    // Clear existing message data
                    lastMessages.clear();
                    lastMessageTimes.clear();
                    unreadCounts.clear();

                    // Log how many chat rooms we found
                    Log.d(TAG, "Found " + snapshot.getChildrenCount() + " total chat rooms");

                    // Process all chat rooms
                    for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                        String roomId = roomSnapshot.getKey();
                        if (roomId == null)
                            continue;

                        Log.d(TAG, "Examining room ID: " + roomId);

                        // We need to determine if this room belongs to the current user
                        // Chat room IDs might be in format: senderId + receiverId
                        String otherUserId = null;

                        // Try to extract the other user ID - there are multiple patterns possible
                        if (roomId.equals(currentUserId)) {
                            // Skip rooms that are just the current user ID (shouldn't happen)
                            continue;
                        } else if (roomId.startsWith(currentUserId)) {
                            // Format: currentUserId + otherUserId
                            otherUserId = roomId.substring(currentUserId.length());
                            Log.d(TAG, "Room starts with current user ID, other user: " + otherUserId);
                        } else if (roomId.endsWith(currentUserId)) {
                            // Format: otherUserId + currentUserId
                            otherUserId = roomId.substring(0, roomId.length() - currentUserId.length());
                            Log.d(TAG, "Room ends with current user ID, other user: " + otherUserId);
                        } else if (roomId.contains("_")) {
                            // Format might be like "uid1_uid2"
                            String[] parts = roomId.split("_");
                            if (parts.length == 2) {
                                if (parts[0].equals(currentUserId)) {
                                    otherUserId = parts[1];
                                    Log.d(TAG, "Room has underscore format, other user: " + otherUserId);
                                } else if (parts[1].equals(currentUserId)) {
                                    otherUserId = parts[0];
                                    Log.d(TAG, "Room has underscore format, other user: " + otherUserId);
                                }
                            }
                        }

                        // If we couldn't determine the other user ID, log and skip
                        if (otherUserId == null || otherUserId.isEmpty()) {
                            Log.d(TAG, "Couldn't determine other user ID for room: " + roomId);
                            continue;
                        }

                        // Check if this room has actual messages
                        if (!roomSnapshot.hasChild("messages")) {
                            // Try alternative paths - it might not be using a "messages" subnode
                            DataSnapshot messagesNode = roomSnapshot;

                            // Find and process the latest message
                            processMessagesInRoom(messagesNode, otherUserId, currentUserId);
                        } else {
                            // There is a "messages" subnode, use it
                            DataSnapshot messagesNode = roomSnapshot.child("messages");

                            // Find and process the latest message
                            processMessagesInRoom(messagesNode, otherUserId, currentUserId);
                        }
                    }

                    Log.d(TAG, "Processed messages, found " + lastMessages.size() + " conversations");

                    // If we're doing a quick refresh (not initial load), update the current users
                    if (isInitialLoadComplete && usersList != null && usersList.size() > 0 && filteredList != null) {
                        Log.d(TAG, "Updating existing user list with new unread counts");

                        // Update unread counts on existing filtered list
                        for (userModel user : filteredList) {
                            String userId = user.getID();
                            if (userId != null) {
                                // Update last message
                                if (lastMessages.containsKey(userId)) {
                                    user.setLastMsg(lastMessages.get(userId));
                                }

                                // Update timestamp
                                if (lastMessageTimes.containsKey(userId)) {
                                    user.setLastMessageTime(lastMessageTimes.get(userId));
                                }

                                // Update unread count
                                if (unreadCounts.containsKey(userId) && unreadCounts.get(userId) > 0) {
                                    user.setUnreadCount(unreadCounts.get(userId));
                                    Log.d(TAG, "Updated unread count for " + user.getName() + ": "
                                            + unreadCounts.get(userId));
                                } else {
                                    // Clear unread count
                                    user.setUnreadCount(0);
                                }
                            }
                        }

                        // Sort by latest message time
                        Collections.sort(filteredList, (u1, u2) -> {
                            Long time1 = u1.getLastMessageTime();
                            Long time2 = u2.getLastMessageTime();

                            if (time1 == null && time2 == null)
                                return 0;
                            if (time1 == null)
                                return 1;
                            if (time2 == null)
                                return -1;

                            return time2.compareTo(time1);
                        });

                        // Update UI
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }

                        // Hide loading indicator
                        if (binding != null && binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }

                        updateEmptyState();
                    } else {
                        // Now load users data after we have the message data
                        database.getReference().child("Users").addValueEventListener(usersListener);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in fetchLastMessages: " + e.getMessage(), e);
                    // Hide loading indicator
                    if (binding != null && binding.progressBar != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                    // Try to load users even if there was an error
                    database.getReference().child("Users").addValueEventListener(usersListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch messages: " + error.getMessage());
                // Hide loading indicator
                if (binding != null && binding.progressBar != null) {
                    binding.progressBar.setVisibility(View.GONE);
                }
                // Try to load users even if there was an error
                database.getReference().child("Users").addValueEventListener(usersListener);
            }
        };

        chatsRef.addValueEventListener(messagesListener);
    }

    // Helper method to process messages in a chat room
    private void processMessagesInRoom(DataSnapshot messagesNode, String otherUserId, String currentUserId) {
        if (!messagesNode.exists() || !messagesNode.hasChildren()) {
            return;
        }

        String latestMessage = null;
        long latestTimestamp = 0;
        int unreadCount = 0;

        // Process all messages to find the latest one
        for (DataSnapshot messageSnapshot : messagesNode.getChildren()) {
            // Try multiple possible fields for the message content
            String message = null;
            if (messageSnapshot.hasChild("message")) {
                message = messageSnapshot.child("message").getValue(String.class);
            } else if (messageSnapshot.hasChild("text")) {
                message = messageSnapshot.child("text").getValue(String.class);
            } else if (messageSnapshot.hasChild("content")) {
                message = messageSnapshot.child("content").getValue(String.class);
            }

            // Try multiple possible fields for the timestamp
            Long timestamp = null;
            if (messageSnapshot.hasChild("timestamp")) {
                Object timestampObj = messageSnapshot.child("timestamp").getValue();
                if (timestampObj != null) {
                    try {
                        timestamp = Long.parseLong(timestampObj.toString());
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing timestamp: " + e.getMessage());
                    }
                }
            } else if (messageSnapshot.hasChild("time")) {
                Object timestampObj = messageSnapshot.child("time").getValue();
                if (timestampObj != null) {
                    try {
                        timestamp = Long.parseLong(timestampObj.toString());
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing timestamp: " + e.getMessage());
                    }
                }
            }

            // Try multiple possible fields for the sender ID
            String senderId = null;
            if (messageSnapshot.hasChild("uId")) {
                senderId = messageSnapshot.child("uId").getValue(String.class);
            } else if (messageSnapshot.hasChild("senderId")) {
                senderId = messageSnapshot.child("senderId").getValue(String.class);
            } else if (messageSnapshot.hasChild("from")) {
                senderId = messageSnapshot.child("from").getValue(String.class);
            }

            // Skip messages sent by current user for unread count
            if (senderId != null && senderId.equals(currentUserId)) {
                // This is a message sent BY the current user (not to count as unread)
                if (message != null && timestamp != null && timestamp > latestTimestamp) {
                    latestTimestamp = timestamp;
                    latestMessage = message;
                }
                continue;
            }

            // For messages sent TO the current user, check read status
            Boolean isRead = null;

            // Check various read status fields
            if (messageSnapshot.hasChild("read")) {
                isRead = messageSnapshot.child("read").getValue(Boolean.class);
            } else if (messageSnapshot.hasChild("seen")) {
                isRead = messageSnapshot.child("seen").getValue(Boolean.class);
            } else if (messageSnapshot.hasChild("isRead")) {
                isRead = messageSnapshot.child("isRead").getValue(Boolean.class);
            }

            // Count as unread only if explicitly false (not null, not true)
            if (isRead != null && !isRead) {
                unreadCount++;
                Log.d(TAG, "Found unread message in conversation with " + otherUserId);
            }

            // Update latest message tracking
            if (message != null && timestamp != null && timestamp > latestTimestamp) {
                latestTimestamp = timestamp;
                latestMessage = message;
            }
        }

        // Store the latest message info for this user
        if (latestMessage != null) {
            Log.d(TAG,
                    "Found conversation with " + otherUserId + ": " + latestMessage + " (unread: " + unreadCount + ")");
            lastMessages.put(otherUserId, latestMessage);
            lastMessageTimes.put(otherUserId, latestTimestamp);

            // Only set unread count if greater than 0
            if (unreadCount > 0) {
                unreadCounts.put(otherUserId, unreadCount);
            } else if (unreadCounts.containsKey(otherUserId)) {
                // Remove any existing unread count if it's 0
                unreadCounts.remove(otherUserId);
            }
        }
    }

    public void filterUsers(String query) {
        if (binding == null || !isAdded()) {
            return;
        }

        try {
            filteredList.clear();

            if (query == null || query.isEmpty()) {
                // When no search query, show only users with conversations
                for (userModel user : usersList) {
                    String userId = user.getID();
                    if (userId != null && lastMessages.containsKey(userId)) {
                        filteredList.add(user);
                    }
                }
            } else {
                // When searching, still only show users with conversations but filter by
                // name/email
                query = query.toLowerCase().trim();

                for (userModel user : usersList) {
                    String userId = user.getID();
                    if (userId == null || !lastMessages.containsKey(userId)) {
                        continue; // Skip users without conversations
                    }

                    String name = user.getName();
                    String email = user.getEmail();

                    if ((name != null && name.toLowerCase().contains(query)) ||
                            (email != null && email.toLowerCase().contains(query))) {
                        filteredList.add(user);
                    }
                }
            }

            // Sort filtered list by last message time
            Collections.sort(filteredList, (user1, user2) -> {
                Long time1 = user1.getLastMessageTime();
                Long time2 = user2.getLastMessageTime();

                if (time1 == null && time2 == null)
                    return 0;
                if (time1 == null)
                    return 1;
                if (time2 == null)
                    return -1;

                return time2.compareTo(time1); // Descending order (newest first)
            });

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            // Update empty state view
            updateEmptyState();

            // Notify filter callback about the filtered results size
            onFilterComplete(filteredList.size());
        } catch (Exception e) {
            Log.e(TAG, "Error filtering users: " + e.getMessage());
        }
    }

    private void updateEmptyState() {
        if (binding == null)
            return;

        // Hide progress bar if it exists
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.GONE);
        }

        if (filteredList.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.chatRecyclerView.setVisibility(View.GONE);

            // Check if we have no chats at all or just no filtered results
            if (lastMessages.isEmpty()) {
                // No chats at all
                binding.emptyStateText
                        .setText("No conversations yet.\nStart chatting with someone from the profiles section!");
            } else {
                // No filtered results
                binding.emptyStateText.setText("No matches found for your search.");
            }
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFilterComplete(int size) {
        updateEmptyState();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // Always fetch messages to update unread status
        if (isInitialLoadComplete) {
            Log.d(TAG, "Refreshing messages to update unread counts");

            // Clear only unread counts but keep other data
            if (unreadCounts != null) {
                unreadCounts.clear();
            }

            // Show loading indicator temporarily
            if (binding != null && binding.progressBar != null) {
                binding.progressBar.setVisibility(View.VISIBLE);
            }

            // Refresh messages to get latest unread counts
            fetchLastMessages();
        } else {
            Log.d(TAG, "Initial load not complete, doing full refresh");
            doFullRefresh();
            isInitialLoadComplete = true;
        }
    }

    // Separate method for full data refresh
    private void doFullRefresh() {
        // Show loading indicator
        if (binding != null && binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        // Clear existing data
        if (lastMessages != null) {
            lastMessages.clear();
        }
        if (lastMessageTimes != null) {
            lastMessageTimes.clear();
        }
        if (unreadCounts != null) {
            unreadCounts.clear();
        }
        if (usersList != null) {
            usersList.clear();
        }
        if (filteredList != null) {
            filteredList.clear();
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Remove existing listeners to prevent duplicates
        removeListeners();

        // Reset and set up listeners again
        loadUsers();

        // Refresh message data - this will trigger the usersListener
        fetchLastMessages();

        // Refresh online users
        monitorOnlineUsers();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateOnlineStatus(boolean isOnline) {
        // Removed - now handled by allenConnectApp
    }

    @Override
    public void onDestroyView() {
        // Remove all Firebase listeners to prevent callbacks after view is destroyed
        removeListeners();

        // Clean up resources
        super.onDestroyView();
        binding = null;
        usersList = null;
        filteredList = null;
        onlineUsersList = null;
        adapter = null;
        onlineUsersAdapter = null;
        lastMessageTimes = null;
        lastMessages = null;
        unreadCounts = null;

        // Note: We intentionally don't reset isInitialLoadComplete here
        // to prevent unnecessary reloading when the fragment is recreated
    }

    private void removeListeners() {
        try {
            if (database != null) {
                if (usersListener != null) {
                    database.getReference().child("Users").removeEventListener(usersListener);
                }

                if (onlineUsersListener != null) {
                    database.getReference().child("Users").removeEventListener(onlineUsersListener);
                }

                if (messagesListener != null) {
                    database.getReference().child("chats").removeEventListener(messagesListener);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing listeners: " + e.getMessage());
        }
    }
}