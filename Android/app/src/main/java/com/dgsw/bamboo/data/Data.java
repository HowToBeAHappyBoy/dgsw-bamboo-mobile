package com.dgsw.bamboo.data;

import android.util.Base64;

public class Data {
    public static String adminName;
    private static byte[] token;

    public static boolean isEmpty() {
        return adminName == null && token == null;
    }

    public static void clear() {
        adminName = null;
        token = null;
    }

    public static void setToken(String token) {
        if (token == null) return;
        Data.token = Base64.encode(token.getBytes(), Base64.DEFAULT);
    }

    public static void setToken(byte[] token) {
        if (token == null) return;
        Data.token = token;
    }

    public static byte[] getToken() {
        return token;
    }

    public static String getTokenToString() {
        return new String(Base64.decode(token, Base64.DEFAULT));
    }
}
