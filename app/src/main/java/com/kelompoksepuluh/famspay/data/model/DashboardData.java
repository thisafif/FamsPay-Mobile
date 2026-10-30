package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardData {
    @SerializedName("wallet_balance")
    public long walletBalance;

    @SerializedName("total_family_wallet_balance")
    public long totalFamilyWalletBalance;

    @SerializedName("current_month")
    public CurrentMonth currentMonth;

    @SerializedName("total_savings")
    public long totalSavings;

    @SerializedName("total_family_savings")
    public long totalFamilySavings;

    @SerializedName("flow_analysis")
    public FlowAnalysis flowAnalysis;

    @SerializedName("budget_analysis")
    public BudgetAnalysis budgetAnalysis;

    @SerializedName("recent_transactions")
    public List<Transaction> recentTransactions;

    @SerializedName("members_summary")
    public List<MemberSummary> membersSummary;

    public static class CurrentMonth {
        public long income;
        public long expense;
        public String period;
    }

    public static class FlowAnalysis {
        public long income;
        public long expense;

        @SerializedName("net_flow")
        public long netFlow;

        public String period;
    }

    public static class BudgetAnalysis {
        @SerializedName("monthly_limit_base")
        public long monthlyLimitBase;

        @SerializedName("remaining_limit")
        public long remainingLimit;

        @SerializedName("total_expense_in_period")
        public long totalExpenseInPeriod;

        @SerializedName("usage_percent")
        public double usagePercent;

        @SerializedName("total_monthly_limit_base")
        public long totalMonthlyLimitBase;

        @SerializedName("total_remaining_limit")
        public long totalRemainingLimit;

        @SerializedName("members_with_limit")
        public int membersWithLimit;

        public String period;
    }

    public static class MemberSummary {
        @SerializedName("user_id")
        public String userId;

        @SerializedName("full_name")
        public String fullName;

        public String role;

        @SerializedName("wallet_balance")
        public long walletBalance;

        @SerializedName("active_goals_count")
        public int activeGoalsCount;
    }
}
