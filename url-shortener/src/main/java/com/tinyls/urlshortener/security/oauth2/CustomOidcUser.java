package com.tinyls.urlshortener.security.oauth2;

import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.security.UserDetailsAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.UUID;

/**
 * Custom implementation of OidcUser for handling OpenID Connect user
 * information.
 * This class wraps the OIDC user attributes and provides additional methods
 * for accessing user-specific information.
 * 
 * The class provides:
 * - Access to OIDC user attributes
 * - Role-based authorities (defaults to ROLE_USER)
 * - User identification (email, name, user ID)
 */
public class CustomOidcUser extends DefaultOidcUser {
    private final User user;
    private final UserDetailsAdapter userDetails;

    public CustomOidcUser(User user, OidcUser delegate) {
        super(delegate.getAuthorities(), delegate.getIdToken(), delegate.getUserInfo(), "email");
        this.user = user;
        this.userDetails = new UserDetailsAdapter(user.getEmail(), user.getPassword(), user.getRole(), true,
                user.getId());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDetails.getAuthorities();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    /**
     * Returns the user's email address from OIDC attributes.
     * 
     * @return the user's email address
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Returns the user's unique identifier.
     * 
     * @return the user's UUID
     */
    public UUID getUserId() {
        return user.getId();
    }

    /**
     * Returns the user's profile picture URL.
     * 
     * @return the user's profile picture URL
     */
    public String getPicture() {
        return user.getAvatarUrl();
    }

    /**
     * Returns the wrapped User entity.
     * 
     * @return the User entity
     */
    public User getUser() {
        return user;
    }
}