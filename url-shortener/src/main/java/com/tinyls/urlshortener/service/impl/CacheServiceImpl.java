package com.tinyls.urlshortener.service.impl;

import com.tinyls.urlshortener.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of CacheService using Redis.
 * Provides Redis-based caching operations with proper error handling and
 * logging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Cached value for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to cache value for key: {}", key, e);
        }
    }

    @Override
    public void set(String key, Object value, long ttl) {
        set(key, value, ttl, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, Object value, long ttl, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
            log.debug("Cached value for key: {} with TTL: {} {}", key, ttl, timeUnit);
        } catch (Exception e) {
            log.error("Failed to cache value for key: {} with TTL: {} {}", key, ttl, timeUnit, e);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache hit for key: {}", key);
                return Optional.of(type.cast(value));
            } else {
                log.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Failed to retrieve value for key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Failed to check existence for key: {}", key, e);
            return false;
        }
    }

    @Override
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Deleted cache entry for key: {}", key);
            } else {
                log.debug("No cache entry found to delete for key: {}", key);
            }
        } catch (Exception e) {
            log.error("Failed to delete cache entry for key: {}", key, e);
        }
    }

    @Override
    public void delete(String... keys) {
        try {
            Long deletedCount = redisTemplate.delete(java.util.Arrays.asList(keys));
            log.debug("Deleted {} cache entries for keys: {}", deletedCount, java.util.Arrays.toString(keys));
        } catch (Exception e) {
            log.error("Failed to delete cache entries for keys: {}", java.util.Arrays.toString(keys), e);
        }
    }

    @Override
    public boolean expire(String key, long ttl, TimeUnit timeUnit) {
        try {
            Boolean expired = redisTemplate.expire(key, ttl, timeUnit);
            if (Boolean.TRUE.equals(expired)) {
                log.debug("Set expiration for key: {} with TTL: {} {}", key, ttl, timeUnit);
            }
            return Boolean.TRUE.equals(expired);
        } catch (Exception e) {
            log.error("Failed to set expiration for key: {} with TTL: {} {}", key, ttl, timeUnit, e);
            return false;
        }
    }

    @Override
    public long getTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("Failed to get TTL for key: {}", key, e);
            return -1;
        }
    }

    @Override
    public long increment(String key) {
        return increment(key, 1);
    }

    @Override
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            log.debug("Incremented value for key: {} by {}, new value: {}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Failed to increment value for key: {} by {}", key, delta, e);
            return 0;
        }
    }
}