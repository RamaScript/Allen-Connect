package com.ramascript.allenconnect.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.ramascript.allenconnect.Models.PostModel;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.ramascript.allenconnect.databinding.FragmentPostBinding;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class PostFragment extends Fragment {

    FragmentPostBinding binding;
    Uri uri;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentPostBinding.inflate(inflater,container,false);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Post Uploading");
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // here i am fetching user data from db and showing in post fragment
        database.getReference()
            .child("Users")
            .child(auth.getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    Picasso.get()
                            .load(userModel.getProfilePhoto())
                            .placeholder(R.drawable.ic_avatar)
                            .into(binding.profileImage);
                    binding.name.setText(userModel.getName());

                    // Set the text based on user type
                    if ( "Student".equals(userModel.getUserType())) {
                        binding.title.setText(userModel.getCourse() + " (" + userModel.getYear() + " year)");
                    } else if ("Alumni".equals(userModel.getUserType())) {
                        binding.title.setText(userModel.getJobRole() + " at " + userModel.getCompany());
                    } else if ("Professor".equals(userModel.getUserType())) {
                        binding.title.setText("Professor at AGOI");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.caption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String caption = binding.caption.getText().toString();
                if(!caption.isEmpty()){
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.btnbg));
                    binding.postBtn.setEnabled(true);
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.white_my));
                }else {
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.border_black));
                    binding.postBtn.setEnabled(false);
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,10);
            }
        });

        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                final StorageReference reference = storage.getReference().child("Posts")
                        .child(auth.getUid())
                        .child(new Date().getTime()+" ");
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                PostModel postModel = new PostModel();
                                postModel.setPostImage(uri.toString());
                                postModel.setPostedBy(auth.getUid());
                                postModel.setPostCaption(binding.caption.getText().toString());
                                postModel.setPostedAt(new Date().getTime());

                                database.getReference().child("Posts")
                                        .push()
                                        .setValue(postModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "Posted Sucessfully", Toast.LENGTH_SHORT).show();

                                        // Replace PostFragment with HomeFragment
                                        Fragment homeFragment = new HomeFragment();
                                        getActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.container, homeFragment) // Replace with your container's id
                                                .addToBackStack(null) // Optional, if you want to add it to the back stack
                                                .commit();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data.getData()!=null){
            uri = data.getData();
            binding.postImage.setImageURI(uri);
            binding.postImage.setVisibility(View.VISIBLE);

            binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.btnbg));
            binding.postBtn.setEnabled(true);
            binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.white_my));
        }

    }
}