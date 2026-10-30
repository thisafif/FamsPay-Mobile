package com.kelompoksepuluh.famspay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class FamilyMemberListAdapter extends RecyclerView.Adapter<FamilyMemberListAdapter.MemberViewHolder> {
    public interface OnMemberActionListener {
        void onDetail(User user);
        void onRoleChange(User user, String nextRole, Button actionButton);
        void onRemove(User user);
    }

    private final List<User> members = new ArrayList<>();
    private final String currentUserId;
    private final OnMemberActionListener listener;

    public FamilyMemberListAdapter(String currentUserId, OnMemberActionListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void submitList(List<User> items) {
        members.clear();
        if (items != null) {
            members.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MemberViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_family_member, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView roleBadge;
        private final TextView email;
        private final TextView limits;
        private final Button detail;
        private final Button role;
        private final Button remove;
        private final TextView selfNote;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvMemberName);
            roleBadge = itemView.findViewById(R.id.tvMemberRoleBadge);
            email = itemView.findViewById(R.id.tvMemberEmail);
            limits = itemView.findViewById(R.id.tvMemberLimits);
            detail = itemView.findViewById(R.id.btnMemberDetail);
            role = itemView.findViewById(R.id.btnMemberRole);
            remove = itemView.findViewById(R.id.btnMemberRemove);
            selfNote = itemView.findViewById(R.id.tvMemberSelfNote);
        }

        void bind(User user) {
            String userRole = normalizedRole(user.role);
            name.setText(user.fullName == null ? "Member" : user.fullName);
            roleBadge.setText(userRole);
            email.setText(user.email == null ? "-" : user.email);
            limits.setText("Sisa limit: " + FormatUtils.rupiah(user.remainingLimit)
                    + "\nSaldo: " + FormatUtils.rupiah(user.walletBalance));
            detail.setOnClickListener(v -> listener.onDetail(user));

            boolean isSelf = user.id != null && user.id.equals(currentUserId);
            if (isSelf) {
                role.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
                selfNote.setVisibility(View.VISIBLE);
                return;
            }

            String nextRole = "admin".equalsIgnoreCase(user.role) ? "member" : "admin";
            role.setVisibility(View.VISIBLE);
            remove.setVisibility(View.VISIBLE);
            selfNote.setVisibility(View.GONE);
            role.setText("Jadikan " + nextRole);
            role.setEnabled(true);
            remove.setEnabled(true);
            role.setOnClickListener(v -> listener.onRoleChange(user, nextRole, role));
            remove.setOnClickListener(v -> listener.onRemove(user));
        }

        private String normalizedRole(String role) {
            return role == null || role.trim().isEmpty() ? "member" : role;
        }
    }
}
