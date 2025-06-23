package com.tinyls.urlshortener.model;

/**
 * Enum representing the authentication providers supported by the application.
 * Used to track how users authenticate with the system.
 */
public enum AuthProvider {
    /**
     * Local authentication using email and password.
     */
    LOCAL,

    /**
     * Google OAuth2 authentication.
     */
    GOOGLE,

    /**
     * GitHub OAuth2 authentication.
     */
    GITHUB
}