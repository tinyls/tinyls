#! /usr/bin/env bash
set -euo pipefail

# Parse command line arguments
TARGET=${1:-vps}  # Default to vps if no argument provided

# Validate target argument
if [[ "$TARGET" != "ec2" && "$TARGET" != "vps" ]]; then
    echo "❌ Error: Invalid target '$TARGET'. Must be 'ec2' or 'vps'."
    echo "Usage: $0 [ec2|vps]"
    echo "Default: vps (if no argument provided)"
    exit 1
fi

echo "⏳ Bootstrapping server for target: $TARGET..."

# TODO: convert apt to snap
# 0) Setup
# --------------------------------------------------
APP_DIR="/opt/tinyls"

STAGING_ENV_FILE="${APP_DIR}/.env.staging"
TRAEFIK_KEYS_FILE="${APP_DIR}/infra/traefik/usersfile"

log(){ printf "\e[32m[INFO]\e[0m %s\n" "$1"; }
warn(){ printf "\e[33m[WARN]\e[0m %s\n" "$1"; }
err(){  printf "\e[31m[ERROR]\e[0m %s\n" "$1"; exit 1; }

# 1) Pre-flight
# --------------------------------------------------
if [ "$(id -u)" -eq 0 ]; then
  error "This script should NOT be run as root user."
fi

# 2) Update system packages
# --------------------------------------------------
log "Updating system packages..."
sudo apt update && sudo apt upgrade -y

# 3) Ensure docker and docker-compose are installed
# --------------------------------------------------
log "Installing Docker ..."
if ! command -v docker &> /dev/null; then
    sudo apt-get install ca-certificates curl
    sudo install -m 0755 -d /etc/apt/keyrings
    sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    sudo chmod a+r /etc/apt/keyrings/docker.asc

    # Add the repository to Apt sources:
    echo \
        "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
        $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
        sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin
    sudo usermod -aG docker $USER
    warn "Docker installed. Please log out and back in for group changes to take effect."
else
    log "Docker is already installed."
fi

log "Installing Docker Compose..."
if ! command -v docker compose &> /dev/null; then
    sudo apt-get install -y docker-compose-plugin
else
    log "Docker Compose is already installed."
fi

# 4) Ensure AWS CLI is installed (only for EC2)
# --------------------------------------------------
if [[ "$TARGET" == "ec2" ]]; then
    log "Installing AWS CLI..."
    if ! command -v aws --version &> /dev/null; then
        sudo snap install aws-cli --classic
        # sudo apt-get install unzip
        # curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
        # unzip awscliv2.zip
        # sudo ./aws/install
        # rm awscliv2.zip
    else
        log "AWS CLI is already installed with the version:"
        aws --version
    fi
else
    log "Skipping AWS CLI installation (not needed for VPS target)"
fi

# 5) Install Certbot as backup
# --------------------------------------------------
log "Installing Certbot as backup for TLS..."
sudo apt install -y certbot
sudo snap install certbot-dns-cloudflare

# 6) Create application directory & clone/pull
# --------------------------------------------------
log "Creating application directory at $APP_DIR..."
sudo mkdir -p $APP_DIR
sudo chown $USER:$USER $APP_DIR
cd $APP_DIR

if [ ! -d "$APP_DIR/.git" ]; then
  log "Cloning repository..."
  git clone https://github.com/imounish/tinyls.git .
else
  log "Repository already exists, pulling latest changes..."
  git pull origin
fi
git checkout deployment/staging

# 7) Fetch env from SSM if not present (only for EC2)
# --------------------------------------------------
if [[ "$TARGET" == "ec2" ]]; then
    log "Setting up environment variables..."
    if [ ! -f $STAGING_ENV_FILE ]; then
        log "Pulling env vars from from AWS SSM..."
        ./scripts/generate-env-staging.sh
    else
        log "$STAGING_ENV_FILE already exists; Skipping"
    fi
else
    log "Skipping SSM env fetch (not needed for VPS target)"
fi

# 8) Ensure Traefik creds (only for EC2)
# --------------------------------------------------
if [[ "$TARGET" == "ec2" ]]; then
    if [ ! -f $TRAEFIK_KEYS_FILE ]; then
      log "Generating Traefik usersfile..."
      aws ssm get-parameter \
        --name "/dev/TRAEFIK_CREDENTIALS" \
        --with-decryption \
        --query Parameter.Value \
        --output text > "$TRAEFIK_KEYS_FILE"
      chmod 600 "$TRAEFIK_KEYS_FILE"
    else
      log "Traefik credentials file exists"
    fi
else
    log "Skipping Traefik credentials setup (not needed for VPS target)"
fi

# 9) Set proper permissions for Traefik certificates (only for EC2)
# --------------------------------------------------
if [[ "$TARGET" == "ec2" ]]; then
    log "Setting up Traefik certificate directory..."
    sudo mkdir -p $APP_DIR/infra/traefik/logs
    sudo chown -R $USER:$USER $APP_DIR/infra/traefik/logs
    sudo chmod 600 $APP_DIR/infra/traefik/acme.json
else
    log "Skipping Traefik certificate setup (not needed for VPS target)"
fi

# 10) Update Traefik configuration with your email
# TODO: remove hardcoded email in `static.staging.yml` and inject in build pipeline
# log "Updating Traefik configuration..."
# if [ -f "$APP_DIR/infra/traefik/config/static.staging.yml" ]; then
#     read -p "Enter your email for Let's Encrypt notifications: " EMAIL
#     sed -i "s/your-email@example.com/$EMAIL/g" $APP_DIR/infra/traefik/config/static.staging.yml
# fi

cd ~

echo "✅ Bootstrap complete for target: $TARGET. Use deploy.sh for everyday pushes."