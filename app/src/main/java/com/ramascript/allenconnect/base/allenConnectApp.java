package com.ramascript.allenconnect.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class allenConnectApp extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "allenConnectApp";
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private int activityReferences = 0;
    private boolean isAppInForeground = false;

    // New variables for improved online status handling
    private Handler onlineStatusHandler;
    private static final long ONLINE_STATUS_UPDATE_INTERVAL = 60000; // 1 minute
    private Runnable onlineStatusRunnable;
    private DatabaseReference userStatusRef;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Initialize handler for periodic online status updates
        onlineStatusHandler = new Handler();

        // register for activity lifecycle events
        registerActivityLifecycleCallbacks(this);

        // Setup online status heartbeat system
        setupOnlineStatusSystem();
    }

    /**
     * Sets up the system for periodic online status updates
     */
    private void setupOnlineStatusSystem() {
        onlineStatusRunnable = new Runnable() {
            @Override
            public void run() {
                // Only send heartbeat updates if the app is in foreground
                if (isAppInForeground && auth.getCurrentUser() != null) {
                    updateOnlineStatus(true);
                    // Schedule next update
                    onlineStatusHandler.postDelayed(this, ONLINE_STATUS_UPDATE_INTERVAL);
                }
            }
        };
    }

    /**
     * Update user's online status
     * 
     * @param isOnline true if user is online, false otherwise
     */
    public void updateOnlineStatus(boolean isOnline) {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            userStatusRef = database.getReference().child("Users").child(userId);

            try {
                Map<String, Object> updates = new HashMap<>();

                if (isOnline) {
                    // Use server timestamp for online status
                    updates.put("online", true);
                    updates.put("lastActive", ServerValue.TIMESTAMP);
                } else {
                    // When going offline, update both flags
                    updates.put("online", false);
                    updates.put("lastSeen", ServerValue.TIMESTAMP);
                    updates.put("lastActive", ServerValue.TIMESTAMP);
                }

                // Update in database
                userStatusRef.updateChildren(updates);

                Log.d(TAG, "User " + userId + " status updated: " + (isOnline ? "online" : "offline"));
            } catch (Exception e) {
                Log.e(TAG, "Failed to update online status: " + e.getMessage());
            }
        }
    }

    /**
     * Setup disconnect hook to ensure proper offline status when app closes
     * abruptly
     */
    private void setupDisconnectHook() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            DatabaseReference userRef = database.getReference().child("Users").child(userId);

            Map<String, Object> offlineUpdates = new HashMap<>();
            offlineUpdates.put("online", false);
            offlineUpdates.put("lastSeen", ServerValue.TIMESTAMP);

            // This will trigger when connection to Firebase is lost
            userRef.onDisconnect().updateChildren(offlineUpdates);
        }
    }

    // Activity lifecycle callbacks - for tracking active activities

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        // Not used
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (activityReferences == 0) {
            // App came to foreground
            isAppInForeground = true;

            // Update online status
            updateOnlineStatus(true);

            // Setup disconnect hook
            setupDisconnectHook();

            // Start periodic updates
            onlineStatusHandler.postDelayed(onlineStatusRunnable, ONLINE_STATUS_UPDATE_INTERVAL);

            Log.d(TAG, "App moved to foreground");
        }
        activityReferences++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // Update status when activity is resumed
        if (isAppInForeground) {
            updateOnlineStatus(true);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // Not used
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        activityReferences--;
        if (activityReferences == 0) {
            // App went to background
            isAppInForeground = false;

            // Update status to offline
            updateOnlineStatus(false);

            // Remove callbacks when app goes to background
            onlineStatusHandler.removeCallbacks(onlineStatusRunnable);

            Log.d(TAG, "App moved to background");
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // Not used
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // Not used
    }
}