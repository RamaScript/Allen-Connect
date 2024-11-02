package com.ramascript.allenconnect.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.CommentActivity;
import com.ramascript.allenconnect.Models.PostModel;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvPostSampleBinding;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {

   ArrayList<PostModel> list;
   Context context;

   FirebaseAuth auth;
   FirebaseDatabase database;

   public PostAdapter(ArrayList<PostModel> list, Context context) {
      this.list = list;
      this.context = context;
   }

   @NonNull
   @Override
   public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.rv_post_sample, parent, false);
      return new viewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull viewHolder holder, int position) {
      PostModel model = list.get(position);

      auth = FirebaseAuth.getInstance();
      database = FirebaseDatabase.getInstance();

      Picasso.get()
         .load(model.getPostImage())
         .placeholder(R.drawable.ic_post_placeholder)
         .into(holder.binding.postImg);

      holder.binding.commentTV.setText(model.getCommentCount() + "");
      holder.binding.likeTV.setText(model.getPostLikes() + "");

      String caption = model.getPostCaption();
      if (caption.equals("")) {
         holder.binding.postcaption.setVisibility(View.GONE);
      } else {
         holder.binding.postcaption.setText(model.getPostCaption());
         holder.binding.postcaption.setVisibility(View.VISIBLE);
      }

      database.getReference()
         .child("Users")
         .child(model.getPostedBy())
         .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               UserModel userModel = snapshot.getValue(UserModel.class);
               Picasso.get()
                  .load(userModel.getProfilePhoto())
                  .placeholder(R.drawable.ic_avatar)
                  .into(holder.binding.profileImagePost);
               holder.binding.userName.setText(userModel.getName());

               // Set the text based on user type
               if ( "Student".equals(userModel.getUserType())) {
                  holder.binding.about.setText(userModel.getCourse() + " (" + userModel.getYear() + " year)");
               } else if ("Alumni".equals(userModel.getUserType())) {
                  holder.binding.about.setText(userModel.getJobRole() + " at " + userModel.getCompany());
               } else if ("Professor".equals(userModel.getUserType())) {
                  holder.binding.about.setText("Professor at AGOI");
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
         });

      database.getReference()
         .child("Posts")
         .child(model.getPostID())
         .child("Likes")
         .child(FirebaseAuth.getInstance().getUid())
         .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()) {
                  holder.binding.likeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up_blue, 0, 0, 0);
               } else {
                  holder.binding.likeTV.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                        FirebaseDatabase.getInstance().getReference()
                           .child("Posts")
                           .child(model.getPostID())
                           .child("postLikes")
                           .addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot snapshot) {
                                 long currentLikes = 0;  // Default to 0 likes
                                 if (snapshot.exists() && snapshot.getValue() != null) {
                                    currentLikes = snapshot.getValue(Long.class);
                                 }

                                 // Increment likes
                                 FirebaseDatabase.getInstance().getReference()
                                    .child("Posts")
                                    .child(model.getPostID())
                                    .child("postLikes")
                                    .setValue(currentLikes + 1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void unused) {
                                          FirebaseDatabase.getInstance().getReference()
                                             .child("Posts")
                                             .child(model.getPostID())
                                             .child("Likes")
                                             .child(FirebaseAuth.getInstance().getUid())
                                             .setValue(true)
                                             .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                   holder.binding.likeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up_blue, 0, 0, 0);
                                                   holder.binding.likeTV.setEnabled(false);
                                                }
                                             });
                                       }
                                    });
                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError error) {
                                 Log.e("FirebaseError", "Error: " + error.getMessage());
                              }
                           });
                     }
                  });
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               Log.e("FirebaseError", "Error: " + error.getMessage());
            }
         });


      holder.binding.commentTV.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", model.getPostID());
            intent.putExtra("postedBy", model.getPostedBy());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
         }
      });
   }

   @Override
   public int getItemCount() {
      return list.size();
   }

   public class viewHolder extends RecyclerView.ViewHolder {

      RvPostSampleBinding binding;

      public viewHolder(@NonNull View itemView) {
         super(itemView);

         binding = RvPostSampleBinding.bind(itemView);

      }
   }
}
