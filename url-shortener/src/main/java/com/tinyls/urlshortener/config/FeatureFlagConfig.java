package com.tinyls.urlshortener.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/**
 * Configuration class for feature flags.
 * 
 * This configuration class enables the feature flags configuration properties
 * and sets up caching for feature flag states to improve performance.
 * 
 * The configuration integrates with the existing Redis caching infrastructure
 * and follows the same patterns used throughout the application.
 * 
 * @see FeatureFlags
 * @see CacheConstants
 */
@Configuration
@EnableConfigurationProperties(FeatureFlags.class)
public class FeatureFlagConfig {

    /**
     * Configure cache manager for feature flags.
     * 
     * Feature flags are cached with a TTL of 5 minutes to balance
     * performance with the ability to update flags without restarting
     * the application.
     * 
     * @param connectionFactory Redis connection factory
     * @return configured cache manager
     */
    @Bean
    public CacheManager featureFlagCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // 5 minutes TTL for feature flags
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new org.springframework.data.redis.serializer.StringRedisSerializer()))
                .serializeValuesWith(
                        org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                .fromSerializer(
                                        new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}