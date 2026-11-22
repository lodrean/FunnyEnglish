# Proposal: add-speaker-path-redesign-v2

## Why

Редизайн «Путь спикера» V1 (`openspec/changes/archive`-преемник: `add-speaker-path-redesign`,
bd `FunnyEnglish-d1e`) сознательно не трогал онбординг и гостевые гейты — передаточный документ
V1 (§4.4/§6) объявил их скоупом следующей итерации: «V2 с онбордингом и гостевыми гейтами».
UX-ревью фиксирует две открытые точки:

- **Точка 5 (средний)** — «Статичный онбординг»: три слайда с иконками не показывают продукт —
  ни видео, ни демо записи, ни того, как выглядит оценка.
- **Точка 6 (низкий)** — emoji-иконка 📬 в гостевых гейтах «вопреки дисциплине системы».

Макетов V2 не существует; дизайн-язык закреплён пакетом V1 (`docs/design/redesign-speaker-path-v1/`,
DESIGN_SYSTEM_SPEC v3.2.0) и является авторитетом. «Живое превью» в Compose реализуется честно:
слайды онбординга рендерят мини-версии НАСТОЯЩИХ компонентов продукта (плеер, Training, оценка)
из тех же токенов — слайд показывает, как продукт выглядит на самом деле.

Согласование: задача V2 поставлена владельцем 2026-09-11 («нужно доделать v2»); скоуп взят
дословно из передаточного документа V1. bd: эпик `FunnyEnglish-txx`.

## What Changes

- **OnboardingScreen**: вместо иконки на цветном контейнере каждый слайд получает живое превью:
  - слайд 1 «Смотри видео» — мини-плеер (тёмная карточка, белая play-кнопка, CC-бейдж,
    персиковая полоса прогресса, строка транскрипта с mark-подсветкой «Hello!» — те же роли,
    что на реальном VideoScreen);
  - слайд 2 «Тренируйся вслух» — мини-Training (чип «Попытка 1 из 3 · 80 сек» уровня 80,
    таймер-кольцо, squircle rec-кнопка с фирменной тенью, waveform) — роли SpeakingRecording B1–B3;
  - слайд 3 «Отправь учителю» — мини-оценка (score 8.5, 4 бара критериев, зелёная плашка
    «Оценка получена») — как MySubmissions gradecard.
- **Гостевые гейты**: `SpeakingGate(emoji = "📬")` → иконка `SpeakingIcons.Send` в цветном круге
  (secondaryContainer/onSecondaryContainer) в трёх местах: GuestProfileStub, LoginScreen,
  RegisterScreen. Emoji-параметр `SpeakingGate` удаляется (дисциплина «no emoji as UI»).
- **НЕ меняется**: тексты слайдов и CTA («Смотри видео»/«Далее»/«Начать» — Maestro-флоу завязаны,
  грабля №122), контракты MVI, теги `onboarding_*`/`guest_profile_*`, маршруты, guest-first флоу,
  Login/Register-формы (кроме замены emoji в их гейтах), токены.

## Impact

- **Affected specs**: delta к capability `wasm-onboarding` (онбординг-превью) —
  `specs/wasm-onboarding/spec.md`; поведенческих изменений нет — только презентационный слой.
- **Affected code**: `OnboardingScreen.kt` (+новый `components/OnboardingPreviews.kt`),
  `SpeakingAuth.kt` (удаление emoji-ветки), `ProfileScreen.kt`/`LoginScreen.kt`/`RegisterScreen.kt`
  (вызовы гейтов), шоукейс (демо превью в StatesSection), тесты (OnboardingUserFlowTest),
  Dropshots-эталоны (Onboarding/Login/Register/GuestProfile), Maestro-онбординг-флоу (проверка).
- **Документы**: `docs/DESIGN_SYSTEM_SPEC.md` → v3.3.0 (B15 «живые превью онбординга»,
  правило «no emoji in UI» распространено на гейты).
