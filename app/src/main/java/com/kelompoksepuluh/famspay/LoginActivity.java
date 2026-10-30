package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.LoginData;
import com.kelompoksepuluh.famspay.data.model.LoginRequest;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import android.util.Log;
import com.kelompoksepuluh.famspay.data.model.SocialLoginRequest;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInHelper googleSignInHelper;

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle;

    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Handle insets for Edge-to-Edge
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize Managers and Services
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(sessionManager).create(ApiService.class);

        // Initialize Views and Listeners
        initViews();
        setupManualLogin();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

    }

    private void setupManualLogin() {
        if (btnLogin == null) return;

        btnLogin.setOnClickListener(v -> {
            String email = etEmail != null && etEmail.getText() != null
                    ? etEmail.getText().toString().trim()
                    : "";

            String password = etPassword != null && etPassword.getText() != null
                    ? etPassword.getText().toString().trim()
                    : "";

            if (email.isEmpty()) {
                if (etEmail != null) {
                    etEmail.setError("Email tidak boleh kosong");
                    etEmail.requestFocus();
                }
                return;
            }

            if (password.isEmpty()) {
                if (etPassword != null) {
                    etPassword.setError("Password tidak boleh kosong");
                    etPassword.requestFocus();
                }
                return;
            }

            loginUser(email, password);
        });
    }

    private void loginUser(String email, String password) {
        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<ApiResponse<LoginData>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<LoginData>> call,
                    Response<ApiResponse<LoginData>> response
            ) {
                setLoading(false);

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().data != null
                        && response.body().data.token != null) {

                    String token = response.body().data.token;
                    sessionManager.saveToken(token);

                    fetchMeAndNavigate();

                } else {
                    String errorMessage = "Email atau password salah";
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            JSONObject jsonObject = new JSONObject(errorStr);
                            if (jsonObject.has("message")) {
                                errorMessage = jsonObject.getString("message");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginData>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(
                        LoginActivity.this,
                        "Koneksi gagal: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void setupGoogleSignIn() {
        if (btnGoogle != null) {
            btnGoogle.setEnabled(false);
            btnGoogle.setAlpha(0.45f);
        }
        if (!isGoogleLoginEnabled()) return;

        googleSignInHelper = new GoogleSignInHelper(this);

        googleSignInHelper.setCallback(new GoogleSignInHelper.GoogleSignInCallback() {
            @Override
            public void onSignInSuccess(String idToken, String email) {
                loginWithGoogleBackend(idToken);
            }

            @Override
            public void onSignInFailure(String errorMessage) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> googleSignInHelper.signIn());
        }
    }

    private void loginWithGoogleBackend(String idToken) {
        setLoading(true);
        SocialLoginRequest request = new SocialLoginRequest(idToken);
        apiService.loginGoogle(request).enqueue(new Callback<ApiResponse<LoginData>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginData>> call, Response<ApiResponse<LoginData>> response) {
                setLoading(false);
                Log.d("LoginActivity", "Google Sync Code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    sessionManager.saveToken(response.body().data.token);
                    Toast.makeText(LoginActivity.this, "Login Google Berhasil", Toast.LENGTH_SHORT).show();
                    navigateToChooseFamily();
                } else {
                    String errorMsg = "Gagal sinkronisasi akun Google ke server";
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            Log.e("LoginActivity", "Google Sync Error: " + errorStr);
                            JSONObject jsonObject = new JSONObject(errorStr);
                            if (jsonObject.has("message")) {
                                errorMsg += ": " + jsonObject.getString("message");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginData>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isGoogleLoginEnabled() {
        return false;
    }

    private void setLoading(boolean isLoading) {
        if (btnLogin != null) {
            btnLogin.setEnabled(!isLoading);
            if (isLoading) {
                btnLogin.setText("Loading...");
            } else {
                btnLogin.setText("Masuk");
            }
        }
        if (btnGoogle != null) {
            btnGoogle.setEnabled(!isLoading);
        }
    }

    private void navigateToChooseFamily() {
        Intent intent = new Intent(LoginActivity.this, ChooseFamilyActivity.class);
        startActivity(intent);
        finish();
    }

    private void fetchMeAndNavigate() {
        apiService.getMe().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    User user = response.body().data;
                    sessionManager.saveUser(user.id, user.fullName, user.role, user.familyId);
                    Toast.makeText(LoginActivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(
                            LoginActivity.this,
                            user.familyId == null ? ChooseFamilyActivity.class : HomeActivity.class
                    );
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, ApiUtils.errorMessage(response, "Gagal mengambil profil"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (googleSignInHelper != null) {
            googleSignInHelper.handleSignInResult(requestCode, resultCode, data);
        }
    }
}
