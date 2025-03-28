package com.ramascript.allenconnect.features.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityCallHistoryBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallHistoryActivity extends AppCompatActivity {

    private ActivityCallHistoryBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private CallHistoryAdapter adapter;
    private List<CallHistoryActivity.CallHistoryModel> callsList;
    private String filterUserId;
    private static final String TAG = "CallHistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Call History");

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Check if we should filter by specific user
        filterUserId = getIntent().getStringExtra("userId");

        // Initialize call history list and adapter
        callsList = new ArrayList<>();
        adapter = new CallHistoryAdapter(this, callsList);

        // Setup recycler view
        binding.callHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.callHistoryRecyclerView.setAdapter(adapter);

        // Load call history
        loadCallHistory();
    }

    private void loadCallHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.noCallsText.setVisibility(View.GONE);

        DatabaseReference callsRef = database.getReference()
                .child("users")
                .child(auth.getUid())
                .child("calls");

        Query query = callsRef.orderByChild("timestamp");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callsList.clear();

                for (DataSnapshot callSnapshot : snapshot.getChildren()) {
                    try {
                        CallHistoryActivity.CallHistoryModel call = callSnapshot
                                .getValue(CallHistoryActivity.CallHistoryModel.class);

                        if (call != null) {
                            // If filtering by user, only include calls with that user
                            if (filterUserId != null && !filterUserId.isEmpty()) {
                                String otherParticipant = call.getCallerId().equals(auth.getUid())
                                        ? call.getReceiverId()
                                        : call.getCallerId();

                                if (otherParticipant.equals(filterUserId)) {
                                    callsList.add(call);
                                }
                            } else {
                                callsList.add(call);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing call: " + e.getMessage());
                    }
                }

                // Sort calls by timestamp (newest first)
                Collections.reverse(callsList);

                // Update UI
                binding.progressBar.setVisibility(View.GONE);

                if (callsList.isEmpty()) {
                    binding.noCallsText.setVisibility(View.VISIBLE);
                    binding.callHistoryRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.noCallsText.setVisibility(View.GONE);
                    binding.callHistoryRecyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.noCallsText.setVisibility(View.VISIBLE);
                binding.callHistoryRecyclerView.setVisibility(View.GONE);
                Toast.makeText(CallHistoryActivity.this, "Error loading call history", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Model class for call history
    public static class CallHistoryModel {
        private String callId;
        private String callerId;
        private String receiverId;
        private String callType;
        private long duration;
        private long timestamp;
        private String status;

        public CallHistoryModel() {
            // Required empty constructor for Firebase
        }

        public String getCallId() {
            return callId;
        }

        public void setCallId(String callId) {
            this.callId = callId;
        }

        public String getCallerId() {
            return callerId;
        }

        public void setCallerId(String callerId) {
            this.callerId = callerId;
        }

        public String getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(String receiverId) {
            this.receiverId = receiverId;
        }

        public String getCallType() {
            return callType;
        }

        public void setCallType(String callType) {
            this.callType = callType;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}