#Requires -Version 5.1
<#
.SYNOPSIS
    FunnyEnglish Simple API Test
#>
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$Colors = @{
    Success = "Green"
    Error = "Red"
    Warning = "Yellow"
    Info = "Cyan"
}

function Test-Endpoint($Name, $Method, $Path, $ExpectedCode = 200, $Body = $null, $SkipAuth = $false) {
    $url = "$BaseUrl$Path"
    Write-Host "Testing $Method $Path..." -NoNewline
    
    $headers = @()
    if (!$SkipAuth -and $script:AuthToken) {
        $headers += "-H"
        $headers += "Authorization: Bearer $script:AuthToken"
    }
    
    $bodyArg = @()
    if ($Body) {
        $Body | ConvertTo-Json -Compress | Out-File -FilePath "$env:TEMP\body.json" -Encoding ascii -Force
        $bodyArg += "-d"
        $bodyArg += "@$env:TEMP\body.json"
    }
    
    try {
        $output = & curl.exe -s -w "`nHTTP_CODE:%{http_code}" --max-time 10 -X $Method $headers $bodyArg $url 2>$null
        $lines = $output -split "`n"
        $codeLine = $lines | Where-Object { $_ -match "HTTP_CODE:(\d+)" } | Select-Object -Last 1
        
        if ($codeLine -match "HTTP_CODE:(\d+)") {
            $code = [int]$matches[1]
            if ($code -eq $ExpectedCode) {
                Write-Host " ✅ PASS (HTTP $code)" -ForegroundColor $Colors.Success
                return $true
            } else {
                Write-Host " ❌ FAIL (Expected $ExpectedCode, got $code)" -ForegroundColor $Colors.Error
                return $false
            }
        }
    }
    catch {
        Write-Host " ❌ FAIL (Error: $_)" -ForegroundColor $Colors.Error
        return $false
    }
}

Write-Host "========================================" -ForegroundColor $Colors.Info
Write-Host "  FunnyEnglish API Tests" -ForegroundColor $Colors.Info
Write-Host "  Target: $BaseUrl" -ForegroundColor $Colors.Info
Write-Host "========================================" -ForegroundColor $Colors.Info
Write-Host ""

$passed = 0
$failed = 0

# Test 1: Health
if (Test-Endpoint "Health Check" "GET" "/categories" 200 $null $true) { $passed++ } else { $failed++ }

# Test 2: Login
Write-Host "Testing POST /auth/login..." -NoNewline
$loginBody = '{"email":"demo@funnyenglish.app","password":"demo123"}' | Out-File -FilePath "$env:TEMP\login.json" -Encoding ascii -Force
$loginResponse = & curl.exe -s -X POST "$BaseUrl/auth/login" -H "Content-Type: application/json" -d "@$env:TEMP\login.json" --max-time 10 2>$null | ConvertFrom-Json
if ($loginResponse.token) {
    Write-Host " ✅ PASS (Got token)" -ForegroundColor $Colors.Success
    $script:AuthToken = $loginResponse.token
    $passed++
} else {
    Write-Host " ❌ FAIL" -ForegroundColor $Colors.Error
    $failed++
}

# Test 3: Protected endpoint
if ($script:AuthToken) {
    if (Test-Endpoint "Admin endpoint" "GET" "/admin/tests") { $passed++ } else { $failed++ }
}

# Test 4: Leaderboard
if (Test-Endpoint "Leaderboard" "GET" "/leaderboard?limit=10" 200 $null $true) { $passed++ } else { $failed++ }

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor $Colors.Info
Write-Host "  Results: $passed passed, $failed failed" -ForegroundColor $(if($failed -eq 0){$Colors.Success}else{$Colors.Error})
Write-Host "========================================" -ForegroundColor $Colors.Info

exit $failed
