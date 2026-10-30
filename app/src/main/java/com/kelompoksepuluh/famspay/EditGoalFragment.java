package com.kelompoksepuluh.famspay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

public class EditGoalFragment extends BottomSheetDialogFragment {
    private static final String ARG_GOAL_ID = "goal_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_TARGET_AMOUNT = "target_amount";
    private String goalId;
    private EditText etGoalName, etAmount;
    private Button btnUpdate;
    private ApiService apiService;

    public interface OnGoalChangedListener {
        void onGoalChanged();
    }

    public static EditGoalFragment newInstance() {
        return new EditGoalFragment();
    }

    public static EditGoalFragment newInstance(Goal goal) {
        EditGoalFragment fragment = new EditGoalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GOAL_ID, goal.id);
        args.putString(ARG_TITLE, goal.title);
        args.putLong(ARG_TARGET_AMOUNT, goal.targetAmount);
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
        return inflater.inflate(R.layout.fragment_edit_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getClient(new SessionManager(requireContext())).create(ApiService.class);
        etGoalName = view.findViewById(R.id.etGoalName);
        etAmount = view.findViewById(R.id.etAmount);
        btnUpdate = view.findViewById(R.id.btnUpdate);

        bindArguments();
        btnUpdate.setOnClickListener(v -> updateGoal());
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    private void bindArguments() {
        Bundle args = getArguments();
        if (args == null) return;

        goalId = args.getString(ARG_GOAL_ID);
        etGoalName.setText(args.getString(ARG_TITLE, ""));
        long targetAmount = args.getLong(ARG_TARGET_AMOUNT, 0);
        if (targetAmount > 0) {
            etAmount.setText(String.valueOf(targetAmount));
        }
    }

    private void updateGoal() {
        String title = etGoalName.getText() == null ? "" : etGoalName.getText().toString().trim();
        long targetAmount = FormatUtils.parseRupiah(etAmount.getText() == null ? "" : etAmount.getText().toString());

        if (goalId == null || goalId.isEmpty()) {
            Toast.makeText(requireContext(), "Target tidak valid", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        if (title.isEmpty()) {
            etGoalName.setError("Nama target wajib diisi");
            return;
        }
        if (targetAmount <= 0) {
            etAmount.setError("Nominal target harus lebih dari 0");
            return;
        }

        btnUpdate.setEnabled(false);
        btnUpdate.setText("Menyimpan...");
        apiService.updateGoal(goalId, new GoalRequest(title, targetAmount)).enqueue(new Callback<ApiResponse<Goal>>() {
            @Override
            public void onResponse(Call<ApiResponse<Goal>> call, Response<ApiResponse<Goal>> response) {
                btnUpdate.setEnabled(true);
                btnUpdate.setText(R.string.edit_trans_submit);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Target berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof OnGoalChangedListener) {
                        ((OnGoalChangedListener) getActivity()).onGoalChanged();
                    }
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), ApiUtils.errorMessage(response, "Gagal memperbarui target"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Goal>> call, Throwable t) {
                btnUpdate.setEnabled(true);
                btnUpdate.setText(R.string.edit_trans_submit);
                Toast.makeText(requireContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
