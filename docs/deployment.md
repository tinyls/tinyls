# Deployment Guide

This guide provides instructions for deploying the TinyLS application to different environments.

## Prerequisites

- Docker and Docker Compose
- Domain name (for production)
- SSL certificates
- Cloud provider account (optional)
- CI/CD pipeline (optional)

## Environment Setup

### 1. Production Environment Variables

Create `.env` files for each environment:

```bash
# Production
cp url-shortener/.env.example url-shortener/.env.prod
cp frontend/.env.example frontend/.env.prod

# Staging
cp url-shortener/.env.example url-shortener/.env.staging
cp frontend/.env.example frontend/.env.staging
```

Update the environment variables with production values (see [Environment Variables](environment-variables.md)).

### 2. SSL Certificates

1. Generate SSL certificates using Let's Encrypt:
   ```bash
   certbot certonly --standalone -d api.tinyls.com -d www.tinyls.com
   ```

2. Configure Traefik to use the certificates:
   ```yaml
   # traefik/traefik.yml
   tls:
     certificates:
       - certFile: /etc/letsencrypt/live/api.tinyls.com/fullchain.pem
         keyFile: /etc/letsencrypt/live/api.tinyls.com/privkey.pem
   ```

## Deployment Methods

### 1. Docker Compose Deployment

#### Production

```bash
# Build and start services
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# View logs
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs -f
```

#### Staging

```bash
docker compose -f docker-compose.yml -f docker-compose.staging.yml up -d
```

### 2. Kubernetes Deployment

1. Create namespace:
   ```bash
   kubectl create namespace tinyls
   ```

2. Apply configurations:
   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/secrets.yaml
   kubectl apply -f k8s/configmaps.yaml
   kubectl apply -f k8s/deployments.yaml
   kubectl apply -f k8s/services.yaml
   kubectl apply -f k8s/ingress.yaml
   ```

3. Verify deployment:
   ```bash
   kubectl get all -n tinyls
   ```

## Infrastructure Components

### 1. Database

#### PostgreSQL Setup

1. Create database:
   ```sql
   CREATE DATABASE tinyls;
   CREATE USER tinyls WITH ENCRYPTED PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE tinyls TO tinyls;
   ```

2. Run migrations:
   ```bash
   cd url-shortener
   ./mvnw flyway:migrate
   ```

### 2. Reverse Proxy (Traefik)

1. Configure Traefik:
   ```yaml
   # traefik/traefik.yml
   entryPoints:
     web:
       address: ":80"
     websecure:
       address: ":443"
   
   providers:
     docker:
       endpoint: "unix:///var/run/docker.sock"
       exposedByDefault: false
   
   certificatesResolvers:
     letsencrypt:
       acme:
         email: "admin@tinyls.com"
         storage: "/etc/traefik/acme.json"
         httpChallenge:
           entryPoint: web
   ```

2. Start Traefik:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.traefik.yml up -d
   ```

### 3. Monitoring Stack

1. Configure Prometheus:
   ```yaml
   # prometheus/prometheus.yml
   global:
     scrape_interval: 15s
   
   scrape_configs:
     - job_name: 'tinyls'
       static_configs:
         - targets: ['backend:8000']
   ```

2. Configure Grafana:
   - Import dashboards
   - Configure data sources
   - Set up alerts

3. Start monitoring stack:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d
   ```

## Deployment Checklist

### Pre-deployment

- [ ] Update environment variables
- [ ] Generate SSL certificates
- [ ] Configure domain DNS
- [ ] Set up monitoring
- [ ] Backup database
- [ ] Test in staging environment

### Deployment

- [ ] Deploy database migrations
- [ ] Deploy backend services
- [ ] Deploy frontend
- [ ] Configure reverse proxy
- [ ] Set up monitoring
- [ ] Verify SSL certificates

### Post-deployment

- [ ] Verify all services are running
- [ ] Check application logs
- [ ] Monitor error rates
- [ ] Test all features
- [ ] Verify monitoring
- [ ] Update documentation

## Scaling

### Horizontal Scaling

1. Scale backend services:
   ```bash
   docker compose up -d --scale backend=3
   ```

2. Scale frontend:
   ```bash
   docker compose up -d --scale frontend=2
   ```

### Database Scaling

1. Set up replication:
   ```bash
   # Primary
   docker run -d --name postgres-primary \
     -e POSTGRES_DB=tinyls \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -e POSTGRES_REPLICATION_MODE=master \
     -e POSTGRES_REPLICATION_USER=repl \
     -e POSTGRES_REPLICATION_PASSWORD=replpass \
     postgres:15

   # Replica
   docker run -d --name postgres-replica \
     -e POSTGRES_DB=tinyls \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -e POSTGRES_REPLICATION_MODE=slave \
     -e POSTGRES_REPLICATION_USER=repl \
     -e POSTGRES_REPLICATION_PASSWORD=replpass \
     -e POSTGRES_MASTER_HOST=postgres-primary \
     postgres:15
   ```

## Backup and Recovery

### Database Backup

1. Automated backup:
   ```bash
   # Backup script
   #!/bin/bash
   TIMESTAMP=$(date +%Y%m%d_%H%M%S)
   docker exec tinyls-postgres pg_dump -U postgres tinyls > backup_$TIMESTAMP.sql
   ```

2. Restore from backup:
   ```bash
   docker exec -i tinyls-postgres psql -U postgres tinyls < backup.sql
   ```

### Application Backup

1. Backup configuration:
   ```bash
   tar -czf config_backup.tar.gz .env* traefik/ prometheus/ grafana/
   ```

2. Backup SSL certificates:
   ```bash
   tar -czf ssl_backup.tar.gz /etc/letsencrypt/live/
   ```

## Monitoring and Maintenance

### Health Checks

1. Configure health endpoints:
   ```yaml
   # docker-compose.yml
   services:
     backend:
       healthcheck:
         test: ["CMD", "curl", "-f", "http://localhost:8000/actuator/health"]
         interval: 30s
         timeout: 10s
         retries: 3
   ```

2. Set up monitoring alerts:
   ```yaml
   # grafana/alerting.yml
   groups:
     - name: tinyls
       rules:
         - alert: HighErrorRate
           expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
           for: 5m
   ```

### Regular Maintenance

1. Update dependencies:
   ```bash
   # Backend
   cd url-shortener
   ./mvnw versions:display-dependency-updates

   # Frontend
   cd frontend
   npm audit
   npm update
   ```

2. Rotate logs:
   ```bash
   # Configure log rotation
   /var/log/tinyls/*.log {
     daily
     rotate 7
     compress
     delaycompress
     missingok
     notifempty
   }
   ```

## Troubleshooting

### Common Issues

1. Database connection issues:
   - Check PostgreSQL logs
   - Verify connection settings
   - Check network connectivity

2. SSL certificate issues:
   - Verify certificate validity
   - Check certificate paths
   - Renew certificates if needed

3. Performance issues:
   - Check application logs
   - Monitor resource usage
   - Review database queries

### Recovery Procedures

1. Service recovery:
   ```bash
   # Restart services
   docker compose restart

   # Check logs
   docker compose logs -f
   ```

2. Database recovery:
   ```bash
   # Restore from backup
   docker exec -i tinyls-postgres psql -U postgres tinyls < backup.sql
   ```

## Security Considerations

1. Network security:
   - Use internal Docker network
   - Configure firewall rules
   - Enable HTTPS only

2. Application security:
   - Regular security updates
   - Input validation
   - Rate limiting
   - CORS configuration

3. Data security:
   - Encrypt sensitive data
   - Regular backups
   - Access control

## Additional Resources

- [Docker Documentation](https://docs.docker.com)
- [Traefik Documentation](https://doc.traefik.io/traefik/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/) 