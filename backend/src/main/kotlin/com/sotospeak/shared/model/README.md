# Копии shared-моделей для backend (bd FunnyEnglish-0w3.2)

Эти 8 файлов — **копии** `shared/src/commonMain/kotlin/com/sotospeak/shared/model/`
(те же FQN `com.sotospeak.shared.model.*`), сделанные 2026-08-29 при изоляции
legacy backend и снятии зависимости `implementation(project(":shared"))`.

Зачем: legacy-контроллеры/сервисы backend (gamification/adaptive/tests) используют
эти модели как DTO. Копирование с сохранением FQN позволило не трогать ~30 импортов.

Правила:
- Оригиналы в `:shared` НЕ удалять — их может использовать composeApp (грабля memory.md №51).
- При правке модели держать копии синхронными (diff с shared-оригиналом).
- Дубликат устраняется при окончательном удалении legacy-кода backend (bd FunnyEnglish-8zm).
