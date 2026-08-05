@echo off
chcp 65001 >nul
echo 🚀 Запуск So to Speak WASM Dev Server...
echo.

:: Check if distribution exists
if not exist "composeApp\build\dist\wasmJs\productionExecutable\index.html" (
    echo ❌ WASM distribution not found. Building first...
    call .\gradlew :composeApp:wasmJsBrowserDistribution --no-configuration-cache
    if errorlevel 1 (
        echo ❌ Build failed!
        exit /b 1
    )
)

echo ✅ WASM distribution found
echo 📂 Location: composeApp\build\dist\wasmJs\productionExecutable\
echo 🌐 Opening http://localhost:8080
echo.
echo Press Ctrl+C to stop
echo.

:: Start Python HTTP server
cd composeApp\build\dist\wasmJs\productionExecutable
python -m http.server 8080
