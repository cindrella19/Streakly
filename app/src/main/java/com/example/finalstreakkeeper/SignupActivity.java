package com.example.finalstreakkeeper;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput, confirmPasswordInput;
    private Button signupBtn;
    private TextView loginLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signupBtn = findViewById(R.id.signupBtn);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        signupBtn.setOnClickListener(v -> signupUser());
        loginLink.setOnClickListener(v -> finish());
    }

    private void signupUser() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {

                        String uid = mAuth.getCurrentUser().getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", "");
                        userData.put("email", email);
                        userData.put("doneToday", 0);
                        userData.put("activeHabits", 0);
                        userData.put("createdAt", FieldValue.serverTimestamp());

                        db.collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                    finishAffinity();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                SignupActivity.this,
                                                "Firestore error: " + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );

                    } else {
                        Toast.makeText(
                                SignupActivity.this,
                                "Signup failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
