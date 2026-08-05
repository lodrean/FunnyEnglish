#Requires -Version 5.1
<#
.SYNOPSIS
    So to Speak Backend E2E Test Suite
    
.DESCRIPTION
    Comprehensive API testing for So to Speak backend.
    Tests authentication, test completion, gamification, and user progress.
    
.EXAMPLE
    .\e2e-backend-tests.ps1
    Runs all tests against http://localhost:8080
    
.EXAMPLE
    .\e2e-backend-tests.ps1 -BaseUrl "http://api.example.com" -Verbose
    Runs tests against custom URL with detailed output
    
#>
[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080/api",
    [string]$DemoEmail = "demo@sotospeak.app",
    [string]$DemoPassword = "demo123",
    [string]$AdminEmail = "admin@sotospeak.com",
    [string]$AdminPassword = "admin123"
)

# Configuration
$script:TestResults = @()
$script:AuthToken = $null
$script:TestUserId = $null
$script:TestCategoryId = $null
$script:TestId = $null
$script:TestProgressId = $null

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
        Write-Host "    Error: $Message" -ForegroundColor $Colors.Warning
    }
    
    $script:TestResults += [PSCustomObject]@{
        TestName = $TestName
        Success = $Success
        Message = $Message
        Timestamp = Get-Date
    }
}

function Invoke-ApiRequest($Method, $Endpoint, $Body = $null, $Token = $null, $ExpectStatus = 200) {
    $headers = @{
        "Content-Type" = "application/json"
    }
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    
    $params = @{
        Method = $Method
        Uri = "$BaseUrl$Endpoint"
        Headers = $headers
    }
    
    if ($Body) {
        $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
    }
    
    try {
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Data = $response; Status = 200 }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $ExpectStatus) {
            return @{ Success = $true; Status = $statusCode }
        }
        return @{ Success = $false; Error = $_.Exception.Message; Status = $statusCode }
    }
}

# Test Results
$passed = 0
$failed = 0

Write-TestHeader "SOTOSPEAK BACKEND E2E TESTS"
Write-Host "Base URL: $BaseUrl"
Write-Host ""

# ============================================
# AUTHENTICATION TESTS
# ============================================
Write-TestHeader "1. AUTHENTICATION TESTS"

# Test 1.1: Demo User Login
Write-Host "`n1.1 Demo User Login..." -NoNewline
$result = Invoke-ApiRequest -Method "POST" -Endpoint "/auth/login" -Body @{
    email = $DemoEmail
    password = $DemoPassword
}
if ($result.Success -and $result.Data.token) {
    $script:AuthToken = $result.Data.token
    $script:TestUserId = $result.Data.user.id
    Write-TestResult "Demo User Login" $true
    $passed++
} else {
    Write-TestResult "Demo User Login" $false $result.Error
    $failed++
}

Start-Sleep -Seconds 3

# Test 1.2: Invalid Login
Write-Host "1.2 Invalid Login..." -NoNewline
$result = Invoke-ApiRequest -Method "POST" -Endpoint "/auth/login" -Body @{
    email = $DemoEmail
    password = "wrongpassword"
}
# Server returns 400 for invalid credentials
if (-not $result.Success -and $result.Status -eq 400) {
    Write-TestResult "Invalid Login (Rejects wrong password)" $true
    $passed++
} else {
    Write-TestResult "Invalid Login" $false "Should reject wrong password (expected 400)"
    $failed++
}

Start-Sleep -Seconds 3

# Test 1.3: Get Current User
Write-Host "1.3 Get Current User..." -NoNewline
$result = Invoke-ApiRequest -Method "GET" -Endpoint "/users/me" -Token $script:AuthToken
if ($result.Success -and $result.Data.email -eq $DemoEmail) {
    Write-TestResult "Get Current User" $true
    $passed++
} else {
    Write-TestResult "Get Current User" $false $result.Error
    $failed++
}

# Test 1.4: Admin Login
Write-Host "1.4 Admin Login..." -NoNewline
$result = Invoke-ApiRequest -Method "POST" -Endpoint "/auth/login" -Body @{
    email = $AdminEmail
    password = $AdminPassword
}
if ($result.Success -and $result.Data.token) {
    Write-TestResult "Admin Login" $true
    $passed++
} else {
    Write-TestResult "Admin Login" $false $result.Error
    $failed++
}

Start-Sleep -Seconds 3

# ============================================
# CATEGORIES TESTS
# ============================================
Write-TestHeader "2. CATEGORIES TESTS"

# Test 2.1: List Categories (Public)
Write-Host "`n2.1 List Categories..." -NoNewline
$result = Invoke-ApiRequest -Method "GET" -Endpoint "/categories"
if ($result.Success -and $result.Data.Count -gt 0) {
    $script:TestCategoryId = $result.Data[0].id
    Write-TestResult "List Categories" $true "Found $($result.Data.Count) categories"
    $passed++
} else {
    Write-TestResult "List Categories" $false $result.Error
    $failed++
}

# Test 2.2: Get Category Tests
Write-Host "2.2 Get Category Tests..." -NoNewline
if ($script:TestCategoryId) {
    $result = Invoke-ApiRequest -Method "GET" -Endpoint "/categories/$($script:TestCategoryId)/tests" -Token $script:AuthToken
    if ($result.Success) {
        if ($result.Data.Count -gt 0) {
            $script:TestId = $result.Data[0].id
        }
        Write-TestResult "Get Category Tests" $true "Found $($result.Data.Count) tests"
        $passed++
    } else {
        Write-TestResult "Get Category Tests" $false $result.Error
        $failed++
    }
} else {
    Write-TestResult "Get Category Tests" $false "No category ID available"
    $failed++
}

# ============================================
# TESTS & QUESTIONS TESTS
# ============================================
Write-TestHeader "3. TEST COMPLETION TESTS"

# Test 3.1: Get Test Details
Write-Host "`n3.1 Get Test Details..." -NoNewline
if ($script:TestId) {
    $result = Invoke-ApiRequest -Method "GET" -Endpoint "/tests/$($script:TestId)" -Token $script:AuthToken
    if ($result.Success -and $result.Data.questions) {
        Write-TestResult "Get Test Details" $true "Test has $($result.Data.questions.Count) questions"
        $passed++
    } else {
        Write-TestResult "Get Test Details" $false $result.Error
        $failed++
    }
} else {
    Write-TestResult "Get Test Details" $false "No test ID available"
    $failed++
}

# Test 3.2: Submit Test (Perfect Score)
Write-Host "3.2 Submit Test (Perfect Score)..." -NoNewline
if ($script:TestId) {
    # First get test details to know questions
    $testDetails = Invoke-ApiRequest -Method "GET" -Endpoint "/tests/$($script:TestId)" -Token $script:AuthToken
    
    if ($testDetails.Success -and $testDetails.Data.questions) {
        $answers = @()
        foreach ($question in $testDetails.Data.questions) {
            # Find correct answer (isCorrect = true)
            $correctAnswer = $question.answers | Where-Object { $_.isCorrect -eq $true } | Select-Object -First 1
            if (-not $correctAnswer) {
                # Fallback to first answer if no correct answer marked
                $correctAnswer = $question.answers[0]
            }
            $answers += @{
                questionId = $question.id
                selectedAnswerIds = @($correctAnswer.id)
                dragDropMatches = $null
            }
        }
        
        $submitBody = @{
            testId = $script:TestId
            answers = $answers
            timeSpentSeconds = 60
        }
        
        Write-Host "`n    Debug: Submitting $($answers.Count) answers" -ForegroundColor $Colors.Info
        $result = Invoke-ApiRequest -Method "POST" -Endpoint "/tests/$($script:TestId)/submit" -Token $script:AuthToken -Body $submitBody
        
        if ($result.Success) {
            $stars = $result.Data.stars
            $score = $result.Data.score
            $maxScore = $result.Data.maxScore
            Write-TestResult "Submit Test ($stars Stars)" $true "Score: $score/$maxScore"
            $passed++
            
            # Check if achievement unlocked
            if ($result.Data.newAchievements -and $result.Data.newAchievements.Count -gt 0) {
                Write-Host "    🏆 Achievement unlocked: $($result.Data.newAchievements[0].name)" -ForegroundColor $Colors.Warning
            }
        } else {
            Write-TestResult "Submit Test" $false "Status: $($result.Status), Error: $($result.Error)"
            $failed++
        }
    } else {
        Write-TestResult "Submit Test" $false "Could not get test details"
        $failed++
    }
} else {
    Write-TestResult "Submit Test" $false "No test ID available"
    $failed++
}

# Test 3.3: Get User Progress
Write-Host "3.3 Get User Progress..." -NoNewline
$result = Invoke-ApiRequest -Method "GET" -Endpoint "/users/me/progress" -Token $script:AuthToken
if ($result.Success) {
    Write-TestResult "Get User Progress" $true "Found $($result.Data.Count) progress records"
    $passed++
} else {
    Write-TestResult "Get User Progress" $false $result.Error
    $failed++
}

# ============================================
# GAMIFICATION TESTS
# ============================================
Write-TestHeader "4. GAMIFICATION TESTS"

# Test 4.1: Get User Achievements
Write-Host "`n4.1 Get User Achievements..." -NoNewline
$result = Invoke-ApiRequest -Method "GET" -Endpoint "/users/me/achievements" -Token $script:AuthToken
if ($result.Success) {
    Write-TestResult "Get User Achievements" $true "Found $($result.Data.Count) achievements"
    $passed++
} else {
    Write-TestResult "Get User Achievements" $false $result.Error
    $failed++
}

# Test 4.2: Get User Streak
Write-Host "4.2 Get User Streak..." -NoNewline
$result = Invoke-ApiRequest -Method "GET" -Endpoint "/users/me/streak" -Token $script:AuthToken
if ($result.Success) {
    Write-TestResult "Get User Streak" $true "Current streak: $($result.Data.currentStreak) days"
    $passed++
} else {
    Write-TestResult "Get User Streak" $false $result.Error
    $failed++
}

# Test 4.3: Get Leaderboard
Write-Host "4.3 Get Leaderboard..." -NoNewline
$result = Invoke-ApiRequest -Method "GET" -Endpoint "/leaderboard?limit=10" -Token $script:AuthToken
if ($result.Success) {
    Write-TestResult "Get Leaderboard" $true "Found $($result.Data.entries.Count) entries"
    $passed++
} else {
    Write-TestResult "Get Leaderboard" $false $result.Error
    $failed++
}

# ============================================
# ADMIN TESTS
# ============================================
Write-TestHeader "5. ADMIN TESTS"

# Get admin token
$adminToken = $null
Write-Host "`n5.0 Admin Login..." -NoNewline
Start-Sleep -Seconds 3
$adminLogin = Invoke-ApiRequest -Method "POST" -Endpoint "/auth/login" -Body @{
    email = $AdminEmail
    password = $AdminPassword
}
if ($adminLogin.Success) {
    $adminToken = $adminLogin.Data.token
    Write-TestResult "Admin Login" $true
    $passed++
} else {
    Write-TestResult "Admin Login" $false $adminLogin.Error
    $failed++
}

# Test 5.1: Get Admin Tests
Write-Host "5.1 Get Admin Tests..." -NoNewline
if ($adminToken) {
    $result = Invoke-ApiRequest -Method "GET" -Endpoint "/admin/tests" -Token $adminToken
    if ($result.Success) {
        Write-TestResult "Get Admin Tests" $true "Found $($result.Data.Count) tests"
        $passed++
    } else {
        Write-TestResult "Get Admin Tests" $false $result.Error
        $failed++
    }
} else {
    Write-TestResult "Get Admin Tests" $false "No admin token"
    $failed++
}

# Test 5.2: Get Admin Analytics
Write-Host "5.2 Get Admin Analytics..." -NoNewline
if ($adminToken) {
    $result = Invoke-ApiRequest -Method "GET" -Endpoint "/admin/analytics" -Token $adminToken
    if ($result.Success) {
        Write-TestResult "Get Admin Analytics" $true
        $passed++
    } else {
        Write-TestResult "Get Admin Analytics" $false $result.Error
        $failed++
    }
} else {
    Write-TestResult "Get Admin Analytics" $false "No admin token"
    $failed++
}

# ============================================
# SUMMARY
# ============================================
Write-TestHeader "TEST SUMMARY"

$total = $passed + $failed
$percentage = if ($total -gt 0) { [math]::Round(($passed / $total) * 100, 2) } else { 0 }

Write-Host "`nTotal Tests: $total"
Write-Host "Passed: $passed" -ForegroundColor $Colors.Success
Write-Host "Failed: $failed" -ForegroundColor $(if ($failed -gt 0) { $Colors.Error } else { $Colors.Success })
Write-Host "Success Rate: $percentage%" -ForegroundColor $(if ($percentage -ge 80) { $Colors.Success } elseif ($percentage -ge 50) { $Colors.Warning } else { $Colors.Error })

# Save results to file
$resultsPath = "reports/backend-e2e-results.json"
New-Item -ItemType Directory -Force -Path "reports" | Out-Null
$script:TestResults | ConvertTo-Json -Depth 5 | Out-File -FilePath $resultsPath -Encoding utf8

Write-Host "`nDetailed results saved to: $resultsPath"

# Exit with error code if tests failed
if ($failed -gt 0) {
    exit 1
}
