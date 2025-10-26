package com.example.instagram_project.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.instagram_project.models.Comment;
import com.example.instagram_project.models.Like;
import com.example.instagram_project.models.Post;
import com.example.instagram_project.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseFirestoreHelper {
    private FirebaseFirestore db;
    private Context context;

    public FirebaseFirestoreHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    // Public method to generate document IDs
    public String generateDocumentId(String collection) {
        return db.collection(collection).document().getId();
    }

    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface FirestoreDataCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    public interface FirestoreListCallback<T> {
        void onSuccess(List<T> data);
        void onFailure(String error);
    }

    // User operations
    public void createUser(User user, FirestoreCallback callback) {
        db.collection("users").document(user.getUserId())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void getUser(String userId, FirestoreDataCallback<User> callback) {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                callback.onSuccess(user);
                            } else {
                                callback.onFailure("User not found");
                            }
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    // Post operations
    public void createPost(Post post, FirestoreCallback callback) {
        db.collection("posts").document(post.getPostId())
                .set(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void getAllPosts(FirestoreListCallback<Post> callback) {
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Post> posts = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class);
                                posts.add(post);
                            }
                            callback.onSuccess(posts);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getUserPosts(String userId, FirestoreListCallback<Post> callback) {
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Post> posts = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class);
                                posts.add(post);
                            }
                            callback.onSuccess(posts);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    // Like operations
    public void likePost(String postId, String userId, String username, FirestoreCallback callback) {
        String likeId = postId + "_" + userId;
        Like like = new Like(likeId, postId, userId, username);

        db.collection("likes").document(likeId)
                .set(like)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update post likes count
                        updatePostLikes(postId, userId, true, callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void unlikePost(String postId, String userId, FirestoreCallback callback) {
        String likeId = postId + "_" + userId;

        db.collection("likes").document(likeId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update post likes count
                        updatePostLikes(postId, userId, false, callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    private void updatePostLikes(String postId, String userId, boolean isLike, FirestoreCallback callback) {
        db.collection("posts").document(postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Post post = document.toObject(Post.class);
                                if (post != null) {
                                    List<String> likes = post.getLikes();
                                    if (likes == null) {
                                        likes = new ArrayList<>();
                                    }

                                    if (isLike && !likes.contains(userId)) {
                                        likes.add(userId);
                                    } else if (!isLike) {
                                        likes.remove(userId);
                                    }

                                    post.setLikes(likes);

                                    db.collection("posts").document(postId)
                                            .set(post)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    callback.onSuccess();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    callback.onFailure(e.getMessage());
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    public void checkIfUserLikedPost(String postId, String userId, FirestoreDataCallback<Boolean> callback) {
        String likeId = postId + "_" + userId;
        db.collection("likes").document(likeId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            callback.onSuccess(document.exists());
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getPostLikes(String postId, FirestoreListCallback<Like> callback) {
        db.collection("likes")
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Like> likes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Like like = document.toObject(Like.class);
                                likes.add(like);
                            }
                            callback.onSuccess(likes);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    // Comment operations


    // This is the new addComment method
    // This is the new addComment method
    // Replace the old addComment method with this one in FirebaseFirestoreHelper.java
    // Replace the entire old addComment method with this one in FirebaseFirestoreHelper.java
    public void addComment(String postId, String userId, String username, String text, FirestoreCallback callback) {
        String commentId = db.collection("comments").document().getId();
        Comment comment = new Comment(commentId, postId, userId, username, text);

        // Get a reference to the post document
        DocumentReference postRef = db.collection("posts").document(postId);

        db.runTransaction(transaction -> {
            // 1. Get the current post snapshot
            DocumentSnapshot postSnapshot = transaction.get(postRef);

            // 2. Get the current comments count
            long currentCount = 0;
            if (postSnapshot.exists() && postSnapshot.contains("commentsCount")) {
                Long count = postSnapshot.getLong("commentsCount");
                if (count != null) {
                    currentCount = count;
                }
            } else if (!postSnapshot.exists()) {
                // Throw an exception if the post doesn't exist to stop the transaction
                throw new FirebaseFirestoreException("Post not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }
            // If commentsCount field doesn't exist yet, it stays 0, which is fine

            // 3. Update the count in the transaction
            transaction.update(postRef, "commentsCount", currentCount + 1);

            // 4. Create the new comment document in the transaction
            DocumentReference commentRef = db.collection("comments").document(commentId);
            transaction.set(commentRef, comment);

            // Transaction requires a return value (can be null)
            return null;
        }).addOnSuccessListener(aVoid -> {
            // Transaction succeeded, call the onSuccess callback
            callback.onSuccess();
        }).addOnFailureListener(e -> {
            // Transaction failed, call the onFailure callback
            callback.onFailure(e.getMessage());
        });
    }
    public void getPostComments(String postId, FirestoreListCallback<Comment> callback) {
        db.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Comment> comments = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Comment comment = document.toObject(Comment.class);
                                comments.add(comment);
                            }
                            callback.onSuccess(comments);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }
}
