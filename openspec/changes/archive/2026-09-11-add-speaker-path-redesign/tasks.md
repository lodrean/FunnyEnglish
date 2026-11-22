# Tasks: add-speaker-path-redesign

## 1. Спека и подготовка
- [x] 1.1 Копия пакета V1 в `docs/design/redesign-speaker-path-v1/` (холст, прототип, UX-ревью, передаточный документ)
- [x] 1.2 `docs/DESIGN_SYSTEM_SPEC.md` → v3.2.0: авторитет V1-пакета для 9 экранов, B8 SpeakingAppBar + back-кнопка, новые brand-компоненты (B10 нав-бейдж, B11 continue/trail, B12 hero-вопрос, B13 setrow, B14 уровень-путь), правило маппинга значений прототипа на токены v1.3.1
- [x] 1.3 `docs/SPEAKING_TRAINER_SPEC_PART2.md`: §1 навигация (back-кнопки, бейдж), §2.1 Library (continue-карточка, trail), §2.4 Questions (hero + dual CTA), профиль/настройки

## 2. Каркас (bd d1e.2)
- [x] 2.1 `SpeakingIcons`: ChevronLeft (icons.svg + ImageVector вручную)
- [x] 2.2 `SpeakingAppBar`: опциональный `onBack` (круглая 40dp, surface, рамка outlineVariant)
- [x] 2.3 Бейдж «Отправки» в `NavItems.kt` (+ rail): счётчик от `submissions_last_seen` (multiplatform-settings), гашение при открытии вкладки
- [x] 2.4 ProfileScreen: строка-вход в Settings

## 3. Библиотека и топики (bd d1e.3)
- [x] 3.1 LibraryScreen: continue-карточка + trail + карточки тем с мини-кольцом
- [x] 3.2 TopicsScreen: toprow-строки со статус-чипами, back-кнопка

## 4. Флоу обучения (bd d1e.4)
- [x] 4.1 VideoScreen: CC-бейдж, peach-прогресс, моно-таймер, CTA-блок + note
- [x] 4.2 QuestionsScreen: hero-карточка + компактный список + dual CTA + note
- [x] 4.3 TrainingScreen/PracticeScreen: выверка деталей (чип попытки, точки, панели result/final, тексты)

## 5. Профиль и настройки (bd d1e.5)
- [x] 5.1 ProfileScreen: кольцо недели, стрик, путь уровней (скрытие без данных), мини-статы
- [x] 5.2 SettingsScreen: setrow-строки, back-кнопка, testTags

## 6. Тесты и гейты (bd d1e.6)
- [x] 6.1 commonTest UI-тесты под новые layouts
- [x] 6.2 Шоукейс: секции для back-аппбара, нав-бейджа, уровня-пути, trail, setrow
- [x] 6.3 Гейты: `:composeApp:desktopTest :composeApp:detekt :composeApp:compileKotlinWasmJs`
- [x] 6.4 Живой прогон на эмуляторе (обе темы, скриншоты)
- [x] 6.5 Dropshots: record → validate (Medium_Phone 1080x2400@420)
- [x] 6.6 e2e-cmp: перекалибровка POS-координат, зелёный прогон
- [x] 6.7 Maestro: синхронизация текстов флоу
