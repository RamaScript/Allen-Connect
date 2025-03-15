package com.ramascript.allenconnect.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;

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
import com.ramascript.allenconnect.userAuth.LoginAs;
import com.ramascript.allenconnect.Features.MeetDevsActivity;
import com.ramascript.allenconnect.Models.FollowerModel;
import com.ramascript.allenconnect.Models.UserModel;
import com.ramascript.allenconnect.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.ramascript.allenconnect.databinding.FragmentProfileBinding;
import com.google.android.material.tabs.TabLayout;
import com.ramascript.allenconnect.Models.PostModel;
import com.ramascript.allenconnect.Adapters.PostAdapter;

public class ProfileFragment extends Fragment {

    ArrayList<FollowerModel> list;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    FragmentProfileBinding binding; // Declare the binding
    ProgressDialog dialog;
    private ViewPager2 viewPager;
    private UserModel currentUser;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        setupProgressDialog();
        setupViewPager();
        loadUserData();
        setupClickListeners();

        return binding.getRoot();
    }

    private void setupProgressDialog() {
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Changing Profile Picture");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    private void setupViewPager() {
        viewPager = binding.viewPager;
        ProfilePagerAdapter pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Posts");
                            break;
                        case 1:
                            tab.setText("Details");
                            break;
                    }
                }).attach();

        // Add page change callback to handle height
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) { // Details tab
                    viewPager.post(() -> {
                        // Get the current fragment
                        Fragment fragment = getChildFragmentManager()
                                .findFragmentByTag("f" + position);
                        if (fragment != null && fragment.getView() != null) {
                            // Measure the details content
                            fragment.getView().measure(
                                    View.MeasureSpec.makeMeasureSpec(viewPager.getWidth(), View.MeasureSpec.EXACTLY),
                                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                            // Set the height to wrap the content
                            ViewGroup.LayoutParams params = viewPager.getLayoutParams();
                            params.height = fragment.getView().getMeasuredHeight();
                            viewPager.setLayoutParams(params);
                        }
                    });
                } else { // Posts tab
                    ViewGroup.LayoutParams params = viewPager.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    viewPager.setLayoutParams(params);
                }
            }
        });

        // Disable swipe
        viewPager.setUserInputEnabled(false);
    }

    private void loadUserData() {
        String uid = auth.getCurrentUser().getUid();
        Log.d("ProfileFragment", "Loading user data for uid: " + uid);

        // First load the user data
        database.getReference()
                .child("Users")
                .child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding != null && snapshot.exists()) {
                            currentUser = snapshot.getValue(UserModel.class);
                            if (currentUser != null) {
                                // Always use the snapshot key as the user ID
                                String userId = snapshot.getKey();
                                Log.d("ProfileFragment", "User data loaded successfully with ID: " + userId);
                                updateUIWithUserData(currentUser, userId);

                                // Load counts after user data is loaded
                                loadFollowersCount(uid);
                                loadPostsCount(uid);
                                loadFollowingCount(uid);

                                // Notify adapter about user data change
                                if (viewPager != null && viewPager.getAdapter() != null) {
                                    viewPager.getAdapter().notifyDataSetChanged();
                                }
                            } else {
                                Log.e("ProfileFragment", "User data is null");
                            }
                        } else {
                            Log.e("ProfileFragment", "Binding is null or snapshot doesn't exist");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ProfileFragment", "Error loading user data: " + error.getMessage());
                    }
                });
    }

    private void updateUIWithUserData(UserModel user, String userId) {
        if (user == null || userId == null) {
            Log.e("ProfileFragment", "User object or userId is null");
            return;
        }

        // Profile Image
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
            Picasso.get()
                    .load(user.getProfilePhoto())
                    .placeholder(R.drawable.ic_avatar)
                    .into(binding.profileImage);
        }

        // Name and Bio
        binding.name.setText(user.getName());

        // Set username/profession based on user type
        String professionText = "";
        String bioText = "";

        switch (user.getUserType()) {
            case "Student":
                professionText = "@" + user.getCRN().toLowerCase();
                bioText = String.format("%s • %s Year\n%s Student at Allen Business School",
                        user.getCourse(), user.getYear(), user.getCourse());
                break;
            case "Professor":
//                professionText = "Professor at Allen Business School";

                bioText = String.format("Professor at Allen Business School", user.getCourse());
                break;
            case "Alumni":
                professionText = String.format("%s at %s", user.getJobRole(), user.getCompany());
                bioText = String.format("Allen Business School Alumni\n%s, %s",
                        user.getCourse(), user.getPassingYear());
                break;
        }

        if (professionText.equals("")){
            binding.professionTV.setVisibility(View.GONE);
        }else {
            binding.professionTV.setVisibility(View.VISIBLE);
            binding.professionTV.setText(professionText);
        }
        binding.BioTV.setText(bioText);

        // Handle follow button visibility
        String currentUserId = auth.getCurrentUser().getUid();

        Log.d("ProfileFragment", "Current User ID: " + currentUserId);
        Log.d("ProfileFragment", "Profile User ID: " + userId);

        if (currentUserId.equals(userId)) {
            // This is the logged-in user's own profile
            binding.followButton.setVisibility(View.GONE);
            binding.profileSettingsMenuBtn.setVisibility(View.VISIBLE);
        } else {
            // This is someone else's profile
            binding.followButton.setVisibility(View.VISIBLE);
            binding.profileSettingsMenuBtn.setVisibility(View.GONE);

            // Check if already following
            checkFollowingStatus(userId);
        }
    }

    private void checkFollowingStatus(String profileUserId) {
        if (profileUserId == null || profileUserId.isEmpty()) {
            Log.e("ProfileFragment", "Invalid profile user ID");
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e("ProfileFragment", "Current user ID is null");
            return;
        }

        Log.d("ProfileFragment", "Checking following status: current=" + currentUserId + ", profile=" + profileUserId);

        database.getReference()
                .child("Users")
                .child(currentUserId)
                .child("Following")
                .child(profileUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding != null) {
                            if (snapshot.exists()) {
                                // Already following
                                binding.followButton.setText("Following");
                                binding.followButton.setBackgroundResource(R.drawable.border_black);
                            } else {
                                // Not following
                                binding.followButton.setText("Follow");
                                binding.followButton.setBackgroundResource(R.drawable.btnbg);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ProfileFragment", "Error checking following status: " + error.getMessage());
                    }
                });
    }

    private void loadFollowersCount(String uid) {
        Log.d("ProfileFragment", "Loading followers count for uid: " + uid);

        database.getReference()
            .child("Users")
            .child(uid)
            .child("Followers") // Counting child nodes inside "followers"
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = (int) snapshot.getChildrenCount(); // Count child nodes
                    Log.d("ProfileFragment", "Followers count from DB: " + count);

                    if (binding != null) {
                        binding.followersCountTV.setText(String.valueOf(count)); // Update UI
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ProfileFragment", "Error loading followers count: " + error.getMessage());
                    if (binding != null) {
                        binding.followersCountTV.setText("0");
                    }
                }
            });
    }

    private void loadFollowingCount(String uid) {
        Log.d("ProfileFragment", "Loading following count for uid: " + uid);

        database.getReference()
            .child("Users")
            .child(uid)
            .child("Following") // Counting child nodes inside "following"
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = (int) snapshot.getChildrenCount(); // Count child nodes
                    Log.d("ProfileFragment", "Following count from DB: " + count);

                    if (binding != null) {
                        binding.followingCountTV.setText(String.valueOf(count)); // Update UI
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ProfileFragment", "Error loading following count: " + error.getMessage());
                    if (binding != null) {
                        binding.followingCountTV.setText("0");
                    }
                }
            });
    }


    private void loadPostsCount(String uid) {
        database.getReference()
                .child("Posts")
                .orderByChild("postedBy")
                .equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding != null) {
                            long count = snapshot.getChildrenCount();
                            binding.postsCountTV.setText(String.valueOf(count));

                            // Update posts count in database
                            database.getReference()
                                    .child("Users")
                                    .child(uid)
                                    .child("postsCount")
                                    .setValue(count);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ProfileFragment", "Error loading posts count: " + error.getMessage());
                        if (binding != null) {
                            binding.postsCountTV.setText("0");
                        }
                    }
                });
    }



    private void setupClickListeners() {
        binding.profileSettingsMenuBtn.setOnClickListener(v -> showProfileMenu(v));
    }

    private void showProfileMenu(View v) {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit_profile) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.action_change_profile_picture) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 11);
                    return true;
                } else if (item.getItemId() == R.id.action_developers) {
                startActivity(new Intent(getContext(), MeetDevsActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.action_logout) {
                    auth.signOut();
                startActivity(new Intent(getActivity(), LoginAs.class));
                requireActivity().finish();
                    return true;
                }
                return false;
            });
            popupMenu.show();
    }

            @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
                            database.getReference().child("Users")
                                    .child(auth.getUid()).child("profilePhoto").setValue(uri.toString());
                        }
                    });
                }
            });
        }
    }

    // ViewPager2 Adapter
    private class ProfilePagerAdapter extends FragmentStateAdapter {
        public ProfilePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new PostsFragment();
            }
            return new DetailsFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    // Posts Fragment
    public static class PostsFragment extends Fragment {
        private RecyclerView postsRecyclerView;
        private View emptyStateView;
        private ArrayList<PostModel> postList;
        private PostAdapter postAdapter;
        private FirebaseAuth auth;
        private FirebaseDatabase database;
        private String userId;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_posts, container, false);

            // Initialize views and variables
            postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
            emptyStateView = view.findViewById(R.id.emptyStateView);
            auth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            postList = new ArrayList<>();

            // Get the user ID from parent fragment
            ProfileFragment parentFragment = (ProfileFragment) getParentFragment();
            if (parentFragment != null) {
                userId = auth.getCurrentUser().getUid();
            }

            setupRecyclerView();
            loadUserPosts();

            return view;
        }

        private void setupRecyclerView() {
            postAdapter = new PostAdapter(postList, requireContext());
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);
            postsRecyclerView.setLayoutManager(layoutManager);
            postsRecyclerView.setAdapter(postAdapter);
        }

        private void loadUserPosts() {
            if (userId == null)
                return;

            database.getReference()
                    .child("Posts")
                    .orderByChild("postedBy")
                    .equalTo(userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            postList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                try {
                                    PostModel post = new PostModel();
                                    post.setPostId(dataSnapshot.getKey());
                                    post.setPostImage(dataSnapshot.child("postImage").getValue(String.class));
                                    post.setPostCaption(dataSnapshot.child("postCaption").getValue(String.class));
                                    post.setPostedBy(dataSnapshot.child("postedBy").getValue(String.class));

                                    if (dataSnapshot.child("postedAt").exists()) {
                                        Long postedAt = dataSnapshot.child("postedAt").getValue(Long.class);
                                        if (postedAt != null) {
                                            post.setPostedAt(postedAt);
                                        }
                                    }

                                    if (dataSnapshot.child("postLikes").exists()) {
                                        Long postLikes = dataSnapshot.child("postLikes").getValue(Long.class);
                                        post.setPostLikes(postLikes != null ? postLikes.intValue() : 0);
                                    }

                                    // Handle comment count
                                    if (dataSnapshot.child("commentCount").exists()) {
                                        Long commentCount = dataSnapshot.child("commentCount").getValue(Long.class);
                                        post.setCommentCount(commentCount != null ? commentCount.intValue() : 0);
                                    } else {
                                        // If commentCount doesn't exist, count the comments manually
                                        int count = (int) dataSnapshot.child("comments").getChildrenCount();
                                        post.setCommentCount(count);

                                        // Update the commentCount in database
                                        database.getReference()
                                                .child("Posts")
                                                .child(post.getPostId())
                                                .child("commentCount")
                                                .setValue(count);
                                    }

                                    postList.add(post);
                                } catch (Exception e) {
                                    Log.e("PostsFragment", "Error parsing post: " + e.getMessage());
                                }
                            }
                            updateUI();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("PostsFragment", "Error loading posts: " + error.getMessage());
                            Toast.makeText(getContext(), "Error loading posts", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void updateUI() {
            if (postList.isEmpty()) {
                emptyStateView.setVisibility(View.VISIBLE);
                postsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyStateView.setVisibility(View.GONE);
                postsRecyclerView.setVisibility(View.VISIBLE);
                postAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
            // Clean up resources
            postList.clear();
            postsRecyclerView.setAdapter(null);
        }
    }

    // Details Fragment
    public static class DetailsFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.view_user_details, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ProfileFragment parentFragment = (ProfileFragment) getParentFragment();
            if (parentFragment != null && parentFragment.currentUser != null) {
                showUserTypeSpecificDetails(view, parentFragment.currentUser);
            }
        }

        private void showUserTypeSpecificDetails(View view, UserModel user) {
            // Find all common TextViews
            TextView nameTV = view.findViewById(R.id.nameTV);
            TextView emailTV = view.findViewById(R.id.emailTV);
            TextView phoneTV = view.findViewById(R.id.phoneTV);

            // Find all section containers
            View studentFields = view.findViewById(R.id.studentFields);
            View professorFields = view.findViewById(R.id.professorFields);
            View alumniFields = view.findViewById(R.id.alumniFields);

            if (nameTV == null || emailTV == null || phoneTV == null) {
                return; // Safety check
            }

            // Set common fields
            nameTV.setText(user.getName());
            emailTV.setText(user.getEmail());
            phoneTV.setText(user.getPhoneNo());

            // Hide all sections initially
            studentFields.setVisibility(View.GONE);
            professorFields.setVisibility(View.GONE);
            alumniFields.setVisibility(View.GONE);

            // Show and populate fields based on user type
            switch (user.getUserType()) {
                case "Student":
                    studentFields.setVisibility(View.VISIBLE);
                    TextView crnTV = view.findViewById(R.id.crnTV);
                    TextView courseTV = view.findViewById(R.id.courseTV);
                    TextView yearTV = view.findViewById(R.id.yearTV);

                    crnTV.setText(user.getCRN());
                    courseTV.setText(user.getCourse());
                    yearTV.setText(user.getYear());
                    break;

                case "Professor":
                    professorFields.setVisibility(View.VISIBLE);
                    TextView departmentTV = view.findViewById(R.id.departmentTV);
                    departmentTV.setText(user.getCourse()); // Using course field for department
                    break;

                case "Alumni":
                    alumniFields.setVisibility(View.VISIBLE);
                    TextView alumniCourseTV = view.findViewById(R.id.alumniCourseTV);
                    TextView companyTV = view.findViewById(R.id.companyTV);
                    TextView jobRoleTV = view.findViewById(R.id.jobRoleTV);
                    TextView passingYearTV = view.findViewById(R.id.passingYearTV);

                    alumniCourseTV.setText(user.getCourse());
                    companyTV.setText(user.getCompany());
                    jobRoleTV.setText(user.getJobRole());
                    passingYearTV.setText(user.getPassingYear());
                    break;
            }
        }
    }

}