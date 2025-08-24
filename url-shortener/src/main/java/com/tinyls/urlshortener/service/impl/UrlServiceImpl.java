package com.tinyls.urlshortener.service.impl;

import com.tinyls.urlshortener.config.CacheConstants;
import com.tinyls.urlshortener.dto.url.UrlDTO;
import com.tinyls.urlshortener.exception.ResourceNotFoundException;
import com.tinyls.urlshortener.exception.UnauthorizedException;
import com.tinyls.urlshortener.exception.ShortCodeAlreadyExistsException;
import com.tinyls.urlshortener.mapper.UrlMapper;
import com.tinyls.urlshortener.model.Url;
import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.model.UrlStatus;
import com.tinyls.urlshortener.repository.UrlRepository;
import com.tinyls.urlshortener.repository.UserRepository;
import com.tinyls.urlshortener.service.CacheService;
import com.tinyls.urlshortener.service.UrlService;
import com.tinyls.urlshortener.service.UrlTitleExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tinyls.urlshortener.util.ShortCodeValidator;

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
    private final UrlTitleExtractionService urlTitleExtractionService;

    @Override
    @Caching(put = {
            @CachePut(value = CacheConstants.URL_CACHE, key = "#result.id"),
            @CachePut(value = CacheConstants.SHORT_CODE_MAPPING_CACHE, key = "#result.shortCode")
    }, evict = {
            @CacheEvict(value = CacheConstants.USER_URL_LIST_CACHE, key = "#userId", condition = "#userId != null")
    })
    public UrlDTO createUrl(UrlDTO urlDTO, UUID userId) {
        log.info("Creating new URL for user: {}", userId);

        // If caller specifies a custom shortCode, validate format and reserved words
        // early
        if (urlDTO.getShortCode() != null && !urlDTO.getShortCode().isBlank()) {
            // Only authenticated users can create custom short codes
            if (userId == null) {
                throw new UnauthorizedException("Custom short codes are only available for authenticated users");
            }

            ShortCodeValidator.validateOrThrow(urlDTO.getShortCode());
        }

        // For authenticated users, check if they already have this URL (re-use
        // existing)
        if (userId != null && (urlDTO.getShortCode() == null || urlDTO.getShortCode().isBlank())) {
            Optional<Url> existingUrl = urlRepository.findFirstByUserIdAndOriginalUrl(userId, urlDTO.getOriginalUrl());
            if (existingUrl.isPresent()) {
                log.debug("Found existing URL for user: {}", userId);
                return urlMapper.toDTO(existingUrl.get());
            }
        } else if (userId == null && (urlDTO.getShortCode() == null || urlDTO.getShortCode().isBlank())) {
            // For anonymous users when not forcing a custom code, reuse existing anonymous
            // URL
            Optional<Url> existingUrl = urlRepository.findFirstByOriginalUrlAndUserIsNull(urlDTO.getOriginalUrl());
            if (existingUrl.isPresent()) {
                log.debug("Found existing anonymous URL");
                return urlMapper.toDTO(existingUrl.get());
            }
        }

        // Map and create new URL
        Url url = urlMapper.toEntity(urlDTO);
        url.setClicks(0L);

        // Set user if authenticated
        if (userId != null) {
            User user = getUserById(userId);
            url.setUser(user);
        }

        try {
            Url savedUrl = urlRepository.saveAndFlush(url);

            // Extract title asynchronously if not provided and title extraction is enabled
            if ((urlDTO.getTitle() == null || urlDTO.getTitle().isBlank()) &&
                    urlTitleExtractionService.isTitleExtractionEnabled()) {

                log.debug("Initiating async title extraction for URL: {}", urlDTO.getOriginalUrl());
                urlTitleExtractionService.extractTitleAsync(urlDTO.getOriginalUrl())
                        .thenAccept(extractedTitle -> {
                            if (extractedTitle != null && !extractedTitle.isBlank()) {
                                try {
                                    // Update the URL with the extracted title
                                    savedUrl.setTitle(extractedTitle);
                                    urlRepository.save(savedUrl);

                                    // Update cache with the new title
                                    cacheManager.getCache(CacheConstants.URL_CACHE).put(savedUrl.getId(),
                                            urlMapper.toDTO(savedUrl));

                                    log.info("Successfully extracted and saved title '{}' for URL ID: {}",
                                            extractedTitle, savedUrl.getId());
                                } catch (Exception e) {
                                    log.warn("Failed to save extracted title '{}' for URL ID: {}: {}", extractedTitle,
                                            savedUrl.getId(), e.getMessage());
                                }
                            }
                        })
                        .exceptionally(throwable -> {
                            log.debug("Title extraction failed for URL ID: {}: {}", savedUrl.getId(),
                                    throwable.getMessage());
                            return null;
                        });
            }

            return urlMapper.toDTO(savedUrl);
        } catch (DataIntegrityViolationException ex) {
            // Handle unique constraint violation on short_code
            throw new ShortCodeAlreadyExistsException(
                    urlDTO.getShortCode() != null ? urlDTO.getShortCode() : "generated");
        }
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
     * Update the status of a URL (ACTIVE <-> INACTIVE) by ID.
     * Only the owner can perform this action.
     *
     * @param id        URL ID
     * @param userId    ID of the user updating the status
     * @param newStatus The new status to set
     * @return updated URL data
     * @throws ResourceNotFoundException if URL not found
     * @throws UnauthorizedException     if user is not the owner
     */
    @Override
    @CachePut(value = CacheConstants.URL_CACHE, key = "#id")
    @CacheEvict(value = CacheConstants.USER_URL_LIST_CACHE, key = "#userId", condition = "#userId != null")
    public UrlDTO updateUrlStatusById(Long id, UUID userId, UrlStatus newStatus) {
        log.info("Updating URL status to {} for URL ID: {} by user: {}", newStatus, id, userId);
        Url url = getUrlByIdAndCheckOwnership(id, userId);
        url.setStatus(newStatus);
        Url savedUrl = urlRepository.save(url);
        return urlMapper.toDTO(savedUrl);
    }

    /**
     * Update URL details by ID.
     * Only the owner can perform this action.
     * 
     * This method is designed to be extensible for future URL fields.
     * Currently supports updating:
     * - originalUrl: The target URL to redirect to
     * - status: The status of the URL (ACTIVE, INACTIVE)
     * - title: The title of the URL
     * - description: The description of the URL
     * 
     * Only provided fields will be updated; omitted fields will retain their
     * current values.
     * Future fields like tags, expirationDate, etc. can be easily added.
     *
     * @param id            URL ID
     * @param userId        ID of the user updating the URL
     * @param updateRequest The update request containing new values
     * @return updated URL data
     * @throws ResourceNotFoundException if URL not found
     * @throws UnauthorizedException     if user is not the owner
     */
    @Override
    @CachePut(value = CacheConstants.URL_CACHE, key = "#id")
    @CacheEvict(value = CacheConstants.USER_URL_LIST_CACHE, key = "#userId", condition = "#userId != null")
    public UrlDTO updateUrlById(Long id, UUID userId, com.tinyls.urlshortener.dto.url.UrlUpdateRequest updateRequest) {
        log.info("Updating URL details for URL ID: {} by user: {}", id, userId);

        Url url = getUrlByIdAndCheckOwnership(id, userId);

        // Update originalUrl if provided
        if (updateRequest.getOriginalUrl() != null) {
            url.setOriginalUrl(updateRequest.getOriginalUrl());
            log.debug("Updated originalUrl for URL ID: {}", id);
        }

        // Update status if provided
        if (updateRequest.getStatus() != null) {
            url.setStatus(updateRequest.getStatus());
            log.debug("Updated status to {} for URL ID: {}", updateRequest.getStatus(), id);
        }

        // Update title if provided
        if (updateRequest.getTitle() != null) {
            url.setTitle(updateRequest.getTitle());
            log.debug("Updated title for URL ID: {}", id);
        }

        // Update description if provided
        if (updateRequest.getDescription() != null) {
            url.setDescription(updateRequest.getDescription());
            log.debug("Updated description for URL ID: {}", id);
        }

        // Future extensible fields can be updated here:
        // if (updateRequest.getTags() != null) {
        // url.setTags(updateRequest.getTags());
        // log.debug("Updated tags for URL ID: {}", id);
        // }
        // if (updateRequest.getExpirationDate() != null) {
        // url.setExpirationDate(updateRequest.getExpirationDate());
        // log.debug("Updated expirationDate for URL ID: {}", id);
        // }
        // if (updateRequest.getPasswordProtection() != null) {
        // url.setPasswordProtection(updateRequest.getPasswordProtection());
        // log.debug("Updated passwordProtection for URL ID: {}", id);
        // }
        // if (updateRequest.getAnalyticsPreferences() != null) {
        // url.setAnalyticsPreferences(updateRequest.getAnalyticsPreferences());
        // log.debug("Updated analyticsPreferences for URL ID: {}", id);
        // }
        // etc.

        Url savedUrl = urlRepository.save(url);
        return urlMapper.toDTO(savedUrl);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.URL_CACHE, key = "#id"),
            @CacheEvict(value = CacheConstants.USER_URL_LIST_CACHE, key = "#userId", condition = "#userId!=null")
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

    // TODO: verify if using cachemanager is correct practice
    // TODO: when url not active, throw correct exception and message
    @Override
    public String getAndIncrementClicks(String shortCode) {
        Url url = urlRepository.findByShortCodeAndStatus(shortCode, UrlStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("URL is not active", shortCode));

        // increment in DB
        urlRepository.incrementClicksById(url.getId());

        // update caches
        UrlDTO freshDTO = urlMapper.toDTO(
                urlRepository.findById(url.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("URL", url.getId().toString())));
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