package com.kelompoksepuluh.famspay.data.remote;

import com.kelompoksepuluh.famspay.data.model.ApiResponse;
import com.kelompoksepuluh.famspay.data.model.CategoryData;
import com.kelompoksepuluh.famspay.data.model.DashboardData;
import com.kelompoksepuluh.famspay.data.model.Family;
import com.kelompoksepuluh.famspay.data.model.FamilyRequest;
import com.kelompoksepuluh.famspay.data.model.Goal;
import com.kelompoksepuluh.famspay.data.model.GoalRequest;
import com.kelompoksepuluh.famspay.data.model.LoginData;
import com.kelompoksepuluh.famspay.data.model.LoginRequest;
import com.kelompoksepuluh.famspay.data.model.MonthlyLimit;
import com.kelompoksepuluh.famspay.data.model.MonthlyLimitRequest;
import com.kelompoksepuluh.famspay.data.model.SocialLoginRequest;
import com.kelompoksepuluh.famspay.data.model.Transaction;
import com.kelompoksepuluh.famspay.data.model.TransactionRequest;
import com.kelompoksepuluh.famspay.data.model.User;
import com.kelompoksepuluh.famspay.data.model.RegisterRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiService {

    @POST("login")
    Call<ApiResponse<LoginData>> login(@Body LoginRequest request);

    @GET("me")
    Call<ApiResponse<User>> getMe();

    @POST("logout")
    Call<ApiResponse<Object>> logout();

    @POST("register")
    Call<ApiResponse<User>> register(@Body RegisterRequest request);

    @POST("login/google")
    Call<ApiResponse<LoginData>> loginGoogle(@Body SocialLoginRequest request);

    @PATCH("me")
    Call<ApiResponse<User>> updateProfile(@Body Map<String, Object> request);

    @GET("categories")
    Call<ApiResponse<CategoryData>> getCategories();

    @GET("dashboard/personal")
    Call<ApiResponse<DashboardData>> getPersonalDashboard();

    @GET("dashboard/family")
    Call<ApiResponse<DashboardData>> getFamilyDashboard();

    @POST("families")
    Call<ApiResponse<Family>> createFamily(@Body FamilyRequest request);

    @POST("families/join")
    Call<ApiResponse<Family>> joinFamily(@Body FamilyRequest request);

    @GET("families/me")
    Call<ApiResponse<Family>> getMyFamily();

    @GET("families/members")
    Call<ApiResponse<List<User>>> getFamilyMembers();

    @PUT("families/{id}")
    Call<ApiResponse<Family>> updateFamily(@Path("id") String id, @Body FamilyRequest request);

    @PUT("families/members/{userId}/role")
    Call<ApiResponse<User>> updateMemberRole(@Path("userId") String userId, @Body Map<String, String> request);

    @DELETE("families/members/{userId}")
    Call<ApiResponse<Object>> removeMember(@Path("userId") String userId);

    @PUT("admin/members/{userId}/monthly-limit")
    Call<ApiResponse<MonthlyLimit>> setMonthlyLimit(@Path("userId") String userId, @Body MonthlyLimitRequest request);

    @GET("transactions")
    Call<ApiResponse<List<Transaction>>> getTransactions(@QueryMap Map<String, String> filters);

    @POST("transactions/preview")
    Call<ApiResponse<Object>> previewTransaction(@Body TransactionRequest request);

    @POST("transactions")
    Call<ApiResponse<Object>> createTransaction(@Body TransactionRequest request);

    @GET("transactions/{id}")
    Call<ApiResponse<Transaction>> getTransaction(@Path("id") String id);

    @PUT("transactions/{id}/preview")
    Call<ApiResponse<Object>> previewEditTransaction(@Path("id") String id, @Body TransactionRequest request);

    @PUT("transactions/{id}")
    Call<ApiResponse<Object>> updateTransaction(@Path("id") String id, @Body TransactionRequest request);

    @DELETE("transactions/{id}")
    Call<ApiResponse<Object>> deleteTransaction(@Path("id") String id);

    @GET("goals")
    Call<ApiResponse<List<Goal>>> getGoals(@Query("include_archived") boolean includeArchived);

    @POST("goals")
    Call<ApiResponse<Goal>> createGoal(@Body GoalRequest request);

    @GET("goals/{id}")
    Call<ApiResponse<Goal>> getGoal(@Path("id") String id);

    @PUT("goals/{id}")
    Call<ApiResponse<Goal>> updateGoal(@Path("id") String id, @Body GoalRequest request);

    @PUT("goals/{id}/archive")
    Call<ApiResponse<Goal>> archiveGoal(@Path("id") String id);

    @POST("goals/{id}/allocate/preview")
    Call<ApiResponse<Object>> previewAllocateGoal(@Path("id") String id, @Body GoalRequest request);

    @POST("goals/{id}/allocate")
    Call<ApiResponse<Object>> allocateGoal(@Path("id") String id, @Body GoalRequest request);

    @POST("goals/{id}/withdraw")
    Call<ApiResponse<Object>> withdrawGoal(@Path("id") String id, @Body GoalRequest request);
}
