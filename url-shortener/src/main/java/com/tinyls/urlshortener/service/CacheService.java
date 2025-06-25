package com.tinyls.urlshortener.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service interface for Redis cache operations.
 * Provides methods for storing, retrieving, and managing cached data.
 */
public interface CacheService {

    /**
     * Stores a value in the cache with the specified key.
     *
     * @param key   the cache key
     * @param value the value to cache
     */
    void set(String key, Object value);

    /**
     * Stores a value in the cache with the specified key and TTL.
     *
     * @param key   the cache key
     * @param value the value to cache
     * @param ttl   the time to live in seconds
     */
    void set(String key, Object value, long ttl);

    /**
     * Stores a value in the cache with the specified key and TTL.
     *
     * @param key      the cache key
     * @param value    the value to cache
     * @param ttl      the time to live
     * @param timeUnit the time unit for TTL
     */
    void set(String key, Object value, long ttl, TimeUnit timeUnit);

    /**
     * Retrieves a value from the cache by key.
     *
     * @param key  the cache key
     * @param type the expected type of the cached value
     * @param <T>  the type parameter
     * @return an Optional containing the cached value if found
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Checks if a key exists in the cache.
     *
     * @param key the cache key
     * @return true if the key exists, false otherwise
     */
    boolean exists(String key);

    /**
     * Removes a value from the cache by key.
     *
     * @param key the cache key
     */
    void delete(String key);

    /**
     * Removes multiple values from the cache by keys.
     *
     * @param keys the cache keys to delete
     */
    void delete(String... keys);

    /**
     * Sets the expiration time for a key.
     *
     * @param key      the cache key
     * @param ttl      the time to live
     * @param timeUnit the time unit for TTL
     * @return true if the expiration was set, false otherwise
     */
    boolean expire(String key, long ttl, TimeUnit timeUnit);

    /**
     * Gets the remaining time to live for a key.
     *
     * @param key the cache key
     * @return the remaining TTL in seconds, or -1 if the key doesn't exist or has
     *         no expiration
     */
    long getTtl(String key);

    /**
     * Increments a numeric value in the cache.
     *
     * @param key the cache key
     * @return the new value after incrementing
     */
    long increment(String key);

    /**
     * Increments a numeric value in the cache by the specified amount.
     *
     * @param key   the cache key
     * @param delta the amount to increment by
     * @return the new value after incrementing
     */
    long increment(String key, long delta);
}