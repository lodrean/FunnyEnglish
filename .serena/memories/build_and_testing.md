# Сборка и тестирование FunnyEnglish

- Android APK: ./gradlew :app:assembleDebug
- Desktop: ./gradlew :composeApp:run
- KMP-тесты: ./gradlew :composeApp:desktopTest (kotest 5.8), UI: ./gradlew :composeApp:uiTest
- ВНИМАНИЕ: detekt объявлен (apply false), но не подключён ни к одному модулю — ./gradlew lint НЕ является quality gate.
- Backend: ./gradlew :backend:bootRun (нужен PostgreSQL на 5432)
- Admin: cd admin-web && npm run dev / npm test (vitest) / npm run test:e2e (playwright)
- Всё вместе: docker compose up -d
- E2E CMP: cd e2e-cmp && npx playwright test; мобильные флоу: maestro test .maestro/
- Base URL API: Android — property FUNNYENGLISH_API_BASE_URL (дефолт http://10.0.2.2:8080/); Desktop — env FUNNYENGLISH_API_BASE_URL (дефолт http://localhost:8080).
- Версии: Kotlin 2.1.0, AGP 8.12.0, CMP 1.7.1, Ktor 3.0.2, Koin 4.0.0, Gradle 8.13, compileSdk 35, minSdk 24.
