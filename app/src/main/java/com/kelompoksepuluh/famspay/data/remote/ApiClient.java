package com.kelompoksepuluh.famspay.data.remote;

import com.kelompoksepuluh.famspay.utils.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/api/v1/";

    public static Retrofit getClient(SessionManager sessionManager) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();

                    Request.Builder requestBuilder = originalRequest.newBuilder()
                            .header("Accept", "application/json");

                    if (sessionManager != null && sessionManager.getToken() != null) {
                        requestBuilder.header("Authorization", "Bearer " + sessionManager.getToken());
                    }

                    return chain.proceed(requestBuilder.build());
                })
                .addInterceptor(loggingInterceptor)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}