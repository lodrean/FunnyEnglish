# 🚀 So to Speak - Production Deploy на Timeweb VPS

> **Цель:** Развернуть Backend + Admin + PostgreSQL + MinIO на российском VPS за 600₽/мес  
> **Время:** ~45 минут  
> **Результат:** Работающий https://admin.sotospeak.ru и https://api.sotospeak.ru

---

## 📋 Содержание

1. [Подготовка](#часть-1-подготовка-5-минут)
2. [Настройка VPS](#часть-2-настройка-vps-10-минут)
3. [Деплой проекта](#часть-3-деплой-проекта-15-минут)
4. [Домен и SSL](#часть-4-домен-и-ssl-10-минут)
5. [Проверка и запуск](#часть-5-проверка-и-запуск-5-минут)
6. [Обслуживание](#часть-6-обслуживание)

---

## Часть 1: Подготовка (5 минут)

### 1.1 Регистрация в Timeweb Cloud

1. Откройте https://timeweb.cloud
2. Нажмите **"Зарегистрироваться"**
3. Введите email и пароль
4. Подтвердите email
5. Пополните баланс минимум на 600₽ (один месяц работы)

### 1.2 Регистрация домена (если ещё нет)

**Вариант A: reg.ru**
1. Перейдите на https://reg.ru
2. Введите желаемый домен (например: `sotospeak.ru`)
3. Добавьте в корзину и оплатите (~600₽/год)

**Вариант B: nic.ru**
1. Перейдите на https://nic.ru
2. Проверьте доступность домена
3. Зарегистрируйте

### 1.3 Проверка требований

Перед началом убедитесь, что у вас есть:
- [ ] Аккаунт Timeweb Cloud с балансом
- [ ] Зарегистрированный домен
- [ ] Доступ к управлению DNS домена
- [ ] SSH клиент (Windows: PowerShell или PuTTY, Mac/Linux: Terminal)

---

## Часть 2: Настройка VPS (10 минут)

### 2.1 Создание VPS

1. В панели Timeweb нажмите **"Создать сервер"**
2. Выберите конфигурацию:
   ```
   Тип: Облачный сервер
   Локация: Москва (или ближайшая к вам)
   Операционная система: Ubuntu 22.04 LTS
   Тариф: Мощность (2 vCPU, 2GB RAM, 30GB NVMe)
   ```
3. Дополнительные опции:
   ```
   ✅ Автоматические бэкапы (рекомендуется +54₽/мес)
   ✅ Защита от DDoS (бесплатно)
   ```
4. Нажмите **"Создать"**
5. Ждите 2-3 минуты пока сервер создаётся

### 2.2 Получение доступа

После создания вам выдадут:
```
IP адрес: 185.XXX.XXX.XXX (запишите!)
Логин: root
Пароль: ******** (скопируйте!)
```

**⚠️ ВАЖНО:** Сохраните эти данные в надёжном месте!

### 2.3 Подключение по SSH

**Windows (PowerShell):**
```powershell
ssh root@185.XXX.XXX.XXX
# Введите пароль при запросе
```

**Mac/Linux:**
```bash
ssh root@185.XXX.XXX.XXX
```

### 2.4 Автоматическая настройка сервера

Запустите установочный скрипт (выполняется ~5 минут):

```bash
# Скачиваем и запускаем скрипт настройки
curl -fsSL https://raw.githubusercontent.com/yourusername/sotospeak/main/scripts/setup-server.sh | bash
```

Или вручную по шагам:

```bash
# 1. Обновление системы
apt update && apt upgrade -y

# 2. Установка базовых утилит
apt install -y curl wget git vim htop ufw fail2ban certbot python3-certbot-nginx

# 3. Создание swap (критично для 2GB RAM!)
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

# 4. Установка Docker
curl -fsSL https://get.docker.com | sh
usermod -aG docker root

# 5. Установка Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 6. Настройка Firewall
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

# 7. Настройка Fail2ban
cat > /etc/fail2ban/jail.local << 'EOF'
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
systemctl restart fail2ban
systemctl enable fail2ban

echo "✅ Базовая настройка сервера завершена!"
```

---

## Часть 3: Деплой проекта (15 минут)

### 3.1 Автоматический деплой (рекомендуется)

```bash
# Создаём директорию проекта
mkdir -p /opt/sotospeak
cd /opt/sotospeak

# Скачиваем скрипт деплоя
curl -fsSL https://raw.githubusercontent.com/yourusername/sotospeak/main/scripts/deploy-production.sh -o deploy.sh
chmod +x deploy.sh

# Запускаем деплой
./deploy.sh \
  --domain=sotospeak.ru \
  --api-subdomain=api \
  --admin-subdomain=admin
```

### 3.2 Ручной деплой

Если автоматический скрипт недоступен, выполните шаги вручную:

```bash
# 1. Создание структуры
mkdir -p /opt/sotospeak/{nginx,postgres-init,certbot/{conf,www},backups}
cd /opt/sotospeak

# 2. Клонирование репозитория
git clone https://github.com/yourusername/sotospeak.git app

# 3. Создание .env файла
DOMAIN="sotospeak.ru"
DB_PASSWORD=$(openssl rand -base64 32)
MINIO_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 64)
ADMIN_PASSWORD=$(openssl rand -base64 16)

cat > .env << EOF
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

# Domains
API_DOMAIN=api.${DOMAIN}
ADMIN_DOMAIN=admin.${DOMAIN}
EOF

# Сохраняем пароли
echo "Database: ${DB_PASSWORD}" > /root/.sotospeak-credentials
echo "MinIO: ${MINIO_PASSWORD}" >> /root/.sotospeak-credentials
echo "Admin: ${ADMIN_PASSWORD}" >> /root/.sotospeak-credentials
chmod 600 /root/.sotospeak-credentials

echo "✅ Проект подготовлен!"
echo "📁 Пароли сохранены в /root/.sotospeak-credentials"
```

### 3.3 Создание Docker Compose

```bash
cat > /opt/sotospeak/docker-compose.yml << 'EOF'
version: '3.8'

services:
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
    container_name: sotospeak-minio
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

  nginx:
    image: nginx:alpine
    container_name: sotospeak-nginx
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
    container_name: sotospeak-certbot
    volumes:
      - ./certbot/conf:/etc/letsencrypt
      - ./certbot/www:/var/www/certbot
    entrypoint: "/bin/sh -c 'trap exit TERM; while :; do certbot renew; sleep 12h & wait $${!}; done;'"

volumes:
  postgres_data:
  minio_data:
EOF
```

---

## Часть 4: Домен и SSL (10 минут)

### 4.1 Настройка DNS

В панели управления вашего домена (reg.ru, nic.ru и т.д.) создайте A-записи:

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

**Или через CNAME:**
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

**⏱️ Ожидание:** DNS обновляется 5-15 минут

### 4.2 Получение SSL сертификатов

```bash
cd /opt/sotospeak

# Запускаем nginx
docker-compose up -d nginx

# Ждём 10 секунд
sleep 10

# Получаем сертификаты Let's Encrypt
docker-compose run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email admin@sotospeak.com \
    --agree-tos \
    --no-eff-email \
    -d api.sotospeak.ru \
    -d admin.sotospeak.ru

# Перезапускаем nginx
docker-compose restart nginx
```

### 4.3 Проверка SSL

```bash
# Проверка API
curl -I https://api.sotospeak.ru/actuator/health

# Должен показать: HTTP/2 200
```

Откройте в браузере:
- https://admin.sotospeak.ru - должен быть зелёный замок 🔒

---

## Часть 5: Проверка и запуск (5 минут)

### 5.1 Запуск всех сервисов

```bash
cd /opt/sotospeak

# Сборка и запуск
docker-compose up -d --build

# Ждём инициализации
echo "Ожидание запуска сервисов..."
sleep 60
```

### 5.2 Проверка статуса

```bash
# Статус контейнеров
docker-compose ps

# Должно показать:
# NAME                     STATUS
# sotospeak-admin       Up
# sotospeak-backend     Up (healthy)
# sotospeak-postgres    Up (healthy)
# sotospeak-minio       Up
# sotospeak-nginx       Up

# Логи backend
docker-compose logs backend | tail -20

# Health check API
curl https://api.sotospeak.ru/actuator/health
# {"status":"UP"}
```

### 5.3 Настройка MinIO

```bash
# Установка mc (MinIO Client)
wget https://dl.min.io/client/mc/release/linux-amd64/mc -O /usr/local/bin/mc
chmod +x /usr/local/bin/mc

# Настройка alias
source /opt/sotospeak/.env
mc alias set local http://localhost:9000 minioadmin "${MINIO_PASSWORD}"

# Создание bucket
mc mb local/sotospeak

# Публичный доступ для чтения
mc policy set download local/sotospeak

echo "✅ MinIO настроен!"
```

### 5.4 Первый вход в админку

1. Откройте https://admin.sotospeak.ru
2. Войдите с учётными данными:
   - Email: `admin@sotospeak.com`
   - Пароль: (из файла `/root/.sotospeak-credentials`)
3. **Сразу смените пароль!**

---

## Часть 6: Обслуживание

### 6.1 Автоматические бэкапы

```bash
# Создание скрипта бэкапа
cat > /opt/sotospeak/backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/sotospeak/backups"
RETENTION_DAYS=7

mkdir -p ${BACKUP_DIR}

# Бэкап PostgreSQL
docker exec sotospeak-postgres pg_dump -U postgres sotospeak | gzip > ${BACKUP_DIR}/postgres_${DATE}.sql.gz

# Бэкап MinIO
tar -czf ${BACKUP_DIR}/minio_${DATE}.tar.gz -C /opt/sotospeak/docker-compose.yml .env nginx/

# Удаление старых бэкапов
find ${BACKUP_DIR} -name "*.gz" -mtime +${RETENTION_DAYS} -delete

echo "[$(date)] Backup completed: ${DATE}"
EOF

chmod +x /opt/sotospeak/backup.sh

# Настройка cron (ежедневно в 3:00)
(crontab -l 2>/dev/null; echo "0 3 * * * /opt/sotospeak/backup.sh >> /var/log/sotospeak-backup.log 2>&1") | crontab -

echo "✅ Автоматические бэкапы настроены!"
```

### 6.2 Обновление приложения

```bash
cd /opt/sotospeak

# Получение обновлений
cd app && git pull && cd ..

# Пересборка и перезапуск
docker-compose up -d --build

# Очистка старых образов
docker system prune -f
```

### 6.3 Мониторинг

```bash
# Статус сервисов
docker-compose ps

# Использование ресурсов
docker stats --no-stream

# Логи в реальном времени
docker-compose logs -f backend

# Проверка диска
df -h

# Проверка памяти
free -h
```

### 6.4 Полезные команды

```bash
# Перезапуск сервиса
docker-compose restart backend

# Остановка всех сервисов
docker-compose down

# Полный сброс (удаление данных!)
docker-compose down -v

# Просмотр логов
docker-compose logs --tail=100 backend

# Вход в контейнер
docker exec -it sotospeak-postgres psql -U postgres -d sotospeak
```

---

## 🔧 Устранение неполадок

### Проблема: "Cannot connect to server"

```bash
# Проверка firewall
ufw status

# Проверка DNS
dig api.sotospeak.ru

# Проверка nginx
docker-compose logs nginx
```

### Проблема: "502 Bad Gateway"

```bash
# Проверка backend
docker-compose logs backend

# Перезапуск backend
docker-compose restart backend
```

### Проблема: "Out of memory"

```bash
# Добавление swap (если не добавлен)
swapon --show
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile

# Перезапуск с ограничением памяти
docker-compose down
docker-compose up -d
```

### Проблема: SSL сертификат не обновляется

```bash
# Ручное обновление
docker-compose run --rm certbot renew

# Перезапуск nginx
docker-compose restart nginx
```

---

## 📞 Поддержка

Если что-то не работает:
1. Проверьте логи: `docker-compose logs`
2. Проверьте статус: `docker-compose ps`
3. Перезапустите: `docker-compose restart`

---

## ✅ Чек-лист завершения

- [ ] VPS создан и настроен
- [ ] Домен настроен с DNS записями
- [ ] SSL сертификаты получены
- [ ] Backend отвечает на https://api.sotospeak.ru
- [ ] Admin panel доступна на https://admin.sotospeak.ru
- [ ] Можно войти в админку
- [ ] Бэкапы настроены
- [ ] Мониторинг работает

**🎉 Поздравляем! So to Speak запущен в production!**
