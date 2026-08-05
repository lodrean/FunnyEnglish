# 💰 So to Speak - Бюджетные Варианты Размещения

## 📊 Сравнение Вариантов

| Вариант | Стоимость/мес | Надёжность | Сложность | Подходит для |
|---------|---------------|------------|-----------|--------------|
| **Home Server** | ~500₽ (электричество) | ⭐⭐ | ⭐⭐⭐⭐ | MVP, тестирование |
| **VPS (Selectel)** | ~600₽ | ⭐⭐⭐⭐ | ⭐⭐ | Начало, < 1000 users |
| **VPS (Yandex)** | ~900₽ | ⭐⭐⭐⭐ | ⭐⭐ | Начало, < 1000 users |
| **Hybrid** | ~300₽ | ⭐⭐⭐ | ⭐⭐⭐ | Оптимальный баланс |
| **Yandex Cloud Full** | ~15,000₽ | ⭐⭐⭐⭐⭐ | ⭐⭐ | Рост, > 1000 users |

---

## 🏠 Option 1: Домашний Сервер (Self-Hosted)

### Минимальные Требования к ПК

```
┌────────────────────────────────────────────┐
│  Старый ПК / Ноутбук                       │
│  ├─ CPU: 4+ ядра (Intel i5 / Ryzen 5)      │
│  ├─ RAM: 8+ GB (16GB лучше)                │
│  ├─ SSD: 50+ GB                            │
│  ├─ Интернет: 50+ Mbps upload              │
│  └─ Статический IP (желательно)            │
└────────────────────────────────────────────┘
```

### Архитектура Домашнего Сервера

```
┌─────────────────────────────────────────────────────────────┐
│                    Домашний ПК (Ubuntu Server)              │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Nginx      │  │   Backend    │  │   PostgreSQL │      │
│  │   (reverse   │◄─┤   (Docker)   │◄─┤   (Docker)   │      │
│  │    proxy)    │  │   8080       │  │   5432       │      │
│  └──────┬───────┘  └──────────────┘  └──────────────┘      │
│         │                                                   │
│  ┌──────┴───────┐  ┌──────────────┐                        │
│  │   Admin      │  │   MinIO      │                        │
│  │   (Docker)   │  │   (S3)       │                        │
│  │   3000       │  │   9000       │                        │
│  └──────────────┘  └──────────────┘                        │
│                                                             │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           │ Port Forwarding (80, 443, 8080)
                           │
              ┌────────────┴────────────┐
              │    Роутер (DDNS)         │
              │  ┌──────────────────┐    │
              │  │  Cloudflare      │    │
              │  │  Tunnel / DDNS   │    │
              │  └──────────────────┘    │
              └───────────┬───────────────┘
                          │
              https://api.sotospeak.ru
              https://admin.sotospeak.ru
```

### Пошаговая Настройка

#### 1. Установка Ubuntu Server
```bash
# Скачать Ubuntu 22.04 LTS Server
# https://ubuntu.com/download/server

# Минимальная установка
# - OpenSSH server ✓
# - Docker ✓
```

#### 2. Настройка Docker
```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### 3. Docker Compose Конфигурация
```yaml
# /opt/sotospeak/docker-compose.yml
version: '3.8'

services:
  # Database
  postgres:
    image: postgres:16-alpine
    container_name: sotospeak-postgres
    environment:
      POSTGRES_DB: sotospeak
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "127.0.0.1:5432:5432"  # Только локально!
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  # S3 Storage
  minio:
    image: minio/minio:latest
    container_name: sotospeak-minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD}
    volumes:
      - minio_data:/data
    ports:
      - "127.0.0.1:9000:9000"  # API - только локально
      - "127.0.0.1:9001:9001"  # Console - только локально
    restart: unless-stopped

  # Backend API
  backend:
    build:
      context: ../backend
      dockerfile: ../docker/Dockerfile.backend
    container_name: sotospeak-backend
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: sotospeak
      DB_USERNAME: postgres
      DB_PASSWORD: ${DB_PASSWORD}
      S3_ENDPOINT: http://minio:9000
      S3_ACCESS_KEY: minioadmin
      S3_SECRET_KEY: ${MINIO_PASSWORD}
      S3_BUCKET: sotospeak
      JWT_SECRET: ${JWT_SECRET}
      ADMIN_EMAIL: admin@sotospeak.com
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
    ports:
      - "127.0.0.1:8080:8080"  # Только локально!
    depends_on:
      postgres:
        condition: service_healthy
      minio:
        condition: service_started
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M

  # Admin Panel
  admin:
    build:
      context: ../admin-web
      dockerfile: ../docker/Dockerfile.admin
    container_name: sotospeak-admin
    environment:
      VITE_API_URL: /api
    ports:
      - "127.0.0.1:3000:80"  # Только локально!
    restart: unless-stopped

  # Reverse Proxy (Nginx)
  nginx:
    image: nginx:alpine
    container_name: sotospeak-nginx
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
    restart: unless-stopped

  # SSL Certificate Automation
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
```

#### 4. Nginx Конфигурация
```nginx
# /opt/sotospeak/nginx/nginx.conf
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Logging
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;
    error_log /var/log/nginx/error.log warn;

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

    # Backend API
    server {
        listen 80;
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
        server_name api.sotospeak.ru;

        ssl_certificate /etc/letsencrypt/live/api.sotospeak.ru/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/api.sotospeak.ru/privkey.pem;
        ssl_trusted_certificate /etc/letsencrypt/live/api.sotospeak.ru/chain.pem;

        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;

        location / {
            limit_req zone=api burst=20 nodelay;
            
            proxy_pass http://backend:8080;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_cache_bypass $http_upgrade;
            
            # Timeouts
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }
    }

    # Admin Panel
    server {
        listen 80;
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
        server_name admin.sotospeak.ru;

        ssl_certificate /etc/letsencrypt/live/admin.sotospeak.ru/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/admin.sotospeak.ru/privkey.pem;

        # API proxy for admin
        location /api/ {
            proxy_pass http://backend:8080/api/;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }

        # Static files
        location / {
            proxy_pass http://admin:80;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
        }
    }
}
```

#### 5. Настройка Доступа из Интернета

##### Вариант A: Статический IP (Идеально)
```bash
# Если провайдер даёт статический IP
# Просто настраиваем Port Forwarding на роутере:
# - 80 → 80 на сервере
# - 443 → 443 на сервере

# Настройка DNS
# A-запись: api.sotospeak.ru → YOUR_STATIC_IP
# A-запись: admin.sotospeak.ru → YOUR_STATIC_IP
```

##### Вариант B: Динамический IP (DDNS)
```bash
# Установить DDNS клиент
sudo apt install ddclient

# Конфигурация /etc/ddclient.conf
protocol=cloudflare, \
zone=sotospeak.ru, \
password=YOUR_CLOUDFLARE_API_TOKEN \
api.sotospeak.ru,admin.sotospeak.ru

# Или использовать Yandex DDNS
# https://dns.yandex.ru
```

##### Вариант C: Cloudflare Tunnel (Рекомендуется!)
```bash
# Самый простой и безопасный вариант
# Не нужен статический IP, не нужен port forwarding!

# Установка cloudflared
wget -q https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
sudo dpkg -i cloudflared-linux-amd64.deb

# Аутентификация
cloudflared tunnel login

# Создание туннеля
cloudflared tunnel create sotospeak

# Конфигурация ~/.cloudflared/config.yml
tunnel: YOUR_TUNNEL_ID
credentials-file: /home/user/.cloudflared/YOUR_TUNNEL_ID.json

ingress:
  - hostname: api.sotospeak.ru
    service: http://localhost:8080
  - hostname: admin.sotospeak.ru
    service: http://localhost:3000
  - service: http_status:404

# Запуск
cloudflared tunnel run sotospeak

# Автозапуск
sudo cloudflared service install
sudo systemctl start cloudflared
```

#### 6. SSL Сертификаты (Let's Encrypt)
```bash
# Получение сертификатов
docker run -it --rm \
  -v /opt/sotospeak/certbot/conf:/etc/letsencrypt \
  -v /opt/sotospeak/certbot/www:/var/www/certbot \
  certbot/certbot certonly \
  --standalone \
  -d api.sotospeak.ru \
  -d admin.sotospeak.ru \
  --agree-tos \
  -m admin@sotospeak.ru

# Автообновление уже настроено в docker-compose
```

### Безопасность Домашнего Сервера

```bash
# 1. Firewall (UFW)
sudo apt install ufw
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable

# 2. Fail2ban (защита от брутфорса)
sudo apt install fail2ban
sudo systemctl enable fail2ban

# 3. Автоматические обновления безопасности
sudo apt install unattended-upgrades
sudo dpkg-reconfigure -plow unattended-upgrades

# 4. Резервное копирование
# Ежедневный backup на внешний диск / Yandex Disk
```

### Мониторинг Домашнего Сервера

```yaml
# Добавить в docker-compose.yml
  prometheus:
    image: prom/prometheus:latest
    container_name: sotospeak-prometheus
    ports:
      - "127.0.0.1:9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    container_name: sotospeak-grafana
    ports:
      - "127.0.0.1:3001:3000"  # 3000 занят admin!
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana:/etc/grafana/provisioning:ro
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD}
    restart: unless-stopped
```

### Стоимость Домашнего Сервера

```
┌─────────────────────────────────────────┐
│  Расходы:                               │
│  ├─ Электричество (~100W 24/7): 400₽   │
│  ├─ Домен (.ru): 600₽/год = 50₽/мес    │
│  └─ Cloudflare (бесплатно): 0₽         │
│                                         │
│  ИТОГО: ~450₽/мес                       │
└─────────────────────────────────────────┘
```

---

## 💻 Option 2: Дешёвый VPS

### Selectel (Самый дешёвый надежный)

```
┌─────────────────────────────────────────┐
│  Selectel VPS (Start)                   │
│  ├─ 1 vCPU                               │
│  ├─ 1 GB RAM                             │
│  ├─ 20 GB SSD                            │
│  ├─ 100 Mbps                             │
│  └─ Цена: 600₽/мес (~$6.50)              │
└─────────────────────────────────────────┘
```

**Для So to Speak:**
- Маловато RAM (1GB)
- Нужен swap файл
- Подходит только для начала

**Конфигурация:**
```bash
# Добавить 2GB swap
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Оптимизация PostgreSQL для мало RAM
# shared_buffers = 128MB
# effective_cache_size = 512MB
```

### Timeweb Cloud (Оптимальный баланс)

```
┌─────────────────────────────────────────┐
│  Timeweb Cloud (Мощность)               │
│  ├─ 2 vCPU                               │
│  ├─ 2 GB RAM                             │
│  ├─ 30 GB NVMe                           │
│  ├─ 200 Mbps                             │
│  └─ Цена: 540₽/мес (~$5.90)              │
└─────────────────────────────────────────┘
```

**Плюсы:**
- NVMe диски (быстрее)
- Российская компания
- Простая панель управления
- Бесплатный бэкап

### Yandex Cloud (Чуть дороже, но надежнее)

```
┌─────────────────────────────────────────┐
│  Yandex Compute (Старт)                 │
│  ├─ 2 vCPU (20% гарантировано)           │
│  ├─ 2 GB RAM                             │
│  ├─ 30 GB SSD                            │
│  ├─ До 100 Mbps                          │
│  └─ Цена: ~890₽/мес                      │
└─────────────────────────────────────────┘
```

---

## 🔀 Option 3: Hybrid (Рекомендуется для старта!)

### Концепция
```
┌──────────────────────────────────────────────────────────┐
│  Бесплатные/Дешевые облачные сервисы                     │
│  + Домашний сервер для разработки/тестов                 │
└──────────────────────────────────────────────────────────┘
```

### Реализация

#### Бэкенд API: Timeweb VPS (540₽/мес)
- 2 CPU, 2GB RAM, 30GB NVMe
- PostgreSQL + Backend API
- Nginx reverse proxy

#### Admin Panel: Yandex Object Storage (бесплатно)
- Статический хостинг
- CDN (400₽/мес при нагрузке)
- API через backend

#### S3 / Файлы: Yandex Object Storage
- 1GB бесплатно
- Далее 1.5₽/GB

#### Мониторинг: Self-hosted на домашнем ПК
- Prometheus + Grafana
- Только для внутреннего доступа

### Итоговая стоимость Hybrid
```
Timeweb VPS (2GB):              540₽/мес
Yandex Object Storage:            0₽ (до 1GB)
CDN (опционально):              400₽/мес
Домен (.ru):                     50₽/мес
────────────────────────────────────────
ИТОГО:                          ~600₽/мес
```

---

## 🚀 Option 4: Serverless (Ultra-бюджетный)

### Yandex Cloud Functions (экспериментально)

```
Бэкенд API → Yandex Cloud Functions (по запросу)
База данных → Yandex Serverless PostgreSQL
Админка → Yandex Object Storage (статика)
```

**Стоимость:**
- До 1M запросов: бесплатно
- Далее: ~0.01₽/запрос
- БД: от 500₽/мес

**Минусы:**
- Cold start latency (~1-2 сек)
- Нужен рефакторинг для stateless
- Сложнее отлаживать

---

## 📋 Выбор по Сценариям

### Сценарий 1: "Совсем нет денег, только тестирование"
```
→ Домашний сервер + Cloudflare Tunnel
→ Стоимость: 500₽/мес (только электричество)
→ Риск: Зависит от домашнего интернета
```

### Сценарий 2: "Небольшой бюджет, хочу стабильность"
```
→ Timeweb VPS 2GB + Yandex Object Storage
→ Стоимость: 600₽/мес
→ Можно начинать с реальными пользователями
```

### Сценарий 3: "Есть стабильный домашний интернет"
```
→ Домашний сервер + DDNS/Cloudflare
→ Стоимость: 500₽/мес
→ Полный контроль
→ Можно апгрейдить железо
```

### Сценарий 4: "Хочу расти без миграций"
```
→ Начать с Timeweb VPS
→ При росте перейти на Yandex Cloud Kubernetes
→ Стоимость: от 600₽ до 15,000₽ по мере роста
```

---

## 🔧 Готовые Скрипты Деплоя

### Автоматическая Установка (Home Server)
```bash
#!/bin/bash
# install.sh - Запускать на Ubuntu Server

set -e

echo "🚀 So to Speak Home Server Setup"

# Update system
sudo apt update && sudo apt upgrade -y

# Install dependencies
sudo apt install -y docker.io docker-compose git curl ufw fail2ban

# Setup firewall
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable

# Setup fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban

# Create app directory
sudo mkdir -p /opt/sotospeak
sudo chown $USER:$USER /opt/sotospeak

# Clone repository
git clone https://github.com/your/sotospeak.git /opt/sotospeak/app

# Create environment file
cat > /opt/sotospeak/.env << EOF
DB_PASSWORD=$(openssl rand -base64 32)
MINIO_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 64)
ADMIN_PASSWORD=admin123
GRAFANA_PASSWORD=$(openssl rand -base64 16)
EOF

# Start services
cd /opt/sotospeak
docker-compose up -d

echo "✅ Installation complete!"
echo "📋 Next steps:"
echo "   1. Configure DNS to point to this server"
echo "   2. Get SSL certificates: docker-compose run --rm certbot certonly --standalone -d your-domain.com"
echo "   3. Access admin at https://admin.your-domain.com"
echo "   4. Default admin credentials: admin@sotospeak.com / admin123"
```

---

## 📝 Чек-лист Миграции на Домашний Сервер

- [ ] Проверить скорость upload интернета (нужно 20+ Mbps)
- [ ] Убедиться что ПК справится (4+ ядра, 8+ GB RAM)
- [ ] Настроить статический IP или DDNS/Cloudflare Tunnel
- [ ] Открыть порты 80, 443 на роутере (если не используете Tunnel)
- [ ] Настроить резервное копирование (внешний диск / облако)
- [ ] Настроить мониторинг (Telegram alerts)
- [ ] Протестировать отказоустойчивость (перезагрузка роутера)

---

**💡 Рекомендация:** Начните с **Timeweb VPS (540₽/мес)** - это оптимальный баланс цена/качество/простота. При росте можно перейти на Yandex Cloud или домашний сервер с мощным железом.

Нужна помощь с настройкой конкретного варианта? Могу подготовить полные конфиги и инструкции для выбранного подхода.
