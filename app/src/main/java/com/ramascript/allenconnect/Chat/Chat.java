package com.ramascript.allenconnect.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.ramascript.allenconnect.Adapters.chatTabViewpagerAdapter;
import com.ramascript.allenconnect.MainActivity;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityChatBinding;
import com.ramascript.allenconnect.Adapters.ChatUsersAdapter;
import com.ramascript.allenconnect.Models.UserModel;

import java.util.ArrayList;

public class Chat extends AppCompatActivity implements ChatUsersAdapter.FilterCallback {

    ActivityChatBinding binding;
    ChatUsersAdapter adapter;
    ArrayList<UserModel> usersList;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize usersList
        usersList = new ArrayList<>();

        // Initialize adapter
        adapter = new ChatUsersAdapter(usersList, this, this);
        binding.chatRecyclerView.setAdapter(adapter);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup search functionality
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Fetch users from Firebase
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

                    // Add user only if:
                    // 1. User object is not null
                    // 2. User has an ID
                    // 3. User is not the current user
                    if (user != null && dataSnapshot.getKey() != null && currentUserId != null
                            && !dataSnapshot.getKey().equals(currentUserId)) {
                        user.setID(dataSnapshot.getKey());
                        usersList.add(user);
                    }
                }
                adapter.updateList(usersList);

                // Show/hide no results view based on list size
                binding.noResultsView.setVisibility(usersList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                binding.noResultsView.setVisibility(View.VISIBLE);
            }
        });

        binding.viewPagerChat.setAdapter(new chatTabViewpagerAdapter(getSupportFragmentManager()));
        binding.tabLayoutChat.setupWithViewPager(binding.viewPagerChat);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(Chat.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onFilterComplete(int size) {
        if (binding != null) {
            binding.noResultsView.setVisibility(size == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}