package com.ramascript.allenconnect.features.chat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IncomingCallService extends Service {

    private static final String TAG = "IncomingCallService";
    private static final String CHANNEL_ID = "video_call_channel";
    private static final int NOTIFICATION_ID = 1001;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ValueEventListener callListener;
    private DatabaseReference callsRef;
    private DatabaseReference activeCallsRef;

    @Override
    public void onCreate() {
        super.onCreate();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if (auth.getCurrentUser() != null) {
            createNotificationChannel();
            setupCallListener();
        } else {
            stopSelf();
        }
    }

    private void setupCallListener() {
        String userId = auth.getUid();
        if (userId == null) {
            stopSelf();
            return;
        }

        // Reference to track active calls
        activeCallsRef = database.getReference().child("active_calls").child(userId);

        // Listen for any incoming calls to this user
        callsRef = database.getReference().child("calls");
        callListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot callSnapshot : snapshot.getChildren()) {
                    String key = callSnapshot.getKey();

                    // Check if this call entry is directed to current user
                    if (key != null && key.endsWith("_" + userId)) {
                        String type = callSnapshot.child("type").getValue(String.class);

                        if ("offer".equals(type)) {
                            // This is an incoming call
                            long timestamp = 0;
                            if (callSnapshot.child("timestamp").exists()) {
                                timestamp = callSnapshot.child("timestamp").getValue(Long.class);
                            }
                            long currentTime = new Date().getTime();

                            // Only handle calls that are recent (within last 30 seconds)
                            if ((currentTime - timestamp) < 30000) {
                                String callerName = callSnapshot.child("callerName").getValue(String.class);
                                String callerId = callSnapshot.child("callerId").getValue(String.class);

                                // Check if we're already in a call before showing notification
                                checkActiveCallsBeforeNotification(callerId, callerName);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        };

        callsRef.addValueEventListener(callListener);
    }

    private void checkActiveCallsBeforeNotification(String callerId, String callerName) {
        if (callerId == null)
            return;

        activeCallsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // No active call record, safe to show notification
                    Log.d(TAG, "No active call found, showing notification for caller: " + callerId);
                    handleIncomingCall(callerId, callerName);
                } else {
                    // Check if the active call involves this caller
                    if (snapshot.child("caller").exists() &&
                            callerId.equals(snapshot.child("caller").getValue(String.class))) {
                        // Already handling this call, don't show notification
                        Log.d(TAG, "Already in call with this user, skipping notification");
                    } else if (snapshot.child("receiver").exists() &&
                            callerId.equals(snapshot.child("receiver").getValue(String.class))) {
                        // Already in call with this user
                        Log.d(TAG, "Already in call with this user, skipping notification");
                    } else {
                        // In call with someone else, show busy notification
                        Log.d(TAG, "User is in another call, showing busy notification");
                        sendBusyResponse(callerId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking active calls: " + error.getMessage());
                // On error, show notification to be safe
                handleIncomingCall(callerId, callerName);
            }
        });
    }

    private void sendBusyResponse(String callerId) {
        if (callerId == null || auth.getCurrentUser() == null)
            return;

        String userId = auth.getUid();
        Map<String, Object> busyData = new HashMap<>();
        busyData.put("type", "busy");
        busyData.put("timestamp", new Date().getTime());

        database.getReference()
                .child("calls")
                .child(userId + "_" + callerId)
                .setValue(busyData);
    }

    private void handleIncomingCall(String callerId, String callerName) {
        if (callerId == null)
            return;

        // Create intent for accepting the call
        Intent acceptIntent = new Intent(this, VideoCallActivity.class);
        acceptIntent.putExtra("userId", callerId);
        acceptIntent.putExtra("userName", callerName != null ? callerName : "User");
        acceptIntent.putExtra("isCallInitiator", false);
        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create decline intent
        Intent declineIntent = new Intent(this, CallActionReceiver.class);
        declineIntent.setAction("DECLINE_CALL");
        declineIntent.putExtra("callerId", callerId);
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create notification for incoming call
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_videocam)
                .setContentTitle("Incoming Video Call")
                .setContentText("Call from " + (callerName != null ? callerName : "User"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_call, "Accept", pendingIntent)
                .addAction(R.drawable.ic_call_end, "Decline", declinePendingIntent);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Video Call Notifications";
            String description = "Notifications for incoming video calls";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callListener != null && callsRef != null) {
            callsRef.removeEventListener(callListener);
        }
    }

    public static class CallActionReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if ("DECLINE_CALL".equals(intent.getAction())) {
                String callerId = intent.getStringExtra("callerId");
                if (callerId != null) {
                    // Send decline response
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null) {
                        String userId = auth.getUid();

                        Map<String, Object> declineData = new HashMap<>();
                        declineData.put("type", "declined");
                        declineData.put("timestamp", new Date().getTime());

                        FirebaseDatabase.getInstance().getReference()
                                .child("calls")
                                .child(userId + "_" + callerId)
                                .setValue(declineData);

                        // Save missed call history
                        saveMissedCallHistory(userId, callerId);
                    }

                    // Dismiss the notification
                    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                    notificationManager.cancel(NOTIFICATION_ID);
                }
            }
        }

        private void saveMissedCallHistory(String userId, String callerId) {
            // Get current timestamp
            long timestamp = new Date().getTime();

            // Create call history entry
            Map<String, Object> callHistory = new HashMap<>();
            callHistory.put("callerId", callerId);
            callHistory.put("receiverId", userId);
            callHistory.put("callType", "video");
            callHistory.put("duration", 0);
            callHistory.put("timestamp", timestamp);
            callHistory.put("status", "missed");

            // Generate unique call ID based on timestamp
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss",
                    java.util.Locale.getDefault());
            String callId = "call_" + sdf.format(new Date());

            // Save to both users' call history
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("calls").child(callId)
                    .setValue(callHistory);
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(callerId).child("calls").child(callId)
                    .setValue(callHistory);

            // Add to chat history
            String chatId = userId.compareTo(callerId) < 0 ? userId + "_" + callerId : callerId + "_" + userId;

            Map<String, Object> lastMsg = new HashMap<>();
            lastMsg.put("lastMessageType", "call");
            lastMsg.put("lastMessage", "Missed video call");
            lastMsg.put("timestamp", com.google.firebase.database.ServerValue.TIMESTAMP);
            lastMsg.put("senderId", callerId);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats").child(chatId).updateChildren(lastMsg);
        }
    }
}