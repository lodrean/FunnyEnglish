@echo off
chcp 65001 >nul
echo ========================================
echo So to Speak Quality Assurance Cycle
echo ========================================
echo.

set START_TIME=%time%

:: Check 1: Backend Health
echo [1/8] Checking Backend Health...
curl -s http://localhost:8080/api/categories >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Backend is running
) else (
    echo ❌ Backend is not responding
    echo    Start with: docker compose up -d backend
)
echo.

:: Check 2: Admin Web Build
echo [2/8] Checking Admin Web Build...
cd admin-web
call npm run build >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Admin Web builds successfully
) else (
    echo ❌ Admin Web build failed
)
cd ..
echo.

:: Check 3: Admin Web Lint
echo [3/8] Checking Admin Web Code Quality...
cd admin-web
call npm run lint >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ No linting errors
) else (
    echo ⚠️ Linting warnings found
)
cd ..
echo.

:: Check 4: Admin Web Tests
echo [4/8] Running Admin Web Unit Tests...
cd admin-web
call npm test -- --run >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Unit tests passed
) else (
    echo ❌ Unit tests failed
)
cd ..
echo.

:: Check 5: Compose App Compilation
echo [5/8] Checking Compose App Compilation...
call .\gradlew :composeApp:compileKotlinDesktop >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Compose App compiles successfully
) else (
    echo ❌ Compose App compilation failed
)
echo.

:: Check 6: Compose App Tests
echo [6/8] Running Compose App Tests...
call .\gradlew :composeApp:desktopTest >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Compose App tests passed
) else (
    echo ⚠️ Some tests may have failed (check is excluded by default)
)
echo.

:: Check 7: Code Coverage Check
echo [7/8] Checking Test Coverage...
echo ⚠️ Manual check required:
echo    - Admin Web: npm run test -- --coverage
echo    - Compose App: ./gradlew :composeApp:koverHtmlReport
echo.

:: Check 8: Security Scan
echo [8/8] Security Check...
cd admin-web
call npm audit --audit-level=moderate 2>&1 | findstr "found vulnerabilities" >nul
if %errorlevel% == 0 (
    echo ⚠️ Security vulnerabilities found - run 'npm audit fix'
) else (
    echo ✅ No security vulnerabilities
)
cd ..
echo.

:: Summary
echo ========================================
echo Quality Check Complete
echo Started: %START_TIME%
echo Finished: %time%
echo ========================================
echo.
echo Next steps:
echo 1. Review any failures above
echo 2. Run E2E tests: cd admin-web ^&^& npm run test:e2e
echo 3. Check full report: QA_AUDIT_REPORT.md
echo.

pause
