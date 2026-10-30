package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class ChooseFamilyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_family);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialCardView cardCreateFamily = findViewById(R.id.cardCreateFamily);
        MaterialCardView cardJoinFamily = findViewById(R.id.cardJoinFamily);

        btnBack.setOnClickListener(v -> finish());

        cardCreateFamily.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseFamilyActivity.this, CreateFamilyActivity.class);
            startActivity(intent);
        });

        cardJoinFamily.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseFamilyActivity.this, JoinFamilyActivity.class);
            startActivity(intent);
        });
    }
}