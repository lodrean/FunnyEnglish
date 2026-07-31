#!/bin/bash
# Script for running tests in Docker
# Usage: ./scripts/run-tests.sh [unit|integration|all]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

TEST_TYPE="${1:-all}"

echo "=========================================="
echo "FunnyEnglish Test Runner"
echo "Test type: $TEST_TYPE"
echo "=========================================="

cd "$PROJECT_ROOT"

case "$TEST_TYPE" in
  unit)
    echo "Running unit tests..."
    docker compose -f docker/docker-compose.test.yml run --rm --build test-runner \
      ./gradlew :backend:test --no-daemon --tests "*Test" -x integrationTest
    ;;
  
  integration)
    echo "Running integration tests..."
    docker compose -f docker/docker-compose.integration-test.yml up --build --abort-on-container-exit
    echo "Cleaning up..."
    docker compose -f docker/docker-compose.integration-test.yml down -v
    ;;
  
  all)
    echo "Running all tests..."
    echo ""
    echo "Step 1: Unit tests"
    docker compose -f docker/docker-compose.test.yml run --rm --build test-runner \
      ./gradlew :backend:test --no-daemon
    
    echo ""
    echo "Step 2: Integration tests"
    docker compose -f docker/docker-compose.integration-test.yml up --build --abort-on-container-exit
    docker compose -f docker/docker-compose.integration-test.yml down -v
    ;;
  
  *)
    echo "Unknown test type: $TEST_TYPE"
    echo "Usage: $0 [unit|integration|all]"
    exit 1
    ;;
esac

echo ""
echo "=========================================="
echo "Tests completed!"
echo "=========================================="
