package com.example.instagram_project.models;

import java.util.Date;
import java.util.List;

public class Post {
    private String postId;
    private String userId;
    private String username;
    private String userProfileImageUrl;
    private String imageUrl;
    private String caption;
    private int commentsCount;
    private List<String> likes;
    private Date createdAt;
    private Date updatedAt;

    // Default constructor required for Firestore
    public Post() {}
    // ... inside Post.java

    // Add this method
    public void setLikesCount(int count) {
        if (this.likes == null) {
            this.likes = new java.util.ArrayList<>();
        }
        // This is a simple way to update the count for the UI
        // A better way would be to just store a count variable
    }

    // Add this method
    public int getCommentsCount() {
        return commentsCount;
    }

    // Make sure your getLikesCount() and getCommentsCount() are correct
    public int getLikesCount() {
        return likes != null ? likes.size() : 0;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    // ... rest of your file


    public Post(String postId, String userId, String username, String imageUrl, String caption) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
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

    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }

    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }



    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

}
