package com.ramascript.allenconnect;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AllenConnectApp extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "AllenConnectApp";
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private int activityReferences = 0;
    private boolean isAppInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Register for activity lifecycle events
        registerActivityLifecycleCallbacks(this);
    }

    /**
     * Update user's online status
     * 
     * @param isOnline true if user is online, false otherwise
     */
    public void updateOnlineStatus(boolean isOnline) {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            // Current timestamp for last seen
            long currentTime = System.currentTimeMillis();

            try {
                Map<String, Object> updates = new HashMap<>();
                updates.put("online", isOnline);

                if (!isOnline) {
                    // Record last seen time when going offline
                    updates.put("lastSeen", currentTime);
                }

                // Update in database
                database.getReference()
                        .child("Users")
                        .child(userId)
                        .updateChildren(updates);

                Log.d(TAG, "User " + userId + " status updated: " + (isOnline ? "online" : "offline") +
                        (isOnline ? "" : ", last seen: " + currentTime));
            } catch (Exception e) {
                Log.e(TAG, "Failed to update online status: " + e.getMessage());
            }
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
            updateOnlineStatus(true);
            Log.d(TAG, "App moved to foreground");
        }
        activityReferences++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // Not used
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
            updateOnlineStatus(false);
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