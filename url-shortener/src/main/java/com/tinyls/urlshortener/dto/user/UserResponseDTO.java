package com.tinyls.urlshortener.dto.user;

import com.tinyls.urlshortener.model.AuthProvider;
import com.tinyls.urlshortener.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Data Transfer Object for user responses.
 * Contains the user's information that is safe to expose to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    /**
     * The unique identifier of the user.
     */
    private UUID id;

    /**
     * The user's email address.
     */
    private String email;

    /**
     * The user's full name.
     */
    private String name;

    /**
     * The authentication provider used by the user.
     * e.g., LOCAL, GOOGLE, GITHUB
     */
    private AuthProvider provider;

    /**
     * The unique identifier from the authentication provider.
     * Only set for OAuth2 users.
     */
    private String providerId;

    /**
     * The URL of the user's profile picture.
     * Set automatically for OAuth2 users from their provider.
     * Can be updated for local users.
     */
    private String avatarUrl;

    /**
     * The user's role in the system.
     * e.g., USER, ADMIN
     */
    private Role role;

    /**
     * The timestamp when the user account was created.
     */
    private Timestamp createdAt;

    /**
     * Indicates whether the user can change their password.
     * Only true for users with LOCAL authentication provider.
     */
    private boolean canChangePassword;
}