
package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

public class ProfileActivity extends AppCompatActivity {

    TextView tvProfileUsername, tvProfileDisplayName;
    TextView btnBackProfile, tvChangeAvatar;

    EditText edtFullName, edtEmail, edtPhone;

    ImageView imgProfile;

    Button btnSaveProfile, btnLogout;

    SharedPreferences prefs;

    ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        initPreferences();
        initImagePicker();
        displayUsername();
        loadProfile();
        loadAvatar();
        setupListeners();
    }

    private void initViews() {
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileDisplayName = findViewById(R.id.tvProfileDisplayName);

        btnBackProfile = findViewById(R.id.btnBackProfile);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);

        imgProfile = findViewById(R.id.imgProfile);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void initPreferences() {
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
    }

    // Chọn ảnh đại diện
    private void initImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );

                            prefs.edit()
                                    .putString("profileImage", uri.toString())
                                    .apply();

                            imgProfile.setImageURI(uri);

                        } catch (Exception e) {
                            showMessage("Không thể lưu ảnh đại diện!");
                        }
                    }
                }
        );
    }

    private void displayUsername() {
        String username = prefs.getString("username", "");
        tvProfileUsername.setText(username);
        updateDisplayName();
    }

    private void updateDisplayName() {
        String fullName = prefs.getString("fullName", "");
        String username = prefs.getString("username", "");

        if (!fullName.isEmpty()) {
            tvProfileDisplayName.setText(fullName);
        } else {
            tvProfileDisplayName.setText(username);
        }
    }

    private void loadProfile() {
        edtFullName.setText(prefs.getString("fullName", ""));
        edtEmail.setText(prefs.getString("email", ""));
        edtPhone.setText(prefs.getString("phone", ""));
    }

    private void loadAvatar() {
        String imageUri = prefs.getString("profileImage", "");

        if (!imageUri.isEmpty()) {
            try {
                imgProfile.setImageURI(Uri.parse(imageUri));
            } catch (Exception e) {
                showMessage("Không thể tải ảnh đại diện!");
            }
        }
    }

    private void setupListeners() {
        btnBackProfile.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        imgProfile.setOnClickListener(v -> openImagePicker());

        tvChangeAvatar.setOnClickListener(v -> openImagePicker());

        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void openImagePicker() {
        pickImageLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(
                                ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE
                        )
                        .build()
        );
    }

    private void saveProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showMessage("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (!phone.matches("[0-9]{10}")) {
            edtPhone.setError("Số điện thoại phải có 10 chữ số");
            edtPhone.requestFocus();
            return;
        }

        prefs.edit()
                .putString("fullName", fullName)
                .putString("email", email)
                .putString("phone", phone)
                .apply();

        updateDisplayName();

        showMessage("Cập nhật thông tin thành công!");
    }

    // Hộp thoại xác nhận đăng xuất
    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .show();
    }

    // Xử lý đăng xuất
    private void logout() {

        // Xóa thông tin tài khoản và dữ liệu cá nhân đã lưu
        prefs.edit()
                .remove("username")
                .remove("fullName")
                .remove("email")
                .remove("phone")
                .remove("profileImage")
                .apply();

        // Quay về màn hình đăng nhập
        Intent intent = new Intent(
                ProfileActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
