# 🚀 So to Speak на Timeweb VPS - Полная Инструкция

> **Цель:** Развернуть весь проект (Backend + Admin + PostgreSQL + MinIO) на VPS за 600₽/мес  
> **Время:** ~45 минут  
> **Результат:** Работающий https://admin.sotospeak.ru и https://api.sotospeak.ru

---

## 📋 Что Будем Разворачивать

```
┌─────────────────────────────────────────────────────────────┐
│  Timeweb VPS (2 vCPU, 2GB RAM, 30GB NVMe)                   │
│  Ubuntu 22.04 LTS                                           │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │    Nginx    │  │   Backend   │  │   Admin     │         │
│  │    :80      │  │   :8080     │  │   :3000     │         │
│  │    :443     │  │  (Spring)   │  │   (React)   │         │
│  └──────┬──────┘  └──────┬──────┘  └─────────────┘         │
│         │                │                                   │
│  ┌──────┴────────────────┴─────────────┐                    │
│  │         Docker Network              │                    │
│  └─────────────────────────────────────┘                    │
│         │                │                                   │
│  ┌──────┴──────┐  ┌──────┴──────┐                          │
│  │ PostgreSQL  │  │    MinIO    │                          │
│  │    :5432    │  │  :9000/9001 │                          │
│  └─────────────┘  └─────────────┘                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Часть 1: Регистрация и Создание VPS (5 минут)

### Шаг 1.1: Регистрация в Timeweb Cloud

1. Откройте https://timeweb.cloud
2. Нажмите "Зарегистрироваться"
3. Введите email и пароль
4. Подтвердите email

### Шаг 1.2: Создание VPS

1. В панели управления нажмите **"Создать сервер"**
2. Выберите конфигурацию:
   ```
   Тип: Облачный сервер
   Локация: Москва (или ближайшая)
   Операционная система: Ubuntu 22.04 LTS
   Тариф: Мощность (2 vCPU, 2GB RAM, 30GB NVMe)
   ```
3. Дополнительные опции:
   ```
   ✅ Автоматические бэкапы (рекомендуется)
   ✅ Защита от DDoS (бесплатно)
   ```
4. Нажмите **"Создать"**
5. Ждите 2-3 минуты пока сервер создаётся

### Шаг 1.3: Получение Доступа

После создания вам выдадут:
```
IP адрес: 185.XXX.XXX.XXX
Логин: root
Пароль: ******** (скопируйте!)
```

**Сохраните эти данные!**

---

## Часть 2: Подготовка VPS (10 минут)

### Шаг 2.1: Подключение по SSH

На Windows:
```powershell
# Используйте PowerShell или Git Bash
ssh root@185.XXX.XXX.XXX
# Введите пароль при запросе
```

На Mac/Linux:
```bash
ssh root@185.XXX.XXX.XXX
```

### Шаг 2.2: Обновление Системы

```bash
# Обновляем пакеты
apt update && apt upgrade -y

# Устанавливаем базовые утилиты
apt install -y \
    curl \
    wget \
    git \
    vim \
    htop \
    ufw \
    fail2ban \
    certbot \
    python3-certbot-nginx
```

### Шаг 2.3: Настройка Swap (Критично для 2GB RAM!)

```bash
# Создаём 2GB swap файл
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile

# Делаем swap постоянным
echo '/swapfile none swap sw 0 0' | tee -a /etc/fstab

# Проверяем
swapon --show
free -h
```

Вывод должен показать swap:
```
NAME      TYPE SIZE USED PRIO
/swapfile file   2G   0B   -2
```

### Шаг 2.4: Оптимизация для PostgreSQL (2GB RAM)

```bash
# Настройки ядра для работы с PostgreSQL
cat >> /etc/sysctl.conf << EOF
# PostgreSQL optimizations
vm.swappiness=10
vm.overcommit_memory=2
vm.overcommit_ratio=80
kernel.shmmax=536870912
kernel.shmall=131072
EOF

sysctl -p
```

### Шаг 2.5: Установка Docker

```bash
# Удаляем старые версии
apt remove -y docker docker-engine docker.io containerd runc

# Устанавливаем Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Добавляем пользователя в группу docker
usermod -aG docker root
newgrp docker

# Проверяем
docker --version
# Docker version 24.0.x, build xxx

# Устанавливаем Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### Шаг 2.6: Настройка Firewall

```bash
# Разрешаем SSH, HTTP, HTTPS
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS

# Включаем firewall
ufw --force enable

# Проверяем статус
ufw status verbose
```

### Шаг 2.7: Настройка Fail2ban (Защита от брутфорса)

```bash
# Конфигурация SSH защиты
cat > /etc/fail2ban/jail.local << EOF
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 3

[sshd]
enabled = true
port = ssh
filter = sshd
logpath = /var/log/auth.log
maxretry = 3
EOF

# Перезапускаем
systemctl restart fail2ban
systemctl enable fail2ban

# Проверяем статус
fail2ban-client status
```

---

## Часть 3: Сборка и Деплой Проекта (15 минут)

### Шаг 3.1: Создание Структуры Проекта

```bash
# Создаём директорию проекта
mkdir -p /opt/sotospeak
cd /opt/sotospeak

# Создаём необходимые директории
mkdir -p {nginx,postgres-init,certbot/conf,certbot/www,backups}
mkdir -p monitoring/{prometheus,grafana}
```

### Шаг 3.2: Создание Environment Файла

```bash
# Генерируем случайные пароли
DB_PASSWORD=$(openssl rand -base64 32)
MINIO_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 64)
ADMIN_PASSWORD="admin123"  # Измените после первого входа!
GRAFANA_PASSWORD=$(openssl rand -base64 16)

# Создаём .env файл
cat > /opt/sotospeak/.env << EOF
# Database
DB_PASSWORD=${DB_PASSWORD}
DB_NAME=sotospeak
DB_USER=postgres

# MinIO S3
MINIO_ROOT_USER=minioadmin
MINIO_PASSWORD=${MINIO_PASSWORD}
MINIO_BUCKET=sotospeak

# Backend
JWT_SECRET=${JWT_SECRET}
ADMIN_EMAIL=admin@sotospeak.com
ADMIN_PASSWORD=${ADMIN_PASSWORD}
ADMIN_DISPLAY_NAME=Admin

# Monitoring
GRAFANA_PASSWORD=${GRAFANA_PASSWORD}

# Domains (замените на свои!)
API_DOMAIN=api.sotospeak.ru
ADMIN_DOMAIN=admin.sotospeak.ru
EOF

# Сохраняем пароли в надёжное место!
echo "Database Password: ${DB_PASSWORD}"
echo "MinIO Password: ${MINIO_PASSWORD}"
echo "Admin Password: ${ADMIN_PASSWORD}"
echo "Grafana Password: ${GRAFANA_PASSWORD}"
```

**⚠️ ВАЖНО:** Сохраните эти пароли! Они понадобятся для доступа.

### Шаг 3.3: Клонирование Репозитория

```bash
# Клонируем проект (замените на свой URL)
cd /opt/sotospeak
git clone https://github.com/yourusername/sotospeak.git app

# Если репозиторий приватный:
# git clone https://username:token@github.com/yourusername/sotospeak.git app
```

### Шаг 3.4: Создание Docker Compose

```bash
cat > /opt/sotospeak/docker-compose.yml << 'EOF'
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:16-alpine
    container_name: sotospeak-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./postgres-init:/docker-entrypoint-initdb.d:ro
    ports:
      - "127.0.0.1:5432:5432"  # Только локально!
    command: >
      postgres 
      -c shared_buffers=256MB
      -c effective_cache_size=768MB
      -c maintenance_work_mem=64MB
      -c work_mem=4MB
      -c max_connections=100
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER} -d ${DB_NAME}"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

  # MinIO S3 Storage
  minio:
    image: minio/minio:latest
    container_name: sotospeak-minio
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ROOT_USER}
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD}
    volumes:
      - minio_data:/data
    ports:
      - "127.0.0.1:9000:9000"  # S3 API
      - "127.0.0.1:9001:9001"  # Console
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3
    deploy:
      resources:
        limits:
          memory: 256M

  # Backend API
  backend:
    build:
      context: ./app/backend
      dockerfile: ../../docker/Dockerfile.backend.prod
    container_name: sotospeak-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: production
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: ${DB_NAME}
      DB_USERNAME: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      S3_ENDPOINT: http://minio:9000
      S3_ACCESS_KEY: ${MINIO_ROOT_USER}
      S3_SECRET_KEY: ${MINIO_PASSWORD}
      S3_BUCKET: ${MINIO_BUCKET}
      S3_PUBLIC_URL: https://${API_DOMAIN}/s3
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: 86400000
      ADMIN_EMAIL: ${ADMIN_EMAIL}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      ADMIN_DISPLAY_NAME: ${ADMIN_DISPLAY_NAME}
      SERVER_PORT: 8080
      CORS_ORIGINS: "https://${ADMIN_DOMAIN}"
    ports:
      - "127.0.0.1:8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      minio:
        condition: service_started
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    deploy:
      resources:
        limits:
          memory: 768M
        reservations:
          memory: 512M

  # Admin Panel
  admin:
    build:
      context: ./app/admin-web
      dockerfile: ../../docker/Dockerfile.admin.prod
      args:
        VITE_API_URL: /api
    container_name: sotospeak-admin
    restart: unless-stopped
    ports:
      - "127.0.0.1:3000:80"
    deploy:
      resources:
        limits:
          memory: 128M

  # Nginx Reverse Proxy
  nginx:
    image: nginx:alpine
    container_name: sotospeak-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
      - ./certbot/conf:/etc/letsencrypt:ro
      - ./certbot/www:/var/www/certbot:ro
    depends_on:
      - backend
      - admin
    deploy:
      resources:
        limits:
          memory: 64M

  # Certbot for SSL
  certbot:
    image: certbot/certbot
    container_name: sotospeak-certbot
    volumes:
      - ./certbot/conf:/etc/letsencrypt
      - ./certbot/www:/var/www/certbot
    entrypoint: "/bin/sh -c 'trap exit TERM; while :; do certbot renew; sleep 12h & wait $${!}; done;'"

volumes:
  postgres_data:
  minio_data:

networks:
  default:
    driver: bridge
EOF
```

### Шаг 3.5: Создание Production Dockerfile для Backend

```bash
mkdir -p /opt/sotospeak/docker

cat > /opt/sotospeak/docker/Dockerfile.backend.prod << 'EOF'
# Stage 1: Build
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app

# Копируем только файлы зависимостей для кэширования
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Скачиваем зависимости
RUN gradle dependencies --no-daemon

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN gradle bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Устанавливаем curl для healthcheck
RUN apk add --no-cache curl

# Создаём непривилегированного пользователя
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Копируем JAR из стадии сборки
COPY --from=builder /app/build/libs/*.jar app.jar

# Меняем владельца
RUN chown -R appuser:appgroup /app

USER appuser

# Открываем порт
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Запуск
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
EOF
```

### Шаг 3.6: Создание Production Dockerfile для Admin

```bash
cat > /opt/sotospeak/docker/Dockerfile.admin.prod << 'EOF'
# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Копируем package.json
COPY package*.json ./

# Устанавливаем зависимости
RUN npm ci

# Копируем исходный код
COPY . .

# Собираем production build
ARG VITE_API_URL
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# Stage 2: Runtime
FROM nginx:alpine

# Копируем собранное приложение
COPY --from=builder /app/dist /usr/share/nginx/html

# Копируем nginx конфиг
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Открываем порт
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1

CMD ["nginx", "-g", "daemon off;"]
EOF
```

### Шаг 3.7: Создание Nginx Config для Admin

```bash
mkdir -p /opt/sotospeak/app/admin-web

cat > /opt/sotospeak/app/admin-web/nginx.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml application/json application/javascript 
               application/rss+xml application/atom+xml image/svg+xml;

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API proxy
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # React Router support
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
}
EOF
```

### Шаг 3.8: Создание Nginx Config для Proxy

```bash
cat > /opt/sotospeak/nginx/nginx.conf << 'EOF'
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Logging format
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    # Basic settings
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    server_tokens off;

    # Gzip
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml application/json application/javascript 
               application/rss+xml application/atom+xml image/svg+xml;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=login:10m rate=5r/m;
    limit_conn_zone $binary_remote_addr zone=addr:10m;

    # Backend API Server
    server {
        listen 80;
        listen [::]:80;
        server_name api.sotospeak.ru;

        # Certbot challenge
        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
        }

        # Redirect to HTTPS
        location / {
            return 301 https://$server_name$request_uri;
        }
    }

    server {
        listen 443 ssl http2;
        listen [::]:443 ssl http2;
        server_name api.sotospeak.ru;

        # SSL certificates (will be created by certbot)
        ssl_certificate /etc/letsencrypt/live/api.sotospeak.ru/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/api.sotospeak.ru/privkey.pem;
        ssl_trusted_certificate /etc/letsencrypt/live/api.sotospeak.ru/chain.pem;

        # SSL settings
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;
        ssl_session_cache shared:SSL:10m;
        ssl_session_timeout 10m;

        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Referrer-Policy "strict-origin-when-cross-origin" always;
        add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self' https:;" always;

        # Proxy to backend
        location / {
            limit_req zone=api burst=20 nodelay;
            limit_conn addr 10;

            proxy_pass http://backend:8080;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_cache_bypass $http_upgrade;

            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
            proxy_buffering on;
            proxy_buffer_size 4k;
            proxy_buffers 8 4k;
        }
    }

    # Admin Panel Server
    server {
        listen 80;
        listen [::]:80;
        server_name admin.sotospeak.ru;

        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
        }

        location / {
            return 301 https://$server_name$request_uri;
        }
    }

    server {
        listen 443 ssl http2;
        listen [::]:443 ssl http2;
        server_name admin.sotospeak.ru;

        ssl_certificate /etc/letsencrypt/live/admin.sotospeak.ru/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/admin.sotospeak.ru/privkey.pem;
        ssl_trusted_certificate /etc/letsencrypt/live/admin.sotospeak.ru/chain.pem;

        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;

        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Referrer-Policy "strict-origin-when-cross-origin" always;

        # API proxy
        location /api/ {
            proxy_pass http://backend:8080/api/;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }

        # S3 proxy
        location /s3/ {
            proxy_pass http://minio:9000/;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_hide_header X-Amz-Id-2;
            proxy_hide_header X-Amz-Request-Id;
            proxy_hide_header X-Amz-Meta-Code;
        }

        # Static files
        location / {
            proxy_pass http://admin:80;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
EOF
```

---

## Часть 4: Настройка Домена и SSL (10 минут)

### Шаг 4.1: Покупка Домена

1. Зайдите на https://reg.ru или https://nic.ru
2. Найдите домен `sotospeak.ru` (или другой)
3. Купите домен (~600₽/год)
4. В панели управления доменом найдите **"Управление DNS"**

### Шаг 4.2: Настройка DNS Записей

Создайте A-записи:
```
Тип: A
Имя: api
Значение: 185.XXX.XXX.XXX (IP вашего VPS)
TTL: 3600

Тип: A
Имя: admin
Значение: 185.XXX.XXX.XXX (IP вашего VPS)
TTL: 3600
```

Или через CNAME:
```
Тип: A
Имя: @
Значение: 185.XXX.XXX.XXX

Тип: CNAME
Имя: api
Значение: sotospeak.ru

Тип: CNAME
Имя: admin
Значение: sotospeak.ru
```

### Шаг 4.3: Получение SSL Сертификатов

```bash
cd /opt/sotospeak

# Запускаем только nginx и certbot для получения сертификатов
docker-compose up -d nginx

# Ждём 10 секунд
sleep 10

# Получаем сертификаты
docker-compose run --rm certbot certonly \
    --standalone \
    --preferred-challenges http \
    -d api.sotospeak.ru \
    -d admin.sotospeak.ru \
    --agree-tos \
    -m admin@sotospeak.ru \
    --no-eff-email

# Перезапускаем nginx с новыми сертификатами
docker-compose restart nginx
```

**⚠️ Если ошибка:** Убедитесь что DNS записи уже обновились (может занять до 15 минут)

### Шаг 4.4: Проверка SSL

```bash
# Проверка сертификата
curl -v https://api.sotospeak.ru/actuator/health 2>&1 | grep "SSL"

# Или через браузер откройте https://api.sotospeak.ru
# Должен быть зелёный замок!
```

---

## Часть 5: Сборка и Запуск (10 минут)

### Шаг 5.1: Сборка Проекта

```bash
cd /opt/sotospeak

# Загружаем переменные окружения
set -a && source .env && set +a

# Собираем и запускаем всё
docker-compose up -d --build

# Ждём инициализации (30-60 секунд)
echo "Waiting for services to start..."
sleep 30
```

### Шаг 5.2: Проверка Статуса

```bash
# Проверяем все контейнеры
docker-compose ps

# Должно показать:
# NAME                     STATUS
# sotospeak-admin       Up 30 seconds
# sotospeak-backend     Up 30 seconds (healthy)
# sotospeak-postgres    Up 30 seconds (healthy)
# sotospeak-minio       Up 30 seconds
# sotospeak-nginx       Up 30 seconds
# sotospeak-certbot     Up 30 seconds

# Проверяем логи backend (должно быть "Started SoToSpeakApplication")
docker-compose logs backend | tail -20

# Проверяем API
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

### Шаг 5.3: Создание Bucket в MinIO

```bash
# Устанавливаем mc (MinIO client)
wget https://dl.min.io/client/mc/release/linux-amd64/mc
chmod +x mc
mv mc /usr/local/bin/

# Настраиваем alias
mc alias set local http://localhost:9000 minioadmin "${MINIO_PASSWORD}"

# Создаём bucket
mc mb local/sotospeak

# Делаем bucket публичным для чтения
mc policy set download local/sotospeak
```

### Шаг 5.4: Проверка Доступа

```bash
# API health check
curl https://api.sotospeak.ru/actuator/health

# Admin panel (должен вернуть HTML)
curl -I https://admin.sotospeak.ru

# Открываем в браузере:
# https://admin.sotospeak.ru
# Логин: admin@sotospeak.com
# Пароль: admin123 (измените!)
```

---

## Часть 6: Мониторинг и Обслуживание

### Шаг 6.1: Установка Мониторинга (Опционально)

```bash
# Добавляем в docker-compose.yml перед запуском

  # Prometheus
  prometheus:
    image: prom/prometheus:latest
    container_name: sotospeak-prometheus
    restart: unless-stopped
    ports:
      - "127.0.0.1:9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/usr/share/prometheus/console_libraries'
      - '--web.console.templates=/usr/share/prometheus/consoles'
      - '--storage.tsdb.retention.time=30d'
      - '--web.enable-lifecycle'

  # Grafana
  grafana:
    image: grafana/grafana:latest
    container_name: sotospeak-grafana
    restart: unless-stopped
    ports:
      - "127.0.0.1:3001:3000"
    volumes:
      - grafana_data:/var/lib/grafana
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD}
      GF_SERVER_ROOT_URL: https://grafana.sotospeak.ru
```

### Шаг 6.2: Автоматические Бэкапы

```bash
# Создаём скрипт бэкапа
cat > /opt/sotospeak/backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/sotospeak/backups"
RETENTION_DAYS=7

# Create backup directory
mkdir -p ${BACKUP_DIR}

# Backup PostgreSQL
docker exec sotospeak-postgres pg_dump -U postgres sotospeak | gzip > ${BACKUP_DIR}/postgres_${DATE}.sql.gz

# Backup MinIO data
tar -czf ${BACKUP_DIR}/minio_${DATE}.tar.gz -C /opt/sotospeak docker-compose.yml .env nginx/

# Remove old backups
find ${BACKUP_DIR} -name "postgres_*.sql.gz" -mtime +${RETENTION_DAYS} -delete
find ${BACKUP_DIR} -name "minio_*.tar.gz" -mtime +${RETENTION_DAYS} -delete

# Upload to Yandex Disk (опционально)
# curl -T ${BACKUP_DIR}/postgres_${DATE}.sql.gz "https://cloud-api.yandex.net/v1/disk/resources/upload?path=/backups/postgres_${DATE}.sql.gz" -H "Authorization: OAuth YOUR_TOKEN"

echo "Backup completed: ${DATE}"
EOF

chmod +x /opt/sotospeak/backup.sh

# Добавляем в cron (ежедневно в 3:00 AM)
(crontab -l 2>/dev/null; echo "0 3 * * * /opt/sotospeak/backup.sh >> /var/log/sotospeak-backup.log 2>&1") | crontab -

# Проверяем
crontab -l
```

### Шаг 6.3: Полезные Команды

```bash
# Перезапуск всех сервисов
cd /opt/sotospeak && docker-compose restart

# Пересборка после изменений
docker-compose up -d --build

# Просмотр логов
docker-compose logs -f backend
docker-compose logs -f admin
docker-compose logs -f nginx

# Вход в контейнер
docker exec -it sotospeak-backend sh
docker exec -it sotospeak-postgres psql -U postgres -d sotospeak

# Очистка неиспользуемых образов
docker system prune -a

# Проверка использования ресурсов
docker stats

# Обновление сертификатов (автоматически, но можно вручную)
docker-compose run --rm certbot renew
```

---

## Часть 7: Устранение Неполадок

### Проблема: Backend не стартует

```bash
# Смотрим логи
docker-compose logs backend

# Проверяем подключение к БД
docker exec -it sotospeak-backend sh
# Внутри контейнера:
apk add --no-cache postgresql-client
pg_isready -h postgres -p 5432 -U postgres

# Проверяем переменные окружения
env | grep DB_
```

### Проблема: 502 Bad Gateway

```bash
# Проверяем что backend запущен
docker-compose ps backend

# Проверяем порт
docker exec sotospeak-nginx nc -zv backend 8080

# Перезапускаем nginx
docker-compose restart nginx
```

### Проблема: SSL не работает

```bash
# Проверяем существование сертификатов
ls -la /opt/sotospeak/certbot/conf/live/

# Перевыпускаем
docker-compose run --rm certbot certonly --force-renew -d api.sotospeak.ru -d admin.sotospeak.ru

# Перезапускаем nginx
docker-compose restart nginx
```

### Проблема: Недостаточно памяти

```bash
# Проверяем использование памяти
free -h
docker stats --no-stream

# Увеличиваем swap (если нужно)
swapoff -a
fallocate -l 4G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile

# Или перезапускаем с ограничением памяти
docker-compose restart backend
```

---

## 🎉 Проверка Результата

Откройте в браузере:

1. **Admin Panel:** https://admin.sotospeak.ru
   - Логин: `admin@sotospeak.com`
   - Пароль: `admin123` (измените!)

2. **API Health:** https://api.sotospeak.ru/actuator/health
   - Должно вернуть: `{"status":"UP"}`

3. **MinIO Console:** http://185.XXX.XXX.XXX:9001
   - Логин: `minioadmin`
   - Пароль: (из .env файла)

---

## 📊 Итоговая Схема

```
Пользователь
    │
    ▼
sotospeak.ru (домен)
    │
    ▼
Timeweb VPS (185.XXX.XXX.XXX)
    │
    ├── Nginx (:80, :443) → SSL termination
    │   ├── /api/* → Backend (:8080)
    │   └── /* → Admin (:3000)
    │
    ├── Backend (Spring Boot + Docker)
    │   └── Подключение к PostgreSQL & MinIO
    │
    ├── Admin (React + Nginx + Docker)
    │
    ├── PostgreSQL (:5432, только localhost)
    │
    └── MinIO (:9000, :9001, только localhost)

SSL: Let's Encrypt (автообновление)
Backup: Ежедневно в 3:00 AM
Monitor: Docker stats + logs
```

---

## 💰 Итоговая Стоимость

| Статья | Стоимость |
|--------|-----------|
| Timeweb VPS (2GB, 30GB NVMe) | 540₽/мес |
| Домен .ru | 50₽/мес (600₽/год) |
| SSL (Let's Encrypt) | 0₽ |
| **ИТОГО** | **590₽/мес** |

---

**Готово!** У вас работает полноценный production-ready сервер за 600₽/мес! 🚀

Если что-то не работает - проверьте логи командами выше или пишите в поддержку Timeweb (у них отличная техподдержка 24/7).
