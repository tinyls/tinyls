package com.tinyls.urlshortener.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for feature flags.
 * 
 * This class provides a centralized way to manage feature flags across the
 * application.
 * Feature flags allow for safe, incremental feature delivery and A/B testing.
 * 
 * All flags default to false for safety, ensuring features are explicitly
 * enabled.
 * 
 * @see FeatureFlagService
 */
@Data
@Component
@ConfigurationProperties(prefix = "features")
public class FeatureFlags {

    /**
     * Enable enhanced redirect caching for better performance.
     * When enabled, redirects are cached more aggressively in Redis.
     */
    private boolean redirectCache = false;

    /**
     * Enable password-protected URL links.
     * When enabled, users can create URLs that require a password to access.
     */
    private boolean passwordLinks = false;

    /**
     * Enable advanced analytics and reporting features.
     * When enabled, provides detailed click analytics, geographic data, and user
     * behavior insights.
     */
    private boolean advancedAnalytics = false;

    /**
     * Enable URL expiration functionality.
     * When enabled, users can set expiration dates for their shortened URLs.
     */
    private boolean urlExpiration = false;

    /**
     * Enable custom domain support for shortened URLs.
     * When enabled, users can use their own domains for shortened URLs.
     */
    private boolean customDomains = false;

    /**
     * Enable bulk URL operations.
     * When enabled, users can create, update, or delete multiple URLs at once.
     */
    private boolean bulkOperations = false;

    /**
     * Enable URL preview functionality.
     * When enabled, users can preview the destination page before creating a short
     * URL.
     */
    private boolean urlPreview = false;

    /**
     * Enable rate limiting per user.
     * When enabled, rate limiting is applied per authenticated user instead of
     * globally.
     */
    private boolean userRateLimiting = false;

    /**
     * Enable URL categorization and tagging.
     * When enabled, users can categorize and tag their shortened URLs.
     */
    private boolean urlCategorization = false;

    /**
     * Enable social media integration.
     * When enabled, users can share shortened URLs directly to social media
     * platforms.
     */
    private boolean socialMediaIntegration = false;

    /**
     * Enable URL health monitoring.
     * When enabled, the system periodically checks if destination URLs are still
     * accessible.
     */
    private boolean urlHealthMonitoring = false;

    /**
     * Enable advanced security features.
     * When enabled, includes additional security measures like suspicious URL
     * detection.
     */
    private boolean advancedSecurity = false;

    /**
     * Enable API rate limiting.
     * When enabled, API endpoints have stricter rate limiting for unauthenticated
     * users.
     */
    private boolean apiRateLimiting = false;

    /**
     * Enable URL click tracking with detailed analytics.
     * When enabled, tracks detailed click information including referrer, user
     * agent, and IP.
     */
    private boolean detailedClickTracking = false;

    /**
     * Enable URL backup and restore functionality.
     * When enabled, users can backup and restore their URL collections.
     */
    private boolean urlBackupRestore = false;

    /**
     * Enable URL sharing with custom messages.
     * When enabled, users can add custom messages when sharing URLs.
     */
    private boolean customShareMessages = false;

    /**
     * Enable URL performance optimization.
     * When enabled, applies additional performance optimizations for URL
     * redirection.
     */
    private boolean performanceOptimization = false;
}