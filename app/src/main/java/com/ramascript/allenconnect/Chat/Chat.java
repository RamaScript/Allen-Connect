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
import com.ramascript.allenconnect.Fragments.ChatsFragment;

import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatsFragment chatsFragment;

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

        // Initialize the fragment
        chatsFragment = new ChatsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, chatsFragment)
                .commit();

        // Setup search functionality using the correct ID from your layout
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Pass search query to fragment
                if (chatsFragment != null) {
                    chatsFragment.filterUsers(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(Chat.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}