package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity implements AddTransactionFragment.OnTransactionChangedListener {

    private static final String FILTER_DAILY = "daily";
    private static final String FILTER_MONTHLY = "monthly";
    private static final String FILTER_YEARLY = "yearly";

    private TextView tvCurrentMonth, tvDateRange, tabDaily, tabMonthly, tabYearly;
    private ApiService apiService;
    private TransactionListAdapter transactionAdapter;
    private final Calendar selectedPeriod = Calendar.getInstance();
    private String selectedFilter = FILTER_MONTHLY;

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
        setupTransactionList();
        setupPeriodControls();
        updatePeriodHeader();
        setupNavbar();
        hideInitialPlaceholders();
        apiService = ApiClient.getClient(new SessionManager(this)).create(ApiService.class);
        fetchTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (apiService != null) {
            fetchTransactions();
        }
    }

    private void initViews() {
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvDateRange = findViewById(R.id.tvDateRange);
        tabDaily = findViewById(R.id.tabDaily);
        tabMonthly = findViewById(R.id.tabMonthly);
        tabYearly = findViewById(R.id.tabYearly);
    }

    private void setupTransactionList() {
        RecyclerView recyclerView = findViewById(R.id.rvHistoryTransactions);
        if (recyclerView == null) return;
        transactionAdapter = new TransactionListAdapter(false, transaction -> {
            Intent intent = new Intent(HistoryActivity.this, TransactionDetailActivity.class);
            intent.putExtra("transaction_id", transaction.id);
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(transactionAdapter);
    }

    private void setupPeriodControls() {
        tabDaily.setOnClickListener(v -> selectFilter(FILTER_DAILY));
        tabMonthly.setOnClickListener(v -> selectFilter(FILTER_MONTHLY));
        tabYearly.setOnClickListener(v -> selectFilter(FILTER_YEARLY));
        findViewById(R.id.btnPreviousPeriod).setOnClickListener(v -> shiftPeriod(-1));
        findViewById(R.id.btnNextPeriod).setOnClickListener(v -> shiftPeriod(1));
        updateFilterTabs();
    }

    private void selectFilter(String filter) {
        selectedFilter = filter;
        updateFilterTabs();
        updatePeriodHeader();
        fetchTransactions();
    }

    private void shiftPeriod(int direction) {
        if (FILTER_DAILY.equals(selectedFilter)) {
            selectedPeriod.add(Calendar.DAY_OF_MONTH, direction);
        } else if (FILTER_YEARLY.equals(selectedFilter)) {
            selectedPeriod.add(Calendar.YEAR, direction);
        } else {
            selectedPeriod.add(Calendar.MONTH, direction);
        }
        updatePeriodHeader();
        fetchTransactions();
    }

    private void updateFilterTabs() {
        bindFilterTab(tabDaily, FILTER_DAILY.equals(selectedFilter));
        bindFilterTab(tabMonthly, FILTER_MONTHLY.equals(selectedFilter));
        bindFilterTab(tabYearly, FILTER_YEARLY.equals(selectedFilter));
    }

    private void bindFilterTab(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.bg_button_green : R.drawable.bg_transaction_input);
        tab.setTextColor(getColor(selected ? R.color.white : R.color.gray));
        tab.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void updatePeriodHeader() {
        Locale idLocale = new Locale("id", "ID");
        if (FILTER_DAILY.equals(selectedFilter)) {
            SimpleDateFormat titleFormat = new SimpleDateFormat("EEEE", idLocale);
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", idLocale);
            tvCurrentMonth.setText(titleFormat.format(selectedPeriod.getTime()));
            tvDateRange.setText(dateFormat.format(selectedPeriod.getTime()));
            return;
        }

        if (FILTER_YEARLY.equals(selectedFilter)) {
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", idLocale);
            tvCurrentMonth.setText(yearFormat.format(selectedPeriod.getTime()));
            tvDateRange.setText("(1 Jan - 31 Des)");
            return;
        }

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", idLocale);
        tvCurrentMonth.setText(monthFormat.format(selectedPeriod.getTime()));

        Calendar range = (Calendar) selectedPeriod.clone();
        SimpleDateFormat rangeFormat = new SimpleDateFormat("d MMM", idLocale);
        range.set(Calendar.DAY_OF_MONTH, 1);
        String firstDay = rangeFormat.format(range.getTime());
        range.set(Calendar.DAY_OF_MONTH, range.getActualMaximum(Calendar.DAY_OF_MONTH));
        String lastDay = rangeFormat.format(range.getTime());

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

        findViewById(R.id.btnNavTarget).setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, GoalActivity.class);
            startActivity(intent);
        });

        View btnNavAdmin = findViewById(R.id.btnNavAdmin);
        if (btnNavAdmin != null) {
            btnNavAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(HistoryActivity.this, AdminActivity.class);
                startActivity(intent);
            });
        }

    }

    private void hideInitialPlaceholders() {
        hideLegacyTransactionSlots();
        bindHistoryStats(null);
        bindHistoryState("Memuat transaksi...", true);
        if (transactionAdapter != null) {
            transactionAdapter.submitList(null);
        }
    }

    private void hideLegacyTransactionSlots() {
        int[] ids = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.tvDateGroup2};
        for (int id : ids) {
            View view = findViewById(id);
            if (view != null) view.setVisibility(View.GONE);
        }
    }

    private void fetchTransactions() {
        bindHistoryStats(null);
        bindHistoryState("Memuat transaksi...", true);
        Calendar calendar = (Calendar) selectedPeriod.clone();
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateFrom;
        String dateTo;

        if (FILTER_DAILY.equals(selectedFilter)) {
            dateFrom = apiFormat.format(calendar.getTime());
            dateTo = dateFrom;
        } else if (FILTER_YEARLY.equals(selectedFilter)) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            dateFrom = apiFormat.format(calendar.getTime());
            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
            calendar.set(Calendar.DAY_OF_MONTH, 31);
            dateTo = apiFormat.format(calendar.getTime());
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            dateFrom = apiFormat.format(calendar.getTime());
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            dateTo = apiFormat.format(calendar.getTime());
        }

        Map<String, String> filters = new HashMap<>();
        filters.put("date_from", dateFrom);
        filters.put("date_to", dateTo);

        apiService.getTransactions(filters).enqueue(new Callback<ApiResponse<List<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindTransactions(response.body().data);
                } else {
                    String message = ApiUtils.errorMessage(response, "Gagal memuat transaksi");
                    bindHistoryStats(null);
                    bindHistoryState(message, true);
                    Toast.makeText(HistoryActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Transaction>>> call, Throwable t) {
                String message = "Koneksi gagal: " + t.getMessage();
                bindHistoryStats(null);
                bindHistoryState(message, true);
                Toast.makeText(HistoryActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindTransactions(List<Transaction> transactions) {
        bindHistoryStats(transactions);

        if (transactionAdapter != null) {
            transactionAdapter.submitList(transactions);
        }
        boolean hasTransactions = transactions != null && !transactions.isEmpty();
        bindHistoryState(hasTransactions ? "" : "Belum ada transaksi pada periode ini.", !hasTransactions);
    }

    private void bindHistoryStats(List<Transaction> transactions) {
        long totalIncome = 0;
        long totalExpense = 0;
        int transactionCount = 0;
        if (transactions != null) {
            for (Transaction transaction : transactions) {
                transactionCount++;
                String type = transaction.type == null ? "expense" : transaction.type;
                if ("income".equalsIgnoreCase(type)) {
                    totalIncome += transaction.amount;
                } else if ("expense".equalsIgnoreCase(type)) {
                    totalExpense += transaction.amount;
                }
            }
        }

        IncomeExpenseDonutView donutView = findViewById(R.id.viewHistoryIncomeExpenseDonut);
        if (donutView != null) {
            donutView.setData(totalIncome, totalExpense);
        }

        TextView totalSpending = findViewById(R.id.tvHistoryTotalSpending);
        if (totalSpending != null) {
            totalSpending.setText(FormatUtils.rupiah(totalExpense));
        }

        long net = totalIncome - totalExpense;
        long totalFlow = totalIncome + totalExpense;
        int expensePercent = totalFlow <= 0 ? 0 : Math.round((totalExpense * 100f) / totalFlow);

        setText(R.id.tvHistoryFlowSummary, "Net " + (net < 0 ? "-" : "+") + FormatUtils.rupiah(Math.abs(net)));
        setText(R.id.tvHistoryExpensePercent, "Pengeluaran " + expensePercent + "% dari arus");
        setText(R.id.tvHistoryIncomeTotal, "Pemasukan\n" + FormatUtils.rupiah(totalIncome));
        setText(R.id.tvHistoryExpenseTotal, "Pengeluaran\n" + FormatUtils.rupiah(totalExpense));
        setText(R.id.tvHistoryNetTotal, "Net\n" + (net < 0 ? "-" : "+") + FormatUtils.rupiah(Math.abs(net)));
        setText(R.id.tvHistoryTransactionCount, transactionCount + " transaksi");
    }

    private void setText(int viewId, String value) {
        TextView textView = findViewById(viewId);
        if (textView != null) {
            textView.setText(value);
        }
    }

    private void bindHistoryState(String message, boolean visible) {
        TextView state = findViewById(R.id.tvHistoryState);
        if (state == null) return;
        state.setText(message);
        state.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onTransactionChanged() {
        fetchTransactions();
    }
}
