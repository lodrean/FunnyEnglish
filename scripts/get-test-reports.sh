#!/bin/bash
# Script to extract test reports from Docker containers
# Usage: ./scripts/get-test-reports.sh [output-dir]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
OUTPUT_DIR="${1:-test-reports}"

echo "Extracting test reports to: $OUTPUT_DIR"

mkdir -p "$OUTPUT_DIR"

# Extract from test container
docker cp sotospeak-test-runner:/app/backend/build/reports "$OUTPUT_DIR/backend-reports" 2>/dev/null || echo "Backend reports not available"

# Try to extract from integration test container  
docker cp sotospeak-backend-integration:/app/backend/build/reports "$OUTPUT_DIR/integration-reports" 2>/dev/null || echo "Integration reports not available"

echo "Reports extracted to: $OUTPUT_DIR"
echo ""
echo "View HTML reports at:"
echo "  - Backend: $OUTPUT_DIR/backend-reports/tests/test/index.html"
echo "  - Integration: $OUTPUT_DIR/integration-reports/tests/test/index.html"
