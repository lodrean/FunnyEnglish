# FunnyEnglish Design System Specification

## Version 1.0 - MVP Foundation
**Date:** 2024-02-03  
**Target:** Desktop (responsive: 600dp / 1200dp breakpoints) + Android (future)  
**Audience:** Children 7-14 years old

---

## 1. Design Principles

### Core Philosophy: "Playful but Clear"
- **Gamification-first UI** - motivate through streaks, quests, achievements
- **Accessibility-first** - support ADHD, dyslexia, anxiety
- **Responsive** - works on laptop (13") to monitor (24"+)
- **Immediate feedback** - every action has visual response

### User Needs
- High contrast for readability
- Large touch targets (48dp min) for kids
- Reduce motion option for neurodiversity
- Clear hierarchy for cognitive load management

---

## 2. Design Tokens

### 2.1 Color Palette

#### Primary Colors (Pastel Base + Vibrant Accents)
```kotlin
object FunnyColorTokens {
    // Primary - Calm Blue (trust, learning)
    val Primary = Color(0xFF5B8DEF)        // Pastel blue
    val PrimaryDark = Color(0xFF3D6BC5)    // Darker for dark mode
    val OnPrimary = Color.White
    
    // Secondary - Playful Purple (creativity)
    val Secondary = Color(0xFF9B7EDE)      // Pastel purple
    val SecondaryDark = Color(0xFF7B5EBE)
    val OnSecondary = Color.White
    
    // Tertiary - Energetic Orange (fun, action)
    val Tertiary = Color(0xFFFF9F6B)       // Pastel orange
    val TertiaryDark = Color(0xFFE07F4B)
    val OnTertiary = Color.White
    
    // Semantic Colors
    val Success = Color(0xFF6BCB8A)        // Soft green
    val Warning = Color(0xFFFFD166)        // Soft yellow
    val Error = Color(0xFFFF6B6B)          // Soft red
    val Info = Color(0xFF4ECDC4)           // Teal
    
    // Light Theme Backgrounds
    val Background = Color(0xFFF8F9FA)     // Very light gray
    val Surface = Color.White
    val SurfaceVariant = Color(0xFFF1F3F5)
    
    // Dark Theme Backgrounds
    val BackgroundDark = Color(0xFF1A1D21)
    val SurfaceDark = Color(0xFF252A30)
    val SurfaceVariantDark = Color(0xFF2D333B)
    
    // Text Colors
    val OnBackground = Color(0xFF212529)        // Almost black
    val OnBackgroundDark = Color(0xFFF8F9FA)    // Almost white
    val TextSecondary = Color(0xFF6C757D)       // Gray
    val TextSecondaryDark = Color(0xFFADB5BD)
    
    // Gamification Colors
    val Streak = Color(0xFFFF6B35)             // Fire orange
    val XP = Color(0xFFFFD166)                 // Gold
    val Gem = Color(0xFF4ECDC4)                // Teal gem
    val Achievement = Color(0xFF9B7EDE)        // Purple
}
```

#### Color Usage Guidelines
- **Background:** Light gray (not pure white) - easier on eyes
- **Cards:** White with elevation shadows (3D effect)
- **Accents:** Use Tertiary (orange) for CTAs and gamification
- **Success states:** Soft green (not harsh bright green)

### 2.2 Typography

```kotlin
object FunnyTypography {
    // Font Family: Nunito (rounded, friendly, accessible)
    val FontFamily = NunitoFontFamily
    
    // Scale: Major Third (1.25)
    val DisplayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Bold
    )
    
    val DisplayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.Bold
    )
    
    val DisplaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold
    )
    
    val HeadlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.SemiBold
    )
    
    val HeadlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.SemiBold
    )
    
    val HeadlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold
    )
    
    val TitleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Medium
    )
    
    val TitleMedium = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium
    )
    
    val TitleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    )
    
    val BodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal
    )
    
    val BodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    )
    
    val BodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal
    )
    
    val LabelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    )
    
    val LabelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    )
    
    val LabelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    )
}
```

#### Typography Guidelines
- **Minimum text size:** 14sp (accessibility for kids)
- **Line height:** 1.5x font size (better readability)
- **Font weight range:** Normal (400) to Bold (700)
- **OpenDyslexic:** Available as toggle in settings

### 2.3 Spacing & Sizing

```kotlin
object FunnySpacing {
    // Base unit: 4dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
}

object FunnySizes {
    // Touch targets (accessibility)
    val touchTargetMin = 48.dp
    val touchTargetRecommended = 56.dp
    
    // Button heights
    val buttonSmall = 36.dp
    val buttonMedium = 48.dp
    val buttonLarge = 56.dp
    
    // Icon sizes
    val iconSmall = 20.dp
    val iconMedium = 24.dp
    val iconLarge = 32.dp
    val iconXLarge = 48.dp
    
    // Card radius (balanced)
    val cardRadiusSmall = 8.dp
    val cardRadiusMedium = 16.dp
    val cardRadiusLarge = 20.dp
    val buttonRadius = 12.dp
    
    // Elevation (3D effect)
    val elevationSmall = 2.dp
    val elevationMedium = 4.dp
    val elevationLarge = 8.dp
    val elevationXLarge = 16.dp
}
```

### 2.4 Shapes

```kotlin
object FunnyShapes {
    // Buttons: Medium rounded
    val ButtonShape = RoundedCornerShape(12.dp)
    
    // Cards: Large rounded
    val CardShape = RoundedCornerShape(20.dp)
    
    // Input fields: Medium rounded
    val InputShape = RoundedCornerShape(12.dp)
    
    // Chips/Tags: Full rounded (pill)
    val ChipShape = RoundedCornerShape(50)
    
    // Bottom sheets/Dialogs: Top rounded
    val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    
    // Avatar: Circle
    val AvatarShape = CircleShape
}
```

---

## 3. Component Library

### 3.1 Buttons

```kotlin
// Types
enum class FunnyButtonType {
    PRIMARY,      // Filled, main action
    SECONDARY,    // Outlined, alternative
    TERTIARY,     // Text only, less important
    GHOST,        // Subtle background
    DESTRUCTIVE   // Red, delete actions
}

enum class FunnyButtonSize {
    SMALL,   // 36dp height
    MEDIUM,  // 48dp height (default)
    LARGE    // 56dp height
}

// Usage
FunnyButton(
    text = "Начать урок",
    type = FunnyButtonType.PRIMARY,
    size = FunnyButtonSize.LARGE,
    icon = Icons.Default.PlayArrow,
    isLoading = false,
    onClick = { }
)
```

#### Button Specs
- **Height:** 48dp (medium), 56dp (large for CTAs)
- **Padding:** Horizontal 24dp, Vertical 12dp
- **Icon:** 24dp, at start (leading)
- **Elevation:** 2dp (rest), 4dp (pressed)
- **Radius:** 12dp

### 3.2 Cards

```kotlin
enum class FunnyCardType {
    ELEVATED,   // White with shadow (default)
    FILLED,     // Subtle background color
    OUTLINED    // Border only
}

FunnyCard(
    type = FunnyCardType.ELEVATED,
    elevation = FunnySizes.elevationMedium,
    onClick = { },
    content = { }
)
```

#### Card Specs
- **Radius:** 20dp
- **Elevation:** 4dp (medium), 8dp (high for featured)
- **Padding:** 16dp internal
- **Background:** White (light), SurfaceDark (dark)

### 3.3 Input Fields

```kotlin
FunnyTextField(
    value = text,
    onValueChange = { },
    label = "Email",
    placeholder = "your@email.com",
    leadingIcon = Icons.Default.Email,
    state = InputState.DEFAULT, // DEFAULT, ERROR, SUCCESS, DISABLED
    errorText = "Invalid email"
)
```

#### Input Specs
- **Height:** 56dp
- **Radius:** 12dp
- **Border:** 1dp (rest), 2dp (focused)
- **Icons:** 24dp, clear button on right when typing

### 3.4 Feedback Components

```kotlin
// Progress indicator
FunnyProgressIndicator(
    progress = 0.7f,
    type = ProgressType.LINEAR // or CIRCULAR, SEGMENTED
)

// Snackbar
FunnySnackbar(
    message = "Урок завершён!",
    type = SnackbarType.SUCCESS, // INFO, SUCCESS, WARNING, ERROR
    action = "Отмена" to { }
)

// Badge
FunnyBadge(
    count = 3,
    type = BadgeType.NUMBER // or DOT
)
```

### 3.5 Gamification Components

```kotlin
// Streak Widget
FunnyStreakWidget(
    streak = 12,
    isAtRisk = false,
    onClick = { }
)

// XP Counter with animation
FunnyXPCounter(
    targetXp = 150,
    duration = 1000
)

// Quest Card
FunnyQuestCard(
    quest = dailyQuest,
    onClaim = { }
)

// Achievement Badge
FunnyAchievementBadge(
    achievement = achievement,
    isUnlocked = true
)

// Level Progress
FunnyLevelProgress(
    currentLevel = 5,
    currentXp = 350,
    xpForNextLevel = 500
)
```

---

## 4. Animation System

### 4.1 Durations

```kotlin
object AnimationDurations {
    const val INSTANT = 50L      // Micro-feedback
    const val FAST = 150L        // Button press, color changes
    const val NORMAL = 300L      // Transitions, reveals
    const val SLOW = 500L        // Page transitions, modals
    const val CELEBRATION = 1000L // XP count, confetti
}
```

### 4.2 Easings

```kotlin
object AnimationEasings {
    // Standard
    val Standard = EaseInOutCubic
    
    // Entering (elements appearing)
    val Enter = EaseOutCubic
    
    // Exiting (elements leaving)
    val Exit = EaseInCubic
    
    // Bouncy (celebrations)
    val Bounce = EaseOutBounce
    
    // Elastic (playful)
    val Elastic = EaseOutElastic
}
```

### 4.3 Micro-interactions (Priority)

| Animation | Priority | Implementation |
|-----------|----------|----------------|
| Confetti on correct answer | 5 (Must) | Particle system, 1-2s duration |
| Page transitions | 5 (Must) | Slide + fade, 300ms |
| Loading skeleton | 5 (Must) | Shimmer effect |
| Progress bar fill | 5 (Must) | Smooth interpolation |
| Button press | 4 (Important) | Scale 0.95 + elevation change |
| XP count-up | 3 (Nice) | Number animation |
| Streak flame pulse | 3 (Nice) | Infinite breathing animation |
| Achievement unlock | 3 (Nice) | 3D rotation + sparkle |

### 4.4 Accessibility

```kotlin
// Reduce motion support
val LocalReduceMotion = compositionLocalOf { false }

fun Modifier.optionalAnimation(
    animation: Modifier,
    reducedMotionAlternative: Modifier = Modifier
): Modifier {
    val reduceMotion = LocalReduceMotion.current
    return if (reduceMotion) reducedMotionAlternative else animation
}

// Usage
Box(
    modifier = Modifier
        .optionalAnimation(
            animation = Modifier.scale(scale),
            reducedMotionAlternative = Modifier.alpha(if (scale > 0.9f) 1f else 0.5f)
        )
)
```

---

## 5. Navigation Structure

### 5.1 Desktop Layout (Sidebar)

```
┌─────────────────────────────────────────────────────────────┐
│  Sidebar (200dp)   │  Main Content Area (adaptive)          │
│                    │                                         │
│  [🔥 Logo]         │  ┌─────────────────────────────────┐   │
│                    │  │  Streak Widget (large)          │   │
│  🏠 Главная        │  └─────────────────────────────────┘   │
│  📚 Уроки          │  ┌──────────┐ ┌──────────┐ ┌────────┐ │   │
│  🏆 Достижения     │  │  Quest 1 │ │  Quest 2 │ │ Quest3 │ │   │
│  👤 Профиль        │  └──────────┘ └──────────┘ └────────┘ │   │
│                    │                                         │
│  ──────────────    │  ┌─────────────────────────────────┐   │
│  ⚙️ Настройки      │  │  [Начать урок]                  │   │
│                    │  └─────────────────────────────────┘   │
└────────────────────┴─────────────────────────────────────────┘
```

### 5.2 Breakpoints

```kotlin
object Breakpoints {
    val COMPACT = 0..600      // Mobile-like, bottom nav
    val MEDIUM = 600..1200    // Tablet, rail nav
    val EXPANDED = 1200..Int.MAX_VALUE // Desktop, sidebar
}
```

### 5.3 Navigation Items

```kotlin
val navigationItems = listOf(
    NavItem("Главная", Icons.Default.Home, Screen.Home),
    NavItem("Уроки", Icons.Default.MenuBook, Screen.Lessons),
    NavItem("Достижения", Icons.Default.EmojiEvents, Screen.Achievements),
    NavItem("Профиль", Icons.Default.Person, Screen.Profile)
)
```

---

## 6. Accessibility Guidelines

### 6.1 Touch Targets
- **Minimum:** 48dp × 48dp
- **Recommended:** 56dp × 56dp for primary actions
- **Spacing:** 8dp minimum between targets

### 6.2 Contrast Ratios
- **Normal text:** 4.5:1 minimum (WCAG AA)
- **Large text:** 3:1 minimum
- **Target:** 7:1 for enhanced (WCAG AAA)

### 6.3 Accessibility Features

```kotlin
// 1. Reduce Motion (for ADHD/Autism)
Settings > Accessibility > Reduce Motion

// 2. OpenDyslexic Font
Settings > Accessibility > Dyslexia Font

// 3. High Contrast
Settings > Accessibility > High Contrast

// 4. Larger Text
Settings > Accessibility > Text Size (100% - 200%)
```

### 6.4 Screen Reader Support
- All icons have content descriptions
- Semantic headings hierarchy
- Live regions for dynamic updates
- Focus management for navigation

---

## 7. File Structure

```
composeApp/src/commonMain/kotlin/com/funnyenglish/designsystem/
├── tokens/
│   ├── FunnyColors.kt          # All color definitions
│   ├── FunnyTypography.kt      # Text styles (Nunito)
│   ├── FunnySpacing.kt         # Spacing & sizing
│   └── FunnyShapes.kt          # Border radius definitions
├── components/
│   ├── buttons/
│   │   ├── FunnyButton.kt      # Main button component
│   │   └── FunnyIconButton.kt  # Icon-only button
│   ├── cards/
│   │   └── FunnyCard.kt        # Card container
│   ├── inputs/
│   │   └── FunnyTextField.kt   # Text input
│   ├── feedback/
│   │   ├── FunnyProgress.kt    # Progress indicators
│   │   ├── FunnySnackbar.kt    # Notifications
│   │   └── FunnyBadge.kt       # Count badges
│   └── gamification/
│       ├── FunnyStreakWidget.kt
│       ├── FunnyXPCounter.kt
│       ├── FunnyQuestCard.kt
│       └── FunnyAchievementBadge.kt
├── animations/
│   ├── AnimationDurations.kt
│   ├── AnimationEasings.kt
│   └── ConfettiAnimation.kt    # Celebration effect
├── accessibility/
│   ├── AccessibilityTokens.kt
│   └── ReduceMotionProvider.kt
└── theme/
    ├── FunnyTheme.kt           # Main theme composition
    └── ThemeExtensions.kt      # Helper functions
```

---

## 8. Implementation Checklist

### Phase 1: Foundation
- [ ] Setup Nunito font family
- [ ] Implement Color tokens (Light + Dark)
- [ ] Implement Typography scale
- [ ] Create FunnyTheme composition

### Phase 2: Basic Components
- [ ] FunnyButton (all types + sizes)
- [ ] FunnyCard (all elevations)
- [ ] FunnyTextField
- [ ] FunnyProgressIndicator

### Phase 3: Gamification Components
- [ ] FunnyStreakWidget
- [ ] FunnyXPCounter (with animation)
- [ ] FunnyQuestCard
- [ ] FunnyAchievementBadge

### Phase 4: Animation System
- [ ] Confetti animation
- [ ] Page transitions
- [ ] Skeleton loading
- [ ] Reduce motion support

### Phase 5: Navigation
- [ ] Sidebar (Desktop)
- [ ] Responsive layout handling
- [ ] Navigation state management

### Phase 6: Documentation
- [ ] Compose Previews for all components
- [ ] Usage examples
- [ ] Migration guide from old components

---

## 9. Migration from Current Code

### Color Migration
```kotlin
// OLD (using FunnyColors directly)
Text(color = FunnyColors.OnBackground)

// NEW (using theme)
Text(color = MaterialTheme.colorScheme.onBackground)
```

### Component Migration
```kotlin
// OLD
Button(onClick = { }) { Text("Click") }

// NEW
FunnyButton(
    text = "Click",
    type = FunnyButtonType.PRIMARY,
    onClick = { }
)
```

---

## Appendix: Reference Materials

### Similar Design Systems
- **Material 3:** Base for tokens and structure
- **Duolingo:** Gamification patterns
- **Khan Academy:** Educational focus
- **Headspace:** Calm, pastel aesthetics

### Tools
- **Figma:** Design mockups
- **Compose Preview:** Component testing
- **Accessibility Scanner:** Testing

---

**Next Step:** Start implementation with Phase 1 (Foundation)
