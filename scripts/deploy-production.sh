#!/bin/bash
# =============================================================================
# FunnyEnglish Production Deploy Script
# Разворачивает Backend + Admin + PostgreSQL + MinIO на VPS
# =============================================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# =============================================================================
# Параметры по умолчанию
# =============================================================================
DOMAIN=""
API_SUBDOMAIN="api"
ADMIN_SUBDOMAIN="admin"
REPO_URL=""
BRANCH="main"
SKIP_SSL=false

# =============================================================================
# Функции
# =============================================================================
print_usage() {
    echo "Использование: $0 [OPTIONS]"
    echo ""
    echo "Обязательные параметры:"
    echo "  --domain=DOMAIN           Основной домен (например: funnyenglish.ru)"
    echo ""
    echo "Опциональные параметры:"
    echo "  --api-subdomain=SUB       API поддомен (default: api)"
    echo "  --admin-subdomain=SUB     Admin поддомен (default: admin)"
    echo "  --repo-url=URL            URL git репозитория"
    echo "  --branch=BRANCH           Git ветка (default: main)"
    echo "  --skip-ssl                Пропустить настройку SSL (для тестов)"
    echo "  --help                    Показать эту справку"
    echo ""
    echo "Пример:"
    echo "  $0 --domain=funnyenglish.ru --repo-url=https://github.com/user/funnyenglish.git"
}

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warn() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# =============================================================================
# Парсинг аргументов
# =============================================================================
while [[ $# -gt 0 ]]; do
    case $1 in
        --domain=*)
            DOMAIN="${1#*=}"
            shift
            ;;
        --api-subdomain=*)
            API_SUBDOMAIN="${1#*=}"
            shift
            ;;
        --admin-subdomain=*)
            ADMIN_SUBDOMAIN="${1#*=}"
            shift
            ;;
        --repo-url=*)
            REPO_URL="${1#*=}"
            shift
            ;;
        --branch=*)
            BRANCH="${1#*=}"
            shift
            ;;
        --skip-ssl)
            SKIP_SSL=true
            shift
            ;;
        --help)
            print_usage
            exit 0
            ;;
        *)
            log_error "Неизвестный параметр: $1"
            print_usage
            exit 1
            ;;
    esac
done

# =============================================================================
# Проверки
# =============================================================================
if [ -z "$DOMAIN" ]; then
    log_error "Не указан домен! Используйте --domain=yourdomain.ru"
    print_usage
    exit 1
fi

if [ "$EUID" -ne 0 ]; then 
    log_error "Запустите скрипт от root (sudo)"
    exit 1
fi

API_DOMAIN="${API_SUBDOMAIN}.${DOMAIN}"
ADMIN_DOMAIN="${ADMIN_SUBDOMAIN}.${DOMAIN}"

echo -e "${GREEN}🚀 FunnyEnglish Production Deploy${NC}"
echo "====================================="
echo ""
log_info "Домен: $DOMAIN"
log_info "API: $API_DOMAIN"
log_info "Admin: $ADMIN_DOMAIN"
if [ -n "$REPO_URL" ]; then
    log_info "Репозиторий: $REPO_URL"
fi
echo ""

# =============================================================================
# Шаг 1: Клонирование репозитория
# =============================================================================
echo -e "\n${YELLOW}📥 Шаг 1: Подготовка проекта...${NC}"

cd /opt/funnyenglish

if [ -n "$REPO_URL" ]; then
    if [ -d "app" ]; then
        log_warn "Директория app существует, обновляем..."
        cd app && git pull && git checkout $BRANCH && cd ..
    else
        git clone -b $BRANCH $REPO_URL app
    fi
    log_success "Репозиторий готов"
else
    if [ ! -d "app" ]; then
        log_warn "Директория app не найдена и не указан --repo-url"
        mkdir -p app
    fi
fi

# =============================================================================
# Шаг 2: Создание .env файла
# =============================================================================
echo -e "\n${YELLOW}🔐 Шаг 2: Генерация секретов...${NC}"

if [ -f ".env" ]; then
    log_warn ".env файл существует, создаём резервную копию"
    cp .env ".env.backup.$(date +%Y%m%d_%H%M%S)"
fi

DB_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
MINIO_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
JWT_SECRET=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-50)
ADMIN_PASSWORD=$(openssl rand -base64 16 | tr -d "=+/" | cut -c1-12)

cat > .env << EOF
# FunnyEnglish Production Environment
# Generated: $(date)

# Database
DB_PASSWORD=${DB_PASSWORD}
DB_NAME=funnyenglish
DB_USER=postgres

# MinIO S3
MINIO_ROOT_USER=minioadmin
MINIO_PASSWORD=${MINIO_PASSWORD}
MINIO_BUCKET=funnyenglish

# Backend
JWT_SECRET=${JWT_SECRET}
ADMIN_EMAIL=admin@funnyenglish.com
ADMIN_PASSWORD=${ADMIN_PASSWORD}
ADMIN_DISPLAY_NAME=Admin

# Domains
API_DOMAIN=${API_DOMAIN}
ADMIN_DOMAIN=${ADMIN_DOMAIN}
EOF

# Сохраняем пароли в файл
cat > /root/.funnyenglish-credentials << EOF
FunnyEnglish Production Credentials
Generated: $(date)
=====================================

Database:
  User: postgres
  Password: ${DB_PASSWORD}

MinIO S3:
  Access Key: minioadmin
  Secret Key: ${MINIO_PASSWORD}
  Bucket: funnyenglish

Admin Panel:
  URL: https://${ADMIN_DOMAIN}
  Email: admin@funnyenglish.com
  Password: ${ADMIN_PASSWORD}

API:
  URL: https://${API_DOMAIN}
EOF

chmod 600 /root/.funnyenglish-credentials

log_success "Секреты сгенерированы и сохранены"
log_info "Пароли сохранены в: /root/.funnyenglish-credentials"

# =============================================================================
# Шаг 3: Создание Docker Compose
# =============================================================================
echo -e "\n${YELLOW}🐳 Шаг 3: Создание Docker Compose...${NC}"

if [ -f "app/docker-compose.prod.yml" ]; then
    cp app/docker-compose.prod.yml docker-compose.yml
    log_success "Используем docker-compose.prod.yml из репозитория"
elif [ -f "app/docker-compose.yml" ]; then
    cp app/docker-compose.yml docker-compose.yml
    log_success "Используем docker-compose.yml из репозитория"
else
    log_info "Создаём docker-compose.yml..."
    cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: funnyenglish-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "127.0.0.1:5432:5432"
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

  minio:
    image: minio/minio:latest
    container_name: funnyenglish-minio
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ROOT_USER}
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD}
    volumes:
      - minio_data:/data
    ports:
      - "127.0.0.1:9000:9000"
      - "127.0.0.1:9001:9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3
    deploy:
      resources:
        limits:
          memory: 256M

  backend:
    build:
      context: ./app/backend
      dockerfile: Dockerfile
    container_name: funnyenglish-backend
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

  admin:
    build:
      context: ./app/admin-web
      dockerfile: Dockerfile
      args:
        VITE_API_URL: /api
    container_name: funnyenglish-admin
    restart: unless-stopped
    ports:
      - "127.0.0.1:3000:80"
    deploy:
      resources:
        limits:
          memory: 128M

  nginx:
    image: nginx:alpine
    container_name: funnyenglish-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./certbot/conf:/etc/letsencrypt:ro
      - ./certbot/www:/var/www/certbot:ro
    depends_on:
      - backend
      - admin

  certbot:
    image: certbot/certbot
    container_name: funnyenglish-certbot
    volumes:
      - ./certbot/conf:/etc/letsencrypt
      - ./certbot/www:/var/www/certbot
    entrypoint: "/bin/sh -c 'trap exit TERM; while :; do certbot renew; sleep 12h & wait $${!}; done;'"

volumes:
  postgres_data:
  minio_data:
EOF
fi

# =============================================================================
# Шаг 4: Создание Nginx конфигурации
# =============================================================================
echo -e "\n${YELLOW}🌐 Шаг 4: Настройка Nginx...${NC}"

mkdir -p nginx

cat > nginx/nginx.conf << EOF
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

    log_format main '\$remote_addr - \$remote_user [\$time_local] "\$request" '
                    '\$status \$body_bytes_sent "\$http_referer" '
                    '"\$http_user_agent" "\$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    server_tokens off;

    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml application/json application/javascript 
               application/rss+xml application/atom+xml image/svg+xml;

    # Rate limiting
    limit_req_zone \$binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone \$binary_remote_addr zone=login:10m rate=5r/m;

    # API Server
    server {
        listen 80;
        listen [::]:80;
        server_name ${API_DOMAIN};

        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
        }

        location / {
            return 301 https://\$server_name\$request_uri;
        }
    }

    server {
        listen 443 ssl http2;
        listen [::]:443 ssl http2;
        server_name ${API_DOMAIN};

        ssl_certificate /etc/letsencrypt/live/${API_DOMAIN}/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/${API_DOMAIN}/privkey.pem;
        ssl_trusted_certificate /etc/letsencrypt/live/${API_DOMAIN}/chain.pem;

        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
        ssl_prefer_server_ciphers off;
        ssl_session_cache shared:SSL:10m;
        ssl_session_timeout 10m;

        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Referrer-Policy "strict-origin-when-cross-origin" always;

        location / {
            limit_req zone=api burst=20 nodelay;

            proxy_pass http://backend:8080;
            proxy_http_version 1.1;
            proxy_set_header Host \$host;
            proxy_set_header X-Real-IP \$remote_addr;
            proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto \$scheme;
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }
    }

    # Admin Server
    server {
        listen 80;
        listen [::]:80;
        server_name ${ADMIN_DOMAIN};

        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
        }

        location / {
            return 301 https://\$server_name\$request_uri;
        }
    }

    server {
        listen 443 ssl http2;
        listen [::]:443 ssl http2;
        server_name ${ADMIN_DOMAIN};

        ssl_certificate /etc/letsencrypt/live/${ADMIN_DOMAIN}/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/${ADMIN_DOMAIN}/privkey.pem;
        ssl_trusted_certificate /etc/letsencrypt/live/${ADMIN_DOMAIN}/chain.pem;

        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
        ssl_prefer_server_ciphers off;

        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Referrer-Policy "strict-origin-when-cross-origin" always;

        location /api/ {
            proxy_pass http://backend:8080/api/;
            proxy_http_version 1.1;
            proxy_set_header Host \$host;
            proxy_set_header X-Real-IP \$remote_addr;
            proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto \$scheme;
        }

        location / {
            proxy_pass http://admin:80;
            proxy_http_version 1.1;
            proxy_set_header Host \$host;
            proxy_set_header X-Real-IP \$remote_addr;
            proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto \$scheme;
        }
    }
}
EOF

log_success "Nginx конфигурация создана"

# =============================================================================
# Шаг 5: Запуск контейнеров
# =============================================================================
echo -e "\n${YELLOW}🚀 Шаг 5: Запуск сервисов...${NC}"

log_info "Останавливаем существующие контейнеры..."
docker-compose down 2>/dev/null || true

log_info "Запускаем сервисы..."
docker-compose up -d

log_success "Контейнеры запущены"

# =============================================================================
# Шаг 6: Получение SSL сертификатов
# =============================================================================
if [ "$SKIP_SSL" = false ]; then
    echo -e "\n${YELLOW}🔒 Шаг 6: Получение SSL сертификатов...${NC}"
    
    log_info "Ждём запуск nginx..."
    sleep 5
    
    log_info "Запрашиваем сертификаты Let's Encrypt..."
    docker-compose run --rm certbot certonly \
        --webroot \
        --webroot-path=/var/www/certbot \
        --email admin@${DOMAIN} \
        --agree-tos \
        --no-eff-email \
        -d ${API_DOMAIN} \
        -d ${ADMIN_DOMAIN} 2>&1 || {
            log_warn "Не удалось получить SSL сертификаты сразу"
            log_warn "Убедитесь что DNS записи обновились и запустите:"
            log_warn "docker-compose run --rm certbot certonly --webroot --webroot-path=/var/www/certbot -d ${API_DOMAIN} -d ${ADMIN_DOMAIN}"
        }
    
    log_info "Перезапускаем nginx..."
    docker-compose restart nginx
    
    log_success "SSL настроен"
else
    log_warn "SSL пропущен (--skip-ssl)"
fi

# =============================================================================
# Шаг 7: Настройка MinIO
# =============================================================================
echo -e "\n${YELLOW}📦 Шаг 7: Настройка MinIO...${NC}"

log_info "Ждём запуск MinIO..."
sleep 10

# Установка mc если не установлен
if ! command -v mc &> /dev/null; then
    wget -q https://dl.min.io/client/mc/release/linux-amd64/mc -O /usr/local/bin/mc
    chmod +x /usr/local/bin/mc
fi

# Настройка bucket
source .env
mc alias set local http://localhost:9000 minioadmin "${MINIO_PASSWORD}" 2>/dev/null || true
mc mb local/funnyenglish 2>/dev/null || true
mc policy set download local/funnyenglish 2>/dev/null || true

log_success "MinIO настроен"

# =============================================================================
# Шаг 8: Проверка статуса
# =============================================================================
echo -e "\n${YELLOW}✅ Шаг 8: Проверка статуса...${NC}"

sleep 5

echo ""
echo "Статус контейнеров:"
docker-compose ps

echo ""
log_info "Проверка API..."
if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    log_success "API работает"
else
    log_warn "API не отвечает, проверьте логи: docker-compose logs backend"
fi

# =============================================================================
# Готово!
# =============================================================================
echo ""
echo -e "${GREEN}🎉 Деплой завершён!${NC}"
echo "====================="
echo ""
echo "Доступ к сервисам:"
echo "  🌐 Admin Panel: https://${ADMIN_DOMAIN}"
echo "  🔌 API: https://${API_DOMAIN}"
echo ""
echo "Учётные данные Admin:"
echo "  Email: admin@funnyenglish.com"
echo "  Пароль: ${ADMIN_PASSWORD}"
echo ""
echo "Пароли сохранены в: /root/.funnyenglish-credentials"
echo ""
echo "Полезные команды:"
echo "  cd /opt/funnyenglish && docker-compose ps"
echo "  cd /opt/funnyenglish && docker-compose logs -f backend"
echo "  cd /opt/funnyenglish && docker-compose restart"
echo ""
