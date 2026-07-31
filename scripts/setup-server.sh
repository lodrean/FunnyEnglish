#!/bin/bash
# =============================================================================
# FunnyEnglish Server Setup Script
# Настройка сервера Ubuntu 22.04 LTS для production deploy
# =============================================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 FunnyEnglish Server Setup${NC}"
echo "================================"

# Проверка root прав
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}❌ Ошибка: Запустите скрипт от root (sudo)${NC}"
    exit 1
fi

# =============================================================================
# Шаг 1: Обновление системы
# =============================================================================
echo -e "\n${YELLOW}📦 Шаг 1: Обновление системы...${NC}"
apt update && apt upgrade -y
echo -e "${GREEN}✅ Система обновлена${NC}"

# =============================================================================
# Шаг 2: Установка базовых утилит
# =============================================================================
echo -e "\n${YELLOW}📦 Шаг 2: Установка утилит...${NC}"
apt install -y \
    curl \
    wget \
    git \
    vim \
    htop \
    ufw \
    fail2ban \
    certbot \
    python3-certbot-nginx \
    openssl \
    ncdu \
    tree
echo -e "${GREEN}✅ Утилиты установлены${NC}"

# =============================================================================
# Шаг 3: Настройка Swap (критично для 2GB RAM!)
# =============================================================================
echo -e "\n${YELLOW}💾 Шаг 3: Настройка Swap...${NC}"

if ! swapon --show | grep -q "/swapfile"; then
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    echo -e "${GREEN}✅ Swap 2GB создан${NC}"
else
    echo -e "${GREEN}✅ Swap уже существует${NC}"
fi

# Оптимизация swap
sysctl vm.swappiness=10
sysctl vm.vfs_cache_pressure=50
echo 'vm.swappiness=10' >> /etc/sysctl.conf
echo 'vm.vfs_cache_pressure=50' >> /etc/sysctl.conf

# =============================================================================
# Шаг 4: Установка Docker
# =============================================================================
echo -e "\n${YELLOW}🐳 Шаг 4: Установка Docker...${NC}"

if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com | sh
    usermod -aG docker root
    systemctl enable docker
    systemctl start docker
    echo -e "${GREEN}✅ Docker установлен${NC}"
else
    echo -e "${GREEN}✅ Docker уже установлен${NC}"
fi

# =============================================================================
# Шаг 5: Установка Docker Compose
# =============================================================================
echo -e "\n${YELLOW}🐳 Шаг 5: Установка Docker Compose...${NC}"

if ! command -v docker-compose &> /dev/null; then
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}✅ Docker Compose установлен${NC}"
else
    echo -e "${GREEN}✅ Docker Compose уже установлен${NC}"
fi

# =============================================================================
# Шаг 6: Настройка Firewall
# =============================================================================
echo -e "\n${YELLOW}🔥 Шаг 6: Настройка Firewall...${NC}"

ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp comment 'SSH'
ufw allow 80/tcp comment 'HTTP'
ufw allow 443/tcp comment 'HTTPS'

# Включаем firewall если он не активен
if ! ufw status | grep -q "Status: active"; then
    ufw --force enable
fi

echo -e "${GREEN}✅ Firewall настроен${NC}"

# =============================================================================
# Шаг 7: Настройка Fail2ban
# =============================================================================
echo -e "\n${YELLOW}🛡️ Шаг 7: Настройка Fail2ban...${NC}"

cat > /etc/fail2ban/jail.local << 'EOF'
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 3
backend = systemd

[sshd]
enabled = true
port = ssh
filter = sshd
logpath = /var/log/auth.log
maxretry = 3

[nginx-http-auth]
enabled = true
filter = nginx-http-auth
port = http,https
logpath = /var/log/nginx/error.log

[nginx-limit-req]
enabled = true
filter = nginx-limit-req
port = http,https
logpath = /var/log/nginx/error.log
EOF

systemctl restart fail2ban
systemctl enable fail2ban

echo -e "${GREEN}✅ Fail2ban настроен${NC}"

# =============================================================================
# Шаг 8: Оптимизация системы для PostgreSQL
# =============================================================================
echo -e "\n${YELLOW}⚙️ Шаг 8: Оптимизация системы...${NC}"

cat >> /etc/sysctl.conf << 'EOF'

# PostgreSQL optimizations
kernel.shmmax=536870912
kernel.shmall=131072
vm.overcommit_memory=2
vm.overcommit_ratio=80
fs.file-max=65536
EOF

sysctl -p

echo -e "${GREEN}✅ Система оптимизирована${NC}"

# =============================================================================
# Шаг 9: Настройка timezone
# =============================================================================
echo -e "\n${YELLOW}🌍 Шаг 9: Настройка timezone...${NC}"
timedatectl set-timezone Europe/Moscow
echo -e "${GREEN}✅ Timezone установлен: Europe/Moscow${NC}"

# =============================================================================
# Шаг 10: Создание директории проекта
# =============================================================================
echo -e "\n${YELLOW}📁 Шаг 10: Создание структуры проекта...${NC}"

mkdir -p /opt/funnyenglish/{nginx,postgres-init,certbot/{conf,www},backups}

echo -e "${GREEN}✅ Структура создана: /opt/funnyenglish${NC}"

# =============================================================================
# Готово!
# =============================================================================
echo ""
echo -e "${GREEN}🎉 Настройка сервера завершена!${NC}"
echo "================================"
echo ""
echo "Следующие шаги:"
echo "1. Клонируйте проект: cd /opt/funnyenglish && git clone <repo> app"
echo "2. Запустите деплой: ./scripts/deploy-production.sh"
echo ""
echo "Полезные команды:"
echo "  - docker --version    # Проверка Docker"
echo "  - ufw status          # Статус firewall"
echo "  - fail2ban-client status  # Статус защиты"
echo "  - free -h             # Проверка памяти"
echo ""
