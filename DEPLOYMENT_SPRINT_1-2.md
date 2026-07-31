# 🏃‍♂️ Sprint 1-2: Pre-Production & DevOps Foundation

## 📅 Спринт 1 (Неделя 1): Тестирование и Quality Gates

### День 1-2: E2E Тестирование - Image Word Match
**Владелец:** QA Engineer  
**Статус:** 🔄 В процессе

```markdown
### Задачи:

#### TC-IWM-001: Создание теста с изображением
- [ ] Загрузка изображения
- [ ] Добавление слов и переводов
- [ ] Создание hotspot'ов
- [ ] Сохранение вопроса
- [ ] Проверка в превью
**ETA:** 4 часа

#### TC-IWM-002: Валидация обязательных полей
- [ ] Попытка сохранения без изображения
- [ ] Попытка сохранения без слов
- [ ] Попытка сохранения без hotspot'ов
- [ ] Проверка сообщений об ошибках
**ETA:** 2 часа

#### TC-IWM-003: Редактирование существующего вопроса
- [ ] Открытие существующего вопроса
- [ ] Изменение слов
- [ ] Перемещение hotspot'ов
- [ ] Проверка сохранения изменений
**ETA:** 3 часа

#### TC-IWM-004: Drag-and-drop в mobile view
- [ ] Эмуляция touch events
- [ ] Перетаскивание hotspot'ов
- [ ] Проверка координат
**ETA:** 4 часа

#### TC-IWM-005: Удаление hotspot'а
- [ ] Удаление через double-click
- [ ] Подтверждение удаления
- [ ] Проверка обновления UI
**ETA:** 2 часа

### Deliverables:
- [ ] 10 E2E тестов в admin-web/e2e/tests/image-word-match/
- [ ] Allure/HTML отчет
- [ ] Интеграция в CI
```

### День 3-4: Мобильное Тестирование (Maestro)
**Владелец:** QA Engineer

```markdown
### Android Flow Tests:

#### AUTH-FLOW-001: Регистрация и вход
```yaml
appId: com.funnyenglish.app
---
- launchApp
- tapOn: "Get Started"
- tapOn: "Email"
- inputText: "test@example.com"
- tapOn: "Password"
- inputText: "password123"
- tapOn: "Sign Up"
- assertVisible: "Welcome"
```
**ETA:** 3 часа

#### TEST-FLOW-001: Прохождение теста Image Word Match
- [ ] Открытие категории
- [ ] Выбор теста
- [ ] Drag-and-drop слов
- [ ] Проверка результатов
- [ ] Проверка начисления очков
**ETA:** 4 часа

#### LEADERBOARD-FLOW-001: Проверка таблицы лидеров
- [ ] Открытие leaderboard
- [ ] Проверка обновления после прохождения теста
- [ ] Проверка своей позиции
**ETA:** 2 часа

### Deliverables:
- [ ] 5 Maestro flow tests
- [ ] Запуск на Firebase Test Lab (или эмуляторе)
- [ ] Отчет о стабильности
```

### День 5: Backend Integration & Security Tests
**Владелец:** Backend Developer

```markdown
### Integration Tests:
- [ ] Добавить недостающий тест (6/7 → 7/7)
- [ ] TestContainers для PostgreSQL
- [ ] S3 integration tests (LocalStack или Yandex S3 mock)

### Security Tests:
- [ ] OWASP ZAP сканирование API
- [ ] JWT token expiration tests
- [ ] Rate limiting tests (100 req/min)
- [ ] SQL injection attempts (sqlmap)

### Deliverables:
- [ ] 100% integration test coverage
- [ ] Security scan report
```

---

## 📅 Спринт 2 (Неделя 2): DevOps Foundation

### День 1: Yandex Cloud Setup
**Владелец:** DevOps Engineer

```markdown
### Infrastructure as Code (Terraform):

```hcl
# terraform/main.tf
provider "yandex" {
  token     = var.yc_token
  cloud_id  = var.yc_cloud_id
  folder_id = var.yc_folder_id
  zone      = "ru-central1-a"
}

# VPC Network
resource "yandex_vpc_network" "funnyenglish" {
  name = "funnyenglish-network"
}

# Subnets
resource "yandex_vpc_subnet" "subnet-a" {
  name           = "subnet-a"
  zone           = "ru-central1-a"
  network_id     = yandex_vpc_network.funnyenglish.id
  v4_cidr_blocks = ["10.0.1.0/24"]
}

# Managed Kubernetes
resource "yandex_kubernetes_cluster" "funnyenglish" {
  name       = "funnyenglish-k8s"
  network_id = yandex_vpc_network.funnyenglish.id
  
  master {
    zonal {
      zone      = "ru-central1-a"
      subnet_id = yandex_vpc_subnet.subnet-a.id
    }
    
    public_ip = true
    version   = "1.28"
  }
  
  service_account_id      = yandex_iam_service_account.k8s.id
  node_service_account_id = yandex_iam_service_account.k8s.id
}

# Node Group
resource "yandex_kubernetes_node_group" "default" {
  cluster_id = yandex_kubernetes_cluster.funnyenglish.id
  name       = "default-node-group"
  
  instance_template {
    platform_id = "standard-v3"
    resources {
      cores  = 4
      memory = 8
    }
    
    boot_disk {
      type = "network-ssd"
      size = 50
    }
  }
  
  scale_policy {
    fixed_scale {
      size = 3
    }
  }
}
```

### Tasks:
- [ ] Создать Yandex Cloud организацию
- [ ] Настроить Terraform backend (S3 для state)
- [ ] Создать VPC и подсети (3 AZ)
- [ ] Развернуть Managed Kubernetes
- [ ] Настроить kubectl access
- [ ] Настроить Yandex Container Registry

### Deliverables:
- [ ] Работающий K8s кластер
- [ ] Terraform конфигурация в infra/
- [ ] kubeconfig для команды
```

### День 2: CI/CD Pipeline (GitFlic)
**Владелец:** DevOps Engineer

```markdown
### GitFlic CI Configuration:

```yaml
# .gitflic-ci.yml
stages:
  - lint
  - test
  - build
  - security
  - deploy

variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"
  DOCKER_REGISTRY: "cr.yandex/<registry-id>"

# Backend Lint
backend:lint:
  stage: lint
  image: gradle:8.5-jdk21
  script:
    - cd backend
    - ./gradlew ktlintCheck detekt
  only:
    - merge_requests
    - main
    - develop

# Backend Unit Tests
backend:test:
  stage: test
  image: gradle:8.5-jdk21
  services:
    - postgres:16-alpine
  variables:
    POSTGRES_DB: test
    POSTGRES_USER: test
    POSTGRES_PASSWORD: test
  script:
    - cd backend
    - ./gradlew test
  artifacts:
    reports:
      junit: backend/build/test-results/test/*.xml
    paths:
      - backend/build/reports/tests/

# Backend Build & Push
backend:build:
  stage: build
  image: docker:24-dind
  services:
    - docker:24-dind
  script:
    - docker build -t $DOCKER_REGISTRY/backend:$CI_COMMIT_SHA -f docker/Dockerfile.backend .
    - docker push $DOCKER_REGISTRY/backend:$CI_COMMIT_SHA
  only:
    - main
    - develop

# Security Scan
security:trivy:
  stage: security
  image: aquasec/trivy:latest
  script:
    - trivy image --exit-code 1 --severity HIGH,CRITICAL $DOCKER_REGISTRY/backend:$CI_COMMIT_SHA
  allow_failure: true

# Admin Web Lint
admin:lint:
  stage: lint
  image: node:20-alpine
  script:
    - cd admin-web
    - npm ci
    - npm run lint
    - npm run typecheck

# Admin Web E2E Tests
admin:e2e:
  stage: test
  image: mcr.microsoft.com/playwright:v1.40.0-jammy
  services:
    - name: postgres:16-alpine
      alias: postgres
    - name: minio/minio
      alias: minio
      command: ["server", "/data"]
  script:
    - cd admin-web
    - npm ci
    - npm run test:e2e:ci
  artifacts:
    when: always
    paths:
      - admin-web/playwright-report/
    expire_in: 1 week

# Deploy to Staging
deploy:staging:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - kubectl config use-context staging
    - helm upgrade --install funnyenglish ./helm/funnyenglish 
        --namespace staging
        --set backend.image.tag=$CI_COMMIT_SHA
        --set admin.image.tag=$CI_COMMIT_SHA
  environment:
    name: staging
    url: https://staging.funnyenglish.ru
  only:
    - develop
```

### Tasks:
- [ ] Настроить GitFlic репозиторий
- [ ] Создать .gitflic-ci.yml
- [ ] Настроить self-hosted runner (Yandex VM)
- [ ] Настроить Docker Registry access
- [ ] Проверить pipeline на тестовом MR

### Deliverables:
- [ ] Работающий CI/CD pipeline
- [ ] Green build на develop
```

### День 3: Helm Charts
**Владелец:** DevOps Engineer

```markdown
### Helm Chart Structure:

```yaml
# helm/funnyenglish/values.yaml
backend:
  replicaCount: 2
  image:
    repository: cr.yandex/<registry>/backend
    tag: latest
    pullPolicy: IfNotPresent
  resources:
    limits:
      cpu: 1000m
      memory: 2Gi
    requests:
      cpu: 500m
      memory: 1Gi
  env:
    SPRING_PROFILES_ACTIVE: production
    DB_HOST: "postgresql.database.svc.cluster.local"
    S3_ENDPOINT: "https://storage.yandexcloud.net"
  ingress:
    enabled: true
    host: api.funnyenglish.ru
    annotations:
      nginx.ingress.kubernetes.io/rate-limit: "100"

admin:
  replicaCount: 2
  image:
    repository: cr.yandex/<registry>/admin
    tag: latest
  ingress:
    host: admin.funnyenglish.ru

postgresql:
  enabled: false  # Using managed PostgreSQL

redis:
  enabled: true
  architecture: standalone
  auth:
    enabled: true
    password: "${REDIS_PASSWORD}"
```

### Tasks:
- [ ] Создать Helm chart structure
- [ ] Backend deployment template
- [ ] Admin deployment template
- [ ] Ingress с TLS
- [ ] ConfigMap для конфигурации
- [ ] External Secrets (Yandex Lockbox integration)
- [ ] Service definitions
- [ ] PDB (Pod Disruption Budget)
- [ ] HPA (Horizontal Pod Autoscaler)

### Deliverables:
- [ ] helm/ директория в проекте
- [ ] Успешный деплой в staging
```

### День 4: Monitoring & Logging
**Владелец:** DevOps Engineer

```markdown
### Prometheus + Grafana Stack:

```yaml
# helm/monitoring/values.yaml
prometheus:
  prometheusSpec:
    retention: 30d
    retentionSize: 50GB
    resources:
      limits:
        memory: 4Gi
    storageSpec:
      volumeClaimTemplate:
        spec:
          storageClassName: yc-network-ssd
          resources:
            requests:
              storage: 100Gi
    additionalScrapeConfigs:
      - job_name: 'funnyenglish-backend'
        static_configs:
          - targets: ['funnyenglish-backend.monitoring.svc.cluster.local:8080']
        metrics_path: /actuator/prometheus

grafana:
  enabled: true
  adminPassword: "${GRAFANA_ADMIN_PASSWORD}"
  ingress:
    enabled: true
    hosts:
      - grafana.funnyenglish.ru
  dashboards:
    default:
      jvm-dashboard:
        url: https://raw.githubusercontent.com/jvm-monitoring/grafana-dashboards/main/jvm-micrometer.json
      spring-boot-dashboard:
        url: https://raw.githubusercontent.com/spring-boot-dashboards/.../dashboard.json

alertmanager:
  config:
    route:
      receiver: 'telegram'
    receivers:
      - name: 'telegram'
        telegram_configs:
          - bot_token: "${TELEGRAM_BOT_TOKEN}"
            chat_id: "${TELEGRAM_CHAT_ID}"
            message: |
              {{ range .Alerts }}
              🚨 <b>{{ .Status | toUpper }}</b>
              <b>{{ .Annotations.summary }}</b>
              {{ .Annotations.description }}
              {{ end }}
```

### Loki for Logs:
```yaml
# helm/logging/values.yaml
loki:
  enabled: true
  persistence:
    enabled: true
    size: 100Gi
    storageClassName: yc-network-ssd

promtail:
  enabled: true
  config:
    snippets:
      scrapeConfigs: |
        - job_name: kubernetes-pods
          kubernetes_sd_configs:
            - role: pod
          relabel_configs:
            - source_labels: [__meta_kubernetes_pod_annotation_kubectl_kubernetes_io_default_container]
              action: replace
              target_label: app
```

### Tasks:
- [ ] Развернуть kube-prometheus-stack
- [ ] Настроить Grafana dashboards
- [ ] Настроить Alertmanager → Telegram
- [ ] Развернуть Loki + Promtail
- [ ] Настроить бизнес-метрики в Grafana

### Deliverables:
- [ ] Доступный Grafana (grafana.funnyenglish.ru)
- [ ] Работающие алерты в Telegram
- [ ] Логи доступны в Grafana
```

### День 5: Domain, DNS & SSL
**Владелец:** DevOps Engineer

```markdown
### Tasks:
- [ ] Купить домен funnyenglish.ru (или другой)
- [ ] Настроить Yandex Cloud DNS:
  ```
  api.funnyenglish.ru → Ingress IP (A record)
  admin.funnyenglish.ru → Ingress IP (A record)
  grafana.funnyenglish.ru → Ingress IP (A record)
  ```
- [ ] Настроить cert-manager в K8s
- [ ] Настроить ClusterIssuer для Let's Encrypt
- [ ] Проверить автоматическое обновление сертификатов
- [ ] Настроить redirects (www → non-www, http → https)

### SSL Configuration:
```yaml
# cert-manager ClusterIssuer
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@funnyenglish.ru
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
      - http01:
          ingress:
            class: nginx
```

### Deliverables:
- [ ] Работающий HTTPS на всех endpoints
- [ ] Автообновление сертификатов
```

---

## 🎯 Sprint Goals

### Sprint 1 (Week 1):
- ✅ 10 E2E тестов для Image Word Match
- ✅ 5 Maestro тестов для Android
- ✅ 100% backend integration coverage
- ✅ Security scan без critical уязвимостей

### Sprint 2 (Week 2):
- ✅ Работающий K8s кластер в Yandex Cloud
- ✅ CI/CD pipeline в GitFlic
- ✅ Автоматический деплой в staging
- ✅ Monitoring (Prometheus + Grafana)
- ✅ HTTPS на всех endpoints

---

## 📊 Definition of Done

### Для каждой задачи:
- [ ] Код ревью пройден
- [ ] Тесты проходят (CI green)
- [ ] Документация обновлена
- [ ] Проверено на staging
- [ ] Нет critical/high security issues

### Ежедневный standup:
- Что сделано вчера?
- Что планируется сегодня?
- Есть ли блокеры?

### Ретроспектива в конце спринта:
- Что пошло хорошо?
- Что можно улучшить?
- Action items для следующего спринта
