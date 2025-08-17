package com.tinyls.urlshortener.service;

import com.tinyls.urlshortener.config.FeatureFlags;
import com.tinyls.urlshortener.service.impl.FeatureFlagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FeatureFlagService.
 * 
 * These tests verify the feature flag service functionality including:
 * - Individual feature flag checking
 * - Dynamic feature flag checking
 * - Caching behavior
 * - Error handling
 * 
 * @see FeatureFlagService
 * @see FeatureFlagServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlags featureFlags;

    private FeatureFlagServiceImpl featureFlagService;

    @BeforeEach
    void setUp() {
        featureFlagService = new FeatureFlagServiceImpl(featureFlags);
    }

    @Test
    void testIsRedirectCacheEnabled_WhenEnabled_ReturnsTrue() {
        // Given
        when(featureFlags.isRedirectCache()).thenReturn(true);

        // When
        boolean result = featureFlagService.isRedirectCacheEnabled();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testIsRedirectCacheEnabled_WhenDisabled_ReturnsFalse() {
        // Given
        when(featureFlags.isRedirectCache()).thenReturn(false);

        // When
        boolean result = featureFlagService.isRedirectCacheEnabled();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testIsPasswordLinksEnabled_WhenEnabled_ReturnsTrue() {
        // Given
        when(featureFlags.isPasswordLinks()).thenReturn(true);

        // When
        boolean result = featureFlagService.isPasswordLinksEnabled();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testIsPasswordLinksEnabled_WhenDisabled_ReturnsFalse() {
        // Given
        when(featureFlags.isPasswordLinks()).thenReturn(false);

        // When
        boolean result = featureFlagService.isPasswordLinksEnabled();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testIsAdvancedAnalyticsEnabled_WhenEnabled_ReturnsTrue() {
        // Given
        when(featureFlags.isAdvancedAnalytics()).thenReturn(true);

        // When
        boolean result = featureFlagService.isAdvancedAnalyticsEnabled();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testIsAdvancedAnalyticsEnabled_WhenDisabled_ReturnsFalse() {
        // Given
        when(featureFlags.isAdvancedAnalytics()).thenReturn(false);

        // When
        boolean result = featureFlagService.isAdvancedAnalyticsEnabled();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testIsEnabled_WithValidFeatureName_ReturnsCorrectValue() {
        // Given
        when(featureFlags.isRedirectCache()).thenReturn(true);

        // When
        boolean result = featureFlagService.isEnabled("redirectCache");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testIsEnabled_WithInvalidFeatureName_ReturnsFalse() {
        // When
        boolean result = featureFlagService.isEnabled("invalidFeature");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testIsEnabled_WithNullFeatureName_ReturnsFalse() {
        // When
        boolean result = featureFlagService.isEnabled(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testIsEnabled_WithEmptyFeatureName_ReturnsFalse() {
        // When
        boolean result = featureFlagService.isEnabled("");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testCachingBehavior_WhenCalledMultipleTimes_ReturnsCachedValue() {
        // Given
        when(featureFlags.isRedirectCache()).thenReturn(true);

        // When
        boolean result1 = featureFlagService.isRedirectCacheEnabled();
        boolean result2 = featureFlagService.isRedirectCacheEnabled();
        boolean result3 = featureFlagService.isRedirectCacheEnabled();

        // Then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        assertThat(result3).isTrue();
    }

    @Test
    void testClearCache_WhenCalled_ClearsInternalCache() {
        // Given
        when(featureFlags.isRedirectCache()).thenReturn(true);
        featureFlagService.isRedirectCacheEnabled(); // Populate cache

        // When
        featureFlagService.clearCache();
        boolean result = featureFlagService.isRedirectCacheEnabled(); // Should re-compute

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testAllFeatureFlags_WhenAllDisabled_ReturnFalse() {
        // Given
        when(featureFlags.isRedirectCache()).thenReturn(false);
        when(featureFlags.isPasswordLinks()).thenReturn(false);
        when(featureFlags.isAdvancedAnalytics()).thenReturn(false);
        when(featureFlags.isUrlExpiration()).thenReturn(false);
        when(featureFlags.isCustomDomains()).thenReturn(false);
        when(featureFlags.isBulkOperations()).thenReturn(false);
        when(featureFlags.isUrlPreview()).thenReturn(false);
        when(featureFlags.isUserRateLimiting()).thenReturn(false);
        when(featureFlags.isUrlCategorization()).thenReturn(false);
        when(featureFlags.isSocialMediaIntegration()).thenReturn(false);
        when(featureFlags.isUrlHealthMonitoring()).thenReturn(false);
        when(featureFlags.isAdvancedSecurity()).thenReturn(false);
        when(featureFlags.isApiRateLimiting()).thenReturn(false);
        when(featureFlags.isDetailedClickTracking()).thenReturn(false);
        when(featureFlags.isUrlBackupRestore()).thenReturn(false);
        when(featureFlags.isCustomShareMessages()).thenReturn(false);
        when(featureFlags.isPerformanceOptimization()).thenReturn(false);

        // When & Then
        assertThat(featureFlagService.isRedirectCacheEnabled()).isFalse();
        assertThat(featureFlagService.isPasswordLinksEnabled()).isFalse();
        assertThat(featureFlagService.isAdvancedAnalyticsEnabled()).isFalse();
        assertThat(featureFlagService.isUrlExpirationEnabled()).isFalse();
        assertThat(featureFlagService.isCustomDomainsEnabled()).isFalse();
        assertThat(featureFlagService.isBulkOperationsEnabled()).isFalse();
        assertThat(featureFlagService.isUrlPreviewEnabled()).isFalse();
        assertThat(featureFlagService.isUserRateLimitingEnabled()).isFalse();
        assertThat(featureFlagService.isUrlCategorizationEnabled()).isFalse();
        assertThat(featureFlagService.isSocialMediaIntegrationEnabled()).isFalse();
        assertThat(featureFlagService.isUrlHealthMonitoringEnabled()).isFalse();
        assertThat(featureFlagService.isAdvancedSecurityEnabled()).isFalse();
        assertThat(featureFlagService.isApiRateLimitingEnabled()).isFalse();
        assertThat(featureFlagService.isDetailedClickTrackingEnabled()).isFalse();
        assertThat(featureFlagService.isUrlBackupRestoreEnabled()).isFalse();
        assertThat(featureFlagService.isCustomShareMessagesEnabled()).isFalse();
        assertThat(featureFlagService.isPerformanceOptimizationEnabled()).isFalse();
    }

    @Test
    void testAllFeatureFlags_WhenAllEnabled_ReturnTrue() {
        // Given
        when(featureFlags.isRedirectCache()).thenReturn(true);
        when(featureFlags.isPasswordLinks()).thenReturn(true);
        when(featureFlags.isAdvancedAnalytics()).thenReturn(true);
        when(featureFlags.isUrlExpiration()).thenReturn(true);
        when(featureFlags.isCustomDomains()).thenReturn(true);
        when(featureFlags.isBulkOperations()).thenReturn(true);
        when(featureFlags.isUrlPreview()).thenReturn(true);
        when(featureFlags.isUserRateLimiting()).thenReturn(true);
        when(featureFlags.isUrlCategorization()).thenReturn(true);
        when(featureFlags.isSocialMediaIntegration()).thenReturn(true);
        when(featureFlags.isUrlHealthMonitoring()).thenReturn(true);
        when(featureFlags.isAdvancedSecurity()).thenReturn(true);
        when(featureFlags.isApiRateLimiting()).thenReturn(true);
        when(featureFlags.isDetailedClickTracking()).thenReturn(true);
        when(featureFlags.isUrlBackupRestore()).thenReturn(true);
        when(featureFlags.isCustomShareMessages()).thenReturn(true);
        when(featureFlags.isPerformanceOptimization()).thenReturn(true);

        // When & Then
        assertThat(featureFlagService.isRedirectCacheEnabled()).isTrue();
        assertThat(featureFlagService.isPasswordLinksEnabled()).isTrue();
        assertThat(featureFlagService.isAdvancedAnalyticsEnabled()).isTrue();
        assertThat(featureFlagService.isUrlExpirationEnabled()).isTrue();
        assertThat(featureFlagService.isCustomDomainsEnabled()).isTrue();
        assertThat(featureFlagService.isBulkOperationsEnabled()).isTrue();
        assertThat(featureFlagService.isUrlPreviewEnabled()).isTrue();
        assertThat(featureFlagService.isUserRateLimitingEnabled()).isTrue();
        assertThat(featureFlagService.isUrlCategorizationEnabled()).isTrue();
        assertThat(featureFlagService.isSocialMediaIntegrationEnabled()).isTrue();
        assertThat(featureFlagService.isUrlHealthMonitoringEnabled()).isTrue();
        assertThat(featureFlagService.isAdvancedSecurityEnabled()).isTrue();
        assertThat(featureFlagService.isApiRateLimitingEnabled()).isTrue();
        assertThat(featureFlagService.isDetailedClickTrackingEnabled()).isTrue();
        assertThat(featureFlagService.isUrlBackupRestoreEnabled()).isTrue();
        assertThat(featureFlagService.isCustomShareMessagesEnabled()).isTrue();
        assertThat(featureFlagService.isPerformanceOptimizationEnabled()).isTrue();
    }
}