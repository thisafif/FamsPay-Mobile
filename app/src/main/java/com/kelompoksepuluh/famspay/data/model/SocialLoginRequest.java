package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.annotations.SerializedName;

public class SocialLoginRequest {
    @SerializedName("id_token")
    private String idToken;
    
    @SerializedName("device_name")
    private String deviceName;

    public SocialLoginRequest(String idToken) {
        this.idToken = idToken;
        this.deviceName = "android";
    }
}