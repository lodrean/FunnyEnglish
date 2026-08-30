# 02-execute — bd FunnyEnglish-5tf.8 «KMP: удалить кастомные дубли Result»

## Что сделано

1. **Удалены устаревшие дубли в `core/domain/` (грабля №6):**
   - `core/domain/.../core/domain/Result.kt` — старый `Result<D, E : Error>` (Success/Error, `asEmptyResult`, `EmptyResult`);
   - `core/domain/.../core/domain/DataError.kt` — старый `DataError : Error`;
   - `core/domain/.../core/domain/Error.kt` — marker-интерфейс, использовался только двумя файлами выше.

   Проверка перед удалением: ни один файл в репозитории не импортировал `com.sotospeak.core.domain.Result/DataError/Error/EmptyResult`, не вызывал `Result.Error` / `asEmptyResult`; в пакете `com.sotospeak.core.domain` других файлов (кроме этих трёх и `UiText.kt`) нет — same-package использований тоже нет. Рабочие типы остаются в `core/domain/util/` (`Result` + `DomainError` + `DataError`, Success/Failure), их используют `core:data` (safeCall) и feature-модули (home/auth/profile).

2. **`core/data/.../network/HttpClientExt.kt`** — поправлен устаревший KDoc: `[Result.Error]` → `[Result.Failure]` (ссылка на удалённый класс; кода это не касалось — safeCall и так возвращает `Result.Failure`).

3. **`memory.md`** — конвенция по типам-обёрткам обновлена, грабля №6 помечена закрытой (по AGENTS.md п.2).

## Что осознанно НЕ сделано (скоуп/риски)

- **Переход feature-модулей на `kotlin.Result`** — НЕ выполнялся: по описанию bd это делается «при переработке feature-модулей», а сами feature-модули не подключены к composeApp (memory §1) и не покрываются гейтами драйвера (`desktopTest`/`compileDebugKotlinAndroid`/`compileKotlinWasmJs` их не компилируют) — миграция была бы вслепую. Рабочий `core/domain/util/Result` оставлен как есть.
- **`core/domain/UiText.kt`** — неиспользуемый дубликат (рабочий — `core/presentation/ui/UiText`, у composeApp — `app/error/UiText`), но это НЕ Result-duplicate, вне скоупа задачи. Кандидат на отдельную задачу (отмечено в memory.md).
- `kotlin.Result` на границе API — не тронут (требование «оставить»).

## Изменённые/удалённые файлы

- D `core/domain/src/commonMain/kotlin/com/sotospeak/core/domain/Result.kt`
- D `core/domain/src/commonMain/kotlin/com/sotospeak/core/domain/DataError.kt`
- D `core/domain/src/commonMain/kotlin/com/sotospeak/core/domain/Error.kt`
- M `core/data/src/commonMain/kotlin/com/sotospeak/core/data/network/HttpClientExt.kt` (KDoc)
- M `memory.md` (конвенция + грабля №6)

## Как проверить

- Статическая проверка (выполнена): `grep -rn "com.sotospeak.core.domain.Result\|core.domain.DataError\|core.domain.Error\|Result.Error\|asEmptyResult" --include=*.kt` → 0 совпадений.
- Гейты драйвера: `:composeApp:desktopTest`, `:composeApp:compileDebugKotlinAndroid`, `:composeApp:compileKotlinWasmJs --no-configuration-cache` (composeApp не зависит от `:core:*`, удаление не должно на них повлиять).
- Дополнительно (опционально): `./gradlew :feature-home:compileKotlinDesktop :feature-auth:compileKotlinDesktop :feature-profile:compileKotlinDesktop :core:domain:compileKotlinDesktop :core:data:compileKotlinDesktop` — компиляция модулей, реально использующих `core/domain/util`.
