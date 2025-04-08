package com.ramascript.allenconnect.features.bot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.ramascript.allenconnect.R;

import java.util.ArrayList;
import java.util.List;

public class BotUploadActivity extends AppCompatActivity {
    private static final int PICK_FILES_REQUEST_CODE = 1001;
    private static final String[] ALLOWED_FILE_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };

    private RecyclerView rvSelectedFiles;
    private TextView tvNoFilesSelected;
    private AppCompatButton btnChooseFiles;
    private AppCompatButton btnUploadToBot;
    private ImageView btnViewUploadedFiles;
    private ImageView backButton;
    private BotFileAdapter fileAdapter;
    private BotFirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_upload);

        // Initialize Firebase service
        firebaseService = new BotFirebaseService(this);

        // Initialize views
        rvSelectedFiles = findViewById(R.id.rvSelectedFiles);
        tvNoFilesSelected = findViewById(R.id.tvNoFilesSelected);
        btnChooseFiles = findViewById(R.id.btnChooseFiles);
        btnUploadToBot = findViewById(R.id.btnUploadToBot);
        btnViewUploadedFiles = findViewById(R.id.btnViewUploadedFiles);
        backButton = findViewById(R.id.backButton);

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Set up RecyclerView
        fileAdapter = new BotFileAdapter();
        rvSelectedFiles.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedFiles.setAdapter(fileAdapter);

        // Set up file removal listener
        fileAdapter.setOnFileRemovedListener(position -> {
            fileAdapter.removeFile(position);
            updateUI();
        });

        // Set up choose files button
        btnChooseFiles.setOnClickListener(v -> openFilePicker());

        // Set up upload button
        btnUploadToBot.setOnClickListener(v -> uploadFilesToFirebase());

        // Set up view uploaded files button
        btnViewUploadedFiles.setOnClickListener(v -> {
            Intent intent = new Intent(this, UploadedFilesActivity.class);
            startActivity(intent);
        });

        updateUI();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, ALLOWED_FILE_TYPES);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Files"), PICK_FILES_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILES_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    // Multiple files selected
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();
                        addFileToList(fileUri);
                    }
                } else if (data.getData() != null) {
                    // Single file selected
                    Uri fileUri = data.getData();
                    addFileToList(fileUri);
                }
                updateUI();
            }
        }
    }

    private void addFileToList(Uri fileUri) {
        String fileName = getFileNameFromUri(fileUri);
        String fileType = getFileTypeFromUri(fileUri);

        // Check if file type is allowed
        if (isFileTypeAllowed(fileType)) {
            BotFileItem fileItem = new BotFileItem(fileUri, fileName, fileType);
            fileAdapter.addFile(fileItem);
        } else {
            Toast.makeText(this, "File type not supported: " + fileName, Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null,
                    null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileTypeFromUri(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) {
            String path = uri.getPath();
            if (path != null) {
                int dotIndex = path.lastIndexOf('.');
                if (dotIndex > 0) {
                    return path.substring(dotIndex + 1).toLowerCase();
                }
            }
            return "";
        }

        // Extract file extension from MIME type
        if (mimeType.equals("application/pdf")) {
            return "pdf";
        } else if (mimeType.equals("application/msword") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return "doc";
        } else if (mimeType.equals("text/plain")) {
            return "txt";
        } else if (mimeType.equals("application/vnd.ms-excel") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return "xls";
        }

        return "";
    }

    private boolean isFileTypeAllowed(String fileType) {
        if (fileType == null || fileType.isEmpty())
            return false;

        fileType = fileType.toLowerCase();
        return fileType.equals("pdf") ||
                fileType.equals("doc") ||
                fileType.equals("docx") ||
                fileType.equals("txt") ||
                fileType.equals("xls") ||
                fileType.equals("xlsx");
    }

    private void updateUI() {
        if (fileAdapter.getItemCount() > 0) {
            rvSelectedFiles.setVisibility(View.VISIBLE);
            tvNoFilesSelected.setVisibility(View.GONE);
            btnUploadToBot.setEnabled(true);
        } else {
            rvSelectedFiles.setVisibility(View.GONE);
            tvNoFilesSelected.setVisibility(View.VISIBLE);
            btnUploadToBot.setEnabled(false);
        }
    }

    private void uploadFilesToFirebase() {
        // Get the current user ID from SharedPreferences
        android.content.SharedPreferences userPrefs = getSharedPreferences("UserPrefs",
                android.content.Context.MODE_PRIVATE);
        String userId = userPrefs.getString("userId", null);

        if (userId == null || userId.equals("unknown")) {
            // Try to get the current user from Firebase Auth
            com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
            } else {
                Toast.makeText(this, "Please log in to upload files", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Get the list of files to upload
        List<BotFileItem> files = fileAdapter.getFileItems();

        // Disable the upload button to prevent multiple uploads
        btnUploadToBot.setEnabled(false);

        // Upload files to Firebase
        firebaseService.uploadFiles(files, userId, new BotFirebaseService.OnUploadCompleteListener() {
            @Override
            public void onComplete(boolean success, String message) {
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(BotUploadActivity.this, "Files uploaded successfully",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(BotUploadActivity.this, "Upload failed: " + message,
                                Toast.LENGTH_SHORT).show();
                        btnUploadToBot.setEnabled(true);
                    }
                });
            }
        });
    }
}