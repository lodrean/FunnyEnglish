# FunnyEnglish Docker Setup

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
| Admin Panel | http://localhost:3000 | `admin@funnyenglish.com` / `admin123` |
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
docker compose exec postgres psql -U postgres -d funnyenglish

# Выполнить SQL запрос
docker compose exec postgres psql -U postgres -d funnyenglish -c "SELECT * FROM users;"

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

## 📦 Production (Prod)

### Пример docker-compose.prod.yml

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: funnyenglish
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
      DB_NAME: funnyenglish
      DB_USERNAME: ${DB_USER:-postgres}
      DB_PASSWORD: ${DB_PASSWORD}
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate
      SPRING_FLYWAY_ENABLED: true
      S3_ENDPOINT: http://minio:9000
      S3_ACCESS_KEY: ${S3_ACCESS_KEY}
      S3_SECRET_KEY: ${S3_SECRET_KEY}
      S3_BUCKET: ${S3_BUCKET:-funnyenglish}
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

# S3/MinIO Storage
S3_ACCESS_KEY=your-minio-access-key
S3_SECRET_KEY=your-minio-secret-key
S3_BUCKET=funnyenglish

# JWT Secret (минимум 32 символа)
JWT_SECRET=your-super-secret-jwt-key-change-in-production-min-32-chars

# Admin User
ADMIN_EMAIL=admin@yourdomain.com
ADMIN_PASSWORD=your-secure-admin-password

# CORS
CORS_ORIGINS=https://admin.yourdomain.com
```

### Production Security Checklist

- [ ] PostgreSQL не доступен извне (нет `ports:`)
- [ ] MinIO не доступен извне (только через backend)
- [ ] Сильные пароли (32+ символов для JWT_SECRET)
- [ ] SSL/TLS сертификаты (Let's Encrypt или свой)
- [ ] `.env` файл не в git (добавлен в .gitignore)
- [ ] Регулярные бэкапы БД
- [ ] `DDL_AUTO: validate` (не `create` или `update`)

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
