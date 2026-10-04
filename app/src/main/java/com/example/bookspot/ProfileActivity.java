package com.example.bookspot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePicture;
    private EditText etFullName;
    private EditText etPhone;
    private EditText etBirthDate;
    private EditText etEmail;

    private AppDatabase db;
    private SharedPreferences preferences;
    private UserProfile currentUser;
    private String currentEmail;

    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    ivProfilePicture.setImageBitmap(bitmap);
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    takePictureLauncher.launch(null);
                } else {
                    Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        preferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        currentEmail = preferences.getString("logged_in_email", "user@bookspot.com");

        db = AppDatabase.getDatabase(this);

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etBirthDate = findViewById(R.id.etBirthDate);
        etEmail = findViewById(R.id.etEmail);

        if (etEmail != null) {
            etEmail.setText(currentEmail);
            etEmail.setEnabled(false);
        }
    }

    private void loadUserData() {
        if (TextUtils.isEmpty(currentEmail)) {
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentUser = db.userDao().getUserByEmail(currentEmail);

            if (currentUser != null) {
                runOnUiThread(() -> {
                    if (etFullName != null) etFullName.setText(currentUser.getFullName());
                    if (etPhone != null) etPhone.setText(currentUser.getPhoneNumber());
                    if (etBirthDate != null) etBirthDate.setText(currentUser.getJoinDate());
                    if (etEmail != null) etEmail.setText(currentUser.getEmail());
                });
            }
        });
    }

    private void setupListeners() {
        Button btnChangePhoto = findViewById(R.id.btnChangePhoto);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        if (btnChangePhoto != null) {
            btnChangePhoto.setOnClickListener(v -> checkCameraPermissionAndOpen());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfileChanges());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }

        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
        }
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch(null);
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void saveProfileChanges() {
        String updatedName = etFullName != null && etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String updatedPhone = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String updatedJoinDate = etBirthDate != null && etBirthDate.getText() != null ? etBirthDate.getText().toString().trim() : "";

        if (TextUtils.isEmpty(updatedName)) {
            if (etFullName != null) etFullName.setError("Name cannot be empty");
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (currentUser == null) {
                String generatedAccountNum = UUID.randomUUID().toString().substring(0, 8);
                currentUser = new UserProfile(
                        generatedAccountNum,
                        updatedName,
                        currentEmail,
                        updatedPhone,
                        updatedJoinDate,
                        "1234"
                );
                db.userDao().insertUser(currentUser);
            } else {
                currentUser.setFullName(updatedName);
                currentUser.setPhoneNumber(updatedPhone);
                currentUser.setJoinDate(updatedJoinDate);
                db.userDao().updateUser(currentUser);
            }

            preferences.edit().putString("logged_in_email", currentUser.getEmail()).apply();

            runOnUiThread(() -> {
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void logout() {
        preferences.edit().remove("logged_in_email").apply();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        if (currentUser == null) {
            finish();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.userDao().deleteUser(currentUser);
            runOnUiThread(() -> {
                preferences.edit().remove("logged_in_email").apply();
                Toast.makeText(ProfileActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}