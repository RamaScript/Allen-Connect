package com.ramascript.allenconnect.features.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvCommStudentBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class userAdapter extends RecyclerView.Adapter<userAdapter.viewHolder>{

   Context context;
   ArrayList<userModel> list;
   FirebaseAuth auth;
   FirebaseDatabase database;

   public userAdapter(Context context, ArrayList<userModel> list) {
      this.context = context;
      this.list = list;
      auth = FirebaseAuth.getInstance();
      database = FirebaseDatabase.getInstance();
   }

   @NonNull
   @Override
   public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.rv_comm_student,parent,false);
      return new viewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull viewHolder holder, int position) {
      userModel userModel = list.get(position);

      Picasso.get()
          .load(userModel.getProfilePhoto())
          .placeholder(R.drawable.ic_avatar)
          .into(holder.profilePic);

      holder.name.setText(userModel.getName());

      // Set the text based on user type
      if (!userModel.getID().equals(auth.getUid()) && "Student".equals(userModel.getUserType())) {
         holder.profession.setText(userModel.getCourse() + " (" + userModel.getYear() + " year)");
      } else if (!userModel.getID().equals(auth.getUid()) && "Alumni".equals(userModel.getUserType())) {
         holder.profession.setText(userModel.getJobRole() + " at " + userModel.getCompany());
      } else if (!userModel.getID().equals(auth.getUid()) && "Professor".equals(userModel.getUserType())) {
         holder.profession.setText("Professor at AGOI");
      }

      // Check if current user is following this user
      database.getReference()
          .child("Users")
          .child(userModel.getID())
          .child("Followers")
          .child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                   holder.binding.followTV.setBackgroundColor(ContextCompat.getColor(context,R.color.transparent));
                   holder.binding.followTV.setText("Following");
                   holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.textColor));
                   holder.binding.followTV.setEnabled(false);
                } else {
                   holder.binding.followTV.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                         long currentTime = new Date().getTime();

                         // 1. Create Follower node in other user's profile
                         followerModel follower = new followerModel();
                         follower.setFollowedBy(auth.getUid());
                         follower.setFollowedAt(currentTime);

                         // 2. Create Following node in current user's profile
                         followingModel following = new followingModel();
                         following.setFollowingTo(userModel.getID());
                         following.setFollowingAt(currentTime);

                         // Start transaction to update both nodes
                         database.getReference().child("Users")
                             .child(userModel.getID())
                             .child("Followers")
                             .child(auth.getUid())
                             .setValue(follower).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                   // Update following count of current user
                                   database.getReference().child("Users")
                                       .child(auth.getUid())
                                       .child("Following")
                                       .child(userModel.getID())
                                       .setValue(following).addOnSuccessListener(new OnSuccessListener<Void>() {
                                          @Override
                                          public void onSuccess(Void unused) {
                                             // Update follower count in the other user's profile
                                             database.getReference().child("Users")
                                                 .child(userModel.getID())
                                                 .child("followersCount")
                                                 .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                       int currentCount = 0;
                                                       if (snapshot.exists()) {
                                                          currentCount = snapshot.getValue(Integer.class);
                                                       }
                                                       database.getReference().child("Users")
                                                           .child(userModel.getID())
                                                           .child("followersCount")
                                                           .setValue(currentCount + 1)
                                                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                              @Override
                                                              public void onSuccess(Void unused) {
                                                                 // Update UI
                                                                 holder.binding.followTV.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                                                                 holder.binding.followTV.setText("Following");
                                                                 holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.textColor));
                                                                 holder.binding.followTV.setEnabled(false);
                                                                 Toast.makeText(context, "You followed " + userModel.getName(), Toast.LENGTH_SHORT).show();
                                                              }
                                                           }).addOnFailureListener(new OnFailureListener() {
                                                              @Override
                                                              public void onFailure(@NonNull Exception e) {
                                                                 Toast.makeText(context, "Failed to update follower count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                              }
                                                           });
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                       Toast.makeText(context, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                 });
                                          }
                                       }).addOnFailureListener(new OnFailureListener() {
                                          @Override
                                          public void onFailure(@NonNull Exception e) {
                                             Toast.makeText(context, "Failed to update following: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                          }
                                       });
                                }
                             }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                   Toast.makeText(context, "Failed to follow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                             });
                      }
                   });
                }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
             }
          });
   }

   @Override
   public int getItemCount() {
      return list.size();
   }

   public class viewHolder extends RecyclerView.ViewHolder{

      RvCommStudentBinding binding;

      ImageView profilePic;
      TextView name,profession,followerCount;

      public viewHolder(@NonNull View itemView) {
         super(itemView);
         binding = RvCommStudentBinding.bind(itemView);
         profilePic = binding.studentPic;
         name = binding.name;
         profession = binding.professionTV;
      }
   }
}