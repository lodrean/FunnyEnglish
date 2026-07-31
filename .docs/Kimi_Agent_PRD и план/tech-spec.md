# FunnyEnglish Technical Specification

## Component Inventory

### shadcn/ui Components (Built-in)

| Component | Purpose | Customization |
|-----------|---------|---------------|
| `Button` | Primary actions, CTAs | Custom colors, sizes, icons |
| `Card` | Content containers | Custom shadows, borders |
| `Dialog` | Modals, achievement unlocks | Custom animations |
| `Progress` | Progress indicators | Circular and linear variants |
| `Avatar` | User avatars | Sizes, borders |
| `Badge` | Notifications, labels | Colors, variants |
| `Tooltip` | Hints, info | Custom delay |
| `ScrollArea` | Custom scrollbars | Hidden scrollbar option |
| `Separator` | Dividers | Custom colors |
| `Sheet` | Bottom sheets, drawers | Slide animations |

### Third-party Components

| Component | Registry | Purpose |
|-----------|----------|---------|
| `@magicui/confetti` | @magicui | Achievement celebrations |
| `@magicui/number-ticker` | @magicui | XP counter animation |
| `@magicui/animated-beam` | @magicui | Connection effects |

### Custom Components to Build

| Component | Purpose | Location |
|-----------|---------|----------|
| `StreakWidget` | Streak display with flame animation | `components/gamification/` |
| `XPCounter` | Animated XP counter | `components/gamification/` |
| `AchievementBadge` | Achievement unlock modal | `components/gamification/` |
| `QuestCard` | Quest progress display | `components/gamification/` |
| `LevelProgress` | Circular level indicator | `components/gamification/` |
| `CategoryCard` | Category carousel item | `components/cards/` |
| `QuestionCard` | Test question option | `components/cards/` |
| `AnswerFeedback` | Correct/incorrect overlay | `components/feedback/` |
| `BottomNav` | Mobile navigation | `components/navigation/` |
| `NavigationRail` | Tablet navigation | `components/navigation/` |
| `AnimatedProgress` | Animated progress bars | `components/animations/` |

---

## Animation Implementation Table

| Animation | Library | Implementation Approach | Complexity |
|-----------|---------|------------------------|------------|
| Button press scale | Framer Motion | `whileTap={{ scale: 0.95 }}` | Low |
| Button hover lift | Framer Motion | `whileHover={{ y: -2, shadow }}` | Low |
| Card hover effect | Framer Motion | `whileHover={{ y: -4 }}` | Low |
| Page transitions | Framer Motion | `AnimatePresence` + variants | Medium |
| Staggered list | Framer Motion | `staggerChildren` in variants | Medium |
| Streak flame pulse | CSS + Framer | Keyframe animation loop | Low |
| XP counter | @magicui/number-ticker | Built-in component | Low |
| Progress fill | Framer Motion | `animate` width/height | Low |
| Circular progress | Framer Motion | SVG stroke animation | Medium |
| Confetti | @magicui/confetti | Trigger on achievement | Low |
| Achievement modal | Framer Motion | Scale + opacity variants | Medium |
| Badge rotation | Framer Motion | `rotate` keyframes | Low |
| Answer feedback | Framer Motion | Shake + color variants | Medium |
| Scroll reveal | Framer Motion | `whileInView` | Low |
| Navigation indicator | Framer Motion | `layoutId` for shared element | Medium |
| Category carousel | CSS + Framer | Scroll snap + drag | Medium |

---

## Animation Library Choices

### Primary: Framer Motion
**Rationale:**
- Native React integration
- Declarative API matches React patterns
- Excellent performance with GPU acceleration
- Built-in gesture support (hover, tap, drag)
- AnimatePresence for exit animations
- Layout animations for shared elements

### Secondary: CSS Animations
**Use for:**
- Simple infinite loops (flame pulse)
- Micro-interactions
- Reduced motion fallbacks

### Third-party: @magicui
**Use for:**
- Confetti effects
- Number tickers
- Complex visual effects

---

## Project File Structure

```
my-app/
├── app/
│   ├── page.tsx                 # Home screen
│   ├── layout.tsx               # Root layout with theme
│   ├── globals.css              # Global styles + CSS variables
│   ├── test/
│   │   └── page.tsx             # Test play screen
│   ├── profile/
│   │   └── page.tsx             # Profile screen
│   ├── achievements/
│   │   └── page.tsx             # Achievements screen
│   └── leaderboard/
│       └── page.tsx             # Leaderboard screen
│
├── components/
│   ├── ui/                      # shadcn/ui components
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── progress.tsx
│   │   ├── avatar.tsx
│   │   ├── badge.tsx
│   │   ├── tooltip.tsx
│   │   ├── scroll-area.tsx
│   │   ├── separator.tsx
│   │   └── sheet.tsx
│   │
│   ├── gamification/            # Gamification components
│   │   ├── streak-widget.tsx
│   │   ├── xp-counter.tsx
│   │   ├── achievement-badge.tsx
│   │   ├── quest-card.tsx
│   │   ├── level-progress.tsx
│   │   └── confetti-effect.tsx
│   │
│   ├── cards/                   # Card components
│   │   ├── daily-progress-card.tsx
│   │   ├── category-card.tsx
│   │   ├── question-card.tsx
│   │   ├── continue-learning-card.tsx
│   │   └── stat-card.tsx
│   │
│   ├── navigation/              # Navigation components
│   │   ├── bottom-nav.tsx
│   │   ├── navigation-rail.tsx
│   │   └── navigation-drawer.tsx
│   │
│   ├── feedback/                # Feedback components
│   │   └── answer-feedback.tsx
│   │
│   ├── animations/              # Animation components
│   │   ├── animated-progress.tsx
│   │   ├── flame-animation.tsx
│   │   └── page-transition.tsx
│   │
│   └── layout/                  # Layout components
│       ├── app-shell.tsx
│       ├── responsive-layout.tsx
│       └── header.tsx
│
├── hooks/
│   ├── use-window-size.ts       # Window size detection
│   ├── use-theme.ts             # Theme management
│   └── use-reduced-motion.ts    # Motion preference
│
├── lib/
│   ├── utils.ts                 # Utility functions
│   ├── animations.ts            # Animation variants
│   └── constants.ts             # App constants
│
├── types/
│   └── index.ts                 # TypeScript types
│
├── public/
│   └── images/                  # Static images
│
├── tailwind.config.ts
├── next.config.js
└── package.json
```

---

## Dependencies

### Core
```bash
# Already included with shadcn init
- next
- react
- react-dom
- typescript
- tailwindcss
- @radix-ui/*
- class-variance-authority
- clsx
- tailwind-merge
- lucide-react
```

### Animation
```bash
npm install framer-motion
```

### Fonts
```bash
# Nunito from Google Fonts (via next/font)
```

### Optional Third-party
```bash
npx shadcn add @magicui/confetti
npx shadcn add @magicui/number-ticker
```

---

## CSS Variables (Tailwind Config)

```javascript
// tailwind.config.ts
theme: {
  extend: {
    colors: {
      // Primary
      primary: {
        DEFAULT: '#3B82F6',
        dark: '#60A5FA',
      },
      // Secondary
      secondary: {
        DEFAULT: '#10B981',
        dark: '#34D399',
      },
      // Tertiary
      tertiary: {
        DEFAULT: '#F97316',
        dark: '#FB923C',
      },
      // Background
      background: '#F8FAFC',
      'background-dark': '#0F172A',
      // Surface
      surface: {
        DEFAULT: '#FFFFFF',
        dark: '#1E293B',
        variant: '#F1F5F9',
        'variant-dark': '#334155',
      },
      // Gamification
      streak: '#FF6B35',
      'xp-gold': '#FFD700',
      'gem-teal': '#14B8A6',
      'accent-purple': '#8B5CF6',
      // Semantic
      success: '#22C55E',
      warning: '#F59E0B',
      error: '#F43F5E',
    },
    fontFamily: {
      nunito: ['var(--font-nunito)', 'sans-serif'],
    },
    borderRadius: {
      'sm': '8px',
      'md': '12px',
      'lg': '16px',
      'xl': '20px',
      'full': '9999px',
    },
    spacing: {
      'xs': '4px',
      'sm': '8px',
      'md': '16px',
      'lg': '24px',
      'xl': '32px',
      '2xl': '48px',
    },
    animation: {
      'flame-pulse': 'flamePulse 2s ease-in-out infinite',
      'bounce-in': 'bounceIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55)',
      'slide-up': 'slideUp 0.3s ease-out',
      'shake': 'shake 0.5s ease-in-out',
    },
    keyframes: {
      flamePulse: {
        '0%, 100%': { transform: 'scale(1)', opacity: '1' },
        '50%': { transform: 'scale(1.05)', opacity: '0.9' },
      },
      bounceIn: {
        '0%': { transform: 'scale(0.8)', opacity: '0' },
        '100%': { transform: 'scale(1)', opacity: '1' },
      },
      slideUp: {
        '0%': { transform: 'translateY(20px)', opacity: '0' },
        '100%': { transform: 'translateY(0)', opacity: '1' },
      },
      shake: {
        '0%, 100%': { transform: 'translateX(0)' },
        '25%': { transform: 'translateX(-5px)' },
        '75%': { transform: 'translateX(5px)' },
      },
    },
  },
}
```

---

## Responsive Breakpoints

```javascript
// tailwind.config.ts
screens: {
  'xs': '360px',
  'sm': '640px',
  'md': '768px',
  'lg': '1024px',
  'xl': '1280px',
}
```

---

## Animation Variants (Framer Motion)

```typescript
// lib/animations.ts

export const fadeInUp = {
  initial: { opacity: 0, y: 20 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -20 },
};

export const fadeIn = {
  initial: { opacity: 0 },
  animate: { opacity: 1 },
  exit: { opacity: 0 },
};

export const scaleIn = {
  initial: { opacity: 0, scale: 0.8 },
  animate: { opacity: 1, scale: 1 },
  exit: { opacity: 0, scale: 0.8 },
};

export const slideInFromBottom = {
  initial: { y: '100%' },
  animate: { y: 0 },
  exit: { y: '100%' },
};

export const staggerContainer = {
  animate: {
    transition: {
      staggerChildren: 0.05,
    },
  },
};

export const buttonTap = {
  scale: 0.95,
};

export const cardHover = {
  y: -4,
  transition: { duration: 0.2 },
};
```

---

## State Management

### Local State (useState)
- Current question index
- Selected answer
- Animation triggers
- Modal open/close

### Context (React Context)
- Theme (light/dark)
- User progress
- Current streak
- Achievements

### No External State Library Needed
The app is primarily presentational with mock data.

---

## Performance Considerations

1. **Animation Performance**
   - Use `transform` and `opacity` only
   - Add `will-change` for heavy animations
   - Respect `prefers-reduced-motion`

2. **Image Optimization**
   - Use Next.js Image component
   - Lazy load below-fold images
   - Provide appropriate sizes

3. **Code Splitting**
   - Dynamic imports for heavy components
   - Route-based splitting (Next.js default)

4. **Bundle Size**
   - Tree-shake unused icons
   - Import only needed Framer Motion features

---

## Accessibility Implementation

1. **Touch Targets**
   - All interactive elements: min 48px
   - Buttons: min 48px height

2. **Focus Management**
   - Visible focus indicators
   - Focus trap in modals
   - Return focus on modal close

3. **Screen Readers**
   - Semantic HTML
   - Aria labels on icons
   - Live regions for updates

4. **Reduced Motion**
   ```typescript
   const prefersReducedMotion = useReducedMotion();
   // Disable animations if true
   ```

---

## Testing Strategy

1. **Visual Regression**
   - Screenshot tests for components
   - Theme switching tests

2. **Interaction Tests**
   - Button clicks
   - Navigation flow
   - Animation triggers

3. **Accessibility Tests**
   - Keyboard navigation
   - Screen reader compatibility
   - Color contrast

4. **Responsive Tests**
   - All breakpoints
   - Orientation changes
