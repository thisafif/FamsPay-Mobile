package com.example.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;
    private ImageView btnBack;
    private MaterialButton btnNext;
    private LinearLayout layoutIndicators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // force pakai light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // layout 1

        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        layoutIndicators = findViewById(R.id.layoutIndicators); // container indikator viewpager

        setupOnboardingItems();
        setupActionButtons();

        // inisialisasi indikator (4 pager)
        setupIndicators(4);
        // indicator set ke 0
        setCurrentIndicator(0);
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> items = new ArrayList<>();

        items.add(new OnboardingItem(R.raw.onboarding1, R.string.onboarding_title_1, R.string.onboarding_desc_1));
        items.add(new OnboardingItem(R.raw.onboarding2, R.string.onboarding_title_2, R.string.onboarding_desc_2));
        items.add(new OnboardingItem(R.raw.onboarding3, R.string.onboarding_title_3, R.string.onboarding_desc_3));
        items.add(new OnboardingItem(R.raw.onboarding4, R.string.onboarding_title_4, R.string.onboarding_desc_4));

        adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // indicator
                setCurrentIndicator(position);

                // visibility tombol
                if (position == 0) {
                    btnBack.setVisibility(View.INVISIBLE);
                } else {
                    btnBack.setVisibility(View.VISIBLE);
                }

                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText(R.string.btn_mulai);
                } else {
                    btnNext.setText(R.string.btn_lanjut);
                }
            }
        });
    }

    private void setupActionButtons() {
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < adapter.getItemCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                // Navigasi ke tahap berikutnya jika onboarding selesai
                // Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                // startActivity(intent);
                // finish();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });
    }

    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews();
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(12, 0, 12, 0); //  jarak titik

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(), R.drawable.indicator_inactive));
            indicators[i].setLayoutParams(params);
            layoutIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicators.getChildAt(i);
            if (i == position) {
                // drawable capsule ijo yang nandain active
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(), R.drawable.indicator_active));
            } else {
                // drawable bulat inactive
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(), R.drawable.indicator_inactive));
            }
        }
    }
}