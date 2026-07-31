@echo off
chcp 65001 >nul
echo ==========================================
echo   FunnyEnglish CMP E2E Tests
echo ==========================================
echo.

:: Check if we're in the right directory
if not exist "e2e-cmp\package.json" (
    echo [ERROR] Please run from project root directory
    pause
    exit /b 1
)

cd e2e-cmp

:: Check if node_modules exists
if not exist "node_modules" (
    echo [INFO] Installing dependencies...
    call npm install
)

:: Check if playwright browsers are installed
if not exist "%LOCALAPPDATA%\ms-playwright" (
    echo [INFO] Installing Playwright browsers...
    call npx playwright install chromium
)

echo.
echo Choose test mode:
echo 1. Run all tests (starts WASM dev server - SLOW)
echo 2. Run with existing server (FAST - requires running server)
echo 3. Run smoke tests only
set /p choice="Enter choice (1-3): "

if "%choice%"=="1" (
    echo.
    echo [INFO] Running tests with WASM dev server...
    echo [WARN] This will take 5-10 minutes for initial compilation
    call npx playwright test
) else if "%choice%"=="2" (
    echo.
    echo [INFO] Running tests with existing server...
    set SKIP_WEB_SERVER=true
    call npx playwright test
) else if "%choice%"=="3" (
    echo.
    echo [INFO] Running smoke tests only...
    call npx playwright test tests/smoke.spec.ts
) else (
    echo [ERROR] Invalid choice
    pause
    exit /b 1
)

echo.
echo [INFO] Tests completed
echo [INFO] Report: npx playwright show-report
pause
