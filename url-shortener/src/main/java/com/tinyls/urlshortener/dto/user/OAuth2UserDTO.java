package com.tinyls.urlshortener.dto.user;

import com.tinyls.urlshortener.model.AuthProvider;
import com.tinyls.urlshortener.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for OAuth2 user information.
 * Contains the user details received from OAuth2 providers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserDTO {
    /**
     * The user's email address from the OAuth2 provider.
     */
    private String email;

    /**
     * The user's name from the OAuth2 provider.
     */
    private String name;

    /**
     * The OAuth2 provider used for authentication.
     * e.g., GOOGLE, GITHUB
     */
    private AuthProvider provider;

    /**
     * The unique identifier from the OAuth2 provider.
     */
    private String providerId;

    /**
     * The user's role in the system.
     * Defaults to USER for OAuth2 users.
     */
    private Role role;
}