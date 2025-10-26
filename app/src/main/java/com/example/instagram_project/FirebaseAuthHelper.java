package com.example.instagram_project;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;

public class FirebaseAuthHelper extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_firebase);
        
        TextView tvStatus = findViewById(R.id.tvStatus);
        
        try {
            // Test if Firebase is initialized
            FirebaseApp app = FirebaseApp.getInstance();
            if (app != null) {
                tvStatus.setText("✅ Firebase initialized successfully!\nProject: " + app.getOptions().getProjectId());
                Log.d("FirebaseTest", "Firebase initialized: " + app.getOptions().getProjectId());
            } else {
                tvStatus.setText("❌ Firebase not initialized");
                Log.e("FirebaseTest", "Firebase not initialized");
            }
        } catch (Exception e) {
            tvStatus.setText("❌ Firebase error: " + e.getMessage());
            Log.e("FirebaseTest", "Firebase error: " + e.getMessage());
        }
    }
}
