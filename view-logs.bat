@echo off
chcp 65001 >nul
echo ===========================================
echo   FunnyEnglish Desktop Logs Viewer
echo ===========================================
echo.

set LOG_DIR=%USERPROFILE%\.funnyenglish\logs

if not exist "%LOG_DIR%" (
    echo [INFO] Log directory not found: %LOG_DIR%
    echo [INFO] Start the application first to create logs.
    pause
    exit /b 1
)

echo Log directory: %LOG_DIR%
echo.

:: List available log files
echo Available log files:
dir /b "%LOG_DIR%\*.log" 2>nul
if errorlevel 1 (
    echo   No log files found.
    pause
    exit /b 1
)
echo.

:: Show the latest log file
for /f "delims=" %%i in ('dir /b /o-d "%LOG_DIR%\*.log"') do (
    echo ===========================================
    echo Showing latest log: %%i
    echo ===========================================
    echo.
    type "%LOG_DIR%\%%i"
    goto :end
)

:end
echo.
echo ===========================================
echo Press any key to exit...
pause >nul
