package com.tinyls.urlshortener.controller;

import com.tinyls.urlshortener.dto.url.UrlDTO;
import com.tinyls.urlshortener.exception.UnauthorizedException;
import com.tinyls.urlshortener.model.UrlStatus;
import com.tinyls.urlshortener.security.UserDetailsAdapter;
import com.tinyls.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller handling URL shortening operations.
 * 
 * All endpoints are prefixed with /api/urls.
 * Most endpoints require authentication except for URL creation and
 * redirection.
 */
@Slf4j
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    /**
     * Creates a new shortened URL.
     * 
     * @param urlDTO      The URL details to create
     * @param userDetails Optional authenticated user details
     * @return The created URL details
     */
    @PostMapping("/")
    public ResponseEntity<UrlDTO> createUrl(
            @Valid @RequestBody UrlDTO urlDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = null;
        if (userDetails != null) {
            userId = ((UserDetailsAdapter) userDetails).getUserId();
            log.debug("Creating URL for authenticated user: {}", userId);
        } else {
            log.debug("Creating URL for anonymous user");
        }
        return new ResponseEntity<>(urlService.createUrl(urlDTO, userId), HttpStatus.CREATED);
    }

    /**
     * Redirects to the original URL and increments the click count.
     * Public endpoint, no authentication required.
     * 
     * @param shortCode The short code of the URL
     * @return A redirect response to the original URL
     */
    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirectToUrl(@PathVariable String shortCode) {
        log.debug("Redirecting to URL with short code: {}", shortCode);
        String originalUrl = urlService.getAndIncrementClicks(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", originalUrl)
                .build();
    }

    /**
     * Retrieves a URL by its ID.
     * Requires authentication.
     * 
     * @param id          The ID of the URL
     * @param userDetails The authenticated user's details
     * @return The URL details
     * @throws AccessDeniedException if the user is not authorized to access the URL
     */
    @GetMapping("/id/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UrlDTO> getUrlById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
            log.debug("Retrieving URL with ID: {} for user: {}", id, userId);
            return ResponseEntity.ok(urlService.getUrlById(id, userId));
        } catch (UnauthorizedException e) {
            throw new AccessDeniedException(e.getMessage());
        }
    }

    /**
     * Deletes a URL by its ID.
     * Requires authentication.
     * 
     * @param id          The ID of the URL to delete
     * @param userDetails The authenticated user's details
     * @return No content response if deletion is successful
     * @throws AccessDeniedException if the user is not authorized to delete the URL
     */
    @DeleteMapping("/id/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUrlById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
            log.info("Deleting URL with ID: {} for user: {}", id, userId);
            urlService.deleteUrlById(id, userId);
            return ResponseEntity.noContent().build();
        } catch (UnauthorizedException e) {
            throw new AccessDeniedException(e.getMessage());
        }
    }

    /**
     * Retrieves all URLs for the authenticated user.
     * Requires authentication.
     * 
     * @param userDetails The authenticated user's details
     * @return A list of the user's URLs
     */
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UrlDTO>> getUrlsByUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
        log.debug("Retrieving all URLs for user: {}", userId);
        return ResponseEntity.ok(urlService.getUrlsByUser(userId));
    }

    /**
     * Updates the status of a URL (ACTIVE <-> INACTIVE) by ID.
     * Only the owner can perform this action.
     *
     * @param id          The ID of the URL
     * @param statusBody  The new status in the request body
     * @param userDetails The authenticated user's details
     * @return The updated URL details
     */
    @PatchMapping("/id/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UrlDTO> updateUrlStatusById(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest statusBody,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
        UrlStatus newStatus = statusBody.getStatus();
        return ResponseEntity.ok(urlService.updateUrlStatusById(id, userId, newStatus));
    }

    /**
     * Request body for status update.
     */
    public static class StatusUpdateRequest {
        private UrlStatus status;

        public UrlStatus getStatus() {
            return status;
        }

        public void setStatus(UrlStatus status) {
            this.status = status;
        }
    }
}