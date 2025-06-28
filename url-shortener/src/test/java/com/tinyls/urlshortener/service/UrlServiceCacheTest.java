package com.tinyls.urlshortener.service;

import com.tinyls.urlshortener.config.CacheConstants;
import com.tinyls.urlshortener.dto.url.UrlDTO;
import com.tinyls.urlshortener.model.Url;
import com.tinyls.urlshortener.model.UrlStatus;
import com.tinyls.urlshortener.repository.UrlRepository;
import com.tinyls.urlshortener.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for URL service caching functionality.
 * Tests Redis caching behavior in the URL service.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UrlServiceCacheTest {

    @Autowired
    private UrlService urlService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private UrlRepository urlRepository;

    private UrlDTO testUrlDTO;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUrlDTO = UrlDTO.builder()
                .originalUrl("https://example.com/test")
                .build();
    }

    // @Test
    // void testGetAndIncrementClicks_CacheHit() {
    // // Create a URL first
    // UrlDTO createdUrl = urlService.createUrl(testUrlDTO, testUserId);
    // String shortCode = createdUrl.getShortCode();

    // // Clear any existing cache
    // String cacheKey = CacheConstants.urlByShortCodeKey(shortCode);
    // cacheService.delete(cacheKey);

    // // First call should be cache miss and populate cache
    // String originalUrl1 = urlService.getAndIncrementClicks(shortCode);
    // assertEquals(testUrlDTO.getOriginalUrl(), originalUrl1);

    // // Verify URL is cached
    // Optional<UrlDTO> cachedUrl = cacheService.get(cacheKey, UrlDTO.class);
    // assertTrue(cachedUrl.isPresent());
    // assertEquals(shortCode, cachedUrl.get().getShortCode());

    // // Second call should be cache hit
    // String originalUrl2 = urlService.getAndIncrementClicks(shortCode);
    // assertEquals(testUrlDTO.getOriginalUrl(), originalUrl2);

    // // Verify clicks were incremented in cache
    // String clicksKey = CacheConstants.clicksKey(shortCode);
    // Optional<Long> cachedClicks = cacheService.get(clicksKey, Long.class);
    // assertTrue(cachedClicks.isPresent());
    // assertTrue(cachedClicks.get() > 0);
    // }

    // @Test
    // void testGetUrlByShortCode_CacheHit() {
    // // Create a URL first
    // UrlDTO createdUrl = urlService.createUrl(testUrlDTO, testUserId);
    // String shortCode = createdUrl.getShortCode();

    // // Clear any existing cache
    // String cacheKey = CacheConstants.urlByShortCodeKey(shortCode);
    // cacheService.delete(cacheKey);

    // // First call should be cache miss and populate cache
    // UrlDTO url1 = urlService.getUrlByShortCode(shortCode, testUserId);
    // assertEquals(shortCode, url1.getShortCode());

    // // Verify URL is cached
    // Optional<UrlDTO> cachedUrl = cacheService.get(cacheKey, UrlDTO.class);
    // assertTrue(cachedUrl.isPresent());
    // assertEquals(shortCode, cachedUrl.get().getShortCode());

    // // Second call should be cache hit
    // UrlDTO url2 = urlService.getUrlByShortCode(shortCode, testUserId);
    // assertEquals(shortCode, url2.getShortCode());
    // }

    @Test
    void testGetUrlsByUser_CacheHit() {
        // Create multiple URLs for the user
        UrlDTO url1 = urlService.createUrl(testUrlDTO, testUserId);
        UrlDTO url2 = urlService.createUrl(
                UrlDTO.builder().originalUrl("https://example.com/test2").build(),
                testUserId);

        // Clear any existing cache
        String cacheKey = CacheConstants.userUrlsKey(testUserId);
        cacheService.delete(cacheKey);

        // First call should be cache miss and populate cache
        var urls1 = urlService.getUrlsByUser(testUserId);
        assertTrue(urls1.size() >= 2);

        // Verify URLs are cached
        Optional<Object> cachedUrls = cacheService.get(cacheKey, Object.class);
        assertTrue(cachedUrls.isPresent());

        // Second call should be cache hit
        var urls2 = urlService.getUrlsByUser(testUserId);
        assertTrue(urls2.size() >= 2);
    }

    // @Test
    // void testCacheInvalidation_OnDelete() {
    // // Create a URL
    // UrlDTO createdUrl = urlService.createUrl(testUrlDTO, testUserId);
    // String shortCode = createdUrl.getShortCode();

    // // Populate cache
    // urlService.getUrlByShortCode(shortCode, testUserId);

    // // Verify cache exists
    // String cacheKey = CacheConstants.urlByShortCodeKey(shortCode);
    // assertTrue(cacheService.exists(cacheKey));

    // // Delete the URL
    // urlService.deleteUrlByShortCode(shortCode, testUserId);

    // // Verify cache is invalidated
    // assertFalse(cacheService.exists(cacheKey));
    // }

    // @Test
    // void testCacheInvalidation_OnUpdate() {
    // // Create a URL
    // UrlDTO createdUrl = urlService.createUrl(testUrlDTO, testUserId);
    // String shortCode = createdUrl.getShortCode();
    // Long urlId = createdUrl.getId();

    // // Populate cache
    // urlService.getUrlByShortCode(shortCode, testUserId);

    // // Verify cache exists
    // String cacheKey = CacheConstants.urlByShortCodeKey(shortCode);
    // assertTrue(cacheService.exists(cacheKey));

    // // Update the URL
    // UrlDTO updateDTO = UrlDTO.builder()
    // .originalUrl("https://example.com/updated")
    // .build();
    // urlService.updateUrlById(urlId, updateDTO, testUserId);

    // // Verify cache is invalidated
    // assertFalse(cacheService.exists(cacheKey));
    // }

    // @Test
    // void testRedirectionOnlyForActiveUrl() {
    // // Create a URL
    // UrlDTO createdUrl = urlService.createUrl(testUrlDTO, testUserId);
    // String shortCode = createdUrl.getShortCode();

    // // Should redirect when ACTIVE
    // String originalUrl = urlService.getAndIncrementClicks(shortCode);
    // assertEquals(testUrlDTO.getOriginalUrl(), originalUrl);

    // // Set to INACTIVE
    // urlService.updateUrlStatusByShortCode(shortCode, testUserId,
    // UrlStatus.INACTIVE);

    // // Should throw when INACTIVE
    // assertThrows(ResourceNotFoundException.class, () ->
    // urlService.getAndIncrementClicks(shortCode));

    // // Set back to ACTIVE
    // urlService.updateUrlStatusByShortCode(shortCode, testUserId,
    // UrlStatus.ACTIVE);

    // // Should redirect again
    // String originalUrl2 = urlService.getAndIncrementClicks(shortCode);
    // assertEquals(testUrlDTO.getOriginalUrl(), originalUrl2);
    // }

    // @Test
    // void testCacheInvalidationOnStatusChange() {
    // // Create a URL and populate cache
    // UrlDTO createdUrl = urlService.createUrl(testUrlDTO, testUserId);
    // String shortCode = createdUrl.getShortCode();
    // urlService.getUrlByShortCode(shortCode, testUserId);
    // String cacheKey = CacheConstants.urlByShortCodeKey(shortCode);
    // assertTrue(cacheService.exists(cacheKey));

    // // Change status
    // urlService.updateUrlStatusByShortCode(shortCode, testUserId,
    // UrlStatus.INACTIVE);

    // // Cache should be invalidated
    // assertFalse(cacheService.exists(cacheKey));
    // }
}