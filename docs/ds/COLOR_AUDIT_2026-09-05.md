# Аудит хардкод-цветов admin-web (bd 2oz.4, 2026-09-05)

Инвентарь: 297 вхождений hex в src (85 уникальных), из них 149 вхождений (38 значений) — точные токены v1.3.1, 148 (47 значений) — вне темы. Скан: `#[0-9a-fA-F]{3,8}` в *.ts/tsx, generated Theme.ts учтён отдельно.

## 1. Выполнено (пиксельно-нейтральная замена, verified 156/156 + tsc + vitest)

| Файл | Было | Стало |
|---|---|---|
| components/common/Logo.tsx | 14 hex в SVG (#5B8DEF/#9B7EDE/#FF9F6B) | theme.palette.primary.main / secondary.main / speaking.record |
| components/data/DataTable.tsx | alpha('#E53935',0.08) | (theme) => alpha(theme.palette.error.main, 0.08) |
| screens/Login.tsx | #121212/#EEF3FF тернарник | theme.palette.background.default |
| theme/GlobalStyles.ts | body #F5F5F5/#212121 императивно | theme.palette.background.default / text.primary |

RubricForm.tsx уже 1:1 с токенами v1.3.0 (hex-тернарники с комментариями) — замена на пути требует экспорта secondary-шкалы в палитру (см. п.3.3), откат из батча.

## 2. Дрейф/легаси — требуют DS-решения (не токены v1.3.1)

| Значение | x | Где | Суть |
|---|---|---|---|
| #4A90D9 | 7 | Pagination, StatusBadge(primary), Analytics, FormActions, SearchInput | СТАРЫЙ primary (до v1.3.1) — заменить на primary.main/primaryStrong по контексту |
| #E8EAF6 | 17 | Theme.ts(dark admin), StatusBadge, ErrorBoundary | indigo-50 ≠ brand.50 #EEF3FF — рассинхрон фоновой шкалы |
| #F5F5F5/#212121/#757575/#E0E0E0/#616161/#9E9E9E/#EEEEEE/#FAFAFA | 29 | Pagination, FormActions, SearchInput, StatusBadge(default/draft), Analytics | MUI grey-шкала мимо text.* / divider / action.* токенов |
| #E8F5E9/#FFEBEE/#FFF3E0/#E3F2FD/#F3E5F5 | 9 | StatusBadge lightColor-пары | контейнерные пары статусов — см. п.3 (v1.3.2) |
| #2196F3/#9C27B0 | 3 | StatusBadge(info/secondary) | Material-легаси, не из DS |
| #357ABD/#3A7BC8/#263238/#FFCDD2/#B0BEC5 | 5 | ErrorBoundary (class component) | нужен рефактор на fc/styled для доступа к теме |
| #006C4C | 1 | GradingDetail:129 | как и #8A5200 — цвета текста рубрик вне палитры |

## 3. Предложение tokens.json v1.3.2 (patch, ADR-007 — требуется согласование)

1. **Видео-акценты composeApp**: #1A2E42 (фон плеера), #FFD666 (акцент) — THEME_GRADIENTS Library/VideoScreen — добавить в tokens.json + сгенерировать.
2. **Контейнерные пары статусов admin**: light-пары (#E8F5E9/#FFEBEE/#FFF3E0/#E3F2FD) частично уже есть в speaking.status; добавить infoContainer/secondaryContainer-пары и вторичные статусы (draft/archived).
3. **Экспорт secondary-шкалы в палитру**: generator кладёт brandColors.secondary[100]/[700] только в raw-константу — palette.secondary не содержит 100/700-ключи (RubricForm не может уйти от hex). Либо доп.ключи, либо фикс generator.
4. **Dark-статусы** (#FFB74D new-dark, #81C784 reviewed-dark, контейнеры #3D2A0A/#1B4D1F) — помечены в Theme.ts как «кандидат на расширение».

## 4. composeApp (KMP)
7 hex: THEME_GRADIENTS (Library), VideoScreen #1A2E42/#FFD666 — войдут в v1.3.2 (п.3.1) и заменяются при генерации дизайн-токенов :design.

