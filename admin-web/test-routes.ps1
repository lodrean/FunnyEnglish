# FunnyEnglish Admin Panel - Route Testing Script
# Run after: docker compose up -d
# 
# Usage: .\test-routes.ps1

$baseUrl = "http://localhost:3000"
$apiUrl = "http://localhost:8080"

Write-Host "=== FunnyEnglish Admin Navigation Test ===" -ForegroundColor Cyan
Write-Host ""

# Test credentials
$loginBody = @{
    email = "admin@funnyenglish.com"
    password = "admin123"
} | ConvertTo-Json

Write-Host "1. Testing API Health..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$apiUrl/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "   ✓ Backend is running" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Backend not responding. Is docker running?" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. Testing Login API..." -ForegroundColor Yellow
try {
    $headers = @{ "Content-Type" = "application/json" }
    $response = Invoke-RestMethod -Uri "$apiUrl/auth/login" -Method POST -Body $loginBody -Headers $headers
    $token = $response.token
    Write-Host "   ✓ Login successful" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Login failed: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "3. Navigation Routes Checklist:" -ForegroundColor Yellow
Write-Host "   (Open these URLs in browser after docker is running)" -ForegroundColor Gray
Write-Host ""

$routes = @(
    @{ Path = "/"; Name = "Dashboard" },
    @{ Path = "/content/categories"; Name = "Categories" },
    @{ Path = "/content/tests"; Name = "Tests" },
    @{ Path = "/content/questions"; Name = "Questions" },
    @{ Path = "/tests/new"; Name = "New Test" },
    @{ Path = "/users"; Name = "Users" },
    @{ Path = "/users/students"; Name = "Students" },
    @{ Path = "/users/admins"; Name = "Admins" },
    @{ Path = "/users/groups"; Name = "Groups" },
    @{ Path = "/analytics/reports"; Name = "Reports" },
    @{ Path = "/analytics/statistics"; Name = "Statistics" },
    @{ Path = "/settings"; Name = "Settings" }
)

foreach ($route in $routes) {
    Write-Host "   [ ] $($route.Name) - $baseUrl$($route.Path)" -ForegroundColor White
}

Write-Host ""
Write-Host "=== Test Instructions ===" -ForegroundColor Cyan
Write-Host "1. Run: docker compose up -d" -ForegroundColor White
Write-Host "2. Wait 30 seconds for services to start" -ForegroundColor White
Write-Host "3. Open: http://localhost:3000" -ForegroundColor White
Write-Host "4. Login with: admin@funnyenglish.com / admin123" -ForegroundColor White
Write-Host "5. Click each menu item in sidebar" -ForegroundColor White
Write-Host "6. Verify no 404 errors in browser console" -ForegroundColor White
Write-Host ""
Write-Host "=== Quick Commands ===" -ForegroundColor Cyan
Write-Host "Start:  docker compose up -d" -ForegroundColor Green
Write-Host "Logs:   docker compose logs -f admin" -ForegroundColor Green
Write-Host "Stop:   docker compose down" -ForegroundColor Green
Write-Host ""
