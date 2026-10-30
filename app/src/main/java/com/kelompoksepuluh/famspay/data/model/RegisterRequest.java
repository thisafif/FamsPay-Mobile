package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("full_name")
    private String fullName;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("device_name")
    private String deviceName;

    public RegisterRequest(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.deviceName = "android";
    }
}