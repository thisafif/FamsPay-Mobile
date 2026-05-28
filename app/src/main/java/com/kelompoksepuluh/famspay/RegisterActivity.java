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

import com.google.android.material.button.MaterialButton;
import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.LoginData;
import com.kelompoksepuluh.famspay.data.model.RegisterRequest;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import com.kelompoksepuluh.famspay.data.model.SocialLoginRequest;
import com.kelompoksepuluh.famspay.data.model.LoginRequest;
import android.util.Log;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword;
    private Button btnRegister;
    private MaterialButton btnGoogle;

    private SessionManager sessionManager;
    private ApiService apiService;
    private GoogleSignInHelper googleSignInHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(sessionManager).create(ApiService.class);

        initViews();
        setupActions();
        setupGoogleSignIn();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogle = findViewById(R.id.btnGoogle);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupActions() {
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText() != null
                    ? etFullName.getText().toString().trim()
                    : "";

            String email = etEmail.getText() != null
                    ? etEmail.getText().toString().trim()
                    : "";

            String password = etPassword.getText() != null
                    ? etPassword.getText().toString().trim()
                    : "";

            if (validateInput(fullName, email, password)) {
                registerUser(fullName, email, password);
            }
        });
    }

    private boolean validateInput(String fullName, String email, String password) {
        if (fullName.isEmpty()) {
            etFullName.setError("Nama tidak boleh kosong");
            etFullName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password tidak boleh kosong");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            etPassword.setError("Password minimal 8 karakter");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String fullName, String email, String password) {
        setLoading(true);

        RegisterRequest request = new RegisterRequest(fullName, email, password);

        apiService.register(request).enqueue(new Callback<ApiResponse<LoginData>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<LoginData>> call,
                    Response<ApiResponse<LoginData>> response
            ) {
                setLoading(false);

                Log.d("RegisterActivity", "Response Code: " + response.code());
                if (response.body() != null) {
                    Log.d("RegisterActivity", "Response Status: " + response.body().status);
                    Log.d("RegisterActivity", "Response Message: " + response.body().message);
                }

                if (response.isSuccessful() && response.body() != null) {
                    // Registrasi berhasil di DB
                    Log.d("RegisterActivity", "Registrasi sukses, mencoba login otomatis...");
                    
                    // Jika backend langsung kasih token di register, pakai itu.
                    // Jika tidak (seperti logcat tadi), kita tembak login otomatis.
                    if (response.body().data != null && response.body().data.token != null) {
                        String token = response.body().data.token;
                        sessionManager.saveToken(token);
                        Toast.makeText(RegisterActivity.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                        navigateToChooseFamily();
                    } else {
                        // Backend tidak kasih token saat register, lakukan auto-login
                        autoLoginAfterRegister(email, password);
                    }

                } else {
                    String errorMessage = "Registrasi gagal";
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
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginData>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(
                        RegisterActivity.this,
                        "Koneksi gagal: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void autoLoginAfterRegister(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        apiService.login(loginRequest).enqueue(new Callback<ApiResponse<LoginData>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginData>> call, Response<ApiResponse<LoginData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    String token = response.body().data.token;
                    sessionManager.saveToken(token);
                    Toast.makeText(RegisterActivity.this, "Registrasi & Login Berhasil", Toast.LENGTH_SHORT).show();
                    navigateToChooseFamily();
                } else {
                    // Registrasi sukses tapi login gagal (jarang terjadi)
                    Toast.makeText(RegisterActivity.this, "Registrasi berhasil, silakan lakukan login.", Toast.LENGTH_LONG).show();
                    finish(); // Kembali ke halaman Login
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginData>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Registrasi berhasil, silakan lakukan login.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void setupGoogleSignIn() {
        googleSignInHelper = new GoogleSignInHelper(this);

        googleSignInHelper.setCallback(new GoogleSignInHelper.GoogleSignInCallback() {
            @Override
            public void onSignInSuccess(String idToken, String email) {
                loginWithGoogleBackend(idToken);
            }

            @Override
            public void onSignInFailure(String errorMessage) {
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        btnGoogle.setOnClickListener(v -> googleSignInHelper.signIn());
    }

    private void loginWithGoogleBackend(String idToken) {
        setLoading(true);
        SocialLoginRequest request = new SocialLoginRequest(idToken);
        apiService.loginGoogle(request).enqueue(new Callback<ApiResponse<LoginData>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginData>> call, Response<ApiResponse<LoginData>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    sessionManager.saveToken(response.body().data.token);
                    Toast.makeText(RegisterActivity.this, "Login Google Berhasil", Toast.LENGTH_SHORT).show();
                    navigateToChooseFamily();
                } else {
                    Toast.makeText(RegisterActivity.this, "Gagal sinkronisasi akun Google ke server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginData>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        btnGoogle.setEnabled(!isLoading);

        if (isLoading) {
            btnRegister.setText("Loading...");
        } else {
            btnRegister.setText("Daftar");
        }
    }

    private void navigateToChooseFamily() {
        Intent intent = new Intent(RegisterActivity.this, ChooseFamilyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (googleSignInHelper != null) {
            googleSignInHelper.handleSignInResult(requestCode, resultCode, data);
        }
    }
}