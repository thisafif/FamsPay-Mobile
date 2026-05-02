package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Button btnGoToLogin = findViewById(R.id.btnLogin);
        Button btnGoToRegister = findViewById(R.id.btnRegister);

        // goto halaman login
        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // goto halaman register
        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}