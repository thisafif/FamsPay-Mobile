package com.kelompoksepuluh.famspay;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.Transaction;
import com.kelompoksepuluh.famspay.data.model.TransactionRequest;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.DialogUtils;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;
import com.kelompoksepuluh.famspay.utils.TransactionCategoryUtils;
import com.kelompoksepuluh.famspay.utils.TransactionTextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTransactionFragment extends BottomSheetDialogFragment {
    private static final String ARG_TRANSACTION_ID = "transaction_id";
    private String transactionId;
    private String selectedType = "expense";
    private EditText etName, etAmount, etCategoryManual, etDate, etNote;
    private AutoCompleteTextView actvCategory;
    private View llManualCategory;
    private TextView btnExpense, btnIncome;
    private Button btnUpdate;
    private ApiService apiService;

    public interface OnTransactionChangedListener {
        void onTransactionChanged();
    }

    public static EditTransactionFragment newInstance() {
        return new EditTransactionFragment();
    }

    public static EditTransactionFragment newInstance(String transactionId) {
        EditTransactionFragment fragment = new EditTransactionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRANSACTION_ID, transactionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionId = getArguments() == null ? null : getArguments().getString(ARG_TRANSACTION_ID);
        apiService = ApiClient.getClient(new SessionManager(requireContext())).create(ApiService.class);
        etName = view.findViewById(R.id.etTransName);
        etAmount = view.findViewById(R.id.etAmount);
        actvCategory = view.findViewById(R.id.actvCategory);
        llManualCategory = view.findViewById(R.id.llManualCategory);
        etCategoryManual = view.findViewById(R.id.etCategoryManual);
        etDate = view.findViewById(R.id.etDate);
        etNote = view.findViewById(R.id.etNote);
        btnExpense = view.findViewById(R.id.btnExpense);
        btnIncome = view.findViewById(R.id.btnIncome);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        setupCategoryPicker();
        updateTypeUi();

        btnExpense.setOnClickListener(v -> selectType("expense"));
        btnIncome.setOnClickListener(v -> selectType("income"));
        etDate.setOnClickListener(v -> showDatePicker());
        btnUpdate.setOnClickListener(v -> submit(false));

        if (transactionId != null) {
            loadTransaction();
        }
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    private void selectType(String type) {
        selectedType = type;
        updateTypeUi();
    }

    private void updateTypeUi() {
        if (btnExpense == null || btnIncome == null || etAmount == null || btnUpdate == null) return;
        boolean expense = "expense".equals(selectedType);
        int activeColor = ContextCompat.getColor(requireContext(), expense ? R.color.trans_red : R.color.green);
        int white = ContextCompat.getColor(requireContext(), R.color.white);
        int gray = ContextCompat.getColor(requireContext(), R.color.gray);

        btnExpense.setBackgroundResource(expense ? R.drawable.bg_button_red : 0);
        btnIncome.setBackgroundResource(expense ? 0 : R.drawable.bg_button_green);
        btnExpense.setTextColor(expense ? white : gray);
        btnIncome.setTextColor(expense ? gray : white);
        btnExpense.setTypeface(null, expense ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        btnIncome.setTypeface(null, expense ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);
        etAmount.setTextColor(activeColor);
        btnUpdate.setBackgroundTintList(ColorStateList.valueOf(activeColor));
        btnUpdate.setText(expense ? R.string.edit_trans_submit_expense : R.string.edit_trans_submit_income);
    }

    private void loadTransaction() {
        apiService.getTransaction(transactionId).enqueue(new Callback<ApiResponse<Transaction>>() {
            @Override
            public void onResponse(Call<ApiResponse<Transaction>> call, Response<ApiResponse<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    Transaction transaction = response.body().data;
                    selectedType = transaction.type;
                    updateTypeUi();
                    etName.setText(TransactionTextUtils.displayTitle(transaction));
                    etAmount.setText(FormatUtils.rupiah(transaction.amount));
                    bindCategory(transaction.categoryName);
                    etDate.setText(FormatUtils.apiDateToDisplay(transaction.txnDate));
                    etNote.setText(TransactionTextUtils.displayNote(transaction));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Transaction>> call, Throwable t) {
                Toast.makeText(requireContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submit(boolean confirm) {
        if (transactionId == null) {
            dismiss();
            return;
        }
        TransactionRequest request = buildRequest(confirm);
        if (request == null) return;

        btnUpdate.setEnabled(false);
        Call<ApiResponse<Object>> call = confirm
                ? apiService.updateTransaction(transactionId, request)
                : apiService.previewEditTransaction(transactionId, request);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                btnUpdate.setEnabled(true);
                if (response.isSuccessful()) {
                    if (confirm) {
                        handleSuccess();
                    } else {
                        saveAfterPreview();
                    }
                    return;
                }
                if (response.code() == 422 && !confirm) {
                    showConfirmDialog(ApiUtils.warningMessage(response, "Perubahan membutuhkan konfirmasi"));
                } else if ((response.code() == 404 || response.code() == 405) && !confirm) {
                    saveAfterPreview();
                } else {
                    Toast.makeText(requireContext(), ApiUtils.errorMessage(response, "Gagal memperbarui transaksi"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                btnUpdate.setEnabled(true);
                Toast.makeText(requireContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAfterPreview() {
        TransactionRequest request = buildRequest(false);
        if (request == null) return;

        btnUpdate.setEnabled(false);
        apiService.updateTransaction(transactionId, request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                btnUpdate.setEnabled(true);
                if (response.isSuccessful()) {
                    handleSuccess();
                    return;
                }
                if (response.code() == 422) {
                    showConfirmDialog(ApiUtils.warningMessage(response, "Perubahan membutuhkan konfirmasi"));
                } else {
                    Toast.makeText(requireContext(), ApiUtils.errorMessage(response, "Gagal memperbarui transaksi"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                btnUpdate.setEnabled(true);
                Toast.makeText(requireContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccess() {
        Toast.makeText(requireContext(), "Transaksi berhasil diperbarui", Toast.LENGTH_SHORT).show();
        if (getActivity() instanceof OnTransactionChangedListener) {
            ((OnTransactionChangedListener) getActivity()).onTransactionChanged();
        }
        dismiss();
    }

    private TransactionRequest buildRequest(boolean confirm) {
        String title = etName.getText() == null ? "" : etName.getText().toString().trim();
        long amount = FormatUtils.parseRupiah(etAmount.getText() == null ? "" : etAmount.getText().toString());
        String category = selectedCategoryName();
        String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();

        if (amount <= 0) {
            etAmount.setError("Nominal harus lebih dari 0");
            return null;
        }
        if (title.isEmpty()) {
            etName.setError("Nama transaksi wajib diisi");
            return null;
        }
        String displayDate = etDate.getText() == null ? "" : etDate.getText().toString().trim();
        if (!FormatUtils.isValidDisplayDate(displayDate)) {
            etDate.setError("Tanggal tidak valid");
            return null;
        }
        if (FormatUtils.isFutureDisplayDate(displayDate)) {
            etDate.setError("Tanggal tidak boleh di masa depan");
            return null;
        }
        if (category.isEmpty()) {
            etCategoryManual.setError("Kategori wajib diisi");
            return null;
        }

        return new TransactionRequest(
                selectedType,
                amount,
                FormatUtils.toApiDate(displayDate),
                category,
                TransactionTextUtils.packNote(title, note),
                confirm
        );
    }

    private void setupCategoryPicker() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                TransactionCategoryUtils.DEFAULT_CATEGORIES
        );
        actvCategory.setAdapter(adapter);
        actvCategory.setThreshold(0);
        actvCategory.setText(TransactionCategoryUtils.DEFAULT_CATEGORIES[0], false);
        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
        actvCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actvCategory.showDropDown();
            }
        });
        actvCategory.setOnItemClickListener((parent, view, position, id) -> updateManualCategoryVisibility());
        updateManualCategoryVisibility();
    }

    private void bindCategory(String categoryName) {
        String category = categoryName == null ? "" : categoryName.trim();
        if (TransactionCategoryUtils.isDefaultCategory(category)) {
            actvCategory.setText(TransactionCategoryUtils.normalizeDefaultCategory(category), false);
            etCategoryManual.setText("");
        } else {
            actvCategory.setText(TransactionCategoryUtils.OTHER_CATEGORY, false);
            etCategoryManual.setText(category);
        }
        updateManualCategoryVisibility();
    }

    private void updateManualCategoryVisibility() {
        boolean isOther = TransactionCategoryUtils.OTHER_CATEGORY.equalsIgnoreCase(selectedCategoryOption());
        llManualCategory.setVisibility(isOther ? View.VISIBLE : View.GONE);
        if (!isOther) {
            etCategoryManual.setText("");
            etCategoryManual.setError(null);
        }
    }

    private String selectedCategoryOption() {
        return actvCategory.getText() == null ? "" : actvCategory.getText().toString().trim();
    }

    private String selectedCategoryName() {
        String selected = TransactionCategoryUtils.normalizeDefaultCategory(selectedCategoryOption());
        if (TransactionCategoryUtils.OTHER_CATEGORY.equalsIgnoreCase(selected)) {
            String manualCategory = etCategoryManual.getText() == null ? "" : etCategoryManual.getText().toString().trim();
            return manualCategory.isEmpty() ? TransactionCategoryUtils.OTHER_CATEGORY : manualCategory;
        }
        return selected;
    }

    private void showDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraints)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            TimeZone timeZone = TimeZone.getDefault();
            long offset = timeZone.getOffset(selection);
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTimeInMillis(selection - offset);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etDate.setText(sdf.format(calendar.getTime()));
        });

        datePicker.show(getChildFragmentManager(), "DATE_PICKER");
    }

    private void showConfirmDialog(String warning) {
        DialogUtils.showConfirm(
                requireContext(),
                "Konfirmasi perubahan",
                warning,
                "Lanjutkan",
                "Batal",
                () -> submit(true)
        );
    }
}
