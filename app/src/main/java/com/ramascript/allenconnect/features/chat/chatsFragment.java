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
import java.util.Map;

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

        loadUsers();
        monitorOnlineUsers();
        fetchLastMessages();

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

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            userModel user = dataSnapshot.getValue(userModel.class);
                            String userId = dataSnapshot.getKey();

                            if (user != null && userId != null && !userId.equals(currentUserId)) {
                                user.setID(userId);

                                // Set last message data if available
                                if (lastMessages.containsKey(userId)) {
                                    user.setLastMsg(lastMessages.get(userId));
                                }

                                // Set last message time if available
                                if (lastMessageTimes.containsKey(userId)) {
                                    user.setLastMessageTime(lastMessageTimes.get(userId));
                                }

                                // Set unread count if available
                                if (unreadCounts.containsKey(userId)) {
                                    user.setUnreadCount(unreadCounts.get(userId));
                                }

                                usersList.add(user);
                                filteredList.add(user);
                            }
                        }

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
                        Log.e(TAG, "Error in loadUsers: " + e.getMessage());
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

            database.getReference().child("Users").addValueEventListener(usersListener);
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

                    // Process all chat rooms
                    for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                        String roomId = roomSnapshot.getKey();

                        // Only process rooms related to the current user
                        if (roomId != null && roomId.startsWith(currentUserId)) {
                            // Extract the other user's ID from the room ID
                            String otherUserId = roomId.substring(currentUserId.length());

                            // Variables to track the latest message
                            String latestMessage = null;
                            long latestTimestamp = 0;
                            int unreadCount = 0;

                            // Process all messages in this room
                            for (DataSnapshot messageSnapshot : roomSnapshot.getChildren()) {
                                String senderId = messageSnapshot.child("uId").getValue(String.class);
                                String message = messageSnapshot.child("message").getValue(String.class);
                                Object timestampObj = messageSnapshot.child("timestamp").getValue();

                                if (message != null && timestampObj != null) {
                                    long timestamp = Long.parseLong(timestampObj.toString());

                                    // Count unread messages (sent by the other user)
                                    Boolean read = messageSnapshot.child("read").getValue(Boolean.class);
                                    if (senderId != null && !senderId.equals(currentUserId)
                                            && (read == null || !read)) {
                                        unreadCount++;
                                    }

                                    // Update latest message tracking
                                    if (timestamp > latestTimestamp) {
                                        latestTimestamp = timestamp;
                                        latestMessage = message;
                                    }
                                }
                            }

                            // If we found messages, update our maps
                            if (latestMessage != null) {
                                lastMessages.put(otherUserId, latestMessage);
                                lastMessageTimes.put(otherUserId, latestTimestamp);
                                if (unreadCount > 0) {
                                    unreadCounts.put(otherUserId, unreadCount);
                                }
                            }
                        }
                    }

                    // Reload users to apply the message data
                    if (binding != null) {
                        loadUsers();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in fetchLastMessages: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch last messages: " + error.getMessage());
            }
        };

        chatsRef.addValueEventListener(messagesListener);
    }

    public void filterUsers(String query) {
        if (binding == null || !isAdded())
            return;

        try {
            filteredList.clear();

            if (query.isEmpty()) {
                filteredList.addAll(usersList);
            } else {
                String searchQuery = query.toLowerCase().trim();
                for (userModel user : usersList) {
                    if (user.getName() != null &&
                            user.getName().toLowerCase().startsWith(searchQuery)) {
                        filteredList.add(user);
                    }
                }
            }

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
            Log.e(TAG, "Error in filterUsers: " + e.getMessage());
        }
    }

    private void updateEmptyState() {
        if (binding != null && isAdded()) {
            binding.noResultsView.setVisibility(
                    filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onFilterComplete(int size) {
        updateEmptyState();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh message data when fragment resumes
        fetchLastMessages();
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