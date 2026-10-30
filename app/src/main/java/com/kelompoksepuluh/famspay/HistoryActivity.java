package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private TextView tvCurrentMonth, tvDateRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupDynamicDate();
        setupNavbar();
    }

    private void initViews() {
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvDateRange = findViewById(R.id.tvDateRange);
    }

    private void setupDynamicDate() {
        Calendar calendar = Calendar.getInstance();
        
        // Maret 2026 format
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"));
        tvCurrentMonth.setText(monthFormat.format(calendar.getTime()));

        // (1 Mar - 31 Mar) format
        SimpleDateFormat rangeFormat = new SimpleDateFormat("d MMM", new Locale("id", "ID"));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String firstDay = rangeFormat.format(calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String lastDay = rangeFormat.format(calendar.getTime());
        
        tvDateRange.setText("(" + firstDay + " - " + lastDay + ")");
    }

    private void setupNavbar() {
        ImageView btnNavHome = findViewById(R.id.btnNavHome);
        btnNavHome.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            AddTransactionFragment.newInstance().show(getSupportFragmentManager(), "AddTransaction");
        });
    }
}