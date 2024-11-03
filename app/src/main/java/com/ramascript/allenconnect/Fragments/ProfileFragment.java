package com.ramascript.allenconnect.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ramascript.allenconnect.Adapters.FollowerAdapter;
import com.ramascript.allenconnect.LoginAs;
import com.ramascript.allenconnect.MeetDevsActivity;
import com.ramascript.allenconnect.Models.FollowerModel;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.ramascript.allenconnect.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    ArrayList<FollowerModel> list;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    FragmentProfileBinding binding; // Declare the binding
    ProgressDialog dialog;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize the binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Changing Profile Picture");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Fetch the profile photo from Firebase Database
        database.getReference().child("Users").child(auth.getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        UserModel userModel = snapshot.getValue(UserModel.class);
                        Picasso.get()
                            .load(userModel.getProfilePhoto())
                            .placeholder(R.drawable.ic_avatar)
                            .into(binding.profileImage); // Using binding


                        binding.name.setText(userModel.getName());

                        if (!snapshot.getChildren().equals(auth.getUid()) && "Student".equals(userModel.getUserType())) {

                            binding.professionTV.setText(userModel.getCourse() + "(" + userModel.getYear() + " year)");
                            binding.BioTV.setText("Hi, I am " + userModel.getName() + " and i am a " + userModel.getCourse() + " (" + userModel.getYear() + " year) student.");

                        } else if (!snapshot.getChildren().equals(auth.getUid()) && "Alumni".equals(userModel.getUserType())) {

                            binding.professionTV.setText(userModel.getJobRole() + " at " + userModel.getCompany());
                            binding.BioTV.setText("Hi, I am " + userModel.getName() + " and i am a " + userModel.getCourse() + " (" + userModel.getPassingYear() + ") passout. currently working at " + userModel.getCompany() + " as " + userModel.getJobRole());

                        } else if (!snapshot.getChildren().equals(auth.getUid()) && "Professor".equals(userModel.getUserType())) {

                            binding.professionTV.setText("Professor at AGOI");
                            binding.BioTV.setText("Hi, I am " + userModel.getName() + " and i am a professor you can ask me about my subject in my DM or call me at " + userModel.getPhoneNo());

                        }
                        binding.followersCountTV.setText(userModel.getFollowersCount() + "");

                        // Fetch the number of posts made by the user and display in postsCountTV
                        database.getReference().child("Posts")
                            .orderByChild("postedBy")
                            .equalTo(auth.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int postCount = (int) snapshot.getChildrenCount();  // Count the number of posts
                                    binding.postsCountTV.setText(String.valueOf(postCount));  // Set the post count in the TextView
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        list = new ArrayList<>();

        FollowerAdapter adapter = new FollowerAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.friendRV.setLayoutManager(layoutManager);  // Using binding
        binding.friendRV.setAdapter(adapter);  // Using binding

        database.getReference().child("Users")
            .child(auth.getUid())
            .child("Followers").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    long followerCount = snapshot.getChildrenCount();

                    if (followerCount == 0) {
                        // Hide "My Followers" TextView if there are no followers
                        binding.myFollowersTV.setVisibility(View.GONE);
                        binding.friendRV.setVisibility(View.GONE);
                    } else {
                        // Show "My Followers" TextView if there are followers
                        binding.myFollowersTV.setVisibility(View.VISIBLE);
                        binding.friendRV.setVisibility(View.VISIBLE);
                    }

                    binding.followersCountTV.setText(String.valueOf(followerCount));

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        FollowerModel followerModel = dataSnapshot.getValue(FollowerModel.class);
                        list.add(followerModel);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        // Profile settings menu button click event
        binding.profileSettingsMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
//                        if (item.getItemId() == R.id.action_edit_profile) {
//                            Toast.makeText(getContext(), "in Developement phase...", Toast.LENGTH_SHORT).show();
//                            return true;
//                        } else
                        if (item.getItemId() == R.id.action_change_profile_picture) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, 11);
                            return true;
                        }
//                        else if (item.getItemId() == R.id.action_settings) {
//                            Toast.makeText(getContext(), "in Developement Phase...", Toast.LENGTH_SHORT).show();
//                            return true;
//                        }
                        else if (item.getItemId() == R.id.action_developers) {
                            Intent i = new Intent(getContext(), MeetDevsActivity.class);
                            startActivity(i);
                            return true;
                        } else if (item.getItemId() == R.id.action_logout) {
                            auth.signOut();
                            Intent intent = new Intent(getActivity(), LoginAs.class);
                            startActivity(intent);
                            getActivity().finish();
                            return true;
                        } else {
                            return false;
                        }
                        }
                    });
                popupMenu.show();
                }
            });
        return view;
        }

        @Override
        public void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                binding.profileImage.setImageURI(uri); // Using binding

                // Show the dialog after the image is selected
                dialog.show();

                final StorageReference reference = storage.getReference().child("profile_pictures")
                    .child(FirebaseAuth.getInstance().getUid());

                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "Profile photo changed", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users")
                                    .child(auth.getUid()).child("profilePhoto").setValue(uri.toString());
                            }
                        });
                    }
                });
            }
        }

    }
