#!/bin/bash
# So to Speak Auto-Installer for Timeweb VPS
# Usage: curl -fsSL https://raw.githubusercontent.com/your/sotospeak/main/install-timeweb.sh | bash

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
INSTALL_DIR="/opt/sotospeak"
GITHUB_REPO="${GITHUB_REPO:-https://github.com/yourusername/sotospeak.git}"

# Logging
log() {
    echo -e "${GREEN}[$(date +%T)]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[$(date +%T)] WARNING:${NC} $1"
}

error() {
    echo -e "${RED}[$(date +%T)] ERROR:${NC} $1"
    exit 1
}

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    error "Please run as root (use sudo)"
fi

# Get server IP
SERVER_IP=$(curl -s ifconfig.me)
log "Server IP: $SERVER_IP"

# User input
read -p "Enter your domain (e.g., sotospeak.ru): " DOMAIN
read -p "Enter admin email for SSL: " ADMIN_EMAIL
read -sp "Enter admin password for So to Speak: " ADMIN_PASSWORD
echo

API_DOMAIN="api.${DOMAIN}"
ADMIN_PANEL_DOMAIN="admin.${DOMAIN}"

log "API will be available at: https://${API_DOMAIN}"
log "Admin panel will be available at: https://${ADMIN_PANEL_DOMAIN}"

# Confirmation
read -p "Continue? (y/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    error "Installation cancelled"
fi

# Step 1: System Update
log "Step 1/10: Updating system..."
apt update && apt upgrade -y
apt install -y curl wget git vim htop ufw fail2ban certbot python3-certbot-nginx

# Step 2: Setup Swap
log "Step 2/10: Setting up swap..."
if ! swapon --show | grep -q "/swapfile"; then
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    log "Swap created successfully"
else
    log "Swap already exists"
fi

# Step 3: Install Docker
log "Step 3/10: Installing Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    usermod -aG docker root
    rm get-docker.sh
    log "Docker installed"
else
    log "Docker already installed"
fi

# Install Docker Compose
if ! command -v docker-compose &> /dev/null; then
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    log "Docker Compose installed"
fi

# Step 4: Setup Firewall
log "Step 4/10: Configuring firewall..."
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable
log "Firewall configured"

# Step 5: Setup Fail2ban
log "Step 5/10: Configuring fail2ban..."
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
systemctl restart fail2ban
systemctl enable fail2ban
log "Fail2ban configured"

# Step 6: Create Project Structure
log "Step 6/10: Creating project structure..."
mkdir -p ${INSTALL_DIR}
cd ${INSTALL_DIR}
mkdir -p {nginx,postgres-init,certbot/conf,certbot/www,backups,docker}

# Generate passwords
DB_PASSWORD=$(openssl rand -base64 32)
MINIO_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 64)
GRAFANA_PASSWORD=$(openssl rand -base64 16)

# Save passwords
cat > ${INSTALL_DIR}/.env << EOF
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
ADMIN_EMAIL=${ADMIN_EMAIL}
ADMIN_PASSWORD=${ADMIN_PASSWORD}
ADMIN_DISPLAY_NAME=Admin

# Monitoring
GRAFANA_PASSWORD=${GRAFANA_PASSWORD}

# Domains
API_DOMAIN=${API_DOMAIN}
ADMIN_DOMAIN=${ADMIN_PANEL_DOMAIN}
EOF

# Save credentials
cat > ${INSTALL_DIR}/credentials.txt << EOF
So to Speak Credentials
========================
Generated: $(date)

Database:
  Host: localhost:5432
  User: postgres
  Password: ${DB_PASSWORD}
  Database: sotospeak

MinIO S3:
  Endpoint: http://localhost:9000
  Console: http://localhost:9001
  Access Key: minioadmin
  Secret Key: ${MINIO_PASSWORD}

So to Speak Admin:
  URL: https://${ADMIN_PANEL_DOMAIN}
  Email: ${ADMIN_EMAIL}
  Password: ${ADMIN_PASSWORD}

Grafana (if enabled):
  URL: http://${SERVER_IP}:3001
  User: admin
  Password: ${GRAFANA_PASSWORD}

IMPORTANT: Change default passwords after first login!
EOF

chmod 600 ${INSTALL_DIR}/.env
chmod 600 ${INSTALL_DIR}/credentials.txt

log "Credentials saved to ${INSTALL_DIR}/credentials.txt"

# Step 7: Clone Repository
log "Step 7/10: Cloning repository..."
if [ -d "${INSTALL_DIR}/app" ]; then
    warn "App directory already exists, skipping clone"
else
    git clone ${GITHUB_REPO} ${INSTALL_DIR}/app || warn "Could not clone repository. Please clone manually."
fi

# Step 8: Create Docker Compose
log "Step 8/10: Creating Docker Compose configuration..."

cat > ${INSTALL_DIR}/docker-compose.yml << 'COMPOSEEOF'
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
      dockerfile: ../../docker/Dockerfile.backend
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
      dockerfile: ../../docker/Dockerfile.admin
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
COMPOSEEOF

# Step 9: Create Dockerfiles
log "Step 9/10: Creating Dockerfiles..."

mkdir -p ${INSTALL_DIR}/docker

# Backend Dockerfile
cat > ${INSTALL_DIR}/docker/Dockerfile.backend << 'DOCKEREOF'
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon
COPY src ./src
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
DOCKEREOF

# Admin Dockerfile
cat > ${INSTALL_DIR}/docker/Dockerfile.admin << 'DOCKEREOF'
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1
CMD ["nginx", "-g", "daemon off;"]
DOCKEREOF

# Admin nginx config
mkdir -p ${INSTALL_DIR}/app/admin-web
cat > ${INSTALL_DIR}/app/admin-web/nginx.conf << 'NGINXEOF'
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;
    gzip on;
    gzip_vary on;
    gzip_types text/plain text/css text/xml application/json application/javascript application/rss+xml application/atom+xml image/svg+xml;
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    location / {
        try_files $uri $uri/ /index.html;
    }
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
NGINXEOF

# Main nginx config
cat > ${INSTALL_DIR}/nginx/nginx.conf << NGINXEOF
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
    server_tokens off;
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    limit_req_zone \$binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone \$binary_remote_addr zone=login:10m rate=5r/m;

    server {
        listen 80;
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
        server_name ${API_DOMAIN};
        ssl_certificate /etc/letsencrypt/live/${API_DOMAIN}/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/${API_DOMAIN}/privkey.pem;
        ssl_trusted_certificate /etc/letsencrypt/live/${API_DOMAIN}/chain.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        location / {
            limit_req zone=api burst=20 nodelay;
            proxy_pass http://backend:8080;
            proxy_http_version 1.1;
            proxy_set_header Host \$host;
            proxy_set_header X-Real-IP \$remote_addr;
            proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto \$scheme;
        }
    }

    server {
        listen 80;
        server_name ${ADMIN_PANEL_DOMAIN};
        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
        }
        location / {
            return 301 https://\$server_name\$request_uri;
        }
    }

    server {
        listen 443 ssl http2;
        server_name ${ADMIN_PANEL_DOMAIN};
        ssl_certificate /etc/letsencrypt/live/${ADMIN_PANEL_DOMAIN}/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/${ADMIN_PANEL_DOMAIN}/privkey.pem;
        ssl_trusted_certificate /etc/letsencrypt/live/${ADMIN_PANEL_DOMAIN}/chain.pem;
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        location /api/ {
            proxy_pass http://backend:8080/api/;
            proxy_http_version 1.1;
            proxy_set_header Host \$host;
        }
        location / {
            proxy_pass http://admin:80;
            proxy_http_version 1.1;
            proxy_set_header Host \$host;
        }
    }
}
NGINXEOF

# Step 10: SSL and Start
log "Step 10/10: Setting up SSL and starting services..."

# Start nginx for certbot
cd ${INSTALL_DIR}
docker-compose up -d nginx

log "Waiting for nginx to start..."
sleep 5

# Get SSL certificates
log "Obtaining SSL certificates from Let's Encrypt..."
docker-compose run --rm certbot certonly \
    --standalone \
    --preferred-challenges http \
    -d ${API_DOMAIN} \
    -d ${ADMIN_PANEL_DOMAIN} \
    --agree-tos \
    -m ${ADMIN_EMAIL} \
    --no-eff-email || warn "SSL certificate issuance failed. You may need to run this again after DNS propagation."

# Restart nginx with SSL
docker-compose restart nginx

# Start all services
log "Starting all services..."
docker-compose up -d

log "Waiting for services to initialize..."
sleep 30

# Setup MinIO bucket
docker exec sotospeak-minio sh -c "mc alias set local http://localhost:9000 minioadmin ${MINIO_PASSWORD} && mc mb local/sotospeak 2>/dev/null || true && mc policy set download local/sotospeak" || warn "Could not setup MinIO bucket automatically"

# Create backup script
cat > ${INSTALL_DIR}/backup.sh << 'BACKUPEOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/sotospeak/backups"
mkdir -p ${BACKUP_DIR}
docker exec sotospeak-postgres pg_dump -U postgres sotospeak | gzip > ${BACKUP_DIR}/postgres_${DATE}.sql.gz
tar -czf ${BACKUP_DIR}/config_${DATE}.tar.gz -C /opt/sotospeak docker-compose.yml .env nginx/
find ${BACKUP_DIR} -name "*.sql.gz" -mtime +7 -delete
find ${BACKUP_DIR} -name "*.tar.gz" -mtime +7 -delete
echo "Backup completed: ${DATE}"
BACKUPEOF

chmod +x ${INSTALL_DIR}/backup.sh

# Add to cron
(crontab -l 2>/dev/null; echo "0 3 * * * /opt/sotospeak/backup.sh >> /var/log/sotospeak-backup.log 2>&1") | crontab -

# Final status
log "=========================================="
log "Installation Complete!"
log "=========================================="
log ""
log "Services Status:"
docker-compose ps
log ""
log "Access Information:"
log "  Admin Panel: https://${ADMIN_PANEL_DOMAIN}"
log "  API: https://${API_DOMAIN}"
log "  Server IP: ${SERVER_IP}"
log ""
log "Credentials saved in: ${INSTALL_DIR}/credentials.txt"
log "  cat ${INSTALL_DIR}/credentials.txt"
log ""
log "Useful Commands:"
log "  cd ${INSTALL_DIR} && docker-compose logs -f"
log "  cd ${INSTALL_DIR} && docker-compose restart"
log "  cd ${INSTALL_DIR} && ./backup.sh"
log ""
log "Don't forget to:"
log "  1. Point your domain DNS to ${SERVER_IP}"
log "  2. Change default admin password after first login"
log "  3. Setup regular backups"
log ""
log "=========================================="
