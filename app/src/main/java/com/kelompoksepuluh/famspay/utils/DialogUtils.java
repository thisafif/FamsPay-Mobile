package com.kelompoksepuluh.famspay.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kelompoksepuluh.famspay.R;

public final class DialogUtils {
    private DialogUtils() {}

    public static AlertDialog showConfirm(
            Context context,
            String title,
            String message,
            String primaryText,
            String secondaryText,
            Runnable onPrimary
    ) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_design_confirm, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .create();

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        Button btnPrimary = view.findViewById(R.id.btnDialogPrimary);
        Button btnSecondary = view.findViewById(R.id.btnDialogSecondary);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnPrimary.setText(primaryText);
        btnSecondary.setText(secondaryText);

        btnPrimary.setOnClickListener(v -> {
            dialog.dismiss();
            onPrimary.run();
        });
        btnSecondary.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });
        dialog.show();
        return dialog;
    }
}
