package com.kelompoksepuluh.famspay;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.Transaction;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;
import com.kelompoksepuluh.famspay.utils.TransactionTextUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionDetailActivity extends AppCompatActivity implements EditTransactionFragment.OnTransactionChangedListener {
    private String transactionId;
    private Transaction transaction;
    private ApiService apiService;
    private TextView tvTransName, tvAmount, tvDate, tvType, tvCategory, tvNote;
    private LinearLayout btnEdit, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        apiService = ApiClient.getClient(new SessionManager(this)).create(ApiService.class);
        transactionId = getIntent().getStringExtra("transaction_id");
        if (transactionId != null) {
            loadTransaction();
        } else {
            Toast.makeText(this, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvTransName = findViewById(R.id.tvTransName);
        tvAmount = findViewById(R.id.tvAmount);
        tvDate = findViewById(R.id.tvDate);
        tvType = findViewById(R.id.tvType);
        tvCategory = findViewById(R.id.tvCategory);
        tvNote = findViewById(R.id.tvNote);

        btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            if (isReadOnlyTransaction()) {
                Toast.makeText(this, "Transaksi sistem hanya bisa diubah dari modul terkait", Toast.LENGTH_SHORT).show();
                return;
            }
            EditTransactionFragment.newInstance(transactionId).show(getSupportFragmentManager(), "EditTransaction");
        });

        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> {
            if (isReadOnlyTransaction()) {
                Toast.makeText(this, "Transaksi sistem tidak bisa dihapus dari modul transaksi", Toast.LENGTH_SHORT).show();
                return;
            }
            showDeleteDialog();
        });
    }

    private void loadTransaction() {
        apiService.getTransaction(transactionId).enqueue(new Callback<ApiResponse<Transaction>>() {
            @Override
            public void onResponse(Call<ApiResponse<Transaction>> call, Response<ApiResponse<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    transaction = response.body().data;
                    bindTransaction();
                } else {
                    Toast.makeText(TransactionDetailActivity.this, ApiUtils.errorMessage(response, "Gagal memuat transaksi"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Transaction>> call, Throwable t) {
                Toast.makeText(TransactionDetailActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindTransaction() {
        tvTransName.setText(TransactionTextUtils.displayTitle(transaction));
        long signedAmount = "expense".equals(transaction.type) ? -transaction.amount : transaction.amount;
        tvAmount.setText((signedAmount < 0 ? "-" : "+") + FormatUtils.rupiah(Math.abs(signedAmount)));
        tvDate.setText(FormatUtils.displayDate(transaction.txnDate));
        tvType.setText("expense".equals(transaction.type) ? "Pengeluaran" : "Pemasukan");
        tvCategory.setText(transaction.categoryName);
        tvNote.setText(TransactionTextUtils.displayNote(transaction));

        int actionVisibility = isReadOnlyTransaction() ? View.GONE : View.VISIBLE;
        btnEdit.setVisibility(actionVisibility);
        btnDelete.setVisibility(actionVisibility);
    }

    private void showDeleteDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_transaction, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnYa = view.findViewById(R.id.btnConfirmDelete);
        Button btnBatal = view.findViewById(R.id.btnCancelDelete);

        btnYa.setOnClickListener(v -> {
            deleteTransaction(dialog);
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteTransaction(Dialog dialog) {
        if (transactionId == null) {
            dialog.dismiss();
            finish();
            return;
        }
        if (isReadOnlyTransaction()) {
            dialog.dismiss();
            Toast.makeText(this, "Transaksi sistem tidak bisa dihapus dari modul transaksi", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.deleteTransaction(transactionId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(TransactionDetailActivity.this, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(TransactionDetailActivity.this, ApiUtils.errorMessage(response, "Gagal menghapus transaksi"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(TransactionDetailActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTransactionChanged() {
        setResult(RESULT_OK);
        loadTransaction();
    }

    private boolean isReadOnlyTransaction() {
        return transaction != null && (transaction.readOnly || transaction.isSystem);
    }
}
