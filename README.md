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

## Учетные записи и переменные окружения
- Админ создается при старте backend, если задан `ADMIN_PASSWORD`.
  Дополнительно можно переопределить `ADMIN_EMAIL` и `ADMIN_DISPLAY_NAME`.
- Демо‑пользователь для мобильного клиента создается миграцией:
  `demo@funnyenglish.app` / `demo123`.

## Локальный запуск мобильного клиента
- По умолчанию debug использует `http://10.0.2.2:8080/`.
- Чтобы переопределить адрес, задайте Gradle‑свойство `FUNNYENGLISH_API_BASE_URL`
  (например, в `gradle.properties` или через `-P`).
- Для физического устройства: `http://<LAN_IP>:8080/`
- Сетевое логирование для debug включено через Ktor Logging + Napier
  (в release отключено).
- Для HTTP в Android 9+ включен cleartext в debug‑сборке
  (иначе получите ошибку `CLEARTEXT communication to localhost not permitted`).
