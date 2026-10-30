package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class TransactionRequest {
    public String type;
    public long amount;

    @SerializedName("txn_date")
    public String txnDate;

    @SerializedName("category_name")
    public String categoryName;

    public String note;
    public boolean confirm;

    public TransactionRequest(String type, long amount, String txnDate, String categoryName, String note, boolean confirm) {
        this.type = type;
        this.amount = amount;
        this.txnDate = txnDate;
        this.categoryName = categoryName;
        this.note = note;
        this.confirm = confirm;
    }
}
