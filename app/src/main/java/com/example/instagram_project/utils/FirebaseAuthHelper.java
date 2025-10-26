package com.example.instagram_project.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.instagram_project.models.User;

public class FirebaseAuthHelper {
    private FirebaseAuth mAuth;
    private Context context;

    public FirebaseAuthHelper(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    public void registerUser(String email, String password, String username, String fullName, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Create user document in Firestore
                                User user = new User(firebaseUser.getUid(), email, username, fullName);
                                FirebaseFirestoreHelper firestoreHelper = new FirebaseFirestoreHelper(context);
                                firestoreHelper.createUser(user, new FirebaseFirestoreHelper.FirestoreCallback() {
                                    @Override
                                    public void onSuccess() {
                                        callback.onSuccess(firebaseUser);
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        callback.onFailure(error);
                                    }
                                });
                            }
                        } else {
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Unknown error occurred";
                            android.util.Log.e("FirebaseAuth", "Registration error: " + errorMessage);
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void logout() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
}
