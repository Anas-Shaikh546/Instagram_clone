package com.example.instagram_project.models;

import java.util.Date;

public class Like {
    private String likeId;
    private String postId;
    private String userId;
    private String username;
    private Date createdAt;

    // Default constructor required for Firestore
    public Like() {}

    public Like(String likeId, String postId, String userId, String username) {
        this.likeId = likeId;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
