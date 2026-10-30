package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.MonthlyLimit;
import com.kelompoksepuluh.famspay.data.model.MonthlyLimitRequest;
import com.kelompoksepuluh.famspay.data.model.Transaction;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberDetailActivity extends AppCompatActivity {
    private String userId;
    private ApiService apiService;
    private TextView tvMemberName, tvSisaLimit, tvTotalLimit, tvUsageStats, tvLimitStats;
    private TextView tvMemberTransactionSummary, tvMemberExpenseSummary, tvMemberTransactionState;
    private ProgressBar progressLimitUsage;
    private TransactionListAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        apiService = ApiClient.getClient(new SessionManager(this)).create(ApiService.class);
        bindIntentData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (apiService != null && userId != null) {
            fetchLatestMemberData();
            fetchMemberTransactions();
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvMemberName = findViewById(R.id.tvMemberName);
        tvSisaLimit = findViewById(R.id.tvSisaLimit);
        tvTotalLimit = findViewById(R.id.tvTotalLimit);
        tvUsageStats = findViewById(R.id.tvUsageStats);
        tvLimitStats = findViewById(R.id.tvLimitStats);
        tvMemberTransactionSummary = findViewById(R.id.tvMemberTransactionSummary);
        tvMemberExpenseSummary = findViewById(R.id.tvMemberExpenseSummary);
        tvMemberTransactionState = findViewById(R.id.tvMemberTransactionState);
        progressLimitUsage = findViewById(R.id.progressLimitUsage);
        setupTransactionList();

        LinearLayout llEditLimit = findViewById(R.id.llEditLimit);
        EditText etNewLimit = findViewById(R.id.etNewLimit);
        View btnChangeLimit = findViewById(R.id.btnChangeLimit);
        View btnSaveLimit = findViewById(R.id.btnSaveLimit);

        btnChangeLimit.setOnClickListener(v -> {
            btnChangeLimit.setVisibility(View.GONE);
            llEditLimit.setVisibility(View.VISIBLE);
            etNewLimit.requestFocus();
        });

        btnSaveLimit.setOnClickListener(v -> {
            long newLimit = FormatUtils.parseRupiah(etNewLimit.getText().toString());
            if (newLimit <= 0) {
                etNewLimit.setError("Masukkan nominal");
                return;
            }

            saveLimit(newLimit, llEditLimit, btnChangeLimit);
        });
    }

    private void bindIntentData() {
        userId = getIntent().getStringExtra("user_id");
        String fullName = getIntent().getStringExtra("full_name");
        long monthlyLimit = getIntent().getLongExtra("monthly_limit_base", 0);
        long remainingLimit = getIntent().getLongExtra("remaining_limit", 0);
        long currentExpense = getIntent().getLongExtra("current_month_expense", 0);

        tvMemberName.setText(fullName == null ? "Member" : fullName);
        tvSisaLimit.setText(FormatUtils.rupiah(remainingLimit));
        tvTotalLimit.setText(FormatUtils.rupiah(monthlyLimit));
        tvUsageStats.setText("Terpakai: " + FormatUtils.rupiah(currentExpense));
        tvLimitStats.setText("Limit: " + FormatUtils.rupiah(monthlyLimit));
        updateLimitProgress(monthlyLimit, currentExpense);
    }

    private void setupTransactionList() {
        RecyclerView recyclerView = findViewById(R.id.rvMemberTransactions);
        if (recyclerView == null) return;
        transactionAdapter = new TransactionListAdapter(false, transaction -> {
            Intent intent = new Intent(MemberDetailActivity.this, TransactionDetailActivity.class);
            intent.putExtra("transaction_id", transaction.id);
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(transactionAdapter);
    }

    private void fetchLatestMemberData() {
        apiService.getFamilyMembers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (User user : response.body().data) {
                        if (user.id != null && user.id.equals(userId)) {
                            bindMemberData(user);
                            return;
                        }
                    }
                    Toast.makeText(MemberDetailActivity.this, "Data anggota tidak ditemukan", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MemberDetailActivity.this, ApiUtils.errorMessage(response, "Gagal memuat data anggota"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                Toast.makeText(MemberDetailActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindMemberData(User user) {
        tvMemberName.setText(user.fullName == null ? "Member" : user.fullName);
        tvSisaLimit.setText(FormatUtils.rupiah(user.remainingLimit));
        tvTotalLimit.setText(FormatUtils.rupiah(user.monthlyLimitBase));
        tvUsageStats.setText("Terpakai: " + FormatUtils.rupiah(user.currentMonthExpense));
        tvLimitStats.setText("Limit: " + FormatUtils.rupiah(user.monthlyLimitBase));
        updateLimitProgress(user.monthlyLimitBase, user.currentMonthExpense);
    }

    private void fetchMemberTransactions() {
        if (userId == null || userId.trim().isEmpty()) {
            bindMemberTransactions(null);
            bindMemberTransactionState("User anggota tidak valid.", true);
            return;
        }

        bindMemberTransactionState("Memuat transaksi anggota...", true);
        bindMemberTransactionSummary(0, 0);
        if (transactionAdapter != null) {
            transactionAdapter.submitList(null);
        }

        apiService.getTransactions(currentMonthFilters()).enqueue(new Callback<ApiResponse<List<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindMemberTransactions(response.body().data);
                } else {
                    bindMemberTransactionState(ApiUtils.errorMessage(response, "Gagal memuat transaksi anggota"), true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Transaction>>> call, Throwable t) {
                bindMemberTransactionState("Koneksi gagal: " + t.getMessage(), true);
            }
        });
    }

    private Map<String, String> currentMonthFilters() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String dateFrom = apiFormat.format(calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String dateTo = apiFormat.format(calendar.getTime());

        Map<String, String> filters = new HashMap<>();
        filters.put("date_from", dateFrom);
        filters.put("date_to", dateTo);
        return filters;
    }

    private void bindMemberTransactions(List<Transaction> transactions) {
        List<Transaction> memberTransactions = new ArrayList<>();
        long income = 0;
        long expense = 0;

        if (transactions != null) {
            for (Transaction transaction : transactions) {
                if (transaction == null || userId == null || !userId.equals(transaction.userId)) {
                    continue;
                }
                memberTransactions.add(transaction);
                if ("income".equalsIgnoreCase(transaction.type)) {
                    income += transaction.amount;
                } else if ("expense".equalsIgnoreCase(transaction.type)) {
                    expense += transaction.amount;
                }
            }
        }

        bindMemberTransactionSummary(income, expense);
        if (transactionAdapter != null) {
            transactionAdapter.submitList(memberTransactions);
        }

        boolean hasTransactions = !memberTransactions.isEmpty();
        bindMemberTransactionState(hasTransactions ? "" : "Belum ada transaksi anggota ini.", !hasTransactions);
    }

    private void bindMemberTransactionSummary(long income, long expense) {
        if (tvMemberTransactionSummary != null) {
            tvMemberTransactionSummary.setText("Masuk " + FormatUtils.rupiah(income)
                    + " | Keluar " + FormatUtils.rupiah(expense)
                    + " | Net " + FormatUtils.rupiah(income - expense));
        }
        if (tvMemberExpenseSummary != null) {
            tvMemberExpenseSummary.setText("Total pengeluaran anggota bulan ini: " + FormatUtils.rupiah(expense));
        }
    }

    private void bindMemberTransactionState(String message, boolean visible) {
        if (tvMemberTransactionState == null) return;
        tvMemberTransactionState.setText(message);
        tvMemberTransactionState.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void updateLimitProgress(long monthlyLimit, long currentExpense) {
        if (progressLimitUsage == null) return;
        int progress = monthlyLimit <= 0 ? 0 : (int) Math.min(100, (currentExpense * 100) / monthlyLimit);
        progressLimitUsage.setProgress(progress);
    }

    private void saveLimit(long newLimit, LinearLayout llEditLimit, View btnChangeLimit) {
        if (userId == null) {
            Toast.makeText(this, "User tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.setMonthlyLimit(userId, new MonthlyLimitRequest(newLimit, FormatUtils.currentPeriodMonth()))
                .enqueue(new Callback<ApiResponse<MonthlyLimit>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MonthlyLimit>> call, Response<ApiResponse<MonthlyLimit>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            MonthlyLimit limit = response.body().data;
                            tvSisaLimit.setText(FormatUtils.rupiah(limit.remainingLimit));
                            tvTotalLimit.setText(FormatUtils.rupiah(limit.monthlyLimitBase));
                            tvLimitStats.setText("Limit: " + FormatUtils.rupiah(limit.monthlyLimitBase));
                            llEditLimit.setVisibility(View.GONE);
                            btnChangeLimit.setVisibility(View.VISIBLE);
                            Toast.makeText(MemberDetailActivity.this, "Limit berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MemberDetailActivity.this, ApiUtils.errorMessage(response, "Gagal memperbarui limit"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<MonthlyLimit>> call, Throwable t) {
                        Toast.makeText(MemberDetailActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
