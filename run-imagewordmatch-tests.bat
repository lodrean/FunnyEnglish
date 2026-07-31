@echo off
chcp 65001 >nul

:: ============================================
:: Скрипт запуска тестов ImageWordMatch
:: ============================================

echo ============================================
echo ImageWordMatch Тестирование
echo ============================================
echo.

:: Проверка наличия gradlew
if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat не найден. Запустите из корня проекта.
    exit /b 1
)

:: Меню выбора
echo Выберите что запустить:
echo.
echo [1] Все ImageWordMatch тесты
echo [2] Flow тесты (основные сценарии)
echo [3] Интеграционные тесты
echo [4] Скриншот тесты
echo [5] Поведенческие тесты
echo [6] Edge Case тесты
echo [7] Page Object тесты
echo [8] Запустить с отчетом (HTML)
echo [9] Очистить результаты тестов
echo [0] Выход
echo.
set /p choice="Ваш выбор: "

if "%choice%"=="1" goto all
if "%choice%"=="2" goto flow
if "%choice%"=="3" goto integration
if "%choice%"=="4" goto screenshot
if "%choice%"=="5" goto behavior
if "%choice%"=="6" goto edgecase
if "%choice%"=="7" goto pageobject
if "%choice%"=="8" goto report
if "%choice%"=="9" goto clean
if "%choice%"=="0" goto exit

echo [ERROR] Неверный выбор
exit /b 1

:all
echo.
echo [INFO] Запуск всех ImageWordMatch тестов...
call .\gradlew.bat :composeApp:testDebugUnitTest --tests "*ImageWordMatch*" --console=plain
goto end

:flow
echo.
echo [INFO] Запуск Flow тестов...
call .\gradlew.bat :composeApp:testDebugUnitTest --tests "*ImageWordMatchFlowTest*" --console=plain
goto end

:integration
echo.
echo [INFO] Запуск Интеграционных тестов...
call .\gradlew.bat :composeApp:testDebugUnitTest --tests "*ImageWordMatchIntegrationTest*" --console=plain
goto end

:screenshot
echo.
echo [INFO] Запуск Скриншот тестов...
call .\gradlew.bat :composeApp:testDebugUnitTest --tests "*ImageWordMatchScreenshotTest*" --console=plain
goto end

:behavior
echo.
echo [INFO] Запуск Поведенческих тестов...
call .\gradlew.bat :composeApp:testDebugUnitTest --tests "*ImageWordMatchBehaviorTest*" --console=plain
goto end

:edgecase
echo.
echo [INFO] Запуск Edge Case тестов...
call .\gradlew.bat :composeApp:testDebugUnitTest --tests "*ImageWordMatchEdgeCaseTest*" --console=plain
goto end

:pageobject
echo.
echo [INFO] Запуск Page Object тестов (ImageWordMatchPage)...
call .\gradlew.bat :composeApp:testDebugUnitTest --tests "*ImageWordMatchPage*" --console=plain
goto end

:report
echo.
echo [INFO] Запуск тестов с HTML отчетом...
call .\gradlew.bat :composeApp:testDebugUnitTest --tests "*ImageWordMatch*" --console=plain
if exist "composeApp\build\reports\tests\testDebugUnitTest\index.html" (
    echo.
    echo [INFO] Открытие отчета...
    start composeApp\build\reports\tests\testDebugUnitTest\index.html
)
goto end

:clean
echo.
echo [INFO] Очистка результатов тестов...
call .\gradlew.bat :composeApp:cleanTestDebugUnitTest
echo [OK] Результаты очищены
goto end

:end
echo.
if %ERRORLEVEL% == 0 (
    echo [OK] Тесты выполнены успешно!
) else (
    echo [ERROR] Тесты завершились с ошибкой (код: %ERRORLEVEL%)
    echo.
    echo Проверьте отчет:
    echo composeApp\build\reports\tests\testDebugUnitTest\index.html
)

:exit
echo.
echo ============================================
echo Готово!
echo ============================================
pause
