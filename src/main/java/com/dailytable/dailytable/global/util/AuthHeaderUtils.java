package com.dailytable.dailytable.global.util;

public final class AuthHeaderUtils {

    private AuthHeaderUtils() {
    }

    public static String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7).trim();
    }
}