package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.kelompoksepuluh.famspay.data.model.Transaction;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFamilyHistoryActivity extends AppCompatActivity {
    private static final String TYPE_ALL = "Semua";
    private static final String TYPE_INCOME = "Income";
    private static final String TYPE_EXPENSE = "Expense";
    private static final String MEMBER_ALL = "Semua anggota";

    private ApiService apiService;
    private TransactionListAdapter adapter;
    private final Calendar selectedMonth = Calendar.getInstance();
    private final List<Transaction> familyTransactions = new ArrayList<>();
    private final Map<String, String> memberNames = new HashMap<>();
    private final Map<String, String> memberOptionToId = new HashMap<>();
    private AutoCompleteTextView typeFilter;
    private AutoCompleteTextView memberFilter;
    private TextView selectedMonthText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_family_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Halaman admin hanya untuk admin family", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient(sessionManager).create(ApiService.class);
        initViews();
        fetchMembers();
        fetchTransactions();
    }

    private void initViews() {
        selectedMonthText = findViewById(R.id.tvSelectedMonth);
        typeFilter = findViewById(R.id.actvTypeFilter);
        memberFilter = findViewById(R.id.actvMemberFilter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            selectedMonth.add(Calendar.MONTH, -1);
            fetchTransactions();
        });
        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            selectedMonth.add(Calendar.MONTH, 1);
            fetchTransactions();
        });

        setupTypeFilter();
        setupMemberFilter(new ArrayList<>());
        setupTransactionList();
        updateMonthLabel();
    }

    private void setupTypeFilter() {
        String[] options = {TYPE_ALL, TYPE_INCOME, TYPE_EXPENSE};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, options);
        typeFilter.setAdapter(typeAdapter);
        typeFilter.setThreshold(0);
        typeFilter.setText(TYPE_ALL, false);
        typeFilter.setOnClickListener(v -> typeFilter.showDropDown());
        typeFilter.setOnItemClickListener((parent, view, position, id) -> fetchTransactions());
    }

    private void setupMemberFilter(List<User> members) {
        List<String> options = new ArrayList<>();
        options.add(MEMBER_ALL);
        memberOptionToId.clear();
        memberOptionToId.put(MEMBER_ALL, null);
        memberNames.clear();
        for (User member : members) {
            String name = member.fullName == null ? "Anggota" : member.fullName;
            options.add(name);
            memberOptionToId.put(name, member.id);
            if (member.id != null) {
                memberNames.put(member.id, name);
            }
        }
        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, options);
        memberFilter.setAdapter(memberAdapter);
        memberFilter.setThreshold(0);
        memberFilter.setText(MEMBER_ALL, false);
        memberFilter.setOnClickListener(v -> memberFilter.showDropDown());
        memberFilter.setOnItemClickListener((parent, view, position, id) -> applyMemberFilter());
        if (adapter != null) {
            adapter.setMemberNames(memberNames);
        }
    }

    private void setupTransactionList() {
        RecyclerView recyclerView = findViewById(R.id.rvAdminFamilyHistory);
        adapter = new TransactionListAdapter(true, transaction -> {
            Intent intent = new Intent(AdminFamilyHistoryActivity.this, TransactionDetailActivity.class);
            intent.putExtra("transaction_id", transaction.id);
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);
    }

    private void fetchMembers() {
        apiService.getFamilyMembers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    setupMemberFilter(response.body().data);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                Toast.makeText(AdminFamilyHistoryActivity.this, "Gagal memuat anggota: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTransactions() {
        updateMonthLabel();
        bindState("Memuat transaksi family...", true);
        adapter.submitList(null);

        Calendar from = (Calendar) selectedMonth.clone();
        from.set(Calendar.DAY_OF_MONTH, 1);
        Calendar to = (Calendar) selectedMonth.clone();
        to.set(Calendar.DAY_OF_MONTH, to.getActualMaximum(Calendar.DAY_OF_MONTH));

        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Map<String, String> filters = new HashMap<>();
        filters.put("date_from", apiFormat.format(from.getTime()));
        filters.put("date_to", apiFormat.format(to.getTime()));
        String selectedType = selectedType();
        if (!TYPE_ALL.equals(selectedType)) {
            filters.put("type", TYPE_INCOME.equals(selectedType) ? "income" : "expense");
        }

        apiService.getTransactions(filters).enqueue(new Callback<ApiResponse<List<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    familyTransactions.clear();
                    if (response.body().data != null) {
                        familyTransactions.addAll(response.body().data);
                    }
                    applyMemberFilter();
                } else {
                    bindState(ApiUtils.errorMessage(response, "Gagal memuat transaksi family"), true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Transaction>>> call, Throwable t) {
                bindState("Koneksi gagal: " + t.getMessage(), true);
            }
        });
    }

    private void applyMemberFilter() {
        String selectedMember = selectedMember();
        String selectedUserId = memberOptionToId.get(selectedMember);
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction transaction : familyTransactions) {
            if (selectedUserId == null || selectedUserId.equals(transaction.userId)) {
                filtered.add(transaction);
            }
        }
        adapter.setMemberNames(memberNames);
        adapter.submitList(filtered);
        bindSummary(filtered);
        bindState(filtered.isEmpty() ? "Belum ada transaksi family pada filter ini." : "", filtered.isEmpty());
    }

    private void bindSummary(List<Transaction> transactions) {
        long income = 0;
        long expense = 0;
        for (Transaction transaction : transactions) {
            if ("income".equalsIgnoreCase(transaction.type)) {
                income += transaction.amount;
            } else if ("expense".equalsIgnoreCase(transaction.type)) {
                expense += transaction.amount;
            }
        }
        TextView summary = findViewById(R.id.tvHistorySummary);
        summary.setText("Masuk " + FormatUtils.rupiah(income)
                + " | Keluar " + FormatUtils.rupiah(expense)
                + " | Net " + FormatUtils.rupiah(income - expense));
    }

    private void bindState(String message, boolean visible) {
        TextView state = findViewById(R.id.tvAdminHistoryState);
        state.setText(message);
        state.setVisibility(visible ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void updateMonthLabel() {
        if (selectedMonthText == null) return;
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"));
        selectedMonthText.setText(monthFormat.format(selectedMonth.getTime()));
    }

    private String selectedType() {
        return typeFilter.getText() == null ? TYPE_ALL : typeFilter.getText().toString();
    }

    private String selectedMember() {
        return memberFilter.getText() == null ? MEMBER_ALL : memberFilter.getText().toString();
    }
}
