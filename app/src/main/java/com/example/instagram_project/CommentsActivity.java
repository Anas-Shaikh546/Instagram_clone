package com.example.instagram_project;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_project.adapters.CommentAdapter;
import com.example.instagram_project.models.Comment;
import com.example.instagram_project.utils.FirebaseAuthHelper;
import com.example.instagram_project.utils.FirebaseFirestoreHelper;
import com.example.instagram_project.utils.SpamDetectionHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private EditText etAddComment;
    private TextView tvPostComment;
    private ImageView ivBack;

    private FirebaseFirestoreHelper firestoreHelper;
    private FirebaseAuthHelper authHelper;

    private String postId;

    // Map to cache userId → username
    private Map<String, String> userIdToUsername = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        postId = getIntent().getStringExtra("POST_ID");
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestoreHelper = new FirebaseFirestoreHelper(this);
        authHelper = new FirebaseAuthHelper(this);

        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        etAddComment = findViewById(R.id.etAddComment);
        tvPostComment = findViewById(R.id.tvPostComment);
        ivBack = findViewById(R.id.ivBack);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);

        ivBack.setOnClickListener(v -> finish());
        tvPostComment.setOnClickListener(v -> postComment());

        loadComments();
    }

    private void loadComments() {
        Log.d("CommentsActivity", "Loading comments for post ID: " + postId);

        firestoreHelper.getPostComments(postId, new FirebaseFirestoreHelper.FirestoreListCallback<Comment>() {
            @Override
            public void onSuccess(List<Comment> data) {
                commentList.clear();
                commentList.addAll(data);
                commentAdapter.notifyDataSetChanged();

                // Fetch usernames for all comments
                for (Comment comment : commentList) {
                    String userId = comment.getUserId();

                    // Use cached username if available
                    if (userIdToUsername.containsKey(userId)) {
                        comment.setUsername(userIdToUsername.get(userId));
                        commentAdapter.notifyDataSetChanged();
                        continue;
                    }

                    // Fetch username from Firestore
                    FirebaseFirestore.getInstance().collection("users").document(userId).get()
                            .addOnSuccessListener(document -> {
                                String username = "Unknown";
                                if (document.exists() && document.getString("username") != null) {
                                    username = document.getString("username");
                                }
                                userIdToUsername.put(userId, username);
                                comment.setUsername(username);
                                commentAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                comment.setUsername("Unknown");
                                commentAdapter.notifyDataSetChanged();
                            });
                }

                Log.d("CommentsActivity", "Loaded " + commentList.size() + " comments.");
            }

            @Override
            public void onFailure(String error) {
                Log.e("CommentsActivity", "Failed to load comments: " + error);
                Toast.makeText(CommentsActivity.this, "Failed to load comments: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {
        String commentText = etAddComment.getText().toString().trim();

        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (authHelper.getCurrentUser() == null) {
            Toast.makeText(this, "You need to be logged in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = authHelper.getCurrentUser().getUid();

        // Fetch username first
        if (userIdToUsername.containsKey(userId)) {
            String username = userIdToUsername.get(userId);
            addCommentToFirestore(userId, username, commentText);
        } else {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        String username = "Unknown";
                        if (document.exists() && document.getString("username") != null) {
                            username = document.getString("username");
                        }
                        userIdToUsername.put(userId, username);
                        addCommentToFirestore(userId, username, commentText);
                    })
                    .addOnFailureListener(e -> {
                        addCommentToFirestore(userId, "Unknown", commentText);
                    });
        }
    }

    private void addCommentToFirestore(String userId, String username, String commentText) {
        SpamDetectionHelper spamHelper = SpamDetectionHelper.getInstance();
        if (!spamHelper.checkSpamAndRecordComment(userId, this)) {
            finish();
            return;
        }

        int remainingComments = 5 - spamHelper.getUserCommentCount(userId);
        if (remainingComments <= 2 && remainingComments > 0) {
            Toast.makeText(this, "⚠️ Warning: " + remainingComments + " comments remaining in 20 seconds",
                    Toast.LENGTH_SHORT).show();
        }

        tvPostComment.setEnabled(false);

        firestoreHelper.addComment(postId, userId, username, commentText, new FirebaseFirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Comment newComment = new Comment("tempId", postId, userId, username, commentText);
                newComment.setCreatedAt(new java.util.Date());
                commentList.add(newComment);
                commentAdapter.notifyItemInserted(commentList.size() - 1);
                recyclerViewComments.scrollToPosition(commentList.size() - 1);

                etAddComment.setText("");
                tvPostComment.setEnabled(true);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CommentsActivity.this, "Failed to post comment: " + error, Toast.LENGTH_SHORT).show();
                tvPostComment.setEnabled(true);
            }
        });
    }
}
