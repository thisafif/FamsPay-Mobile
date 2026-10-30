package com.kelompoksepuluh.famspay;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kelompoksepuluh.famspay.data.model.Transaction;
import com.kelompoksepuluh.famspay.utils.FormatUtils;
import com.kelompoksepuluh.famspay.utils.TransactionTextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder> {
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, String> memberNames = new HashMap<>();
    private final boolean showMemberName;
    private final OnTransactionClickListener listener;

    public TransactionListAdapter(boolean showMemberName, OnTransactionClickListener listener) {
        this.showMemberName = showMemberName;
        this.listener = listener;
    }

    public void submitList(List<Transaction> items) {
        transactions.clear();
        if (items != null) {
            transactions.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setMemberNames(Map<String, String> names) {
        memberNames.clear();
        if (names != null) {
            memberNames.putAll(names);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(transactions.get(position));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView icon;
        private final TextView title;
        private final TextView subtitle;
        private final TextView amount;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.tvTransactionIcon);
            title = itemView.findViewById(R.id.tvTransactionTitle);
            subtitle = itemView.findViewById(R.id.tvTransactionSubtitle);
            amount = itemView.findViewById(R.id.tvTransactionAmount);
        }

        void bind(Transaction transaction) {
            String type = transaction.type == null ? "expense" : transaction.type;
            boolean expense = "expense".equalsIgnoreCase(type);
            long signedAmount = expense ? -transaction.amount : transaction.amount;
            String displayTitle = TransactionTextUtils.displayTitle(transaction);
            String displayCategory = transaction.categoryName == null ? "-" : transaction.categoryName;
            String displayDate = FormatUtils.displayDate(transaction.txnDate);

            title.setText(displayTitle == null || displayTitle.trim().isEmpty() ? "Transaksi" : displayTitle);
            subtitle.setText(buildSubtitle(transaction, displayCategory, displayDate));
            amount.setText((signedAmount < 0 ? "-" : "+") + FormatUtils.rupiah(Math.abs(signedAmount)));
            amount.setTextColor(ContextCompat.getColor(itemView.getContext(), expense ? R.color.trans_red : R.color.green));
            icon.setText(expense ? "OUT" : "IN");
            icon.setTextColor(ContextCompat.getColor(itemView.getContext(), expense ? R.color.trans_red : R.color.primary_dark));
            icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                    itemView.getContext(),
                    expense ? R.color.chart_blue : R.color.chart_cyan
            )));
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionClick(transaction);
                }
            });
        }

        private String buildSubtitle(Transaction transaction, String category, String date) {
            if (!showMemberName) {
                return category + " - " + date;
            }
            String memberName = memberNames.get(transaction.userId);
            if (memberName == null || memberName.trim().isEmpty()) {
                memberName = "Anggota";
            }
            return memberName + " - " + category + " - " + date;
        }
    }
}
