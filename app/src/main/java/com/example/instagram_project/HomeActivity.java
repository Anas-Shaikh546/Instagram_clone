package com.example.instagram_project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.instagram_project.models.Post;
import com.example.instagram_project.utils.FirebaseAuthHelper;
import com.example.instagram_project.utils.FirebaseFirestoreHelper;
import com.example.instagram_project.utils.SpamDetectionHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuthHelper authHelper;
    private FirebaseFirestoreHelper firestoreHelper;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        authHelper = new FirebaseAuthHelper(this);
        firestoreHelper = new FirebaseFirestoreHelper(this);

        if (!authHelper.isUserLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        // You can set the "Instagram" text here if you want
        // tvWelcome.setText("Instagram");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // This line connects to your BottomNavigationView
        bottomNav.setOnItemSelectedListener(navListener);

        // Load the home fragment by default
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, new HomeFragment()).commit();
    }
    // ADD THIS METHOD to HomeActivity.java
    public void navigateToHome() {
        // Find the bottom navigation bar and select the home tab
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_home);

        // Switch the fragment back to HomeFragment
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, new HomeFragment()).commit();
    }

    // This listener handles the clicks on your navigation bar
    private NavigationBarView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();

                    if (itemId == R.id.navigation_home) {
                        selectedFragment = new HomeFragment();
                    }   else if (itemId == R.id.navigation_add_post) {
                    // Launch the new CreatePostActivity
                    Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
                    startActivity(intent);
                    return false; // Don't select the "Post" tab

                }else if (itemId == R.id.navigation_profile) {
                        // Show the new ProfileFragment
                        selectedFragment = new ProfileFragment();
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(
                                R.id.fragment_container, selectedFragment).commit();
                    }
                    return true;
                }
            };

    private void logoutUser() {
        // Clear spam history for this user before logout
        String userId = authHelper.getCurrentUser().getUid();
        SpamDetectionHelper.getInstance().clearUserHistory(userId);

        authHelper.logout();
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
        finish();
    }

    // This code stays in HomeActivity
    private void showCreatePostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Post");

        final EditText etCaption = new EditText(this);
        etCaption.setHint("Enter caption...");

        final EditText etImageUrl = new EditText(this);
        etImageUrl.setHint("Enter image URL (placeholder)");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(etImageUrl);
        layout.addView(etCaption);

        builder.setView(layout);

        builder.setPositiveButton("Post", (dialog, which) -> {
            String caption = etCaption.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();

            if (imageUrl.isEmpty()) {
                imageUrl = "https://via.placeholder.com/400x400?text=Post+Image";
            }

            if (!caption.isEmpty()) {
                createPost(caption, imageUrl);
            } else {
                Toast.makeText(this, "Please enter a caption", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createPost(String caption, String imageUrl) {
        String postId = firestoreHelper.generateDocumentId("posts");
        String userId = authHelper.getCurrentUser().getUid();
        String username = authHelper.getCurrentUser().getEmail(); // Using email as username for now

        Post post = new Post(postId, userId, username, imageUrl, caption);

        firestoreHelper.createPost(post, new FirebaseFirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(HomeActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                // Refresh the posts list by reloading the fragment
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container, new HomeFragment()).commit();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(HomeActivity.this, "Failed to create post: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}