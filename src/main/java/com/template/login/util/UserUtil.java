package com.template.login.util;

import java.util.UUID;

public class UserUtil {

    public static String generateUserId() {
        return "USER-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    public static String generateSessionId() {
        return UUID.randomUUID().toString().substring(0, 8);  // Gets the first 8 characters of the UUID
    }
}
