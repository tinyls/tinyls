package com.tinyls.urlshortener.controller;

import com.tinyls.urlshortener.service.FeatureFlagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FeatureFlagController.
 * 
 * These tests verify the REST endpoints for feature flag operations including:
 * - Response format and content
 * - Error handling
 * - Service integration
 * 
 * Note: Security tests are excluded from this unit test to keep it simple.
 * Security should be tested in integration tests with proper Spring Security
 * setup.
 * 
 * @see FeatureFlagController
 */
@ExtendWith(MockitoExtension.class)
class FeatureFlagControllerTest {

    @Mock
    private FeatureFlagService featureFlagService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FeatureFlagController controller = new FeatureFlagController(featureFlagService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    void testCheckFeatureFlag_WhenFeatureEnabled_ReturnsTrue() throws Exception {
        // Given
        when(featureFlagService.isEnabled("redirectCache")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/features/redirectCache")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.feature").value("redirectCache"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testCheckFeatureFlag_WhenFeatureDisabled_ReturnsFalse() throws Exception {
        // Given
        when(featureFlagService.isEnabled("passwordLinks")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/features/passwordLinks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.feature").value("passwordLinks"))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetAllFeatureFlags_ReturnsAllFlags() throws Exception {
        // Given
        when(featureFlagService.isRedirectCacheEnabled()).thenReturn(true);
        when(featureFlagService.isPasswordLinksEnabled()).thenReturn(false);
        when(featureFlagService.isAdvancedAnalyticsEnabled()).thenReturn(true);
        when(featureFlagService.isUrlExpirationEnabled()).thenReturn(false);
        when(featureFlagService.isCustomDomainsEnabled()).thenReturn(true);
        when(featureFlagService.isBulkOperationsEnabled()).thenReturn(false);
        when(featureFlagService.isUrlPreviewEnabled()).thenReturn(true);
        when(featureFlagService.isUserRateLimitingEnabled()).thenReturn(false);
        when(featureFlagService.isUrlCategorizationEnabled()).thenReturn(true);
        when(featureFlagService.isSocialMediaIntegrationEnabled()).thenReturn(false);
        when(featureFlagService.isUrlHealthMonitoringEnabled()).thenReturn(true);
        when(featureFlagService.isAdvancedSecurityEnabled()).thenReturn(false);
        when(featureFlagService.isApiRateLimitingEnabled()).thenReturn(true);
        when(featureFlagService.isDetailedClickTrackingEnabled()).thenReturn(false);
        when(featureFlagService.isUrlBackupRestoreEnabled()).thenReturn(true);
        when(featureFlagService.isCustomShareMessagesEnabled()).thenReturn(false);
        when(featureFlagService.isPerformanceOptimizationEnabled()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/features/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.features.redirectCache").value(true))
                .andExpect(jsonPath("$.features.passwordLinks").value(false))
                .andExpect(jsonPath("$.features.advancedAnalytics").value(true))
                .andExpect(jsonPath("$.features.urlExpiration").value(false))
                .andExpect(jsonPath("$.features.customDomains").value(true))
                .andExpect(jsonPath("$.features.bulkOperations").value(false))
                .andExpect(jsonPath("$.features.urlPreview").value(true))
                .andExpect(jsonPath("$.features.userRateLimiting").value(false))
                .andExpect(jsonPath("$.features.urlCategorization").value(true))
                .andExpect(jsonPath("$.features.socialMediaIntegration").value(false))
                .andExpect(jsonPath("$.features.urlHealthMonitoring").value(true))
                .andExpect(jsonPath("$.features.advancedSecurity").value(false))
                .andExpect(jsonPath("$.features.apiRateLimiting").value(true))
                .andExpect(jsonPath("$.features.detailedClickTracking").value(false))
                .andExpect(jsonPath("$.features.urlBackupRestore").value(true))
                .andExpect(jsonPath("$.features.customShareMessages").value(false))
                .andExpect(jsonPath("$.features.performanceOptimization").value(true));
    }

    @Test
    void testGetCommonFeatureFlags_ReturnsCommonFlags() throws Exception {
        // Given
        when(featureFlagService.isRedirectCacheEnabled()).thenReturn(true);
        when(featureFlagService.isPasswordLinksEnabled()).thenReturn(false);
        when(featureFlagService.isAdvancedAnalyticsEnabled()).thenReturn(true);
        when(featureFlagService.isUrlExpirationEnabled()).thenReturn(false);
        when(featureFlagService.isCustomDomainsEnabled()).thenReturn(true);
        when(featureFlagService.isBulkOperationsEnabled()).thenReturn(false);
        when(featureFlagService.isUrlPreviewEnabled()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/features/common")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.features.redirectCache").value(true))
                .andExpect(jsonPath("$.features.passwordLinks").value(false))
                .andExpect(jsonPath("$.features.advancedAnalytics").value(true))
                .andExpect(jsonPath("$.features.urlExpiration").value(false))
                .andExpect(jsonPath("$.features.customDomains").value(true))
                .andExpect(jsonPath("$.features.bulkOperations").value(false))
                .andExpect(jsonPath("$.features.urlPreview").value(true));
    }
}