package com.kelompoksepuluh.famspay.data.model;

import com.google.gson.JsonObject;

public class ApiResponse<T> {
    public String status;
    public String message;
    public T data;
    public JsonObject errors;
    public JsonObject meta;
}
