package com.tinyls.urlshortener.service;

/**
 * Service interface for managing feature flags.
 * 
 * This service provides a centralized way to check feature availability
 * throughout the application. It abstracts the feature flag implementation
 * and provides a clean API for feature gating.
 * 
 * Feature flags enable:
 * - Safe, incremental feature delivery
 * - A/B testing capabilities
 * - Environment-specific feature control
 * - Rollback capabilities for problematic features
 * 
 * @see FeatureFlagServiceImpl
 * @see FeatureFlags
 */
public interface FeatureFlagService {

    /**
     * Check if a specific feature is enabled.
     * 
     * @param featureName the name of the feature to check
     * @return true if the feature is enabled, false otherwise
     */
    boolean isEnabled(String featureName);

    /**
     * Check if enhanced redirect caching is enabled.
     * 
     * @return true if redirect caching is enabled
     */
    boolean isRedirectCacheEnabled();

    /**
     * Check if password-protected URL links are enabled.
     * 
     * @return true if password links are enabled
     */
    boolean isPasswordLinksEnabled();

    /**
     * Check if advanced analytics are enabled.
     * 
     * @return true if advanced analytics are enabled
     */
    boolean isAdvancedAnalyticsEnabled();

    /**
     * Check if URL expiration functionality is enabled.
     * 
     * @return true if URL expiration is enabled
     */
    boolean isUrlExpirationEnabled();

    /**
     * Check if custom domain support is enabled.
     * 
     * @return true if custom domains are enabled
     */
    boolean isCustomDomainsEnabled();

    /**
     * Check if bulk operations are enabled.
     * 
     * @return true if bulk operations are enabled
     */
    boolean isBulkOperationsEnabled();

    /**
     * Check if URL preview functionality is enabled.
     * 
     * @return true if URL preview is enabled
     */
    boolean isUrlPreviewEnabled();

    /**
     * Check if user-specific rate limiting is enabled.
     * 
     * @return true if user rate limiting is enabled
     */
    boolean isUserRateLimitingEnabled();

    /**
     * Check if URL categorization is enabled.
     * 
     * @return true if URL categorization is enabled
     */
    boolean isUrlCategorizationEnabled();

    /**
     * Check if social media integration is enabled.
     * 
     * @return true if social media integration is enabled
     */
    boolean isSocialMediaIntegrationEnabled();

    /**
     * Check if URL health monitoring is enabled.
     * 
     * @return true if URL health monitoring is enabled
     */
    boolean isUrlHealthMonitoringEnabled();

    /**
     * Check if advanced security features are enabled.
     * 
     * @return true if advanced security is enabled
     */
    boolean isAdvancedSecurityEnabled();

    /**
     * Check if API rate limiting is enabled.
     * 
     * @return true if API rate limiting is enabled
     */
    boolean isApiRateLimitingEnabled();

    /**
     * Check if detailed click tracking is enabled.
     * 
     * @return true if detailed click tracking is enabled
     */
    boolean isDetailedClickTrackingEnabled();

    /**
     * Check if URL backup and restore functionality is enabled.
     * 
     * @return true if URL backup/restore is enabled
     */
    boolean isUrlBackupRestoreEnabled();

    /**
     * Check if custom share messages are enabled.
     * 
     * @return true if custom share messages are enabled
     */
    boolean isCustomShareMessagesEnabled();

    /**
     * Check if performance optimization features are enabled.
     * 
     * @return true if performance optimization is enabled
     */
    boolean isPerformanceOptimizationEnabled();
}