package com.ramascript.allenconnect.features.community;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.RvCommStudentBinding;
import com.ramascript.allenconnect.features.user.followerModel;
import com.ramascript.allenconnect.features.user.followingModel;
import com.ramascript.allenconnect.features.user.userModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class communityUserAdapter extends RecyclerView.Adapter<communityUserAdapter.viewHolder> {

   Context context;
   ArrayList<userModel> list;
   FirebaseAuth auth;
   FirebaseDatabase database;

   public communityUserAdapter(Context context, ArrayList<userModel> list) {
      this.context = context;
      this.list = list;
      auth = FirebaseAuth.getInstance();
      database = FirebaseDatabase.getInstance();
   }

   @NonNull
   @Override
   public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.rv_comm_student, parent, false);
      return new viewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull viewHolder holder, int position) {
      userModel userModel = list.get(position);

      // Skip deleted users (should not occur since they are filtered in fragment,
      // but adding extra safety check)
      Boolean isDeleted = userModel.isDeleted();
      if (isDeleted != null && isDeleted) {
         holder.itemView.setVisibility(View.GONE);
         holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
         return;
      } else {
         holder.itemView.setVisibility(View.VISIBLE);
         holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
               ViewGroup.LayoutParams.MATCH_PARENT,
               ViewGroup.LayoutParams.WRAP_CONTENT));
      }

      // Load profile picture
      if (userModel.getProfilePhoto() != null && !userModel.getProfilePhoto().isEmpty()) {
         Picasso.get()
               .load(userModel.getProfilePhoto())
               .placeholder(R.drawable.ic_avatar)
               .into(holder.profilePic);
      } else {
         holder.profilePic.setImageResource(R.drawable.ic_avatar);
      }

      // Set user name
      holder.name.setText(userModel.getName() != null ? userModel.getName() : "User");

      // Format user info with dots between segments
      String userInfo = formatUserInfo(userModel);
      holder.profession.setText(userInfo);

      // Check if current user is following this user
      checkFollowStatus(holder, userModel);

      // Add click listener to open the user's profile
      holder.itemView.setOnClickListener(v -> {
         navigateToProfile(userModel.getID());
      });
   }

   // Helper method to format user info with dots
   private String formatUserInfo(userModel user) {
      StringBuilder info = new StringBuilder();

      if (user.getID().equals(auth.getUid())) {
         // For current user
         return "You";
      }

      String userType = user.getUserType();
      if (userType == null) {
         return "";
      }

      switch (userType) {
         case "Student":
            String course = user.getCourse();
            String year = user.getYear();

            if (course != null && !course.isEmpty()) {
               info.append(course);
            }

            if (year != null && !year.isEmpty()) {
               if (info.length() > 0)
                  info.append(" • ");
               info.append(year).append(" Year");
            }
            break;

         case "Alumni":
            String alumniCourse = user.getCourse();
            String jobRole = user.getJobRole();
            String company = user.getCompany();

            info.append("Alumni");

            if (alumniCourse != null && !alumniCourse.isEmpty()) {
               info.append(" • ").append(alumniCourse);
            }

            if (jobRole != null && !jobRole.isEmpty()) {
               if (company != null && !company.isEmpty()) {
                  info.append(" • ").append(jobRole).append(" at ").append(company);
               } else {
                  info.append(" • ").append(jobRole);
               }
            }
            break;

         case "Professor":
            String department = user.getCourse(); // Using course for department

            info.append("Professor");

            if (department != null && !department.isEmpty()) {
               info.append(" • ").append(department);
            } else {
               info.append(" at Allen Business School");
            }
            break;

         default:
            return userType;
      }

      return info.toString();
   }

   private void checkFollowStatus(@NonNull viewHolder holder, userModel userModel) {
      database.getReference()
            .child("Users")
            .child(userModel.getID())
            .child("Followers")
            .child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                  if (snapshot.exists()) {
                     // User is following - show unfollow option
                     holder.binding.followTV.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                     holder.binding.followTV.setText("Following");
                     holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.textColor));

                     // Add click listener for unfollow
                     holder.binding.followTV.setOnClickListener(v -> {
                        unfollowUser(holder, userModel);
                     });
                  } else {
                     // User is not following - show follow option
                     holder.binding.followTV.setBackgroundResource(R.drawable.gradient_btn);
                     holder.binding.followTV.setText("Follow");
                     holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.white));

                     // Add click listener for follow
                     holder.binding.followTV.setOnClickListener(v -> {
                        followUser(holder, userModel);
                     });
                  }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {
                  Toast.makeText(context, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
               }
            });
   }

   private void followUser(@NonNull viewHolder holder, userModel userModel) {
      long currentTime = new Date().getTime();

      // 1. Create Follower node in other user's profile
      followerModel follower = new followerModel();
      follower.setFollowedBy(auth.getUid());
      follower.setFollowedAt(currentTime);

      // 2. Create Following node in current user's profile
      followingModel following = new followingModel();
      following.setFollowingTo(userModel.getID());
      following.setFollowingAt(currentTime);

      // Disable the button during operation
      holder.binding.followTV.setEnabled(false);

      // Update UI immediately to prevent jitter
      holder.binding.followTV.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
      holder.binding.followTV.setText("Following");
      holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.textColor));

      // Start transaction to update both nodes
      database.getReference().child("Users")
            .child(userModel.getID())
            .child("Followers")
            .child(auth.getUid())
            .setValue(follower).addOnSuccessListener(unused -> {
               // Update following count of current user
               database.getReference().child("Users")
                     .child(auth.getUid())
                     .child("Following")
                     .child(userModel.getID())
                     .setValue(following).addOnSuccessListener(unused1 -> {
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
                                          .addOnSuccessListener(unused2 -> {
                                             // Re-enable button after operation completes
                                             holder.binding.followTV.setEnabled(true);

                                             // Update click listener for unfollow
                                             holder.binding.followTV.setOnClickListener(v -> {
                                                unfollowUser(holder, userModel);
                                             });

                                             Toast.makeText(context, "You followed " + userModel.getName(),
                                                   Toast.LENGTH_SHORT).show();
                                          }).addOnFailureListener(e -> {
                                             // Revert UI on failure
                                             holder.binding.followTV.setEnabled(true);
                                             holder.binding.followTV.setBackgroundResource(R.drawable.gradient_btn);
                                             holder.binding.followTV.setText("Follow");
                                             holder.binding.followTV
                                                   .setTextColor(context.getResources().getColor(R.color.white));

                                             Toast.makeText(context,
                                                   "Failed to update follower count: " + e.getMessage(),
                                                   Toast.LENGTH_SHORT).show();
                                          });
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError error) {
                                    // Revert UI on failure
                                    holder.binding.followTV.setEnabled(true);
                                    holder.binding.followTV.setBackgroundResource(R.drawable.gradient_btn);
                                    holder.binding.followTV.setText("Follow");
                                    holder.binding.followTV
                                          .setTextColor(context.getResources().getColor(R.color.white));

                                    Toast.makeText(context, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT)
                                          .show();
                                 }
                              });
                     }).addOnFailureListener(e -> {
                        // Revert UI on failure
                        holder.binding.followTV.setEnabled(true);
                        holder.binding.followTV.setBackgroundResource(R.drawable.gradient_btn);
                        holder.binding.followTV.setText("Follow");
                        holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.white));

                        Toast.makeText(context, "Failed to update following: " + e.getMessage(), Toast.LENGTH_SHORT)
                              .show();
                     });
            }).addOnFailureListener(e -> {
               // Revert UI on failure
               holder.binding.followTV.setEnabled(true);
               holder.binding.followTV.setBackgroundResource(R.drawable.gradient_btn);
               holder.binding.followTV.setText("Follow");
               holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.white));

               Toast.makeText(context, "Failed to follow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
   }

   private void unfollowUser(@NonNull viewHolder holder, userModel userModel) {
      // Disable the button during operation
      holder.binding.followTV.setEnabled(false);

      // Update UI immediately to prevent jitter
      holder.binding.followTV.setBackgroundResource(R.drawable.gradient_btn);
      holder.binding.followTV.setText("Follow");
      holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.white));

      // Remove follower relationship
      database.getReference().child("Users")
            .child(userModel.getID())
            .child("Followers")
            .child(auth.getUid())
            .removeValue()
            .addOnSuccessListener(unused -> {
               // Remove following relationship
               database.getReference().child("Users")
                     .child(auth.getUid())
                     .child("Following")
                     .child(userModel.getID())
                     .removeValue()
                     .addOnSuccessListener(unused1 -> {
                        // Update follower count
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
                                    if (currentCount > 0) {
                                       database.getReference().child("Users")
                                             .child(userModel.getID())
                                             .child("followersCount")
                                             .setValue(currentCount - 1)
                                             .addOnSuccessListener(unused2 -> {
                                                // Re-enable button after operation completes
                                                holder.binding.followTV.setEnabled(true);

                                                // Update click listener for follow
                                                holder.binding.followTV.setOnClickListener(v -> {
                                                   followUser(holder, userModel);
                                                });

                                                Toast.makeText(context, "Unfollowed " + userModel.getName(),
                                                      Toast.LENGTH_SHORT).show();
                                             }).addOnFailureListener(e -> {
                                                // Revert UI on failure
                                                holder.binding.followTV.setEnabled(true);
                                                holder.binding.followTV.setBackgroundColor(
                                                      ContextCompat.getColor(context, R.color.transparent));
                                                holder.binding.followTV.setText("Following");
                                                holder.binding.followTV
                                                      .setTextColor(context.getResources().getColor(R.color.textColor));

                                                Toast.makeText(context,
                                                      "Failed to update follower count: " + e.getMessage(),
                                                      Toast.LENGTH_SHORT).show();
                                             });
                                    }
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError error) {
                                    // Revert UI on failure
                                    holder.binding.followTV.setEnabled(true);
                                    holder.binding.followTV
                                          .setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                                    holder.binding.followTV.setText("Following");
                                    holder.binding.followTV
                                          .setTextColor(context.getResources().getColor(R.color.textColor));

                                    Toast.makeText(context, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT)
                                          .show();
                                 }
                              });
                     }).addOnFailureListener(e -> {
                        // Revert UI on failure
                        holder.binding.followTV.setEnabled(true);
                        holder.binding.followTV
                              .setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                        holder.binding.followTV.setText("Following");
                        holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.textColor));

                        Toast.makeText(context, "Failed to unfollow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                     });
            }).addOnFailureListener(e -> {
               // Revert UI on failure
               holder.binding.followTV.setEnabled(true);
               holder.binding.followTV.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
               holder.binding.followTV.setText("Following");
               holder.binding.followTV.setTextColor(context.getResources().getColor(R.color.textColor));

               Toast.makeText(context, "Failed to unfollow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
   }

   // Method to navigate to the profile fragment
   private void navigateToProfile(String userId) {
      if (context instanceof androidx.fragment.app.FragmentActivity) {
         androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) context;

         // Create new profile fragment instance with the user ID
         com.ramascript.allenconnect.features.user.profileFragment profileFragment = com.ramascript.allenconnect.features.user.profileFragment
               .newInstance(userId);

         // Replace the current fragment with the profile fragment
         activity.getSupportFragmentManager()
               .beginTransaction()
               .replace(R.id.container, profileFragment)
               .addToBackStack("communityFragment") // Use a named back stack for better control
               .commit();
      }
   }

   @Override
   public int getItemCount() {
      return list.size();
   }

   public class viewHolder extends RecyclerView.ViewHolder {

      RvCommStudentBinding binding;

      ImageView profilePic;
      TextView name, profession, followerCount;

      public viewHolder(@NonNull View itemView) {
         super(itemView);
         binding = RvCommStudentBinding.bind(itemView);
         profilePic = binding.studentPic;
         name = binding.name;
         profession = binding.professionTV;
      }
   }
}