package com.tinyls.urlshortener.service;

import com.tinyls.urlshortener.dto.url.UrlDTO;
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
     * Get URL by short code
     * 
     * @param shortCode URL short code
     * @param userId    ID of the user requesting the URL
     * @return URL data
     * @throws jakarta.persistence.EntityNotFoundException if URL not found
     */
    UrlDTO getUrlByShortCode(String shortCode, UUID userId);

    /**
     * Delete URL by ShortCode
     * 
     * @param shortCode URL short code
     * @param userId    ID of the user deleting the URL
     * @throws jakarta.persistence.EntityNotFoundException             if URL not
     *                                                                 found
     * @throws com.tinyls.urlshortener.exception.UnauthorizedException if user is
     *                                                                 not the owner
     */
    void deleteUrlByShortCode(String shortCode, UUID userId);

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
     * Update URL data
     * 
     * @param id     URL ID
     * @param urlDTO updated URL data
     * @param userId ID of the user updating the URL
     * @return updated URL
     * @throws jakarta.persistence.EntityNotFoundException             if URL not
     *                                                                 found
     * @throws com.tinyls.urlshortener.exception.UnauthorizedException if user is
     *                                                                 not the owner
     */
    UrlDTO updateUrlById(Long id, UrlDTO urlDTO, UUID userId);

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
     * Increment click count for a URL
     * 
     * @param shortCode URL short code
     * @param userId    ID of the user incrementing clicks
     * @return updated URL data
     * @throws jakarta.persistence.EntityNotFoundException             if URL not
     *                                                                 found
     * @throws com.tinyls.urlshortener.exception.UnauthorizedException if user is
     *                                                                 not the owner
     */
    UrlDTO incrementClicks(String shortCode, UUID userId);

    /**
     * Get original URL and increment click count
     * 
     * @param shortCode URL short code
     * @return original URL
     * @throws jakarta.persistence.EntityNotFoundException if URL not found
     */
    String getAndIncrementClicks(String shortCode);
}