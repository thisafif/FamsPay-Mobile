package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    public String id;
    
    @SerializedName("full_name")
    public String fullName;
    
    public String email;
    public String role;
    
    @SerializedName("avatar_url")
    public String avatarUrl;
    
    @SerializedName("family_id")
    public String familyId;
    
    @SerializedName("wallet_balance")
    public long walletBalance;

    @SerializedName("monthly_limit_base")
    public long monthlyLimitBase;

    @SerializedName("remaining_limit")
    public long remainingLimit;

    @SerializedName("current_month_expense")
    public long currentMonthExpense;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;
}
