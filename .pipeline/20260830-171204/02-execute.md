# 02-execute — bd FunnyEnglish-2oz.7: DS генератор токенов из tokens.json + CI-гейт

## Что сделано

1. **Генератор `scripts/generate_design_tokens.py`** (Python 3, stdlib only).
   Единственный источник истины — `.docs/design-system/tokens.json` (v1.3.1).
   Скрипт детерминированно регенерирует 4 артефакта из шаблонов с плейсхолдерами
   (`@kc:color.brand.primary@`, `@px:radius.card@`, `@bez:motion.*@`, `@rem:font.scale.*@` и др.):
   - `.docs/design-system/tokens.css`
   - `composeApp/.../designsystem/theme/SpeakingTokens.kt`
   - `composeApp/.../designsystem/theme/SpeakingColorScheme.kt`
   - `admin-web/src/theme/Theme.ts`
   Режимы: без флагов — перезапись файлов; `--check` — проверка дрейфа (exit 1 + список файлов).
   Workflow: правка tokens.json → `python scripts/generate_design_tokens.py` → коммит.
2. **Артефакты перегенерированы** с заголовком «GENERATED FILE». По значениям (hex/px/ms/rem/sp/dp)
   вывод байт-в-байт совпал со старыми ручными версиями (проверено diff + хэшами мультимножеств
   всех HEX и дименшенов) — поведение UI не изменилось. Побочные правки: версии в комментариях
   актуализированы (v1.1/v1.3.0 → v1.3.1 из `$metadata.version`), css-тени приведены к формату
   tokens.json (rgba без пробелов).
3. **CI-гейт** в `.github/workflows/ci.yml`: job `design-tokens` (setup-python 3.12 →
   `python scripts/generate_design_tokens.py --check`), включён в `ci-summary` (needs + таблица + цикл).
4. **memory.md** — запись в «Решения и договорённости».

## Проверки (выполнены)

- `python scripts/generate_design_tokens.py` → exit 0, повторный `--check` → exit 0 (идемпотентно).
- Негативный тест: добавлена строка в Theme.ts → `--check` → exit 1 «DRIFT» → файл восстановлен.
- Все HEX-цвета и дименшены старых и новых файлов совпали (md5 мультимножеств) для 4 файлов.
- Theme.ts — парсинг TypeScript-компилятором (`ts.transpileModule`, 0 errors).
- `ci.yml` — валидный YAML.
- Gradle-сборки/тесты не запускались (гейты драйвера).

## Кандидаты на решение владельца (НЕ блокеры, ADR-007)

Значения, отсутствующие в tokens.json и зашитые литералами в шаблонах (помечены комментариями
«нет в tokens.json»): производные MUI-оттенки brand-шкалы (200/400/600/700/800), light/dark-варианты
success/error/warning, **dark-статусы** (#FFB74D/#3D2A0A/#81C784/#1B4D1F — дублируются в
SpeakingTokens.kt и Theme.ts), **errorText** (#B3261E light / #F2B8B5 dark), массивы теней MUI,
dark surfaceCard #252B4A. Имеет смысл вынести в tokens.json (минорная версия) — тогда гейт
покроет и их.

## Изменённые/созданные файлы

- `scripts/generate_design_tokens.py` — новый
- `.docs/design-system/tokens.css` — перегенерирован (только заголовок/формат теней)
- `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/theme/SpeakingTokens.kt` — перегенерирован
- `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/theme/SpeakingColorScheme.kt` — перегенерирован
- `admin-web/src/theme/Theme.ts` — перегенерирован
- `.github/workflows/ci.yml` — job `design-tokens` + включение в `ci-summary`
- `memory.md` — запись о решении
