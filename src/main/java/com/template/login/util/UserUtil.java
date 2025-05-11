package com.template.login.util;

import java.util.UUID;

public class UserUtil {

    public static String generateUserId() {
        return "USER-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
