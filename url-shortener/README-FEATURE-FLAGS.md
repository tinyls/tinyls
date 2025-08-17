# Feature Flags Implementation

This document describes the feature flags implementation for the TinyLS URL shortener backend.

## Overview

The feature flags system provides a centralized way to manage feature availability across the application. It enables safe, incremental feature delivery and A/B testing capabilities.

## Architecture

### Components

1. **FeatureFlags** - Configuration properties class that holds all feature flag values
2. **FeatureFlagService** - Service interface for checking feature availability
3. **FeatureFlagServiceImpl** - Implementation with caching and error handling
4. **FeatureFlagController** - REST endpoints for feature flag operations
5. **FeatureFlagConfig** - Configuration class for feature flag caching

### Design Principles

- **Safety First**: All flags default to `false` to ensure features are explicitly enabled
- **Performance**: Feature flags are cached to avoid repeated configuration lookups
- **Flexibility**: Support for both static and dynamic feature flag checking
- **Integration**: Seamless integration with existing Redis caching infrastructure

## Available Feature Flags

### Core Features

| Flag | Description | Default |
|------|-------------|---------|
| `redirectCache` | Enhanced redirect caching for better performance | `false` |
| `passwordLinks` | Password-protected URL links | `false` |
| `advancedAnalytics` | Advanced analytics and reporting features | `false` |
| `urlExpiration` | URL expiration functionality | `false` |
| `customDomains` | Custom domain support for shortened URLs | `false` |

### User Experience Features

| Flag | Description | Default |
|------|-------------|---------|
| `bulkOperations` | Bulk URL operations (create, update, delete multiple URLs) | `false` |
| `urlPreview` | URL preview functionality | `false` |
| `urlCategorization` | URL categorization and tagging | `false` |
| `socialMediaIntegration` | Social media integration for sharing | `false` |
| `customShareMessages` | Custom messages when sharing URLs | `false` |

### Performance & Security Features

| Flag | Description | Default |
|------|-------------|---------|
| `userRateLimiting` | Rate limiting per user instead of globally | `false` |
| `advancedSecurity` | Advanced security features | `false` |
| `apiRateLimiting` | Stricter API rate limiting | `false` |
| `performanceOptimization` | Performance optimizations for URL redirection | `false` |

### Monitoring & Analytics Features

| Flag | Description | Default |
|------|-------------|---------|
| `detailedClickTracking` | Detailed click tracking with analytics | `false` |
| `urlHealthMonitoring` | URL health monitoring | `false` |
| `urlBackupRestore` | URL backup and restore functionality | `false` |

## Configuration

### Application Properties

Feature flags are configured in `application.properties`:

```properties
# Feature Flags Configuration
features.redirect-cache=true
features.password-links=false
features.advanced-analytics=false
features.url-expiration=false
features.custom-domains=false
features.bulk-operations=false
features.url-preview=false
features.user-rate-limiting=false
features.url-categorization=false
features.social-media-integration=false
features.url-health-monitoring=false
features.advanced-security=false
features.api-rate-limiting=false
features.detailed-click-tracking=false
features.url-backup-restore=false
features.custom-share-messages=false
features.performance-optimization=false
```

### Environment-Specific Configuration

Enable features for specific environments by setting them to `true`:

```properties
# Development environment
features.redirect-cache=true  # Already implemented
features.password-links=true
features.advanced-analytics=true

# Staging environment
features.redirect-cache=true  # Already implemented
features.password-links=true
features.advanced-analytics=true
features.url-expiration=true
features.custom-domains=true

# Production environment
features.redirect-cache=true  # Already implemented
features.password-links=false
features.advanced-analytics=false
```

## Usage

### In Services

```java
@Service
@RequiredArgsConstructor
public class UrlService {
    
    private final FeatureFlagService featureFlagService;
    
    public UrlDTO createUrl(UrlDTO urlDTO, UUID userId) {
        // Check if password links feature is enabled
        if (featureFlagService.isPasswordLinksEnabled()) {
            // Implement password-protected URL logic
            return createPasswordProtectedUrl(urlDTO, userId);
        }
        
        // Standard URL creation logic
        return createStandardUrl(urlDTO, userId);
    }
    
    public String getAndIncrementClicks(String shortCode) {
        // Check if enhanced caching is enabled
        if (featureFlagService.isRedirectCacheEnabled()) {
            return getCachedRedirect(shortCode);
        }
        
        // Standard redirect logic
        return getStandardRedirect(shortCode);
    }
}
```

### In Controllers

```java
@RestController
@RequiredArgsConstructor
public class UrlController {
    
    private final FeatureFlagService featureFlagService;
    
    @PostMapping("/urls")
    public ResponseEntity<UrlDTO> createUrl(@RequestBody UrlDTO urlDTO) {
        // Check if bulk operations are enabled
        if (featureFlagService.isBulkOperationsEnabled()) {
            return createUrlWithBulkSupport(urlDTO);
        }
        
        return createStandardUrl(urlDTO);
    }
}
```

### Dynamic Feature Checking

```java
// Check any feature flag dynamically
boolean isEnabled = featureFlagService.isEnabled("customDomains");

// This is equivalent to:
boolean isEnabled = featureFlagService.isCustomDomainsEnabled();
```

## REST API

### Check Specific Feature Flag

```http
GET /api/features/{featureName}
Authorization: Bearer <token>
```

**Response:**
```json
{
  "feature": "passwordLinks",
  "enabled": true,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Get All Feature Flags (Admin Only)

```http
GET /api/features/
Authorization: Bearer <token>
```

**Response:**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "features": {
    "redirectCache": true,
    "passwordLinks": false,
    "advancedAnalytics": true,
    "urlExpiration": false,
    "customDomains": true,
    "bulkOperations": false,
    "urlPreview": true,
    "userRateLimiting": false,
    "urlCategorization": true,
    "socialMediaIntegration": false,
    "urlHealthMonitoring": true,
    "advancedSecurity": false,
    "apiRateLimiting": true,
    "detailedClickTracking": false,
    "urlBackupRestore": true,
    "customShareMessages": false,
    "performanceOptimization": true
  }
}
```

### Get Common Feature Flags

```http
GET /api/features/common
Authorization: Bearer <token>
```

**Response:**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "features": {
    "redirectCache": true,
    "passwordLinks": false,
    "advancedAnalytics": true,
    "urlExpiration": false,
    "customDomains": true,
    "bulkOperations": false,
    "urlPreview": true
  }
}
```

## Caching

### Cache Configuration

Feature flags are cached with the following configuration:

- **Cache Name**: `feature_flags`
- **TTL**: 5 minutes (configurable)
- **Serialization**: JSON with String keys
- **Null Values**: Disabled

### Cache Keys

- Individual feature flags: `feature_flags::redirectCache`
- Dynamic feature checks: `feature_flags::{featureName}`

### Cache Invalidation

The in-memory cache can be cleared programmatically:

```java
@Autowired
private FeatureFlagServiceImpl featureFlagService;

// Clear the cache
featureFlagService.clearCache();
```

## Testing

### Unit Tests

```java
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
}
```

### Integration Tests

```java
@WebMvcTest(FeatureFlagController.class)
class FeatureFlagControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FeatureFlagService featureFlagService;
    
    @Test
    @WithMockUser
    void testCheckFeatureFlag_WhenAuthenticated_ReturnsFeatureStatus() throws Exception {
        // Given
        when(featureFlagService.isEnabled("redirectCache")).thenReturn(true);
        
        // When & Then
        mockMvc.perform(get("/api/features/redirectCache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }
}
```

## Best Practices

### 1. Feature Flag Naming

- Use descriptive, camelCase names
- Prefix with feature category if needed
- Keep names consistent across environments

### 2. Default Values

- Always default to `false` for safety
- Enable features explicitly in environment-specific configs
- Document the purpose and impact of each flag

### 3. Performance Considerations

- Cache feature flag checks when possible
- Avoid checking flags in tight loops
- Use bulk flag checking for multiple features

### 4. Testing

- Test both enabled and disabled states
- Mock feature flags in unit tests
- Include feature flag scenarios in integration tests

### 5. Monitoring

- Log feature flag usage for analytics
- Monitor cache hit/miss ratios
- Track feature adoption rates

## Migration Guide

### Adding New Feature Flags

1. **Add to FeatureFlags class:**
   ```java
   @Data
   public class FeatureFlags {
       private boolean newFeature = false;
   }
   ```

2. **Add to FeatureFlagService interface:**
   ```java
   public interface FeatureFlagService {
       boolean isNewFeatureEnabled();
   }
   ```

3. **Implement in FeatureFlagServiceImpl:**
   ```java
   @Override
   public boolean isNewFeatureEnabled() {
       return getCachedFlag("newFeature", featureFlags::isNewFeature);
   }
   ```

4. **Add to application.properties:**
   ```properties
   features.new-feature=false
   ```

5. **Add to tests:**
   ```java
   @Test
   void testIsNewFeatureEnabled_WhenEnabled_ReturnsTrue() {
       when(featureFlags.isNewFeature()).thenReturn(true);
       assertThat(featureFlagService.isNewFeatureEnabled()).isTrue();
   }
   ```

### Removing Feature Flags

1. Remove from all configuration files
2. Remove from FeatureFlags class
3. Remove from FeatureFlagService interface
4. Remove from FeatureFlagServiceImpl
5. Update tests
6. Remove from documentation

## Troubleshooting

### Common Issues

1. **Feature flag not working:**
   - Check configuration in application.properties
   - Verify cache is not stale
   - Check logs for configuration errors

2. **Cache not updating:**
   - Clear the cache manually
   - Check Redis connectivity
   - Verify cache configuration

3. **Performance issues:**
   - Monitor cache hit/miss ratios
   - Check for excessive feature flag calls
   - Consider bulk flag checking

### Debugging

Enable debug logging for feature flags:

```properties
logging.level.com.tinyls.urlshortener.service.FeatureFlagService=DEBUG
```

This will log feature flag checks and cache operations.

## Future Enhancements

1. **Runtime Flag Updates**: Support for updating flags without restart
2. **User-Specific Flags**: Per-user feature flag overrides
3. **Percentage Rollouts**: Gradual feature rollouts
4. **A/B Testing**: Built-in A/B testing capabilities
5. **Flag Analytics**: Usage analytics and reporting
6. **Flag Dependencies**: Support for flag dependencies and conditions 