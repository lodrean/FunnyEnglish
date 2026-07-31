# Release Flow (2026-07-20)

## Ветки
- `develop` — основная разработка, сюда идут PR.
- `main` — релизная ветка. Мердж develop → main = релиз.

## CI-пайплайны (кто когда запускается)
| Workflow | Триггер | Назначение |
|---|---|---|
| `ci.yml` | push/PR в main, develop | Основной gate: backend-test, shared-test, android-build, admin-web-test, docker-build |
| `quality-check.yml` | push/PR + schedule | Линт/качество + security-scan |
| `qa-automation.yml` | push/PR | Unit + API (Newman) + visual |
| `cmp-e2e-tests.yml` | push/PR | E2E WASM-приложения (Playwright) |
| `deploy.yml` | push в main | Сборка образов → ghcr → деплой на сервер + health check |
| `android-release.yml` | tag `v*` / ручной | Подписанные APK/AAB из `:app` (нужны secrets: ANDROID_KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD, API_BASE_URL) |
| `chromatic.yml` | — | Визуальные снапшоты Storybook |
| ~~`tests.yml`~~ | только workflow_dispatch | DEPRECATED: дублирует ci.yml, удалить после валидации |

## Деплой
1. PR в develop → зелёный ci.yml.
2. Мердж develop → main → `deploy.yml`: bootJar → docker build/push (ghcr) → ssh на сервер → `docker compose pull && up -d` → health `${SERVER_URL}/api/actuator/health`.
3. На сервере: `/opt/funnyenglish/docker-compose.prod.yml` + `.env` (см. `docker/.env.example`). Секреты НЕ в git.
4. Android-релиз: тег `v*` → `android-release.yml` → артефакты APK/AAB.

## TLS (HTTPS)
Prod-стек включает **Caddy** (`docker/Caddyfile`) — единственная публичная точка входа (80/443):
- `ADMIN_HOST` → admin:80, `API_HOST` → backend:8080, `MEDIA_HOST` → minio:9000;
- сертификаты Let's Encrypt получаются и продлеваются автоматически (нужны DNS A-записи + `ACME_EMAIL` в .env);
- backend/admin/minio наружу не проброшены (только `expose` во внутренней сети).

## Необходимые GitHub secrets
- Деплой: `SERVER_HOST`, `SERVER_USER`, `SERVER_SSH_KEY`, `SERVER_URL`
- Android: `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `API_BASE_URL`
