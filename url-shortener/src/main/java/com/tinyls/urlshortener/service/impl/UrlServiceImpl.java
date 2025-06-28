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

    @Override
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

        // Invalidate user URLs cache
        if (userId != null) {
            String userUrlsKey = CacheConstants.userUrlsKey(userId);
            cacheService.delete(userUrlsKey);
            log.debug("Invalidated user URLs cache for user: {}", userId);
        }

        return savedUrlDTO;
    }

    @Override
    public String getAndIncrementClicks(String shortCode) {
        log.debug("Getting and incrementing clicks for URL with short code: {}", shortCode);

        String mappingKey = CacheConstants.shortCodeToIdKey(shortCode);
        Optional<Long> cachedUrlId = cacheService.get(mappingKey, Long.class);

        if (cachedUrlId.isPresent()) {
            log.debug("Cache hit for short code mapping: {} -> ID: {}", shortCode, cachedUrlId.get());
            // Get the URL by ID using cached ID
            UrlDTO urlDTO = getUrlById(cachedUrlId.get(), null); // No user check for redirects
            if (urlDTO.getStatus() != UrlStatus.ACTIVE) {
                throw new ResourceNotFoundException("URL is not active", shortCode);
            }
            // Use ID for increment operation
            urlRepository.incrementClicksById(urlDTO.getId());
            cacheService.increment(CacheConstants.clicksKey(shortCode));

            // Get fresh data from database after increment to ensure cache consistency
            Url refreshedUrl = refreshUrlAfterClickIncrement(urlDTO.getId());
            UrlDTO freshUrlDTO = urlMapper.toDTO(refreshedUrl);

            // Force update cache with fresh data
            forceUpdateUrlCache(refreshedUrl, freshUrlDTO);

            if (freshUrlDTO.getUserId() != null) {
                String userUrlsKey = CacheConstants.userUrlsKey(freshUrlDTO.getUserId());
                try {
                    cacheService.delete(userUrlsKey);
                    log.info("Invalidated user URLs cache for user: {} (redirect)", freshUrlDTO.getUserId());
                } catch (Exception e) {
                    log.error("Failed to invalidate user URLs cache for user: {} (redirect)", freshUrlDTO.getUserId(),
                            e);
                }
            }
            return freshUrlDTO.getOriginalUrl();
        }

        log.debug("Cache miss for short code: {}", shortCode);
        Url url = urlRepository.findByShortCodeAndStatus(shortCode, UrlStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("URL is not active", shortCode));

        // Use ID for increment operation
        urlRepository.incrementClicksById(url.getId());

        // Force transaction commit and refresh the URL to get the updated click count
        Url refreshedUrl = refreshUrlAfterClickIncrement(url.getId());

        UrlDTO urlDTO = urlMapper.toDTO(refreshedUrl);

        // Force update cache with fresh data
        forceUpdateUrlCache(refreshedUrl, urlDTO);

        cacheService.increment(CacheConstants.clicksKey(shortCode));

        if (refreshedUrl.getUser() != null) {
            cacheService.delete(CacheConstants.userUrlsKey(refreshedUrl.getUser().getId()));
        }
        return refreshedUrl.getOriginalUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public UrlDTO getUrlById(Long id, UUID userId) {
        log.debug("Retrieving URL with ID: {} for user: {}", id, userId);

        // Try to get from cache first
        String cacheKey = CacheConstants.urlKey(id);
        log.debug("Using cache key for URL ID {}: {}", id, cacheKey);
        Optional<UrlDTO> cachedUrl = cacheService.get(cacheKey, UrlDTO.class);

        if (cachedUrl.isPresent()) {
            log.debug("Cache hit for URL ID: {} - Status: {}", id, cachedUrl.get().getStatus());
            UrlDTO urlDTO = cachedUrl.get();

            // Check ownership for cached URL
            if (urlDTO.getUserId() != null && !urlDTO.getUserId().equals(userId)) {
                throw new UnauthorizedException("You don't have permission to access this URL");
            }

            // For now, return cached data but log a warning if it might be stale
            // In a production environment, you might want to add a timestamp check
            return urlDTO;
        }

        // Cache miss - get from database
        log.debug("Cache miss for URL ID: {}", id);
        Url url = getUrlByIdAndCheckOwnership(id, userId);
        UrlDTO urlDTO = urlMapper.toDTO(url);
        log.debug("Retrieved from database - URL ID: {}, Status: {}", id, urlDTO.getStatus());

        // Cache the URL for future requests
        cacheService.set(cacheKey, urlDTO, CacheConstants.URL_TTL);
        log.debug("Cached URL for ID: {} with key: {}", id, cacheKey);

        return urlDTO;
    }

    @Override
    public void deleteUrlById(Long id, UUID userId) {
        log.info("Deleting URL with ID: {} for user: {}", id, userId);
        Url url = getUrlByIdAndCheckOwnership(id, userId);

        // Get short code and user ID before deletion for cache invalidation
        String shortCode = url.getShortCode();
        UUID urlUserId = url.getUser() != null ? url.getUser().getId() : null;

        urlRepository.delete(url);

        // Invalidate caches
        String mappingKey = CacheConstants.shortCodeToIdKey(shortCode);
        String urlByIdKey = CacheConstants.urlKey(id);
        String clicksKey = CacheConstants.clicksKey(shortCode);
        cacheService.delete(mappingKey, urlByIdKey, clicksKey);

        if (urlUserId != null) {
            String userUrlsKey = CacheConstants.userUrlsKey(urlUserId);
            cacheService.delete(userUrlsKey);
            log.debug("Invalidated user URLs cache for user: {}", urlUserId);
        }

        log.debug("Invalidated URL cache for ID: {} and short code: {}", id, shortCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UrlDTO> getUrlsByUser(UUID userId) {
        log.debug("Retrieving all URLs for user: {}", userId);

        // Try to get from cache first
        String cacheKey = CacheConstants.userUrlsKey(userId);

        // Use a wrapper class to preserve type information during serialization
        try {
            Optional<UrlListWrapper> cachedWrapper = cacheService.get(cacheKey, UrlListWrapper.class);
            if (cachedWrapper.isPresent()) {
                log.debug("Cache hit for user URLs: {}", userId);
                return cachedWrapper.get().getUrls();
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached URLs for user: {}, will fetch from database", userId, e);
            // Delete the corrupted cache entry
            cacheService.delete(cacheKey);
        }

        // Cache miss - get from database
        log.debug("Cache miss for user URLs: {}", userId);
        List<UrlDTO> urls = urlRepository.findByUserId(userId)
                .stream()
                .map(urlMapper::toDTO)
                .toList();

        // Cache the URLs using a wrapper to preserve type information
        try {
            UrlListWrapper wrapper = new UrlListWrapper(urls);
            cacheService.set(cacheKey, wrapper, CacheConstants.URL_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache URLs for user: {}", userId, e);
        }

        return urls;
    }

    /**
     * Update URL status by ID.
     * 
     * @param id        URL ID
     * @param userId    User ID
     * @param newStatus New status
     * @return Updated URL DTO
     */
    public UrlDTO updateUrlStatusById(Long id, UUID userId, UrlStatus newStatus) {
        log.info("Updating status for URL with ID: {} to {} for user: {}", id, newStatus, userId);

        // Check ownership BEFORE making any changes to ensure security
        Url url = getUrlByIdAndCheckOwnership(id, userId);
        log.debug("Ownership verified for URL ID: {} - user: {}", id, userId);

        // Use ID for status update operation
        urlRepository.updateStatusById(id, newStatus);
        log.debug("Database update completed for URL ID: {}", id);

        // Force transaction commit and refresh the URL to get the updated status
        Url refreshedUrl = refreshUrlAfterStatusUpdate(id);
        log.debug("Refreshed URL from database - ID: {}, Status: {}, ShortCode: {}",
                id, refreshedUrl.getStatus(), refreshedUrl.getShortCode());

        UrlDTO updatedUrlDTO = urlMapper.toDTO(refreshedUrl);
        log.debug("Created DTO from refreshed URL - ID: {}, Status: {}",
                updatedUrlDTO.getId(), updatedUrlDTO.getStatus());

        // Force update cache with fresh data to ensure consistency
        forceUpdateUrlCache(refreshedUrl, updatedUrlDTO);

        // Invalidate user URLs cache to ensure fresh data on next fetch
        if (userId != null) {
            String userUrlsKey = CacheConstants.userUrlsKey(userId);
            cacheService.delete(userUrlsKey);
            log.debug("Invalidated user URLs cache for user: {} (status update)", userId);
        }

        log.info("Successfully updated URL status - ID: {}, New Status: {}, ShortCode: {}",
                id, newStatus, refreshedUrl.getShortCode());
        return updatedUrlDTO;
    }

    /**
     * Force update the cache with fresh data from database.
     * This ensures cache consistency after any URL modifications.
     */
    private void forceUpdateUrlCache(Url url, UrlDTO urlDTO) {
        String shortCode = url.getShortCode();
        String mappingKey = CacheConstants.shortCodeToIdKey(shortCode);
        String urlByIdKey = CacheConstants.urlKey(url.getId());

        // Update both the mapping and the URL cache with fresh data
        cacheService.set(mappingKey, url.getId(), CacheConstants.SHORT_CODE_MAPPING_TTL);
        cacheService.set(urlByIdKey, urlDTO, CacheConstants.URL_TTL);

        log.debug("Force updated cache for URL ID: {} with fresh data - Status: {}",
                url.getId(), urlDTO.getStatus());
    }

    /**
     * Debug method to manually clear cache for a specific URL.
     * This can be used for testing and debugging cache issues.
     */
    public void clearUrlCache(Long urlId, String shortCode) {
        String mappingKey = CacheConstants.shortCodeToIdKey(shortCode);
        String urlByIdKey = CacheConstants.urlKey(urlId);

        cacheService.delete(mappingKey, urlByIdKey);
        log.info("Manually cleared cache for URL ID: {}, ShortCode: {}", urlId, shortCode);
    }

    /**
     * Debug method to check cache contents for a specific URL.
     * This can be used for testing and debugging cache issues.
     */
    public void debugUrlCache(Long urlId, String shortCode) {
        String mappingKey = CacheConstants.shortCodeToIdKey(shortCode);
        String urlByIdKey = CacheConstants.urlKey(urlId);

        Optional<Long> cachedUrlId = cacheService.get(mappingKey, Long.class);
        Optional<UrlDTO> cachedById = cacheService.get(urlByIdKey, UrlDTO.class);

        log.info("Cache debug for URL ID: {}, ShortCode: {}", urlId, shortCode);
        log.info("ShortCode mapping key: {} - Present: {}, Mapped ID: {}",
                mappingKey, cachedUrlId.isPresent(),
                cachedUrlId.isPresent() ? cachedUrlId.get() : "N/A");
        log.info("ID cache key: {} - Present: {}, Status: {}",
                urlByIdKey, cachedById.isPresent(),
                cachedById.isPresent() ? cachedById.get().getStatus() : "N/A");
    }

    /**
     * Debug method to force refresh cache from database.
     * This can be used for testing and debugging cache issues.
     */
    public UrlDTO forceRefreshUrlCache(Long urlId, UUID userId) {
        log.info("Force refreshing cache for URL ID: {}", urlId);

        // Clear existing cache
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new ResourceNotFoundException("URL", urlId.toString()));
        String shortCode = url.getShortCode();
        clearUrlCache(urlId, shortCode);

        // Force cache miss by calling getUrlById
        return getUrlById(urlId, userId);
    }

    /**
     * Refresh URL after status update to ensure transaction is committed.
     * Uses a separate transaction to force commit.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Url refreshUrlAfterStatusUpdate(Long urlId) {
        return urlRepository.findById(urlId)
                .orElseThrow(() -> new ResourceNotFoundException("URL", urlId.toString()));
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

    /**
     * Wrapper class to preserve type information when caching List<UrlDTO>.
     * This helps Jackson properly serialize/deserialize the list.
     */
    private static class UrlListWrapper {
        private List<UrlDTO> urls;

        // Default constructor for Jackson
        public UrlListWrapper() {
        }

        public UrlListWrapper(List<UrlDTO> urls) {
            this.urls = urls;
        }

        public List<UrlDTO> getUrls() {
            return urls;
        }

        public void setUrls(List<UrlDTO> urls) {
            this.urls = urls;
        }
    }
}