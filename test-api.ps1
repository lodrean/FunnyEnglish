#Requires -Version 5.1
<#
.SYNOPSIS
    FunnyEnglish API Test Suite
    
.DESCRIPTION
    Automated API testing script for FunnyEnglish backend.
    Tests authentication, categories, tests, and user flows.
    
.EXAMPLE
    .\test-api.ps1
    Runs all tests against http://localhost:8080
    
.EXAMPLE
    .\test-api.ps1 -BaseUrl "http://api.example.com" -Verbose
    Runs tests against custom URL with detailed output
    
#>
[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminEmail = "demo@funnyenglish.app",
    [string]$AdminPassword = "demo123"
)

# Configuration
$script:TestResults = @()
$script:AuthToken = $null
$script:TestUserId = $null

# Colors for output
$Colors = @{
    Success = "Green"
    Error = "Red"
    Warning = "Yellow"
    Info = "Cyan"
}

function Write-TestHeader($Title) {
    Write-Host "`n========================================" -ForegroundColor $Colors.Info
    Write-Host "  $Title" -ForegroundColor $Colors.Info
    Write-Host "========================================" -ForegroundColor $Colors.Info
}

function Write-TestResult($TestName, $Success, $Message = "") {
    $status = if ($Success) { "✅ PASS" } else { "❌ FAIL" }
    $color = if ($Success) { $Colors.Success } else { $Colors.Error }
    
    Write-Host "  $status - $TestName" -ForegroundColor $color
    if ($Message -and !$Success) {
        Write-Host "         $Message" -ForegroundColor $Colors.Warning
    }
    
    $script:TestResults += [PSCustomObject]@{
        Test = $TestName
        Success = $Success
        Message = $Message
        Timestamp = Get-Date -Format "HH:mm:ss"
    }
}

function Invoke-ApiRequest {
    param(
        [string]$Method = "GET",
        [string]$Endpoint,
        [object]$Body = $null,
        [hashtable]$Headers = @{},
        [switch]$SkipAuth
    )
    
    $url = "$BaseUrl$Endpoint"
    
    if ($script:AuthToken -and !$SkipAuth) {
        $Headers["Authorization"] = "Bearer $script:AuthToken"
    }
    
    $Headers["Content-Type"] = "application/json"
    
    try {
        # Build curl command
        $curlArgs = @("-s", "-w", "`nHTTP_CODE:%{http_code}", "--max-time", "10", "-X", $Method)
        
        # Add headers
        foreach ($h in $Headers.GetEnumerator()) {
            $curlArgs += "-H"
            $curlArgs += "$($h.Key): $($h.Value)"
        }
        
        # Add body if present
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10 -Compress
            $curlArgs += "-d"
            $curlArgs += $jsonBody
        }
        
        $curlArgs += $url
        
        # Execute curl
        $response = & curl.exe @curlArgs
        
        # Parse response
        $lines = $response -split "`n"
        $httpCodeLine = $lines | Where-Object { $_ -match "HTTP_CODE:(\d+)" } | Select-Object -Last 1
        $statusCode = 0
        if ($httpCodeLine -match "HTTP_CODE:(\d+)") {
            $statusCode = [int]$matches[1]
        }
        
        # Get JSON content (everything before HTTP_CODE line)
        $jsonContent = $response -replace "`nHTTP_CODE:\d+", "" | ConvertFrom-Json -ErrorAction SilentlyContinue
        
        return @{
            Success = $statusCode -ge 200 -and $statusCode -lt 300
            StatusCode = $statusCode
            Content = $jsonContent
            RawContent = $response
        }
    }
    catch {
        return @{
            Success = $false
            StatusCode = 0
            Error = $_.Exception.Message
            RawContent = $null
        }
    }
}

# ==================== TESTS ====================

function Test-Health {
    Write-TestHeader "Health Check"
    
    $result = Invoke-ApiRequest -Endpoint "/categories" -SkipAuth
    $success = $result.StatusCode -eq 200
    
    Write-TestResult "Backend is running" $success "Status: $($result.StatusCode)"
    return $success
}

function Test-Authentication {
    Write-TestHeader "Authentication Tests"
    
    # Test 1: Login with valid credentials
    $result = Invoke-ApiRequest -Method POST -Endpoint "/auth/login" -SkipAuth -Body @{
        email = $AdminEmail
        password = $AdminPassword
    }
    
    $success = $result.StatusCode -eq 200 -and $result.Content.token
    if ($success) {
        $script:AuthToken = $result.Content.token
        $script:TestUserId = $result.Content.user.id
    }
    
    Write-TestResult "Login with valid credentials" $success
    
    # Test 2: Login with invalid credentials
    $result = Invoke-ApiRequest -Method POST -Endpoint "/auth/login" -SkipAuth -Body @{
        email = $AdminEmail
        password = "wrongpassword"
    }
    
    Write-TestResult "Login with invalid credentials (401)" ($result.StatusCode -eq 401)
    
    # Test 3: Access protected endpoint without token
    $result = Invoke-ApiRequest -Endpoint "/admin/tests" -SkipAuth
    Write-TestResult "Access admin without token (401)" ($result.StatusCode -eq 401)
    
    # Test 4: Access protected endpoint with token
    if ($script:AuthToken) {
        $result = Invoke-ApiRequest -Endpoint "/admin/tests"
        Write-TestResult "Access admin with token (200)" ($result.StatusCode -eq 200)
    }
}

function Test-Categories {
    Write-TestHeader "Categories Tests"
    
    # Test 1: Get all categories
    $result = Invoke-ApiRequest -Endpoint "/categories"
    $success = $result.StatusCode -eq 200 -and $result.Content.Count -gt 0
    
    Write-TestResult "Get all categories" $success "Found: $(($result.Content).Count) categories"
    
    # Save first category for later tests
    if ($success) {
        $script:FirstCategory = $result.Content[0]
    }
    
    # Test 2: Get tests for category
    if ($script:FirstCategory) {
        $result = Invoke-ApiRequest -Endpoint "/categories/$($script:FirstCategory.id)/tests"
        $success = $result.StatusCode -eq 200
        Write-TestResult "Get tests for category" $success "Category: $($script:FirstCategory.name)"
        
        if ($success -and $result.Content.Count -gt 0) {
            $script:FirstTest = $result.Content[0]
        }
    }
}

function Test-Tests {
    Write-TestHeader "Tests CRUD"
    
    # Test 1: Get all tests
    $result = Invoke-ApiRequest -Endpoint "/tests"
    Write-TestResult "Get all tests" ($result.StatusCode -eq 200)
    
    # Test 2: Get specific test
    if ($script:FirstTest) {
        $result = Invoke-ApiRequest -Endpoint "/tests/$($script:FirstTest.id)"
        $success = $result.StatusCode -eq 200 -and $result.Content.title
        Write-TestResult "Get test by ID" $success "Test: $($script:FirstTest.title)"
    }
    
    # Test 3: Create test (Admin only)
    if ($script:AuthToken) {
        $newTest = @{
            title = "API Test $(Get-Date -Format 'yyyyMMddHHmmss')"
            categoryId = $script:FirstCategory.id
            difficulty = "EASY"
            description = "Created by automated test"
            pointsReward = 10
            questions = @()
        }
        
        $result = Invoke-ApiRequest -Method POST -Endpoint "/admin/tests" -Body $newTest
        $success = $result.StatusCode -eq 200 -or $result.StatusCode -eq 201
        Write-TestResult "Create test (Admin)" $success
        
        if ($success -and $result.Content.id) {
            $script:CreatedTestId = $result.Content.id
        }
    }
}

function Test-Images {
    Write-TestHeader "Image Handling"
    
    # Check if test has thumbnail
    if ($script:FirstTest -and $script:FirstTest.thumbnailUrl) {
        Write-TestResult "Test has thumbnail URL" $true "URL: $($script:FirstTest.thumbnailUrl)"
        
        # Try to access the image
        try {
            $imageResponse = Invoke-WebRequest -Uri $script:FirstTest.thumbnailUrl -Method HEAD -TimeoutSec 10
            Write-TestResult "Thumbnail image accessible" ($imageResponse.StatusCode -eq 200)
        }
        catch {
            Write-TestResult "Thumbnail image accessible" $false "Error: $($_.Exception.Message)"
        }
    }
    else {
        Write-TestResult "Test has thumbnail URL" $false "No thumbnailUrl in response"
    }
}

function Test-Leaderboard {
    Write-TestHeader "Leaderboard"
    
    $result = Invoke-ApiRequest -Endpoint "/leaderboard?limit=10"
    $success = $result.StatusCode -eq 200
    
    Write-TestResult "Get leaderboard" $success
}

function Test-ErrorHandling {
    Write-TestHeader "Error Handling"
    
    # Test 1: 404 for non-existent test
    $result = Invoke-ApiRequest -Endpoint "/tests/00000000-0000-0000-0000-000000000000"
    Write-TestResult "404 for non-existent test" ($result.StatusCode -eq 404)
    
    # Test 2: Invalid UUID format
    $result = Invoke-ApiRequest -Endpoint "/tests/invalid-uuid"
    Write-TestResult "400 for invalid UUID" ($result.StatusCode -eq 400)
}

function Test-CORS {
    Write-TestHeader "CORS Configuration"
    
    $headers = @{
        "Origin" = "http://localhost:3002"
    }
    
    $result = Invoke-ApiRequest -Endpoint "/categories" -SkipAuth -Headers $headers
    
    # Note: Invoke-WebRequest doesn't expose CORS headers easily
    # This is a basic check that request succeeds
    Write-TestResult "CORS request from localhost:3002" ($result.StatusCode -eq 200)
}

function Test-Cleanup {
    Write-TestHeader "Cleanup"
    
    # Delete created test
    if ($script:CreatedTestId -and $script:AuthToken) {
        $result = Invoke-ApiRequest -Method DELETE -Endpoint "/admin/tests/$($script:CreatedTestId)"
        Write-TestResult "Delete created test" ($result.StatusCode -eq 204 -or $result.StatusCode -eq 200)
    }
}

# ==================== MAIN ====================

function Show-TestSummary {
    Write-Host "`n========================================" -ForegroundColor $Colors.Info
    Write-Host "  TEST SUMMARY" -ForegroundColor $Colors.Info
    Write-Host "========================================" -ForegroundColor $Colors.Info
    
    $total = $script:TestResults.Count
    $passed = ($script:TestResults | Where-Object Success).Count
    $failed = $total - $passed
    
    Write-Host "`nTotal:  $total" -ForegroundColor $Colors.Info
    Write-Host "Passed: $passed" -ForegroundColor $Colors.Success
    Write-Host "Failed: $failed" -ForegroundColor $(if ($failed -gt 0) { $Colors.Error } else { $Colors.Success })
    
    if ($failed -gt 0) {
        Write-Host "`nFailed Tests:" -ForegroundColor $Colors.Error
        $script:TestResults | Where-Object { !$_.Success } | ForEach-Object {
            Write-Host "  - $($_.Test)" -ForegroundColor $Colors.Warning
            if ($_.Message) {
                Write-Host "    $($_.Message)" -ForegroundColor $Colors.Warning
            }
        }
    }
    
    Write-Host ""
    return $failed -eq 0
}

# Main execution
Write-Host "========================================" -ForegroundColor $Colors.Info
Write-Host "  FunnyEnglish API Test Suite" -ForegroundColor $Colors.Info
Write-Host "  Target: $BaseUrl" -ForegroundColor $Colors.Info
Write-Host "========================================" -ForegroundColor $Colors.Info

$allPassed = $true

# Run tests
if (Test-Health) {
    Test-Authentication
    Test-Categories
    Test-Tests
    Test-Images
    Test-Leaderboard
    Test-ErrorHandling
    Test-CORS
    Test-Cleanup
}
else {
    Write-Host "`n❌ Backend is not accessible. Tests aborted." -ForegroundColor $Colors.Error
    $allPassed = $false
}

# Summary
$success = Show-TestSummary

# Exit code
exit [int](!$success)
