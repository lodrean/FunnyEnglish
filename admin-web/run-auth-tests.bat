@echo off
chcp 65001 >nul
echo ========================================
echo Запуск тестов авторизации (Auth Tests)
echo ========================================
echo.

REM Проверка наличия сервера
curl -s -o nul -w "%%{http_code}" http://localhost:3000 > %TEMP%\server_check.txt
set /p SERVER_STATUS=<%TEMP%\server_check.txt
del %TEMP%\server_check.txt

if "%SERVER_STATUS%"=="200" (
    echo ✅ Сервер admin доступен на http://localhost:3000
) else (
    echo ❌ Сервер admin не доступен. Запустите: docker-compose up -d admin
    exit /b 1
)

echo.
echo Запуск auth тестов...
echo.

REM Установка переменных окружения
set SKIP_WEB_SERVER=true
set ADMIN_URL=http://localhost:3000

REM Запуск только auth тестов
call npx playwright test --project=auth-tests

if %ERRORLEVEL% == 0 (
    echo.
    echo ========================================
    echo ✅ Все auth тесты пройдены!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo ❌ Некоторые auth тесты не пройдены
    echo ========================================
)

exit /b %ERRORLEVEL%
