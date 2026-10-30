package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class Goal {
    public String id;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("family_id")
    public String familyId;

    public String title;

    @SerializedName("target_amount")
    public long targetAmount;

    @SerializedName("current_amount")
    public long currentAmount;

    public String status;
}
