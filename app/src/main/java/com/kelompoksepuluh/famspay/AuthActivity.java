package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.kelompoksepuluh.famspay.utils.SessionManager;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(
                    AuthActivity.this,
                    sessionManager.getFamilyId() == null ? ChooseFamilyActivity.class : HomeActivity.class
            );
            startActivity(intent);
            finish();
            return;
        }

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
