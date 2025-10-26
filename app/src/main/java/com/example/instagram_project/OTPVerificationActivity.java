package com.example.instagram_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OTPVerificationActivity extends AppCompatActivity {

    private EditText etOTP;
    private Button btnVerify;
    private String generatedOTP, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);

        etOTP = findViewById(R.id.etOTP);
        btnVerify = findViewById(R.id.btnVerify);

        // Get OTP from Intent
        generatedOTP = getIntent().getStringExtra("generatedOTP");
        email = getIntent().getStringExtra("email");

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOTP = etOTP.getText().toString().trim();
                if (enteredOTP.equals(generatedOTP)) {
                    Toast.makeText(OTPVerificationActivity.this, "OTP Verified!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OTPVerificationActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(OTPVerificationActivity.this, "Incorrect OTP! Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
