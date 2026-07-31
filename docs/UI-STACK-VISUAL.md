# FunnyEnglish UI Stack Visual Reference

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FUNNYENGLISH UI STACK 2024                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                     APPLICATION LAYER (Screens)                      │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │   │
│  │  │   Home       │  │  Test Play   │  │   Profile    │  │ Settings │ │   │
│  │  │   Screen     │  │   Screen     │  │   Screen     │  │  Screen  │ │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └──────────┘ │   │
│  └──────────────────────────────┬──────────────────────────────────────┘   │
│                                 │                                           │
│  ┌──────────────────────────────▼──────────────────────────────────────┐   │
│  │                   DESIGN SYSTEM LAYER (Components)                   │   │
│  │                                                                      │   │
│  │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │   │
│  │   │  Buttons    │  │   Cards     │  │   Inputs    │  │ Feedback  │  │   │
│  │   │  ├ Primary  │  │  ├ Elevated │  │  ├ TextField│  │ ├ Snackbar│  │   │
│  │   │  ├ Secondary│  │  ├ Filled   │  │  ├ Search   │  │ ├ Dialog  │  │   │
│  │   │  ├ Tertiary │  │  ├ Outlined │  │  ├ Dropdown │  │ ├ Tooltip │  │   │
│  │   │  ├ FAB      │  │  ├ Featured │  │  ├ Chip     │  │ └ Skeleton│  │   │
│  │   │  └ IconBtn  │  │  └ Clickable│  │  └ Filter   │  │           │  │   │
│  │   └─────────────┘  └─────────────┘  └─────────────┘  └───────────┘  │   │
│  │                                                                      │   │
│  │   ┌─────────────────────────────────────────────────────────────┐   │   │
│  │   │              GAMIFICATION COMPONENTS                         │   │   │
│  │   │  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐ │   │   │
│  │   │  │Streak Widget │ │ XP Counter   │ │ Achievement Badge    │ │   │   │
│  │   │  │ [🔥 12 days] │ │ [+50 ⭐]     │ │ 🏆 Новичок           │ │   │   │
│  │   │  └──────────────┘ └──────────────┘ └──────────────────────┘ │   │   │
│  │   │  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐ │   │   │
│  │   │  │Level Progress│ │  Quest Card  │ │ Leaderboard Item     │ │   │   │
│  │   │  │ ┌────────┐   │ │ [Complete 5] │ │ #1 Alex ⭐ 1250      │ │   │   │
│  │   │  │ │  LVL 5 │   │ │ [████████░░] │ │ #2 Sam  ⭐ 980       │ │   │   │
│  │   │  │ │  75%   │   │ │ [Claim 🎁]   │ │ #3 Kim  ⭐ 875       │ │   │   │
│  │   │  │ └────────┘   │ └──────────────┘ └──────────────────────┘ │   │   │
│  │   │  └──────────────┘                                           │   │   │
│  │   └─────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        THEME LAYER (Tokens)                          │   │
│  │                                                                      │   │
│  │  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────────┐ │   │
│  │  │   COLORS        │ │   TYPOGRAPHY    │ │      SPACING            │ │   │
│  │  │  Light  Dark    │ │  Nunito Font    │ │  SpaceXs = 4.dp         │ │   │
│  │  │  ├ Primary      │ │  ├ DisplayLarge │ │  SpaceSm = 8.dp         │ │   │
│  │  │  ├ Secondary    │ │  ├ Headline     │ │  SpaceMd = 16.dp        │ │   │
│  │  │  ├ Background   │ │  ├ Title        │ │  SpaceLg = 24.dp        │ │   │
│  │  │  ├ Surface      │ │  ├ Body         │ │  SpaceXl = 32.dp        │ │   │
│  │  │  └ Gamification │ │  └ Label        │ │  TouchTarget = 48.dp    │ │   │
│  │  └─────────────────┘ └─────────────────┘ └─────────────────────────┘ │   │
│  │                                                                      │   │
│  │  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────────┐ │   │
│  │  │     SHAPES      │ │  ANIMATIONS     │ │    ACCESSIBILITY        │ │   │
│  │  │  Button = 12dp  │ │  Instant = 50ms │ │  ├ Touch targets 48dp   │ │   │
│  │  │  Card   = 20dp  │ │  Fast = 150ms   │ │  ├ Content descriptions │ │   │
│  │  │  Input  = 12dp  │ │  Normal = 300ms │ │  ├ Focus indicators     │ │   │
│  │  │  Chip   = 50%   │ │  Slow = 500ms   │ │  ├ Reduce motion        │ │   │
│  │  │  Dialog = 20dp  │ │  Celebration=1s │ │  └ High contrast mode   │ │   │
│  │  └─────────────────┘ └─────────────────┘ └─────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      ADAPTIVE LAYER (Responsive)                     │   │
│  │                                                                      │   │
│  │   ┌────────────┐      ┌────────────┐      ┌────────────────────┐   │   │
│  │   │  COMPACT   │      │   MEDIUM   │      │     EXPANDED       │   │   │
│  │   │  <600dp    │      │  600-840dp │      │      >840dp        │   │   │
│  │   │            │      │            │      │                    │   │   │
│  │   │ ┌────────┐ │      │ ┌───┬────┐ │      │ ┌────┬─────┬─────┐ │   │   │
│  │   │ │   📱   │ │      │ │   │    │ │      │ │    │     │     │ │   │   │
│  │   │ │  [⊔]   │ │      │ │ ⊡ │    │ │      │ │ 🖥️ │     │     │ │   │   │
│  │   │ │ Single │ │      │ │   │    │ │      │ │    │     │     │ │   │   │
│  │   │ │ Column │ │      │ │   │    │ │      │ │ 3  │Pane │Side │ │   │   │
│  │   │ └────────┘ │      │ └───┴────┘ │      │ │Pane│     │bar  │ │   │   │
│  │   │            │      │            │      │ └────┴─────┴─────┘ │   │   │
│  │   │ Bottom Nav │      │  Rail Nav  │      │  Permanent Drawer  │   │   │
│  │   └────────────┘      └────────────┘      └────────────────────┘   │   │
│  │                                                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        PLATFORM LAYER                                │   │
│  │                                                                      │   │
│  │   ┌─────────────────────────────────────────────────────────────┐   │   │
│  │   │  Jetpack Compose 1.6+  │  Compose Multiplatform  │  Material 3 │   │   │
│  │   │  + Animation API       │  1.6+ (iOS/Desktop)     │  1.2+       │   │   │
│  │   └─────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════
                              COLOR PALETTE REFERENCE
═══════════════════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│                              LIGHT THEME                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Primary        Secondary       Background       Surface                    │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐      ┌─────────┐                │
│  │ #3B82F6 │    │ #10B981 │    │ #F8FAFC │      │ #FFFFFF │                │
│  │  ████   │    │  ████   │    │  ████   │      │  ████   │                │
│  └─────────┘    └─────────┘    └─────────┘      └─────────┘                │
│                                                                             │
│  Gamification Colors:                                                       │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐                        │
│  │ #FF6B35 │  │ #FFD700 │  │ #14B8A6 │  │ #8B5CF6 │                        │
│  │ 🔥 Fire │  │ ⭐ Gold │  │ 💎 Gem  │  │ 🏆 Rank │                        │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              DARK THEME                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Primary        Secondary       Background       Surface                    │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐      ┌─────────┐                │
│  │ #60A5FA │    │ #34D399 │    │ #0F172A │      │ #1E293B │                │
│  │  ████   │    │  ████   │    │  ████   │      │  ████   │                │
│  └─────────┘    └─────────┘    └─────────┘      └─────────┘                │
│                                                                             │
│  (Brighter, more saturated for better visibility on dark backgrounds)       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════
                              COMPONENT EXAMPLES
═══════════════════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│ BUTTONS                                                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  PRIMARY              SECONDARY           TERTIARY          FAB             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────┐      ┌────┐          │
│  │ 🚀 Continue  │    │ 📝 Skip      │    │ Cancel   │      │ +  │          │
│  └──────────────┘    └──────────────┘    └──────────┘      └────┘          │
│  [Filled Blue]       [Outlined]          [Text only]      [Circular]       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ CARDS                                                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ELEVATED                    FEATURED                   OUTLINED            │
│  ┌──────────────┐           ┌──────────────┐           ┌──────────────┐    │
│  │              │           │ ╔══════════╗ │           │              │    │
│  │   Content    │           │ ║ Content  ║ │           │   Content    │    │
│  │              │           │ ╚══════════╝ │           │              │    │
│  └──────────────┘           └──────────────┘           └──────────────┘    │
│  (4dp shadow)               (Accent border)            (1dp outline)       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ GAMIFICATION WIDGETS                                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  STREAK WIDGET               XP COUNTER                QUEST CARD           │
│  ┌──────────────┐           ┌──────────────┐          ┌──────────────┐     │
│  │   🔥 12      │           │   +50 ⭐     │          │ Complete 5   │     │
│  │   дней       │           │   (counting) │          │ lessons      │     │
│  │ [░░▓▓▓▓▓▓░░] │           │              │          │ [████████░░] │     │
│  │  MTWTFSS     │           │              │          │ [Claim 🎁]   │     │
│  └──────────────┘           └──────────────┘          └──────────────┘     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════
                           ANIMATION TIMING CHART
═══════════════════════════════════════════════════════════════════════════════

  Duration    Use Case                          Example
  ───────────────────────────────────────────────────────────────────────────
  50ms   │    Micro-feedback                 │  Color change on hover
         │                                   │
  150ms  │    Button presses                 │  Scale down 0.95 on press
         │    State toggles                  │  Switch animation
         │                                   │
  300ms  │    Page transitions               │  Screen slide
         │    Content reveals                │  Card expand
         │                                   │
  500ms  │    Major layout changes           │  Bottom sheet open
         │    Modal dialogs                  │  Dialog scale + fade
         │                                   │
  1000ms │    Gamification celebrations      │  Confetti explosion
         │    Achievement unlocks            │  Badge bounce + rotate
         │                                   │
  2000ms │    Extended celebrations          │  Level up ceremony
         │                                   │


═══════════════════════════════════════════════════════════════════════════════
                           RESPONSIVE BREAKPOINTS
═══════════════════════════════════════════════════════════════════════════════

  Width          Class          Layout                      Navigation
  ─────────────────────────────────────────────────────────────────────────────
  < 360dp   │    xs         │  Compact (reduced)       │  Bottom
  360-600dp │    sm         │  Compact (standard)      │  Bottom
  600-840dp │    md         │  Two-column              │  Rail
  840-1200dp│    lg         │  Three-column            │  Drawer
  > 1200dp  │    xl         │  Full desktop            │  Drawer


═══════════════════════════════════════════════════════════════════════════════
                              FILE STRUCTURE
═══════════════════════════════════════════════════════════════════════════════

composeApp/src/commonMain/kotlin/com/funnyenglish/designsystem/
│
├── theme/
│   ├── FunnyTheme.kt                 # Main theme wrapper
│   ├── FunnyColorScheme.kt           # Light/Dark schemes
│   └── DynamicColorSupport.kt        # Android 12+ dynamic colors
│
├── tokens/
│   ├── FunnyColors.kt                # Color definitions
│   ├── FunnyTypography.kt            # Text styles (Nunito)
│   ├── FunnySpacing.kt               # Spacing scale
│   └── FunnyShapes.kt                # Component shapes
│
├── components/
│   ├── buttons/
│   │   ├── FunnyButton.kt            # All button variants
│   │   ├── FunnyFAB.kt               # Floating action button
│   │   └── FunnyIconButton.kt        # Icon-only buttons
│   │
│   ├── cards/
│   │   ├── FunnyElevatedCard.kt
│   │   ├── FunnyFilledCard.kt
│   │   ├── FunnyOutlinedCard.kt
│   │   └── FunnyFeaturedCard.kt
│   │
│   ├── inputs/
│   │   ├── FunnyTextField.kt
│   │   ├── FunnySearchBar.kt
│   │   ├── FunnyDropdown.kt
│   │   └── FunnyChip.kt
│   │
│   ├── feedback/
│   │   ├── FunnySnackbar.kt
│   │   ├── FunnyDialog.kt
│   │   ├── FunnyBottomSheet.kt
│   │   └── FunnyProgressIndicator.kt
│   │
│   └── gamification/
│       ├── FunnyStreakWidget.kt
│       ├── FunnyXPCounter.kt
│       ├── FunnyAchievementBadge.kt
│       └── FunnyLevelProgress.kt
│
├── animations/
│   ├── AnimationDurations.kt         # Duration constants
│   ├── AnimationEasings.kt           # Easing curves
│   └── PageTransitions.kt            # Navigation transitions
│
├── accessibility/
│   ├── AccessibilityUtils.kt
│   └── ReduceMotionProvider.kt
│
└── adaptive/
    ├── WindowSizeClass.kt            # Size class definitions
    ├── AdaptiveLayout.kt             # Responsive layouts
    └── ResponsiveUtils.kt            # Helper functions


═══════════════════════════════════════════════════════════════════════════════
                              QUICK START
═══════════════════════════════════════════════════════════════════════════════

1. Wrap your app with FunnyTheme:

   @Composable
   fun MyApp() {
       FunnyTheme(
           darkTheme = isSystemInDarkTheme(),
           dynamicColor = true
       ) {
           // Your content
       }
   }

2. Use components:

   FunnyPrimaryButton(
       text = "Continue",
       onClick = { /* ... */ },
       icon = Icons.Default.ArrowForward
   )

3. Make responsive:

   val windowSizeClass = calculateWindowSizeClass()
   
   when (windowSizeClass) {
       WindowSizeClass.COMPACT -> CompactLayout()
       WindowSizeClass.MEDIUM -> MediumLayout()
       WindowSizeClass.EXPANDED -> ExpandedLayout()
   }

═══════════════════════════════════════════════════════════════════════════════
```

---

**Reference:** This visual guide complements the full [PRD](../prd/FUNNYENGLISH-REDESIGN-2024.prd.md)
