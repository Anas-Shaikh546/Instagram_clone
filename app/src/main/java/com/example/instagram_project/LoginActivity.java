package com.example.instagram_project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.instagram_project.utils.FirebaseAuthHelper;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextView tvForgotPassword,tvSignUp;

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);

        authHelper = new FirebaseAuthHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement forgot password logic
                Toast.makeText(LoginActivity.this, "Forgot Password clicked (not implemented)", Toast.LENGTH_SHORT).show();
            }
        });
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to RegisterActivity
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                // Optional: finish(); // Close login screen if you don't want users to go back
            }
        });
        SpannableString ss = new SpannableString("Don't have an account? Sign up");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false); // No underline
                ds.setColor(Color.parseColor("#0095F6")); // Blue color
            }
        };
        // Apply the span to the "Sign up" part (characters 23 to end)
        ss.setSpan(clickableSpan, 23, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvSignUp.setText(ss);
        tvSignUp.setMovementMethod(LinkMovementMethod.getInstance()); // Make the link clickable
        tvSignUp.setHighlightColor(Color.TRANSPARENT); // Remove click highlight
        // --- END OF SPANNABLESTRING CODE ---
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        authHelper.loginUser(email, password, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
