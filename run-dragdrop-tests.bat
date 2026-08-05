@echo off
chcp 65001 >nul
REM ============================================
REM Запуск E2E тестов Drag-and-Drop
REM ============================================

echo ============================================
echo So to Speak: Drag-and-Drop E2E Tests
echo ============================================
echo.

REM Проверка наличия тестового изображения
if not exist "admin-web\e2e\fixtures\test-image.jpg" (
    echo [WARNING] Тестовое изображение не найдено!
    echo Создаем тестовое изображение...
    
    REM Копируем существующее изображение или создаем placeholder
    if exist "image-word-match-success.png" (
        copy "image-word-match-success.png" "admin-web\e2e\fixtures\test-image.jpg" >nul
        echo [OK] Тестовое изображение создано из существующего
    ) else (
        echo Test image placeholder > "admin-web\e2e\fixtures\test-image.jpg"
        echo [WARNING] Создан placeholder - замените на реальное изображение
    )
)

echo.
echo Выберите тип тестов:
echo 1. Playwright E2E (Admin Panel)
echo 2. Maestro E2E (Mobile App)
echo 3. Все тесты
echo.

if "%~1"=="" (
    set /p choice="Ваш выбор (1-3): "
) else (
    set choice=%~1
)

if "%choice%"=="1" goto playwright
if "%choice%"=="2" goto maestro
if "%choice%"=="3" goto all
goto end

:playwright
echo.
echo ============================================
echo Запуск Playwright E2E тестов
echo ============================================
cd admin-web

REM Проверка установки Playwright
if not exist "node_modules\@playwright" (
    echo [INFO] Установка Playwright...
    npm install
    npx playwright install chromium
)

echo.
echo Запуск тестов drag-and-drop...
npx playwright test tests/dragdrop/ --reporter=list

cd ..
goto end

:maestro
echo.
echo ============================================
echo Запуск Maestro E2E тестов
echo ============================================

echo.
echo Проверка установки Maestro...
maestro --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maestro не установлен!
    echo Установите: https://maestro.mobile.dev/getting-started/installing-maestro
    goto end
)

echo.
echo Доступные тесты:
echo 1. image_word_match_admin.yaml - Создание вопроса
echo 2. image_word_match_play.yaml - Прохождение теста
echo 3. dragdrop_comprehensive.yaml - Комплексные тесты
echo.

if "%~2"=="" (
    set /p maestro_choice="Ваш выбор (1-3): "
) else (
    set maestro_choice=%~2
)

if "%maestro_choice%"=="1" (
    maestro test maestro\flows\image_word_match_admin.yaml
)
if "%maestro_choice%"=="2" (
    maestro test maestro\flows\image_word_match_play.yaml
)
if "%maestro_choice%"=="3" (
    maestro test maestro\flows\dragdrop_comprehensive.yaml
)
goto end

:all
echo.
echo ============================================
echo Запуск всех тестов
echo ============================================
call :playwright
call :maestro
goto end

:end
echo.
echo ============================================
echo Тестирование завершено
echo ============================================
pause
