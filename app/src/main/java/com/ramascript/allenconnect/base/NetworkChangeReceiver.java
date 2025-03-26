package com.ramascript.allenconnect.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * BroadcastReceiver to monitor network connectivity changes
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";
    private NetworkCallback networkCallback;

    public interface NetworkCallback {
        void onNetworkChanged(boolean isConnected);
    }

    public NetworkChangeReceiver(NetworkCallback callback) {
        this.networkCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean isConnected = isNetworkAvailable(context);
            Log.d(TAG, "Network connectivity changed: " + (isConnected ? "Connected" : "Disconnected"));

            // If network is disconnected, force update user status to offline immediately
            if (!isConnected) {
                forceOfflineStatus();
            }

            if (networkCallback != null) {
                networkCallback.onNetworkChanged(isConnected);
            }
        }
    }

    /**
     * Force update user's online status to offline immediately when network is
     * disconnected
     */
    private void forceOfflineStatus() {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();
                Log.d(TAG, "Network disconnected, forcing offline status for user: " + userId);

                // Directly set online status to false in the database
                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(userId)
                        .child("online")
                        .setValue(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error forcing offline status: " + e.getMessage());
        }
    }

    /**
     * Check if the device has active network connectivity
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
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
}