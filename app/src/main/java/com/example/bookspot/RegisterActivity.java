package com.example.bookspot;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUserCode;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etBirthDate;
    private TextInputEditText etPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getDatabase(this);

        etUserCode = findViewById(R.id.etUserCode);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etPhone);
        etBirthDate = findViewById(R.id.etBirthDate);
        etPassword = findViewById(R.id.etRegPassword);

        Button btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnRegisterSubmit.setOnClickListener(v -> handleRegistration());

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 18;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etBirthDate.setText(formattedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void handleRegistration() {
        String userCode = etUserCode.getText() != null ? etUserCode.getText().toString().trim() : "";
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String birthDate = etBirthDate.getText() != null ? etBirthDate.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(userCode)) {
            etUserCode.setError("Please enter your User ID");
            return;
        }
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Please enter your full name");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Please enter your phone number");
            return;
        }
        if (TextUtils.isEmpty(birthDate)) {
            etBirthDate.setError("Please select birth date");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        UserProfile newUser = new UserProfile(userCode, fullName, email, phone, birthDate, password);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserProfile existing = db.userDao().getUserByEmail(email);
            if (existing != null) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Email already registered", Toast.LENGTH_SHORT).show());
                return;
            }

            db.userDao().insertUser(newUser);
            runOnUiThread(() -> {
                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}