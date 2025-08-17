package com.tinyls.urlshortener.controller;

import com.tinyls.urlshortener.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for feature flag operations.
 * 
 * This controller provides endpoints to check feature flag status and get
 * information about available features. It's primarily used for:
 * - Frontend integration to conditionally render features
 * - Monitoring and debugging feature flag states
 * - Administrative purposes to verify feature flag configuration
 * 
 * All endpoints require authentication and appropriate authorization.
 * 
 * @see FeatureFlagService
 */
@Slf4j
@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
@Tag(name = "Feature Flags", description = "Feature flag management endpoints")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    /**
     * Check if a specific feature is enabled.
     * 
     * @param featureName the name of the feature to check
     * @return response indicating if the feature is enabled
     */
    @GetMapping("/{featureName}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check feature flag status", description = "Check if a specific feature flag is enabled")
    public ResponseEntity<Map<String, Object>> checkFeatureFlag(
            @Parameter(description = "Name of the feature to check") @PathVariable String featureName) {

        log.debug("Checking feature flag: {}", featureName);

        boolean isEnabled = featureFlagService.isEnabled(featureName);

        Map<String, Object> response = new HashMap<>();
        response.put("feature", featureName);
        response.put("enabled", isEnabled);
        response.put("timestamp", java.time.Instant.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Get status of all feature flags.
     * 
     * @return response containing all feature flag states
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all feature flags", description = "Get the status of all feature flags (admin only)")
    public ResponseEntity<Map<String, Object>> getAllFeatureFlags() {

        log.debug("Retrieving all feature flags");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", java.time.Instant.now());

        Map<String, Boolean> features = new HashMap<>();
        features.put("redirectCache", featureFlagService.isRedirectCacheEnabled());
        features.put("passwordLinks", featureFlagService.isPasswordLinksEnabled());
        features.put("advancedAnalytics", featureFlagService.isAdvancedAnalyticsEnabled());
        features.put("urlExpiration", featureFlagService.isUrlExpirationEnabled());
        features.put("customDomains", featureFlagService.isCustomDomainsEnabled());
        features.put("bulkOperations", featureFlagService.isBulkOperationsEnabled());
        features.put("urlPreview", featureFlagService.isUrlPreviewEnabled());
        features.put("userRateLimiting", featureFlagService.isUserRateLimitingEnabled());
        features.put("urlCategorization", featureFlagService.isUrlCategorizationEnabled());
        features.put("socialMediaIntegration", featureFlagService.isSocialMediaIntegrationEnabled());
        features.put("urlHealthMonitoring", featureFlagService.isUrlHealthMonitoringEnabled());
        features.put("advancedSecurity", featureFlagService.isAdvancedSecurityEnabled());
        features.put("apiRateLimiting", featureFlagService.isApiRateLimitingEnabled());
        features.put("detailedClickTracking", featureFlagService.isDetailedClickTrackingEnabled());
        features.put("urlBackupRestore", featureFlagService.isUrlBackupRestoreEnabled());
        features.put("customShareMessages", featureFlagService.isCustomShareMessagesEnabled());
        features.put("performanceOptimization", featureFlagService.isPerformanceOptimizationEnabled());

        response.put("features", features);

        return ResponseEntity.ok(response);
    }

    /**
     * Get status of commonly used feature flags for frontend integration.
     * 
     * @return response containing commonly used feature flag states
     */
    @GetMapping("/common")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get common feature flags", description = "Get the status of commonly used feature flags for frontend integration")
    public ResponseEntity<Map<String, Object>> getCommonFeatureFlags() {

        log.debug("Retrieving common feature flags");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", java.time.Instant.now());

        Map<String, Boolean> features = new HashMap<>();
        features.put("redirectCache", featureFlagService.isRedirectCacheEnabled());
        features.put("passwordLinks", featureFlagService.isPasswordLinksEnabled());
        features.put("advancedAnalytics", featureFlagService.isAdvancedAnalyticsEnabled());
        features.put("urlExpiration", featureFlagService.isUrlExpirationEnabled());
        features.put("customDomains", featureFlagService.isCustomDomainsEnabled());
        features.put("bulkOperations", featureFlagService.isBulkOperationsEnabled());
        features.put("urlPreview", featureFlagService.isUrlPreviewEnabled());

        response.put("features", features);

        return ResponseEntity.ok(response);
    }
}