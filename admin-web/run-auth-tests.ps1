# Скрипт для запуска тестов авторизации
# Auth тесты запускаются отдельно без storageState

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Запуск тестов авторизации (Auth Tests)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Проверка наличия сервера
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -Method HEAD -TimeoutSec 5 -ErrorAction SilentlyContinue
    Write-Host "✅ Сервер admin доступен на http://localhost:3000" -ForegroundColor Green
} catch {
    Write-Host "❌ Сервер admin не доступен. Запустите: docker-compose up -d admin" -ForegroundColor Red
    exit 1
}

# Установка переменных окружения
$env:SKIP_WEB_SERVER = "true"
$env:ADMIN_URL = "http://localhost:3000"

Write-Host "Запуск auth тестов..." -ForegroundColor Yellow
Write-Host ""

# Запуск только auth тестов
npx playwright test --project=auth-tests

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ Все auth тесты пройдены!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ Некоторые auth тесты не пройдены" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
}

exit $LASTEXITCODE
