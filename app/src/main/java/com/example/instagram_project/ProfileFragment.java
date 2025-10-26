package com.example.instagram_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_project.adapters.PostAdapter;
import com.example.instagram_project.models.Post;
import com.example.instagram_project.models.User;
import com.example.instagram_project.utils.FirebaseAuthHelper;
import com.example.instagram_project.utils.FirebaseFirestoreHelper;
import com.example.instagram_project.utils.SpamDetectionHelper;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FirebaseAuthHelper authHelper;
    private FirebaseFirestoreHelper firestoreHelper;

    // Views
    private TextView tvToolbarUsername, tvPostsCount, tvFollowersCount, tvFollowingCount, tvBio;
    private ImageView ivProfileImage, ivBack;
    private Button btnLogout;
    private RecyclerView recyclerViewUserPosts;

    private PostAdapter postAdapter;
    private List<Post> userPosts;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize helpers
        authHelper = new FirebaseAuthHelper(getContext());
        firestoreHelper = new FirebaseFirestoreHelper(getContext());

        // Find all views
        tvToolbarUsername = view.findViewById(R.id.tvToolbarUsername);
        tvPostsCount = view.findViewById(R.id.tvPostsCount);
        tvFollowersCount = view.findViewById(R.id.tvFollowersCount);
        tvFollowingCount = view.findViewById(R.id.tvFollowingCount);
        tvBio = view.findViewById(R.id.tvBio);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        ivBack = view.findViewById(R.id.ivBack);
        btnLogout = view.findViewById(R.id.btnLogout);
        recyclerViewUserPosts = view.findViewById(R.id.recyclerViewUserPosts);

        // Setup click listeners
        btnLogout.setOnClickListener(v -> logoutUser());
        ivBack.setOnClickListener(v -> {
            // Go back to the HomeFragment by calling a method in HomeActivity
            if (getActivity() != null) {
                ((HomeActivity) getActivity()).navigateToHome();
            }
        });

        // Load user data
        loadUserData();

        // Setup RecyclerView
        setupRecyclerView();

        // Load user's posts
        loadUserPosts();

        return view;
    }
    private void loadUserData() {
        // Ensure authHelper isn't null and user is logged in
        if (authHelper == null || authHelper.getCurrentUser() == null || getContext() == null) {
            Log.e("ProfileFragment", "AuthHelper/User/Context is null in loadUserData");
            return; // Cannot load data without helper or user
        }

        FirebaseUser fUser = authHelper.getCurrentUser();
        String userId = fUser.getUid();
        Log.d("ProfileFragment", "Loading user data for ID: " + userId); // Log user ID

        // Get user data document from Firestore
        firestoreHelper.getUser(userId, new FirebaseFirestoreHelper.FirestoreDataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // Successfully fetched the User object
                Log.d("ProfileFragment", "User data loaded successfully. Username: " + user.getUsername()); // Log success
                tvToolbarUsername.setText(user.getUsername());
                // You can add more fields here later, like full name
                tvBio.setText("Welcome to my profile, " + user.getUsername() + "! 👋");

                // TODO: Load real profile image using Glide
                // if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty() && getContext() != null) {
                //     Glide.with(getContext()).load(user.getProfileImageUrl()).into(ivProfileImage);
                // } else {
                //     ivProfileImage.setImageResource(R.drawable.ic_profile); // Set default if no URL
                // }
            }

            @Override
            public void onFailure(String error) {
                // Failed to load the user document from Firestore
                Log.e("ProfileFragment", "Failed to load user data: " + error); // Log failure
                // Fallback to using email if the Firestore fetch fails
                tvToolbarUsername.setText(fUser.getEmail()); // Show email in toolbar
                tvBio.setText("Welcome to my profile! 👋"); // Default bio
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load profile details: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set static follower/following counts for now
        tvFollowersCount.setText("0");
        tvFollowingCount.setText("0");
    }


    private void setupRecyclerView() {
        userPosts = new ArrayList<>();
        // We re-use the same PostAdapter, which shows the full post.
        // A real Instagram grid would use a different, simpler adapter.
        postAdapter = new PostAdapter(userPosts, getContext(), authHelper, firestoreHelper);
        recyclerViewUserPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUserPosts.setAdapter(postAdapter);
    }

    private void loadUserPosts() {
        // Ensure authHelper isn't null and user is logged in
        if (authHelper == null || authHelper.getCurrentUser() == null || getContext() == null) {
            Log.e("ProfileFragment", "AuthHelper/User/Context is null in loadUserPosts");
            return;
        }
        String userId = authHelper.getCurrentUser().getUid();
        Log.d("ProfileFragment", "Attempting to load posts for user ID: " + userId); // <-- Add log

        firestoreHelper.getUserPosts(userId, new FirebaseFirestoreHelper.FirestoreListCallback<Post>() {
            @Override
            public void onSuccess(List<Post> postList) {
                // --- ADD LOGS ---
                Log.d("ProfileFragment", "Successfully loaded " + postList.size() + " posts for this user.");
                if (postList.isEmpty()) {
                    Log.w("ProfileFragment", "User post list is empty.");
                } else if(postList.get(0) != null) {
                    Log.d("ProfileFragment", "First user post caption: " + postList.get(0).getCaption());
                }
                // --- END LOGS ---

                userPosts.clear();
                userPosts.addAll(postList);
                postAdapter.notifyDataSetChanged();
                Log.d("ProfileFragment", "User post adapter notified. Item count: " + postAdapter.getItemCount()); // <-- Add log

                // Update the "Posts" count text view
                tvPostsCount.setText(String.valueOf(postList.size()));
            }

            @Override
            public void onFailure(String error) {
                Log.e("ProfileFragment", "Failed to load user posts: " + error); // <-- Add log
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load posts: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logoutUser() {
        if (authHelper.getCurrentUser() != null) {
            String userId = authHelper.getCurrentUser().getUid();
            SpamDetectionHelper.getInstance().clearUserHistory(userId);
        }
        authHelper.logout();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}