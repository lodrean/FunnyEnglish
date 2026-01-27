# FunnyEnglish

Учебное приложение для изучения английского языка с общими бизнес‑модулями,
кроссплатформенным клиентом и отдельной админ‑панелью.

## Состав проекта
- `backend` — сервер на Spring Boot (REST, безопасность, БД, миграции).
- `shared` — общий KMP‑модуль (сеть, модели, утилиты).
- `composeApp` — UI на Compose Multiplatform (Android/iOS/Desktop).
- `androidApp` — Android‑оболочка для `composeApp`.
- `admin-web` — веб‑админка на Vite/TypeScript.
- `docker` — контейнеризация и инфраструктурные файлы.

## Технологии
Kotlin Multiplatform, Jetpack/Compose Multiplatform, Spring Boot, PostgreSQL,
Flyway, Ktor Client, Koin, Vite, TypeScript.

## Быстрый запуск (опционально)
- Backend: `./gradlew :backend:bootRun`
- Desktop (Compose): `./gradlew :composeApp:run`
- Android: открыть проект в Android Studio и запустить `androidApp`
- Admin web: `cd admin-web && npm install && npm run dev`
