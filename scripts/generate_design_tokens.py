#!/usr/bin/env python3
"""Генератор дизайн-токенов Playful Coach (bd FunnyEnglish-2oz.7).

Единственный источник истины: .docs/design-system/tokens.json (W3C DTCG draft).
Скрипт детерминированно регенерирует 4 артефакта:
  - .docs/design-system/tokens.css
  - composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/theme/SpeakingTokens.kt
  - composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/theme/SpeakingColorScheme.kt
  - admin-web/src/theme/Theme.ts

CI-гейт: после `python scripts/generate_design_tokens.py` `git diff` обязан быть пустым.
Значения, которых НЕТ в tokens.json (производные оттенки MUI-шкалы, dark-статусы,
errorText, массивы теней), зашиты в шаблонах ниже и помечены комментариями —
кандидаты на расширение tokens.json (решение владельца).

Использование:
  python scripts/generate_design_tokens.py          # перезаписать артефакты
  python scripts/generate_design_tokens.py --check  # только проверить дрейф (exit 1 при diff)
"""

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TOKENS_JSON = ROOT / ".docs" / "design-system" / "tokens.json"

TARGETS = {
    "css": ROOT / ".docs" / "design-system" / "tokens.css",
    "speaking_tokens": ROOT / "composeApp" / "src" / "commonMain" / "kotlin"
        / "com" / "sotospeak" / "designsystem" / "theme" / "SpeakingTokens.kt",
    "color_scheme": ROOT / "composeApp" / "src" / "commonMain" / "kotlin"
        / "com" / "sotospeak" / "designsystem" / "theme" / "SpeakingColorScheme.kt",
    "theme_ts": ROOT / "admin-web" / "src" / "theme" / "Theme.ts",
}

with TOKENS_JSON.open(encoding="utf-8") as f:
    T = json.load(f)

VERSION = T["$metadata"]["version"]


def tok(path: str):
    """Значение токена по пути 'color.brand.primary'."""
    node = T
    for part in path.split("."):
        node = node[part]
    if isinstance(node, dict):
        return node["$value"]
    return node


def _hex(path: str) -> str:
    return tok(path).lstrip("#")


def kc(path: str) -> str:
    """Kotlin Color: #RRGGBB -> Color(0xFFRRGGBB), #RRGGBBAA -> Color(0xAARRGGBB)."""
    h = _hex(path)
    if len(h) == 6:
        return f"Color(0xFF{h})"
    return f"Color(0x{h[6:]}{h[:6]})"


def css_rgba(path: str) -> str:
    """#RRGGBBAA -> 'rgba(r, g, b, a)' с alpha, округлённой до 2 знаков."""
    h = _hex(path)
    r, g, b = int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)
    a = int(h[6:8], 16) / 255
    a_s = f"{a:.2f}".rstrip("0").rstrip(".")
    return f"rgba({r}, {g}, {b}, {a_s})"


def px(path: str) -> int:
    return int(str(tok(path)).removesuffix("px"))


def ms(path: str) -> int:
    return int(str(tok(path)).removesuffix("ms"))


def secs(path: str) -> int:
    return int(str(tok(path)).removesuffix("s"))


def bezier(path: str) -> str:
    """'cubic-bezier(0.16, 1, 0.3, 1)' -> '0.16f, 1f, 0.3f, 1f' (Kotlin)."""
    v = tok(path)
    nums = v[v.index("(") + 1:v.index(")")].split(",")

    def fmt(x: str) -> str:
        fl = float(x.strip())
        return f"{int(fl)}f" if fl == int(fl) else f"{fl}f"

    return ", ".join(fmt(x) for x in nums)


def rem(path: str) -> str:
    """px-дименшен -> rem-строка TS ('31px' -> '1.9375rem')."""
    return f"{px(path) / 16:g}rem"


def ts_font(path: str) -> str:
    """Font stack из tokens.json -> TS-строка (одинарные кавычки -> двойные)."""
    return tok(path).replace("'", '"')


def ts_rgba(hex_path: str, alpha: float) -> str:
    """'#161A2E' + 0.08 -> 'rgba(22,26,46,0.08)' (TS-формат без пробелов)."""
    h = _hex(hex_path)
    r, g, b = int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)
    return f"rgba({r},{g},{b},{alpha:g})"


def _rgba2(arg: str) -> str:
    """'color.dark.background,state.hover' -> 'rgba(22,26,46,0.08)' (TS)."""
    hex_path, alpha_path = arg.split(",")
    return ts_rgba(hex_path, float(tok(alpha_path)))


FUNCS = {
    "raw": tok,
    "kc": kc,
    "rgba": css_rgba,
    "px": px,
    "ms": ms,
    "secs": secs,
    "bez": bezier,
    "rem": rem,
    "tsfont": ts_font,
    "rgba2": _rgba2,
}

PLACEHOLDER = re.compile(r"@(?:(\w+):)?([\w.,]+)@")


def render(template: str) -> str:
    def sub(m: re.Match) -> str:
        fn, path = m.group(1), m.group(2)
        if fn is None:
            if path == "ver":
                return VERSION
            raise KeyError(f"Неизвестный плейсхолдер @{path}@")
        return str(FUNCS[fn](path))

    return PLACEHOLDER.sub(sub, template)


# =============================================================================
# tokens.css
# =============================================================================

CSS_TEMPLATE = """/* FunnyEnglish Speaking Trainer — Design Tokens (Playful Coach, Variant B)
   Источник: tokens.json (W3C DTCG draft). Одна палитра — два таргета (Compose + MUI).
   GENERATED FILE v@ver@ — не редактировать вручную; генератор: scripts/generate_design_tokens.py.
   v1.2.0 · 2026-08-01: аудит WCAG — primaryStrong, textMuted/timer затемнены, recordContainer.
   v1.3.0 · 2026-08-07: DSM-2 — M3-роли (surfaceContainer*, outlineVariant, inverse*, scrim,
   surfaceTint, state layers, elevation levels, M3-easing). Цвета v1.2.0 не изменены.
   v1.3.1 · 2026-08-10: errata (утв. владельцем 2026-08-08) — dark onPrimary/onSecondary = #1A2F5E
   (белый на dark primary #8FB3F5 / secondary #B79EED = WCAG FAIL); dark primaryStrong = #8FB3F5;
   удалён --color-surface-warm (нет в tokens.json). */

:root {
  /* Brand */
  --color-primary: @raw:color.brand.primary@;
  --color-primary-strong: @raw:color.brand.primaryStrong@; /* белый текст на кнопках/чипах/nav (4.76:1 AA) */
  --color-on-primary: @raw:color.brand.onPrimary@;
  --color-primary-container: @raw:color.brand.primaryContainer@;
  --color-on-primary-container: @raw:color.brand.onPrimaryContainer@;
  --color-secondary: @raw:color.brand.secondary@;
  --color-on-secondary: @raw:color.brand.onSecondary@;
  --color-secondary-container: @raw:color.brand.secondaryContainer@;
  --color-on-secondary-container: @raw:color.brand.onSecondaryContainer@;
  --color-tertiary: @raw:color.brand.tertiary@;
  --color-on-tertiary: @raw:color.brand.onTertiary@;

  /* Semantic */
  --color-record: @raw:color.semantic.record@;
  --color-record-active: @raw:color.semantic.recordActive@; /* аудит: ≥3:1 */
  --color-record-shadow: @raw:color.semantic.recordShadow@;
  --color-record-container: @raw:color.semantic.recordContainer@;
  --color-on-record-container: @raw:color.semantic.onRecordContainer@;
  --color-on-record: @raw:color.semantic.onRecord@; /* тёмный на record (5.81:1; белый = 2.01 fail) */
  --color-waveform-playback: @raw:color.semantic.waveformPlayback@;
  --color-success: @raw:color.semantic.success@;
  --color-warning: @raw:color.semantic.warning@;
  --color-error: @raw:color.semantic.error@;
  --color-on-error: @raw:color.semantic.onError@;

  /* Timer levels (затемнены ≥3:1 для графики, аудит 2026-08-01) */
  --color-timer-80: @raw:color.timer.level80@;
  --color-timer-50: @raw:color.timer.level50@;
  --color-timer-30: @raw:color.timer.level30@;

  /* Status (контейнеры общие для light/dark — текст на них тёмный, AA) */
  --color-status-new: @raw:color.status.new@;
  --color-status-new-container: @raw:color.status.newContainer@;
  --color-status-reviewed: @raw:color.status.reviewed@;
  --color-status-reviewed-container: @raw:color.status.reviewedContainer@;

  /* Neutral (light periwinkle) */
  --color-background: @raw:color.neutral.background@;
  --color-surface: @raw:color.neutral.surface@;
  --color-surface-card: @raw:color.neutral.surfaceCard@;
  --color-surface-variant: @raw:color.neutral.surfaceVariant@;
  --color-outline: @raw:color.neutral.outline@;
  --color-text: @raw:color.neutral.text@;
  --color-text-muted: @raw:color.neutral.textMuted@; /* 5.32:1 AA (был #6E76A8 = 3.92 fail) */
  --color-scrim-subtitle: @rgba:color.neutral.scrimSubtitle@;
  --color-scrim-video-controls: @rgba:color.neutral.scrimVideoControls@;

  /* M3 roles (v1.3.0) — surface containers / outlines / inverse / scrim / tint */
  --color-surface-container-lowest: @raw:color.m3.surfaceContainerLowest@;
  --color-surface-container-low: @raw:color.m3.surfaceContainerLow@;
  --color-surface-container: @raw:color.m3.surfaceContainer@;
  --color-surface-container-high: @raw:color.m3.surfaceContainerHigh@;
  --color-surface-container-highest: @raw:color.m3.surfaceContainerHighest@; /* = surface-variant, трек кольца */
  --color-outline-variant: @raw:color.m3.outlineVariant@; /* мягкие разделители */
  --color-inverse-surface: @raw:color.m3.inverseSurface@; /* фон Snackbar */
  --color-inverse-on-surface: @raw:color.m3.inverseOnSurface@;
  --color-inverse-primary: @raw:color.m3.inversePrimary@;
  --color-scrim: @rgba:color.m3.scrim@; /* scrim модалок/шторок */
  --color-surface-tint: @raw:color.m3.surfaceTint@; /* = primary, tonal elevation tint */

  /* M3 state layers (v1.3.0) — alpha оверлея цвета контента на контейнере */
  --state-hover: @raw:state.hover@;
  --state-focus: @raw:state.focus@;
  --state-pressed: @raw:state.pressed@;
  --state-dragged: @raw:state.dragged@;
  --state-disabled-content: @raw:state.disabledContent@;
  --state-disabled-container: @raw:state.disabledContainer@;

  /* M3 tonal elevation (v1.3.0) */
  --elevation-level-0: @raw:elevationLevel.level0@;
  --elevation-level-1: @raw:elevationLevel.level1@;  /* ElevatedCard */
  --elevation-level-2: @raw:elevationLevel.level2@;  /* NavigationBar */
  --elevation-level-3: @raw:elevationLevel.level3@;  /* Dialog, FAB, bottom sheet */
  --elevation-level-4: @raw:elevationLevel.level4@;  /* Navigation drawer */
  --elevation-level-5: @raw:elevationLevel.level5@; /* Modal-поверхности */

  /* Typography */
  --font-brand: @raw:font.family.brand@;
  --font-mono: @raw:font.family.mono@;
  --text-label-small: @raw:font.scale.labelSmall@;
  --text-body-small: @raw:font.scale.bodySmall@;
  --text-body-medium: @raw:font.scale.bodyMedium@;
  --text-title-medium: @raw:font.scale.titleMedium@;
  --text-question: @raw:font.scale.questionText@;
  --text-headline-small: @raw:font.scale.headlineSmall@;
  --text-timer: @raw:font.scale.timerDisplay@;
  --text-subtitle: @raw:font.scale.subtitleText@;
  --weight-regular: @raw:font.weight.regular@;
  --weight-semibold: @raw:font.weight.semibold@;
  --weight-bold: @raw:font.weight.bold@;
  --weight-extrabold: @raw:font.weight.extrabold@;

  /* Spacing (4dp grid) */
  --space-xs: @raw:spacing.xs@;
  --space-s: @raw:spacing.s@;
  --space-m: @raw:spacing.m@;
  --space-l: @raw:spacing.l@;
  --space-xl: @raw:spacing.xl@;
  --space-xxl: @raw:spacing.xxl@;

  /* Radius */
  --radius-button: @raw:radius.button@;
  --radius-card: @raw:radius.card@;
  --radius-card-large: @raw:radius.cardLarge@;
  --radius-sheet: @raw:radius.sheet@;
  --radius-chip: @raw:radius.chip@;
  --radius-recorder: @raw:radius.recorder@;   /* squircle кнопки записи — НЕ круг */
  --radius-full: @raw:radius.full@;

  /* Size */
  --size-touch: @raw:size.touchTarget@;
  --size-recorder: @raw:size.recorderButton@;
  --size-recorder-small: @raw:size.recorderButtonSmall@;

  /* Elevation */
  --shadow-card: @raw:elevation.card@;
  --shadow-fab: @raw:elevation.fab@;  /* «оттопыренная» жёсткая тень; :active → 0 1px 0 */
  --shadow-sheet: @raw:elevation.sheet@;
  --shadow-focus: 0 0 0 2px var(--color-background), 0 0 0 4px var(--color-primary);

  /* Motion */
  --ease-standard: @raw:motion.easingStandard@;
  --ease-bounce: @raw:motion.easingBounce@;  /* игровой overshoot для ✅ и появления кнопок */
  --ease-m3-standard: @raw:motion.m3Standard@;    /* v1.3.0 · M3 Standard */
  --ease-m3-emphasized: @raw:motion.m3Emphasized@; /* v1.3.0 · M3 Emphasized */
  --duration-fast: @raw:motion.durationFast@;
  --duration-medium: @raw:motion.durationMedium@;
  --duration-slow: @raw:motion.durationSlow@;
  --duration-m3-state: @raw:motion.m3DurationState@; /* v1.3.0 · M3 state-layer переходы */
  --duration-rec-pulse: @raw:motion.recPulse@;
}

[data-theme="dark"] {
  --color-background: @raw:color.dark.background@;
  --color-surface: @raw:color.dark.surface@;
  --color-surface-card: #252B4A;
  --color-surface-variant: @raw:color.dark.surfaceVariant@;
  --color-text: @raw:color.dark.text@;
  --color-text-muted: @raw:color.dark.textMuted@;
  --color-primary: @raw:color.dark.primary@;
  --color-on-primary: @raw:color.dark.onPrimary@; /* v1.3.1 errata: белый на #8FB3F5 = WCAG FAIL */
  --color-primary-strong: @raw:color.dark.primary@; /* v1.3.1: dark filled-кнопки = primary (текст onPrimary) */
  --color-secondary: @raw:color.dark.secondary@;
  --color-on-secondary: @raw:color.dark.onSecondary@; /* v1.3.1 errata: белый на #B79EED = WCAG FAIL */
  --color-record: @raw:color.dark.record@;
  --color-outline: @raw:color.dark.outline@;
  --shadow-card: 0 1px 2px rgba(0, 0, 0, 0.3), 0 2px 8px rgba(0, 0, 0, 0.25);
  --shadow-focus: 0 0 0 2px var(--color-background), 0 0 0 4px var(--color-primary);

  /* M3 roles (v1.3.0) — dark */
  --color-surface-container-lowest: @raw:color.dark.surfaceContainerLowest@;
  --color-surface-container-low: @raw:color.dark.surfaceContainerLow@;
  --color-surface-container: @raw:color.dark.surfaceContainer@; /* = surface */
  --color-surface-container-high: @raw:color.dark.surfaceContainerHigh@;
  --color-surface-container-highest: @raw:color.dark.surfaceContainerHighest@; /* = surface-variant */
  --color-outline-variant: @raw:color.dark.outlineVariant@;
  --color-primary-container: @raw:color.dark.primaryContainer@;
  --color-on-primary-container: @raw:color.dark.onPrimaryContainer@;
  --color-secondary-container: @raw:color.dark.secondaryContainer@;
  --color-on-secondary-container: @raw:color.dark.onSecondaryContainer@;
  --color-record-container: @raw:color.dark.recordContainer@;
  --color-on-record-container: @raw:color.dark.onRecordContainer@;
  --color-inverse-surface: @raw:color.dark.inverseSurface@;
  --color-inverse-on-surface: @raw:color.dark.inverseOnSurface@;
  --color-inverse-primary: @raw:color.dark.inversePrimary@;
  --color-surface-tint: @raw:color.dark.surfaceTint@;
}

/* Tabular numerals — таймер и длительности не прыгают по ширине */
.tnum {
  font-family: var(--font-mono);
  font-feature-settings: "tnum" 1;
  font-variant-numeric: tabular-nums;
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
"""


def gen_css() -> str:
    return render(CSS_TEMPLATE)


# =============================================================================
# SpeakingTokens.kt
# =============================================================================

SPEAKING_TOKENS_TEMPLATE = """// GENERATED FILE — не редактировать вручную.
// Источник: .docs/design-system/tokens.json; генератор: scripts/generate_design_tokens.py.
package com.sotospeak.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.designsystem.tokens.NunitoFontFamily

/**
 * Speaking Trainer — токены Playful Coach v@ver@ (.docs/design-system/tokens.json).
 *
 * Используются speaking-экранами (legacy-палитра FunnyColorScheme удалена, bd FunnyEnglish-2oz.6).
 * Доступ: `MaterialTheme.speakingColors` или `LocalSpeakingColors.current`.
 *
 * WCAG: на record-фоне (#FF9F6B) — только тёмный текст [text] (5.8:1);
 * белый на record = 2.0:1 (FAIL). textMuted 3.9:1 — только large text.
 */
@Immutable
data class SpeakingColors(
    val primary: Color,            // #5B8DEF — навигация, play-контролы
    val primaryStrong: Color,      // #3B6FD4 — белый текст на кнопках/чипах/nav (4.76:1 AA, аудит 2026-08-01)
    val onPrimary: Color,
    val primaryContainer: Color,   // #DDE8FD
    val onPrimaryContainer: Color, // #1A2F5E
    val secondary: Color,          // #9B7EDE — фирменный фиолетовый
    val onSecondary: Color,        // контент на secondary (dark: #1A2F5E — белый на #B79EED = 2.2:1 FAIL)
    val secondaryContainer: Color, // #E5DCFF (note-bg)
    val onSecondaryContainer: Color, // текст на secondaryContainer (аватар профиля)
    val background: Color,         // #EEF3FF светлый / #161A2E тёмный
    val surface: Color,
    val surfaceVariant: Color,     // трек таймер-кольца
    val text: Color,
    val textMuted: Color,
    val outline: Color,
    val record: Color,             // #FF9F6B — персиковый, НЕ error!
    val onRecord: Color,           // тёмный текст на record (WCAG AA)
    val recordActive: Color,       // #D97238 — waveform при записи (аудит 2026-08-01)
    val recordShadow: Color,       // #D97238 — жёсткая тень rec-кнопки
    val recordContainer: Color,    // #FFE3D1 — подложка record-элементов (аудит 2026-08-01)
    val onRecordContainer: Color,  // #8A3B0E — текст на recordContainer
    val waveformPlayback: Color,   // #5B8DEF
    val timerLevel80: Color,
    val timerLevel50: Color,
    val timerLevel30: Color,
    val statusNew: Color,          // #FB8C00
    val statusNewContainer: Color, // #FFE0B2
    val statusReviewed: Color,     // #43A047
    val statusReviewedContainer: Color, // #C8E6C9
    val success: Color,
    val error: Color,              // #E53935
    val errorText: Color,          // #B3261E — мелкий текст ошибок (WCAG AA 6.4:1, --color-error даёт только 4.29:1)
    val scrimSubtitle: Color,      // #000000B3 — подложка субтитров 70%
    val scrimVideoControls: Color  // #00000080
)

val LightSpeakingColors = SpeakingColors(
    primary = @kc:color.brand.primary@,
    primaryStrong = @kc:color.brand.primaryStrong@,
    onPrimary = @kc:color.brand.onPrimary@,
    primaryContainer = @kc:color.brand.primaryContainer@,
    onPrimaryContainer = @kc:color.brand.onPrimaryContainer@,
    secondary = @kc:color.brand.secondary@,
    onSecondary = @kc:color.brand.onSecondary@,
    secondaryContainer = @kc:color.brand.secondaryContainer@,
    onSecondaryContainer = @kc:color.brand.onSecondaryContainer@,
    background = @kc:color.neutral.background@,
    surface = @kc:color.neutral.surface@,
    surfaceVariant = @kc:color.neutral.surfaceVariant@,
    text = @kc:color.neutral.text@,
    textMuted = @kc:color.neutral.textMuted@,
    outline = @kc:color.neutral.outline@,
    record = @kc:color.semantic.record@,
    onRecord = @kc:color.semantic.onRecord@,
    recordActive = @kc:color.semantic.recordActive@,
    recordShadow = @kc:color.semantic.recordShadow@,
    recordContainer = @kc:color.semantic.recordContainer@,
    onRecordContainer = @kc:color.semantic.onRecordContainer@,
    waveformPlayback = @kc:color.semantic.waveformPlayback@,
    timerLevel80 = @kc:color.timer.level80@,
    timerLevel50 = @kc:color.timer.level50@,
    timerLevel30 = @kc:color.timer.level30@,
    statusNew = @kc:color.status.new@,
    statusNewContainer = @kc:color.status.newContainer@,
    statusReviewed = @kc:color.status.reviewed@,
    statusReviewedContainer = @kc:color.status.reviewedContainer@,
    success = @kc:color.semantic.success@,
    error = @kc:color.semantic.error@,
    errorText = Color(0xFFB3261E), // нет в tokens.json: M3 error text role, кандидат на расширение токенов
    scrimSubtitle = @kc:color.neutral.scrimSubtitle@,
    scrimVideoControls = @kc:color.neutral.scrimVideoControls@
)

val DarkSpeakingColors = LightSpeakingColors.copy(
    primary = @kc:color.dark.primary@,
    primaryStrong = @kc:color.dark.primary@,
    onPrimary = @kc:color.dark.onPrimary@,            // errata dark-ролей: белый на #8FB3F5 = 2.2:1 FAIL
    onSecondary = @kc:color.dark.onSecondary@,          // errata dark-ролей: белый на #B79EED = 2.2:1 FAIL
    primaryContainer = @kc:color.dark.primaryContainer@,     // v1.3.0 M3 dark
    onPrimaryContainer = @kc:color.dark.onPrimaryContainer@,   // v1.3.0 M3 dark
    secondary = @kc:color.dark.secondary@,
    secondaryContainer = @kc:color.dark.secondaryContainer@,   // v1.3.0 M3 dark
    onSecondaryContainer = @kc:color.dark.onSecondaryContainer@, // v1.3.0 M3 dark
    background = @kc:color.dark.background@,
    surface = @kc:color.dark.surface@,
    surfaceVariant = @kc:color.dark.surfaceVariant@,
    text = @kc:color.dark.text@,
    textMuted = @kc:color.dark.textMuted@,
    outline = @kc:color.dark.outline@,
    record = @kc:color.dark.record@,
    onRecord = @kc:color.dark.background@,
    recordContainer = @kc:color.dark.recordContainer@,      // v1.3.0 M3 dark (был #4A2A18)
    onRecordContainer = @kc:color.dark.onRecordContainer@,    // v1.3.0 M3 dark (был #FFCCAA)
    statusNew = Color(0xFFFFB74D),           // нет в tokens.json: dark-статусы, кандидат на расширение
    statusNewContainer = Color(0xFF3D2A0A),  // нет в tokens.json
    statusReviewed = Color(0xFF81C784),      // нет в tokens.json
    statusReviewedContainer = Color(0xFF1B4D1F), // нет в tokens.json
    errorText = Color(0xFFF2B8B5)  // M3 dark error: на #161A2E читается по AA (нет в tokens.json)
)

val LocalSpeakingColors = staticCompositionLocalOf { LightSpeakingColors }

/** Текстовые стили Speaking Trainer (tokens.json font.scale).
 * Базовый шрифт — bundled Nunito (composeResources), таймер — mono tnum. */
@Immutable
object SpeakingTextStyles {
    /** Вопросы читаются с расстояния вытянутой руки: 25sp, w600, lineHeight 1.35 */
    val QuestionText: TextStyle
        @Composable get() = TextStyle(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = @px:font.scale.questionText@.sp,
            lineHeight = 34.sp
        )

    /** Таймер: моноширинные tabular-цифры (tnum) — не прыгает по ширине, 64sp */
    val TimerDisplay = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = @px:font.scale.timerDisplay@.sp,
        lineHeight = 72.sp,
        fontFeatureSettings = "tnum"
    )

    /** Субтитры поверх scrim-подложки: 17sp, lineHeight 1.4 */
    val SubtitleText: TextStyle
        @Composable get() = TextStyle(
            fontFamily = NunitoFontFamily,
            fontSize = @px:font.scale.subtitleText@.sp,
            lineHeight = 24.sp
        )
}

/** Формы Speaking Trainer (tokens.json radius) */
@Immutable
object SpeakingShapes {
    val Recorder = RoundedCornerShape(@px:radius.recorder@.dp)   // squircle кнопки записи, НЕ круг
    val Card = RoundedCornerShape(@px:radius.card@.dp)       // фирменный радиус Variant B
    val CardLarge = RoundedCornerShape(@px:radius.cardLarge@.dp)  // onb-emoji карточка онбординга
    val Button = RoundedCornerShape(@px:radius.button@.dp)     // кнопки и input'ы auth (radius-button)
    val Chip = RoundedCornerShape(@px:radius.chip@.dp)
    val Sheet = RoundedCornerShape(@px:radius.sheet@.dp)      // top corners bottom sheet
    val StatusPill = RoundedCornerShape(@px:radius.full@.dp)
}

/** Жёсткая «оттопыренная» тень rec-кнопки: 0 4px 0 recordShadow; при нажатии — 1dp */
@Immutable
object SpeakingElevation {
    val RecorderShadowOffsetY = 4.dp
    val RecorderShadowPressedOffsetY = 1.dp
}

/** Motion-токены Speaking Trainer (tokens.json motion) */
@Immutable
object SpeakingMotion {
    /** Основной easing UI-переходов: cubic-bezier(0.16, 1, 0.3, 1) */
    val EasingStandard = CubicBezierEasing(@bez:motion.easingStandard@)
    /** Игровой overshoot для ✅ попыток и появления кнопок: cubic-bezier(0.34, 1.56, 0.64, 1) */
    val EasingBounce = CubicBezierEasing(@bez:motion.easingBounce@)

    const val DurationFast = @ms:motion.durationFast@
    const val DurationMedium = @ms:motion.durationMedium@
    const val DurationSlow = @ms:motion.durationSlow@
    /** Пульсация REC; при Reduce motion — статичный индикатор */
    const val RecPulseMs = @ms:motion.recPulse@
    /** Токен motion.timerAnnounceInterval (5s): TalkBack-анонс остатка таймера
     * не чаще раза в 5с, чтобы не спамить скринридер (бриф §3, §3.1 Д2) */
    const val TimerAnnounceIntervalSeconds = @secs:motion.timerAnnounceInterval@

    // M3 motion (tokens v1.3.0 / DSM-5 §3)
    /** M3 Standard: cubic-bezier(0.2, 0, 0, 1) — стандартные переходы */
    val EasingM3Standard = CubicBezierEasing(@bez:motion.m3Standard@)
    /** M3 Emphasized: cubic-bezier(0.05, 0.7, 0.1, 1) — экранные переходы */
    val EasingM3Emphasized = CubicBezierEasing(@bez:motion.m3Emphasized@)
    /** M3 state-анимации (hover/press/смена цвета уровня таймера) */
    const val DurationState = @ms:motion.m3DurationState@

    fun <T> tweenFast(): TweenSpec<T> = tween(DurationFast, easing = EasingStandard)
    fun <T> tweenMedium(): TweenSpec<T> = tween(DurationMedium, easing = EasingStandard)
    fun <T> tweenSlow(): TweenSpec<T> = tween(DurationSlow, easing = EasingStandard)
    fun <T> tweenBounce(): TweenSpec<T> = tween(DurationSlow, easing = EasingBounce)
}
"""


def gen_speaking_tokens() -> str:
    return render(SPEAKING_TOKENS_TEMPLATE)


# =============================================================================
# SpeakingColorScheme.kt
# =============================================================================

COLOR_SCHEME_TEMPLATE = """// GENERATED FILE — не редактировать вручную.
// Источник: .docs/design-system/tokens.json; генератор: scripts/generate_design_tokens.py.
package com.sotospeak.designsystem.theme

import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.designsystem.tokens.NunitoFontFamily

/**
 * M3 color scheme Playful Coach — DSM-5 §1.1 (docs/design/M3_IMPLEMENTATION_MAPPING.md),
 * значения HEX 1:1 из tokens.json v@ver@.
 *
 * Ключевое правило WCAG (спека §3): light `primary` = primaryStrong #3B6FD4,
 * потому что M3 кладёт белый onPrimary на primary (белый на #5B8DEF = 3.23:1 FAIL).
 * «Красивый» #5B8DEF остаётся в surfaceTint / иконках / ссылках.
 *
 * Отступление от DSM-5 (dark onPrimary/onSecondary): в таблице §1.1 указан #FFFFFF,
 * но белый на #8FB3F5/#B79EED = ~2.2:1 FAIL. Взята тёмная пара #1A2F5E (M3-конвенция
 * dark: тёмный контент на светлом primary) — вопрос вынесен в отчёт владельцу.
 */
fun speakingLightColorScheme() = lightColorScheme(
    primary = @kc:color.brand.primaryStrong@,
    onPrimary = @kc:color.brand.onPrimary@,
    primaryContainer = @kc:color.brand.primaryContainer@,
    onPrimaryContainer = @kc:color.brand.onPrimaryContainer@,
    secondary = @kc:color.brand.secondary@,
    onSecondary = @kc:color.brand.onSecondary@,
    secondaryContainer = @kc:color.brand.secondaryContainer@,
    onSecondaryContainer = @kc:color.brand.onSecondaryContainer@,
    tertiary = @kc:color.brand.tertiary@,
    onTertiary = @kc:color.brand.onTertiary@,
    error = @kc:color.semantic.error@,
    onError = @kc:color.semantic.onError@,
    background = @kc:color.neutral.background@,
    onBackground = @kc:color.neutral.text@,
    surface = @kc:color.neutral.surface@,
    onSurface = @kc:color.neutral.text@,
    surfaceVariant = @kc:color.neutral.surfaceVariant@,
    onSurfaceVariant = @kc:color.neutral.textMuted@,
    surfaceContainerLowest = @kc:color.m3.surfaceContainerLowest@,
    surfaceContainerLow = @kc:color.m3.surfaceContainerLow@,
    surfaceContainer = @kc:color.m3.surfaceContainer@,
    surfaceContainerHigh = @kc:color.m3.surfaceContainerHigh@,
    surfaceContainerHighest = @kc:color.m3.surfaceContainerHighest@,
    outline = @kc:color.neutral.outline@,
    outlineVariant = @kc:color.m3.outlineVariant@,
    inverseSurface = @kc:color.m3.inverseSurface@,
    inverseOnSurface = @kc:color.m3.inverseOnSurface@,
    inversePrimary = @kc:color.m3.inversePrimary@,
    scrim = @kc:color.m3.scrim@,
    surfaceTint = @kc:color.m3.surfaceTint@
)

fun speakingDarkColorScheme() = darkColorScheme(
    primary = @kc:color.dark.primary@,
    onPrimary = @kc:color.dark.onPrimary@,
    primaryContainer = @kc:color.dark.primaryContainer@,
    onPrimaryContainer = @kc:color.dark.onPrimaryContainer@,
    secondary = @kc:color.dark.secondary@,
    onSecondary = @kc:color.dark.onSecondary@,
    secondaryContainer = @kc:color.dark.secondaryContainer@,
    onSecondaryContainer = @kc:color.dark.onSecondaryContainer@,
    tertiary = @kc:color.brand.tertiary@,
    onTertiary = @kc:color.brand.onTertiary@,
    error = @kc:color.semantic.error@,
    onError = @kc:color.semantic.onError@,
    background = @kc:color.dark.background@,
    onBackground = @kc:color.dark.text@,
    surface = @kc:color.dark.surface@,
    onSurface = @kc:color.dark.text@,
    surfaceVariant = @kc:color.dark.surfaceVariant@,
    onSurfaceVariant = @kc:color.dark.textMuted@,
    surfaceContainerLowest = @kc:color.dark.surfaceContainerLowest@,
    surfaceContainerLow = @kc:color.dark.surfaceContainerLow@,
    surfaceContainer = @kc:color.dark.surfaceContainer@,
    surfaceContainerHigh = @kc:color.dark.surfaceContainerHigh@,
    surfaceContainerHighest = @kc:color.dark.surfaceContainerHighest@,
    outline = @kc:color.dark.outline@,
    outlineVariant = @kc:color.dark.outlineVariant@,
    inverseSurface = @kc:color.dark.inverseSurface@,
    inverseOnSurface = @kc:color.dark.inverseOnSurface@,
    inversePrimary = @kc:color.dark.inversePrimary@,
    scrim = @kc:color.m3.scrim@,
    surfaceTint = @kc:color.dark.surfaceTint@
)

/**
 * M3 type scale Playful Coach — DSM-5 §2. Размеры/веса Nunito без изменений
 * (tokens.json font.scale), роли — по M3.
 * Основной шрифт — bundled Nunito (composeResources), таймер/таймстемпы — mono tnum.
 */
@Composable
fun speakingTypography() = Typography(
    // timerDisplay 64 · mono tnum · 700
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = @px:font.scale.timerDisplay@.sp,
        lineHeight = 72.sp,
        fontFeatureSettings = "tnum"
    ),
    // headlineSmall 31 · 800
    headlineSmall = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = @px:font.scale.headlineSmall@.sp
    ),
    // questionText 25/1.35 · 600
    titleLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = @px:font.scale.questionText@.sp,
        lineHeight = 34.sp
    ),
    // titleMedium 20 · 800
    titleMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = @px:font.scale.titleMedium@.sp
    ),
    // bodyMedium 16 · 400
    bodyLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = @px:font.scale.bodyMedium@.sp
    ),
    // bodySmall 14 · 400
    bodyMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = @px:font.scale.bodySmall@.sp
    ),
    // labelSmall 12 · 800 · caps
    labelSmall = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = @px:font.scale.labelSmall@.sp,
        letterSpacing = 0.72.sp
    ),
    // timestamps · mono tnum
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = @px:font.scale.labelSmall@.sp,
        fontFeatureSettings = "tnum"
    ),
    // Кнопки M3 (labelLarge): 16 · 800 (weight extrabold из tokens; Nunito 800 даёт акцент,
    // uppercase не нужен — как в MUI-override Theme.ts)
    labelLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = @px:font.scale.bodyMedium@.sp
    )
)

/** M3 shapes-шкала Playful Coach — DSM-5 §2: small=12(chip), medium=16(button), large=22(card), extraLarge=28(sheet/dialog) */
fun speakingShapes() = Shapes(
    small = RoundedCornerShape(@px:radius.chip@.dp),
    medium = RoundedCornerShape(@px:radius.button@.dp),
    large = RoundedCornerShape(@px:radius.card@.dp),
    extraLarge = RoundedCornerShape(@px:radius.sheet@.dp)
)
"""


def gen_color_scheme() -> str:
    return render(COLOR_SCHEME_TEMPLATE)


# =============================================================================
# Theme.ts (admin-web, MUI v6)
# =============================================================================

THEME_TS_PART1 = """// GENERATED FILE — не редактировать вручную.
// Источник: .docs/design-system/tokens.json; генератор: scripts/generate_design_tokens.py.
/**
 * So to speak Admin Web — Design System "Playful Coach" v@ver@
 * Источник HEX: .docs/design-system/tokens.json v@ver@ (вариант B, утверждён владельцем 2026-07-31)
 * Full light/dark theme support with MUI v6
 */

import { createTheme, ThemeOptions, alpha, PaletteMode } from '@mui/material/styles';

// =============================================================================
// DESIGN TOKENS (tokens.json v@ver@ — HEX 1:1)
// Значения без пути в tokens.json (производные оттенки шкал, dark-статусы) —
// литералы, кандидаты на расширение tokens.json (решение владельца).
// =============================================================================

// Brand Colors — шкала вокруг primary #5B8DEF
const brandColors = {
  primary: {
    50: '@raw:color.neutral.background@', // neutral.background (periwinkle)
    100: '@raw:color.brand.primaryContainer@', // primaryContainer
    200: '#BBD0FA',
    300: '@raw:color.dark.primary@', // dark.primary
    400: '#719FF2',
    500: '@raw:color.brand.primary@', // Main brand color
    600: '#4A78D4',
    700: '#3B63B8',
    800: '#2F4F96',
    900: '@raw:color.brand.onPrimaryContainer@', // onPrimaryContainer
  },
  secondary: {
    100: '@raw:color.brand.secondaryContainer@', // secondaryContainer
    300: '@raw:color.dark.secondary@', // dark.secondary
    500: '@raw:color.brand.secondary@', // Main secondary (firm violet)
    700: '@raw:color.brand.onSecondaryContainer@', // onSecondaryContainer
  },
};

// Semantic Colors (tokens.json color.semantic + color.status)
const semanticColors = {
  success: {
    light: '#66BB6A',
    main: '@raw:color.semantic.success@', // semantic.success / status.reviewed
    dark: '#2E7D32',
    contrastText: '#FFFFFF',
  },
  error: {
    light: '#FF897D',
    main: '@raw:color.semantic.error@', // semantic.error
    dark: '#C62828',
    contrastText: '#FFFFFF',
  },
  warning: {
    light: '#FFB74D',
    main: '@raw:color.semantic.warning@', // semantic.warning / status.new
    dark: '#EF6C00',
    contrastText: '#FFFFFF',
  },
  info: {
    light: '@raw:color.dark.primary@',
    main: '@raw:color.brand.primary@', // = brand primary
    dark: '@raw:color.brand.primaryStrong@', // brand primaryStrong (аудит 2026-08-01: белый текст AA)
    contrastText: '#FFFFFF',
  },
};

// Speaking Trainer custom palette (record/timer/status)
const speakingLight = {
  record: '@raw:color.semantic.record@', // semantic.record — дружелюбный персиковый, НЕ красный
  recordActive: '@raw:color.semantic.recordActive@',
  recordShadow: '@raw:color.semantic.recordShadow@',
  recordContainer: '@raw:color.semantic.recordContainer@',
  onRecordContainer: '@raw:color.semantic.onRecordContainer@',
  onRecord: '@raw:color.semantic.onRecord@', // тёмный на record (5.81:1 AA)
  primaryStrong: '@raw:color.brand.primaryStrong@', // кнопки/активные чипы/nav с белым текстом
  waveformPlayback: '@raw:color.semantic.waveformPlayback@',
  timer: {
    level80: '@raw:color.timer.level80@', // затемнены ≥3:1 (аудит 2026-08-01)
    level50: '@raw:color.timer.level50@',
    level30: '@raw:color.timer.level30@',
  },
  status: {
    new: '@raw:color.status.new@',
    newContainer: '@raw:color.status.newContainer@',
    reviewed: '@raw:color.status.reviewed@',
    reviewedContainer: '@raw:color.status.reviewedContainer@',
  },
};

// Dark-вариант: record осветлён для контраста (tokens.json color.dark)
// + dark-контейнеры v1.3.0 (M3): primaryContainer/secondaryContainer/recordContainer
// + dark-статусы: 1:1 composeApp DarkSpeakingColors — единые токены статус-чипов обоих клиентов
const speakingDark = {
  ...speakingLight,
  record: '@raw:color.dark.record@',
  recordContainer: '@raw:color.dark.recordContainer@',
  onRecordContainer: '@raw:color.dark.onRecordContainer@',
  primaryContainer: '@raw:color.dark.primaryContainer@',
  onPrimaryContainer: '@raw:color.dark.onPrimaryContainer@',
  secondaryContainer: '@raw:color.dark.secondaryContainer@',
  onSecondaryContainer: '@raw:color.dark.onSecondaryContainer@',
  status: {
    new: '#FFB74D', // нет в tokens.json: dark-статусы, кандидат на расширение
    newContainer: '#3D2A0A',
    reviewed: '#81C784',
    reviewedContainer: '#1B4D1F',
  },
};

// Chart Colors for Data Visualization (первый = brand primary)
const chartColors = [
  '@raw:color.brand.primary@', // Primary Blue
  '@raw:color.semantic.success@', // Success Green
  '@raw:color.semantic.warning@', // Warning Orange
  '@raw:color.semantic.error@', // Error Red
  '@raw:color.brand.secondary@', // Brand Violet
  '#00BCD4', // Cyan
  '@raw:color.semantic.record@', // Record Peach
  '#795548', // Brown
  '#607D8B', // Blue Grey
  '#FF5722', // Deep Orange
];

// Фирменная тень карточки (tokens.json elevation.card)
const cardShadow = '@raw:elevation.card@';
// Focus ring (tokens.json elevation.focusRing)
const focusRing = '@raw:elevation.focusRing@';
"""

THEME_TS_PART2 = """
// Тёплые индиго-тени вместо нейтрально-чёрных (производная шкала, нет в tokens.json)
const lightShadows = [
  'none',
  '0 1px 2px rgba(45,53,97,0.05)',
  cardShadow,
  '0 3px 6px rgba(45,53,97,0.07)',
  '0 4px 8px rgba(45,53,97,0.08)',
  '0 5px 10px rgba(45,53,97,0.09)',
  '0 6px 12px rgba(45,53,97,0.10)',
  '0 7px 14px rgba(45,53,97,0.11)',
  '0 8px 16px rgba(45,53,97,0.12)',
  '0 9px 18px rgba(45,53,97,0.13)',
  '0 10px 20px rgba(45,53,97,0.14)',
  '0 11px 22px rgba(45,53,97,0.15)',
  '0 12px 24px rgba(45,53,97,0.16)',
  '0 13px 26px rgba(45,53,97,0.17)',
  '0 14px 28px rgba(45,53,97,0.18)',
  '0 15px 30px rgba(45,53,97,0.19)',
  '0 16px 32px rgba(45,53,97,0.20)',
  '0 17px 34px rgba(45,53,97,0.21)',
  '0 18px 36px rgba(45,53,97,0.22)',
  '0 19px 38px rgba(45,53,97,0.23)',
  '0 20px 40px rgba(45,53,97,0.24)',
  '0 21px 42px rgba(45,53,97,0.25)',
  '0 22px 44px rgba(45,53,97,0.26)',
  '0 23px 46px rgba(45,53,97,0.27)',
  '0 24px 48px rgba(45,53,97,0.28)',
] as ThemeOptions['shadows'];

const darkShadows = [
  'none',
  '0 1px 2px rgba(0,0,0,0.3)',
  '0 2px 4px rgba(0,0,0,0.4)',
  '0 3px 6px rgba(0,0,0,0.5)',
  '0 4px 8px rgba(0,0,0,0.5)',
  '0 5px 10px rgba(0,0,0,0.5)',
  '0 6px 12px rgba(0,0,0,0.5)',
  '0 7px 14px rgba(0,0,0,0.5)',
  '0 8px 16px rgba(0,0,0,0.5)',
  '0 9px 18px rgba(0,0,0,0.5)',
  '0 10px 20px rgba(0,0,0,0.5)',
  '0 11px 22px rgba(0,0,0,0.5)',
  '0 12px 24px rgba(0,0,0,0.5)',
  '0 13px 26px rgba(0,0,0,0.5)',
  '0 14px 28px rgba(0,0,0,0.5)',
  '0 15px 30px rgba(0,0,0,0.5)',
  '0 16px 32px rgba(0,0,0,0.5)',
  '0 17px 34px rgba(0,0,0,0.5)',
  '0 18px 36px rgba(0,0,0,0.5)',
  '0 19px 38px rgba(0,0,0,0.5)',
  '0 20px 40px rgba(0,0,0,0.5)',
  '0 21px 42px rgba(0,0,0,0.5)',
  '0 22px 44px rgba(0,0,0,0.5)',
  '0 23px 46px rgba(0,0,0,0.5)',
  '0 24px 48px rgba(0,0,0,0.5)',
] as ThemeOptions['shadows'];

// =============================================================================
// SHARED (typography / shape / spacing)
// =============================================================================

const typography: ThemeOptions['typography'] = {
  fontFamily: '@tsfont:font.family.brand@',
  h1: {
    fontSize: '2.5rem',
    fontWeight: @raw:font.weight.extrabold@,
    lineHeight: 1.2,
    letterSpacing: '-0.01562em',
  },
  h2: {
    fontSize: '2rem',
    fontWeight: @raw:font.weight.bold@,
    lineHeight: 1.3,
    letterSpacing: '-0.00833em',
  },
  h3: {
    fontSize: '1.75rem',
    fontWeight: @raw:font.weight.bold@,
    lineHeight: 1.4,
    letterSpacing: '0em',
    // DSM-5 §2: h3 = timerDisplay — моноширинные tabular-цифры (таймер/длительности)
    fontFamily: '@tsfont:font.family.mono@',
    fontVariantNumeric: 'tabular-nums',
  },
  h4: {
    fontSize: '@rem:font.scale.headlineSmall@', // 31px — headlineSmall (DSM-5 §2)
    fontWeight: @raw:font.weight.extrabold@,
    lineHeight: 1.4,
    letterSpacing: '0.00735em',
  },
  h5: {
    fontSize: '1.25rem',
    fontWeight: @raw:font.weight.bold@,
    lineHeight: 1.5,
    letterSpacing: '0em',
  },
  h6: {
    fontSize: '@rem:font.scale.questionText@', // 25px — questionText (DSM-5 §2)
    fontWeight: @raw:font.weight.semibold@,
    lineHeight: 1.5,
    letterSpacing: '0.0075em',
  },
  subtitle1: {
    fontSize: '@rem:font.scale.titleMedium@', // 20px — titleMedium (DSM-5 §2)
    fontWeight: @raw:font.weight.extrabold@,
    lineHeight: 1.5,
    letterSpacing: '0.00938em',
  },
  subtitle2: {
    fontSize: '@rem:font.scale.bodySmall@',
    fontWeight: @raw:font.weight.semibold@,
    lineHeight: 1.5,
    letterSpacing: '0.00714em',
  },
  body1: {
    fontSize: '@rem:font.scale.bodyMedium@',
    fontWeight: @raw:font.weight.regular@,
    lineHeight: 1.5,
    letterSpacing: '0.00938em',
  },
  body2: {
    fontSize: '@rem:font.scale.bodySmall@',
    fontWeight: @raw:font.weight.regular@,
    lineHeight: 1.5,
    letterSpacing: '0.01071em',
  },
  button: {
    fontSize: '@rem:font.scale.bodySmall@',
    fontWeight: @raw:font.weight.extrabold@, // DSM-5 §2/§5: Nunito 800 даёт акцент, uppercase не нужен
    lineHeight: 1.75,
    letterSpacing: '0.02857em',
    textTransform: 'none',
  },
  caption: {
    fontSize: '@rem:font.scale.labelSmall@',
    fontWeight: @raw:font.weight.extrabold@, // DSM-5 §2: labelSmall 12 · 800 · caps
    lineHeight: 1.66,
    letterSpacing: '0.06em',
    textTransform: 'uppercase',
  },
  overline: {
    fontSize: '@rem:font.scale.labelSmall@',
    fontWeight: @raw:font.weight.semibold@,
    lineHeight: 2.66,
    letterSpacing: '0.08333em',
    textTransform: 'uppercase',
    // DSM-5 §2: timestamps — моноширинные tabular-цифры
    fontFamily: '@tsfont:font.family.mono@',
    fontVariantNumeric: 'tabular-nums',
  },
};

// M3 motion (tokens v1.3.0 / DSM-5 §3): standard easing для UI, emphasized — экранные переходы
const m3Transitions: ThemeOptions['transitions'] = {
  easing: {
    easeInOut: '@raw:motion.m3Standard@', // M3 standard
    sharp: '@raw:motion.m3Emphasized@', // M3 emphasized
  },
  duration: {
    short: @ms:motion.m3DurationState@, // M3 state duration
  },
};
"""

THEME_TS_PART3 = """
// =============================================================================
// LIGHT THEME
// =============================================================================

const lightThemeOptions: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: '@raw:color.brand.primaryStrong@', // brand primaryStrong — белый текст AA (аудит 2026-08-01)
      light: brandColors.primary[500], // #5B8DEF — навигация/акценты
      dark: brandColors.primary[700],
      contrastText: '@raw:color.brand.onPrimary@',
    },
    secondary: {
      main: brandColors.secondary[500],
      light: brandColors.secondary[300],
      dark: brandColors.secondary[700],
      contrastText: '@raw:color.brand.onSecondary@',
    },
    ...semanticColors,
    background: {
      default: '@raw:color.neutral.background@', // neutral.background — светлая перивинкл-подложка
      paper: '@raw:color.neutral.surface@',
    },
    text: {
      primary: '@raw:color.neutral.text@', // neutral.text — глубокий индиго-чаркоал
      secondary: '@raw:color.neutral.textMuted@', // neutral.textMuted (5.32:1 AA, аудит 2026-08-01)
      disabled: alpha('@raw:color.neutral.text@', @raw:state.disabledContent@),
    },
    divider: '@raw:color.m3.outlineVariant@', // m3.outlineVariant (v1.3.0) — мягкие разделители
    action: {
      active: alpha('@raw:color.neutral.text@', 0.54),
      hover: alpha(brandColors.primary[500], @raw:state.hover@), // M3 state layer hover 8%
      selected: alpha(brandColors.primary[500], 0.12),
      disabled: alpha('@raw:color.neutral.text@', 0.26),
      disabledBackground: alpha('@raw:color.neutral.text@', @raw:state.disabledContainer@),
    },
    // Custom admin colors
    admin: {
      sidebar: '@raw:color.neutral.surface@',
      sidebarText: '@raw:color.neutral.text@',
      header: '@raw:color.neutral.surface@',
      border: '@raw:color.neutral.outline@',
      hover: alpha(brandColors.primary[500], 0.06),
      selected: alpha(brandColors.primary[500], 0.12),
      chart: chartColors,
    },
    // Speaking Trainer palette (record/timer/status)
    speaking: speakingLight,
  },
  typography,
  transitions: m3Transitions,
  shape: {
    borderRadius: @px:radius.button@, // radius.button — игровая мягкость
  },
  shadows: lightShadows,
  spacing: 8,
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: @px:radius.button@,
          textTransform: 'none',
          fontWeight: @raw:font.weight.extrabold@,
          padding: '8px 16px',
          transition: 'all 0.2s @raw:motion.m3Standard@', // M3 standard easing
          '&:focus-visible': {
            boxShadow: focusRing,
          },
        },
        contained: {
          boxShadow: 'none', // M3 filled button — без resting-тени (tonal elevation)
          '&:hover': {
            boxShadow: 'none',
          },
        },
        containedPrimary: {
          backgroundColor: '@raw:color.brand.primaryStrong@', // primaryStrong — правило §3 спеки (белый текст AA)
          '&:hover': {
            backgroundColor: '@raw:color.brand.primaryStrong@',
            backgroundImage: 'linear-gradient(@rgba2:color.brand.onPrimary,state.hover@, @rgba2:color.brand.onPrimary,state.hover@)', // M3 hover state layer
          },
        },
        containedSecondary: {
          background: brandColors.secondary[500],
          '&:hover': {
            background: brandColors.secondary[700],
          },
        },
        outlined: {
          borderWidth: 1.5,
          '&:hover': {
            borderWidth: 1.5,
          },
        },
        sizeSmall: {
          padding: '4px 12px',
          fontSize: '0.8125rem',
        },
        sizeLarge: {
          padding: '12px 24px',
          fontSize: '@rem:font.scale.bodyMedium@',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: @px:radius.card@, // radius.card — фирменный радиус Variant B
          boxShadow: cardShadow,
          transition: 'all 0.2s @raw:motion.easingStandard@',
          '&:hover': {
            boxShadow: '0 8px 24px rgba(45,53,97,0.10)',
          },
        },
      },
    },
    MuiCardContent: {
      styleOverrides: {
        root: {
          padding: 24,
          '&:last-child': {
            paddingBottom: 24,
          },
        },
      },
    },
    MuiCardHeader: {
      styleOverrides: {
        root: {
          padding: '16px 24px',
        },
        title: {
          fontSize: '1.125rem',
          fontWeight: @raw:font.weight.bold@,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          padding: '16px',
          borderBottom: `1px solid ${alpha('@raw:color.neutral.text@', @raw:state.hover@)}`,
        },
        head: {
          fontWeight: @raw:font.weight.bold@,
          backgroundColor: alpha('@raw:color.brand.primary@', 0.06),
          color: '@raw:color.neutral.text@',
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: alpha('@raw:color.brand.primary@', @raw:state.hover@), // M3 state layer hover 8%
          },
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '@raw:color.m3.surfaceContainer@', // m3.surfaceContainer (v1.3.0)
          color: '@raw:color.neutral.text@',
          boxShadow: 'none', // M3 — без тени
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: 'none',
          boxShadow: '2px 0 8px rgba(45,53,97,0.06)',
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: @px:radius.full@, // M3 pill (навигационный индикатор)
          margin: '4px 8px',
          padding: '10px 16px',
          transition: 'all 0.2s @raw:motion.m3Standard@',
          '&:hover': {
            backgroundColor: alpha(brandColors.primary[500], @raw:state.hover@),
          },
          '&.Mui-selected': {
            backgroundColor: brandColors.primary[100], // primaryContainer #DDE8FD
            color: brandColors.primary[900], // onPrimaryContainer #1A2F5E
            '&:hover': {
              backgroundColor: alpha(brandColors.primary[500], 0.18),
            },
            '& .MuiListItemIcon-root': {
              color: brandColors.primary[900],
            },
          },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: {
        root: {
          minWidth: 40,
          color: '@raw:color.neutral.textMuted@',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: @px:radius.button@,
            '& fieldset': {
              borderWidth: 1,
              borderColor: '@raw:color.neutral.outline@',
            },
            '&:hover fieldset': {
              borderWidth: 1.5,
              borderColor: brandColors.primary[300],
            },
            '&.Mui-focused fieldset': {
              borderWidth: 2,
              borderColor: '@raw:color.brand.primaryStrong@', // primaryStrong (правило §3: primary-слот M3 в light)
            },
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: @px:radius.chip@, // radius.chip
          fontWeight: @raw:font.weight.semibold@,
        },
      },
    },
    MuiAvatar: {
      styleOverrides: {
        root: {
          fontWeight: @raw:font.weight.semibold@,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          borderRadius: 8,
          fontSize: '@rem:font.scale.labelSmall@',
          fontWeight: @raw:font.weight.semibold@,
        },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: {
          borderRadius: @px:radius.button@,
          boxShadow: '0 4px 20px rgba(45,53,97,0.14)',
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: @px:radius.sheet@, // M3 dialog (shapes.extraLarge)
          backgroundColor: '@raw:color.m3.surfaceContainerHigh@', // m3.surfaceContainerHigh
          boxShadow: '0 24px 48px rgba(45,53,97,0.22)',
        },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: {
          backgroundColor: '@raw:color.m3.surfaceContainerHigh@', // m3.surfaceContainerHigh
          borderRadius: 8,
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: @px:radius.chip@, // DSM-5 E15
        },
      },
    },
    MuiSnackbar: {
      defaultProps: {
        anchorOrigin: { vertical: 'bottom', horizontal: 'center' }, // DSM-5 E15
      },
    },
    MuiSlider: {
      styleOverrides: {
        root: {
          height: 6, // DSM-5 E22: track 6px radius 3, thumb 22px primary
        },
        track: {
          borderRadius: 3,
        },
        rail: {
          borderRadius: 3,
        },
        thumb: {
          width: 22,
          height: 22,
        },
      },
    },
  },
};
"""

THEME_TS_PART4 = """
// =============================================================================
// DARK THEME (tokens.json color.dark)
// =============================================================================

const darkThemeOptions: ThemeOptions = {
  palette: {
    mode: 'dark',
    primary: {
      main: '@raw:color.dark.primary@', // dark.primary
      light: '#BBD0FA',
      dark: '@raw:color.brand.primary@',
      contrastText: '@raw:color.dark.onPrimary@', // dark.onPrimary (errata DSM-5 §1.1, утв. 2026-08-08)
    },
    secondary: {
      main: '@raw:color.dark.secondary@', // dark.secondary
      light: '#D5C5F5',
      dark: '@raw:color.brand.secondary@',
      contrastText: '@raw:color.dark.onSecondary@', // dark.onSecondary (errata DSM-5 §1.1, утв. 2026-08-08)
    },
    success: {
      light: '#81C784',
      main: '@raw:color.semantic.success@',
      dark: '#2E7D32',
      contrastText: '#FFFFFF',
    },
    error: {
      light: '#FF897D',
      main: '@raw:color.semantic.error@',
      dark: '#C62828',
      contrastText: '#FFFFFF',
    },
    warning: {
      light: '#FFB74D',
      main: '@raw:color.semantic.warning@',
      dark: '#EF6C00',
      contrastText: '@raw:color.dark.background@',
    },
    info: {
      light: '#BBD0FA',
      main: '@raw:color.dark.primary@',
      dark: '@raw:color.brand.primary@',
      contrastText: '@raw:color.dark.background@',
    },
    background: {
      default: '@raw:color.dark.background@', // dark.background — индиго-ночь
      paper: '@raw:color.dark.surface@', // dark.surface
    },
    text: {
      primary: '@raw:color.dark.text@', // dark.text
      secondary: '@raw:color.dark.textMuted@', // dark.textMuted
      disabled: alpha('@raw:color.dark.text@', @raw:state.disabledContent@),
    },
    divider: '@raw:color.dark.outlineVariant@', // m3 dark outlineVariant (v1.3.0)
    action: {
      active: alpha('@raw:color.dark.text@', 0.7),
      hover: alpha('@raw:color.dark.primary@', @raw:state.hover@),
      selected: alpha('@raw:color.dark.primary@', 0.16),
      disabled: alpha('@raw:color.dark.text@', 0.3),
      disabledBackground: alpha('@raw:color.dark.text@', @raw:state.disabledContainer@),
    },
    // Custom admin colors for dark mode
    admin: {
      sidebar: '@raw:color.dark.surface@',
      sidebarText: '@raw:color.dark.text@',
      header: '@raw:color.dark.surface@',
      border: '@raw:color.dark.outline@',
      hover: alpha('@raw:color.dark.primary@', @raw:state.hover@),
      selected: alpha('@raw:color.dark.primary@', 0.2),
      chart: chartColors.map(c => c + 'CC'), // Add transparency for dark mode
    },
    speaking: speakingDark,
  },
  typography,
  transitions: m3Transitions,
  shape: lightThemeOptions.shape,
  shadows: darkShadows,
  spacing: 8,
  components: {
    ...lightThemeOptions.components,
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: @px:radius.button@,
          textTransform: 'none',
          fontWeight: @raw:font.weight.extrabold@,
          padding: '8px 16px',
          transition: 'all 0.2s @raw:motion.m3Standard@', // M3 standard easing
          '&:focus-visible': {
            boxShadow: '0 0 0 2px @raw:color.dark.background@, 0 0 0 4px @raw:color.dark.primary@',
          },
        },
        contained: {
          boxShadow: 'none', // M3 filled button — без resting-тени
          '&:hover': {
            boxShadow: 'none',
          },
        },
        containedPrimary: {
          backgroundColor: '@raw:color.dark.primary@',
          color: '@raw:color.dark.background@',
          '&:hover': {
            backgroundColor: '@raw:color.dark.primary@',
            // M3 hover state layer: 8% onPrimary поверх dark.primary (НЕ светлый #5B8DEF)
            backgroundImage: 'linear-gradient(@rgba2:color.dark.background,state.hover@, @rgba2:color.dark.background,state.hover@)',
          },
        },
        containedSecondary: {
          backgroundColor: '@raw:color.dark.secondary@',
          color: '@raw:color.dark.background@',
          '&:hover': {
            backgroundColor: '@raw:color.dark.secondary@',
            // M3 hover state layer: 8% onSecondary поверх dark.secondary (НЕ светлый #9B7EDE)
            backgroundImage: 'linear-gradient(@rgba2:color.dark.background,state.hover@, @rgba2:color.dark.background,state.hover@)',
          },
        },
        outlined: {
          borderWidth: 1.5,
          borderColor: alpha('@raw:color.dark.text@', 0.23),
          '&:hover': {
            borderWidth: 1.5,
            borderColor: alpha('@raw:color.dark.text@', 0.4),
          },
        },
        sizeSmall: {
          padding: '4px 12px',
          fontSize: '0.8125rem',
        },
        sizeLarge: {
          padding: '12px 24px',
          fontSize: '@rem:font.scale.bodyMedium@',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: @px:radius.card@,
          backgroundColor: '@raw:color.dark.surface@',
          border: `1px solid ${alpha('@raw:color.dark.text@', @raw:state.hover@)}`,
          boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
          transition: 'all 0.2s @raw:motion.easingStandard@',
          '&:hover': {
            boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
            borderColor: alpha('@raw:color.dark.text@', 0.12),
          },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          padding: '16px',
          borderBottom: `1px solid ${alpha('@raw:color.dark.text@', @raw:state.hover@)}`,
          color: '@raw:color.dark.text@',
        },
        head: {
          fontWeight: @raw:font.weight.bold@,
          backgroundColor: alpha('@raw:color.dark.primary@', @raw:state.hover@),
          color: '@raw:color.dark.text@',
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: alpha('@raw:color.dark.primary@', @raw:state.hover@), // M3 state layer hover 8%
          },
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '@raw:color.dark.surface@', // m3 dark surfaceContainer
          boxShadow: 'none', // M3 — без тени
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: '@raw:color.dark.surface@',
          borderRight: `1px solid ${alpha('@raw:color.dark.text@', @raw:state.hover@)}`,
          boxShadow: '2px 0 8px rgba(0,0,0,0.3)',
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: @px:radius.full@, // M3 pill (навигационный индикатор)
          margin: '4px 8px',
          padding: '10px 16px',
          transition: 'all 0.2s @raw:motion.m3Standard@',
          '&:hover': {
            backgroundColor: alpha('@raw:color.dark.primary@', @raw:state.hover@),
          },
          '&.Mui-selected': {
            backgroundColor: '@raw:color.dark.primaryContainer@', // m3 dark primaryContainer (v1.3.0)
            color: '@raw:color.dark.onPrimaryContainer@', // m3 dark onPrimaryContainer
            '&:hover': {
              backgroundColor: alpha('@raw:color.dark.primaryContainer@', 0.92),
            },
            '& .MuiListItemIcon-root': {
              color: '@raw:color.dark.onPrimaryContainer@',
            },
          },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: {
        root: {
          minWidth: 40,
          color: '@raw:color.dark.textMuted@',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: @px:radius.button@,
            backgroundColor: alpha('@raw:color.dark.text@', 0.05),
            '& fieldset': {
              borderWidth: 1,
              borderColor: '@raw:color.dark.outline@',
            },
            '&:hover fieldset': {
              borderWidth: 1.5,
              borderColor: alpha('@raw:color.dark.text@', 0.4),
            },
            '&.Mui-focused fieldset': {
              borderWidth: 2,
              borderColor: '@raw:color.dark.primary@',
            },
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: '@raw:color.dark.surface@',
        },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: {
          backgroundColor: '@raw:color.dark.surface@',
          border: `1px solid ${alpha('@raw:color.dark.text@', @raw:state.hover@)}`,
          boxShadow: '0 4px 20px rgba(0,0,0,0.4)',
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: @px:radius.sheet@, // M3 dialog (shapes.extraLarge)
          backgroundColor: '@raw:color.dark.surfaceContainerHigh@', // m3 dark surfaceContainerHigh
          border: `1px solid ${alpha('@raw:color.dark.text@', @raw:state.hover@)}`,
          boxShadow: '0 24px 48px rgba(0,0,0,0.4)',
        },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: {
          backgroundColor: '@raw:color.dark.surfaceContainerHigh@', // m3 dark surfaceContainerHigh
          borderRadius: 8,
        },
      },
    },
  },
};

// =============================================================================
// THEME CREATION
// =============================================================================

export const createAppTheme = (mode: PaletteMode) => {
  return createTheme(mode === 'light' ? lightThemeOptions : darkThemeOptions);
};

// Export individual themes for specific use cases
export const lightTheme = createTheme(lightThemeOptions);
export const darkTheme = createTheme(darkThemeOptions);

// Export default theme (light)
export default lightTheme;

// Export chart colors for use in charts
export { chartColors };

// Type augmentation for custom palette values
declare module '@mui/material/styles' {
  interface SpeakingPalette {
    record: string;
    recordActive: string;
    recordShadow: string;
    waveformPlayback: string;
    timer: {
      level80: string;
      level50: string;
      level30: string;
    };
    status: {
      new: string;
      newContainer: string;
      reviewed: string;
      reviewedContainer: string;
    };
  }
  interface Palette {
    admin: {
      sidebar: string;
      sidebarText: string;
      header: string;
      border: string;
      hover: string;
      selected: string;
      chart: string[];
    };
    speaking: SpeakingPalette;
  }
  interface PaletteOptions {
    admin?: {
      sidebar?: string;
      sidebarText?: string;
      header?: string;
      border?: string;
      hover?: string;
      selected?: string;
      chart?: string[];
    };
    speaking?: SpeakingPalette;
  }
}
"""

THEME_TS_TEMPLATE = (
    THEME_TS_PART1 + THEME_TS_PART2 + THEME_TS_PART3 + THEME_TS_PART4
)


def gen_theme_ts() -> str:
    return render(THEME_TS_TEMPLATE)


# =============================================================================
# main
# =============================================================================

GENERATORS = {
    "css": gen_css,
    "speaking_tokens": gen_speaking_tokens,
    "color_scheme": gen_color_scheme,
    "theme_ts": gen_theme_ts,
}


def main() -> int:
    check = "--check" in sys.argv[1:]
    drifted = []
    for name, gen in GENERATORS.items():
        path = TARGETS[name]
        content = gen()
        if check:
            current = path.read_text(encoding="utf-8") if path.exists() else None
            if current != content:
                drifted.append(path)
                print(f"DRIFT: {path.relative_to(ROOT)}")
        else:
            path.write_text(content, encoding="utf-8", newline="\n")
            print(f"OK: {path.relative_to(ROOT)}")
    if check and drifted:
        print(
            "\nДрейф токенов: артефакты расходятся с tokens.json.\n"
            "Выполните: python scripts/generate_design_tokens.py и закоммитьте результат.",
            file=sys.stderr,
        )
        return 1
    if check:
        print("OK: дрейфа нет, все артефакты соответствуют tokens.json")
    return 0


if __name__ == "__main__":
    sys.exit(main())
