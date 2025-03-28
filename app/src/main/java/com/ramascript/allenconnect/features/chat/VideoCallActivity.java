package com.ramascript.allenconnect.features.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.permissionx.guolindev.PermissionX;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityVideoCallBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;

public class VideoCallActivity extends AppCompatActivity {

    private ActivityVideoCallBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private MediaPlayer ringtonePlayer;

    private String userId; // Current user's ID
    private String remoteUserId; // ID of the remote user
    private String userName; // Name of the remote user
    private String profilePic; // Profile picture of the remote user
    private boolean isCallInitiator; // Whether the current user initiated the call

    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ImageCapture imageCapture;
    private boolean isCameraOn = true;
    private boolean isMicOn = true;
    private DatabaseReference callSignalingRef;
    private DatabaseReference myCallRef; // Reference to the call node where I'm the receiver
    private ValueEventListener signalListener;
    private ValueEventListener frameListener;

    // Add a database reference to track active calls to prevent self-notifications
    private DatabaseReference activeCallsRef;
    private ValueEventListener activeCallsListener;

    // Call timer variables
    private long callStartTime = 0;
    private Timer callTimer;
    private Handler timerHandler = new Handler();

    private Timer frameTimer;
    private boolean isCallConnected = false;
    private boolean isCallEnding = false;

    // Track call status for debugging
    private String lastRemoteFrameTime = "None";
    private int framesReceived = 0;
    private int framesSent = 0;

    private static final String TAG = "VideoCallActivity";
    private static final int FRAME_INTERVAL_MS = 200; // Send frames every 200ms for better performance

    // Add audio streaming variables
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private boolean isAudioStreaming = false;
    private Thread audioThread;
    private static final int SAMPLE_RATE = 16000; // 16kHz
    private static final int BUFFER_SIZE = 1024;
    private DatabaseReference audioRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Keep screen on during the call
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userId = auth.getUid();

        // Initialize active calls reference
        activeCallsRef = database.getReference().child("active_calls");

        // Initialize audio reference
        audioRef = database.getReference().child("audio").child(userId + "_" + remoteUserId);

        // Get data from intent
        remoteUserId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        profilePic = getIntent().getStringExtra("profilePic");
        isCallInitiator = getIntent().getBooleanExtra("isCallInitiator", false);

        if (remoteUserId == null) {
            Toast.makeText(this, "Error: User ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Call role: " + (isCallInitiator ? "CALLER" : "RECEIVER"));
        Log.d(TAG, "Self: " + userId + ", Remote: " + remoteUserId);

        // Setup signaling references for call
        // IMPORTANT: If I'm calling user X, I need to:
        // 1. Write to X_Me node for initial call signal
        // 2. Listen to Me_X node for responses
        callSignalingRef = database.getReference().child("calls").child(remoteUserId + "_" + userId);
        myCallRef = database.getReference().child("calls").child(userId + "_" + remoteUserId);

        // If I'm the caller, immediately register this as an active call to prevent
        // self-notification
        if (isCallInitiator) {
            Map<String, Object> activeCall = new HashMap<>();
            activeCall.put("caller", userId);
            activeCall.put("receiver", remoteUserId);
            activeCall.put("timestamp", System.currentTimeMillis());
            activeCallsRef.child(userId).setValue(activeCall);

            // Start playing ringtone for outgoing call
            playRingtone(R.raw.outgoing_call);
        } else {
            // Play ringtone for incoming call
            playRingtone(R.raw.incoming_call);
        }

        // Request permissions for camera and microphone
        PermissionX.init(this)
                .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        setupUI();
                        setupCallControls();
                        setupDebugInfo();

                        if (isCallInitiator) {
                            // Show calling UI - I'm calling someone
                            binding.callStatusLayout.setVisibility(View.VISIBLE);
                            binding.incomingCallLayout.setVisibility(View.GONE);
                            binding.remoteView.setVisibility(View.GONE); // Hide until connected
                            binding.callStatus.setText("Calling " + userName + "...");

                            // Start my camera for preview
                            setupLocalCamera();

                            // Create call notification in the database
                            sendCallRequest();
                        } else {
                            // Show incoming call UI - Someone is calling me
                            binding.callStatusLayout.setVisibility(View.GONE);
                            binding.incomingCallLayout.setVisibility(View.VISIBLE);
                            binding.remoteView.setVisibility(View.GONE); // Hide until connected
                            setupIncomingCallUI();
                        }
                    } else {
                        Toast.makeText(this, "Camera and microphone permissions are required", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                });
    }

    private void playRingtone(int resourceId) {
        try {
            // Release any previous MediaPlayer instance
            if (ringtonePlayer != null) {
                ringtonePlayer.release();
            }

            // Create and setup the MediaPlayer
            ringtonePlayer = MediaPlayer.create(this, resourceId);
            ringtonePlayer.setLooping(true);
            ringtonePlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Error playing ringtone: " + e.getMessage());
        }
    }

    private void stopRingtone() {
        if (ringtonePlayer != null && ringtonePlayer.isPlaying()) {
            ringtonePlayer.stop();
            ringtonePlayer.release();
            ringtonePlayer = null;
        }
    }

    private void startCallTimer() {
        // Stop ringtone when call connects
        stopRingtone();

        // Record the call start time
        callStartTime = SystemClock.elapsedRealtime();

        // Show the timer
        binding.callTimer.setVisibility(View.VISIBLE);

        // Update the timer every second
        callTimer = new Timer();
        callTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long elapsedMillis = SystemClock.elapsedRealtime() - callStartTime;
                updateCallTimerUI(elapsedMillis);
            }
        }, 0, 1000);
    }

    private void updateCallTimerUI(long elapsedMillis) {
        // Format the elapsed time as MM:SS
        final String formattedTime = String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(elapsedMillis),
                TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60);

        // Update the UI on the main thread
        runOnUiThread(() -> binding.callTimer.setText(formattedTime));
    }

    private void stopCallTimer() {
        if (callTimer != null) {
            callTimer.cancel();
            callTimer = null;
        }
    }

    private void saveCallHistory(long durationSeconds) {
        // Get current timestamp
        long timestamp = new Date().getTime();

        // Create call history entry
        Map<String, Object> callHistory = new HashMap<>();
        callHistory.put("callerId", isCallInitiator ? userId : remoteUserId);
        callHistory.put("receiverId", isCallInitiator ? remoteUserId : userId);
        callHistory.put("callType", "video");
        callHistory.put("duration", durationSeconds);
        callHistory.put("timestamp", timestamp);
        callHistory.put("status", "completed");

        // Format date for display
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String callId = "call_" + sdf.format(new Date());

        // Save to both users' call history
        database.getReference().child("users").child(userId).child("calls").child(callId)
                .setValue(callHistory);
        database.getReference().child("users").child(remoteUserId).child("calls").child(callId)
                .setValue(callHistory);

        // Add to chat history
        String chatId = userId.compareTo(remoteUserId) < 0 ? userId + "_" + remoteUserId : remoteUserId + "_" + userId;

        Map<String, Object> lastMsg = new HashMap<>();
        String callDuration = String.format(Locale.getDefault(), "%d:%02d",
                durationSeconds / 60, durationSeconds % 60);

        lastMsg.put("lastMessageType", "call");
        lastMsg.put("lastMessage", "Video call (" + callDuration + ")");
        lastMsg.put("timestamp", ServerValue.TIMESTAMP);
        lastMsg.put("senderId", isCallInitiator ? userId : remoteUserId);

        database.getReference().child("chats").child(chatId).updateChildren(lastMsg);
    }

    private void setupDebugInfo() {
        // Debug info is now hidden by default
        binding.debugInfo.setVisibility(View.GONE);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isCallEnding) {
                    runOnUiThread(() -> {
                        binding.debugInfo.setText(
                                "Role: " + (isCallInitiator ? "CALLER" : "RECEIVER") + "\n" +
                                        "Connected: " + isCallConnected + "\n" +
                                        "Frames received: " + framesReceived + "\n" +
                                        "Frames sent: " + framesSent + "\n" +
                                        "Last frame: " + lastRemoteFrameTime);
                    });
                }
            }
        }, 0, 1000);
    }

    private void setupLocalCamera() {
        // Get a camera provider instance
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Get the camera provider
                cameraProvider = cameraProviderFuture.get();

                // Default to front camera
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                // Set up preview
                preview = new Preview.Builder().build();
                binding.localView.setVisibility(View.VISIBLE);
                preview.setSurfaceProvider(binding.localView.getSurfaceProvider());

                // Set up image capture for video frames
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                // Bind to lifecycle
                if (cameraProvider != null) {
                    // Unbind any previous use cases
                    cameraProvider.unbindAll();

                    // Bind the preview and image capture
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error setting up camera: " + e.getMessage());
                Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, getExecutor());
    }

    private void setupRemoteView() {
        // Show remote view
        runOnUiThread(() -> {
            binding.remoteContainer.setVisibility(View.VISIBLE);
            binding.remoteView.setVisibility(View.GONE); // We're not using PreviewView for remote
            binding.remoteStatusText.setVisibility(View.VISIBLE);
            binding.remoteStatusText.setText(userName + "'s camera");

            // Make sure the placeholder is visible and in front
            binding.remotePlaceholderImage.setVisibility(View.VISIBLE);

            // Start listening for remote video frames
            setupRemoteVideoListener();

            // Start the call timer
            startCallTimer();

            // Start audio streaming
            startAudioStreaming();
        });
    }

    private void setupFrameCapture() {
        if (frameTimer != null) {
            frameTimer.cancel();
            frameTimer = null;
        }

        frameTimer = new Timer();
        frameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isCallConnected && isCameraOn && imageCapture != null && !isCallEnding) {
                    captureAndSendFrame();
                }
            }
        }, 0, FRAME_INTERVAL_MS);
    }

    private void captureAndSendFrame() {
        if (imageCapture == null)
            return;

        imageCapture.takePicture(getExecutor(), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                try {
                    // Get the buffer from the image
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);

                    // Compress bitmap to reduce size for faster transmission
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bitmap != null) {
                        // Apply some size reduction for faster transmission
                        int originalWidth = bitmap.getWidth();
                        int originalHeight = bitmap.getHeight();

                        // Scale down the image to reduce size (quarter size for better performance)
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                                bitmap, originalWidth / 4, originalHeight / 4, true);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos); // Compress to 30% quality
                        byte[] compressedBytes = baos.toByteArray();

                        if (!isCallConnected || isCallEnding) {
                            // Don't send frame if call is ending or not connected
                            return;
                        }

                        // Convert bytes to Base64 string for Firebase Realtime Database
                        String base64Frame = android.util.Base64.encodeToString(compressedBytes,
                                android.util.Base64.DEFAULT);

                        // Only update if call is still active
                        if (isCallConnected && !isCallEnding) {
                            // Send frame directly in the database
                            Map<String, Object> frameData = new HashMap<>();
                            frameData.put("frame", base64Frame);
                            frameData.put("timestamp", System.currentTimeMillis());
                            frameData.put("senderId", userId);

                            // Key fix - always write to the node with MY ID first
                            DatabaseReference targetRef = database.getReference().child("calls")
                                    .child(userId + "_" + remoteUserId).child("senderFrame");

                            targetRef.setValue(frameData)
                                    .addOnSuccessListener(aVoid -> {
                                        framesSent++;
                                        Log.d(TAG, "Frame sent successfully. Total: " + framesSent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error sending frame: " + e.getMessage());
                                    });
                        }

                        // Clean up unused bitmaps
                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        if (!scaledBitmap.isRecycled()) {
                            scaledBitmap.recycle();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing frame: " + e.getMessage());
                } finally {
                    // Always close the image proxy
                    image.close();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Error capturing frame: " + exception.getMessage());
            }
        });
    }

    private void setupRemoteVideoListener() {
        // The key fix - listen to the node with REMOTE USER's ID first
        DatabaseReference frameRef = database.getReference().child("calls")
                .child(remoteUserId + "_" + userId).child("senderFrame");

        Log.d(TAG, "Listening for remote frames at: " + frameRef.getKey());

        if (frameListener != null) {
            frameRef.removeEventListener(frameListener);
        }

        frameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("frame").exists()) {
                    String base64Frame = snapshot.child("frame").getValue(String.class);
                    String senderId = snapshot.child("senderId").getValue(String.class);

                    // Verify that this frame is from the remote user
                    if (base64Frame != null && senderId != null && senderId.equals(remoteUserId)) {
                        framesReceived++;
                        lastRemoteFrameTime = new Date().toString();

                        // Decode Base64 string to bitmap
                        try {
                            byte[] decodedBytes = android.util.Base64.decode(base64Frame, android.util.Base64.DEFAULT);
                            Bitmap frameBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                            // Display the frame
                            runOnUiThread(() -> {
                                if (frameBitmap != null) {
                                    // Make sure it's visible and update with remote user's frame
                                    binding.remotePlaceholderImage.setVisibility(View.VISIBLE);
                                    binding.remotePlaceholderImage.setImageBitmap(frameBitmap);
                                    binding.remoteStatusText.setText(userName + "'s camera");
                                    binding.remoteStatusText.setVisibility(View.VISIBLE);
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error decoding frame: " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Frame listener error: " + error.getMessage());
            }
        };

        frameRef.addValueEventListener(frameListener);
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void setupUI() {
        // Set user info
        binding.userName.setText(userName);
        binding.incomingUserName.setText(userName);

        if (profilePic != null && !profilePic.isEmpty()) {
            Picasso.get()
                    .load(profilePic)
                    .placeholder(R.drawable.ic_avatar)
                    .into(binding.profileImage);

            Picasso.get()
                    .load(profilePic)
                    .placeholder(R.drawable.ic_avatar)
                    .into(binding.incomingProfileImage);
        }
    }

    private void setupIncomingCallUI() {
        binding.acceptCallBtn.setOnClickListener(v -> {
            // Hide incoming call layout and show call layout
            binding.incomingCallLayout.setVisibility(View.GONE);
            binding.callStatusLayout.setVisibility(View.VISIBLE);
            binding.callStatus.setText("Connected");

            // Accept call and create answer
            acceptCall();
        });

        binding.declineCallBtn.setOnClickListener(v -> {
            // Send decline message to caller and end the activity
            sendCallResponse("declined");
            finish();
        });
    }

    private void setupCallControls() {
        binding.toggleMicBtn.setOnClickListener(v -> {
            isMicOn = !isMicOn;
            binding.toggleMicBtn.setImageResource(isMicOn ? R.drawable.ic_mic : R.drawable.ic_mic_off);

            // Actually mute/unmute audio here
            if (isMicOn) {
                startAudioStreaming();
            } else {
                pauseAudioStreaming();
            }

            Toast.makeText(this, isMicOn ? "Microphone on" : "Microphone off", Toast.LENGTH_SHORT).show();

            // Update status in Firebase
            Map<String, Object> micStatus = new HashMap<>();
            micStatus.put("micEnabled", isMicOn);
            DatabaseReference targetRef = isCallInitiator ? callSignalingRef : myCallRef;
            targetRef.updateChildren(micStatus);
        });

        binding.toggleCameraBtn.setOnClickListener(v -> {
            isCameraOn = !isCameraOn;
            binding.toggleCameraBtn.setImageResource(isCameraOn ? R.drawable.ic_videocam : R.drawable.ic_videocam_off);

            if (isCameraOn) {
                setupLocalCamera(); // Turn camera back on
                binding.localView.setVisibility(View.VISIBLE);
            } else if (cameraProvider != null) {
                cameraProvider.unbindAll(); // Turn camera off
                binding.localView.setVisibility(View.GONE);
            }

            // Update status in Firebase
            Map<String, Object> cameraStatus = new HashMap<>();
            cameraStatus.put("cameraEnabled", isCameraOn);
            DatabaseReference targetRef = isCallInitiator ? callSignalingRef : myCallRef;
            targetRef.updateChildren(cameraStatus);
        });

        binding.endCallBtn.setOnClickListener(v -> {
            endCall();
        });
    }

    private void setupSignalingListener() {
        // If I'm the caller, I need to listen to the node I sent the call to
        // (callSignalingRef)
        // If I'm the receiver, I need to listen to the node where I got the call
        // (myCallRef)
        DatabaseReference signalRef = isCallInitiator ? callSignalingRef : myCallRef;

        Log.d(TAG, (isCallInitiator ? "CALLER" : "RECEIVER") + " listening for signals at: " + signalRef.getKey());

        if (signalListener != null) {
            signalRef.removeEventListener(signalListener);
        }

        signalListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String type = snapshot.child("type").getValue(String.class);
                    if (type != null) {
                        Log.d(TAG, "Signal received: " + type);

                        switch (type) {
                            case "offer":
                                // Only handle if I'm the receiver
                                if (!isCallInitiator) {
                                    // Already showing incoming call UI from onCreate
                                }
                                break;

                            case "answer":
                                // Only handle if I'm the caller
                                if (isCallInitiator) {
                                    isCallConnected = true;
                                    runOnUiThread(() -> {
                                        binding.callStatus.setText("Connected");
                                        setupRemoteView();
                                    });
                                    setupFrameCapture();
                                }
                                break;

                            case "end":
                                // Other user ended the call
                                if (!isCallEnding) { // Prevent multiple endings
                                    isCallEnding = true;
                                    runOnUiThread(() -> {
                                        Toast.makeText(VideoCallActivity.this, "Call ended by " + userName,
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                }
                                break;

                            case "declined":
                                // Call was declined
                                runOnUiThread(() -> {
                                    Toast.makeText(VideoCallActivity.this, "Call declined by " + userName,
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                                break;
                        }
                    }

                    // Check camera status updates
                    if (snapshot.child("cameraEnabled").exists()) {
                        Boolean remoteCamera = snapshot.child("cameraEnabled").getValue(Boolean.class);
                        if (remoteCamera != null && !remoteCamera) {
                            // Remote user disabled camera
                            runOnUiThread(() -> {
                                binding.remoteStatusText.setText(userName + "'s camera is off");
                                binding.remotePlaceholderImage.setImageResource(R.drawable.ic_videocam_off);
                                binding.remotePlaceholderImage.setVisibility(View.VISIBLE);
                            });
                        } else if (remoteCamera != null) {
                            // Remote user enabled camera
                            runOnUiThread(() -> {
                                binding.remoteStatusText.setText(userName + "'s camera");
                                binding.remotePlaceholderImage.setVisibility(View.VISIBLE);
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Signaling database error: " + error.getMessage());
            }
        };

        signalRef.addValueEventListener(signalListener);
    }

    private void sendCallRequest() {
        Map<String, Object> offerData = new HashMap<>();
        offerData.put("type", "offer");
        offerData.put("timestamp", new Date().getTime());
        offerData.put("callerName", auth.getCurrentUser().getDisplayName());
        offerData.put("callerId", userId);
        offerData.put("cameraEnabled", true);
        offerData.put("micEnabled", true);

        // Write to target user's node to create the call
        Log.d(TAG, "Sending call request to: " + callSignalingRef.getKey());
        callSignalingRef.setValue(offerData)
                .addOnSuccessListener(aVoid -> {
                    // Setup signaling listener to get response
                    setupSignalingListener();

                    // Set a timeout for the call if not answered
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // Check if call is still in "offer" state after 30 seconds
                            if (!isCallConnected && !isCallEnding && !isFinishing()) {
                                runOnUiThread(() -> {
                                    Toast.makeText(VideoCallActivity.this, "No answer - Call timed out",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                        }
                    }, 30000); // 30 seconds timeout
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void acceptCall() {
        // Send answer to database - IMPORTANT: write to MY node
        Map<String, Object> answerData = new HashMap<>();
        answerData.put("type", "answer");
        answerData.put("timestamp", new Date().getTime());
        answerData.put("cameraEnabled", true);
        answerData.put("micEnabled", true);

        Log.d(TAG, "Sending accept to: " + myCallRef.getKey());
        myCallRef.setValue(answerData)
                .addOnSuccessListener(aVoid -> {
                    // Register this as an active call
                    Map<String, Object> activeCall = new HashMap<>();
                    activeCall.put("caller", remoteUserId);
                    activeCall.put("receiver", userId);
                    activeCall.put("timestamp", System.currentTimeMillis());
                    activeCallsRef.child(userId).setValue(activeCall);

                    // Start local camera
                    setupLocalCamera();

                    // Show remote view
                    setupRemoteView();

                    // Ensure audio is working for incoming calls
                    setupRemoteAudioListener();
                    startAudioStreaming();

                    // Setup frame capturing
                    isCallConnected = true;
                    setupFrameCapture();

                    // Setup signaling listener if not already set up
                    if (signalListener == null) {
                        setupSignalingListener();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to accept call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void sendCallResponse(String response) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("type", response);
        responseData.put("timestamp", new Date().getTime());

        myCallRef.setValue(responseData);
    }

    private void endCall() {
        // Calculate call duration (if connected)
        long durationSeconds = 0;
        if (callStartTime > 0) {
            durationSeconds = TimeUnit.MILLISECONDS.toSeconds(SystemClock.elapsedRealtime() - callStartTime);
        }

        // Stop the timer
        stopCallTimer();

        // Save call history if call was connected and lasted at least 1 second
        if (isCallConnected && durationSeconds > 0) {
            saveCallHistory(durationSeconds);
        }

        // Mark as ending to prevent further processing
        isCallEnding = true;

        // Stop frame capturing
        if (frameTimer != null) {
            frameTimer.cancel();
            frameTimer = null;
        }

        // Send end call signal to both paths to ensure it's received
        Map<String, Object> endData = new HashMap<>();
        endData.put("type", "end");
        endData.put("timestamp", new Date().getTime());

        // Send to both nodes to ensure both sides receive the end signal
        callSignalingRef.setValue(endData)
                .addOnSuccessListener(aVoid -> {
                    myCallRef.setValue(endData)
                            .addOnSuccessListener(aVoid2 -> {
                                // Clean up active call status
                                activeCallsRef.child(userId).removeValue();

                                // Finish activity
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    // Try to end on at least one path
                    myCallRef.setValue(endData);
                    finish();
                });

        // Stop audio streaming
        stopAudioStreaming();
    }

    private void startAudioStreaming() {
        if (isAudioStreaming || !isMicOn)
            return;

        try {
            isAudioStreaming = true;

            // Initialize AudioRecord for capturing microphone audio
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

            // Initialize AudioTrack for playing received audio
            int playbackBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    playbackBufferSize,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();
            audioRecord.startRecording();

            // Create a thread for continuous audio processing
            audioThread = new Thread(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (isAudioStreaming && !isCallEnding && isMicOn) {
                    // Read audio data from microphone
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        // Convert to base64 for Firebase transmission
                        String base64Audio = android.util.Base64.encodeToString(
                                buffer, 0, bytesRead, android.util.Base64.DEFAULT);

                        // Send audio data to remote user
                        Map<String, Object> audioData = new HashMap<>();
                        audioData.put("audio", base64Audio);
                        audioData.put("timestamp", ServerValue.TIMESTAMP);

                        // Only send if call is connected
                        if (isCallConnected) {
                            audioRef.setValue(audioData);
                        }
                    }

                    try {
                        // Small delay to prevent flooding the database
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });

            audioThread.start();

            // Set up listener for remote audio
            setupRemoteAudioListener();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up audio streaming: " + e.getMessage());
            isAudioStreaming = false;
        }
    }

    private void setupRemoteAudioListener() {
        // Reference to the other user's audio node
        DatabaseReference remoteAudioRef = database.getReference()
                .child("audio")
                .child(remoteUserId + "_" + userId);

        remoteAudioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && audioTrack != null
                        && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    String base64Audio = snapshot.child("audio").getValue(String.class);
                    if (base64Audio != null) {
                        // Decode and play the audio
                        byte[] audioData = android.util.Base64.decode(base64Audio, android.util.Base64.DEFAULT);
                        audioTrack.write(audioData, 0, audioData.length);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Remote audio listener cancelled: " + error.getMessage());
            }
        });
    }

    private void pauseAudioStreaming() {
        isAudioStreaming = false;
        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }
    }

    private void stopAudioStreaming() {
        isAudioStreaming = false;

        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }

        if (audioRecord != null) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            audioRecord.release();
            audioRecord = null;
        }

        if (audioTrack != null) {
            if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop();
            }
            audioTrack.release();
            audioTrack = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop ringtone if still playing
        stopRingtone();

        // Stop call timer
        stopCallTimer();

        // Mark as ending to prevent further processing
        isCallEnding = true;

        // Cancel frame timer
        if (frameTimer != null) {
            frameTimer.cancel();
            frameTimer = null;
        }

        // Clean up camera resources
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }

        // Remove listeners
        if (signalListener != null) {
            DatabaseReference signalRef = isCallInitiator ? callSignalingRef : myCallRef;
            signalRef.removeEventListener(signalListener);
        }

        if (frameListener != null) {
            DatabaseReference frameRef = isCallInitiator ? callSignalingRef.child("senderFrame")
                    : myCallRef.child("senderFrame");
            frameRef.removeEventListener(frameListener);
        }

        if (activeCallsListener != null) {
            activeCallsRef.child(userId).removeEventListener(activeCallsListener);
        }

        // Clean up active call status
        activeCallsRef.child(userId).removeValue();

        // Clean up call signaling data from both paths
        myCallRef.removeValue();
        callSignalingRef.removeValue();

        // Clean up audio resources
        stopAudioStreaming();
    }
}