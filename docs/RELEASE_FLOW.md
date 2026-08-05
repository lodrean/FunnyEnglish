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
3. На сервере: `/opt/sotospeak/docker-compose.prod.yml` + `.env` (см. `docker/.env.example`). Секреты НЕ в git.
4. Android-релиз: тег `v*` → `android-release.yml` → артефакты APK/AAB.

## Staging (приёмка перед prod)
Перед релизом фичи прогоняются на изолированном staging-окружении (`docker-compose.staging.yml`,
проект `sotospeak-staging`): свои БД/MinIO, `ddl-auto=validate` (как prod), Mailpit для почты.
Runbook и порты — секция «Staging» в `DOCKER.md`; seed-контент — `scripts/seed-speaking-content.sh`;
сценарий приёмки MVP — `docs/qa/MVP_ACCEPTANCE.md`. Порядок: зелёные тесты → staging (up + seed +
приёмка по чек-листу) → go владельца → деплой в prod (шаги 2–3 выше).

## TLS (HTTPS)
Prod-стек включает **Caddy** (`docker/Caddyfile`) — единственная публичная точка входа (80/443):
- `ADMIN_HOST` → admin:80, `API_HOST` → backend:8080, `MEDIA_HOST` → minio:9000;
- сертификаты Let's Encrypt получаются и продлеваются автоматически (нужны DNS A-записи + `ACME_EMAIL` в .env);
- backend/admin/minio наружу не проброшены (только `expose` во внутренней сети).

## Object Storage (S3)
- Конфигурация через env: `S3_ENDPOINT`, `S3_REGION`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`, `S3_PUBLIC_URL`.
- **Дефолт** — MinIO в compose-стеке (`S3_ENDPOINT` не задавать), публикация файлов через Caddy по `MEDIA_HOST`.
- **Внешний российский S3** (Yandex Cloud / Selectel / VK Cloud) — задайте `S3_ENDPOINT`/`S3_REGION` в `.env`
  (готовые примеры endpoint'ов — в `docker/.env.example`).
- `S3_PUBLIC_URL` — публичный URL медиа (обычно CDN-домен перед бакетом, напр. `https://cdn.yourdomain.com/sotospeak`);
  backend отдаёт его клиентам в `videoUrl`/`subtitleUrl`/`audioUrl` (BUG-004).
- Лимиты: видео до **200 МБ** (nginx `client_max_body_size 200m` + `multipart.max-file-size=200MB`),
  practice-аудио ≤ 5 МБ (валидация в backend).

## Необходимые GitHub secrets
- Деплой: `SERVER_HOST`, `SERVER_USER`, `SERVER_SSH_KEY`, `SERVER_URL`
- Android: `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `API_BASE_URL`
