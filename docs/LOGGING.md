# Логирование в So to Speak

## Desktop Application

### Где смотреть логи

Логи Desktop приложения доступны в нескольких местах:

#### 1. Консоль (Terminal/IDEA Console)
При запуске через `./gradlew :composeApp:run` логи выводятся прямо в консоль.

#### 2. Файл логов
Логи автоматически сохраняются в файл:
```
Windows: %USERPROFILE%\.sotospeak\logs\app-YYYY-MM-DD.log
Linux/Mac: ~/.sotospeak/logs/app-YYYY-MM-DD.log
```

Пример пути на Windows:
```
C:\Users\YourName\.sotospeak\logs\app-2026-02-03.log
```

### Просмотр логов

#### Через скрипт (Windows)
```bash
# Показать последний лог файл
.\view-logs.bat

# Запустить с отображением логов в консоли
.\start-desktop-with-logs.bat
```

#### Вручную
```bash
# Windows PowerShell
Get-Content $env:USERPROFILE\.sotospeak\logs\app-2026-02-03.log -Tail 50

# Windows CMD
type %USERPROFILE%\.sotospeak\logs\app-2026-02-03.log

# Linux/Mac
tail -f ~/.sotospeak/logs/app-2026-02-03.log
```

### Уровни логирования

- **DEBUG** - детальная информация (HTTP запросы/ответы)
- **INFO** - общая информация (запуск, подключение)
- **WARN** - предупреждения
- **ERROR** - ошибки

### Что логируется

#### HTTP запросы (включено по умолчанию)
```
[D] HttpClient: REQUEST: http://localhost:8080/categories
[D] HttpClient: METHOD: GET
[D] HttpClient: RESPONSE: 200 
[D] HttpClient: BODY: [{"id":"...",...}]
```

#### Загрузка изображений
```
[D] HttpClient: REQUEST: http://localhost:9000/sotospeak/thumbnails/test_colors.png
[D] HttpClient: RESPONSE: 200
```

#### Ошибки
```
[E] HttpClient: HTTP call failed
java.net.ConnectException: Connection refused
```

### Включение/выключение логов

#### Через переменную окружения
```bash
# Windows PowerShell
$env:SOTOSPEAK_HTTP_LOGS="true"
.\gradlew :composeApp:run

# Windows CMD
set SOTOSPEAK_HTTP_LOGS=true
gradlew :composeApp:run
```

#### Через системное свойство
```bash
.\gradlew :composeApp:run -Dsotospeak.debug=true
```

#### По умолчанию
В Desktop версии логи **включены по умолчанию**.

---

## Backend API

### Где смотреть логи

#### Консоль запуска
При запуске `./gradlew :backend:bootRun` логи выводятся в консоль.

#### Файл логов
```
backend/build/logs/
```

### Уровни логирования

Настраиваются в `backend/src/main/resources/application.yml`:

```yaml
logging:
  level:
    com.sotospeak: DEBUG      # Бизнес-логика
    org.springframework.web: DEBUG  # HTTP запросы
    org.apache.coyote.http11: DEBUG # Tomcat
```

### HTTP логи

Пример вывода:
```
2026-02-03 10:50:27 [http-nio-8080-exec-1] DEBUG o.s.w.s.m.m.a.RequestMappingHandlerMapping 
  - Mapped to com.sotospeak.controller.CategoryController#getAllCategories

2026-02-03 10:50:27 [http-nio-8080-exec-1] DEBUG o.s.w.c.s.DefaultCorsProcessor 
  - Cross-origin request allowed

2026-02-03 10:50:27 [http-nio-8080-exec-1] DEBUG c.f.c.CategoryController 
  - Fetching categories for user: null
```

### SQL логи

Для включения SQL запросов:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

---

## Admin Web

### Браузерная консоль

1. Откройте Admin Panel (http://localhost:3002)
2. Нажмите F12 (DevTools)
3. Перейдите во вкладку Console

### Сетевые запросы

В DevTools → Network можно видеть все API запросы:
- URL
- Method (GET/POST/PUT/DELETE)
- Status Code
- Request/Response Body
- Headers

---

## Отладка проблем

### "Не загружаются картинки"

1. Проверьте URL в логах:
   ```
   [D] HttpClient: REQUEST: http://localhost:9000/...
   ```

2. Проверьте ответ:
   - `200` - OK
   - `404` - файл не найден
   - `403` - нет доступа
   - `Connection refused` - MinIO не запущен

3. Откройте URL в браузере:
   ```
   http://localhost:9000/sotospeak/thumbnails/test_colors.png
   ```

### "Network Error" в Admin Panel

1. Откройте DevTools (F12)
2. В Console найдите ошибку CORS:
   ```
   Access to fetch at 'http://localhost:8080/...' from origin 
   'http://localhost:3002' has been blocked by CORS policy
   ```

3. Проверьте backend логи - должен быть запрос с указанным Origin

### "Долгие запросы"

1. Включите timestamp в логах
2. Найдите REQUEST и соответствующий RESPONSE
3. Разница во времени = время выполнения

Пример:
```
[10:50:27] REQUEST: GET /categories
[10:50:30] RESPONSE: 200  <- 3 секунды!
```
