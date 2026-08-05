#!/usr/bin/env pwsh
# E2E Test Runner for So to Speak Application
# Runs both Playwright (admin-web) and Maestro (mobile) tests

param(
    [Parameter()]
    [ValidateSet("all", "admin", "mobile", "backend")]
    [string]$Target = "all",
    
    [Parameter()]
    [switch]$Headed,
    
    [Parameter()]
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$results = @{
    Admin = @{ Passed = 0; Failed = 0; Skipped = $false }
    Mobile = @{ Passed = 0; Failed = 0; Skipped = $false }
    Backend = @{ Passed = 0; Failed = 0; Skipped = $false }
}

function Write-Header($text) {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host $text -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
}

function Write-Success($text) {
    Write-Host "✓ $text" -ForegroundColor Green
}

function Write-Error($text) {
    Write-Host "✗ $text" -ForegroundColor Red
}

function Write-Warning($text) {
    Write-Host "⚠ $text" -ForegroundColor Yellow
}

# Check prerequisites
Write-Header "Checking Prerequisites"

if ($Target -eq "all" -or $Target -eq "admin") {
    $playwrightInstalled = Get-Command npx -ErrorAction SilentlyContinue
    if (-not $playwrightInstalled) {
        Write-Error "npx not found. Please install Node.js and npm."
        $results.Admin.Skipped = $true
    } else {
        Write-Success "npx found"
    }
}

if ($Target -eq "all" -or $Target -eq "mobile") {
    $maestroInstalled = Get-Command maestro -ErrorAction SilentlyContinue
    if (-not $maestroInstalled) {
        Write-Warning "Maestro not found. Mobile tests will be skipped."
        Write-Host "Install Maestro: https://maestro.mobile.dev/getting-started/installing-maestro"
        $results.Mobile.Skipped = $true
    } else {
        Write-Success "Maestro found"
    }
}

# Build application if needed
if (-not $SkipBuild -and $Target -eq "mobile") {
    Write-Header "Building Mobile Application"
    try {
        ./gradlew :composeApp:assembleDebug -q
        Write-Success "Mobile app built successfully"
    } catch {
        Write-Error "Failed to build mobile app: $_"
        exit 1
    }
}

# Run Admin Web Tests (Playwright)
if (($Target -eq "all" -or $Target -eq "admin") -and -not $results.Admin.Skipped) {
    Write-Header "Running Admin Web E2E Tests (Playwright)"
    
    Push-Location admin-web
    try {
        # Check if dependencies are installed
        if (-not (Test-Path node_modules)) {
            Write-Host "Installing dependencies..."
            npm install
        }
        
        # Run tests
        $playwrightArgs = @("test", "--project=chromium-headless", "--reporter=list")
        if ($Headed) {
            $playwrightArgs = @("test", "--headed", "--reporter=list")
        }
        
        $env:SKIP_WEB_SERVER = "true"
        $env:ADMIN_URL = "http://localhost:5173"
        
        & npx playwright @playwrightArgs
        
        if ($LASTEXITCODE -eq 0) {
            $results.Admin.Passed = 1
            Write-Success "Admin tests passed"
        } else {
            $results.Admin.Failed = 1
            Write-Error "Admin tests failed (server may not be running)"
        }
    } catch {
        $results.Admin.Failed = 1
        Write-Error "Admin tests error: $_"
    } finally {
        Pop-Location
    }
}

# Run Mobile Tests (Maestro)
if (($Target -eq "all" -or $Target -eq "mobile") -and -not $results.Mobile.Skipped) {
    Write-Header "Running Mobile E2E Tests (Maestro)"
    
    $maestroFlows = @(
        ".maestro/flows/login.yaml",
        ".maestro/flows/home_navigation.yaml",
        ".maestro/flows/design_system_check.yaml",
        ".maestro/flows/leaderboard_view.yaml",
        ".maestro/flows/gamification_streak.yaml",
        ".maestro/flows/complete_test.yaml"
    )
    
    foreach ($flow in $maestroFlows) {
        if (Test-Path $flow) {
            Write-Host "`nRunning: $flow" -ForegroundColor Yellow
            try {
                & maestro test $flow
                if ($LASTEXITCODE -eq 0) {
                    $results.Mobile.Passed++
                    Write-Success "Flow passed: $flow"
                } else {
                    $results.Mobile.Failed++
                    Write-Error "Flow failed: $flow"
                }
            } catch {
                $results.Mobile.Failed++
                Write-Error "Flow error: $flow - $_"
            }
        } else {
            Write-Warning "Flow not found: $flow"
        }
    }
}

# Run Backend Tests (PowerShell)
if ($Target -eq "all" -or $Target -eq "backend") {
    Write-Header "Running Backend E2E Tests"
    
    if (Test-Path "api-tests/e2e-backend-tests.ps1") {
        try {
            & api-tests/e2e-backend-tests.ps1
            $results.Backend.Passed = 1
            Write-Success "Backend tests passed"
        } catch {
            $results.Backend.Failed = 1
            Write-Error "Backend tests failed: $_"
        }
    } else {
        Write-Warning "Backend tests script not found"
        $results.Backend.Skipped = $true
    }
}

# Summary
Write-Header "Test Results Summary"

$totalPassed = 0
$totalFailed = 0

foreach ($key in $results.Keys) {
    $result = $results[$key]
    if ($result.Skipped) {
        Write-Host "$key`: Skipped" -ForegroundColor Yellow
    } elseif ($result.Failed -eq 0) {
        Write-Host "$key`: Passed ($($result.Passed))" -ForegroundColor Green
        $totalPassed += $result.Passed
    } else {
        Write-Host "$key`: Failed ($($result.Failed)), Passed ($($result.Passed))" -ForegroundColor Red
        $totalPassed += $result.Passed
        $totalFailed += $result.Failed
    }
}

Write-Host "`n----------------------------------------" -ForegroundColor Cyan
if ($totalFailed -eq 0) {
    Write-Host "All tests passed! Total: $totalPassed" -ForegroundColor Green
    exit 0
} else {
    Write-Host "Tests completed with failures. Passed: $totalPassed, Failed: $totalFailed" -ForegroundColor Red
    exit 1
}
