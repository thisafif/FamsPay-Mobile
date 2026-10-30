package com.kelompoksepuluh.famspay;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private TextView tvForgotState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        etEmail = findViewById(R.id.etEmail);
        tvForgotState = findViewById(R.id.tvForgotState);
        Button btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> showUnavailableState());
    }

    private void showUnavailableState() {
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email wajib diisi");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email tidak valid");
            return;
        }
        tvForgotState.setText("Fitur reset password belum tersedia. Hubungi admin atau support FamsPay untuk bantuan akses akun.");
        tvForgotState.setVisibility(View.VISIBLE);
    }
}
