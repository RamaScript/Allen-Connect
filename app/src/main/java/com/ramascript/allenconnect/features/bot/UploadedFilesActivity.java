package com.ramascript.allenconnect.features.bot;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ramascript.allenconnect.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UploadedFilesActivity extends AppCompatActivity {
    private RecyclerView rvUploadedFiles;
    private TextView tvNoFiles;
    private ProgressBar progressBar;
    private ImageView backButton;
    private ImageView btnMenu;
    private ImageView btnCloseSelection;
    private ImageView btnSelectAll;
    private ImageView btnDeleteSelected;
    private TextView tvSelectionCount;
    private LinearLayout normalAppBar;
    private LinearLayout selectionAppBar;

    private UploadedFilesAdapter adapter;
    private DatabaseReference database;
    private FirebaseStorage storage;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded_files);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();

        // Get current user ID
        currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Please log in to view uploaded files", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        rvUploadedFiles = findViewById(R.id.rvUploadedFiles);
        tvNoFiles = findViewById(R.id.tvNoFiles);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backButton);
        btnMenu = findViewById(R.id.btnMenu);
        btnCloseSelection = findViewById(R.id.btnCloseSelection);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        tvSelectionCount = findViewById(R.id.tvSelectionCount);
        normalAppBar = findViewById(R.id.normalAppBar);
        selectionAppBar = findViewById(R.id.selectionAppBar);

        // Set up back button
        backButton.setOnClickListener(v -> {
            if (adapter != null && adapter.isMultiSelectMode()) {
                exitMultiSelectMode();
            } else {
                finish();
            }
        });

        // Set up menu button
        btnMenu.setOnClickListener(v -> showMenu());

        // Set up multi-select mode buttons
        btnCloseSelection.setOnClickListener(v -> exitMultiSelectMode());
        btnSelectAll.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.selectAll();
                updateSelectionCount();
            }
        });
        btnDeleteSelected.setOnClickListener(v -> {
            if (adapter != null && adapter.getSelectedItemCount() > 0) {
                showDeleteConfirmationDialog(adapter.getSelectedFiles());
            }
        });

        // Set up RecyclerView
        adapter = new UploadedFilesAdapter(new ArrayList<>());
        rvUploadedFiles.setLayoutManager(new LinearLayoutManager(this));
        rvUploadedFiles.setAdapter(adapter);

        // Set up delete listener for single item deletion
        adapter.setOnFileDeleteListener(fileId -> {
            UploadedFile fileToDelete = null;
            for (UploadedFile file : adapter.getFiles()) {
                if (file.getFileId().equals(fileId)) {
                    fileToDelete = file;
                    break;
                }
            }
            if (fileToDelete != null) {
                List<UploadedFile> filesToDelete = new ArrayList<>();
                filesToDelete.add(fileToDelete);
                showDeleteConfirmationDialog(filesToDelete);
            }
        });

        // Set up long click listener to enter multi-select mode
        adapter.setOnFileLongClickListener(position -> {
            if (!adapter.isMultiSelectMode()) {
                enterMultiSelectMode();
                adapter.toggleSelection(position);
                updateSelectionCount();
            }
            return true;
        });

        // Set up item click listener
        adapter.setOnFileItemClickListener(position -> {
            if (adapter.isMultiSelectMode()) {
                updateSelectionCount();
            }
        });

        // Load uploaded files
        loadUploadedFiles();
    }

    private void showMenu() {
        PopupMenu popup = new PopupMenu(this, btnMenu);
        popup.getMenuInflater().inflate(R.menu.menu_uploaded_files, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_select_multiple) {
                enterMultiSelectMode();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void enterMultiSelectMode() {
        adapter.setMultiSelectMode(true);
        normalAppBar.setVisibility(View.GONE);
        selectionAppBar.setVisibility(View.VISIBLE);
        updateSelectionCount();
    }

    private void exitMultiSelectMode() {
        adapter.setMultiSelectMode(false);
        normalAppBar.setVisibility(View.VISIBLE);
        selectionAppBar.setVisibility(View.GONE);
    }

    private void updateSelectionCount() {
        int count = adapter.getSelectedItemCount();
        tvSelectionCount.setText(count + " selected");
    }

    private String getCurrentUserId() {
        // Try to get the current user from Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }

        // Try to get from SharedPreferences
        android.content.SharedPreferences userPrefs = getSharedPreferences("UserPrefs",
                android.content.Context.MODE_PRIVATE);
        String userId = userPrefs.getString("userId", null);

        if (userId != null && !userId.equals("unknown")) {
            return userId;
        }

        return null;
    }

    private void loadUploadedFiles() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoFiles.setVisibility(View.GONE);
        rvUploadedFiles.setVisibility(View.GONE);

        // Query files for the current user
        Query query = database.child("data_for_chatbot")
                .orderByChild("userId")
                .equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UploadedFile> files = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String fileId = snapshot.getKey();
                    String fileName = snapshot.child("fileName").getValue(String.class);
                    String fileType = snapshot.child("fileType").getValue(String.class);
                    String downloadUrl = snapshot.child("downloadUrl").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String uploadTime = snapshot.child("uploadTime").getValue(String.class);

                    if (fileName != null && fileType != null && downloadUrl != null) {
                        UploadedFile file = new UploadedFile(
                                fileId, fileName, fileType, downloadUrl, description, uploadTime);
                        files.add(file);
                    }
                }

                adapter.setFiles(files);

                progressBar.setVisibility(View.GONE);
                if (files.isEmpty()) {
                    tvNoFiles.setVisibility(View.VISIBLE);
                    rvUploadedFiles.setVisibility(View.GONE);
                } else {
                    tvNoFiles.setVisibility(View.GONE);
                    rvUploadedFiles.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UploadedFilesActivity.this,
                        "Error loading files: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(List<UploadedFile> filesToDelete) {
        if (filesToDelete == null || filesToDelete.isEmpty()) {
            return;
        }

        // Build file names list for the dialog message
        StringBuilder fileListBuilder = new StringBuilder();
        for (UploadedFile file : filesToDelete) {
            fileListBuilder.append("• ").append(file.getFileName()).append("\n");
        }

        String message;
        if (filesToDelete.size() == 1) {
            message = "Are you sure you want to delete this file?\n\n" + fileListBuilder.toString();
        } else {
            message = "Are you sure you want to delete these " + filesToDelete.size() + " files?\n\n" +
                    fileListBuilder.toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteMultipleFiles(filesToDelete);
                })
                .setNegativeButton("Cancel", null);

        builder.show();
    }

    private void deleteMultipleFiles(List<UploadedFile> filesToDelete) {
        if (filesToDelete.isEmpty()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Use counter to track when all deletions are complete
        final AtomicInteger pendingDeletions = new AtomicInteger(filesToDelete.size());
        final AtomicInteger successfulDeletions = new AtomicInteger(0);

        for (UploadedFile file : filesToDelete) {
            deleteUploadedFile(file.getFileId(), file.getDownloadUrl(), new DeleteCallback() {
                @Override
                public void onSuccess() {
                    successfulDeletions.incrementAndGet();
                    checkDeletionComplete();
                }

                @Override
                public void onFailure() {
                    checkDeletionComplete();
                }

                private void checkDeletionComplete() {
                    if (pendingDeletions.decrementAndGet() == 0) {
                        // All deletions have completed
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (successfulDeletions.get() > 0) {
                                if (successfulDeletions.get() == filesToDelete.size()) {
                                    Toast.makeText(UploadedFilesActivity.this,
                                            "All files deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(UploadedFilesActivity.this,
                                            successfulDeletions.get() + " of " + filesToDelete.size() +
                                                    " files deleted successfully",
                                            Toast.LENGTH_SHORT).show();
                                }

                                // Exit multi-select mode if we're in it
                                if (adapter.isMultiSelectMode()) {
                                    exitMultiSelectMode();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private interface DeleteCallback {
        void onSuccess();

        void onFailure();
    }

    private void deleteUploadedFile(String fileId, String downloadUrl, DeleteCallback callback) {
        if (downloadUrl != null) {
            // Delete from storage first
            try {
                StorageReference storageRef = storage.getReferenceFromUrl(downloadUrl);
                storageRef.delete()
                        .addOnSuccessListener(aVoid -> deleteFromDatabase(fileId, callback))
                        .addOnFailureListener(e -> {
                            // If we can't delete from storage, still try to delete from database
                            deleteFromDatabase(fileId, callback);
                        });
            } catch (Exception e) {
                // If we can't create a reference, still try to delete from database
                deleteFromDatabase(fileId, callback);
            }
        } else {
            // Just delete the database entry if no URL
            deleteFromDatabase(fileId, callback);
        }
    }

    private void deleteFromDatabase(String fileId, DeleteCallback callback) {
        // Delete from database
        database.child("data_for_chatbot").child(fileId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (adapter != null && adapter.isMultiSelectMode()) {
            exitMultiSelectMode();
        } else {
            super.onBackPressed();
        }
    }
}