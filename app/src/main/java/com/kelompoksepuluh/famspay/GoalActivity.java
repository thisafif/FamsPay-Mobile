package com.kelompoksepuluh.famspay;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.kelompoksepuluh.famspay.data.model.Goal;
import com.kelompoksepuluh.famspay.data.model.GoalRequest;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.DialogUtils;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalActivity extends AppCompatActivity implements AddTransactionFragment.OnTransactionChangedListener, AddGoalFragment.OnGoalCreatedListener, EditGoalFragment.OnGoalChangedListener {
    private ApiService apiService;
    private List<Goal> goals;

    private interface GoalClickAction {
        void onClick(Goal goal);
    }

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
        apiService = ApiClient.getClient(new SessionManager(this)).create(ApiService.class);
        showGoalState("Memuat target...", true);
        bindGoals(null);
        fetchGoals();
    }

    private void setupNavbar() {
        findViewById(R.id.btnNavHome).setOnClickListener(v -> {
            Intent intent = new Intent(GoalActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnNavHistory).setOnClickListener(v -> {
            Intent intent = new Intent(GoalActivity.this, HistoryActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnNavAdmin).setOnClickListener(v -> {
            Intent intent = new Intent(GoalActivity.this, AdminActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            AddTransactionFragment.newInstance().show(getSupportFragmentManager(), "AddTransaction");
        });
    }

    private void setupActions() {
        findViewById(R.id.btnAddTarget).setOnClickListener(v -> {
            AddGoalFragment.newInstance().show(getSupportFragmentManager(), "AddGoal");
        });

        setupEditActions();
        setupDeleteActions();
        setupAllocateActions();
        setupWithdrawActions();
    }

    private void setupEditActions() {
        int[] editIds = {R.id.btnEdit1, R.id.btnEdit2, R.id.btnEdit3, R.id.btnEdit4};
        bindGoalActions(editIds, goal ->
                EditGoalFragment.newInstance(goal).show(getSupportFragmentManager(), "EditGoal"));
    }

    private void setupAllocateActions() {
        int[] allocateIds = {R.id.btnAllocate1, R.id.btnAllocate2, R.id.btnAllocate3, R.id.btnAllocate4};
        bindGoalActions(allocateIds, this::showAllocateDialog);
    }

    private void setupWithdrawActions() {
        int[] withdrawIds = {R.id.btnWithdraw1, R.id.btnWithdraw2, R.id.btnWithdraw3, R.id.btnWithdraw4};
        bindGoalActions(withdrawIds, this::showWithdrawDialog);
    }

    private void setupDeleteActions() {
        int[] deleteIds = {R.id.btnDelete1, R.id.btnDelete2, R.id.btnDelete3, R.id.btnDelete4};
        bindGoalActions(deleteIds, this::showDeleteGoalDialog);
    }

    private void bindGoalActions(int[] viewIds, GoalClickAction action) {
        for (int i = 0; i < viewIds.length; i++) {
            final int position = i;
            View view = findViewById(viewIds[i]);
            if (view != null) {
                view.setOnClickListener(v -> {
                    Goal goal = getDisplayedGoal(position);
                    if (goal == null) {
                        Toast.makeText(this, "Target tidak ditemukan", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    action.onClick(goal);
                });
            }
        }
    }

    private Goal getDisplayedGoal(int position) {
        if (goals == null || position < 0 || position >= goals.size()) {
            return null;
        }
        return goals.get(position);
    }

    private void showDeleteGoalDialog(Goal goal) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_goal, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnYa = view.findViewById(R.id.btnConfirmDelete);
        Button btnBatal = view.findViewById(R.id.btnCancelDelete);

        btnYa.setOnClickListener(v -> {
            archiveGoal(dialog, goal.id);
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchGoals() {
        showGoalState("Memuat target...", true);
        bindGoals(null);
        apiService.getGoals(false).enqueue(new Callback<ApiResponse<List<Goal>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Goal>>> call, Response<ApiResponse<List<Goal>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    goals = response.body().data;
                    bindGoals(goals);
                } else {
                    goals = null;
                    String message = ApiUtils.errorMessage(response, "Gagal memuat target");
                    bindGoals(null);
                    showGoalState(message, true);
                    Toast.makeText(GoalActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Goal>>> call, Throwable t) {
                goals = null;
                String message = "Koneksi gagal: " + t.getMessage();
                bindGoals(null);
                showGoalState(message, true);
                Toast.makeText(GoalActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindGoals(List<Goal> goals) {
        boolean hasGoals = goals != null && !goals.isEmpty();
        if (goals != null) {
            showGoalState(hasGoals ? "" : "Belum ada target tabungan. Tambahkan target pertama kamu.", !hasGoals);
        }

        int[] cardIds = {R.id.cardGoal1, R.id.cardGoal2, R.id.cardGoal3, R.id.cardGoal4};
        int[] nameIds = {R.id.tvGoalName1, R.id.tvGoalName2, R.id.tvGoalName3, R.id.tvGoalName4};
        int[] amountIds = {R.id.tvGoalAmount1, R.id.tvGoalAmount2, R.id.tvGoalAmount3, R.id.tvGoalAmount4};
        int[] statusIds = {R.id.tvGoalStatus1, R.id.tvGoalStatus2, R.id.tvGoalStatus3, R.id.tvGoalStatus4};
        int[] allocateIds = {R.id.btnAllocate1, R.id.btnAllocate2, R.id.btnAllocate3, R.id.btnAllocate4};
        int[] withdrawIds = {R.id.btnWithdraw1, R.id.btnWithdraw2, R.id.btnWithdraw3, R.id.btnWithdraw4};
        int[] progressIds = {R.id.progressGoal1, R.id.progressGoal2, R.id.progressGoal3, R.id.progressGoal4};

        for (int i = 0; i < cardIds.length; i++) {
            View card = findViewById(cardIds[i]);
            TextView name = findViewById(nameIds[i]);
            TextView amount = findViewById(amountIds[i]);
            TextView status = findViewById(statusIds[i]);
            View allocate = findViewById(allocateIds[i]);
            View withdraw = findViewById(withdrawIds[i]);
            ProgressBar progress = findViewById(progressIds[i]);
            if (goals != null && i < goals.size()) {
                Goal goal = goals.get(i);
                boolean completed = isCompletedGoal(goal);
                card.setVisibility(View.VISIBLE);
                name.setText(goal.title);
                amount.setText(FormatUtils.rupiah(goal.currentAmount) + " / " + FormatUtils.rupiah(goal.targetAmount));
                if (status != null) {
                    status.setVisibility(completed ? View.VISIBLE : View.GONE);
                    status.setText("Completed");
                }
                if (allocate != null) {
                    allocate.setVisibility(View.VISIBLE);
                }
                if (withdraw != null) {
                    withdraw.setVisibility(goal.currentAmount > 0 ? View.VISIBLE : View.GONE);
                }
                if (progress != null) {
                    int percentage = completed
                            ? 100
                            : goal.targetAmount <= 0
                            ? 0
                            : (int) Math.min(100, (goal.currentAmount * 100) / goal.targetAmount);
                    progress.setVisibility(View.VISIBLE);
                    progress.setProgress(percentage);
                }
            } else if (card != null) {
                card.setVisibility(View.GONE);
            }
        }
    }

    private boolean isCompletedGoal(Goal goal) {
        return goal != null && ("completed".equalsIgnoreCase(goal.status)
                || (goal.targetAmount > 0 && goal.currentAmount >= goal.targetAmount));
    }

    private void showGoalState(String message, boolean visible) {
        TextView state = findViewById(R.id.tvGoalState);
        if (state == null) {
            return;
        }
        state.setText(message);
        state.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showAllocateDialog(Goal goal) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_design_amount, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        TextView title = view.findViewById(R.id.tvDialogTitle);
        TextView message = view.findViewById(R.id.tvDialogMessage);
        EditText etAmount = view.findViewById(R.id.etDialogAmount);
        Button btnPrimary = view.findViewById(R.id.btnDialogPrimary);
        Button btnSecondary = view.findViewById(R.id.btnDialogSecondary);

        title.setText("Alokasi dana");
        message.setText(goal.title);
        btnPrimary.setText("Simpan");
        btnSecondary.setText("Batal");

        btnPrimary.setOnClickListener(v -> {
            long amount = FormatUtils.parseRupiah(etAmount.getText() == null ? "" : etAmount.getText().toString());
            if (amount <= 0) {
                etAmount.setError("Nominal harus lebih dari 0");
                return;
            }
            setAllocateDialogLoading(dialog, true);
            previewAllocate(goal.id, amount, dialog);
        });
        btnSecondary.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        dialog.show();
    }

    private void previewAllocate(String goalId, long amount, AlertDialog dialog) {
        apiService.previewAllocateGoal(goalId, new GoalRequest(amount, false))
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful()) {
                            allocateGoal(goalId, amount, false, dialog);
                            return;
                        }
                        setAllocateDialogLoading(dialog, false);
                        if (response.code() == 422) {
                            showAllocateConfirmDialog(goalId, amount, dialog,
                                    ApiUtils.warningMessage(response, "Alokasi membutuhkan konfirmasi"));
                        } else if (response.code() == 404 || response.code() == 405) {
                            setAllocateDialogLoading(dialog, true);
                            allocateGoal(goalId, amount, false, dialog);
                        } else {
                            Toast.makeText(GoalActivity.this, ApiUtils.errorMessage(response, "Gagal memvalidasi alokasi"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        setAllocateDialogLoading(dialog, false);
                        Toast.makeText(GoalActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAllocateConfirmDialog(String goalId, long amount, AlertDialog allocateDialog, String warning) {
        DialogUtils.showConfirm(
                this,
                "Konfirmasi alokasi",
                warning,
                "Lanjutkan",
                "Batal",
                () -> {
                    setAllocateDialogLoading(allocateDialog, true);
                    allocateGoal(goalId, amount, true, allocateDialog);
                }
        );
    }

    private void allocateGoal(String goalId, long amount, boolean confirm, AlertDialog dialog) {
        apiService.allocateGoal(goalId, new GoalRequest(amount, confirm))
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        setAllocateDialogLoading(dialog, false);
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(GoalActivity.this, "Dana berhasil dialokasikan", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            fetchGoals();
                        } else if (response.code() == 422 && !confirm) {
                            showAllocateConfirmDialog(goalId, amount, dialog,
                                    ApiUtils.warningMessage(response, "Alokasi membutuhkan konfirmasi"));
                        } else {
                            Toast.makeText(GoalActivity.this, ApiUtils.errorMessage(response, "Gagal mengalokasikan dana"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        setAllocateDialogLoading(dialog, false);
                        Toast.makeText(GoalActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showWithdrawDialog(Goal goal) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_design_amount, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        TextView title = view.findViewById(R.id.tvDialogTitle);
        TextView message = view.findViewById(R.id.tvDialogMessage);
        EditText etAmount = view.findViewById(R.id.etDialogAmount);
        Button btnPrimary = view.findViewById(R.id.btnDialogPrimary);
        Button btnSecondary = view.findViewById(R.id.btnDialogSecondary);

        title.setText("Withdraw dana");
        message.setText(goal.title + " - tersedia " + FormatUtils.rupiah(goal.currentAmount));
        btnPrimary.setText("Tarik");
        btnSecondary.setText("Batal");

        btnPrimary.setOnClickListener(v -> {
            long amount = FormatUtils.parseRupiah(etAmount.getText() == null ? "" : etAmount.getText().toString());
            if (amount <= 0) {
                etAmount.setError("Nominal harus lebih dari 0");
                return;
            }
            if (amount > goal.currentAmount) {
                etAmount.setError("Nominal melebihi dana target");
                return;
            }
            setAmountDialogLoading(dialog, true, "Tarik");
            withdrawGoal(goal.id, amount, dialog);
        });
        btnSecondary.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        dialog.show();
    }

    private void withdrawGoal(String goalId, long amount, AlertDialog dialog) {
        apiService.withdrawGoal(goalId, new GoalRequest(amount, false))
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        setAmountDialogLoading(dialog, false, "Tarik");
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(GoalActivity.this, "Dana berhasil ditarik", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            fetchGoals();
                        } else {
                            Toast.makeText(GoalActivity.this, ApiUtils.errorMessage(response, "Gagal menarik dana"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        setAmountDialogLoading(dialog, false, "Tarik");
                        Toast.makeText(GoalActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setAllocateDialogLoading(AlertDialog dialog, boolean loading) {
        setAmountDialogLoading(dialog, loading, "Simpan");
    }

    private void setAmountDialogLoading(AlertDialog dialog, boolean loading, String idleText) {
        Button positive = dialog.findViewById(R.id.btnDialogPrimary);
        Button negative = dialog.findViewById(R.id.btnDialogSecondary);
        if (positive != null) {
            positive.setEnabled(!loading);
            positive.setText(loading ? "Menyimpan..." : idleText);
        }
        if (negative != null) {
            negative.setEnabled(!loading);
        }
    }

    private void archiveGoal(AlertDialog dialog, String goalId) {
        if (goalId == null) {
            dialog.dismiss();
            return;
        }
        apiService.archiveGoal(goalId).enqueue(new Callback<ApiResponse<Goal>>() {
            @Override
            public void onResponse(Call<ApiResponse<Goal>> call, Response<ApiResponse<Goal>> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(GoalActivity.this, "Target berhasil dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    fetchGoals();
                } else {
                    Toast.makeText(GoalActivity.this, ApiUtils.errorMessage(response, "Gagal mengarsipkan target"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Goal>> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(GoalActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTransactionChanged() {
        fetchGoals();
    }

    @Override
    public void onGoalCreated() {
        fetchGoals();
    }

    @Override
    public void onGoalChanged() {
        fetchGoals();
    }
}
