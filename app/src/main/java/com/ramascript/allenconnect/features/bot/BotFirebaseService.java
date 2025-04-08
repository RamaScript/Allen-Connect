package com.ramascript.allenconnect.features.bot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ramascript.allenconnect.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BotFirebaseService {
    private static final String CHANNEL_ID = "bot_upload_channel";
    private static final int NOTIFICATION_ID = 1002;
    private static final String TAG = "BotFirebaseService";

    private final Context context;
    private final FirebaseStorage storage;
    private final DatabaseReference database;
    private final NotificationManager notificationManager;
    private final Handler mainHandler;

    public BotFirebaseService(Context context) {
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.database = FirebaseDatabase.getInstance().getReference();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.mainHandler = new Handler(Looper.getMainLooper());

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bot Upload Notifications",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Notifications for AllenBot file uploads");
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void uploadFiles(List<BotFileItem> files, String userId, OnUploadCompleteListener listener) {
        if (files == null || files.isEmpty()) {
            if (listener != null) {
                listener.onComplete(false, "No files to upload");
            }
            return;
        }

        // Show initial notification
        showUploadNotification(0, files.size());

        // Create a unique ID for this upload batch
        String batchId = UUID.randomUUID().toString();
        final int[] completedUploads = { 0 };
        final int[] failedUploads = { 0 };

        for (BotFileItem file : files) {
            uploadFile(file, userId, batchId, new OnFileUploadListener() {
                @Override
                public void onProgress(int progress) {
                    // Update notification with progress
                    int totalProgress = (completedUploads[0] * 100 + progress) / files.size();
                    showUploadNotification(totalProgress, files.size());
                }

                @Override
                public void onSuccess(String downloadUrl, String fileId) {
                    completedUploads[0]++;

                    // Now extract and save the text content
                    extractAndSaveFileText(file, fileId, userId);

                    // Check if all uploads are complete
                    if (completedUploads[0] + failedUploads[0] == files.size()) {
                        if (failedUploads[0] == 0) {
                            // All uploads successful
                            showUploadCompleteNotification(files.size());
                            if (listener != null) {
                                listener.onComplete(true, "All files uploaded successfully");
                            }
                        } else {
                            // Some uploads failed
                            showUploadPartialNotification(completedUploads[0], failedUploads[0]);
                            if (listener != null) {
                                listener.onComplete(false,
                                        completedUploads[0] + " files uploaded, " + failedUploads[0] + " failed");
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    failedUploads[0]++;

                    // Check if all uploads are complete
                    if (completedUploads[0] + failedUploads[0] == files.size()) {
                        if (completedUploads[0] == 0) {
                            // All uploads failed
                            showUploadFailedNotification();
                            if (listener != null) {
                                listener.onComplete(false, "All uploads failed");
                            }
                        } else {
                            // Some uploads failed
                            showUploadPartialNotification(completedUploads[0], failedUploads[0]);
                            if (listener != null) {
                                listener.onComplete(false,
                                        completedUploads[0] + " files uploaded, " + failedUploads[0] + " failed");
                            }
                        }
                    }
                }
            });
        }
    }

    private void uploadFile(BotFileItem file, String userId, String batchId, OnFileUploadListener listener) {
        // Create a unique file path in Firebase Storage
        String fileExtension = file.getFileType();
        String fileName = file.getFileName();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String storagePath = "bot_data/" + userId + "/" + timestamp + "_" + fileName;

        StorageReference fileRef = storage.getReference().child(storagePath);

        // Upload the file
        UploadTask uploadTask = fileRef.putFile(file.getFileUri());

        // Listen for upload progress
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            listener.onProgress((int) progress);
        });

        // Listen for upload success
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the download URL
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Save file metadata to Firebase Realtime Database
                saveFileMetadata(userId, batchId, fileName, file.getFileType(),
                        uri.toString(), file.getDescription(), listener);
            }).addOnFailureListener(e -> {
                listener.onFailure(e);
            });
        }).addOnFailureListener(e -> {
            listener.onFailure(e);
        });
    }

    private void saveFileMetadata(String userId, String batchId, String fileName, String fileType,
            String downloadUrl, String description, OnFileUploadListener listener) {
        // Create a unique ID for this file
        String fileId = UUID.randomUUID().toString();

        // Get current timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create file metadata
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("userId", userId);
        fileData.put("fileName", fileName);
        fileData.put("fileType", fileType);
        fileData.put("downloadUrl", downloadUrl);
        fileData.put("description", description);
        fileData.put("uploadTime", timestamp);
        fileData.put("batchId", batchId);

        // Save to Firebase Realtime Database
        DatabaseReference fileRef = database.child("data_for_chatbot").child(fileId);
        fileRef.setValue(fileData)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess(downloadUrl, fileId);
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(e);
                });
    }

    private void extractAndSaveFileText(BotFileItem file, String fileId, String userId) {
        // Extract text from the file and save it to Firebase
        FileTextExtractor.extractTextFromFile(context, file.getFileUri(), file.getFileType(),
                new FileTextExtractor.TextExtractionCallback() {
                    @Override
                    public void onTextExtracted(String text) {
                        // Save the extracted text to Firebase
                        database.child("data_for_chatbot").child(fileId).child("extractedText").setValue(text)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Text extraction saved successfully for file: " + fileId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error saving extracted text", e);
                                });
                    }

                    @Override
                    public void onExtractionFailed(Exception e) {
                        Log.e(TAG, "Text extraction failed for file: " + fileId, e);
                    }
                });
    }

    private void showUploadNotification(int progress, int totalFiles) {
        String title = "Uploading files to Allen Bot";
        String content = progress + "% (" + totalFiles + " files)";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void showUploadCompleteNotification(int totalFiles) {
        String title = "Upload Complete";
        String content = "Successfully uploaded " + totalFiles + " files";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void showUploadFailedNotification() {
        String title = "Upload Failed";
        String content = "Failed to upload files";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void showUploadPartialNotification(int successCount, int failureCount) {
        String title = "Upload Partially Complete";
        String content = "Uploaded " + successCount + " files, " + failureCount + " failed";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public interface OnUploadCompleteListener {
        void onComplete(boolean success, String message);
    }

    public interface OnFileUploadListener {
        void onProgress(int progress);

        void onSuccess(String downloadUrl, String fileId);

        void onFailure(Exception e);
    }
}