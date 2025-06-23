package com.tinyls.urlshortener.service.impl;

import com.tinyls.urlshortener.dto.url.UrlDTO;
import com.tinyls.urlshortener.exception.ResourceNotFoundException;
import com.tinyls.urlshortener.exception.UnauthorizedException;
import com.tinyls.urlshortener.mapper.UrlMapper;
import com.tinyls.urlshortener.model.Url;
import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.repository.UrlRepository;
import com.tinyls.urlshortener.repository.UserRepository;
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
 * and click tracking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UrlServiceImpl implements UrlService {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final UrlMapper urlMapper;

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
        return urlMapper.toDTO(savedUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public UrlDTO getUrlByShortCode(String shortCode, UUID userId) {
        log.debug("Retrieving URL with short code: {} for user: {}", shortCode, userId);

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

        return urlMapper.toDTO(url);
    }

    @Override
    public void deleteUrlByShortCode(String shortCode, UUID userId) {
        log.info("Deleting URL with short code: {} for user: {}", shortCode, userId);
        Url url = getUrlByShortCodeAndCheckOwnership(shortCode, userId);
        urlRepository.delete(url);
    }

    @Override
    public UrlDTO incrementClicks(String shortCode, UUID userId) {
        log.debug("Incrementing clicks for URL with short code: {} for user: {}", shortCode, userId);
        Url url = getUrlByShortCodeAndCheckOwnership(shortCode, userId);
        url.setClicks(url.getClicks() + 1);
        Url updatedUrl = urlRepository.save(url);
        return urlMapper.toDTO(updatedUrl);
    }

    @Override
    public String getAndIncrementClicks(String shortCode) {
        log.debug("Getting and incrementing clicks for URL with short code: {}", shortCode);
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL", shortCode));

        url.setClicks(url.getClicks() + 1);
        urlRepository.save(url);
        return url.getOriginalUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public UrlDTO getUrlById(Long id, UUID userId) {
        log.debug("Retrieving URL with ID: {} for user: {}", id, userId);
        Url url = getUrlByIdAndCheckOwnership(id, userId);
        return urlMapper.toDTO(url);
    }

    @Override
    public UrlDTO updateUrlById(Long id, UrlDTO urlDTO, UUID userId) {
        log.info("Updating URL with ID: {} for user: {}", id, userId);
        Url url = getUrlByIdAndCheckOwnership(id, userId);
        urlMapper.updateEntityFromDTO(urlDTO, url);
        Url updatedUrl = urlRepository.save(url);
        return urlMapper.toDTO(updatedUrl);
    }

    @Override
    public void deleteUrlById(Long id, UUID userId) {
        log.info("Deleting URL with ID: {} for user: {}", id, userId);
        Url url = getUrlByIdAndCheckOwnership(id, userId);
        urlRepository.delete(url);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UrlDTO> getUrlsByUser(UUID userId) {
        log.debug("Retrieving all URLs for user: {}", userId);
        return urlRepository.findByUserId(userId)
                .stream()
                .map(urlMapper::toDTO)
                .toList();
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