package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    public String id;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("family_id")
    public String familyId;

    public String type;
    public long amount;

    @SerializedName("txn_date")
    public String txnDate;

    public String title;

    @SerializedName("category_name")
    public String categoryName;

    public String note;

    @SerializedName("goal_id")
    public String goalId;

    @SerializedName("is_system")
    public boolean isSystem;

    @SerializedName("is_deleted")
    public boolean isDeleted;

    @SerializedName("read_only")
    public boolean readOnly;
}
