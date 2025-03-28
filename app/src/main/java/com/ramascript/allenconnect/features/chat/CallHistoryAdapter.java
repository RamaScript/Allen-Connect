package com.ramascript.allenconnect.features.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.CallViewHolder> {

    private final Context context;
    private final List<CallHistoryActivity.CallHistoryModel> calls;
    private final String currentUserId;
    private final SimpleDateFormat dateFormat;

    public CallHistoryAdapter(Context context, List<CallHistoryActivity.CallHistoryModel> calls) {
        this.context = context;
        this.calls = calls;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_call_history, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        CallHistoryActivity.CallHistoryModel call = calls.get(position);

        // Determine if outgoing or incoming call
        boolean isOutgoing = call.getCallerId().equals(currentUserId);

        // Get other user's ID
        String otherUserId = isOutgoing ? call.getReceiverId() : call.getCallerId();

        // Load user details
        loadUserDetails(otherUserId, holder);

        // Set call direction icon
        if (isOutgoing) {
            holder.callDirectionIcon.setImageResource(R.drawable.ic_call_made);
        } else {
            holder.callDirectionIcon.setImageResource(R.drawable.ic_call_received);
        }

        // Set call status icon
        if ("completed".equals(call.getStatus())) {
            holder.callStatusIcon.setImageResource(R.drawable.ic_call_completed);
            holder.callStatusIcon.setColorFilter(context.getResources().getColor(R.color.colorSuccess));
        } else if ("missed".equals(call.getStatus())) {
            holder.callStatusIcon.setImageResource(R.drawable.ic_call_missed);
            holder.callStatusIcon.setColorFilter(context.getResources().getColor(R.color.colorError));
        } else if ("declined".equals(call.getStatus())) {
            holder.callStatusIcon.setImageResource(R.drawable.ic_call_declined);
            holder.callStatusIcon.setColorFilter(context.getResources().getColor(R.color.colorError));
        }

        // Set call type
        holder.callTypeText.setText(call.getCallType());

        // Format call duration
        String durationText = formatDuration(call.getDuration());
        holder.callDurationText.setText(durationText);

        // Format timestamp
        String dateTimeText = dateFormat.format(new Date(call.getTimestamp()));
        holder.callTimeText.setText(dateTimeText);

        // Setup call back action
        holder.callBackButton.setOnClickListener(v -> initiateCall(otherUserId));

        // Setup item click to view user profile
        holder.itemView.setOnClickListener(v -> {
            // Navigate to user profile
        });
    }

    private void loadUserDetails(String userId, CallViewHolder holder) {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String profilePic = snapshot.child("profilePic").getValue(String.class);

                            holder.userNameText.setText(name);

                            if (profilePic != null && !profilePic.isEmpty()) {
                                Picasso.get()
                                        .load(profilePic)
                                        .placeholder(R.drawable.ic_avatar)
                                        .into(holder.userProfileImage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void initiateCall(String userId) {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String profilePic = snapshot.child("profilePic").getValue(String.class);

                            Intent intent = new Intent(context, VideoCallActivity.class);
                            intent.putExtra("userId", userId);
                            intent.putExtra("userName", name);
                            intent.putExtra("profilePic", profilePic);
                            intent.putExtra("isCallInitiator", true);
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private String formatDuration(long durationSeconds) {
        if (durationSeconds < 60) {
            return durationSeconds + " sec";
        } else {
            long minutes = TimeUnit.SECONDS.toMinutes(durationSeconds);
            long seconds = durationSeconds - TimeUnit.MINUTES.toSeconds(minutes);
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }
    }

    @Override
    public int getItemCount() {
        return calls.size();
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        final ImageView userProfileImage;
        final TextView userNameText;
        final ImageView callDirectionIcon;
        final ImageView callStatusIcon;
        final TextView callTypeText;
        final TextView callDurationText;
        final TextView callTimeText;
        final ImageView callBackButton;

        public CallViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.user_profile_image);
            userNameText = itemView.findViewById(R.id.user_name);
            callDirectionIcon = itemView.findViewById(R.id.call_direction_icon);
            callStatusIcon = itemView.findViewById(R.id.call_status_icon);
            callTypeText = itemView.findViewById(R.id.call_type);
            callDurationText = itemView.findViewById(R.id.call_duration);
            callTimeText = itemView.findViewById(R.id.call_time);
            callBackButton = itemView.findViewById(R.id.call_back_button);
        }
    }
}