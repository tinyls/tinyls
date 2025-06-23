package com.tinyls.urlshortener.security.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.security.UserDetailsAdapter;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Custom implementation of OAuth2User for handling OAuth2 user information.
 * This class wraps the OAuth2 user attributes and provides additional methods
 * for accessing user-specific information.
 * 
 * The class provides:
 * - Access to OAuth2 user attributes
 * - Role-based authorities (defaults to ROLE_USER)
 * - User identification (email, name, user ID)
 */
public class CustomOAuth2User implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;
    private final UserDetailsAdapter userDetails;

    /**
     * Creates a new CustomOAuth2User instance.
     * Initializes the user with OAuth2 attributes and default USER role.
     * 
     * @param user       the application user
     * @param attributes the OAuth2 user attributes
     */
    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
        this.userDetails = new UserDetailsAdapter(user.getEmail(), user.getPassword(), user.getRole(), true,
                user.getId());
    }

    /**
     * Returns the OAuth2 user attributes.
     * 
     * @return map of user attributes
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Returns the authorities granted to the user.
     * Currently, this is a singleton list containing ROLE_USER.
     * 
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDetails.getAuthorities();
    }

    /**
     * Returns the user's name from OAuth2 attributes.
     * 
     * @return the user's name
     */
    @Override
    public String getName() {
        return user.getName();
    }

    /**
     * Returns the user's email address from OAuth2 attributes.
     * 
     * @return the user's email address
     */
    public String getEmail() {
        return (String) attributes.get("email");
    }

    /**
     * Returns the user's unique identifier from OAuth2 attributes.
     * 
     * @return the user's UUID
     */
    public UUID getUserId() {
        return user.getId();
    }

    /**
     * Returns the user's profile picture URL from OAuth2 attributes.
     * 
     * @return the user's profile picture URL
     */
    public String getPicture() {
        return user.getAvatarUrl();
    }
}