# Environment Variables

This document describes all the environment variables used in the tinyls project.

## Backend (url-shortener)

### Server Configuration

- `SERVER_PORT`: Port number for the Spring Boot application (default: 8000)
- `SERVER_CONTEXT_PATH`: Base path for all API endpoints (default: /api)

### Database Configuration

- `SPRING_DATASOURCE_URL`: PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: Database schema update strategy
- `SPRING_JPA_SHOW_SQL`: Enable/disable SQL query logging

### JWT Configuration

- `JWT_SECRET`: Secret key for JWT token generation
- `JWT_EXPIRATION`: JWT token expiration time in milliseconds

### OAuth2 Configuration

- `GOOGLE_CLIENT_ID`: Google OAuth2 client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth2 client secret
- `GITHUB_CLIENT_ID`: GitHub OAuth2 client ID
- `GITHUB_CLIENT_SECRET`: GitHub OAuth2 client secret

### URL Configuration

- `BASE_URL`: Base URL for the application
- `SHORT_URL_DOMAIN`: Domain for shortened URLs
- `URL_LENGTH`: Length of generated short URLs

### Security Configuration

- `CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed origins
- `RATE_LIMIT_REQUESTS`: Maximum number of requests per duration
- `RATE_LIMIT_DURATION`: Rate limit duration in seconds

### Monitoring Configuration

- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE`: Exposed management endpoints
- `MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED`: Enable Prometheus metrics

### Logging Configuration

- `LOGGING_LEVEL_ROOT`: Root logging level
- `LOGGING_LEVEL_COM_TINYLS`: Application-specific logging level
- `LOGGING_PATTERN`: Log message pattern

### Cache Configuration

- `SPRING_CACHE_TYPE`: Cache provider type
- `SPRING_CACHE_CAFFEINE_SPEC`: Caffeine cache configuration

## Frontend

### API Configuration

- `VITE_API_URL`: Backend API URL
- `VITE_API_TIMEOUT`: API request timeout in milliseconds

### Authentication

- `VITE_GOOGLE_CLIENT_ID`: Google OAuth2 client ID
- `VITE_GITHUB_CLIENT_ID`: GitHub OAuth2 client ID

### Feature Flags

- `VITE_ENABLE_ANALYTICS`: Enable/disable analytics features
- `VITE_ENABLE_QR_CODE`: Enable/disable QR code generation

## Example Configuration

### Backend (.env)

```env
# Server Configuration
SERVER_PORT=8000
SERVER_CONTEXT_PATH=/api

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tinyls
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

# JWT Configuration
JWT_SECRET=your-jwt-secret-key
JWT_EXPIRATION=86400000

# OAuth2 Configuration
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# URL Configuration
BASE_URL=http://localhost:8000
SHORT_URL_DOMAIN=localhost:8000
URL_LENGTH=6

# Security Configuration
CORS_ALLOWED_ORIGINS=http://localhost:5173
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_DURATION=3600

# Monitoring Configuration
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics,prometheus
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true

# Logging Configuration
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_TINYLS=DEBUG
LOGGING_PATTERN=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Cache Configuration
SPRING_CACHE_TYPE=caffeine
SPRING_CACHE_CAFFEINE_SPEC=maximumSize=500,expireAfterWrite=600s
```

### Frontend (.env)

```env
# API Configuration
VITE_API_URL=http://localhost:8000/api
VITE_API_TIMEOUT=5000

# Authentication
VITE_GOOGLE_CLIENT_ID=your-google-client-id
VITE_GITHUB_CLIENT_ID=your-github-client-id

# Feature Flags
VITE_ENABLE_ANALYTICS=true
VITE_ENABLE_QR_CODE=true
```

## Security Notes

1. Never commit actual environment files (`.env`) to version control
2. Use strong, unique secrets for production environments
3. Regularly rotate sensitive credentials
4. Use different credentials for development, staging, and production
5. Consider using a secrets management service for production deployments
