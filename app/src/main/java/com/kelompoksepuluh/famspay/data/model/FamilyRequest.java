package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class FamilyRequest {
    @SerializedName("family_name")
    public String familyName;

    @SerializedName("join_code")
    public String joinCode;

    public FamilyRequest(String familyName, String joinCode) {
        this.familyName = familyName;
        this.joinCode = joinCode;
    }
}
