package com.kelompoksepuluh.famspay.data.remote;

import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.LoginData;
import com.kelompoksepuluh.famspay.data.model.LoginRequest;
import com.kelompoksepuluh.famspay.data.model.SocialLoginRequest;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.data.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("login")
    Call<ApiResponse<LoginData>> login(@Body LoginRequest request);

    @GET("me")
    Call<ApiResponse<User>> getMe();

    @POST("logout")
    Call<ApiResponse<Object>> logout();

    @POST("register")
    Call<ApiResponse<LoginData>> register(@Body RegisterRequest request);

    @POST("login/google")
    Call<ApiResponse<LoginData>> loginGoogle(@Body SocialLoginRequest request);
}