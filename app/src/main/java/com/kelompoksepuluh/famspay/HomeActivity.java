package com.kelompoksepuluh.famspay;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView tvUserName;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(sessionManager).create(ApiService.class);

        initViews();
        fetchUserData();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
    }

    private void fetchUserData() {
        apiService.getMe().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    User user = response.body().data;
                    String name = user.fullName != null ? user.fullName : "User";
                    tvUserName.setText(getString(R.string.home_hello, name));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                // Handle failure if needed
            }
        });
    }
}