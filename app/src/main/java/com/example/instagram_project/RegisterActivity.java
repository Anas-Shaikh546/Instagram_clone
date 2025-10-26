package com.example.instagram_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.instagram_project.utils.FirebaseAuthHelper;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etUsername, etConfirmPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuthHelper authHelper;
    private String generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authHelper = new FirebaseAuthHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) { etEmail.setError("Email is required"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password is required"); return; }
        if (password.length() < 6) { etPassword.setError("Password must be at least 6 characters"); return; }
        if (TextUtils.isEmpty(username)) { etUsername.setError("Username is required"); return; }
        if (TextUtils.isEmpty(confirmPassword)) { etConfirmPassword.setError("Please confirm your password"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Passwords do not match"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        authHelper.registerUser(email, password, username, confirmPassword, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                // Generate 6-digit OTP
                generatedOTP = String.valueOf((int)(Math.random() * 900000) + 100000);

                // Send OTP via email
                String subject = "Your OTP for Instagram Project";
                String message = "Hello " + username + ",\n\nYour OTP is: " + generatedOTP;
                new JavaMailAPI(email, subject, message).execute();

                // Navigate to OTP verification activity
                Intent intent = new Intent(RegisterActivity.this, OTPVerificationActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("generatedOTP", generatedOTP);
                startActivity(intent);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
