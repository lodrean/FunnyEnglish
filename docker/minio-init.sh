#!/bin/sh
# MinIO initialization script
# Creates bucket and sets public access
# Runs inside minio/mc container AFTER minio is healthy (compose depends_on),
# so no wait-loop is needed. MinIO is reachable at http://minio:9000.

set -e

MINIO_HOST=${MINIO_HOST:-http://minio:9000}
MINIO_USER=${MINIO_ROOT_USER:-minioadmin}
MINIO_PASS=${MINIO_ROOT_PASSWORD:-minioadmin}
BUCKET=${S3_BUCKET:-funnyenglish}

echo "Configuring MinIO at ${MINIO_HOST}..."

# Configure mc alias
mc alias set local "${MINIO_HOST}" "${MINIO_USER}" "${MINIO_PASS}" || {
    echo "Failed to set mc alias"
    exit 1
}

# Create bucket if it doesn't exist
mc mb "local/${BUCKET}" --ignore-existing || {
    echo "Failed to create bucket (may already exist)"
}

# Set public access
mc anonymous set public "local/${BUCKET}" || {
    echo "Failed to set bucket policy"
}

echo "MinIO configuration completed!"
