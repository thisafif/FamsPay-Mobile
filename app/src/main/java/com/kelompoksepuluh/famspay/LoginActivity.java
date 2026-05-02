package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.kelompoksepuluh.famspay.ChooseFamilyActivity;
import com.kelompoksepuluh.famspay.ForgotPasswordActivity;
import com.kelompoksepuluh.famspay.GoogleSignInHelper;
import com.kelompoksepuluh.famspay.R;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInHelper googleSignInHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // goto forgotpw
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // google signin
        googleSignInHelper = new GoogleSignInHelper(this);
        googleSignInHelper.setCallback(new GoogleSignInHelper.GoogleSignInCallback() {
            @Override
            public void onSignInSuccess(String userId, String email) {
                //goto ChooseFamilyActivity
                navigateToChooseFamily();
            }

            @Override
            public void onSignInFailure(String errorMessage) {
                // error toast
                android.widget.Toast.makeText(LoginActivity.this, errorMessage, android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // google onclick
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> googleSignInHelper.signIn());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleSignInHelper.handleSignInResult(requestCode, resultCode, data);
    }

    private void navigateToChooseFamily() {
        Intent intent = new Intent(LoginActivity.this, ChooseFamilyActivity.class);
        startActivity(intent);
        finish();
    }
}