#!/usr/bin/env bash
set -euo pipefail

# Name of the output env file
ENV_FILE=".env"
STAGING_ENV_FILE=".env.staging"
# Traefik usersfile path
TRAEFIK_USERSFILE="infra/traefik/usersfile"

# Overwrite any existing file and add a header
cat > "${STAGING_ENV_FILE}" <<EOF
# Auto-generated .env.staging ($(date -u +"%Y-%m-%dT%H:%M:%SZ"))
# Fetched from AWS SSM Parameter Store under /dev/
EOF

cat > "${ENV_FILE}" <<EOF
# Auto-generated .env ($(date -u +"%Y-%m-%dT%H:%M:%SZ"))
# Fetched from AWS SSM Parameter Store under /dev/
EOF

# List of parameter *names* under /dev/ in AWS SSM Parameter Store for ${STAGING_ENV_FILE}:
STAGING_PARAM_KEYS=(
  DOMAIN
  FRONTEND_URL
  GITHUB_CLIENT_ID
  GITHUB_CLIENT_SECRET
  GOOGLE_CLIENT_ID
  GOOGLE_CLIENT_SECRET
  POSTGRES_DB
  POSTGRES_PASSWORD
  POSTGRES_PORT
  POSTGRES_SERVER
  POSTGRES_USER
  SECRET_KEY
  STACK_NAME
  CF_API_EMAIL
  CF_DNS_API_TOKEN
  DOCKER_IMAGE_BACKEND
  DOCKER_IMAGE_FRONTEND
)

# List of parameter *names* under /dev/ in AWS SSM Parameter Store for ${ENV_FILE}:
PARAM_KEYS=(
  DOMAIN
  POSTGRES_DB
  POSTGRES_USER
  DOCKER_IMAGE_BACKEND
  DOCKER_IMAGE_FRONTEND
  CF_API_EMAIL
  CF_DNS_API_TOKEN
)

# Loop and fetch each parameter and append KEY="VALUE" to ${STAGING_ENV_FILE}
for KEY in "${STAGING_PARAM_KEYS[@]}"; do
  PARAM_NAME="/dev/${KEY}"
  echo "Fetching ${PARAM_NAME} for ${STAGING_ENV_FILE}…"
  VALUE=$(aws ssm get-parameter \
    --name "${PARAM_NAME}" \
    --with-decryption \
    --output text \
    --query Parameter.Value)
  # Append to .env.staging
  printf '%s=%s\n' "${KEY}" "${VALUE}" >> "${STAGING_ENV_FILE}"
done

for KEY in "${PARAM_KEYS[@]}"; do
  PARAM_NAME="/dev/${KEY}"
  echo "Fetching ${PARAM_NAME} for ${ENV_FILE} …"
  VALUE=$(aws ssm get-parameter \
    --name "${PARAM_NAME}" \
    --with-decryption \
    --output text \
    --query Parameter.Value)
  # Append to .env
  printf '%s=%s\n' "${KEY}" "${VALUE}" >> "${ENV_FILE}"
done

# Now fetch Traefik credentials and write to infra/traefik/usersfile
echo "Fetching /dev/TRAEFIK_CREDENTIALS …"
TRAEFIK_CREDS=$(aws ssm get-parameter \
  --name "/dev/TRAEFIK_CREDENTIALS" \
  --with-decryption \
  --output text \
  --query Parameter.Value)

# Fetch Cloudflare DNS API token
echo "Fetching /dev/CF_DNS_API_TOKEN …"
CF_TOKEN=$(aws ssm get-parameter \
  --name "/dev/CF_DNS_API_TOKEN" \
  --with-decryption \
  --output text --query Parameter.Value)
printf 'CF_DNS_API_TOKEN="%s"\n' "$CF_TOKEN" >> .env.staging

# (Optional) If you stored a CF_API_EMAIL in SSM:
echo "Fetching /dev/CF_API_EMAIL …"
CF_EMAIL=$(aws ssm get-parameter \
  --name "/dev/CF_API_EMAIL" \
  --output text --query Parameter.Value)
printf 'CF_API_EMAIL="%s"\n' "$CF_EMAIL" >> .env.staging

# Write the raw credentials (e.g. "user:hashedpassword") into usersfile
printf '%s\n' "${TRAEFIK_CREDS}" > "${TRAEFIK_USERSFILE}"

chmod 600 ${TRAEFIK_USERSFILE}

echo "✅ ${ENV_FILE} file generated successfully."
echo "✅ ${STAGING_ENV_FILE} file generated successfully."
echo "✅ Written Traefik usersfile to ${TRAEFIK_USERSFILE}"
