package com.example.instagram_project;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseFirestoreHelper extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_firebase);

        TextView tvStatus = findViewById(R.id.tvStatus);

        try {
            // Test if Firestore is accessible
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            if (db != null) {
                tvStatus.setText("✅ Firestore initialized successfully!\nDatabase: " + db.getApp().getOptions().getProjectId());
                Log.d("FirestoreTest", "Firestore initialized: " + db.getApp().getOptions().getProjectId());
            } else {
                tvStatus.setText("❌ Firestore not initialized");
                Log.e("FirestoreTest", "Firestore not initialized");
            }
        } catch (Exception e) {
            tvStatus.setText("❌ Firestore error: " + e.getMessage());
            Log.e("FirestoreTest", "Firestore error: " + e.getMessage());
        }
    }
}
