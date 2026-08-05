# 🚀 So to Speak на Timeweb - Быстрый Старт

## ⚡ Минимальные Действия (3 шага)

### Шаг 1: Купить VPS (5 минут)
```
https://timeweb.cloud → Облачные серверы → Мощность (2GB RAM, 30GB NVMe)
→ Ubuntu 22.04 → Оплатить
```
**Получите:** IP адрес, логин `root`, пароль

### Шаг 2: Подключиться и запустить скрипт (5 минут)

```bash
# На Windows - PowerShell или Git Bash
ssh root@ВАШ_IP

# На сервере выполнить:
curl -fsSL https://sotospeak.ru/install.sh | bash

# Или если хотите ручную настройку:
# git clone https://github.com/your/sotospeak.git
# cd sotospeak && ./deploy/timeweb.sh
```

### Шаг 3: Настроить DNS (5 минут)

В панели управления доменом (reg.ru / nic.ru):
```
Тип: A | Имя: api    | Значение: ВАШ_IP
Тип: A | Имя: admin  | Значение: ВАШ_IP
```

Ждём 5-15 минут...

---

## ✅ Проверка

Открываем в браузере:
- **https://admin.sotospeak.ru** ← Admin panel
- **https://api.sotospeak.ru/actuator/health** ← API health

Логин: `admin@sotospeak.com`  
Пароль: (тот что указали при установке или `admin123`)

---

## 📊 Что Получаете

```
┌─────────────────────────────────────────┐
│  Timeweb VPS (590₽/мес)                 │
│                                         │
│  ✓ PostgreSQL (2GB настроено)          │
│  ✓ MinIO S3 (файлы/картинки)           │
│  ✓ Backend API (Spring Boot)           │
│  ✓ Admin Panel (React + Nginx)         │
│  ✓ SSL (Let's Encrypt, автообновление) │
│  ✓ Firewall + Fail2ban                 │
│  ✓ Автобэкапы (ежедневно)              │
│  ✓ Мониторинг (опционально)            │
└─────────────────────────────────────────┘
```

---

## 🎛️ Управление

### Полезные команды

```bash
# Подключиться к серверу
ssh root@ВАШ_IP

# Посмотреть статус
cd /opt/sotospeak && docker-compose ps

# Посмотреть логи
cd /opt/sotospeak && docker-compose logs -f backend
cd /opt/sotospeak && docker-compose logs -f admin
cd /opt/sotospeak && docker-compose logs -f nginx

# Перезапуск
cd /opt/sotospeak && docker-compose restart

# Обновление после git pull
cd /opt/sotospeak && docker-compose up -d --build

# Ручной бэкап
cd /opt/sotospeak && ./backup.sh

# Проверить использование ресурсов
free -h
docker stats --no-stream
```

### Файлы и Директории

```
/opt/sotospeak/
├── docker-compose.yml      # Основная конфигурация
├── .env                    # Пароли (никому не показывать!)
├── credentials.txt         # Сохранённые пароли
├── app/                    # Исходный код (git clone)
├── nginx/                  # Nginx конфиги
├── backups/                # Автобэкапы
└── docker/                 # Dockerfiles
```

---

## 🔐 Безопасность

### Сменить пароль Admin
1. Залогиниться в https://admin.sotospeak.ru
2. Settings → Profile → Change Password

### Посмотреть все пароли
```bash
cat /opt/sotospeak/credentials.txt
```

### SSH ключ вместо пароля (рекомендуется)
```bash
# На вашем компьютере:
ssh-copy-id root@ВАШ_IP

# Теперь можно входить без пароля:
ssh root@ВАШ_IP
```

---

## 🆘 Если Что-то Не Работает

### Сайт не открывается
```bash
# 1. Проверить DNS
nslookup admin.sotospeak.ru
# Должен показывать ВАШ_IP

# 2. Проверить что сервисы работают
cd /opt/sotospeak && docker-compose ps

# 3. Перезапустить
cd /opt/sotospeak && docker-compose restart
```

### SSL ошибка
```bash
# Перевыпустить сертификаты
cd /opt/sotospeak
docker-compose run --rm certbot certonly --force-renew \
  -d api.sotospeak.ru -d admin.sotospeak.ru
docker-compose restart nginx
```

### Недостаточно памяти
```bash
# Проверить
free -h

# Увеличить swap
swapoff -a
fallocate -l 4G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
```

---

## 📈 Масштабирование

### Нужно больше ресурсов?
```
Timeweb Cloud → Ваш сервер → Изменить конфигурацию
→ Выбрать больше RAM/CPU → Перезагрузить
```

Всё сохранится, просто перезапустить Docker:
```bash
cd /opt/sotospeak && docker-compose up -d
```

---

## 💰 Стоимость

| Статья | Цена |
|--------|------|
| VPS (2GB RAM, 30GB NVMe) | 540₽/мес |
| Домен .ru | 50₽/мес |
| SSL | 0₽ |
| **ИТОГО** | **590₽/мес** |

---

## 📞 Поддержка

- **Timeweb:** https://timeweb.cloud/support (24/7)
- **Документация:** https://docs.timeweb.cloud
- **Telegram:** @timeweb_cloud

---

**Готово к работе!** 🎉
