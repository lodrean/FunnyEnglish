@echo off
chcp 65001 >nul
echo ===========================================
echo   FunnyEnglish Test Suite Runner
echo ===========================================
echo.

REM Check if backend is running
echo [1/4] Checking backend...
curl -s http://localhost:8080/categories >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Backend is not running on localhost:8080
    echo.
    echo To start backend:
    echo   .\gradlew :backend:bootRun
    echo.
    pause
    exit /b 1
)
echo [OK] Backend is running
echo.

REM Load test data
echo [2/4] Loading test data...
if exist "docs\testing\test-data.sql" (
    docker exec -i funnyenglish-postgres-dev psql -U postgres -d funnyenglish < docs\testing\test-data.sql 2>nul
    if errorlevel 1 (
        echo [WARNING] Could not load test data via Docker
        echo [INFO] You can run manually:
        echo   docker exec -i funnyenglish-postgres-dev psql -U postgres -d funnyenglish ^< docs\testing\test-data.sql
    ) else (
        echo [OK] Test data loaded
    )
) else (
    echo [WARNING] test-data.sql not found
)
echo.

REM Run API tests
echo [3/4] Running API tests...
echo.
if exist "test-api.ps1" (
    powershell -ExecutionPolicy Bypass -File test-api.ps1
    if errorlevel 1 (
        echo.
        echo [WARNING] Some tests failed
    )
) else (
    echo [WARNING] test-api.ps1 not found
    echo Run manual tests or check TEST_SCENARIOS.md
)
echo.

REM Check images
echo [4/4] Checking test images...
echo.
curl -s -I http://localhost:9000/funnyenglish/thumbnails/test_colors.png | findstr /i "200 OK" >nul
if errorlevel 1 (
    echo [WARNING] Test image not accessible
    echo [INFO] Upload test images via admin panel
) else (
    echo [OK] Test image is accessible
)
echo.

echo ===========================================
echo   Test Suite Complete
echo ===========================================
echo.
echo Next steps:
echo   1. Start Compose Desktop: .\gradlew :composeApp:run
 echo   2. Open Admin Panel: http://localhost:3002
echo   3. Run manual tests from TEST_SCENARIOS.md
echo.
pause
