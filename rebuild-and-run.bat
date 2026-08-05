@echo off
chcp 65001 >nul
echo ===========================================
echo   Rebuild and Run So to Speak
echo ===========================================
echo.

REM Kill existing processes
echo [1/4] Stopping existing processes...
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
echo.

REM Compile backend
echo [2/4] Compiling backend...
cd /d C:\Users\etaba\IdeaProjects\projects\Packages\So to Speak
call .\gradlew :backend:classes -x test --quiet
if errorlevel 1 (
    echo [ERROR] Backend compilation failed!
    pause
    exit /b 1
)
echo [OK] Backend compiled
echo.

REM Start backend
echo [3/4] Starting backend...
start /B .\gradlew :backend:bootRun -x test --quiet > C:\Users\etaba\backend.log 2>&1
timeout /t 15 /nobreak >nul
echo [OK] Backend started
echo.

REM Check API
echo [4/4] Checking API...
curl -s http://localhost:8080/categories >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Backend not responding!
    type C:\Users\etaba\backend.log | tail -20
    pause
    exit /b 1
)
echo [OK] API responding
echo.

echo ===========================================
echo   Ready! Starting Compose Desktop...
echo ===========================================
echo.
start /B .\gradlew :composeApp:run --quiet 2>&1
echo Desktop app starting in background...
echo.
echo Logs: %USERPROFILE%\.sotospeak\logs\
echo Backend: http://localhost:8080
echo.
pause
