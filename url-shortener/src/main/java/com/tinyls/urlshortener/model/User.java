package com.tinyls.urlshortener.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Entity class representing a user in the system.
 * Maps to the 'users' table in the database.
 * Supports both local and OAuth2 authentication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    /**
     * Unique identifier for the user.
     * Generated as a UUID for better security and distribution.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User's email address.
     * Must be unique and is used for authentication.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * User's password.
     * Stored as a hashed value for local authentication.
     * Null for OAuth2 users.
     */
    @Column
    private String password;

    /**
     * User's full name.
     * Required for all users.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * The authentication provider used by the user.
     * Determines how the user authenticates (LOCAL, GOOGLE, GITHUB).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    /**
     * The unique identifier from the authentication provider.
     * Used to link OAuth2 accounts.
     * Null for local users.
     */
    @Column
    private String providerId;

    /**
     * The URL of the user's profile picture.
     * Set automatically for OAuth2 users from their provider.
     * Can be updated for local users.
     */
    @Column
    private String avatarUrl;

    /**
     * The user's role in the system.
     * Used for authorization and access control.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * The timestamp when the user account was created.
     * Automatically set when the user is created.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Url> urls = new ArrayList<>();

    // TODO: implement the updatedAt timestamp logic
    // @CreationTimestamp
    // @Column(nullable = false)
    // private Timestamp updatedAt;

    public void validatePassword() {
        if (provider == AuthProvider.LOCAL && (password == null || password.trim().isEmpty())) {
            throw new IllegalStateException("Password is required for local users");
        }
        if (provider != AuthProvider.LOCAL && password != null) {
            throw new IllegalStateException("Password must be null for OAuth users");
        }
    }
}