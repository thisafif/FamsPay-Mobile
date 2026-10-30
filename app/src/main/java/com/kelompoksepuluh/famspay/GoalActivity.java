package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GoalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_goal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavbar();
        setupActions();
    }

    private void setupNavbar() {
        ImageView btnNavHome = findViewById(R.id.btnNavHome);
        btnNavHome.setOnClickListener(v -> {
            Intent intent = new Intent(GoalActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        ImageView btnNavHistory = findViewById(R.id.btnNavHistory);
        btnNavHistory.setOnClickListener(v -> {
            Intent intent = new Intent(GoalActivity.this, HistoryActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            AddTransactionFragment.newInstance().show(getSupportFragmentManager(), "AddTransaction");
        });
    }

    private void setupActions() {
        findViewById(R.id.btnAddTarget).setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Tambah Target segera hadir", Toast.LENGTH_SHORT).show();
        });
        
        // Example for item click
        findViewById(R.id.btnEdit1).setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Edit Target segera hadir", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btnDelete1).setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Hapus Target segera hadir", Toast.LENGTH_SHORT).show();
        });
    }
}