package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class Family {
    public String id;

    @SerializedName("family_name")
    public String familyName;

    @SerializedName("join_code")
    public String joinCode;

    @SerializedName("created_by")
    public String createdBy;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;
}
