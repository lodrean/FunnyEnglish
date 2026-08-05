#!/usr/bin/env powershell
#Requires -Version 5.1

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "🚀 Запуск So to Speak WASM Dev Server..." -ForegroundColor Cyan
Write-Host ""

# Check if distribution exists
$distPath = "composeApp\build\dist\wasmJs\productionExecutable"
$indexPath = Join-Path $distPath "index.html"

if (-not (Test-Path $indexPath)) {
    Write-Host "❌ WASM distribution not found. Building first..." -ForegroundColor Yellow
    
    $buildResult = Start-Process -FilePath ".\gradlew" -ArgumentList ":composeApp:wasmJsBrowserDistribution", "--no-configuration-cache" -Wait -PassThru
    
    if ($buildResult.ExitCode -ne 0) {
        Write-Host "❌ Build failed!" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✅ WASM distribution found" -ForegroundColor Green
Write-Host "📂 Location: $distPath" -ForegroundColor Gray
Write-Host "🌐 Opening http://localhost:8080" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow
Write-Host ""

# Start Python HTTP server
Set-Location $distPath
try {
    python -m http.server 8080
} finally {
    Set-Location $PSScriptRoot
}
