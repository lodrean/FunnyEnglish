# Research: BUG-004 - MinIO URL использует внутренний hostname

## Ticket
BUG-004

## Objective
Исследовать и подтвердить что медиа-URL используют публичный адрес вместо внутреннего Docker hostname.

## Current Code Analysis

### application.yml (строки 60-66)
```yaml
app:
  s3:
    endpoint: ${S3_ENDPOINT:http://localhost:9000}
    public-url: ${S3_PUBLIC_URL:${S3_ENDPOINT:http://localhost:9000}}
    access-key: ${S3_ACCESS_KEY:minioadmin}
    secret-key: ${S3_SECRET_KEY:minioadmin}
    bucket: ${S3_BUCKET:funnyenglish}
    region: ${S3_REGION:us-east-1}
```
**Статус**: ✅ Уже добавлена настройка `public-url`

### StorageService.kt (строки 16-22, 76-96)
```kotlin
@Service
class StorageService(
    private val s3Client: S3Client,
    @Value("\${app.s3.bucket}") private val bucket: String,
    @Value("\${app.s3.endpoint}") private val endpoint: String,
    @Value("\${app.s3.public-url}") private val publicUrl: String
) {
    private fun buildObjectUrl(key: String): String {
        val baseUrl = publicUrl.trimEnd('/')
        // ... логика построения URL с использованием publicUrl
    }
}
```
**Статус**: ✅ Уже используется `publicUrl` для построения URL

## Affected Areas
- `backend/src/main/resources/application.yml` - ✅ Исправлен
- `backend/.../service/StorageService.kt` - ✅ Исправлен

## Verification Steps
1. Запустить backend с `S3_PUBLIC_URL=http://YOUR_IP:9000`
2. Загрузить файл через Admin Panel
3. Проверить что URL содержит `http://YOUR_IP:9000` а не `http://minio:9000`
4. Проверить загрузку файла с мобильного устройства

## Complexity Assessment
- **Estimated scope**: Already Fixed
- **Risk areas**: Конфигурация окружения

## Open Questions
- [x] Проверить application.yml (Status: RESOLVED - public-url добавлен)
- [x] Проверить StorageService.kt (Status: RESOLVED - использует publicUrl)

## Recommendation
Баг уже исправлен в коде. Нужно:
1. Установить переменную окружения `S3_PUBLIC_URL` с внешним IP
2. Протестировать загрузку и отображение медиа
