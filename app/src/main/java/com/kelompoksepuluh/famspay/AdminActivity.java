package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.DashboardData;
import com.kelompoksepuluh.famspay.data.model.Family;
import com.kelompoksepuluh.famspay.data.model.FamilyRequest;
import com.kelompoksepuluh.famspay.data.model.Transaction;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.DialogUtils;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity implements AddTransactionFragment.OnTransactionChangedListener {
    private ApiService apiService;
    private SessionManager sessionManager;
    private List<User> members;
    private Family currentFamily;
    private String joinCode;
    private TransactionListAdapter recentTransactionsAdapter;
    private FamilyMemberListAdapter memberAdapter;
    private final Map<String, String> memberNames = new HashMap<>();

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

        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Halaman admin hanya untuk admin family", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        apiService = ApiClient.getClient(sessionManager).create(ApiService.class);
        setupNavbar();
        setupRecentTransactionsList();
        setupMemberList();
        setupActions();
        clearPlaceholders();
        fetchFamily();
        fetchFamilyDashboard();
        fetchMembers();
    }

    private void setupNavbar() {
        findViewById(R.id.btnNavHome).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnNavHistory).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminFamilyHistoryActivity.class);
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
        View itemMember1 = findViewById(R.id.itemMember1);
        View itemMember2 = findViewById(R.id.itemMember2);
        if (itemMember1 != null) itemMember1.setVisibility(View.GONE);
        if (itemMember2 != null) itemMember2.setVisibility(View.GONE);
        hideLegacyRecentTransactions();

        findViewById(R.id.btnEditFamilyName).setOnClickListener(v -> showEditFamilyNameDialog());
        findViewById(R.id.tvViewFamilyHistory).setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, AdminFamilyHistoryActivity.class)));

        findViewById(R.id.btnCopyCode).setOnClickListener(v -> {
            if (joinCode == null) return;
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("FamsPay join code", joinCode));
            Toast.makeText(this, "Kode disalin", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnShareCode).setOnClickListener(v -> {
            if (joinCode == null) return;
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Kode family FamsPay: " + joinCode);
            startActivity(Intent.createChooser(intent, "Bagikan kode"));
        });
    }

    private void setupRecentTransactionsList() {
        RecyclerView recyclerView = findViewById(R.id.rvAdminRecentTransactions);
        if (recyclerView == null) return;
        recentTransactionsAdapter = new TransactionListAdapter(true, transaction -> {
            Intent intent = new Intent(AdminActivity.this, TransactionDetailActivity.class);
            intent.putExtra("transaction_id", transaction.id);
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(recentTransactionsAdapter);
    }

    private void setupMemberList() {
        RecyclerView recyclerView = findViewById(R.id.layoutMemberList);
        if (recyclerView == null) return;
        memberAdapter = new FamilyMemberListAdapter(sessionManager.getUserId(), new FamilyMemberListAdapter.OnMemberActionListener() {
            @Override
            public void onDetail(User user) {
                openMemberDetail(user);
            }

            @Override
            public void onRoleChange(User user, String nextRole, Button actionButton) {
                updateMemberRole(user, nextRole, actionButton);
            }

            @Override
            public void onRemove(User user) {
                confirmRemoveMember(user);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(memberAdapter);
    }

    private void hideLegacyRecentTransactions() {
        int[] ids = {R.id.tvRecentTransaction1, R.id.tvRecentTransaction2, R.id.tvRecentTransaction3};
        for (int id : ids) {
            View view = findViewById(id);
            if (view != null) view.setVisibility(View.GONE);
        }
    }

    private void openMemberDetail(User user) {
        if (user == null) return;
        Intent intent = new Intent(AdminActivity.this, MemberDetailActivity.class);
        intent.putExtra("user_id", user.id);
        intent.putExtra("full_name", user.fullName);
        intent.putExtra("wallet_balance", user.walletBalance);
        intent.putExtra("monthly_limit_base", user.monthlyLimitBase);
        intent.putExtra("remaining_limit", user.remainingLimit);
        intent.putExtra("current_month_expense", user.currentMonthExpense);
        startActivity(intent);
    }

    private void fetchFamily() {
        apiService.getMyFamily().enqueue(new Callback<ApiResponse<Family>>() {
            @Override
            public void onResponse(Call<ApiResponse<Family>> call, Response<ApiResponse<Family>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    Family family = response.body().data;
                    currentFamily = family;
                    joinCode = family.joinCode;
                    bindFamily(family);
                } else {
                    Toast.makeText(AdminActivity.this, ApiUtils.errorMessage(response, "Gagal memuat family"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Family>> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindFamily(Family family) {
        TextView tvFamilyName = findViewById(R.id.tvFamilyName);
        TextView tvJoinCode = findViewById(R.id.tvJoinCode);
        if (tvFamilyName != null) {
            tvFamilyName.setText(family == null || family.familyName == null ? "-" : family.familyName);
        }
        if (tvJoinCode != null) {
            tvJoinCode.setText(joinCode == null ? "-" : joinCode);
        }
    }

    private void clearPlaceholders() {
        TextView tvJoinCode = findViewById(R.id.tvJoinCode);
        if (tvJoinCode != null) {
            tvJoinCode.setText("-");
        }
        TextView tvFamilyName = findViewById(R.id.tvFamilyName);
        if (tvFamilyName != null) {
            tvFamilyName.setText("-");
        }
        bindDashboardLoading();
        bindMembersLoading();
    }

    private void fetchFamilyDashboard() {
        bindDashboardLoading();
        apiService.getFamilyDashboard().enqueue(new Callback<ApiResponse<DashboardData>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardData>> call, Response<ApiResponse<DashboardData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    bindFamilyDashboard(response.body().data);
                } else {
                    bindDashboardError(ApiUtils.errorMessage(response, "Gagal memuat dashboard family"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardData>> call, Throwable t) {
                bindDashboardError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    private void bindDashboardLoading() {
        TextView status = findViewById(R.id.tvDashboardStatus);
        if (status != null) {
            status.setVisibility(android.view.View.VISIBLE);
            status.setText("Memuat dashboard family...");
        }

        setDashboardSummary(0, 0, 0, 0);
        TextView flow = findViewById(R.id.tvFlowAnalysis);
        TextView budget = findViewById(R.id.tvBudgetAnalysis);
        if (flow != null) flow.setText("Flow: -");
        if (budget != null) budget.setText("Budget: -");
        bindRecentTransactions(null);
    }

    private void bindDashboardError(String message) {
        TextView status = findViewById(R.id.tvDashboardStatus);
        if (status != null) {
            status.setVisibility(android.view.View.VISIBLE);
            status.setText(message);
        }
        bindRecentTransactions(null);
    }

    private void bindFamilyDashboard(DashboardData dashboard) {
        TextView status = findViewById(R.id.tvDashboardStatus);
        if (status != null) {
            status.setVisibility(android.view.View.GONE);
        }

        long income = dashboard.currentMonth == null ? 0 : dashboard.currentMonth.income;
        long expense = dashboard.currentMonth == null ? 0 : dashboard.currentMonth.expense;
        setDashboardSummary(
                dashboard.totalFamilyWalletBalance,
                income,
                expense,
                dashboard.totalFamilySavings
        );

        TextView flow = findViewById(R.id.tvFlowAnalysis);
        if (flow != null) {
            DashboardData.FlowAnalysis flowAnalysis = dashboard.flowAnalysis;
            if (flowAnalysis == null) {
                flow.setText("Flow: -");
            } else {
                flow.setText("Flow " + flowAnalysis.period + ": masuk "
                        + FormatUtils.rupiah(flowAnalysis.income)
                        + ", keluar " + FormatUtils.rupiah(flowAnalysis.expense)
                        + ", net " + FormatUtils.rupiah(flowAnalysis.netFlow));
            }
        }

        TextView budget = findViewById(R.id.tvBudgetAnalysis);
        if (budget != null) {
            DashboardData.BudgetAnalysis budgetAnalysis = dashboard.budgetAnalysis;
            if (budgetAnalysis == null) {
                budget.setText("Budget: -");
            } else {
                budget.setText("Budget " + budgetAnalysis.period + ": limit "
                        + FormatUtils.rupiah(budgetAnalysis.totalMonthlyLimitBase)
                        + ", sisa " + FormatUtils.rupiah(budgetAnalysis.totalRemainingLimit)
                        + ", terpakai " + budgetAnalysis.usagePercent + "%");
            }
        }

        bindRecentTransactions(dashboard.recentTransactions);
    }

    private void setDashboardSummary(long balance, long income, long expense, long savings) {
        TextView tvBalance = findViewById(R.id.tvFamilyBalance);
        TextView tvIncome = findViewById(R.id.tvFamilyIncome);
        TextView tvExpense = findViewById(R.id.tvFamilyExpense);
        TextView tvSavings = findViewById(R.id.tvFamilySavings);

        if (tvBalance != null) tvBalance.setText(FormatUtils.rupiah(balance));
        if (tvIncome != null) tvIncome.setText(FormatUtils.rupiah(income));
        if (tvExpense != null) tvExpense.setText(FormatUtils.rupiah(expense));
        if (tvSavings != null) tvSavings.setText(FormatUtils.rupiah(savings));
    }

    private void bindRecentTransactions(List<Transaction> transactions) {
        TextView empty = findViewById(R.id.tvRecentEmpty);
        boolean hasTransactions = transactions != null && !transactions.isEmpty();
        if (empty != null) {
            empty.setVisibility(hasTransactions ? android.view.View.GONE : android.view.View.VISIBLE);
        }
        if (recentTransactionsAdapter != null) {
            recentTransactionsAdapter.setMemberNames(memberNames);
            recentTransactionsAdapter.submitList(transactions);
        }
    }

    private void fetchMembers() {
        bindMembersLoading();
        apiService.getFamilyMembers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    members = response.body().data;
                    bindMembers();
                } else {
                    members = null;
                    String message = ApiUtils.errorMessage(response, "Gagal memuat anggota");
                    bindMembersError(message);
                    Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                members = null;
                String message = "Koneksi gagal: " + t.getMessage();
                bindMembersError(message);
                Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindMembers() {
        if (members == null || members.isEmpty()) {
            if (memberAdapter != null) {
                memberAdapter.submitList(null);
            }
            showMemberState("Belum ada anggota family.", true);
            return;
        }

        memberNames.clear();
        showMemberState("", false);
        for (User user : members) {
            if (user.id != null) {
                memberNames.put(user.id, user.fullName == null ? "Anggota" : user.fullName);
            }
        }
        if (memberAdapter != null) {
            memberAdapter.submitList(members);
        }
        if (recentTransactionsAdapter != null) {
            recentTransactionsAdapter.setMemberNames(memberNames);
        }
    }

    private void bindMembersLoading() {
        if (memberAdapter != null) {
            memberAdapter.submitList(null);
        }
        showMemberState("Memuat anggota...", true);
    }

    private void bindMembersError(String message) {
        if (memberAdapter != null) {
            memberAdapter.submitList(null);
        }
        showMemberState(message, true);
    }

    private void showMemberState(String message, boolean visible) {
        TextView state = findViewById(R.id.tvMemberState);
        if (state == null) return;
        state.setText(message);
        state.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showEditFamilyNameDialog() {
        if (currentFamily == null || currentFamily.id == null) {
            Toast.makeText(this, "Data family belum siap", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_design_text, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        TextView title = view.findViewById(R.id.tvDialogTitle);
        TextView message = view.findViewById(R.id.tvDialogMessage);
        EditText input = view.findViewById(R.id.etDialogText);
        Button primary = view.findViewById(R.id.btnDialogPrimary);
        Button secondary = view.findViewById(R.id.btnDialogSecondary);

        title.setText("Edit nama family");
        message.setText("Nama ini akan terlihat oleh semua anggota family.");
        input.setText(currentFamily.familyName == null ? "" : currentFamily.familyName);
        input.setSelection(input.getText().length());
        primary.setText("Simpan");
        secondary.setText("Batal");

        primary.setOnClickListener(v -> {
            String familyName = input.getText() == null ? "" : input.getText().toString().trim();
            if (familyName.length() < 3) {
                input.setError("Nama family minimal 3 karakter");
                return;
            }
            setDialogLoading(dialog, true, "Simpan");
            updateFamilyName(familyName, dialog);
        });
        secondary.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });
        dialog.show();
    }

    private void updateFamilyName(String familyName, AlertDialog dialog) {
        apiService.updateFamily(currentFamily.id, new FamilyRequest(familyName, null))
                .enqueue(new Callback<ApiResponse<Family>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Family>> call, Response<ApiResponse<Family>> response) {
                        setDialogLoading(dialog, false, "Simpan");
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(AdminActivity.this, "Nama family berhasil diperbarui", Toast.LENGTH_SHORT).show();
                            refreshAdminData();
                        } else {
                            Toast.makeText(AdminActivity.this, ApiUtils.errorMessage(response, "Gagal memperbarui family"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Family>> call, Throwable t) {
                        setDialogLoading(dialog, false, "Simpan");
                        Toast.makeText(AdminActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateMemberRole(User user, String role, Button button) {
        if (user == null || user.id == null) return;
        button.setEnabled(false);
        button.setText("Menyimpan...");
        Map<String, String> request = new HashMap<>();
        request.put("role", role);
        apiService.updateMemberRole(user.id, request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                button.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Role anggota berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    refreshAdminData();
                } else {
                    button.setText("Jadikan " + role);
                    Toast.makeText(AdminActivity.this, ApiUtils.errorMessage(response, "Gagal memperbarui role"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                button.setEnabled(true);
                button.setText("Jadikan " + role);
                Toast.makeText(AdminActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmRemoveMember(User user) {
        if (user == null || user.id == null) return;
        DialogUtils.showConfirm(
                this,
                "Remove anggota",
                "Hapus " + (user.fullName == null ? "anggota ini" : user.fullName) + " dari family?",
                "Remove",
                "Batal",
                () -> removeMember(user)
        );
    }

    private void removeMember(User user) {
        bindMembersLoading();
        apiService.removeMember(user.id).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Anggota berhasil dihapus", Toast.LENGTH_SHORT).show();
                    refreshAdminData();
                } else {
                    bindMembers();
                    Toast.makeText(AdminActivity.this, ApiUtils.errorMessage(response, "Gagal menghapus anggota"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                bindMembers();
                Toast.makeText(AdminActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshAdminData() {
        fetchFamily();
        fetchFamilyDashboard();
        fetchMembers();
    }

    private void setDialogLoading(AlertDialog dialog, boolean loading, String idleText) {
        Button primary = dialog.findViewById(R.id.btnDialogPrimary);
        Button secondary = dialog.findViewById(R.id.btnDialogSecondary);
        if (primary != null) {
            primary.setEnabled(!loading);
            primary.setText(loading ? "Menyimpan..." : idleText);
        }
        if (secondary != null) {
            secondary.setEnabled(!loading);
        }
    }

    private String normalizedRole(String role) {
        return role == null || role.trim().isEmpty() ? "member" : role;
    }

    @Override
    public void onTransactionChanged() {
        refreshAdminData();
    }
}
