package com.kelompoksepuluh.famspay.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "FAMSPAY_SESSION";
    private static final String KEY_TOKEN = "TOKEN";

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

    public boolean isLoggedIn(){
        return getToken() != null;
    }

    public void clearSession(){
        preferences.edit().clear().apply();
    }
}
