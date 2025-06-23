package com.tinyls.urlshortener.repository;

import java.util.Optional;
import java.util.UUID;

import com.tinyls.urlshortener.model.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tinyls.urlshortener.model.User;

/**
 * Repository interface for User entity.
 * Provides methods to perform database operations on User entities.
 * 
 * This repository extends JpaRepository to inherit basic CRUD operations
 * and adds custom methods for user-specific queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Find a user by their email address.
     * 
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by their OAuth2 provider and provider-specific ID.
     * Used for OAuth2 authentication to find existing users.
     * 
     * @param provider   the OAuth2 provider (e.g., GOOGLE, GITHUB)
     * @param providerId the unique identifier from the provider
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    /**
     * Check if a user exists with the given email address.
     * Used for validation during user registration.
     * 
     * @param email the email address to check
     * @return true if a user exists with the email, false otherwise
     */
    boolean existsByEmail(String email);
}