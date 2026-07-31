@echo off
chcp 65001 >nul
echo ==========================================
echo   FunnyEnglish Web (WASM) - Local Server
echo ==========================================
echo.

:: Build WASM distribution
echo Building WASM distribution...
call gradlew.bat :composeApp:wasmJsBrowserDistribution --quiet

if errorlevel 1 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo [OK] Build successful!
echo.

:: Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Python not found. Trying python3...
    python3 --version >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Python is required to run the web server.
        echo Please install Python from https://python.org
        pause
        exit /b 1
    )
    set PYTHON=python3
) else (
    set PYTHON=python
)

echo Starting web server on http://localhost:8081
echo Press Ctrl+C to stop
echo.

:: Serve the WASM distribution
cd composeApp\build\dist\wasmJs\browserProductionWebpack
call %PYTHON% -m http.server 8081

cd ..\..\..\..
echo.
echo Server stopped.
pause
