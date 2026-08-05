# Структура проекта So to Speak (монорепо)

- backend/ — Spring Boot 3.4.1 (Kotlin 2.1.0, Java 21), PostgreSQL+Flyway, JWT, S3/MinIO. Порт 8080.
- admin-web/ — React 18 + TS + MUI 6 + TanStack Query + Vite. Порт 3000. API-клиент: src/api/client.ts.
- composeApp/ — ОСНОВНОЙ KMP UI-модуль (android/ios/desktop/wasmJs). Монолит: app/screens/*, app/viewmodel/*, app/di/*. Точка входа desktop: com.sotospeak.app.MainKt; App.kt — KoinApplication + ручная навигация (sealed AppScreen + mutableStateOf, БЕЗ NavHost).
- app/ — тонкая Android-обёртка (SoToSpeakApplication, MainActivity).
- shared/ — KMP: api/SoToSpeakApi.kt (Ktor), model/, platform/, GuestProgressRepository.
- core/ (+core:domain, core:data, core:presentation) — инфраструктура: di/CoreModule.kt, network/HttpClientFactory.kt, Result/DataError/UiText (рабочие в core/domain/util/!), общие UI-компоненты.
- feature-api/ + feature-*/ — целевая модульная архитектура (ADR-006). НЕ подключены к composeApp; feature-tests пустой. Новый код — в composeApp, если не указано иное.
- design/ — дизайн-система KMP (M3).
- docker-compose.yml — postgres:5432, minio:9000/9001, backend:8080, admin:3000.
