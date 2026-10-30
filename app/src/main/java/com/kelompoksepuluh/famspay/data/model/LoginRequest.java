package com.kelompoksepuluh.famspay.data.model;

public class LoginRequest {
    private String email;
    private String password;
    private String device_name;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.device_name = "android";
    }
}