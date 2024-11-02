package com.ramascript.allenconnect;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.Adapters.ChatMsgAdapter;
import com.ramascript.allenconnect.Models.ChatMsgModel;
import com.ramascript.allenconnect.databinding.ActivityChatDetailBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        final String senderId = auth.getUid();
        String receiveId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePicture = getIntent().getStringExtra("profilePicture");

        binding.userName.setText(userName);
        Picasso.get()
                .load(profilePicture)
                .placeholder(R.drawable.ic_avatar)
                .into(binding.profileImage);

        binding.backBtnIV.setOnClickListener(v -> {
            Intent intent = new Intent(ChatDetailActivity.this, Chat.class);
            startActivity(intent);
            finish();
        });

        final ArrayList<ChatMsgModel> chatMsgModel = new ArrayList<>();
        final ChatMsgAdapter chatMsgAdapter = new ChatMsgAdapter(chatMsgModel, this);
        binding.chatRecyclerView.setAdapter(chatMsgAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                binding.getRoot().getWindowVisibleDisplayFrame(r);
                int screenHeight = binding.getRoot().getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // If the keypad height is more than 200 pixels, consider it open
                if (keypadHeight > 200) {
                    // Move the LinearLayout above the keyboard
                    binding.linear.setTranslationY(-keypadHeight);
                    // Scroll to the last item in the RecyclerView when the keyboard opens
                    if (chatMsgAdapter.getItemCount() > 0) {
                        binding.chatRecyclerView.smoothScrollToPosition(chatMsgAdapter.getItemCount() - 1);
                    }
                } else {
                    // Reset the LinearLayout position when the keyboard is hidden
                    binding.linear.setTranslationY(0);
                }
            }
        });

        final String senderRoom = senderId + receiveId;
        final String receiverRoom = receiveId + senderId;

        ValueEventListener messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMsgModel.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        ChatMsgModel model = snapshot1.getValue(ChatMsgModel.class);
                        if (model != null) {
                            chatMsgModel.add(model);
                        }
                    }
                    chatMsgAdapter.notifyDataSetChanged();
                    if (chatMsgAdapter.getItemCount() > 0) {
                        binding.chatRecyclerView.smoothScrollToPosition(chatMsgAdapter.getItemCount() - 1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors here
            }
        };

        database.getReference().child("chats").child(senderRoom).addValueEventListener(messageListener);
        database.getReference().child("chats").child(receiverRoom).addValueEventListener(messageListener);

        binding.send.setOnClickListener(v -> {
            String message = binding.msgEt.getText().toString();

            if (!message.isEmpty()) {
                ChatMsgModel model = new ChatMsgModel(senderId, message);
                model.setTimestamp(new Date().getTime());
                binding.msgEt.setText("");

                database.getReference().child("chats").child(senderRoom).push().setValue(model);
                database.getReference().child("chats").child(receiverRoom).push().setValue(model);
            }
        });
    }
}
