package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class MonthlyLimitRequest {
    @SerializedName("monthly_limit_base")
    public long monthlyLimitBase;

    @SerializedName("period_month")
    public String periodMonth;

    public MonthlyLimitRequest(long monthlyLimitBase, String periodMonth) {
        this.monthlyLimitBase = monthlyLimitBase;
        this.periodMonth = periodMonth;
    }
}
