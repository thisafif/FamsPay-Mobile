package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.Family;
import com.kelompoksepuluh.famspay.data.model.FamilyRequest;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateFamilyActivity extends AppCompatActivity {
    private EditText etFamilyName;
    private Button btnCreateFamily;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_family);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(sessionManager).create(ApiService.class);

        ImageView btnBack = findViewById(R.id.btnBack);
        etFamilyName = findViewById(R.id.etFamilyName);
        btnCreateFamily = findViewById(R.id.btnCreateFamily);

        btnBack.setOnClickListener(v -> finish());
        btnCreateFamily.setOnClickListener(v -> createFamily());
    }

    private void createFamily() {
        String familyName = etFamilyName.getText() == null ? "" : etFamilyName.getText().toString().trim();
        if (familyName.length() < 3) {
            etFamilyName.setError("Nama keluarga minimal 3 karakter");
            return;
        }

        btnCreateFamily.setEnabled(false);
        apiService.createFamily(new FamilyRequest(familyName, null)).enqueue(new Callback<ApiResponse<Family>>() {
            @Override
            public void onResponse(Call<ApiResponse<Family>> call, Response<ApiResponse<Family>> response) {
                if (response.isSuccessful()) {
                    refreshUserAndGoHome();
                } else {
                    btnCreateFamily.setEnabled(true);
                    Toast.makeText(CreateFamilyActivity.this, ApiUtils.errorMessage(response, "Gagal membuat family"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Family>> call, Throwable t) {
                btnCreateFamily.setEnabled(true);
                Toast.makeText(CreateFamilyActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshUserAndGoHome() {
        apiService.getMe().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    User user = response.body().data;
                    sessionManager.saveUser(user.id, user.fullName, user.role, user.familyId);
                }
                Intent intent = new Intent(CreateFamilyActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Intent intent = new Intent(CreateFamilyActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
