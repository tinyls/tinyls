package com.tinyls.urlshortener.service.impl;

import com.tinyls.urlshortener.config.CacheConstants;
import com.tinyls.urlshortener.dto.url.UrlDTO;
import com.tinyls.urlshortener.exception.ResourceNotFoundException;
import com.tinyls.urlshortener.exception.UnauthorizedException;
import com.tinyls.urlshortener.mapper.UrlMapper;
import com.tinyls.urlshortener.model.Url;
import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.model.UrlStatus;
import com.tinyls.urlshortener.repository.UrlRepository;
import com.tinyls.urlshortener.repository.UserRepository;
import com.tinyls.urlshortener.service.CacheService;
import com.tinyls.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the UrlService interface.
 * Handles URL shortening operations including creation, retrieval, updates,
 * and click tracking with Redis caching support.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UrlServiceImpl implements UrlService {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final UrlMapper urlMapper;
    private final CacheService cacheService;
    private final CacheManager cacheManager;

    @Override
    @Caching(
            put = {
                    @CachePut(value = CacheConstants.URL_CACHE, key = "#result.id"),
                    @CachePut(value = CacheConstants.SHORT_CODE_MAPPING_CACHE, key = "#result.shortCode")
            },
            evict = {
                    @CacheEvict(value = CacheConstants.USER_URL_LIST_CACHE, key = "#userId", condition = "#userId != null")
            }
    )
    public UrlDTO createUrl(UrlDTO urlDTO, UUID userId) {
        log.info("Creating new URL for user: {}", userId);

        // For authenticated users, check if they already have this URL
        if (userId != null) {
            Optional<Url> existingUrl = urlRepository.findFirstByUserIdAndOriginalUrl(userId, urlDTO.getOriginalUrl());
            if (existingUrl.isPresent()) {
                log.debug("Found existing URL for user: {}", userId);
                return urlMapper.toDTO(existingUrl.get());
            }
        } else {
            // For anonymous users, check if this URL exists without a user
            Optional<Url> existingUrl = urlRepository.findFirstByOriginalUrlAndUserIsNull(urlDTO.getOriginalUrl());
            if (existingUrl.isPresent()) {
                log.debug("Found existing anonymous URL");
                return urlMapper.toDTO(existingUrl.get());
            }
        }

        // Create new URL
        Url url = urlMapper.toEntity(urlDTO);
        url.setClicks(0L);

        // Set user if authenticated
        if (userId != null) {
            User user = getUserById(userId);
            url.setUser(user);
        }

        Url savedUrl = urlRepository.saveAndFlush(url);
        UrlDTO savedUrlDTO = urlMapper.toDTO(savedUrl);

        return savedUrlDTO;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.URL_CACHE, key = "#id")
    public UrlDTO getUrlById(Long id, UUID userId) {
        log.debug("Retrieving URL with ID: {} for user: {}", id, userId);

        Url url = urlRepository.findById(id)
                          .orElseThrow(() -> new ResourceNotFoundException("URL", id.toString()));
        checkOwnership(url, userId);
        return urlMapper.toDTO(url);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.USER_URL_LIST_CACHE, key = "#userId")
    public List<UrlDTO> getUrlsByUser(UUID userId) {
        log.debug("Retrieving all URLs for user: {}", userId);
        return urlRepository.findByUserId(userId)
                       .stream()
                       .map(urlMapper::toDTO)
                       .toList();
    }

    /**
     * Update URL status by ID.
     *
     * @param id        URL ID
     * @param userId    User ID
     * @param newStatus New status
     * @return Updated URL DTO
     */
    @Override
    @Caching(
            put = {
                    @CachePut(value = CacheConstants.URL_CACHE, key = "#id")
            },
            evict = {
                    @CacheEvict(value = CacheConstants.USER_URL_LIST_CACHE, key = "#userId", condition = "#userId != null")
            }
    )
    public UrlDTO updateUrlStatusById(Long id, UUID userId, UrlStatus newStatus) {
        log.info("Updating status for URL with ID: {} to {} for user: {}", id, newStatus, userId);

        // Check ownership BEFORE making any changes to ensure security
        Url url = getUrlByIdAndCheckOwnership(id, userId);
        log.debug("Ownership verified for URL ID: {} - user: {}", id, userId);

        urlRepository.updateStatusById(id, newStatus);
        log.debug("Database update completed for URL ID: {}", id);

        Url refreshedUrl = urlRepository.findById(id)
                                   .orElseThrow(() -> new ResourceNotFoundException("URL", id.toString()));
        UrlDTO updatedUrlDTO = cacheUrl(refreshedUrl);

        log.info("Successfully updated URL status - ID: {}, New Status: {}, ShortCode: {}",
                id, newStatus, refreshedUrl.getShortCode());
        return updatedUrlDTO;
    }

    @Override
    @Caching(evict={
            @CacheEvict(value=CacheConstants.URL_CACHE,key="#id"),
            @CacheEvict(value=CacheConstants.USER_URL_LIST_CACHE,key="#userId",condition="#userId!=null")
    })
    public void deleteUrlById(Long id, UUID userId) {
        log.info("Deleting URL with ID: {} for user: {}", id, userId);
        Url url = getUrlByIdAndCheckOwnership(id, userId);

        // Capture short code for cache eviction
        String shortCode = url.getShortCode();

        urlRepository.delete(url);

        cacheManager.getCache(CacheConstants.SHORT_CODE_MAPPING_CACHE).evict(shortCode);
        cacheManager.getCache(CacheConstants.CLICKS_CACHE).evict(shortCode);

        // Invalidate caches
        String mappingKey = CacheConstants.shortCodeToIdKey(shortCode);
        String clicksKey = CacheConstants.clicksKey(shortCode);

        cacheService.delete(mappingKey, clicksKey);

        log.debug("Invalidated URL cache for ID: {} and short code: {}", id, shortCode);
    }

    @Override
    public String getAndIncrementClicks(String shortCode) {
        Url url = urlRepository.findByShortCodeAndStatus(shortCode, UrlStatus.ACTIVE)
                          .orElseThrow(() -> new ResourceNotFoundException("URL is not active", shortCode));

        // increment in DB
        urlRepository.incrementClicksById(url.getId());

        // update caches
        UrlDTO freshDTO = urlMapper.toDTO(
                urlRepository.findById(url.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("URL", url.getId().toString()))
        );
        // refresh both caches
        cacheManager.getCache(CacheConstants.URL_CACHE).put(freshDTO.getId(), freshDTO);
        cacheManager.getCache(CacheConstants.SHORT_CODE_MAPPING_CACHE)
                .put(freshDTO.getShortCode(), freshDTO.getId());
        // bump click‐count cache
        cacheService.increment(CacheConstants.clicksKey(shortCode));

        return freshDTO.getOriginalUrl();
    }


    /**
     * Update the URL cache with the latest entity state.
     */
    @CachePut(value = CacheConstants.URL_CACHE, key = "#url.id")
    public UrlDTO cacheUrl(Url url) {
        return urlMapper.toDTO(url);
    }

    /**
     * Refresh URL after click increment to ensure transaction is committed.
     * Uses a separate transaction to force commit.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Url refreshUrlAfterClickIncrement(Long urlId) {
        return urlRepository.findById(urlId)
                .orElseThrow(() -> new ResourceNotFoundException("URL", urlId.toString()));
    }

    /**
     * Retrieves a user by their ID.
     * 
     * @param id The user's ID
     * @return The user
     * @throws ResourceNotFoundException if the user is not found
     */
    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
    }

    /**
     * Retrieves a URL by its short code and verifies ownership.
     * 
     * @param shortCode The URL's short code
     * @param userId    The user's ID
     * @return The URL
     * @throws ResourceNotFoundException if the URL is not found
     * @throws UnauthorizedException     if the user is not authorized to access the
     *                                   URL
     */
    private Url getUrlByShortCodeAndCheckOwnership(String shortCode, UUID userId) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL", shortCode));
        checkOwnership(url, userId);
        return url;
    }

    /**
     * Retrieves a URL by its ID and verifies ownership.
     * 
     * @param id     The URL's ID
     * @param userId The user's ID
     * @return The URL
     * @throws ResourceNotFoundException if the URL is not found
     * @throws UnauthorizedException     if the user is not authorized to access the
     *                                   URL
     */
    private Url getUrlByIdAndCheckOwnership(Long id, UUID userId) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL", id.toString()));
        checkOwnership(url, userId);
        return url;
    }

    /**
     * Verifies that a user has permission to access or modify a URL.
     * 
     * @param url    The URL to check
     * @param userId The user's ID
     * @throws UnauthorizedException if the user is not authorized
     */
    private void checkOwnership(Url url, UUID userId) {
        if (url.getUser() == null) {
            throw new UnauthorizedException("This URL is anonymous and cannot be modified");
        }
        if (!url.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this URL");
        }
    }
}