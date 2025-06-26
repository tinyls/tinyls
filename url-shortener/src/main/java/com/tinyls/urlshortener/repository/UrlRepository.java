package com.tinyls.urlshortener.repository;

import com.tinyls.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Url entity.
 * Provides methods to perform database operations on Url entities.
 * 
 * This repository extends JpaRepository to inherit basic CRUD operations
 * and adds custom methods for URL-specific queries.
 */
@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    /**
     * Find a URL by its short code.
     * Used for URL redirection and retrieval.
     * 
     * @param shortCode the unique short code of the URL
     * @return an Optional containing the URL if found, empty otherwise
     */
    Optional<Url> findByShortCode(String shortCode);

    /**
     * Find all URLs associated with a specific user.
     * Used to list a user's shortened URLs.
     * 
     * @param userId the ID of the user
     * @return a list of URLs belonging to the user
     */
    List<Url> findByUserId(UUID userId);

    /**
     * Check if a URL exists with the given short code.
     * Used for validation during URL creation.
     * 
     * @param shortCode the short code to check
     * @return true if a URL exists with the short code, false otherwise
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Delete a URL by its short code.
     * Used for URL deletion operations.
     * 
     * @param shortCode the short code of the URL to delete
     */
    void deleteByShortCode(String shortCode);

    /**
     * Find the first URL that matches both the user ID and original URL.
     * Used to prevent duplicate URLs for the same user.
     * 
     * @param userId      the ID of the user
     * @param originalUrl the original URL to search for
     * @return an Optional containing the first matching URL if found, empty
     *         otherwise
     */
    Optional<Url> findFirstByUserIdAndOriginalUrl(UUID userId, String originalUrl);

    /**
     * Find the first URL that matches the original URL.
     * Used to check for existing URLs regardless of user.
     * 
     * @param originalUrl the original URL to search for
     * @return an Optional containing the first matching URL if found, empty
     *         otherwise
     */
    Optional<Url> findFirstByOriginalUrl(String originalUrl);

    /**
     * Find the first anonymous URL (no associated user) that matches the original
     * URL.
     * Used to check for existing anonymous URLs.
     * 
     * @param originalUrl the original URL to search for
     * @return an Optional containing the first matching anonymous URL if found,
     *         empty otherwise
     */
    Optional<Url> findFirstByOriginalUrlAndUserIsNull(String originalUrl);

    /**
     * Atomically increment the clicks count for a URL by its short code.
     *
     * @param shortCode the unique short code of the URL
     */
    @Modifying
    @Transactional
    @Query("UPDATE Url u SET u.clicks = u.clicks + 1 WHERE u.shortCode = :shortCode")
    void incrementClicks(String shortCode);
}