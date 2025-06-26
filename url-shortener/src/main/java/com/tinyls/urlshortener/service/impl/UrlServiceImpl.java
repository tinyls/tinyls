package com.tinyls.urlshortener.service.impl;

import com.tinyls.urlshortener.config.CacheConstants;
import com.tinyls.urlshortener.dto.url.UrlDTO;
import com.tinyls.urlshortener.exception.ResourceNotFoundException;
import com.tinyls.urlshortener.exception.UnauthorizedException;
import com.tinyls.urlshortener.mapper.UrlMapper;
import com.tinyls.urlshortener.model.Url;
import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.repository.UrlRepository;
import com.tinyls.urlshortener.repository.UserRepository;
import com.tinyls.urlshortener.service.CacheService;
import com.tinyls.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public UrlDTO getUrlByShortCode(String shortCode, UUID userId) {
        log.debug("Retrieving URL with short code: {} for user: {}", shortCode, userId);

        // Try to get from cache first
        String cacheKey = CacheConstants.urlByShortCodeKey(shortCode);
        Optional<UrlDTO> cachedUrl = cacheService.get(cacheKey, UrlDTO.class);

        if (cachedUrl.isPresent()) {
            log.debug("Cache hit for short code: {}", shortCode);
            UrlDTO urlDTO = cachedUrl.get();

            // Check ownership for cached URL
            if (urlDTO.getUserId() != null) {
                if (!urlDTO.getUserId().equals(userId)) {
                    throw new UnauthorizedException("You don't have permission to access this URL");
                }
            } else {
                if (userId != null) {
                    throw new UnauthorizedException("You don't have permission to access this URL");
                }
            }

            return urlDTO;
        }

        // Cache miss - get from database
        log.debug("Cache miss for short code: {}", shortCode);
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL", shortCode));

        // Check if URL belongs to a user
        if (url.getUser() != null) {
            // If URL belongs to a user, verify ownership
            if (!url.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("You don't have permission to access this URL");
            }
        } else {
            // If URL is anonymous, only allow access if the requesting user is also
            // anonymous
            if (userId != null) {
                throw new UnauthorizedException("You don't have permission to access this URL");
            }
        }

        UrlDTO urlDTO = urlMapper.toDTO(url);
        // Cache the URL for future requests
        cacheService.set(cacheKey, urlDTO, CacheConstants.URL_TTL);

        return urlDTO;
    }

    @Override
    public void deleteUrlByShortCode(String shortCode, UUID userId) {
        log.info("Deleting URL with short code: {} for user: {}", shortCode, userId);
        Url url = getUrlByShortCodeAndCheckOwnership(shortCode, userId);

        // Get user ID before deletion for cache invalidation
        UUID urlUserId = url.getUser() != null ? url.getUser().getId() : null;

        urlRepository.delete(url);

        // Invalidate caches
        String urlKey = CacheConstants.urlByShortCodeKey(shortCode);
        String clicksKey = CacheConstants.clicksKey(shortCode);
        cacheService.delete(urlKey, clicksKey);

        if (urlUserId != null) {
            String userUrlsKey = CacheConstants.userUrlsKey(urlUserId);
            cacheService.delete(userUrlsKey);
            log.debug("Invalidated user URLs cache for user: {}", urlUserId);
        }

        log.debug("Invalidated URL cache for short code: {}", shortCode);
    }

    @Override
    public UrlDTO incrementClicks(String shortCode, UUID userId) {
        log.debug("Incrementing clicks for URL with short code: {} for user: {}", shortCode, userId);

        String cacheKey = CacheConstants.urlByShortCodeKey(shortCode);
        Optional<UrlDTO> cachedUrl = cacheService.get(cacheKey, UrlDTO.class);

        if (cachedUrl.isPresent()) {
            log.debug("Cache hit for short code: {}", shortCode);
            UrlDTO urlDTO = cachedUrl.get();

            if (urlDTO.getUserId() != null && !urlDTO.getUserId().equals(userId)) {
                throw new UnauthorizedException("You don't have permission to access this URL");
            }

            urlRepository.incrementClicks(shortCode);
            cacheService.increment(CacheConstants.clicksKey(shortCode));
            urlDTO.setClicks(urlDTO.getClicks() + 1);
            cacheService.set(cacheKey, urlDTO, CacheConstants.URL_TTL);
            // Also update the cache for URL by ID
            String urlByIdKey = CacheConstants.urlKey(urlDTO.getId());
            cacheService.set(urlByIdKey, urlDTO, CacheConstants.URL_TTL);

            if (urlDTO.getUserId() != null) {
                String userUrlsKey = CacheConstants.userUrlsKey(urlDTO.getUserId());
                try {
                    cacheService.delete(userUrlsKey);
                    log.info("Invalidated user URLs cache for user: {}", urlDTO.getUserId());
                } catch (Exception e) {
                    log.error("Failed to invalidate user URLs cache for user: {}", urlDTO.getUserId(), e);

                }
            }

            return urlDTO;
        }

        log.debug("Cache miss for short code: {}", shortCode);
        Url url = getUrlByShortCodeAndCheckOwnership(shortCode, userId);
        url.setClicks(url.getClicks() + 1);
        Url updatedUrl = urlRepository.save(url);
        UrlDTO updatedUrlDTO = urlMapper.toDTO(updatedUrl);
        cacheService.set(cacheKey, updatedUrlDTO, CacheConstants.URL_TTL);
        // Also update the cache for URL by ID
        String urlByIdKey = CacheConstants.urlKey(updatedUrlDTO.getId());
        cacheService.set(urlByIdKey, updatedUrlDTO, CacheConstants.URL_TTL);
        return updatedUrlDTO;
    }

    @Override
    public String getAndIncrementClicks(String shortCode) {
        log.debug("Getting and incrementing clicks for URL with short code: {}", shortCode);

        String cacheKey = CacheConstants.urlByShortCodeKey(shortCode);
        Optional<UrlDTO> cachedUrl = cacheService.get(cacheKey, UrlDTO.class);

        if (cachedUrl.isPresent()) {
            log.debug("Cache hit for short code: {}", shortCode);
            urlRepository.incrementClicks(shortCode);
            cacheService.increment(CacheConstants.clicksKey(shortCode));
            UrlDTO urlDTO = cachedUrl.get();
            urlDTO.setClicks(urlDTO.getClicks() + 1);
            cacheService.set(cacheKey, urlDTO, CacheConstants.URL_TTL);
            // Also update the cache for URL by ID
            String urlByIdKey = CacheConstants.urlKey(urlDTO.getId());
            cacheService.set(urlByIdKey, urlDTO, CacheConstants.URL_TTL);
            if (urlDTO.getUserId() != null) {
                String userUrlsKey = CacheConstants.userUrlsKey(urlDTO.getUserId());
                try {
                    cacheService.delete(userUrlsKey);
                    log.info("Invalidated user URLs cache for user: {} (redirect)", urlDTO.getUserId());
                } catch (Exception e) {
                    log.error("Failed to invalidate user URLs cache for user: {} (redirect)", urlDTO.getUserId(), e);
                }
            }
            return urlDTO.getOriginalUrl();
        }

        log.debug("Cache miss for short code: {}", shortCode);
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL", shortCode));
        url.setClicks(url.getClicks() + 1);
        Url savedUrl = urlRepository.save(url);
        UrlDTO urlDTO = urlMapper.toDTO(savedUrl);
        cacheService.set(cacheKey, urlDTO, CacheConstants.URL_TTL);
        cacheService.increment(CacheConstants.clicksKey(shortCode));
        // Also update the cache for URL by ID
        String urlByIdKey = CacheConstants.urlKey(urlDTO.getId());
        cacheService.set(urlByIdKey, urlDTO, CacheConstants.URL_TTL);
        if (savedUrl.getUser() != null) {
            cacheService.delete(CacheConstants.userUrlsKey(savedUrl.getUser().getId()));
        }
        return savedUrl.getOriginalUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public UrlDTO getUrlById(Long id, UUID userId) {
        log.debug("Retrieving URL with ID: {} for user: {}", id, userId);

        // Try to get from cache first
        String cacheKey = CacheConstants.urlKey(id);
        Optional<UrlDTO> cachedUrl = cacheService.get(cacheKey, UrlDTO.class);

        if (cachedUrl.isPresent()) {
            log.debug("Cache hit for URL ID: {}", id);
            UrlDTO urlDTO = cachedUrl.get();

            // Check ownership for cached URL
            if (urlDTO.getUserId() != null && !urlDTO.getUserId().equals(userId)) {
                throw new UnauthorizedException("You don't have permission to access this URL");
            }

            return urlDTO;
        }

        // Cache miss - get from database
        log.debug("Cache miss for URL ID: {}", id);
        Url url = getUrlByIdAndCheckOwnership(id, userId);
        UrlDTO urlDTO = urlMapper.toDTO(url);

        // Cache the URL for future requests
        cacheService.set(cacheKey, urlDTO, CacheConstants.URL_TTL);

        return urlDTO;
    }

    @Override
    public UrlDTO updateUrlById(Long id, UrlDTO urlDTO, UUID userId) {
        log.info("Updating URL with ID: {} for user: {}", id, userId);
        Url url = getUrlByIdAndCheckOwnership(id, userId);

        // Get short code before update for cache invalidation
        String shortCode = url.getShortCode();
        UUID urlUserId = url.getUser() != null ? url.getUser().getId() : null;

        urlMapper.updateEntityFromDTO(urlDTO, url);
        Url updatedUrl = urlRepository.save(url);
        UrlDTO updatedUrlDTO = urlMapper.toDTO(updatedUrl);

        // Invalidate caches
        String urlKey = CacheConstants.urlByShortCodeKey(shortCode);
        String urlByIdKey = CacheConstants.urlKey(id);
        cacheService.delete(urlKey, urlByIdKey);

        if (urlUserId != null) {
            String userUrlsKey = CacheConstants.userUrlsKey(urlUserId);
            cacheService.delete(userUrlsKey);
            log.debug("Invalidated user URLs cache for user: {}", urlUserId);
        }

        log.debug("Invalidated URL cache for ID: {} and short code: {}", id, shortCode);

        return updatedUrlDTO;
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
        String urlKey = CacheConstants.urlByShortCodeKey(shortCode);
        String urlByIdKey = CacheConstants.urlKey(id);
        String clicksKey = CacheConstants.clicksKey(shortCode);
        cacheService.delete(urlKey, urlByIdKey, clicksKey);

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