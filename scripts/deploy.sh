#! /usr/bin/env bash
set -euo pipefail

echo "🚀 Starting docker compose deployment for tinyls stack..."

APP_DIR="/opt/tinyls"
COMPOSE_BASE_FILE="docker-compose.yml"
COMPOSE_TARGET_ENV_FILE="docker-compose.staging.yml"
STAGING_ENV_FILE="${APP_DIR}/.env.staging"

log(){ printf "\e[32m[INFO]\e[0m %s\n" "$1"; }
warn(){ printf "\e[33m[WARN]\e[0m %s\n" "$1"; }
err(){  printf "\e[31m[ERROR]\e[0m %s\n" "$1"; exit 1; }

cd "$APP_DIR"

# 0) Ensure we’re on the right branch and up-to-date
git fetch origin deployment/staging
git checkout deployment/staging
git pull

chmod 600 infra/traefik/usersfile
chmod 600 infra/traefik/acme.json


# 1) Create Docker network
# --------------------------------------------------
log "Creating Docker network..."
docker network inspect proxy-public &> /dev/null || docker network create proxy-public

# 2) Pull & deploy
# --------------------------------------------------
log "Pulling new images..."
docker compose --env-file "$STAGING_ENV_FILE" -f "$COMPOSE_BASE_FILE" -f "$COMPOSE_TARGET_ENV_FILE" pull

log "Starting containers..."
docker compose --env-file "$STAGING_ENV_FILE" -f "$COMPOSE_BASE_FILE" -f "$COMPOSE_TARGET_ENV_FILE" up -d --remove-orphans

log "Pruning unused images..."
docker image prune -f

# 3) Done
# --------------------------------------------------
log "✅ Deployment complete! Services are up:"
docker compose -f "$COMPOSE_BASE_FILE" -f "$COMPOSE_TARGET_ENV_FILE" ps