package com.example.instagram_project;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.instagram_project.models.Post;
import com.example.instagram_project.utils.FirebaseAuthHelper;
import com.example.instagram_project.utils.FirebaseFirestoreHelper;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etImageUrl, etCaption;
    private Button btnSubmitPost;
    private ProgressBar progressBar;

    private FirebaseAuthHelper authHelper;
    private FirebaseFirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Initialize helpers
        authHelper = new FirebaseAuthHelper(this);
        firestoreHelper = new FirebaseFirestoreHelper(this);

        // Find views
        etImageUrl = findViewById(R.id.etImageUrl);
        etCaption = findViewById(R.id.etCaption);
        btnSubmitPost = findViewById(R.id.btnSubmitPost);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener
        btnSubmitPost.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String imageUrl = etImageUrl.getText().toString().trim();
        String caption = etCaption.getText().toString().trim();

        // Use a default placeholder if URL is empty
        if (TextUtils.isEmpty(imageUrl)) {
            imageUrl = "https://via.placeholder.com/400x400?text=Post+Image"; // Default
        }

        if (TextUtils.isEmpty(caption)) {
            Toast.makeText(this, "Please enter a caption", Toast.LENGTH_SHORT).show();
            return;
        }

        if (authHelper.getCurrentUser() == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress and disable button
        progressBar.setVisibility(View.VISIBLE);
        btnSubmitPost.setEnabled(false);

        // Call Firestore save method directly
        createPostInFirestore(caption, imageUrl);
    }

    // Saves post data (caption + image URL) to Firestore
    private void createPostInFirestore(String caption, String imageUrl) {
        String postId = firestoreHelper.generateDocumentId("posts");
        String userId = authHelper.getCurrentUser().getUid();
        String username = authHelper.getCurrentUser().getEmail(); // TODO: Get real username

        Post post = new Post(postId, userId, username, imageUrl, caption);

        firestoreHelper.createPost(post, new FirebaseFirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreatePostActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close activity and return to home feed
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnSubmitPost.setEnabled(true); // Re-enable button on failure
                Toast.makeText(CreatePostActivity.this, "Failed to create post: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}