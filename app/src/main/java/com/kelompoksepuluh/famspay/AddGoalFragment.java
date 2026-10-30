package com.kelompoksepuluh.famspay;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.Goal;
import com.kelompoksepuluh.famspay.data.model.GoalRequest;
import com.kelompoksepuluh.famspay.data.remote.ApiClient;
import com.kelompoksepuluh.famspay.data.remote.ApiService;
import com.kelompoksepuluh.famspay.utils.ApiUtils;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddGoalFragment extends BottomSheetDialogFragment {
    private EditText etGoalName, etAmount;
    private Button btnSubmit;
    private ApiService apiService;
    private OnGoalCreatedListener listener;

    public interface OnGoalCreatedListener {
        void onGoalCreated();
    }

    public static AddGoalFragment newInstance() {
        return new AddGoalFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnGoalCreatedListener) {
            listener = (OnGoalCreatedListener) context;
        }
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
        return inflater.inflate(R.layout.fragment_add_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getClient(new SessionManager(requireContext())).create(ApiService.class);
        etGoalName = view.findViewById(R.id.etGoalName);
        etAmount = view.findViewById(R.id.etAmount);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> submit());
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    private void submit() {
        String title = etGoalName.getText() == null ? "" : etGoalName.getText().toString().trim();
        long target = FormatUtils.parseRupiah(etAmount.getText() == null ? "" : etAmount.getText().toString());

        if (title.isEmpty()) {
            etGoalName.setError("Nama target wajib diisi");
            return;
        }
        if (target <= 0) {
            etAmount.setError("Nominal harus lebih dari 0");
            return;
        }

        btnSubmit.setEnabled(false);
        apiService.createGoal(new GoalRequest(title, target)).enqueue(new Callback<ApiResponse<Goal>>() {
            @Override
            public void onResponse(Call<ApiResponse<Goal>> call, Response<ApiResponse<Goal>> response) {
                btnSubmit.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Target berhasil dibuat", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onGoalCreated();
                    }
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), ApiUtils.errorMessage(response, "Gagal membuat target"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Goal>> call, Throwable t) {
                btnSubmit.setEnabled(true);
                Toast.makeText(requireContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
