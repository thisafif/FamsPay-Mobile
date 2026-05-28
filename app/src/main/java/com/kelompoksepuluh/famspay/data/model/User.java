package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    // Diubah ke String karena backend menggunakan MongoDB ID (hex string)
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
    public double walletBalance; // Gunakan double untuk mengantisipasi nilai desimal dari API
}