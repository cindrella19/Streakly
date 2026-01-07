package com.example.finalstreakkeeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView displayNameText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        profileImage = findViewById(R.id.profileImageLarge);
        displayNameText = findViewById(R.id.profileDisplayName);
        FloatingActionButton addPhotoBtn = findViewById(R.id.addPhotoBtn);
        TextView emailText = findViewById(R.id.profileEmail);
        Button logoutBtn = findViewById(R.id.logoutBtnProfile);

        // ✅ USE CUSTOM BRAND ICON (NO STORAGE)
        profileImage.setImageResource(R.mipmap.ic_launcher_round);
        
        // ✅ DISABLE PHOTO UPLOAD (NOT USED)
        addPhotoBtn.hide();
        addPhotoBtn.setEnabled(false);

        String name = user.getDisplayName();
        displayNameText.setText(name != null && !name.isEmpty() ? name : "Streakly User");
        emailText.setText(user.getEmail());

        displayNameText.setOnClickListener(v -> showEditNameDialog());
        logoutBtn.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showEditNameDialog() {
        EditText input = new EditText(this);
        input.setText(displayNameText.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle("Edit Name")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) updateProfileName(newName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProfileName(String newName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest updates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(updates).addOnSuccessListener(aVoid ->
                    displayNameText.setText(newName)
            );
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (d, w) -> {
                    mAuth.signOut();
                    startActivity(new Intent(this, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
