# Infrastructure Documentation

This directory contains the infrastructure configuration for the Tinyls application. The infrastructure is designed to support multiple environments (staging and production) using Docker and Traefik as the reverse proxy.

## Staging Environment

The staging environment is configured to run all services in Docker containers with Traefik handling routing and SSL termination.

### Prerequisites

1. Docker and Docker Compose installed
2. A domain name with DNS access (e.g., tinyls.com)
3. mkcert for local SSL certificate generation

### Required Environment Variables

Create a `.env.staging` file in the root directory with the following variables:

```env
# Database
POSTGRES_DB=tinyls
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password
POSTGRES_HOST=db
POSTGRES_PORT=5432

# Backend
ENVIRONMENT=staging
SECRET_KEY=your_secure_secret_key
DEBUG=False
ALLOWED_HOSTS=api.staging.tinyls.com
CORS_ALLOWED_ORIGINS=https://staging.tinyls.com

# Frontend
VITE_API_URL=https://api.staging.tinyls.com
NODE_ENV=production

# Grafana
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=your_secure_password
```

### SSL Certificate Setup

1. Install mkcert:
   ```bash
   # macOS
   brew install mkcert
   mkcert -install
   ```

2. Create certificates for staging domains:
   ```bash
   mkdir -p certs
   cd certs
   mkcert "*.staging.tinyls.com" staging.tinyls.com
   ```

3. The certificates will be mounted into the Traefik container for SSL termination.

### Network Setup

1. Create the Traefik network:
   ```bash
   docker network create traefik-public
   ```

### Service Architecture

The staging environment consists of the following services:

1. **Traefik (Proxy)**
   - Handles routing and SSL termination
   - Accessible at `traefik.staging.tinyls.com`
   - Metrics available at `traefik.staging.tinyls.com/metrics`

2. **Backend (FastAPI)**
   - API service running on port 8000
   - Accessible at `api.staging.tinyls.com`
   - Metrics available at `api.staging.tinyls.com/metrics`

3. **Frontend (Vue.js)**
   - Web interface running on port 80
   - Accessible at `staging.tinyls.com`

4. **Database (PostgreSQL)**
   - Running on port 5432
   - Accessible via Adminer at `adminer.staging.tinyls.com`

5. **Monitoring Stack**
   - Prometheus: `prometheus.staging.tinyls.com`
   - Grafana: `grafana.staging.tinyls.com`

### Starting the Environment

1. Ensure all prerequisites are met
2. Create the required environment files
3. Generate SSL certificates
4. Start the services:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.staging.yml up -d
   ```

### API Testing

The API collection is available in the `bruno` directory. To use it:

1. Install Bruno from [brunoapp.com](https://www.brunoapp.com)
2. Open the collection in Bruno
3. Set the environment variables in Bruno to match your staging environment
4. Run the tests

### Monitoring

1. Access Grafana at `grafana.staging.tinyls.com`
   - Default credentials: admin/admin
   - Change the default password on first login

2. Access Prometheus at `prometheus.staging.tinyls.com`
   - View raw metrics and targets

3. Access Traefik dashboard at `traefik.staging.tinyls.com`
   - View routing configuration and service health

### Troubleshooting

1. Check container logs:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.staging.yml logs -f [service_name]
   ```

2. Verify Traefik configuration:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.staging.yml exec proxy traefik version
   ```

3. Check SSL certificate validity:
   ```bash
   openssl x509 -in certs/*.staging.tinyls.com+1.pem -text -noout
   ```

### Security Considerations

1. All services are configured to use HTTPS
2. HTTP traffic is automatically redirected to HTTPS
3. CORS is configured to only allow specific origins
4. Database is not exposed to the public network
5. Grafana and Prometheus are protected by HTTPS
6. Traefik dashboard is protected by HTTPS

### Backup and Recovery

1. Database backups:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.staging.yml exec db pg_dump -U postgres tinyls > backup.sql
   ```

2. Restore database:
   ```bash
   cat backup.sql | docker compose -f docker-compose.yml -f docker-compose.staging.yml exec -T db psql -U postgres tinyls
   ```

### Maintenance

1. Update containers:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.staging.yml pull
   docker compose -f docker-compose.yml -f docker-compose.staging.yml up -d
   ```

2. Clean up unused resources:
   ```bash
   docker system prune -f
   ```

### Directory Structure

```
infra/
├── traefik/
│   ├── config/
│   │   ├── static.staging.yml    # Traefik static configuration
│   │   └── dynamic.staging.yml   # Traefik dynamic configuration
├── prometheus/
│   └── staging.yml               # Prometheus configuration
└── grafana/
    └── provisioning/            # Grafana dashboards and datasources
``` 