package com.tinyls.urlshortener.model;

/**
 * Enum representing user roles in the application.
 * Used for role-based access control and authorization.
 */
public enum Role {
    /**
     * Regular user role with basic access rights.
     */
    USER,

    /**
     * Administrator role with elevated privileges.
     */
    ADMIN
}