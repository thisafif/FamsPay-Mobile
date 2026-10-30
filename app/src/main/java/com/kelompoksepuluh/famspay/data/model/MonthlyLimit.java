package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class MonthlyLimit {
    public String id;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("family_id")
    public String familyId;

    @SerializedName("period_month")
    public String periodMonth;

    @SerializedName("monthly_limit_base")
    public long monthlyLimitBase;

    @SerializedName("remaining_limit")
    public long remainingLimit;
}
