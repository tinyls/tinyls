package com.tinyls.urlshortener.security;

import com.tinyls.urlshortener.model.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Adapter class that implements Spring Security's UserDetails interface.
 * This class wraps our application's User entity to provide the necessary
 * security-related information to Spring Security.
 * 
 * The adapter provides:
 * - User authentication details (email, password)
 * - User authorization details (role-based authorities)
 * - Account status information
 * - User identification (userId)
 */
@Getter
public class UserDetailsAdapter implements UserDetails {
    private final String email;
    private final String password;
    private final Role role;
    private final boolean enabled;
    private final UUID userId;

    /**
     * Creates a new UserDetailsAdapter instance.
     * 
     * @param email    the user's email address
     * @param password the user's encoded password
     * @param role     the user's role in the system
     * @param enabled  whether the user account is enabled
     * @param userId   the user's unique identifier
     */
    public UserDetailsAdapter(String email, String password, Role role, boolean enabled, UUID userId) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.userId = userId;
    }

    /**
     * Returns the authorities granted to the user.
     * Currently, this is based on the user's role.
     * 
     * @return a collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns the user's encoded password.
     * 
     * @return the encoded password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     * In this implementation, the email address is used as the username.
     * 
     * @return the email address
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account has expired.
     * Currently, accounts do not expire.
     * 
     * @return true if the account is valid, false if expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * Currently, accounts cannot be locked.
     * 
     * @return true if the account is not locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) have expired.
     * Currently, credentials do not expire.
     * 
     * @return true if the credentials are valid, false if expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * 
     * @return true if the user is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}