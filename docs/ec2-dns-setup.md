# EC2 DNS Setup Guide for Cloudflare

This guide explains how to configure DNS records in Cloudflare to point your subdomains to your EC2 instance and work properly with Traefik using DNS challenge for Let's Encrypt.

## Prerequisites

1. Domain registered with Cloudflare (`tinyls.com`)
2. EC2 instance with public IP address
3. Application deployed and running on EC2

## Step 1: Create Cloudflare API Token

1. Log in to your Cloudflare dashboard
2. Go to **My Profile** → **API Tokens**
3. Click **Create Token**
4. Use the **Custom token** template
5. Configure the token with these permissions:
   - **Zone** → **Zone** → **Read**
   - **Zone** → **DNS** → **Edit**
6. Set **Zone Resources** to **Include** → **Specific zone** → **tinyls.com**
7. Click **Continue to summary** and then **Create Token**
8. Copy the generated token (you'll need this for the `CF_DNS_API_TOKEN` environment variable)

## Step 2: Get Your EC2 Public IP

1. Go to AWS Console → EC2 → Instances
2. Select your EC2 instance
3. Copy the **Public IPv4 address** (e.g., `3.250.123.45`)

## Step 3: Configure Cloudflare DNS Records

1. Log in to your Cloudflare dashboard
2. Select your domain (`tinyls.com`)
3. Go to **DNS** → **Records**
4. Add the following DNS records:

### A Records (IPv4)

| Type | Name | IPv4 address | Proxy status |
|------|------|--------------|--------------|
| A | staging | `YOUR_EC2_PUBLIC_IP` | **Proxied (orange cloud)** |
| A | api.staging | `YOUR_EC2_PUBLIC_IP` | **Proxied (orange cloud)** |
| A | traefik.staging | `YOUR_EC2_PUBLIC_IP` | **Proxied (orange cloud)** |
| A | prometheus.staging | `YOUR_EC2_PUBLIC_IP` | **Proxied (orange cloud)** |
| A | grafana.staging | `YOUR_EC2_PUBLIC_IP` | **Proxied (orange cloud)** |
| A | adminer.staging | `YOUR_EC2_PUBLIC_IP` | **Proxied (orange cloud)** |

### Example Configuration

```
Type: A
Name: staging
IPv4 address: 3.250.123.45
Proxy status: Proxied (orange cloud)
TTL: Auto
```

**Important**: Use **Proxied (orange cloud)** to enable Cloudflare's security and performance features.

## Step 4: Configure Environment Variables

1. On your EC2 instance, edit the environment file:
   ```bash
   nano /opt/tinyls/.env.staging
   ```

2. Add your Cloudflare API token:
   ```env
   CF_DNS_API_TOKEN=your-cloudflare-dns-api-token-here
   ```

3. Save the file and restart the services:
   ```bash
   docker-compose -f docker-compose.staging.yml down
   docker-compose -f docker-compose.staging.yml up -d
   ```

## Step 5: Cloudflare SSL/TLS Settings

1. Go to **SSL/TLS** → **Overview**
2. Set SSL/TLS encryption mode to **Full (strict)**
3. Go to **SSL/TLS** → **Edge Certificates**
4. Enable **Always Use HTTPS**
5. Enable **Minimum TLS Version** (set to 1.2)
6. Enable **Opportunistic Encryption**
7. Enable **TLS 1.3**
8. Enable **Automatic HTTPS Rewrites**

## Step 6: Security Settings

1. Go to **Security** → **WAF**
2. Enable **Web Application Firewall**
3. Go to **Security** → **DDoS**
4. Enable **DDoS Protection**
5. Go to **Security** → **Bot Fight Mode**
6. Enable **Bot Fight Mode**

## Step 7: Performance Settings

1. Go to **Speed** → **Optimization**
2. Enable **Auto Minify** for JavaScript, CSS, and HTML
3. Enable **Brotli** compression
4. Enable **Rocket Loader**
5. Enable **Early Hints**

## Step 8: Verify DNS Challenge

After starting the services, Traefik will automatically:

1. Create DNS records for Let's Encrypt verification
2. Request SSL certificates using DNS challenge
3. Clean up the verification records

You can monitor this process in the Traefik logs:
```bash
docker-compose -f docker-compose.staging.yml logs -f proxy
```

## Step 9: Test HTTPS

Once DNS challenge is complete, test HTTPS connectivity:

```bash
# Test HTTPS connectivity
curl -I https://staging.tinyls.com
curl -I https://api.staging.tinyls.com
```

## Benefits of DNS Challenge

Using DNS challenge instead of HTTP challenge provides:

1. **Works with Cloudflare Proxy**: No need to expose port 80 for HTTP challenge
2. **More Secure**: No need to serve files on port 80
3. **Wildcard Certificates**: Can issue wildcard certificates if needed
4. **Reliable**: Works even if your server is behind a firewall
5. **Automatic**: Traefik handles DNS record creation and cleanup

## Troubleshooting

### DNS Challenge Issues

1. **Check API Token**: Verify the `CF_DNS_API_TOKEN` is correct
2. **Check Permissions**: Ensure the token has Zone:Read and DNS:Edit permissions
3. **Check Zone**: Verify the token is scoped to the correct domain
4. **Check Logs**: Monitor Traefik logs for DNS challenge errors

### DNS Not Resolving

1. Check if DNS records are correctly configured
2. Wait for DNS propagation (can take up to 24 hours)
3. Use `dig` or `nslookup` to verify resolution
4. Ensure Cloudflare proxy is enabled (orange cloud)

### SSL Certificate Issues

1. Check Traefik logs: `docker-compose -f docker-compose.staging.yml logs proxy`
2. Verify DNS propagation before requesting certificates
3. Check Cloudflare SSL/TLS mode is set to "Full (strict)"
4. Ensure the Cloudflare API token has correct permissions

### Connection Refused

1. Check EC2 Security Group settings
2. Ensure ports 80 and 443 are open
3. Verify application is running: `docker-compose -f docker-compose.staging.yml ps`
4. Check if Cloudflare proxy is interfering (try DNS only temporarily)

## Security Group Configuration

Your EC2 Security Group should allow:

| Port | Protocol | Source | Description |
|------|----------|--------|-------------|
| 22 | TCP | Your IP | SSH access |
| 80 | TCP | 0.0.0.0/0 | HTTP (optional with DNS challenge) |
| 443 | TCP | 0.0.0.0/0 | HTTPS |

**Note**: With DNS challenge, port 80 is not strictly required for Let's Encrypt, but it's still good to have for HTTP to HTTPS redirects.

## Monitoring

After setup, you can monitor your application at:

- **Traefik Dashboard**: https://traefik.staging.tinyls.com
- **Prometheus**: https://prometheus.staging.tinyls.com
- **Grafana**: https://grafana.staging.tinyls.com

## Benefits of Cloudflare Proxy

With Cloudflare proxy enabled, you get:

1. **DDoS Protection**: Automatic protection against attacks
2. **Global CDN**: Faster loading times worldwide
3. **SSL/TLS**: Free SSL certificates and encryption
4. **Security**: WAF, bot protection, and threat intelligence
5. **Performance**: Caching, compression, and optimization
6. **Analytics**: Detailed traffic and security analytics

## Next Steps

1. Set up monitoring alerts in Grafana
2. Configure backup strategies
3. Set up CI/CD pipeline for automated deployments
4. Implement logging aggregation
5. Set up health checks and auto-scaling
6. Configure Cloudflare Page Rules for specific optimizations 