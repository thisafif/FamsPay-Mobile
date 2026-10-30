package com.kelompoksepuluh.famspay.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "FAMSPAY_SESSION";
    private static final String KEY_TOKEN = "TOKEN";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_FULL_NAME = "FULL_NAME";
    private static final String KEY_ROLE = "ROLE";
    private static final String KEY_FAMILY_ID = "FAMILY_ID";

    private final SharedPreferences preferences;

    public SessionManager(Context context){
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token){
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken(){
        return preferences.getString(KEY_TOKEN, null);
    }

    public void saveUser(String userId, String fullName, String role, String familyId) {
        preferences.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_ROLE, role)
                .putString(KEY_FAMILY_ID, familyId)
                .apply();
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public String getFullName() {
        return preferences.getString(KEY_FULL_NAME, null);
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, "member");
    }

    public String getFamilyId() {
        return preferences.getString(KEY_FAMILY_ID, null);
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getRole());
    }

    public boolean isLoggedIn(){
        return getToken() != null;
    }

    public void clearSession(){
        preferences.edit().clear().apply();
    }
}
