# Redis Caching Implementation

This document describes the Redis caching implementation for the TinyLS URL shortener backend.

## Overview

The application uses Redis for caching to improve performance and reduce database load. Caching is implemented at the service layer with proper cache invalidation strategies.

## Architecture

### Components

1. **RedisConfig** - Configuration for Redis connection and serialization
2. **CacheService** - Interface for Redis operations
3. **CacheServiceImpl** - Implementation of cache operations with error handling
4. **CacheConstants** - Constants for cache keys and TTL values
5. **UrlServiceImpl** - Service layer with integrated caching

### Cache Strategy

- **Cache-Aside Pattern**: Data is loaded into cache on demand
- **Write-Through**: Cache is updated immediately when data is modified
- **Cache Invalidation**: Relevant caches are invalidated on data changes

## Cache Keys

### URL Caching
- `url:shortcode:{shortCode}` - URL by short code (TTL: 1 hour)
- `url:id:{id}` - URL by ID (TTL: 1 hour)
- `url:user:{userId}` - User's URLs list (TTL: 1 hour)

### Click Tracking
- `clicks:{shortCode}` - Click count for URL (TTL: 24 hours)

### User Caching
- `user:id:{userId}` - User by ID (TTL: 30 minutes)
- `user:email:{email}` - User by email (TTL: 30 minutes)

## Implementation Details

### Cache Hit/Miss Flow

1. **Cache Hit**: Data is retrieved from Redis, reducing database load
2. **Cache Miss**: Data is fetched from database and cached for future requests
3. **Cache Invalidation**: Relevant caches are cleared when data is modified

### Error Handling

- Redis connection failures are logged but don't break application functionality
- Cache operations are wrapped in try-catch blocks
- Application gracefully falls back to database when cache is unavailable

### Performance Benefits

- **URL Redirection**: Cached URLs reduce database queries for high-traffic short links
- **User Dashboard**: User's URL list is cached to improve dashboard performance
- **Click Tracking**: Click counts are cached to reduce database writes

## Configuration

### Redis Settings (application.properties)

```properties
# Redis Configuration
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.data.redis.database=${REDIS_DATABASE:0}
spring.data.redis.timeout=2000ms

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=3600000
spring.cache.redis.cache-null-values=false
spring.cache.redis.use-key-prefix=true
spring.cache.redis.key-prefix=tinyls:
```

### Docker Configuration

Redis is configured in `docker-compose.yml`:

```yaml
redis:
  image: redis:7-alpine
  restart: always
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    retries: 5
    start_period: 10s
    timeout: 5s
  volumes:
    - redis-data:/data
  environment:
    - REDIS_PASSWORD=${REDIS_PASSWORD:}
```

## Testing

Integration tests are available in `UrlServiceCacheTest.java` that verify:

- Cache hit/miss behavior
- Cache invalidation on data changes
- Click tracking in cache
- User URL list caching

Run tests with:
```bash
mvn test -Dtest=UrlServiceCacheTest
```

## Monitoring

### Health Checks

Redis health is monitored via Spring Boot Actuator health checks.

### Logging

Cache operations are logged at DEBUG level:
- Cache hits/misses
- Cache invalidation events
- Redis connection errors

### Metrics

Consider adding Redis metrics using:
- Spring Boot Actuator metrics
- Redis INFO command monitoring
- Application performance monitoring

## Best Practices

1. **TTL Management**: Use appropriate TTL values based on data volatility
2. **Cache Invalidation**: Always invalidate relevant caches on data changes
3. **Error Handling**: Graceful degradation when Redis is unavailable
4. **Key Naming**: Use consistent, descriptive cache key patterns
5. **Memory Management**: Monitor Redis memory usage and set appropriate limits

## Future Enhancements

1. **Cache Warming**: Pre-populate cache with frequently accessed data
2. **Distributed Caching**: Support for Redis cluster for high availability
3. **Cache Analytics**: Detailed cache hit/miss ratio monitoring
4. **Adaptive TTL**: Dynamic TTL based on access patterns
5. **Cache Compression**: Compress cached data for memory efficiency 