package com.kelompoksepuluh.famspay;

import android.os.Bundle;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.kelompoksepuluh.famspay.data.model.ApiResponse;
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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionFragment extends BottomSheetDialogFragment {
    private String selectedType = "expense";
    private EditText etName, etAmount, etCategoryManual, etDate, etNote;
    private AutoCompleteTextView actvCategory;
    private View llManualCategory;
    private TextView btnExpense, btnIncome;
    private Button btnSubmit;
    private ApiService apiService;

    public interface OnTransactionChangedListener {
        void onTransactionChanged();
    }

    public static AddTransactionFragment newInstance() {
        return new AddTransactionFragment();
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
        return inflater.inflate(R.layout.fragment_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
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
        btnSubmit = view.findViewById(R.id.btnSubmit);

        setCurrentDate(etDate);
        setupCategoryPicker();
        updateTypeUi();

        etDate.setOnClickListener(v -> showDatePicker(etDate));
        btnExpense.setOnClickListener(v -> selectType("expense"));
        btnIncome.setOnClickListener(v -> selectType("income"));
        btnSubmit.setOnClickListener(v -> submit(false));
    }

    private void selectType(String type) {
        selectedType = type;
        updateTypeUi();
    }

    private void updateTypeUi() {
        if (btnExpense == null || btnIncome == null || etAmount == null || btnSubmit == null) return;
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
        btnSubmit.setBackgroundTintList(ColorStateList.valueOf(activeColor));
        btnSubmit.setText(expense ? R.string.add_trans_submit_expense : R.string.add_trans_submit_income);
    }

    private void setCurrentDate(EditText etDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDate.setText(sdf.format(new Date()));
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

    private void updateManualCategoryVisibility() {
        boolean isOther = TransactionCategoryUtils.OTHER_CATEGORY.equalsIgnoreCase(selectedCategoryOption());
        llManualCategory.setVisibility(isOther ? View.VISIBLE : View.GONE);
        if (!isOther) {
            etCategoryManual.setText("");
            etCategoryManual.setError(null);
        }
    }

    private void showDatePicker(EditText etDate) {
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

    private void submit(boolean confirm) {
        TransactionRequest request = buildRequest(confirm);
        if (request == null) return;

        btnSubmit.setEnabled(false);
        Call<ApiResponse<Object>> call = confirm
                ? apiService.createTransaction(request)
                : apiService.previewTransaction(request);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                btnSubmit.setEnabled(true);
                if (response.isSuccessful()) {
                    if (confirm) {
                        handleSuccess();
                    } else {
                        saveAfterPreview();
                    }
                    return;
                }
                if (response.code() == 422 && !confirm) {
                    showConfirmDialog(ApiUtils.warningMessage(response, "Transaksi membutuhkan konfirmasi"));
                } else if ((response.code() == 404 || response.code() == 405) && !confirm) {
                    saveAfterPreview();
                } else {
                    Toast.makeText(requireContext(), ApiUtils.errorMessage(response, "Gagal menyimpan transaksi"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                btnSubmit.setEnabled(true);
                Toast.makeText(requireContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAfterPreview() {
        TransactionRequest request = buildRequest(false);
        if (request == null) return;

        btnSubmit.setEnabled(false);
        apiService.createTransaction(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                btnSubmit.setEnabled(true);
                if (response.isSuccessful()) {
                    handleSuccess();
                    return;
                }
                if (response.code() == 422) {
                    showConfirmDialog(ApiUtils.warningMessage(response, "Transaksi membutuhkan konfirmasi"));
                } else {
                    Toast.makeText(requireContext(), ApiUtils.errorMessage(response, "Gagal menyimpan transaksi"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                btnSubmit.setEnabled(true);
                Toast.makeText(requireContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccess() {
        Toast.makeText(requireContext(), "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show();
        if (getActivity() instanceof OnTransactionChangedListener) {
            ((OnTransactionChangedListener) getActivity()).onTransactionChanged();
        }
        dismiss();
    }

    private TransactionRequest buildRequest(boolean confirm) {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        long amount = FormatUtils.parseRupiah(etAmount.getText() == null ? "" : etAmount.getText().toString());
        String category = selectedCategoryName();
        String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();

        if (amount <= 0) {
            etAmount.setError("Nominal harus lebih dari 0");
            return null;
        }
        if (name.isEmpty()) {
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
                TransactionTextUtils.packNote(name, note),
                confirm
        );
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

    private void showConfirmDialog(String warning) {
        DialogUtils.showConfirm(
                requireContext(),
                "Konfirmasi transaksi",
                warning,
                "Lanjutkan",
                "Batal",
                () -> submit(true)
        );
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }
}
