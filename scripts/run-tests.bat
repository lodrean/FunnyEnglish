@echo off
chcp 65001 >nul
REM Script for running tests in Docker
REM Usage: run-tests.bat [unit|integration|all]

setlocal enabledelayedexpansion

set "TEST_TYPE=%~1"
if "%~1"=="" set "TEST_TYPE=all"

echo ==========================================
echo So to Speak Test Runner
echo Test type: %TEST_TYPE%
echo ==========================================

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

cd /d "%PROJECT_ROOT%"

if "%TEST_TYPE%"=="unit" (
    echo Running unit tests...
    docker compose -f docker/docker-compose.test.yml run --rm --build test-runner ./gradlew :backend:test --no-daemon --tests "*Test" -x integrationTest
) else if "%TEST_TYPE%"=="integration" (
    echo Running integration tests...
    docker compose -f docker/docker-compose.integration-test.yml up --build --abort-on-container-exit
    echo Cleaning up...
    docker compose -f docker/docker-compose.integration-test.yml down -v
) else if "%TEST_TYPE%"=="all" (
    echo Running all tests...
    echo.
    echo Step 1: Unit tests
    docker compose -f docker/docker-compose.test.yml run --rm --build test-runner ./gradlew :backend:test --no-daemon
    
    echo.
    echo Step 2: Integration tests
    docker compose -f docker/docker-compose.integration-test.yml up --build --abort-on-container-exit
    docker compose -f docker/docker-compose.integration-test.yml down -v
) else (
    echo Unknown test type: %TEST_TYPE%
    echo Usage: %0 [unit^|integration^|all]
    exit /b 1
)

echo.
echo ==========================================
echo Tests completed!
echo ==========================================

endlocal
