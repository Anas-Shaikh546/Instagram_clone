package com.example.instagram_project.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.instagram_project.MainActivity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpamDetectionHelper {
    private static final int MAX_COMMENTS_PER_WINDOW = 5;
    private static final long TIME_WINDOW_MS = 20000; // 20 seconds
    
    private static SpamDetectionHelper instance;
    private List<CommentRecord> commentHistory;
    
    private SpamDetectionHelper() {
        commentHistory = new ArrayList<>();
    }
    
    public static SpamDetectionHelper getInstance() {
        if (instance == null) {
            instance = new SpamDetectionHelper();
        }
        return instance;
    }
    
    public boolean checkSpamAndRecordComment(String userId, Context context) {
        long currentTime = System.currentTimeMillis();
        
        // Remove old comments outside the time window
        commentHistory.removeIf(record -> 
            currentTime - record.getTimestamp() > TIME_WINDOW_MS);
        
        // Count comments from this user in the time window
        int userCommentCount = 0;
        for (CommentRecord record : commentHistory) {
            if (record.getUserId().equals(userId)) {
                userCommentCount++;
            }
        }
        
        // Check if user exceeds the limit
        if (userCommentCount >= MAX_COMMENTS_PER_WINDOW) {
            // Spam detected - logout user
            logoutUserForSpam(context);
            return false; // Block the comment
        }
        
        // Record this comment
        commentHistory.add(new CommentRecord(userId, currentTime));
        return true; // Allow the comment
    }
    
    private void logoutUserForSpam(Context context) {
        // Show warning message
        Toast.makeText(context, 
            "⚠️ Spam Detection: Account logged out due to excessive commenting. Please wait before commenting again.", 
            Toast.LENGTH_LONG).show();
        
        // Logout user
        FirebaseAuthHelper authHelper = new FirebaseAuthHelper(context);
        authHelper.logout();
        
        // Navigate to main activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    public void clearUserHistory(String userId) {
        commentHistory.removeIf(record -> record.getUserId().equals(userId));
    }
    
    public int getUserCommentCount(String userId) {
        long currentTime = System.currentTimeMillis();
        int count = 0;
        
        for (CommentRecord record : commentHistory) {
            if (record.getUserId().equals(userId) && 
                currentTime - record.getTimestamp() <= TIME_WINDOW_MS) {
                count++;
            }
        }
        
        return count;
    }
    
    public long getTimeRemaining(String userId) {
        long currentTime = System.currentTimeMillis();
        long oldestCommentTime = Long.MAX_VALUE;
        
        for (CommentRecord record : commentHistory) {
            if (record.getUserId().equals(userId) && 
                currentTime - record.getTimestamp() <= TIME_WINDOW_MS) {
                oldestCommentTime = Math.min(oldestCommentTime, record.getTimestamp());
            }
        }
        
        if (oldestCommentTime == Long.MAX_VALUE) {
            return 0;
        }
        
        long timeRemaining = TIME_WINDOW_MS - (currentTime - oldestCommentTime);
        return Math.max(0, timeRemaining);
    }
    
    private static class CommentRecord {
        private String userId;
        private long timestamp;
        
        public CommentRecord(String userId, long timestamp) {
            this.userId = userId;
            this.timestamp = timestamp;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
