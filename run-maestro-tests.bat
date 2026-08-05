@echo off
chcp 65001 >nul
echo ===========================================
echo So to Speak Maestro E2E Test Runner
echo ===========================================
echo.

REM Check if Maestro is installed
where maestro >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Maestro not found!
    echo Please install Maestro from: https://maestro.mobile.dev/getting-started/installing-maestro
    exit /b 1
)

echo ✅ Maestro found

REM Check if device is connected
adb devices | findstr /R /C:"device$" >nul
if %ERRORLEVEL% neq 0 (
    echo ❌ No Android device connected!
    echo Please connect a device or start an emulator.
    exit /b 1
)

echo ✅ Android device connected

REM Check if app is installed
adb shell pm list packages | findstr "com.sotospeak.app" >nul
if %ERRORLEVEL% neq 0 (
    echo ⚠️  App not installed. Building...
    call gradlew :composeApp:assembleDebug
    if %ERRORLEVEL% neq 0 (
        echo ❌ Build failed!
        exit /b 1
    )
    echo Installing app...
    adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
)

echo ✅ App installed
echo.
echo ===========================================
echo Running Maestro Tests
echo ===========================================
echo.

setlocal enabledelayedexpansion
set PASSED=0
set FAILED=0

REM Run each test flow
for %%F in (
    .maestro\flows\login.yaml
    .maestro\flows\home_navigation.yaml
    .maestro\flows\design_system_check.yaml
    .maestro\flows\leaderboard_view.yaml
    .maestro\flows\gamification_streak.yaml
    .maestro\flows\complete_test.yaml
) do (
    if exist "%%F" (
        echo.
        echo Running: %%F
        maestro test "%%F"
        if !ERRORLEVEL! equ 0 (
            echo ✅ PASSED: %%F
            set /a PASSED+=1
        ) else (
            echo ❌ FAILED: %%F
            set /a FAILED+=1
        )
    ) else (
        echo ⚠️  Not found: %%F
    )
)

echo.
echo ===========================================
echo Test Summary
echo ===========================================
echo Passed: %PASSED%
echo Failed: %FAILED%
echo.

if %FAILED% gtr 0 (
    echo ❌ Some tests failed!
    exit /b 1
) else (
    echo ✅ All tests passed!
    exit /b 0
)
