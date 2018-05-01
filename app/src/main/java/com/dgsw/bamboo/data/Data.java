package com.dgsw.bamboo.data;

public class Data {
    public static String adminName;
    public static String token;

    public static boolean isEmpty() {
        return adminName == null && token == null;
    }

    public static void clear() {
        adminName = null;
        token = null;
    }
}
