# 🇷🇺 So to Speak - Руководство по Российским Альтернативам

## ⚠️ Текущие Ограничения и Риски

| Сервис | Статус | Риск | Действие |
|--------|--------|------|----------|
| GitHub | Ограничен доступ | Высокий | Миграция на GitFlic/RuGit |
| Docker Hub | Блокировка частичная | Средний | Yandex Container Registry |
| Firebase | Работает, но риск | Средний | AppMetrica + RuStore |
| Sentry | Блокировка возможна | Средний | Self-hosted |
| Stripe | Полная блокировка | Нет | ЮKassa / Robokassa |
| SendGrid | Блокировка | Нет | Mail.ru / SMTP |

---

## 🔄 Миграция Step-by-Step

### 1. Git Repository Migration

#### Опция A: GitFlic (Ростелеком)
**Плюсы:**
- Бесплатный для open source
- CI/CD встроенный (GitFlic CI)
- Русскоязычная поддержка
- Нет ограничений по санкциям

**Минусы:**
- Меньше экосистема чем GitLab
- Новый сервис (меньше документации)

**Миграция:**
```bash
# Создать репозиторий на GitFlic
# Настройки → Repository → Import

# Или вручную:
git clone --mirror https://github.com/your/sotospeak.git
cd sotospeak.git
git remote set-url origin https://gitflic.ru/project/your/sotospeak.git
git push --mirror
```

#### Опция B: RuGit (Mail.ru)
**Плюсы:**
- Поддержка Mail.ru
- Интеграция с облаком Mail.ru

**Минусы:**
- Меньше функционала CI/CD

#### Опция C: Self-hosted GitLab
**Плюсы:**
- Полный контроль
- Мощный CI/CD
- Единовременные затраты

**Минусы:**
- Нужно администрировать
- ~5000₽/мес за сервер

**Рекомендация:** GitFlic для начала, self-hosted GitLab при масштабировании

---

### 2. CI/CD Migration

#### GitFlic CI Configuration
```yaml
# .gitflic-ci.yml (аналог .gitlab-ci.yml)
stages:
  - build
  - test
  - deploy

variables:
  GRADLE_USER_HOME: "$CI_PROJECT_DIR/.gradle"

cache:
  paths:
    - .gradle/
    - admin-web/node_modules/

before_script:
  - export GRADLE_OPTS="-Dorg.gradle.daemon=false"

backend:build:
  stage: build
  image: gradle:8.5-jdk21-alpine
  script:
    - cd backend
    - ./gradlew clean bootJar
  artifacts:
    paths:
      - backend/build/libs/*.jar
    expire_in: 1 week

backend:test:
  stage: test
  image: gradle:8.5-jdk21-alpine
  services:
    - name: postgres:16-alpine
      alias: postgres
  variables:
    POSTGRES_DB: test
    POSTGRES_USER: test
    POSTGRES_PASSWORD: test
  script:
    - cd backend
    - ./gradlew test
  coverage: '/Total: ([0-9.]+)%/'
```

#### Self-hosted Runner (Yandex Cloud)
```bash
# Создать VM для runner
yc compute instance create \
  --name gitlab-runner \
  --zone ru-central1-a \
  --cores 4 \
  --memory 8G \
  --image-family ubuntu-2204-lts \
  --ssh-key ~/.ssh/id_rsa.pub

# Установить runner
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh | sudo bash
sudo apt install gitlab-runner

# Регистрация
sudo gitlab-runner register \
  --url https://gitflic.ru/ \
  --registration-token YOUR_TOKEN \
  --executor docker \
  --docker-image docker:24-dind \
  --docker-privileged
```

---

### 3. Container Registry Migration

#### Yandex Container Registry
```bash
# Создать registry
yc container registry create --name sotospeak

# Аутентификация
yc container registry configure-docker

# Build и push
docker build -t cr.yandex/<registry-id>/backend:latest -f docker/Dockerfile.backend .
docker push cr.yandex/<registry-id>/backend:latest
```

#### Harbor (Self-hosted альтернатива)
```bash
# Установка через Helm
helm repo add harbor https://helm.goharbor.io
helm install harbor harbor/harbor \
  --set expose.type=ingress \
  --set expose.ingress.hosts.core=registry.sotospeak.ru
```

---

### 4. Cloud Infrastructure

#### Yandex Cloud (Рекомендуется)

**Создание инфраструктуры:**
```hcl
# terraform/main.tf
terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
  }
}

provider "yandex" {
  token     = var.yc_token
  cloud_id  = var.yc_cloud_id
  folder_id = var.yc_folder_id
}

# Сеть
resource "yandex_vpc_network" "main" {
  name = "sotospeak"
}

resource "yandex_vpc_subnet" "subnet_a" {
  name           = "subnet-a"
  zone           = "ru-central1-a"
  network_id     = yandex_vpc_network.main.id
  v4_cidr_blocks = ["10.0.1.0/24"]
}

# Managed Kubernetes
resource "yandex_kubernetes_cluster" "main" {
  name       = "sotospeak"
  network_id = yandex_vpc_network.main.id
  
  master {
    zonal {
      zone      = "ru-central1-a"
      subnet_id = yandex_vpc_subnet.subnet_a.id
    }
    public_ip = true
  }
  
  service_account_id      = yandex_iam_service_account.k8s.id
  node_service_account_id = yandex_iam_service_account.k8s.id
}

# Node Group
resource "yandex_kubernetes_node_group" "main" {
  cluster_id = yandex_kubernetes_cluster.main.id
  name       = "main"
  
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
    auto_scale {
      min     = 2
      max     = 5
      initial = 2
    }
  }
}

# Managed PostgreSQL
resource "yandex_mdb_postgresql_cluster" "main" {
  name        = "sotospeak"
  environment = "PRODUCTION"
  network_id  = yandex_vpc_network.main.id
  
  config {
    version = "16"
    resources {
      resource_preset_id = "s2.micro"
      disk_type_id       = "network-ssd"
      disk_size          = 50
    }
  }
  
  host {
    zone             = "ru-central1-a"
    subnet_id        = yandex_vpc_subnet.subnet_a.id
    assign_public_ip = false
  }
}

# Object Storage
resource "yandex_storage_bucket" "media" {
  bucket     = "sotospeak-media"
  access_key = yandex_iam_service_account_storage.access_key
  secret_key = yandex_iam_service_account_storage.secret_key
}
```

**Deploy:**
```bash
cd terraform
terraform init
terraform plan
terraform apply

# Получить kubeconfig
yc managed-kubernetes cluster get-credentials sotospeak --external
```

#### Selectel (Альтернатива)
```bash
# Selectel - российский хостинг с Kubernetes
# Цены схожи с Yandex Cloud
# Плюс: меньше нагрузка на поддержку (меньше клиентов)
# Минус: меньше сервисов чем у Yandex
```

---

### 5. Analytics & Monitoring Migration

#### AppMetrica (вместо Firebase Analytics)

**Android Integration:**
```kotlin
// build.gradle.kts
dependencies {
    implementation("com.yandex.android:mobmetricalib:5.3.0")
}

// Application.onCreate()
YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(API_KEY)
    .withLocationTracking(true)
    .build()
YandexMetrica.activate(applicationContext, config)

// Track events
YandexMetrica.reportEvent("test_started", mapOf(
    "test_id" to testId,
    "category" to category
))

YandexMetrica.reportEvent("test_completed", mapOf(
    "test_id" to testId,
    "score" to score,
    "stars" to stars
))
```

**Web Integration:**
```typescript
// AppMetrica для web
import { counter } from '@yandex/metrica-tag';

counter.init({
  counterId: 123456789,
  clickmap: true,
  trackLinks: true,
  accurateTrackBounce: true,
});

// Custom events
counter.reachGoal('test_created', {
  question_type: 'image_word_match',
});
```

#### Self-hosted Sentry
```bash
# Установка Sentry через Docker Compose
curl -sL https://sentry.io/get-cli/ | bash

# Или self-hosted
git clone https://github.com/getsentry/self-hosted.git
cd self-hosted
./install.sh
docker-compose up -d

# Интеграция с backend
sentry:
  dsn: https://xxx@o0.ingest.sentry.io/0
  environment: production
```

---

### 6. Push Notifications

#### Firebase Cloud Messaging → RuStore Push (или Yandex Push)

**RuStore Push:**
```kotlin
// build.gradle.kts
implementation("ru.rustore.push:pushclient:1.0.0")

// Инициализация
RuStorePushClient.init(applicationContext, PROJECT_ID)

// Получение токена
RuStorePushClient.getToken()
    .addOnSuccessListener { token ->
        // Отправить на сервер
        apiService.registerPushToken(token)
    }
```

**Fallback на Firebase:**
```kotlin
// Пробуем RuStore, если нет - Firebase
try {
    RuStorePushClient.init(...)
} catch (e: Exception) {
    FirebaseMessaging.getInstance().token
}
```

---

### 7. App Distribution

#### RuStore (Основной канал для России)

**Подготовка:**
1. Регистрация на https://console.rustore.ru
2. Создание приложения
3. Загрузка APK/AAB
4. Прохождение модерации

**Gradle Configuration:**
```kotlin
// build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("sotospeak.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "sotospeak"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### Google Play (Оставить для зарубежных пользователей)
```markdown
- Собирать dual APK (RuStore + Play)
- Для RuStore: отключить Google Billing
- Для Play: оставить как есть
```

---

### 8. Payments (если будет монетизация)

#### ЮKassa (ЮMoney)
```kotlin
// Backend integration
@RestController
class PaymentController {
    
    @PostMapping("/api/payments/create")
    fun createPayment(@RequestBody request: PaymentRequest): ResponseEntity<PaymentResponse> {
        val payment = yookassaClient.createPayment(
            amount = request.amount,
            currency = "RUB",
            description = "So to Speak Premium",
            returnUrl = "https://sotospeak.ru/payment/success",
            capture = true
        )
        return ResponseEntity.ok(PaymentResponse(payment.confirmation.confirmationUrl))
    }
}
```

#### Robokassa
```kotlin
// Альтернатива с большим выбором способов оплаты
val signature = MD5("${merchantLogin}:${amount}:${invId}:${password1}")
val paymentUrl = "https://auth.robokassa.ru/Merchant/Index.aspx" +
    "?MerchantLogin=$merchantLogin" +
    "&OutSum=$amount" +
    "&InvId=$invId" +
    "&SignatureValue=$signature"
```

---

### 9. Email Service

#### Mail.ru Cloud Solutions (для массовых рассылок)
```yaml
# application.yml
spring:
  mail:
    host: smtp.mail.ru
    port: 465
    username: noreply@sotospeak.ru
    password: ${MAIL_PASSWORD}
    protocol: smtps
    properties:
      mail.smtp.auth: true
      mail.smtp.ssl.enable: true
```

#### Unisender (API для рассылок)
```kotlin
val unisender = UnisenderClient(apiKey = "...")
unisender.sendEmail(
    to = user.email,
    subject = "Добро пожаловать в So to Speak!",
    body = templateEngine.render("welcome", context),
    from = "noreply@sotospeak.ru"
)
```

---

### 10. CDN и DDoS Защита

#### Yandex Cloud CDN
```hcl
resource "yandex_cdn_origin_group" "main" {
  name = "sotospeak"
  origin {
    source = "storage.yandexcloud.net"
  }
}

resource "yandex_cdn_resource" "main" {
  cname = "cdn.sotospeak.ru"
  origin_group_id = yandex_cdn_origin_group.main.id
  
  options {
    edge_cache_settings = "86400"
    browser_cache_settings = "3600"
    gzip_on = true
  }
}
```

#### DDoS-Guard (если нужна защита)
```markdown
- Пропускная способность: от 1000₽/мес
- Защита L3/L4/L7
- Российская компания
```

---

## 💰 Примерный Бюджет (месяц)

### Yandex Cloud (Production)
```
Kubernetes (3 nodes, 4 vCPU, 8GB):    8,000 ₽
Managed PostgreSQL (2 vCPU, 8GB):     3,500 ₽
Object Storage (100GB + трафик):        500 ₽
Container Registry:                     500 ₽
Load Balancer:                        1,500 ₽
CDN (100GB):                            400 ₽
Public IP:                              300 ₽
------------------------------------------------
ИТОГО:                               14,700 ₽
```

### Дополнительно
```
Domain (.ru):                          600 ₽/год
SSL Certificate (Let's Encrypt):         0 ₽
Monitoring (Grafana Cloud free):         0 ₽
AppMetrica:                              0 ₽ (до 1M событий)
RuStore:                                 0 ₽ (15% комиссия)
------------------------------------------------
ИТОГО:                               ~15,000 ₽/мес
```

---

## 📋 Migration Checklist

### Week 1: Infrastructure
- [ ] Создать Yandex Cloud организацию
- [ ] Настроить Terraform
- [ ] Развернуть Managed Kubernetes
- [ ] Создать Container Registry
- [ ] Настроить Managed PostgreSQL

### Week 2: CI/CD
- [ ] Мигрировать репозиторий на GitFlic
- [ ] Настроить GitFlic CI
- [ ] Настроить self-hosted runner
- [ ] Проверить pipeline (build, test, deploy)

### Week 3: Services Integration
- [ ] Интегрировать AppMetrica
- [ ] Настроить RuStore Push
- [ ] Мигрировать S3 на Yandex Object Storage
- [ ] Настроить Yandex Cloud CDN

### Week 4: App Distribution
- [ ] Создать RuStore аккаунт
- [ ] Подготовить релизную сборку
- [ ] Пройти модерацию RuStore
- [ ] Настроить автоматическую публикацию

---

## 🆘 Emergency Contacts

| Сервис | Поддержка | Статус |
|--------|-----------|--------|
| Yandex Cloud | cloud-support@yandex.ru | ✅ |
| GitFlic | support@gitflic.ru | ✅ |
| RuStore | support@rustore.ru | ✅ |
| Selectel | support@selectel.ru | ✅ |

---

## 📚 Полезные Ссылки

- [Yandex Cloud Documentation](https://cloud.yandex.ru/docs)
- [GitFlic Documentation](https://docs.gitflic.ru)
- [RuStore Developer Console](https://console.rustore.ru)
- [AppMetrica Documentation](https://appmetrica.yandex.ru/docs/)
- [Terraform Yandex Provider](https://registry.terraform.io/providers/yandex-cloud/yandex/latest/docs)
