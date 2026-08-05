# 🚀 Быстрый старт - Деплой So to Speak

> **Бюджет:** 600₽/мес | **Время:** 45 минут | **Хостинг:** Timeweb Cloud (Россия)

---

## ⚡ Быстрый деплой (3 команды)

```bash
# 1. Создать VPS на https://timeweb.cloud (Ubuntu 22.04, 2GB RAM)

# 2. Подключиться по SSH и настроить сервер
ssh root@YOUR_SERVER_IP "curl -fsSL https://raw.githubusercontent.com/YOUR_USERNAME/sotospeak/main/scripts/setup-server.sh | bash"

# 3. Запустить деплой
ssh root@YOUR_SERVER_IP "cd /opt/sotospeak && curl -fsSL https://raw.githubusercontent.com/YOUR_USERNAME/sotospeak/main/scripts/deploy-production.sh | bash -s -- --domain=sotospeak.ru --repo-url=https://github.com/YOUR_USERNAME/sotospeak.git"
```

---

## 📋 Пошаговая инструкция

### Шаг 1: Регистрация (5 минут)

| Сервис | Действие | Ссылка |
|--------|----------|--------|
| Timeweb Cloud | Создать аккаунт, пополнить на 600₽ | https://timeweb.cloud |
| reg.ru | Купить домен (sotospeak.ru) | https://reg.ru |

### Шаг 2: Создание VPS (5 минут)

В панели Timeweb:
```
Тип: Облачный сервер
Локация: Москва
ОС: Ubuntu 22.04 LTS
Тариф: Мощность (2 vCPU, 2GB RAM, 30GB NVMe)
Дополнительно: ✅ Автобэкапы
```

После создания запишите:
- **IP адрес:** `185.XXX.XXX.XXX`
- **Пароль root:** `********`

### Шаг 3: Настройка DNS (5 минут)

В панели управления доменом (reg.ru):
```
Тип: A
Имя: api
Значение: 185.XXX.XXX.XXX

Тип: A
Имя: admin
Значение: 185.XXX.XXX.XXX
```

Ждать: 5-15 минут

### Шаг 4: Деплой (30 минут)

Подключитесь по SSH:
```bash
ssh root@185.XXX.XXX.XXX
```

Выполните команды:
```bash
# Настройка сервера (~5 минут)
curl -fsSL https://raw.githubusercontent.com/YOUR_USERNAME/sotospeak/main/scripts/setup-server.sh | bash

# Переход в директорию
cd /opt/sotospeak

# Клонирование репозитория
git clone https://github.com/YOUR_USERNAME/sotospeak.git app

# Деплой (~20 минут)
./scripts/deploy-production.sh \
  --domain=sotospeak.ru \
  --api-subdomain=api \
  --admin-subdomain=admin
```

### Шаг 5: Проверка

Откройте в браузере:
- ✅ https://admin.sotospeak.ru - Admin panel
- ✅ https://api.sotospeak.ru/actuator/health - API health

Логин в админку:
- Email: `admin@sotospeak.com`
- Пароль: смотрите в `/root/.sotospeak-credentials`

---

## 💰 Стоимость

| Статья | Сумма |
|--------|-------|
| VPS Timeweb (2GB RAM) | 540₽/мес |
| Домен .ru | 50₽/мес |
| SSL (Let's Encrypt) | 0₽ |
| **ИТОГО** | **~600₽/мес** |

---

## 🔧 Обслуживание

```bash
# Подключение к серверу
ssh root@sotospeak.ru

# Просмотр статуса
cd /opt/sotospeak && docker-compose ps

# Логи
cd /opt/sotospeak && docker-compose logs -f backend

# Перезапуск
cd /opt/sotospeak && docker-compose restart

# Обновление приложения
cd /opt/sotospeak/app && git pull && cd .. && docker-compose up -d --build

# Бэкап вручную
cd /opt/sotospeak && ./backup.sh
```

---

## 📞 Поддержка

При проблемах:
1. Проверить логи: `docker-compose logs`
2. Проверить статус: `docker-compose ps`
3. Перезапустить: `docker-compose restart`

---

## 📚 Документация

- Полная инструкция: `PRODUCTION_DEPLOY_TIMEBEB.md`
- Feature Flags: `FEATURE_FLAGS_GUIDE.md`
- Master Plan: `PRODUCTION_DEPLOY_MASTER_PLAN.md`
