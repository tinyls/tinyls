# Traefik Configuration

This directory contains the Traefik configuration for the URL shortener application. The configuration is split into two main files:

## Configuration Files

### `config/static.yml`
Contains static configuration that rarely changes:
- Global settings
- Entry points (HTTP/HTTPS)
- Certificate resolvers (Let's Encrypt)
- Providers configuration
- Logging settings
- Metrics and API settings

### `config/dynamic.yml`
Contains dynamic configuration that changes based on services:
- HTTP middlewares
- Routers
- Services
- Rate limiting
- Caching headers

## Directory Structure

```
traefik/
├── config/
│   ├── static.yml         # Production static configuration
│   ├── static.local.yml   # Local development static configuration
│   ├── dynamic.yml        # Production dynamic configuration
│   └── dynamic.local.yml  # Local development dynamic configuration
├── acme.json              # Let's Encrypt certificates
└── logs/                  # Traefik logs
```

## Features

1. **HTTPS Redirection**
   - Automatic HTTP to HTTPS redirection
   - Let's Encrypt certificate management

2. **URL Shortening**
   - Direct routing for short URLs (`/r/{code}`)
   - Rate limiting (100 req/s average, 50 burst)
   - Caching headers for better performance

3. **Security**
   - HTTPS enforcement
   - Rate limiting
   - Secure headers

4. **Monitoring**
   - Access logs
   - Prometheus metrics
   - Dashboard (secured)

## Environment Variables

Required environment variables:
- `DOMAIN`: Your domain name
- `ACME_EMAIL`: Email for Let's Encrypt notifications

## Usage

### Production

1. Create required directories:
   ```bash
   mkdir -p traefik/config traefik/logs
   touch traefik/acme.json
   chmod 600 traefik/acme.json
   ```

2. Set environment variables in `.env`:
   ```
   DOMAIN=yourdomain.com
   ACME_EMAIL=your@email.com
   ```

3. Start the services:
   ```bash
   docker-compose up -d
   ```

### Local Development

1. Create required directories:
   ```bash
   mkdir -p traefik/config traefik/logs
   ```

2. Set environment variables in `.env`:
   ```
   DOMAIN=localhost
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=postgres
   POSTGRES_DB=app
   ```

3. Start the services:
   ```bash
   docker-compose -f docker-compose.local.yml up -d
   ```

4. Access the services:
   - API: http://api.localhost
   - Frontend: http://dashboard.localhost
   - Traefik Dashboard: http://localhost:8080
   - Short URLs: http://localhost/r/{code}

5. Add local domain entries to `/etc/hosts`:
   ```
   127.0.0.1 api.localhost
   127.0.0.1 dashboard.localhost
   ```

## Maintenance

- Logs are stored in `traefik/logs/`
- Certificates are stored in `traefik/acme.json`
- Configuration changes require Traefik restart

## Local Testing Tips

1. **View Logs**:
   ```bash
   docker-compose -f docker-compose.local.yml logs -f traefik
   ```

2. **Check Traefik Configuration**:
   - Visit http://localhost:8080 to access the Traefik dashboard
   - Check the "HTTP" section to see all configured routes

3. **Test URL Shortening**:
   ```bash
   # Create a short URL
   curl -X POST http://api.localhost/api/v1/urls/ \
     -H "Content-Type: application/json" \
     -d '{"original_url": "https://example.com"}'

   # Test redirection
   curl -I http://localhost/r/{short_code}
   ```

4. **Debug Issues**:
   - Check Traefik logs for routing issues
   - Verify service health in the Traefik dashboard
   - Ensure all services are in the `traefik-public` network 