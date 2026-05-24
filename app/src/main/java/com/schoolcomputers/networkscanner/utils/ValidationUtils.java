package com.networkscanner.utils;

import android.util.Patterns;

/**
 * Static helpers for validating user-supplied auth form input.
 */
public class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        String trimmed = username.trim();
        // 3–20 alphanumeric + underscore characters
        return trimmed.length() >= 3 && trimmed.length() <= 20
                && trimmed.matches("[a-zA-Z0-9_]+");
    }

    public static boolean isValidPassword(String password) {
        // At least 6 characters (Firebase minimum)
        return password != null && password.length() >= 6;
    }

    public static boolean passwordsMatch(String password, String rePassword) {
        return password != null && password.equals(rePassword);
    }

    /** Returns an error message or null if the field is valid. */
    public static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) return "Username is required";
        if (!isValidUsername(username)) return "3–20 alphanumeric characters or underscores";
        return null;
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "Email is required";
        if (!isValidEmail(email)) return "Enter a valid email address";
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) return "Password is required";
        if (!isValidPassword(password)) return "Password must be at least 6 characters";
        return null;
    }

    public static String validateRePassword(String password, String rePassword) {
        if (rePassword == null || rePassword.isEmpty()) return "Please confirm your password";
        if (!passwordsMatch(password, rePassword)) return "Passwords do not match";
        return null;
    }
}
