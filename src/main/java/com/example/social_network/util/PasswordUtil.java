package com.example.social_network.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// all the passwords will be pass + "name" (e.g. passAlice, passBob, etc.)

public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}
