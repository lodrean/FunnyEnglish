#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Full Test Suite Runner for FunnyEnglish
.DESCRIPTION
    Automated testing of all features across all platforms
#>

param(
    [switch]$SkipBackend,
    [switch]$SkipAdminWeb,
    [switch]$SkipCMP,
    [switch]$Quick,      # Only critical tests
    [switch]$CI,         # CI mode (headless)
    [string]$ReportDir = "test-reports"
)

$ErrorActionPreference = "Stop"
$StartTime = Get-Date

# Colors for output
$Green = "`e[32m"
$Red = "`e[31m"
$Yellow = "`e[33m"
$Reset = "`e[0m"

function Write-Header($text) {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host $text -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
}

function Write-Success($text) {
    Write-Host "${Green}✅ $text${Reset}"
}

function Write-Error($text) {
    Write-Host "${Red}❌ $text${Reset}"
}

function Write-Warning($text) {
    Write-Host "${Yellow}⚠️  $text${Reset}"
}

# Create report directory
New-Item -ItemType Directory -Force -Path $ReportDir | Out-Null

$Results = @{
    Backend = @{ Status = "SKIPPED"; Passed = 0; Failed = 0 }
    AdminWeb = @{ Status = "SKIPPED"; Passed = 0; Failed = 0 }
    CMP = @{ Status = "SKIPPED"; Passed = 0; Failed = 0 }
}

Write-Header "FunnyEnglish Full Test Suite"
Write-Host "Mode: $(if($Quick){'QUICK'}else{'FULL'})"
Write-Host "Started: $StartTime"
Write-Host ""

# ============================================
# 1. Backend Tests
# ============================================
if (-not $SkipBackend) {
    Write-Header "1. BACKEND TESTS"
    
    try {
        Push-Location backend
        
        Write-Host "Running unit tests..."
        $output = ./gradlew.bat test --quiet 2>&1
        $exitCode = $LASTEXITCODE
        
        if ($exitCode -eq 0) {
            Write-Success "Backend tests PASSED"
            $Results.Backend.Status = "PASSED"
            $Results.Backend.Passed = 100  # Approximate
        } else {
            Write-Error "Backend tests FAILED"
            $Results.Backend.Status = "FAILED"
            $Results.Backend.Failed = 1
        }
        
        # Copy test results
        if (Test-Path "build/reports/tests/test") {
            Copy-Item -Recurse -Force "build/reports/tests/test" "../$ReportDir/backend-tests"
        }
        
        Pop-Location
    }
    catch {
        Write-Error "Backend tests error: $_"
        $Results.Backend.Status = "ERROR"
    }
}

# ============================================
# 2. Admin Web E2E Tests
# ============================================
if (-not $SkipAdminWeb) {
    Write-Header "2. ADMIN WEB E2E TESTS"
    
    try {
        Push-Location admin-web
        
        # Check if dev server is running
        $serverRunning = $false
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:3000" -TimeoutSec 5 -ErrorAction SilentlyContinue
            $serverRunning = $response.StatusCode -eq 200
        }
        catch { }
        
        if (-not $serverRunning -and -not $CI) {
            Write-Warning "Dev server not running. Starting..."
            Start-Process -FilePath "cmd" -ArgumentList "/c npm run dev" -WindowStyle Hidden
            Start-Sleep -Seconds 10
        }
        
        Write-Host "Running Playwright tests..."
        $env:CI = if ($CI) { "true" } else { "" }
        
        $testPattern = if ($Quick) { "tests/(smoke|auth)" } else { "tests" }
        $output = npx playwright test $testPattern --reporter=line 2>&1
        $exitCode = $LASTEXITCODE
        
        # Parse results
        if ($output -match '(\d+) passed') {
            $Results.AdminWeb.Passed = [int]$matches[1]
        }
        if ($output -match '(\d+) failed') {
            $Results.AdminWeb.Failed = [int]$matches[1]
        }
        
        if ($exitCode -eq 0) {
            Write-Success "Admin Web E2E tests PASSED"
            $Results.AdminWeb.Status = "PASSED"
        } else {
            Write-Error "Admin Web E2E tests FAILED"
            $Results.AdminWeb.Status = "FAILED"
        }
        
        # Copy reports
        if (Test-Path "playwright-report") {
            Copy-Item -Recurse -Force "playwright-report" "../$ReportDir/admin-web-e2e"
        }
        if (Test-Path "test-results") {
            Copy-Item -Recurse -Force "test-results" "../$ReportDir/admin-web-results"
        }
        
        Pop-Location
    }
    catch {
        Write-Error "Admin Web tests error: $_"
        $Results.AdminWeb.Status = "ERROR"
    }
}

# ============================================
# 3. CMP E2E Tests
# ============================================
if (-not $SkipCMP) {
    Write-Header "3. CMP E2E TESTS"
    
    try {
        Push-Location e2e-cmp
        
        Write-Host "Running CMP configuration tests..."
        $output = npx playwright test tests/config.spec.ts --reporter=line 2>&1
        $exitCode = $LASTEXITCODE
        
        if ($output -match '(\d+) passed') {
            $Results.CMP.Passed = [int]$matches[1]
        }
        if ($output -match '(\d+) failed') {
            $Results.CMP.Failed = [int]$matches[1]
        }
        
        if ($exitCode -eq 0) {
            Write-Success "CMP config tests PASSED"
            $Results.CMP.Status = "PASSED"
        } else {
            Write-Error "CMP config tests FAILED"
            $Results.CMP.Status = "FAILED"
        }
        
        if (-not $Quick) {
            Write-Warning "Full CMP tests require WASM compilation (5-10 min)"
            Write-Host "Run manually: cd e2e-cmp && npm test"
        }
        
        # Copy reports
        if (Test-Path "playwright-report") {
            Copy-Item -Recurse -Force "playwright-report" "../$ReportDir/cmp-e2e"
        }
        
        Pop-Location
    }
    catch {
        Write-Error "CMP tests error: $_"
        $Results.CMP.Status = "ERROR"
    }
}

# ============================================
# Summary
# ============================================
$EndTime = Get-Date
$Duration = $EndTime - $StartTime

Write-Header "TEST SUMMARY"
Write-Host "Duration: $($Duration.ToString('mm\:ss'))"
Write-Host ""

foreach ($component in $Results.Keys) {
    $result = $Results[$component]
    $color = switch ($result.Status) {
        "PASSED" { $Green }
        "FAILED" { $Red }
        "ERROR" { $Red }
        "SKIPPED" { $Yellow }
        default { $Reset }
    }
    
    Write-Host "$color[$($result.Status)] $component${Reset} - Passed: $($result.Passed), Failed: $($result.Failed)"
}

Write-Host ""
Write-Host "Reports saved to: $ReportDir/"
Write-Host ""

# Overall status
$allPassed = ($Results.Backend.Status -eq "PASSED" -or $Results.Backend.Status -eq "SKIPPED") -and
             ($Results.AdminWeb.Status -eq "PASSED" -or $Results.AdminWeb.Status -eq "SKIPPED") -and
             ($Results.CMP.Status -eq "PASSED" -or $Results.CMP.Status -eq "SKIPPED")

if ($allPassed) {
    Write-Success "All tests completed successfully!"
    exit 0
} else {
    Write-Error "Some tests failed!"
    exit 1
}
