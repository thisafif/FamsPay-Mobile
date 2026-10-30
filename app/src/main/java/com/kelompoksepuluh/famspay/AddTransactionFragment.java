package com.kelompoksepuluh.famspay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    public static AddTransactionFragment newInstance() {
        return new AddTransactionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        view.findViewById(R.id.btnSubmit).setOnClickListener(v -> dismiss());
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }
}