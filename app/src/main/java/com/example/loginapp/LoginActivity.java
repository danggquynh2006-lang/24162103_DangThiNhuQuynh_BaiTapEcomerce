
package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    // Ánh xạ các View
    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
    }

    // Thiết lập sự kiện đăng nhập
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
    }

    // Xử lý đăng nhập
    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ thông tin!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        saveUsername(username);
        openHome();
    }

    // Lưu tên người dùng
    private void saveUsername(String username) {
        SharedPreferences prefs = getSharedPreferences(
                "MyPrefs",
                MODE_PRIVATE
        );

        prefs.edit()
                .putString("username", username)
                .apply();
    }

    // Chuyển sang màn hình trang chủ
    private void openHome() {
        Intent intent = new Intent(
                LoginActivity.this,
                HomeActivity.class
        );

        startActivity(intent);
    }
}
