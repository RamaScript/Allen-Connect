package com.ramascript.allenconnect.features.comment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.base.mainActivity;
import com.ramascript.allenconnect.features.post.postModel;
import com.ramascript.allenconnect.features.user.userModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.ActivityCommentBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class commentActivity extends AppCompatActivity {

    ActivityCommentBinding binding;
    Intent intent;
    String postId;
    String postedBy;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<commentModel> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iHome = new Intent(commentActivity.this, mainActivity.class);
                startActivity(iHome);
                finish();
            }
        });

        intent = getIntent();
        postId = intent.getStringExtra("postId");
        postedBy = intent.getStringExtra("postedBy");

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        database.getReference()
                .child("Posts")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    postModel postModel = snapshot.getValue(com.ramascript.allenconnect.features.post.postModel.class);
                    if (postModel != null) {
                        Picasso.get()
                                .load(postModel.getPostImage())
                                .placeholder(R.drawable.ic_post_placeholder)
                                .into(binding.postImageComment);
                        binding.captionComment.setText(postModel.getPostCaption());
                        binding.likeTV.setText(postModel.getPostLikes()+"");
                        binding.commentTV.setText(postModel.getCommentCount()+"");

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error: " + error.getMessage());
            }
        });

        database.getReference()
                .child("Users")
                .child(postedBy).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModel userModel = snapshot.getValue(com.ramascript.allenconnect.features.user.userModel.class);
                Picasso.get()
                        .load(userModel.getProfilePhoto())
                        .placeholder(R.drawable.ic_avatar)
                        .into(binding.profileImagePost);

                binding.autherName.setText(userModel.getName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                commentModel commentModel = new commentModel();
                commentModel.setCommentBody(binding.commentET.getText().toString());
                commentModel.setCommentedAt(new Date().getTime());
                commentModel.setCommentedBy(auth.getUid());

                database.getReference()
                        .child("Posts")
                        .child(postId)
                        .child("comments")
                        .push()
                        .setValue(commentModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference()
                                .child("Posts")
                                .child(postId)
                                .child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int commentCount = 0;
                                if (snapshot.exists()){
                                    commentCount = snapshot.getValue(Integer.class);
                                }
                                database.getReference()
                                        .child("Posts")
                                        .child(postId)
                                        .child("commentCount")
                                        .setValue(commentCount+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                binding.commentET.setText("");
                                            }
                                        });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }
        });

        commentAdapter adapter = new commentAdapter(this,list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.commentsRV.setLayoutManager(layoutManager);
        binding.commentsRV.setAdapter(adapter);

        database.getReference()
                .child("Posts")
                .child(postId)
                .child("comments")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    commentModel commentModel = dataSnapshot.getValue(com.ramascript.allenconnect.features.comment.commentModel.class);
                    list.add(commentModel);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}