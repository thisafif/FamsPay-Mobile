package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavbar();
        setupActions();
    }

    private void setupNavbar() {
        findViewById(R.id.btnNavHome).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnNavHistory).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, HistoryActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnNavTarget).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, GoalActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            AddTransactionFragment.newInstance().show(getSupportFragmentManager(), "AddTransaction");
        });
    }

    private void setupActions() {
        findViewById(R.id.itemMember1).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, MemberDetailActivity.class);
            startActivity(intent);
        });
    }
}