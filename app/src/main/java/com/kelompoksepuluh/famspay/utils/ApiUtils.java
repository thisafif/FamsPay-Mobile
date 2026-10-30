package com.kelompoksepuluh.famspay.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import retrofit2.Response;

public final class ApiUtils {
    private ApiUtils() {}

    public static String errorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() == null) return fallback;
            JsonObject object = JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
            if (object.has("message") && !object.get("message").isJsonNull()) {
                return object.get("message").getAsString();
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    public static String warningMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() == null) return fallback;
            JsonObject object = JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
            if (object.has("errors")) {
                JsonObject errors = object.getAsJsonObject("errors");
                if (errors != null && errors.has("warnings")) {
                    JsonArray warnings = errors.getAsJsonArray("warnings");
                    if (warnings != null && warnings.size() > 0) {
                        return warnings.get(0).getAsString();
                    }
                }
            }
            if (object.has("message")) return object.get("message").getAsString();
        } catch (Exception ignored) {
        }
        return fallback;
    }
}
