package com.tinyls.urlshortener.service.impl;

import com.tinyls.urlshortener.config.CacheConstants;
import com.tinyls.urlshortener.config.FeatureFlags;
import com.tinyls.urlshortener.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the FeatureFlagService.
 * 
 * This service provides feature flag checking capabilities with caching
 * support.
 * Feature flags are cached to avoid repeated configuration lookups and improve
 * performance.
 * 
 * The service integrates with the existing Redis caching infrastructure and
 * follows
 * the same patterns used throughout the application.
 * 
 * @see FeatureFlagService
 * @see FeatureFlags
 * @see CacheConstants
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFlagServiceImpl implements FeatureFlagService {

    private final FeatureFlags featureFlags;

    // In-memory cache for feature flag states to avoid repeated reflection calls
    private final Map<String, Boolean> featureFlagCache = new ConcurrentHashMap<>();

    @Override
    @Cacheable(value = "feature_flags", key = "#featureName", cacheManager = "featureFlagCacheManager")
    public boolean isEnabled(String featureName) {
        try {
            // Use reflection to dynamically check feature flags
            // This allows for runtime feature flag checking without hardcoding
            String methodName = "is" + capitalize(featureName) + "Enabled";
            java.lang.reflect.Method method = this.getClass().getMethod(methodName);
            return (Boolean) method.invoke(this);
        } catch (Exception e) {
            log.warn("Failed to check feature flag: {}", featureName, e);
            return false; // Default to disabled for safety
        }
    }

    @Override
    public boolean isRedirectCacheEnabled() {
        return getCachedFlag("redirectCache", featureFlags::isRedirectCache);
    }

    @Override
    public boolean isPasswordLinksEnabled() {
        return getCachedFlag("passwordLinks", featureFlags::isPasswordLinks);
    }

    @Override
    public boolean isAdvancedAnalyticsEnabled() {
        return getCachedFlag("advancedAnalytics", featureFlags::isAdvancedAnalytics);
    }

    @Override
    public boolean isUrlExpirationEnabled() {
        return getCachedFlag("urlExpiration", featureFlags::isUrlExpiration);
    }

    @Override
    public boolean isCustomDomainsEnabled() {
        return getCachedFlag("customDomains", featureFlags::isCustomDomains);
    }

    @Override
    public boolean isBulkOperationsEnabled() {
        return getCachedFlag("bulkOperations", featureFlags::isBulkOperations);
    }

    @Override
    public boolean isUrlPreviewEnabled() {
        return getCachedFlag("urlPreview", featureFlags::isUrlPreview);
    }

    @Override
    public boolean isUserRateLimitingEnabled() {
        return getCachedFlag("userRateLimiting", featureFlags::isUserRateLimiting);
    }

    @Override
    public boolean isUrlCategorizationEnabled() {
        return getCachedFlag("urlCategorization", featureFlags::isUrlCategorization);
    }

    @Override
    public boolean isSocialMediaIntegrationEnabled() {
        return getCachedFlag("socialMediaIntegration", featureFlags::isSocialMediaIntegration);
    }

    @Override
    public boolean isUrlHealthMonitoringEnabled() {
        return getCachedFlag("urlHealthMonitoring", featureFlags::isUrlHealthMonitoring);
    }

    @Override
    public boolean isAdvancedSecurityEnabled() {
        return getCachedFlag("advancedSecurity", featureFlags::isAdvancedSecurity);
    }

    @Override
    public boolean isApiRateLimitingEnabled() {
        return getCachedFlag("apiRateLimiting", featureFlags::isApiRateLimiting);
    }

    @Override
    public boolean isDetailedClickTrackingEnabled() {
        return getCachedFlag("detailedClickTracking", featureFlags::isDetailedClickTracking);
    }

    @Override
    public boolean isUrlBackupRestoreEnabled() {
        return getCachedFlag("urlBackupRestore", featureFlags::isUrlBackupRestore);
    }

    @Override
    public boolean isCustomShareMessagesEnabled() {
        return getCachedFlag("customShareMessages", featureFlags::isCustomShareMessages);
    }

    @Override
    public boolean isPerformanceOptimizationEnabled() {
        return getCachedFlag("performanceOptimization", featureFlags::isPerformanceOptimization);
    }

    /**
     * Get a cached feature flag value, computing it if not already cached.
     * 
     * @param flagName     the name of the feature flag
     * @param flagSupplier the supplier to compute the flag value
     * @return the cached or computed flag value
     */
    private boolean getCachedFlag(String flagName, java.util.function.Supplier<Boolean> flagSupplier) {
        return featureFlagCache.computeIfAbsent(flagName, k -> {
            boolean value = flagSupplier.get();
            log.debug("Feature flag '{}' is {}", flagName, value ? "enabled" : "disabled");
            return value;
        });
    }

    /**
     * Capitalize the first letter of a string.
     * 
     * @param str the string to capitalize
     * @return the capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Clear the in-memory feature flag cache.
     * This method can be called to refresh feature flag states.
     */
    public void clearCache() {
        featureFlagCache.clear();
        log.debug("Feature flag cache cleared");
    }
}