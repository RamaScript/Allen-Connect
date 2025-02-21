package com.ramascript.allenconnect.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ramascript.allenconnect.Activities.EditProfileActivity;
import com.ramascript.allenconnect.Adapters.FollowerAdapter;
import com.ramascript.allenconnect.Chat.ChatDetailActivity;
import com.ramascript.allenconnect.Features.MeetDevsActivity;
import com.ramascript.allenconnect.Models.FollowerModel;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentProfileBinding;
import com.ramascript.allenconnect.userAuth.LoginAs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ValueEventListener postsCountListener;
    ArrayList<FollowerModel> list;
    ProgressDialog dialog;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Changing Profile Picture");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Setup UI and listeners
        setupProfileMenu();
        loadProfileData();

        list = new ArrayList<>();

        FollowerAdapter adapter = new FollowerAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        binding.friendRV.setLayoutManager(layoutManager); // Using binding
        binding.friendRV.setAdapter(adapter); // Using binding

        database.getReference().child("Users").child(auth.getUid()).child("Followers")
                .addValueEventListener(new ValueEventListener() {
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

        // Handle back button press
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Show the confirmation dialog
                        new AlertDialog.Builder(getContext()).setMessage("Do you want to leave the app?")
                                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Finish the activity, closing the app
                                        requireActivity().finish();
                                    }
                                }).setNegativeButton("No", null) // Just dismiss the dialog
                                .show();
                    }
                });

        return binding.getRoot();
    }

    private void setupProfileMenu() {
        binding.profileSettingsMenuBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit_profile) {
                    startActivity(new Intent(getActivity(), EditProfileActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.action_change_profile_picture) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 11);
                    return true;
                } else if (item.getItemId() == R.id.action_developers) {
                    Intent i = new Intent(getContext(), MeetDevsActivity.class);
                    startActivity(i);
                    return true;
                } else if (item.getItemId() == R.id.action_logout) {
                    auth.signOut();
                    Intent intent = new Intent(getActivity(), LoginAs.class);
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private void loadProfileData() {
        String uid = auth.getCurrentUser().getUid();

        // Create the listener
        postsCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) { // Check if binding is still valid
                    long postCount = snapshot.getChildrenCount();
                    binding.postsCountTV.setText(String.valueOf(postCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };

        // Attach the listener
        database.getReference().child("posts").child(uid)
                .addValueEventListener(postsCountListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                            database.getReference().child("Users").child(auth.getUid()).child("profilePhoto")
                                    .setValue(uri.toString());
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the listener and clear the binding
        if (auth.getCurrentUser() != null && postsCountListener != null) {
            database.getReference().child("posts")
                    .child(auth.getCurrentUser().getUid())
                    .removeEventListener(postsCountListener);
        }
        binding = null;
    }

}
