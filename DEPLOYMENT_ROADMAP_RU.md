# 🚀 FunnyEnglish - Roadmap к Production Deploy (Российские ресурсы)

## 📋 Executive Summary

| Параметр | Значение |
|----------|----------|
| **Текущий статус** | MVP Complete, E2E тесты 100% |
| **Целевая инфраструктура** | Yandex Cloud / Selectel |
| **Ориентировочный срок** | 6-8 недель |
| **Бюджет (ориентир)** | 15-25 тыс. руб./мес |

---

## 🗓️ Phase 1: Pre-Production Подготовка (Недели 1-2)

### 1.1 Quality Gates & Финальное Тестирование

#### ✅ E2E Тестирование (Приоритет: Critical)
```markdown
[ ] Добавить E2E тесты для Image-Word-Match (playwright) - в процессе
[ ] Покрытие критических пользовательских сценариев:
    - Регистрация / Авторизация
    - Прохождение теста
    - Начисление очков и уровней
    - Drag-and-drop на мобильных
[ ] Настроить parallel execution для скорости
[ ] Добавить visual regression тесты (Chromatic)
[ ] Интеграция с Telegram для отчетов
```

**Владелец:** QA Engineer  
**Выход:** Отчет о покрытии >85%

#### ✅ Мобильное Тестирование
```markdown
[ ] Maestro тесты для Android (уже начато)
[ ] Тестирование на реальных устройствах (Firebase Test Lab → Yandex.Test или ручное)
[ ] Проверка offline-режима
[ ] Тестирование push-уведомлений
[ ] Performance testing (запуск < 3 сек, использование памяти < 200MB)
```

#### ✅ Backend Integration Tests
```markdown
[ ] Добавить недостающий тест (сейчас 6/7)
[ ] Нагрузочное тестирование API (k6 / Yandex.Tank)
    - Целевые метрики: 1000 RPS, latency p95 < 200ms
[ ] Тестирование миграций БД
[ ] Проверка работы с S3 (Yandex Object Storage)
```

### 1.2 Анализ и Приведение Дизайна

#### 🎨 Design System Audit
```markdown
[ ] Завершить миграцию всех компонентов на Material You
[ ] Проверить accessibility (WCAG 2.1 AA):
    - Контрастность текста
    - Screen reader labels
    - Keyboard navigation
[ ] Адаптация для RTL языков (на будущее)
[ ] Dark mode тестирование
[ ] Проверка на малых экранах (< 360dp)
```

**Инструменты:** Figma,axe DevTools, Accessibility Scanner

#### 📱 UI/UX Тестирование
```markdown
[ ] Скриншот-тесты всех экранов
[ ] Анимации: проверка 60fps
[ ] LCP (Largest Contentful Paint) для Web < 2.5s
[ ] Интерактивные элементы ≥ 44×44dp (mobile)
```

### 1.3 Security Audit
```markdown
[ ] OWASP Top 10 проверка
[ ] Аудит зависимостей (OWASP Dependency-Check)
[ ] Проверка secrets (gitleaks / truffleHog)
[ ] Настройка CSP headers
[ ] Rate limiting тестирование (уже есть, проверить)
[ ] SQL Injection тесты
[ ] XSS тестирование
```

---

## 🗓️ Phase 2: DevOps & Инфраструктура (Недели 2-4)

### 2.1 Выбор и Настройка Платформы

#### 🏗️ Инфраструктурные Решения (Российские альтернативы)

| Зарубежный сервис | Российская альтернатива | Выбор |
|-------------------|------------------------|-------|
| AWS/GCP/Azure | Yandex Cloud, Selectel, Timeweb Cloud | **Yandex Cloud** |
| GitHub Actions | GitFlic CI, RuGit, self-hosted GitLab | **GitFlic + self-hosted Runner** |
| Docker Hub | Yandex Container Registry, Harbor | **YCR** |
| Sentry | Self-hosted Sentry или ELK | **Self-hosted Sentry** |
| Firebase Analytics | AppMetrica, MyTracker | **AppMetrica** |
| SendGrid/AWS SES | Mail.ru Cloud Solutions, Unisender | **Mail.ru / SMTP** |
| CloudFlare | Yandex Cloud CDN, DDoS-Guard | **Yandex CDN** |

#### 💰 Примерная схема затрат (Yandex Cloud)

```yaml
Compute:
  - Kubernetes (3 nodes, 4 vCPU, 8GB RAM each): ~8,000 ₽/мес
  
Storage:
  - PostgreSQL Managed (2 vCPU, 8GB): ~3,500 ₽/мес
  - Object Storage (100GB): ~300 ₽/мес
  - Container Registry: ~500 ₽/мес

Networking:
  - Load Balancer: ~1,500 ₽/мес
  - CDN (100GB трафика): ~400 ₽/мес
  - Public IP: ~300 ₽/мес

Итого базовая конфигурация: ~14,500 ₽/мес
```

### 2.2 CI/CD Pipeline

#### 🔄 GitFlic CI / GitLab CI Конфигурация
```markdown
[ ] Настроить self-hosted runner (Yandex Cloud VM)
[ ] Pipeline stages:
    ├─ lint (ktlint, detekt, eslint, prettier)
    ├─ test (unit, integration)
    ├─ build (backend Docker, admin-web Docker)
    ├─ security-scan (trivy, sonarqube)
    ├─ e2e (playwright в Docker)
    └─ deploy (helm/kubectl)
[ ] Кэширование gradle dependencies
[ ] Кэширование npm dependencies
[ ] Parallel jobs для скорости
[ ] Telegram notifications для pipeline status
```

**Файлы для создания:**
- `.gitflic-ci.yml` или `.gitlab-ci.yml`
- `docker/Dockerfile.backend.prod`
- `docker/Dockerfile.admin.prod`
- `helm/` чарты для Kubernetes

### 2.3 Container Orchestration

#### ☸️ Kubernetes в Yandex Cloud
```markdown
[ ] Создать Managed Kubernetes кластер
[ ] Настроить Ingress (NGINX + cert-manager для TLS)
[ ] Настроить HPA (Horizontal Pod Autoscaler)
[ ] Настроить PDB (Pod Disruption Budget)
[ ] Network Policies для изоляции
[ ] Resource limits для всех контейнеров
```

**Helm Charts структура:**
```
helm/
├── funnyenglish/
│   ├── Chart.yaml
│   ├── values.yaml
│   ├── values-production.yaml
│   └── templates/
│       ├── backend-deployment.yaml
│       ├── admin-deployment.yaml
│       ├── ingress.yaml
│       ├── configmap.yaml
│       └── secrets.yaml
```

### 2.4 Мониторинг и Логирование

#### 📊 Observability Stack
```markdown
[ ] Prometheus + Grafana (Yandex Monitoring или self-hosted)
[ ] Loki для логов
[ ] Jaeger для distributed tracing
[ ] Алерты в Telegram (alertmanager)
[ ] Dashboard'ы:
    - JVM метрики (heap, GC, threads)
    - HTTP requests (RPS, latency, errors)
    - Database connections
    - Business metrics (DAU, MAU, completion rate)
```

**Интеграция с мобильным приложением:**
```markdown
[ ] Настроить AppMetrica SDK
[ ] Custom events (test_started, test_completed, level_up)
[ ] Crash reporting
[ ] Funnel analysis
```

---

## 🗓️ Phase 3: Production-Ready Код (Недели 3-5)

### 3.1 Backend Оптимизации

#### ⚡ Performance
```markdown
[ ] Connection pooling (HikariCP tuning)
[ ] Redis для кэширования (сессии, leaderboard)
[ ] Database query optimization (N+1 проверка)
[ ] Pagination для всех list endpoints
[ ] Async processing для тяжелых операций
[ ] Read replicas для PostgreSQL (при необходимости)
```

#### 🔧 Configuration Management
```markdown
[ ] Externalize configuration (не в JAR)
[ ] Spring Cloud Config или просто env vars
[ ] Feature flags система (встроенная в core/toggle)
[ ] Secrets management (Yandex Lockbox)
```

### 3.2 Admin Panel Оптимизации

#### 🌐 Web Performance
```markdown
[ ] Lazy loading для route-based splitting
[ ] Image optimization (WebP, responsive images)
[ ] Service Worker для offline
[ ] Bundle analysis и оптимизация
[ ] Preload critical resources
```

### 3.3 Mobile Release Preparation

#### 📱 Android Release
```markdown
[ ] ProGuard / R8 minification
[ ] App signing (создать keystore)
[ ] App Bundle (AAB) вместо APK
[ ] Play Console / RuStore publishing
[ ] In-app update API
[ ] Обрезка ресурсов для разных dpi
```

#### 🖥️ Desktop Release
```markdown
[ ] Windows installer (MSI/EXE)
[ ] macOS notarization
[ ] Linux AppImage/Snap
[ ] Auto-update механизм
```

---

## 🗓️ Phase 4: Data & Backup Strategy (Недели 4-5)

### 4.1 Database
```markdown
[ ] Настроить automated backups (Yandex Managed PostgreSQL)
[ ] Point-in-time recovery (PITR)
[ ] Cross-region backup replication
[ ] Тестирование restore процедуры
[ ] Database migration strategy (Flyway уже есть, проверить)
```

### 4.2 File Storage (S3)
```markdown
[ ] Миграция с MinIO на Yandex Object Storage
[ ] Настройка lifecycle policies
[ ] CDN интеграция для images
[ ] Backup в другой регион
```

---

## 🗓️ Phase 5: Закрытый Beta & Staging (Недели 5-6)

### 5.1 Staging Environment
```markdown
[ ] Полное копирование production конфигурации
[ ] Пониженные ресурсы (для экономии)
[ ] Автоматический деплой из develop ветки
[ ] Smoke tests после каждого деплоя
```

### 5.2 Beta Testing
```markdown
[ ] Закрытый список бета-тестеров (50-100 человек)
[ ] Firebase App Distribution / RuStore тестирование
[ ] Сбор обратной связи через форму
[ ] Crash reporting анализ
[ ] Perfomance metrics анализ
```

---

## 🗓️ Phase 6: Production Deploy (Недели 7-8)

### 6.1 Pre-Launch Checklist
```markdown
[ ] SSL сертификаты (Let's Encrypt)
[ ] DNS настройки (Yandex Cloud DNS)
[ ] DDoS защита (Yandex Cloud или DDoS-Guard)
[ ] Domain зарегистрирован
[ ] Privacy Policy / Terms of Service страницы
[ ] Cookie consent banner
[ ] Локализация (русский, английский - полная)
```

### 6.2 Go-Live
```markdown
[ ] Blue-green deployment strategy
[ ] Database migration с миграцией без downtime
[ ] Feature flags для постепенного rollout
[ ] Real-time monitoring в день релиза
[ ] On-call дежурство (Telegram alerts)
```

### 6.3 Post-Launch
```markdown
[ ] Health checks каждые 5 минут
[ ] Daily backup verification
[ ] Weekly security scans
[ ] Monthly performance review
[ ] Quarterly penetration testing
```

---

## 📚 Документация для Deploy

### Обязательная документация:
```markdown
[ ] RUNBOOK.md - операционные процедуры
[ ] INCIDENT_RESPONSE.md - план реагирования на инциденты
[ ] BACKUP_RESTORE.md - инструкции по восстановлению
[ ] SCALING_GUIDE.md - когда и как масштабировать
[ ] SECURITY.md - security policies и contact
[ ] API_DOCUMENTATION.md - для интеграций
```

---

## 🛠️ Немедленные Действия (This Week)

### Начать немедленно:

1. **Регистрация в Yandex Cloud**
   - Создать организацию
   - Настроить billing alerts
   - Создать первый VPC

2. **Git Repository Migration**
   - Зеркалировать на GitFlic или RuGit
   - Настроить webhook'и
   - Проверить CI/CD runner

3. **Domain & SSL**
   - Купить домен (рекомендую reg.ru или nic.ru)
   - Настроить Yandex Cloud DNS
   - Подготовить SSL (Let's Encrypt)

4. **E2E Tests Priority**
   - Довести Image-Word-Match тесты до 100%
   - Настроить CI интеграцию

---

## 📞 Contacts & Escalation

| Роль | Ответственность |
|------|-----------------|
| DevOps Engineer | CI/CD, Infrastructure, Monitoring |
| QA Engineer | Testing, Quality Gates |
| Backend Developer | API, Database, Performance |
| Mobile Developer | Android/Desktop releases |
| Product Owner | Prioritization, Acceptance |

---

## 🎯 Success Criteria

- [ ] Все E2E тесты проходят в CI
- [ ] Security scan без critical/high уязвимостей
- [ ] Performance benchmarks достигнуты
- [ ] 99.9% uptime на staging (2 недели)
- [ ] Zero critical bugs в beta
- [ ] Документация complete

---

*Документ будет обновляться по мере прогресса*
