package com.tinyls.urlshortener.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for caching and session management.
 * Provides Redis template configuration and cache manager setup.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configures Redis template with proper serialization.
     * Uses String keys and JSON values for better readability and debugging.
     *
     * @param connectionFactory Redis connection factory
     * @return Configured Redis template
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();

        // register JavaTimeModule so dates still work
//        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
//
//        jsonSerializer.setObjectMapper(mapper);

        // Key serializer
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value serializer
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configures Redis cache manager with proper serialization and TTL settings.
     *
     * @param connectionFactory Redis connection factory
     * @return Configured cache manager
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer jsonSer = createJsonSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Default TTL of 1 hour
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                           .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                             .fromSerializer(jsonSer))
                .disableCachingNullValues();

        JdkSerializationRedisSerializer jdkSer = new JdkSerializationRedisSerializer();
        RedisCacheConfiguration listConfig = RedisCacheConfiguration.defaultCacheConfig()
                                                     .entryTtl(Duration.ofHours(1))
                                                     .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                                                     .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jdkSer))
                                                     .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put(CacheConstants.USER_URL_LIST_CACHE, listConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(configs)
                .transactionAware()
                .build();
    }

    /**
     * Creates a JSON serializer with proper type information for deserialization.
     *
     * @return Configured JSON serializer
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}