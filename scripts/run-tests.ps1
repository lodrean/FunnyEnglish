# PowerShell script for running tests in Docker
# Usage: .\scripts\run-tests.ps1 [unit|integration|all]

param(
    [Parameter(Position=0)]
    [ValidateSet("unit", "integration", "all")]
    [string]$TestType = "all"
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "So to Speak Test Runner" -ForegroundColor Cyan
Write-Host "Test type: $TestType" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

Set-Location $ProjectRoot

switch ($TestType) {
    "unit" {
        Write-Host "Running unit tests..." -ForegroundColor Yellow
        docker compose -f docker/docker-compose.test.yml run --rm --build test-runner `
            ./gradlew :backend:test --no-daemon --tests "*Test" -x integrationTest
    }
    
    "integration" {
        Write-Host "Running integration tests..." -ForegroundColor Yellow
        docker compose -f docker/docker-compose.integration-test.yml up --build --abort-on-container-exit
        Write-Host "Cleaning up..." -ForegroundColor Yellow
        docker compose -f docker/docker-compose.integration-test.yml down -v
    }
    
    "all" {
        Write-Host "Running all tests..." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Step 1: Unit tests" -ForegroundColor Green
        docker compose -f docker/docker-compose.test.yml run --rm --build test-runner `
            ./gradlew :backend:test --no-daemon
        
        Write-Host ""
        Write-Host "Step 2: Integration tests" -ForegroundColor Green
        docker compose -f docker/docker-compose.integration-test.yml up --build --abort-on-container-exit
        docker compose -f docker/docker-compose.integration-test.yml down -v
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "Tests completed!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
