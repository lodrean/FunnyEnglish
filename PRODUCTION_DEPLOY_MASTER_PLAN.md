# 🎯 FunnyEnglish - Production Deploy Master Plan

> **Цель:** Запустить FunnyEnglish в production на российской инфраструктуре за 6-8 недель  
> **Статус:** MVP ✅ | Pre-Production 🔄  
> **Дата обновления:** March 2026

---

## 📊 Текущее Состояние

```
┌─────────────────────────────────────────────────────────────┐
│  Backend           ████████████████████░░░░ 85%             │
│  - API: Complete                                            │
│  - Tests: 6/7 integration ✅                                │
│  - Security: Rate limiting ✅                               │
│  └─ Осталось: Load testing, optimizations                  │
├─────────────────────────────────────────────────────────────┤
│  Admin Web         ████████████████████░░░░ 80%             │
│  - UI: Material You ✅                                      │
│  - E2E: 15 tests ✅                                         │
│  └─ Осталось: IWM tests (в процессе), performance          │
├─────────────────────────────────────────────────────────────┤
│  Mobile (KMP)      ██████████████████░░░░░░ 75%             │
│  - Features: Complete                                       │
│  - Tests: Maestro flows 🔄                                  │
│  └─ Осталось: Device testing, store preparation            │
├─────────────────────────────────────────────────────────────┤
│  DevOps            ████████░░░░░░░░░░░░░░░░ 30%             │
│  - Docker: ✅                                               │
│  - CI/CD: 🔄 (GitFlic migration)                           │
│  - K8s: ⬜ (Yandex Cloud pending)                          │
│  └─ Осталось: Infrastructure, monitoring, SSL              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗓️ Timeline Overview

```
Week 1-2    │████████████████████████████████│  Testing & Quality Gates
Week 3-4    │████████████████████████████████│  DevOps & Infrastructure  
Week 5-6    │████████████████████████████████│  Staging & Beta
Week 7-8    │████████████████████████████████│  Production Deploy
```

---

## ✅ Week 1-2: FINAL TESTING SPRINT

### 🎯 Цель: 100% тестовое покрытие критического пути

| День | Задача | Владелец | Deliverable |
|------|--------|----------|-------------|
| **1** | Image Word Match E2E (5 тестов) | QA | playwright-report/ |
| **2** | Image Word Match E2E (5 тестов) | QA | All tests passing |
| **3** | Maestro Android flows (3 теста) | QA | .maestro/flows/ |
| **4** | Backend integration test #7 | Backend | 100% coverage |
| **5** | Security scan (OWASP ZAP) | DevOps | security-report.md |
| **6** | Performance baseline | Backend | k6 results |
| **7** | Sprint 1 Review | Team | Sign-off ✅ |
| **8** | Design audit completion | Designer | design-review.md |
| **9** | Accessibility testing | QA | a11y-report.md |
| **10** | Cross-browser testing | QA | browser-matrix.md |
| **11** | Device testing (Android) | QA | device-results.md |
| **12** | Bug fixes & regression | Team | 0 critical bugs |
| **13** | Load testing (1000 users) | DevOps | load-test-results.md |
| **14** | Sprint 2 Review | Team | Sign-off ✅ |

### 🚪 Exit Criteria Week 2:
- [ ] 100% Critical Path Tests passing
- [ ] 0 Critical / 0 High security issues
- [ ] Performance: p95 < 200ms (API)
- [ ] All browsers/devices smoke tested
- [ ] QA Sign-off ✅

---

## ⚙️ Week 3-4: DEVOPS FOUNDATION

### 🎯 Цель: Полностью работающая production инфраструктура

### Day 1-2: Yandex Cloud Setup
```bash
# Задачи:
□ Terraform init & plan
□ VPC + 3 subnets (ru-central1-a,b,c)
□ Managed Kubernetes (3 nodes)
□ Container Registry
□ Managed PostgreSQL
□ Object Storage bucket

# Deliverables:
✓ terraform/ директория в проекте
✓ Работающий kubectl
✓ YCR с тестовым образом
```

### Day 3-4: CI/CD Pipeline
```bash
# Задачи:
□ Миграция на GitFlic
□ .gitflic-ci.yml конфигурация
□ Self-hosted runner (Yandex VM)
□ Pipeline: lint → test → build → security → deploy
□ Telegram notifications

# Deliverables:
✓ Green build на develop
✓ Автоматический deploy в staging
```

### Day 5-6: Helm Charts & K8s
```bash
# Задачи:
□ Helm chart структура
□ Backend deployment + service
□ Admin deployment + service
□ Ingress с TLS (cert-manager)
□ ConfigMap + Secrets (Yandex Lockbox)
□ HPA (Horizontal Pod Autoscaler)
□ PDB (Pod Disruption Budget)

# Deliverables:
✓ helm/ директория
✓ Успешный деплой в staging
```

### Day 7-8: Monitoring & Logging
```bash
# Задачи:
□ Prometheus + Grafana
□ Loki + Promtail для логов
□ JVM метрики (Micrometer)
□ Business metrics dashboard
□ Alertmanager → Telegram
□ AppMetrica integration

# Deliverables:
✓ grafana.funnyenglish.ru доступен
✓ Алерты приходят в Telegram
✓ Логи в Grafana
```

### Day 9-10: Domain & SSL
```bash
# Задачи:
□ Купить funnyenglish.ru
□ Yandex Cloud DNS настройка
□ cert-manager + Let's Encrypt
□ HTTP→HTTPS redirect
□ www→non-www redirect
□ Security headers (CSP)

# Deliverables:
✓ https://api.funnyenglish.ru
✓ https://admin.funnyenglish.ru
✓ SSL Labs A+ rating
```

### Day 11-14: Staging Environment
```bash
# Задачи:
□ Production-like конфигурация
□ Smoke tests после деплоя
□ Data seed для тестов
□ Backup restoration test
□ Failover testing

# Deliverables:
✓ https://staging.funnyenglish.ru
✓ Автоматический деплой из develop
```

### 🚪 Exit Criteria Week 4:
- [ ] K8s кластер работает
- [ ] CI/CD pipeline green
- [ ] Мониторинг настроен
- [ ] HTTPS на всех endpoints
- [ ] Staging стабильно работает 48 часов

---

## 🧪 Week 5-6: STAGING & BETA

### 🎯 Цель: Проверка всего перед production

### Beta Testing Program
```markdown
□ Форма набора бета-тестеров (Google Forms → Яндекс Формы)
□ 50-100 тестеров
□ Тестовая группа в Telegram
□ Инструкция по установке (RuStore)
□ Форма обратной связи
□ Crash reporting (AppMetrica)

# Длительность: 1 неделя
# Success criteria:
- Crash-free sessions > 95%
- Average rating > 4.0
- < 10 critical bugs
```

### Load Testing Production
```markdown
□ 1000 concurrent users
□ 24-hour soak test
□ Database connection pool monitoring
□ Memory leak detection
□ CPU profiling
□ Network latency testing

# Tools:
- k6 для API load
- JMeter для комплексных сценариев
- Yandex.Tank (альтернатива)
```

### Security Audit
```markdown
□ Penetration testing (можно OWASP ZAP)
□ Dependency audit (trivy)
□ SAST (SonarQube или аналог)
□ Container scan
□ Secrets scan
□ DDoS test (если есть защита)
```

### Documentation
```markdown
□ RUNBOOK.md - операционные процедуры
□ INCIDENT_RESPONSE.md - план инцидентов
□ BACKUP_RESTORE.md - восстановление
□ SCALING_GUIDE.md - масштабирование
□ API_DOCUMENTATION.md - для интеграций
□ ONBOARDING.md - для новых разработчиков
```

### 🚪 Exit Criteria Week 6:
- [ ] Beta test complete, отзывы собраны
- [ ] Load test passed (1000 users)
- [ ] Security audit passed
- [ ] Документация complete
- [ ] 0 critical bugs

---

## 🚀 Week 7-8: PRODUCTION DEPLOY

### Pre-Launch (Week 7)
```markdown
□ Final security scan
□ Database migration test (production copy)
□ SSL certificate check
□ DNS propagation check
□ Backup verification
□ Monitoring dashboards review
□ On-call schedule
□ Communication plan (пользователям)
```

### Go-Live Strategy: Blue-Green Deployment
```bash
# Phase 1: Database migration (zero-downtime)
□ Выполнить migrations
□ Проверить data integrity

# Phase 2: Blue deployment (current version)
□ Deploy текущую стабильную версию
□ Smoke tests

# Phase 3: Green deployment (новая версия)
□ Deploy новую версию
□ Внутреннее тестирование
□ Переключение трафика (Ingress)

# Phase 4: Monitoring
□ 2 часа интенсивного мониторинга
□ Rollback plan ready
□ Telegram alerts enabled
```

### Launch Day Checklist
```markdown
□ [ ] 09:00 - Final smoke test
□ [ ] 09:30 - Database backup
□ [ ] 10:00 - Start deployment
□ [ ] 10:30 - Blue environment ready
□ [ ] 11:00 - Green deployment start
□ [ ] 11:30 - Internal testing
□ [ ] 12:00 - Switch traffic to Green
□ [ ] 12:00-14:00 - Intensive monitoring
□ [ ] 14:00 - Go/No-go decision
□ [ ] 18:00 - First day review
□ [ ] 24h - 24-hour report
```

### Post-Launch (Week 8)
```markdown
□ Daily standup на первой неделе
□ Monitoring dashboard review
□ User feedback analysis
□ Performance metrics review
□ First patch (если нужен)
□ Post-mortem meeting
```

### 🎉 Success Criteria (End of Week 8)
- [ ] Сервис доступен 24/7
- [ ] 99.9% uptime
- [ ] < 1% error rate
- [ ] Users активно используют
- [ ] 0 critical incidents

---

## 👥 Team Roles & Responsibilities

```
┌─────────────────┬────────────────────────────────────────┐
│ DevOps Engineer │ Infrastructure, CI/CD, Monitoring      │
│                 │ Yandex Cloud, Kubernetes, Terraform    │
├─────────────────┼────────────────────────────────────────┤
│ QA Engineer     │ Testing, Quality Gates                 │
│                 │ E2E, Performance, Security testing     │
├─────────────────┼────────────────────────────────────────┤
│ Backend Dev     │ API, Database, Performance             │
│                 │ Optimization, Load testing             │
├─────────────────┼────────────────────────────────────────┤
│ Frontend Dev    │ Admin Web, Mobile optimization         │
│                 │ React, KMP, Release builds             │
├─────────────────┼────────────────────────────────────────┤
│ Product Owner   │ Priorities, Acceptance, Beta           │
│                 │ User feedback, Go/No-go decisions      │
└─────────────────┴────────────────────────────────────────┘
```

---

## 💰 Budget Summary

### One-time Costs
```
Domain registration (.ru):            600 ₽
Code signing certificate:          10,000 ₽ (опционально)
Yandex Cloud setup (1st month):    15,000 ₽
------------------------------------------------
ИТОГО one-time:                    25,600 ₽
```

### Monthly Costs (Yandex Cloud)
```
Kubernetes (3 nodes):               8,000 ₽
Managed PostgreSQL:                 3,500 ₽
Object Storage:                       500 ₽
Container Registry:                   500 ₽
Load Balancer:                      1,500 ₽
CDN:                                  400 ₽
Public IP:                            300 ₽
Monitoring (self-hosted):               0 ₽
------------------------------------------------
ИТОГО monthly:                     14,700 ₽
```

**Total First Month:** ~40,000 ₽  
**Monthly Recurring:** ~15,000 ₽

---

## 📋 Quick Start (This Week)

### Immediate Actions (Priority 1)
```markdown
1. [ ] Регистрация Yandex Cloud
   - Ссылка: https://cloud.yandex.ru
   - Создать организацию
   - Пополнить баланс (1000₽ для начала)

2. [ ] Регистрация домена
   - Рекомендую: reg.ru или nic.ru
   - funnyenglish.ru или funnyenglish.app
   - Добавить в Yandex Cloud DNS

3. [ ] GitFlic регистрация
   - Ссылка: https://gitflic.ru
   - Создать проект
   - Mirror с GitHub

4. [ ] RuStore Developer
   - Ссылка: https://console.rustore.ru
   - Создать приложение
   - Подготовить материалы
```

### This Week Goals
- [ ] Yandex Cloud organization created
- [ ] Terraform initialized
- [ ] GitFlic repository ready
- [ ] Domain purchased
- [ ] IWM E2E tests complete

---

## 📚 Documentation Index

| Документ | Описание |
|----------|----------|
| `DEPLOYMENT_ROADMAP_RU.md` | Полный roadmap (8 недель) |
| `DEPLOYMENT_SPRINT_1-2.md` | Детальный план первых 2 недель |
| `TESTING_DEPLOYMENT_CHECKLIST.md` | Чек-лист тестирования |
| `RUSSIAN_ALTERNATIVES_GUIDE.md` | Миграция на российские сервисы |
| `PRODUCTION_DEPLOY_MASTER_PLAN.md` | Этот документ - мастер-план |

---

## 🎯 Next Steps (Right Now)

1. **Прочитайте** все 5 документов выше
2. **Создайте** Yandex Cloud аккаунт
3. **Зарегистрируйте** домен
4. **Создайте** GitFlic репозиторий
5. **Запустите** первый Terraform apply
6. **Начните** Image Word Match тесты

---

## 📞 Questions?

Если нужна помощь с конкретной задачей:
1. DevOps → Terraform, K8s, CI/CD
2. QA → Testing strategy, automation
3. Backend → Performance, security
4. Mobile → RuStore, release builds

---

**🚀 Готовы к production deploy через 8 недель!**
