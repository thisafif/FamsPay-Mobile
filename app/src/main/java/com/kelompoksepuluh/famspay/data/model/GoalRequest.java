package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class GoalRequest {
    public String title;

    @SerializedName("target_amount")
    public long targetAmount;

    public long amount;
    public boolean confirm;

    public GoalRequest(String title, long targetAmount) {
        this.title = title;
        this.targetAmount = targetAmount;
    }

    public GoalRequest(long amount, boolean confirm) {
        this.amount = amount;
        this.confirm = confirm;
    }
}
