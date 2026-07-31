#Requires -Version 5.1
<#
.SYNOPSIS
    FunnyEnglish Integration Tests
    
.DESCRIPTION
    Complex integration tests covering multiple endpoints and workflows.
#>
[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080/api",
    [string]$DemoEmail = "demo@funnyenglish.app",
    [string]$DemoPassword = "demo123"
)

# Colors
$Colors = @{ Success = "Green"; Error = "Red"; Info = "Cyan"; Warning = "Yellow" }

function Write-TestHeader($Title) {
    Write-Host "`n========================================" -ForegroundColor $Colors.Info
    Write-Host "  $Title" -ForegroundColor $Colors.Info
    Write-Host "========================================" -ForegroundColor $Colors.Info
}

function Invoke-ApiRequest($Method, $Endpoint, $Body = $null, $Token = $null) {
    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    
    try {
        $params = @{ Method = $Method; Uri = "$BaseUrl$Endpoint"; Headers = $headers }
        if ($Body) { $params["Body"] = ($Body | ConvertTo-Json -Depth 10) }
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Data = $response }
    }
    catch {
        return @{ Success = $false; Error = $_.Exception.Message; Status = $_.Exception.Response.StatusCode.value__ }
    }
}

$passed = 0
$failed = 0

Write-TestHeader "INTEGRATION TESTS"

# Login
$login = Invoke-ApiRequest -Method "POST" -Endpoint "/auth/login" -Body @{ email = $DemoEmail; password = $DemoPassword }
if (-not $login.Success) { Write-Host "Login failed!"; exit 1 }
$token = $login.Data.token
$userId = $login.Data.user.id
Write-Host "Logged in as: $($login.Data.user.displayName)"

# ============================================
# WORKFLOW 1: Complete Learning Session
# ============================================
Write-TestHeader "WORKFLOW 1: Complete Learning Session"

# Get initial XP
$initialXp = Invoke-ApiRequest -Method "GET" -Endpoint "/users/me/profile" -Token $token
$startXp = $initialXp.Data.user.totalPoints
Write-Host "Initial XP: $startXp"

# Get a test and complete it
$categories = Invoke-ApiRequest -Method "GET" -Endpoint "/categories" -Token $token
$categoryId = $categories.Data[0].id
$tests = Invoke-ApiRequest -Method "GET" -Endpoint "/categories/$categoryId/tests" -Token $token
$testId = $tests.Data[0].id

$testDetails = Invoke-ApiRequest -Method "GET" -Endpoint "/tests/$testId" -Token $token
$answers = @()
foreach ($q in $testDetails.Data.questions) {
    $correct = $q.answers | Where-Object { $_.isCorrect } | Select-Object -First 1
    $answers += @{ questionId = $q.id; selectedAnswerIds = @($correct.id) }
}

$result = Invoke-ApiRequest -Method "POST" -Endpoint "/tests/$testId/submit" -Token $token -Body @{
    testId = $testId
    answers = $answers
    timeSpentSeconds = 120
}

if ($result.Success) {
    Write-Host "✅ Test completed: $($result.Data.stars) stars, +$($result.Data.pointsEarned) XP" -ForegroundColor $Colors.Success
    
    # Check XP increased
    $newProfile = Invoke-ApiRequest -Method "GET" -Endpoint "/users/me/profile" -Token $token
    $newXp = $newProfile.Data.user.totalPoints
    
    if ($newXp -gt $startXp) {
        Write-Host "✅ XP increased from $startXp to $newXp" -ForegroundColor $Colors.Success
        $passed++
    } else {
        Write-Host "❌ XP not increased" -ForegroundColor $Colors.Error
        $failed++
    }
    
    # Check achievement unlocked
    if ($result.Data.newAchievements.Count -gt 0) {
        Write-Host "✅ Achievement unlocked: $($result.Data.newAchievements[0].name)" -ForegroundColor $Colors.Success
    }
    
    # Check progress recorded
    $progress = Invoke-ApiRequest -Method "GET" -Endpoint "/users/me/progress" -Token $token
    $testProgress = $progress.Data | Where-Object { $_.testId -eq $testId }
    if ($testProgress) {
        Write-Host "✅ Progress recorded: $($testProgress.stars) stars" -ForegroundColor $Colors.Success
        $passed++
    } else {
        Write-Host "❌ Progress not recorded" -ForegroundColor $Colors.Error
        $failed++
    }
} else {
    Write-Host "❌ Test submission failed" -ForegroundColor $Colors.Error
    $failed += 2
}

# ============================================
# WORKFLOW 2: Gamification Features
# ============================================
Write-TestHeader "WORKFLOW 2: Gamification Features"

# Check streak
$streak = Invoke-ApiRequest -Method "GET" -Endpoint "/users/me/streak" -Token $token
if ($streak.Success) {
    Write-Host "✅ Streak: $($streak.Data.currentStreak) days" -ForegroundColor $Colors.Success
    $passed++
} else {
    Write-Host "❌ Failed to get streak" -ForegroundColor $Colors.Error
    $failed++
}

# Check achievements
$achievements = Invoke-ApiRequest -Method "GET" -Endpoint "/users/me/achievements" -Token $token
if ($achievements.Success) {
    $earnedCount = ($achievements.Data | Where-Object { $_.earned }).Count
    Write-Host "✅ Achievements: $earnedCount earned" -ForegroundColor $Colors.Success
    $passed++
} else {
    Write-Host "❌ Failed to get achievements" -ForegroundColor $Colors.Error
    $failed++
}

# Check leaderboard position
$leaderboard = Invoke-ApiRequest -Method "GET" -Endpoint "/leaderboard?limit=10" -Token $token
if ($leaderboard.Success) {
    $userRank = $leaderboard.Data.userRank
    Write-Host "✅ Leaderboard rank: $userRank" -ForegroundColor $Colors.Success
    $passed++
} else {
    Write-Host "❌ Failed to get leaderboard" -ForegroundColor $Colors.Error
    $failed++
}

# ============================================
# WORKFLOW 3: Admin Operations
# ============================================
Write-TestHeader "WORKFLOW 3: Admin Operations"

# Admin login
$adminLogin = Invoke-ApiRequest -Method "POST" -Endpoint "/auth/login" -Body @{ 
    email = "admin@funnyenglish.com"; password = "admin123" 
}

if ($adminLogin.Success) {
    $adminToken = $adminLogin.Data.token
    
    # Get analytics
    $analytics = Invoke-ApiRequest -Method "GET" -Endpoint "/admin/analytics" -Token $adminToken
    if ($analytics.Success) {
        Write-Host "✅ Analytics loaded" -ForegroundColor $Colors.Success
        Write-Host "   - Total users: $($analytics.Data.totalUsers)"
        Write-Host "   - Total tests: $($analytics.Data.totalTests)"
        $passed++
    } else {
        Write-Host "❌ Failed to load analytics" -ForegroundColor $Colors.Error
        $failed++
    }
    
    # Get admin tests list
    $adminTests = Invoke-ApiRequest -Method "GET" -Endpoint "/admin/tests" -Token $adminToken
    if ($adminTests.Success) {
        Write-Host "✅ Admin tests list: $($adminTests.Data.Count) tests" -ForegroundColor $Colors.Success
        $passed++
    } else {
        Write-Host "❌ Failed to get admin tests" -ForegroundColor $Colors.Error
        $failed++
    }
} else {
    Write-Host "❌ Admin login failed" -ForegroundColor $Colors.Error
    $failed += 2
}

# ============================================
# SUMMARY
# ============================================
Write-TestHeader "INTEGRATION TEST SUMMARY"

$total = $passed + $failed
$percentage = if ($total -gt 0) { [math]::Round(($passed / $total) * 100, 2) } else { 0 }

Write-Host "`nTotal Tests: $total"
Write-Host "Passed: $passed" -ForegroundColor $Colors.Success
Write-Host "Failed: $failed" -ForegroundColor $(if ($failed -gt 0) { $Colors.Error } else { $Colors.Success })
Write-Host "Success Rate: $percentage%"

if ($failed -gt 0) { exit 1 }
