package com.tinyls.urlshortener.config;

/**
 * Constants for cache keys and configurations.
 * Provides consistent naming conventions for Redis cache keys.
 * Uses single source of truth approach: URLs cached by ID only.
 */
public final class CacheConstants {

    private CacheConstants() {
        // Utility class - prevent instantiation
    }

    // Cache key prefixes
    public static final String URL_PREFIX = "url:";
    public static final String USER_PREFIX = "user:";
    public static final String CLICKS_PREFIX = "clicks:";
    public static final String SHORT_CODE_MAPPING_PREFIX = "shortcode_mapping:";

    // Cache names for Spring Cache annotations
    public static final String URL_CACHE = "urls";
    public static final String USER_CACHE = "users";
    public static final String CLICKS_CACHE = "clicks";
    public static final String SHORT_CODE_MAPPING_CACHE = "shortcode_mappings";

    // TTL values in seconds
    public static final long URL_TTL = 3600; // 1 hour
    public static final long USER_TTL = 1800; // 30 minutes
    public static final long CLICKS_TTL = 86400; // 24 hours
    public static final long SHORT_CODE_MAPPING_TTL = 7200; // 2 hours

    /**
     * Generates a cache key for URL by ID (single source of truth).
     *
     * @param id the URL ID
     * @return the cache key
     */
    public static String urlKey(Long id) {
        return URL_PREFIX + "id:" + id;
    }

    /**
     * Generates a cache key for short code to ID mapping.
     * This maps short code to URL ID for quick lookups.
     *
     * @param shortCode the URL short code
     * @return the cache key
     */
    public static String shortCodeToIdKey(String shortCode) {
        return SHORT_CODE_MAPPING_PREFIX + shortCode;
    }

    /**
     * Generates a cache key for user URLs.
     *
     * @param userId the user ID
     * @return the cache key
     */
    public static String userUrlsKey(java.util.UUID userId) {
        return URL_PREFIX + "user:" + userId;
    }

    /**
     * Generates a cache key for user by ID.
     *
     * @param id the user ID
     * @return the cache key
     */
    public static String userKey(java.util.UUID id) {
        return USER_PREFIX + "id:" + id;
    }

    /**
     * Generates a cache key for user by email.
     *
     * @param email the user email
     * @return the cache key
     */
    public static String userByEmailKey(String email) {
        return USER_PREFIX + "email:" + email;
    }

    /**
     * Generates a cache key for URL clicks.
     *
     * @param shortCode the URL short code
     * @return the cache key
     */
    public static String clicksKey(String shortCode) {
        return CLICKS_PREFIX + shortCode;
    }
}