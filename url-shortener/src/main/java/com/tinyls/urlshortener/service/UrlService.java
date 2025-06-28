package com.tinyls.urlshortener.service;

import com.tinyls.urlshortener.dto.url.UrlDTO;
import com.tinyls.urlshortener.model.UrlStatus;

import java.util.List;
import java.util.UUID;

public interface UrlService {
    /**
     * Create a new URL or return existing URL if found
     * 
     * @param urlDTO URL data
     * @param userId ID of the user creating the URL (null for anonymous users)
     * @return created URL with generated short code or existing URL if found
     * @throws jakarta.persistence.EntityNotFoundException if user not found (when
     *                                                     userId is provided)
     */
    UrlDTO createUrl(UrlDTO urlDTO, UUID userId);

    /**
     * Get URL by ID
     * 
     * @param id     URL ID
     * @param userId ID of the user requesting the URL
     * @return URL data
     * @throws jakarta.persistence.EntityNotFoundException             if URL not
     *                                                                 found
     * @throws com.tinyls.urlshortener.exception.UnauthorizedException if user is
     *                                                                 not the owner
     */
    UrlDTO getUrlById(Long id, UUID userId);

    /**
     * Delete URL
     * 
     * @param id     URL ID
     * @param userId ID of the user deleting the URL
     * @throws jakarta.persistence.EntityNotFoundException             if URL not
     *                                                                 found
     * @throws com.tinyls.urlshortener.exception.UnauthorizedException if user is
     *                                                                 not the owner
     */
    void deleteUrlById(Long id, UUID userId);

    /**
     * Get all URLs for the authenticated user
     * 
     * @param userId ID of the authenticated user
     * @return list of user's URLs
     * @throws jakarta.persistence.EntityNotFoundException if user not found
     */
    List<UrlDTO> getUrlsByUser(UUID userId);

    /**
     * Get original URL and increment click count
     * 
     * @param shortCode URL short code
     * @return original URL
     * @throws jakarta.persistence.EntityNotFoundException if URL not found
     */
    String getAndIncrementClicks(String shortCode);

    /**
     * Update the status of a URL (ACTIVE <-> INACTIVE) by ID.
     * Only the owner can perform this action.
     *
     * @param id        URL ID
     * @param userId    ID of the user updating the status
     * @param newStatus The new status to set
     * @return updated URL data
     * @throws jakarta.persistence.EntityNotFoundException             if URL not
     *                                                                 found
     * @throws com.tinyls.urlshortener.exception.UnauthorizedException if user is
     *                                                                 not the owner
     */
    UrlDTO updateUrlStatusById(Long id, UUID userId, UrlStatus newStatus);
}