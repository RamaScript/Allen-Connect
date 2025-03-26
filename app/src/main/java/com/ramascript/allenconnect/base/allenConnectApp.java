package com.ramascript.allenconnect.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class allenConnectApp extends Application implements Application.ActivityLifecycleCallbacks,
        NetworkChangeReceiver.NetworkCallback {

    private static final String TAG = "allenConnectApp";
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private int activityReferences = 0;
    private boolean isAppInForeground = false;
    private boolean isConnectedToInternet = false;

    // New variables for improved online status handling
    private Handler onlineStatusHandler;
    private static final long ONLINE_STATUS_UPDATE_INTERVAL = 60000; // 1 minute
    private Runnable onlineStatusRunnable;
    private DatabaseReference userStatusRef;
    private ConnectivityManager connectivityManager;

    // Network change monitoring
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Firebase disk persistence for offline capability
        try {
            // This must be set before any other Firebase database usage
            // And can only be called once per application
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // This may throw if it's already been called before, which is okay
            Log.e(TAG, "Error enabling Firebase persistence: " + e.getMessage());
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Initialize ConnectivityManager
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize handler for periodic online status updates
        onlineStatusHandler = new Handler();

        // Register for activity lifecycle events
        registerActivityLifecycleCallbacks(this);

        // Setup online status heartbeat system
        setupOnlineStatusSystem();

        // Register network change receiver
        setupNetworkMonitoring();
    }

    /**
     * Set up network monitoring to detect connectivity changes
     */
    private void setupNetworkMonitoring() {
        // Check initial network state
        isConnectedToInternet = isNetworkAvailable();

        // Create and register network change receiver
        networkChangeReceiver = new NetworkChangeReceiver(this);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);

        Log.d(TAG, "Network monitoring initialized, current state: " +
                (isConnectedToInternet ? "Connected" : "Disconnected"));
    }

    /**
     * Called when the network connectivity state changes
     */
    @Override
    public void onNetworkChanged(boolean isConnected) {
        Log.d(TAG, "Network state changed: " + (isConnected ? "Connected" : "Disconnected"));

        // Update connectivity state
        boolean previousState = isConnectedToInternet;
        isConnectedToInternet = isConnected;

        // Update online status if app is in foreground and connectivity changed
        if (isAppInForeground && previousState != isConnectedToInternet) {
            updateOnlineStatus(isConnectedToInternet);

            // Show toast message about connectivity change
            String message = isConnectedToInternet ? "Connected to network"
                    : "Network connection lost, you appear offline to others";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
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
                    // Check internet connection and update status
                    isConnectedToInternet = isNetworkAvailable();
                    updateOnlineStatus(isConnectedToInternet && isAppInForeground);

                    // Schedule next update
                    onlineStatusHandler.postDelayed(this, ONLINE_STATUS_UPDATE_INTERVAL);
                }
            }
        };
    }

    /**
     * Check if the device currently has an internet connection
     */
    public boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
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
                // Always check network connectivity before marking as online
                boolean actuallyOnline = isOnline && isNetworkAvailable();

                Map<String, Object> updates = new HashMap<>();

                if (actuallyOnline) {
                    // Use server timestamp for online status
                    updates.put("online", true);
                    updates.put("lastActive", ServerValue.TIMESTAMP);
                } else {
                    // When going offline, update both flags
                    updates.put("online", false);
                    updates.put("lastSeen", ServerValue.TIMESTAMP);
                    updates.put("lastActive", ServerValue.TIMESTAMP);
                }

                // Update in database with priority for offline updates
                userStatusRef.updateChildren(updates, (error, ref) -> {
                    if (error != null) {
                        Log.e(TAG, "Error updating online status: " + error.getMessage());
                    } else {
                        Log.d(TAG, "Successfully updated online status for " + userId +
                                ": " + (actuallyOnline ? "online" : "offline"));
                    }
                });

                Log.d(TAG, "User " + userId + " status updated: " + (actuallyOnline ? "online" : "offline") +
                        " (network available: " + isNetworkAvailable() + ")");

                // Store current state
                isConnectedToInternet = isNetworkAvailable();

                // If we're going offline, set up a special listener to confirm changes were
                // applied
                if (!actuallyOnline) {
                    userStatusRef.child("online").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean currentOnlineState = snapshot.getValue(Boolean.class);
                            Log.d(TAG, "Current online state in database: " + currentOnlineState);

                            // If still showing as online in database, try to force it offline
                            if (currentOnlineState != null && currentOnlineState) {
                                Log.d(TAG, "Forcing offline status update...");
                                userStatusRef.child("online").setValue(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error checking online status: " + error.getMessage());
                        }
                    });
                }
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

            // Make sure we have a proper connection status check
            DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean connected = snapshot.getValue(Boolean.class);
                    if (connected == null)
                        connected = false;

                    Log.d(TAG, "Firebase connection state: " + (connected ? "connected" : "disconnected"));

                    if (!connected) {
                        // We're disconnected from Firebase, make sure we're marked offline
                        // Force offline status immediately
                        userRef.child("online").setValue(false);

                        // Try alternative path to ensure offline status is set
                        DatabaseReference statusRef = database.getReference().child("Users").child(userId);
                        statusRef.child("online").setValue(false);
                    } else if (isAppInForeground && isConnectedToInternet) {
                        // Only reestablish disconnect operation if we truly have connectivity
                        userRef.onDisconnect().updateChildren(offlineUpdates);

                        // Re-check internet connectivity and update accordingly
                        boolean actuallyConnected = isNetworkAvailable();
                        if (actuallyConnected) {
                            userRef.child("online").setValue(true);
                        } else {
                            userRef.child("online").setValue(false);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase connection monitor was cancelled: " + error.getMessage());
                }
            });
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

            // Check internet connection
            isConnectedToInternet = isNetworkAvailable();

            // Update online status based on network availability
            updateOnlineStatus(isConnectedToInternet && isAppInForeground);

            // Setup disconnect hook
            setupDisconnectHook();

            // Start periodic updates
            onlineStatusHandler.postDelayed(onlineStatusRunnable, ONLINE_STATUS_UPDATE_INTERVAL);

            Log.d(TAG, "App moved to foreground, network available: " + isConnectedToInternet);
        }
        activityReferences++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // Update status when activity is resumed
        if (isAppInForeground) {
            // Check internet connection
            isConnectedToInternet = isNetworkAvailable();
            updateOnlineStatus(isConnectedToInternet && isAppInForeground);
            Log.d(TAG, "Activity resumed, network available: " + isConnectedToInternet);
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

    @Override
    public void onTerminate() {
        super.onTerminate();

        // Clean up - unregister network receiver
        if (networkChangeReceiver != null) {
            try {
                unregisterReceiver(networkChangeReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering network receiver: " + e.getMessage());
            }
        }
    }
}