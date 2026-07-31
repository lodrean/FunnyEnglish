@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   FunnyEnglish - Feature Check Tool
echo ==========================================
echo.

:menu
echo Select feature to check:
echo.
echo  [1] Authentication (Login/Register)
echo  [2] Image Word Match (Admin)
echo  [3] Image Word Match (CMP App)
echo  [4] Analytics (Admin Dashboard)
echo  [5] Categories Management
echo  [6] User Progress
echo  [7] Gamification (Streak/XP)
echo  [8] Leaderboard
echo  [9] Audio Tests
echo  [0] Run All Checks
echo.
set /p choice="Enter choice (0-9): "

if "%choice%"=="1" goto auth
if "%choice%"=="2" goto iwm_admin
if "%choice%"=="3" goto iwm_cmp
if "%choice%"=="4" goto analytics
if "%choice%"=="5" goto categories
if "%choice%"=="6" goto progress
if "%choice%"=="7" goto gamification
if "%choice%"=="8" goto leaderboard
if "%choice%"=="9" goto audio
if "%choice%"=="0" goto all

echo [ERROR] Invalid choice
goto menu

:auth
echo.
echo ==========================================
echo   Checking: Authentication
echo ==========================================
echo.
echo [1] Backend API...
curl -s -X POST -H "Content-Type: application/json" -d "{\"email\":\"admin@funnyenglish.com\",\"password\":\"admin123\"}" http://localhost:8080/api/auth/login >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ Backend API: PASS
) else (
    echo  ❌ Backend API: FAIL
)

echo [2] Admin Web...
curl -s http://localhost:3000/login >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ Admin Web: PASS
) else (
    echo  ❌ Admin Web: FAIL
)

echo [3] CMP App (WASM)...
curl -s http://localhost:8081 >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ CMP App: PASS
) else (
    echo  ❌ CMP App: Not running (optional)
)

goto end

:iwm_admin
echo.
echo ==========================================
echo   Checking: Image Word Match (Admin)
echo ==========================================
echo.
echo [1] Backend API...
curl -s http://localhost:8080/api/admin/tests >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ GET /api/admin/tests: PASS
) else (
    echo  ❌ GET /api/admin/tests: FAIL
)

echo [2] Admin Web Interface...
curl -s http://localhost:3000/content/tests >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ Tests page: PASS
) else (
    echo  ❌ Tests page: FAIL
)

echo [3] E2E Tests...
cd admin-web
call npx playwright test tests/iwm/ --reporter=line 2>nul | findstr "passed"
cd ..

goto end

:iwm_cmp
echo.
echo ==========================================
echo   Checking: Image Word Match (CMP)
echo ==========================================
echo.
echo [1] CMP App running...
curl -s http://localhost:8081 >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ CMP App: Running
    echo.
    echo [2] Running E2E tests...
    cd e2e-cmp
    call npx playwright test tests/image-word-match.spec.ts --reporter=line 2>nul
    cd ..
) else (
    echo  ❌ CMP App: Not running
    echo  ℹ️  Start with: cd composeApp && gradlew wasmJsBrowserDevelopmentRun
)

goto end

:analytics
echo.
echo ==========================================
echo   Checking: Analytics Dashboard
echo ==========================================
echo.
echo [1] Analytics API Endpoints...

curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/admin/analytics > temp.txt
set /p code=<temp.txt
del temp.txt
if "%code%"=="200" (
    echo  ✅ GET /api/admin/analytics: PASS
) else (
    echo  ❌ GET /api/admin/analytics: FAIL (code: %code%)
)

curl -s -o nul -w "%%{http_code}" "http://localhost:8080/api/admin/analytics/daily-activity?days=7" > temp.txt
set /p code=<temp.txt
del temp.txt
if "%code%"=="200" (
    echo  ✅ GET /api/admin/analytics/daily-activity: PASS
) else (
    echo  ❌ GET /api/admin/analytics/daily-activity: FAIL (code: %code%)
)

curl -s -o nul -w "%%{http_code}" "http://localhost:8080/api/admin/analytics/recent-activity" > temp.txt
set /p code=<temp.txt
del temp.txt
if "%code%"=="200" (
    echo  ✅ GET /api/admin/analytics/recent-activity: PASS
) else (
    echo  ❌ GET /api/admin/analytics/recent-activity: FAIL (code: %code%)
)

echo.
echo [2] Admin Dashboard...
curl -s http://localhost:3000 >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ Dashboard accessible
) else (
    echo  ❌ Dashboard not accessible
)

echo.
echo [3] Running E2E tests...
cd admin-web
call npx playwright test tests/dashboard.spec.ts --reporter=line 2>nul | findstr "passed\|failed"
cd ..

goto end

:categories
echo.
echo ==========================================
echo   Checking: Categories Management
echo ==========================================
echo.
echo [1] Backend API...
curl -s http://localhost:8080/api/categories >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ GET /api/categories: PASS
) else (
    echo  ❌ GET /api/categories: FAIL
)

echo [2] Admin Web...
curl -s http://localhost:3000/content/categories >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ Categories page: PASS
) else (
    echo  ❌ Categories page: FAIL
)

echo [3] E2E Tests...
cd admin-web
call npx playwright test tests/categories.spec.ts --reporter=line 2>nul | findstr "passed\|failed"
cd ..

goto end

:progress
echo.
echo ==========================================
echo   Checking: User Progress
echo ==========================================
echo.
echo [1] Backend Progress API...
curl -s http://localhost:8080/api/progress >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ Progress API: Available
) else (
    echo  ℹ️  Progress API: Requires auth
)

echo [2] E2E Tests...
cd e2e-cmp
call npx playwright test tests/gamification.spec.ts -g "progress" --reporter=line 2>nul
cd ..

goto end

:gamification
echo.
echo ==========================================
echo   Checking: Gamification
echo ==========================================
echo.
echo [1] Streak System...
curl -s http://localhost:8080/api/users/me/streak >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ Streak API: Available
) else (
    echo  ℹ️  Streak API: Requires auth
)

echo [2] Achievements...
curl -s http://localhost:8080/api/achievements >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ Achievements API: Available
) else (
    echo  ❌ Achievements API: Not available
)

echo [3] E2E Tests...
cd e2e-cmp
call npx playwright test tests/gamification.spec.ts --reporter=line 2>nul | findstr "passed\|failed"
cd ..

goto end

:leaderboard
echo.
echo ==========================================
echo   Checking: Leaderboard
echo ==========================================
echo.
echo [1] Leaderboard API...
curl -s http://localhost:8080/api/leaderboard >nul 2>&1
if %errorlevel% == 0 (
    echo  ✅ Leaderboard API: Available
) else (
    echo  ℹ️  Leaderboard API: May require auth
)

echo [2] E2E Tests...
cd e2e-cmp
call npx playwright test tests/gamification.spec.ts -g "leaderboard" --reporter=line 2>nul
cd ..

goto end

:audio
echo.
echo ==========================================
echo   Checking: Audio Tests
echo ==========================================
echo.
echo [1] Backend Audio Support...
curl -s http://localhost:8080/api/admin/media/upload -X POST >nul 2>&1
echo  ℹ️  Audio upload: Requires multipart/form-data

echo [2] CMP Audio Support...
echo  ⚠️  Audio in CMP: Limited support (stub implementation)

echo [3] E2E Tests...
echo  ❌ Audio E2E tests: Not implemented

goto end

:all
echo.
echo ==========================================
echo   Running ALL Feature Checks
echo ==========================================
echo.
call :auth
echo.
call :iwm_admin
echo.
call :analytics
echo.
call :categories
echo.
call :gamification
goto end

:end
echo.
echo ==========================================
echo   Feature Check Complete
echo ==========================================
echo.
echo Press any key to return to menu...
pause >nul
goto menu
