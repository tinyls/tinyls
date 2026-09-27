#! /usr/bin/env bash

set -e
set -x

# Create a temporary directory for the OpenAPI spec
TEMP_DIR=$(mktemp -d)
trap 'rm -rf "$TEMP_DIR"' EXIT

# Ensure we only use the Orval-generated client
rm -rf frontend/src/api/client frontend/src/api/schemas

# Download the OpenAPI spec from the running Spring Boot application
# -f ensures a non-zero exit on HTTP errors
curl -sf http://localhost:8000/api-docs > "$TEMP_DIR/openapi.json"

# Move the OpenAPI spec to the frontend directory
mv "$TEMP_DIR/openapi.json" frontend/

# Change to frontend directory and generate the API client
cd frontend
npm run generate-api

# Format the generated files
npx biome format --write ./src/api 
