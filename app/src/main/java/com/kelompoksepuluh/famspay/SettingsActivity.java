package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.DialogUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ApiService apiService;
    private TextView tvUserName, tvEmail, tvRoleBadge, tvProfileState;
    private EditText etFirstName, etLastName, etEmail, etPassword, etAvatarUrl;
    private ImageView ivAvatar;
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(sessionManager).create(ApiService.class);

        initViews();
        fetchProfile();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        tvProfileState = findViewById(R.id.tvProfileState);
        ivAvatar = findViewById(R.id.ivAvatar);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAvatarUrl = findViewById(R.id.etAvatarUrl);
        clearProfilePlaceholders();

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            updateProfile();
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        DialogUtils.showConfirm(
                this,
                "Keluar",
                getString(R.string.settings_logout_confirm),
                "Ya, Keluar",
                "Batal",
                this::logout
        );
    }

    private void logout() {
        apiService.logout().enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                clearAndGoAuth();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                clearAndGoAuth();
            }
        });
    }

    private void fetchProfile() {
        showProfileState("Memuat profil...", true);
        apiService.getMe().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    bindProfile(response.body().data);
                    showProfileState("", false);
                } else {
                    String message = ApiUtils.errorMessage(response, "Gagal memuat profil");
                    showProfileState(message, true);
                    Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                String message = "Koneksi gagal: " + t.getMessage();
                showProfileState(message, true);
                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearProfilePlaceholders() {
        String cachedName = sessionManager.getFullName();
        tvUserName.setText(cachedName == null ? "User" : cachedName);
        tvEmail.setText("-");
        tvRoleBadge.setText(sessionManager.getRole());
        etFirstName.setText("");
        etLastName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etAvatarUrl.setText("");
        ivAvatar.setImageResource(R.drawable.ic_person);
    }

    private void bindProfile(User user) {
        sessionManager.saveUser(user.id, user.fullName, user.role, user.familyId);
        String fullName = user.fullName == null ? "" : user.fullName.trim();
        String[] parts = fullName.split("\\s+", 2);

        tvUserName.setText(fullName.isEmpty() ? "User" : fullName);
        tvEmail.setText(user.email == null ? "-" : user.email);
        tvRoleBadge.setText(user.role == null ? "member" : user.role);
        etFirstName.setText(parts.length > 0 ? parts[0] : "");
        etLastName.setText(parts.length > 1 ? parts[1] : "");
        etEmail.setText(user.email == null ? "" : user.email);
        etPassword.setText("");
        etAvatarUrl.setText(user.avatarUrl == null ? "" : user.avatarUrl);
        loadAvatar(user.avatarUrl);
    }

    private void updateProfile() {
        String firstName = etFirstName.getText() == null ? "" : etFirstName.getText().toString().trim();
        String lastName = etLastName.getText() == null ? "" : etLastName.getText().toString().trim();
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
        String avatarUrl = etAvatarUrl.getText() == null ? "" : etAvatarUrl.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("Nama depan wajib diisi");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email wajib diisi");
            return;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("full_name", (firstName + " " + lastName).trim());
        request.put("email", email);
        if (!avatarUrl.isEmpty()) {
            request.put("avatar_url", avatarUrl);
        }
        if (!password.isEmpty()) {
            request.put("password", password);
        }

        showProfileState("Menyimpan profil...", true);
        apiService.updateProfile(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    bindProfile(response.body().data);
                    showProfileState("", false);
                    Toast.makeText(SettingsActivity.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                } else {
                    String message = ApiUtils.errorMessage(response, "Gagal memperbarui profil");
                    showProfileState(message, true);
                    Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                String message = "Koneksi gagal: " + t.getMessage();
                showProfileState(message, true);
                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            ivAvatar.setImageResource(R.drawable.ic_person);
            return;
        }
        imageExecutor.execute(() -> {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new URL(avatarUrl).openStream());
                mainHandler.post(() -> ivAvatar.setImageBitmap(bitmap));
            } catch (Exception ignored) {
                mainHandler.post(() -> ivAvatar.setImageResource(R.drawable.ic_person));
            }
        });
    }

    private void showProfileState(String message, boolean visible) {
        if (tvProfileState == null) return;
        tvProfileState.setText(message);
        tvProfileState.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void clearAndGoAuth() {
        sessionManager.clearSession();
        Intent intent = new Intent(SettingsActivity.this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        imageExecutor.shutdownNow();
        super.onDestroy();
    }
}
