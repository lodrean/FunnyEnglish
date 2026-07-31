# FunnyEnglish Mobile App - UI Design Prompt for Stitch

## App Overview
Educational mobile app for learning English through interactive tests and gamification. Target audience: Russian-speaking users learning English, all ages.

## Design Style
- **Style**: Modern, playful, child-friendly but not childish
- **Colors**:
  - Primary: Bright blue (#2196F3) or vibrant purple (#7C4DFF)
  - Secondary: Warm orange (#FF9800) for accents and achievements
  - Success: Green (#4CAF50)
  - Background: Light cream/white (#FAFAFA)
  - Cards: White with soft shadows
- **Typography**: Rounded, friendly sans-serif (like Nunito, Quicksand)
- **Icons**: Filled, rounded style with playful feel
- **Illustrations**: Simple, colorful mascot characters (animals or friendly creatures)

## Screens to Design

### 1. Splash Screen
- App logo "FunnyEnglish" with mascot character
- Tagline: "Learn English the fun way!"
- Loading indicator

### 2. Login Screen
- Welcoming illustration at top
- Email input field with icon
- Password input field with show/hide toggle
- "Sign In" button (primary, rounded, full width)
- "Forgot Password?" link
- Divider with "or"
- Social login buttons (Google, Apple)
- "Don't have an account? Sign Up" link at bottom

### 3. Registration Screen
- Progress steps indicator (1. Details, 2. Level, 3. Goals)
- Display name input
- Email input
- Password input with strength indicator
- "Create Account" button
- Terms checkbox
- Back to login link

### 4. Home Screen (Main Dashboard)
**Top Section:**
- User greeting: "Hello, [Name]!"
- User avatar (circular, with level badge)
- Daily streak flame icon with count
- Points/coins display

**Progress Card:**
- Circular progress ring showing level progress
- "Level [X]" label
- "[Y] points to next level"
- Stars collected count

**Categories Section:**
- Horizontal scrollable cards
- Each card: Category icon, name, progress bar, "X/Y completed"
- Categories: Animals, Colors, Numbers, Food, Family, Clothes

**Recent/Recommended Tests:**
- Vertical list of test cards
- Each card: Thumbnail, title, difficulty badge (Easy/Medium/Hard), stars (0-3), points reward

**Bottom Navigation:**
- Home (house icon)
- Categories (grid icon)
- Leaderboard (trophy icon)
- Profile (person icon)

### 5. Categories Screen
- Grid layout (2 columns)
- Large category cards with:
  - Big colorful icon/illustration
  - Category name
  - "X tests" count
  - Progress bar
  - Stars collected

### 6. Category Tests List Screen
**Header:**
- Back button
- Category name and icon
- Total progress for category

**Filter Chips:**
- All, Easy, Medium, Hard
- Completed, Not Started

**Tests List:**
- Card for each test:
  - Left: Test thumbnail or icon
  - Center: Title, difficulty badge, "X questions"
  - Right: Stars (filled/empty), Best score percentage
  - Bottom: Points reward badge

### 7. Test Play Screen
**Top Bar:**
- Close/Exit button (X)
- Progress: "Question 3 of 10"
- Timer (if timed): "02:45"
- Progress bar

**Question Area:**
- Question number badge
- Question text (large, clear)
- Optional: Image or audio play button

**Answers Area (for TEXT_SELECT type):**
- 4 answer option cards
- Each card: Radio/checkbox, answer text
- Selected state: Primary color border, filled background
- Correct state (after submit): Green with checkmark
- Wrong state (after submit): Red with X

**Bottom Section:**
- "Check Answer" / "Next Question" button
- Question dots/numbers for navigation

### 8. Test Results Screen
**Celebration Area:**
- Confetti animation background
- Large stars display (0-3 stars, animated)
- "Great Job!" / "Perfect!" / "Keep Trying!" message

**Score Card:**
- Circular score: "85%"
- "17/20 correct"
- Points earned: "+50 points"
- New best score badge (if applicable)

**Achievements Unlocked:**
- Horizontal scroll of achievement badges
- Each badge: Icon, name, points

**Level Up Section (if applicable):**
- "Level Up!" celebration
- Old level -> New level animation

**Action Buttons:**
- "Try Again" (secondary)
- "Back to Home" (primary)
- "Share" (icon button)

### 9. Profile Screen
**Header:**
- Large avatar (editable)
- Display name
- Email (muted)
- Edit button

**Stats Cards Row:**
- Total Points card
- Tests Completed card
- Current Streak card
- Stars Earned card

**Level Progress:**
- Level badge
- XP progress bar
- "X points to Level Y"

**Achievements Section:**
- "View All" link
- Horizontal preview of recent achievements

**Settings Links:**
- Language
- Notifications
- Sound
- About
- Sign Out

### 10. Achievements Screen
**Header:**
- Back button
- "Achievements"
- Progress: "12/25 unlocked"

**Achievement Grid:**
- Unlocked achievements: Full color with checkmark
- Locked achievements: Grayscale with lock icon
- Each badge:
  - Icon
  - Name
  - Description (on tap)
  - Points value
  - Unlock date (if unlocked)

**Achievement Categories:**
- First Steps (first test, first perfect score)
- Consistency (streaks)
- Mastery (complete categories)
- Collection (total tests, total stars)

### 11. Leaderboard Screen
**Header:**
- "Leaderboard"
- Time filter tabs: This Week, This Month, All Time

**Top 3 Podium:**
- Visual podium with avatars
- 1st: Gold crown, larger
- 2nd: Silver
- 3rd: Bronze
- Points displayed

**Full List:**
- Your rank highlight card
- Scrollable list:
  - Rank number
  - Avatar
  - Display name
  - Points
  - Level badge

## Component Library Needed

### Buttons
- Primary (filled, rounded)
- Secondary (outlined)
- Text button
- Icon button
- FAB (floating action button)

### Input Fields
- Text input with icon
- Password input with visibility toggle
- Search input

### Cards
- Test card
- Category card
- Achievement badge
- Stats card
- Result card

### Navigation
- Bottom navigation bar
- Top app bar with back button
- Tab bar

### Feedback
- Loading spinner
- Skeleton loaders
- Toast messages
- Modal dialogs
- Snackbar

### Gamification Elements
- Star rating (0-3)
- Progress bar
- Level badge
- Streak flame
- Points display
- Achievement badge (locked/unlocked)

## Animations to Consider
- Screen transitions (slide, fade)
- Button press feedback
- Star fill animation on results
- Confetti on perfect score
- Level up celebration
- Streak flame flicker
- Achievement unlock pop

## Responsive Considerations
- Support phones and tablets
- Landscape mode for tablets
- Safe areas for notches/rounded corners
- Dynamic text sizing for accessibility
