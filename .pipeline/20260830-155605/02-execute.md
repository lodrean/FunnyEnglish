# 02-execute — bd FunnyEnglish-qbq.6 «INF: golden-эталоны Dropshots в репо»

## Что сделано

1. **Проверено фактическое состояние**: утверждение аудита (`PROJECT_AUDIT_2026-08-29` §1.1, «0 golden-эталонов в репо») **устарело**.
   12 golden-эталонов Dropshots **уже закоммичены** в `composeApp/src/androidTest/screenshots/`
   коммитом `f408bf1` (2026-08-12, «test(composeApp): Dropshots screenshot-тесты (golden-эталоны 12 экранов)»),
   коммит является предком текущей ветки (проверено `git merge-base --is-ancestor`).
   Файлы трекаются git'ом, не игнорируются, на диске совпадают с индексом (git status чист).
2. **Сверено покрытие**: имена 12 эталонов 1:1 совпадают с 12 snapshot-именами в
   `composeApp/src/androidInstrumentedTest/.../screenshot/ScreenshotTest.kt`
   (Library, Login, MySubmissions, Onboarding, Practice_ready, Profile_auth, Profile_guest,
   Questions_auth, Questions_guest_gate, Register, Topics, Training_idle).
3. **Найдена причина ложного вывода аудита**: комментарий в `composeApp/build.gradle.kts:156`
   указывал неверный путь (`src/androidInstrumentedTest/screenshots`). Исправлен на фактический
   путь `src/androidTest/screenshots` (AGP-директория Dropshots-плагина, грабля №98 memory.md).
   Это единственная правка кода.

## Изменённые файлы

- `composeApp/build.gradle.kts` — только комментарий (путь к golden-эталонам).

## Известные оговорки (не блокеры, действия для драйвера/следующих сессий)

- **Эталоны сняты 2026-08-10, ДО перехода на bundled Nunito** (bd FunnyEnglish-2oz.3, 2026-08-30;
  в memory.md прямо зафиксировано: «Скриншот-эталоны дадут diff по гарнитуре — переснимать при
  следующем record»). Пересъёмка требует эмулятора Medium_Phone 1080x2400@420 и gradle record-таски
  (`./gradlew :composeApp:recordDebugAndroidTestScreenshots`) — по ограничениям задачи gradle не
  запускался. Рекомендуется переснять эталоны при ближайшем прогоне гейта драйвером.
- CI-гейт остаётся локальным: ни один workflow в `.github/workflows/` не запускает
  `connectedDebugAndroidTest` (нужен Android-эмулятор в раннере). Подключение CI-джоба —
  отдельная задача, выходила бы за рамки «закоммитить эталоны».

## Как проверить

```bash
git ls-files composeApp/src/androidTest/screenshots/ | wc -l   # → 12
git log --oneline -1 -- composeApp/src/androidTest/screenshots/ # → f408bf1
# Полный гейт (нужен эмулятор Medium_Phone 1080x2400@420, одно устройство):
./gradlew :composeApp:connectedDebugAndroidTest
```
