@echo off
chcp 65001 >nul
echo ===========================================
echo So to Speak Playwright E2E Test Runner
echo ===========================================
echo.

cd admin-web

REM Check if node_modules exists
if not exist "node_modules\" (
    echo 📦 Installing dependencies...
    call npm install
    if %ERRORLEVEL% neq 0 (
        echo ❌ Failed to install dependencies!
        exit /b 1
    )
)

echo ✅ Dependencies installed

REM Set environment variables
set SKIP_WEB_SERVER=true
set ADMIN_URL=http://localhost:5173

echo.
echo ===========================================
echo Running Playwright Tests
echo ===========================================
echo.

if "%1"=="headed" (
    echo Running in headed mode...
    call npx playwright test --headed --reporter=list
) else (
    echo Running in headless mode...
    call npx playwright test --project=chromium-headless --reporter=list
)

if %ERRORLEVEL% neq 0 (
    echo.
    echo ❌ Some tests failed!
    echo.
    echo To view report, run: npx playwright show-report
    exit /b 1
) else (
    echo.
    echo ✅ All tests passed!
    exit /b 0
)
