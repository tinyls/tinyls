package com.tinyls.urlshortener.util;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility for validating custom short codes.
 */
public final class ShortCodeValidator {
    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9_-]{3,8}$");

    private static final Set<String> RESERVED = Set.of(
            // common app routes / words
            "api", "login", "logout", "register", "signup", "signin", "health", "status",
            // back-end specific URL controller paths
            "r", "id");

    private ShortCodeValidator() {
    }

    public static void validateOrThrow(String shortCode) {
        if (shortCode == null || shortCode.isBlank()) {
            throw new IllegalArgumentException("shortCode must not be blank");
        }
        if (!FORMAT.matcher(shortCode).matches()) {
            throw new IllegalArgumentException(
                    "Short code must be 3-8 chars and contain only letters, numbers, underscores, or hyphens");
        }
        String normalized = shortCode.toLowerCase();
        if (RESERVED.contains(normalized)) {
            throw new IllegalArgumentException("Short code is reserved: " + shortCode);
        }
    }
}