# So to Speak Docker Setup

## 🚀 Быстрая шпаргалка

```bash
# После изменений в Backend (Kotlin/Spring Boot)
./gradlew :backend:bootJar -x test
docker compose up -d --build backend

# После изменений в Admin Web (React/TypeScript)
docker compose up -d --build admin

# После добавления SQL миграций
./gradlew :backend:bootJar -x test
docker compose up -d --build backend

# Или просто перезапуск (если JAR уже собран)
docker compose restart backend

# Полный перезапуск всего
./gradlew :backend:bootJar -x test
docker compose up -d --build

# Полный сброс (удалит данные БД!)
docker compose down -v
docker compose up -d --build
```

## 🌐 URL и доступы

| Сервис | URL | Credentials |
|--------|-----|-------------|
| Backend API | http://localhost:8080/api | - |
| Admin Panel | http://localhost:3000 | `admin@sotospeak.com` / `admin123` |
| MinIO Console | http://localhost:9001 | `minioadmin` / `minioadmin` |
| PostgreSQL | localhost:5432 | `postgres` / `postgres` |

## 📋 Полезные команды

```bash
# Статус всех сервисов
docker compose ps

# Логи в реальном времени
docker compose logs -f backend      # бэкенд
docker compose logs -f postgres     # база данных
docker compose logs -f minio        # хранилище файлов

# Перезапуск одного сервиса
docker compose restart backend
docker compose restart postgres
docker compose restart minio

# Остановка (сохранить данные)
docker compose down

# Подключиться к базе данных
docker compose exec postgres psql -U postgres -d sotospeak

# Выполнить SQL запрос
docker compose exec postgres psql -U postgres -d sotospeak -c "SELECT * FROM users;"

# Очистка неиспользуемых образов
docker system prune -f
```

## 🔧 Первый запуск (чистая установка)

```bash
# 1. Собрать JAR
./gradlew :backend:bootJar -x test

# 2. Запустить всё
./gradlew :backend:bootJar -x test && docker compose up -d --build

# 3. Проверить
sleep 10
docker compose ps
docker compose logs backend --tail 20
```

## 🔍 Отладка проблем

```bash
# Бэкенд не стартует - посмотреть ошибки
docker compose logs backend | tail -50

# Проверить миграции БД
docker compose logs backend | grep -i flyway

# БД не доступна - проверить postgres
docker compose logs postgres

# Проверить подключение к БД из контейнера
docker compose exec backend nc -zv postgres 5432
```

## ☁️ Object Storage (S3)

Видео, субтитры (WebVTT), обложки и practice-аудио хранятся в S3-совместимом хранилище.
Backend настраивается через env-переменные:

| Переменная | Назначение | Дефолт (dev) |
|---|---|---|
| `S3_ENDPOINT` | Endpoint S3 API | `http://localhost:9000` (MinIO) |
| `S3_REGION` | Регион | `us-east-1` |
| `S3_ACCESS_KEY` / `S3_SECRET_KEY` | Ключи доступа (обязательны, дефолта нет) | — |
| `S3_BUCKET` | Имя бакета | `sotospeak` |
| `S3_PUBLIC_URL` | Публичный URL медиа (обычно CDN-домен перед бакетом) | `S3_ENDPOINT` |

**Дефолт — MinIO** в compose-стеке (dev: `docker-compose.yml`, prod: `docker-compose.prod.yml`),
наружу не проброшен — публикация файлов идёт через Caddy по `MEDIA_HOST` (`S3_PUBLIC_URL`).

**Внешний российский S3** (Yandex Cloud / Selectel / VK Cloud) — раскомментируйте
`S3_ENDPOINT`/`S3_REGION` в `.env` (примеры в `docker/.env.example`) и задайте
`S3_PUBLIC_URL` на CDN-домен перед бакетом, например:

```bash
S3_ENDPOINT=https://storage.yandexcloud.net
S3_REGION=ru-central1
S3_PUBLIC_URL=https://cdn.yourdomain.com/sotospeak
```

`S3_PUBLIC_URL` важен (BUG-004): backend отдаёт клиентам именно публичные URL,
доступные с устройств, а не внутренний endpoint контейнера.

**Лимиты загрузки:** practice-аудио ≤ 5 МБ (валидация в backend);
видео — до **200 МБ** (`client_max_body_size 200m` в `docker/nginx.conf` +
`spring.servlet.multipart.max-file-size=200MB` в backend).

## 📦 Production (Prod)

### Пример docker-compose.prod.yml

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: sotospeak
      POSTGRES_USER: ${DB_USER:-postgres}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    # Не expose порт наружу!
    
  minio:
    image: minio/minio:latest
    environment:
      MINIO_ROOT_USER: ${S3_ACCESS_KEY}
      MINIO_ROOT_PASSWORD: ${S3_SECRET_KEY}
    command: server /data
    volumes:
      - minio_data:/data
    # Не expose порт наружу!
    
  backend:
    build:
      context: .
      dockerfile: docker/Dockerfile.backend
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: sotospeak
      DB_USERNAME: ${DB_USER:-postgres}
      DB_PASSWORD: ${DB_PASSWORD}
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate
      SPRING_FLYWAY_ENABLED: true
      S3_ENDPOINT: http://minio:9000
      S3_REGION: ${S3_REGION:-us-east-1}
      S3_ACCESS_KEY: ${S3_ACCESS_KEY}
      S3_SECRET_KEY: ${S3_SECRET_KEY}
      S3_BUCKET: ${S3_BUCKET:-sotospeak}
      # Публичный URL медиа для клиентов (CDN-домен через Caddy → MinIO), см. раздел S3 выше
      S3_PUBLIC_URL: ${S3_PUBLIC_URL}
      JWT_SECRET: ${JWT_SECRET}
      ADMIN_EMAIL: ${ADMIN_EMAIL}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      CORS_ORIGINS: ${CORS_ORIGINS}
    # Не expose порт наружу! Используй reverse proxy

volumes:
  postgres_data:
  minio_data:
```

### Required Environment Variables (Prod)

Создать `.env` файл в корне:

```bash
# Database
DB_PASSWORD=your-strong-password-here-min-16-chars

# S3/MinIO Storage (подробности — раздел «Object Storage (S3)» выше)
# Для внешнего S3 (Yandex/Selectel/VK) добавьте S3_ENDPOINT и S3_REGION
S3_ACCESS_KEY=your-minio-access-key
S3_SECRET_KEY=your-minio-secret-key
S3_BUCKET=sotospeak
# Публичный URL медиа (CDN-домен перед бакетом)
S3_PUBLIC_URL=https://cdn.yourdomain.com/sotospeak

# JWT Secret (минимум 32 символа)
JWT_SECRET=your-super-secret-jwt-key-change-in-production-min-32-chars

# Admin User
ADMIN_EMAIL=admin@yourdomain.com
ADMIN_PASSWORD=your-secure-admin-password

# CORS
CORS_ORIGINS=https://admin.yourdomain.com

# Email-верификация (OpenSpec add-email-verification)
EMAIL_VERIFICATION_ENABLED=true
# SMTP-провайдер (Yandex 360 для домена / Unisender / SendPulse)
SPRING_MAIL_HOST=smtp.yandex.ru
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=noreply@yourdomain.com
SPRING_MAIL_PASSWORD=your-smtp-password
SPRING_MAIL_AUTH=true
SPRING_MAIL_STARTTLS=true
MAIL_FROM=noreply@yourdomain.com
# Публичный base URL API для ссылок в письмах (БЕЗ /api)
PUBLIC_APP_URL=https://api.yourdomain.com
```

> Для доставляемости писем настройте SPF/DKIM домена у почтового провайдера,
> иначе письма верификации будут падать в спам.

### Production Security Checklist

- [ ] PostgreSQL не доступен извне (нет `ports:`)
- [ ] MinIO не доступен извне (только через backend)
- [ ] Сильные пароли (32+ символов для JWT_SECRET)
- [ ] SSL/TLS сертификаты (Let's Encrypt или свой)
- [ ] `.env` файл не в git (добавлен в .gitignore)
- [ ] Регулярные бэкапы БД
- [ ] `DDL_AUTO: validate` (не `create` или `update`)

## 🧪 Staging (тестовое окружение)

Изолированное окружение для приёмки MVP (`docker-compose.staging.yml`, проект
`sotospeak-staging`): свои volume'ы PostgreSQL/MinIO (данные не пересекаются
с dev/prod), `ddl-auto=validate` (как prod), **Mailpit** (SMTP-ловушка для
email-верификации), `DEMO_USER_ENABLED=true`, ослабленные rate-limit'ы.

```bash
# Подготовка
cp docker/.env.staging.example docker/.env.staging   # заполнить значения!
./gradlew :backend:bootJar

# Запуск
docker compose -f docker-compose.staging.yml --env-file docker/.env.staging up -d --build

# Статус / логи
docker compose -f docker-compose.staging.yml ps
docker compose -f docker-compose.staging.yml logs -f backend

# Остановка / полный сброс данных staging
docker compose -f docker-compose.staging.yml down
docker compose -f docker-compose.staging.yml down -v
```

### URL и порты staging (не конфликтуют с dev/prod на той же машине)

| Сервис | URL |
|--------|-----|
| Backend API | http://localhost:8180/api |
| Admin Panel | http://localhost:3100 |
| Mailpit UI (перехваченные письма) | http://localhost:8125 |
| MinIO Console | http://localhost:9101 |
| PostgreSQL | localhost:5433 |

### Вариант с поддоменами через общий Caddy (на сервере)

Если staging живёт на том же сервере, что и prod (Caddy держит 80/443),
порты из `docker-compose.staging.yml` НЕ пробрасывать наружу, а в `docker/Caddyfile`
добавить блоки staging-хостов (DNS A-записи на тот же сервер):

```
{$STAGING_ADMIN_HOST} {
	reverse_proxy sotospeak-staging-admin:80
}
{$STAGING_API_HOST} {
	reverse_proxy sotospeak-staging-backend:8080
}
{$STAGING_MEDIA_HOST} {
	reverse_proxy sotospeak-staging-minio:9000
}
```

Для этого staging-контейнеры должны быть в одной docker-сети с Caddy
(external network), `S3_PUBLIC_URL=https://<STAGING_MEDIA_HOST>/sotospeak`.

### APK под staging

```bash
# Эмулятор
./gradlew :app:assembleDebug -PSOTOSPEAK_API_BASE_URL=http://10.0.2.2:8180/
# Реальное устройство (LAN-IP сервера)
./gradlew :app:assembleDebug -PSOTOSPEAK_API_BASE_URL=http://192.168.x.x:8180/
```

## 🐛 Troubleshooting

### Порт уже занят
```bash
# Windows - найти процесс
netstat -ano | findstr :5432

# Linux/macOS
lsof -i :5432

# Использовать другой порт в docker-compose.yml
# Изменить "5432:5432" на "5434:5432"
```

### Миграции не применились
```bash
# Проверить статус миграций
docker compose logs backend | grep -i "flyway\|migration"

# Перезапустить бэкенд (применит миграции)
docker compose restart backend
```

### Проблемы с правами MinIO
```bash
docker compose exec minio chmod 777 /data
docker compose restart minio
```

### Полный сброс (осторожно!)
```bash
# Удалит ВСЕ данные: БД, файлы, всё!
docker compose down -v

# Запустить заново
./gradlew :backend:bootJar -x test
docker compose up -d --build
```
