package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.kelompoksepuluh.famspay.data.model.DashboardData;
import com.kelompoksepuluh.famspay.data.model.Goal;
import com.kelompoksepuluh.famspay.data.model.Transaction;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements AddTransactionFragment.OnTransactionChangedListener {

    private TextView tvUserName, tvDateRange, tvBalance;
    private SessionManager sessionManager;
    private ApiService apiService;
    private TransactionListAdapter recentTransactionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(sessionManager).create(ApiService.class);

        initViews();
        setupDynamicDate();
        hideInitialPlaceholders();
        fetchUserData();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvDateRange = findViewById(R.id.tvDateRange);
        tvBalance = findViewById(R.id.tvBalance);
        setupRecentTransactionsList();

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            AddTransactionFragment.newInstance().show(getSupportFragmentManager(), "AddTransaction");
        });

        findViewById(R.id.btnNavHistory).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnNavTarget).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GoalActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnNavAdmin).setOnClickListener(v -> {
            if (!sessionManager.isAdmin()) {
                Toast.makeText(this, "Halaman admin hanya untuk admin family", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(HomeActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvManageGoals).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GoalActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvSeeMore).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, HistoryActivity.class)));
    }

    private void setupRecentTransactionsList() {
        RecyclerView recyclerView = findViewById(R.id.rvRecentTransactions);
        if (recyclerView == null) return;
        recentTransactionsAdapter = new TransactionListAdapter(false, transaction -> {
            Intent intent = new Intent(HomeActivity.this, TransactionDetailActivity.class);
            intent.putExtra("transaction_id", transaction.id);
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(recentTransactionsAdapter);
    }

    private void setupDynamicDate() {
        Calendar calendar = Calendar.getInstance();
        
        // format tanggal
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", new Locale("id", "ID"));
        
        // tgl sekarang
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String firstDay = dateFormat.format(calendar.getTime());
        
        // tgl terakhir bulan
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
        String lastDay = dateFormat.format(calendar.getTime());
        
        // Set ke TextView
        String rangeText = "(" + firstDay + " - " + lastDay + ")";
        if (tvDateRange != null) {
            tvDateRange.setText(rangeText);
        }
    }

    private void fetchUserData() {
        apiService.getMe().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    User user = response.body().data;
                    sessionManager.saveUser(user.id, user.fullName, user.role, user.familyId);
                    String name = user.fullName != null ? user.fullName : "User";
                    tvUserName.setText(getString(R.string.home_hello, name));
                    if (tvBalance != null) {
                        tvBalance.setText(FormatUtils.rupiah(user.walletBalance));
                    }
                    fetchDashboard();
                } else if (response.code() == 401) {
                    sessionManager.clearSession();
                    startActivity(new Intent(HomeActivity.this, AuthActivity.class));
                    finish();
                } else {
                    String message = ApiUtils.errorMessage(response, "Gagal memuat profil");
                    bindHomeError(message);
                    Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                String message = "Koneksi gagal: " + t.getMessage();
                bindHomeError(message);
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideInitialPlaceholders() {
        View categoryCard = findViewById(R.id.cardCategory);
        if (categoryCard != null) {
            categoryCard.setVisibility(View.GONE);
        }
        hideLegacyTransactionSlots();
        bindDashboardLoading();
        if (recentTransactionsAdapter != null) {
            recentTransactionsAdapter.submitList(null);
        }
        bindGoals(null);
    }

    private void hideLegacyTransactionSlots() {
        int[] legacyIds = {R.id.itemTrans1, R.id.itemTrans2, R.id.itemTrans3};
        for (int id : legacyIds) {
            View view = findViewById(id);
            if (view != null) view.setVisibility(View.GONE);
        }
    }

    private void fetchDashboard() {
        bindDashboardLoading();
        apiService.getPersonalDashboard().enqueue(new Callback<ApiResponse<DashboardData>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardData>> call, Response<ApiResponse<DashboardData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    bindDashboard(response.body().data);
                } else {
                    String message = ApiUtils.errorMessage(response, "Gagal memuat dashboard");
                    bindDashboardError(message);
                    Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardData>> call, Throwable t) {
                String message = "Koneksi gagal: " + t.getMessage();
                bindDashboardError(message);
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindHomeError(String message) {
        if (tvUserName != null) {
            tvUserName.setText(getString(R.string.home_hello, "User"));
        }
        if (tvBalance != null) {
            tvBalance.setText(FormatUtils.rupiah(0));
        }
        bindDashboardError(message);
        bindGoals(null);
    }

    private void bindDashboardError(String message) {
        TextView savings = findViewById(R.id.tvTotalSavings);
        TextView flow = findViewById(R.id.tvFlowAnalysisHome);
        TextView budget = findViewById(R.id.tvBudgetAnalysisHome);
        TextView incomeChip = findViewById(R.id.tvIncomeChip);
        TextView expenseChip = findViewById(R.id.tvExpenseChip);
        TextView totalSpending = findViewById(R.id.tvTotalSpending);

        if (savings != null) savings.setText("Total tabungan: -");
        if (flow != null) flow.setText("Flow: -");
        if (budget != null) budget.setText("Budget: -");
        if (incomeChip != null) incomeChip.setText("Masuk -");
        if (expenseChip != null) expenseChip.setText("Keluar -");
        if (totalSpending != null) totalSpending.setText(FormatUtils.rupiah(0));
        bindIncomeExpenseDonut(0, 0);
        if (recentTransactionsAdapter != null) {
            recentTransactionsAdapter.submitList(null);
        }
        bindRecentState(message, true);
    }

    private void bindDashboardLoading() {
        TextView savings = findViewById(R.id.tvTotalSavings);
        TextView flow = findViewById(R.id.tvFlowAnalysisHome);
        TextView budget = findViewById(R.id.tvBudgetAnalysisHome);
        TextView totalSpending = findViewById(R.id.tvTotalSpending);
        if (savings != null) savings.setText("Total tabungan: Memuat...");
        if (flow != null) flow.setText("Flow: Memuat...");
        if (budget != null) budget.setText("Budget: Memuat...");
        if (totalSpending != null) totalSpending.setText(FormatUtils.rupiah(0));
        bindIncomeExpenseDonut(0, 0);
        bindRecentState("Memuat transaksi...", true);
    }

    private void bindDashboard(DashboardData dashboard) {
        if (tvBalance != null) {
            tvBalance.setText(FormatUtils.rupiah(dashboard.walletBalance));
        }
        TextView incomeChip = findViewById(R.id.tvIncomeChip);
        TextView expenseChip = findViewById(R.id.tvExpenseChip);
        TextView totalSpending = findViewById(R.id.tvTotalSpending);

        long income = dashboard.currentMonth == null ? 0 : dashboard.currentMonth.income;
        long expense = dashboard.currentMonth == null ? 0 : dashboard.currentMonth.expense;
        incomeChip.setText("Masuk " + FormatUtils.rupiah(income));
        expenseChip.setText("Keluar " + FormatUtils.rupiah(expense));
        totalSpending.setText(FormatUtils.rupiah(expense));
        bindIncomeExpenseDonut(income, expense);
        bindPersonalInsights(dashboard);

        bindRecentTransactions(dashboard.recentTransactions);
        fetchGoals();
    }

    private void bindIncomeExpenseDonut(long income, long expense) {
        IncomeExpenseDonutView donutView = findViewById(R.id.viewIncomeExpenseDonut);
        if (donutView != null) {
            donutView.setData(income, expense);
        }
        TextView flowSummary = findViewById(R.id.tvDonutFlowSummary);
        TextView expensePercent = findViewById(R.id.tvDonutExpensePercent);
        TextView incomeLegend = findViewById(R.id.tvDonutIncomeLegend);
        TextView expenseLegend = findViewById(R.id.tvDonutExpenseLegend);

        long totalFlow = income + expense;
        long net = income - expense;
        int expenseShare = totalFlow <= 0 ? 0 : (int) Math.round((expense * 100.0) / totalFlow);

        if (flowSummary != null) {
            flowSummary.setText("Net " + FormatUtils.rupiah(net));
        }
        if (expensePercent != null) {
            expensePercent.setText("Pengeluaran " + expenseShare + "% dari arus bulan ini");
        }
        if (incomeLegend != null) {
            incomeLegend.setText("Masuk " + FormatUtils.rupiah(income));
        }
        if (expenseLegend != null) {
            expenseLegend.setText("Keluar " + FormatUtils.rupiah(expense));
        }
    }

    private void bindPersonalInsights(DashboardData dashboard) {
        TextView savings = findViewById(R.id.tvTotalSavings);
        TextView flow = findViewById(R.id.tvFlowAnalysisHome);
        TextView budget = findViewById(R.id.tvBudgetAnalysisHome);
        if (savings != null) {
            savings.setText("Total tabungan: " + FormatUtils.rupiah(dashboard.totalSavings));
        }
        if (flow != null) {
            DashboardData.FlowAnalysis flowAnalysis = dashboard.flowAnalysis;
            if (flowAnalysis == null) {
                flow.setText("Flow: -");
            } else {
                flow.setText("Flow " + flowAnalysis.period
                        + ": masuk " + FormatUtils.rupiah(flowAnalysis.income)
                        + ", keluar " + FormatUtils.rupiah(flowAnalysis.expense)
                        + ", net " + FormatUtils.rupiah(flowAnalysis.netFlow));
            }
        }
        if (budget != null) {
            DashboardData.BudgetAnalysis budgetAnalysis = dashboard.budgetAnalysis;
            if (budgetAnalysis == null) {
                budget.setText("Budget: -");
            } else {
                budget.setText("Budget " + budgetAnalysis.period
                        + ": limit " + FormatUtils.rupiah(budgetAnalysis.monthlyLimitBase)
                        + ", sisa " + FormatUtils.rupiah(budgetAnalysis.remainingLimit)
                        + ", terpakai " + budgetAnalysis.usagePercent + "%");
            }
        }
    }

    private void bindRecentTransactions(List<Transaction> transactions) {
        if (recentTransactionsAdapter != null) {
            recentTransactionsAdapter.submitList(transactions);
        }
        boolean hasTransactions = transactions != null && !transactions.isEmpty();
        bindRecentState(hasTransactions ? "" : "Belum ada transaksi terbaru.", !hasTransactions);
    }

    private void bindRecentState(String message, boolean visible) {
        TextView state = findViewById(R.id.tvRecentState);
        if (state == null) return;
        state.setText(message);
        state.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void fetchGoals() {
        apiService.getGoals(false).enqueue(new Callback<ApiResponse<List<Goal>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Goal>>> call, Response<ApiResponse<List<Goal>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindGoals(response.body().data);
                } else {
                    bindGoals(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Goal>>> call, Throwable t) {
                bindGoals(null);
            }
        });
    }

    private void bindGoals(List<Goal> goals) {
        int[] rowIds = {R.id.rowGoal1, R.id.rowGoal2, R.id.rowGoal3, R.id.rowGoal4};
        int[] nameIds = {R.id.tvGoalNameHome1, R.id.tvGoalNameHome2, R.id.tvGoalNameHome3, R.id.tvGoalNameHome4};
        int[] amountIds = {R.id.tvGoalAmountHome1, R.id.tvGoalAmountHome2, R.id.tvGoalAmountHome3, R.id.tvGoalAmountHome4};
        int[] progressIds = {R.id.progressGoalHome1, R.id.progressGoalHome2, R.id.progressGoalHome3, R.id.progressGoalHome4};

        for (int i = 0; i < rowIds.length; i++) {
            View row = findViewById(rowIds[i]);
            ProgressBar progress = findViewById(progressIds[i]);
            if (goals != null && i < goals.size()) {
                Goal goal = goals.get(i);
                row.setVisibility(View.VISIBLE);
                progress.setVisibility(View.VISIBLE);
                ((TextView) findViewById(nameIds[i])).setText(goal.title);
                ((TextView) findViewById(amountIds[i])).setText(
                        FormatUtils.rupiah(goal.currentAmount) + " / " + FormatUtils.rupiah(goal.targetAmount)
                );
                int percentage = goal.targetAmount <= 0 ? 0 : (int) Math.min(100, (goal.currentAmount * 100) / goal.targetAmount);
                progress.setProgress(percentage);
            } else {
                row.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onTransactionChanged() {
        fetchUserData();
    }
}
