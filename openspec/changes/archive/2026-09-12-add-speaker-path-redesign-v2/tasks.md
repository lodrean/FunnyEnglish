# Tasks: add-speaker-path-redesign-v2

## 1. Спека
- [x] 1.1 `docs/DESIGN_SYSTEM_SPEC.md` → v3.3.0: B15 живые превью онбординга; гейты без emoji
- [x] 1.2 delta-spec `wasm-onboarding` (этот change)

## 2. Онбординг (bd txx.2)
- [x] 2.1 `components/OnboardingPreviews.kt`: VideoPreview / TrainingPreview / GradePreview (токены, роли B1–B3, mark-подсветка)
- [x] 2.2 `OnboardingScreen`: превью вместо иконки на контейнере; слайд-структура и тексты без изменений

## 3. Гейты (bd txx.3)
- [x] 3.1 `SpeakingGate`: удалить emoji-ветку; иконка Send в цветном круге
- [x] 3.2 GuestProfileStub / LoginScreen / RegisterScreen — вызовы без emoji

## 4. Верификация (bd txx.4)
- [x] 4.1 Шоукейс: DemoCard с тремя превью (StatesSection)
- [x] 4.2 OnboardingUserFlowTest + гостевые тесты под новую вёрстку
- [x] 4.3 Гейт: desktopTest + detekt + compileKotlinWasmJs
- [x] 4.4 Dropshots: record → validate (Onboarding/Login/Register/Profile_guest)
- [x] 4.5 Maestro онбординг-флоу: живой прогон
