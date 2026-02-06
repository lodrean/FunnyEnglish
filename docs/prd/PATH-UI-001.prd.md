# PRD: Learning Path UI

## Ticket
PATH-UI-001

## Status
DRAFT

## Context
На основе исследования Duolingo и best practices геймификации, нам нужно внедрить систему визуального Learning Path, которая заменит текущий список категорий на линейный путь прогрессии.

## Goals

1. **Увеличить engagement**: Линейный путь мотивирует продолжать обучение
2. **Улучшить retention**: Пользователи видят свой прогресс и хотят идти дальше
3. **Упростить navigation**: Clear progression vs выбор из списка
4. **Увеличить completion rate**: Меньше выбора = меньше decision fatigue

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Daily Active Users | +20% | Analytics |
| Session Length | +15% | Analytics |
| Lesson Completion Rate | +25% | Backend stats |
| User Retention (Day 7) | +10% | Cohort analysis |
| Path Node Completion | 80%+ | Backend stats |

## User Stories

### Story 1: First-time User
As a new user, I want to see a clear path forward so that I know what to do next.

**Acceptance Criteria:**
- [ ] Path показывается сразу после onboarding
- [ ] Первый node подсвечен как "Start here"
- [ ] Визуальная подсказка о том, как начать

### Story 2: Returning User
As a returning user, I want to continue from where I left off so that I don't waste time navigating.

**Acceptance Criteria:**
- [ ] Path автоматически скроллится к текущему node
- [ ] Текущий node явно выделен
- [ ] Можно быстро начать следующий урок

### Story 3: Motivated Learner
As a motivated learner, I want to see upcoming content so that I stay excited about learning.

**Acceptance Criteria:**
- [ ] Locked nodes видны, но затемнены
- [ ] Milestone nodes выделены особо
- [ ] Rewards за upcoming milestones показаны

### Story 4: Casual User
As a casual user, I want to understand my progress at a glance so that I feel accomplished.

**Acceptance Criteria:**
- [ ] Progress percentage показан
- [ ] Completed nodes имеют visual celebration
- [ ] Streak integration visible

## Out of Scope

- Branching paths (для MVP только linear)
- Custom path creation by users
- Social features on path (friends progress)
- Premium/locked content gates

## Path Structure

```
Unit 1: Basics (10 nodes)
├── Node 1-1: Introduction ✓
├── Node 1-2: Alphabet ✓
├── Node 1-3: Greetings ✓
├── Node 1-4: Basic Phrases ← Current
├── Node 1-5: Numbers [Locked]
├── Node 1-6: Colors [Locked]
├── [Milestone: Unit 1 Complete - Reward: 50 gems]
└── Node 1-7: Family [Locked]
    ...

Unit 2: Everyday Life (15 nodes)
├── Node 2-1: Food [Locked]
└── ...
```

## Node Types

| Type | Icon | State | Description |
|------|------|-------|-------------|
| **Lesson** | Book/Star | Current, Completed, Locked | Regular lesson node |
| **Quiz** | Question | Current, Completed, Locked | Checkpoint quiz |
| **Milestone** | Trophy/Gem | Reached, Locked | Unit completion reward |
| **Story** | Chat bubble | Current, Completed, Locked | Dialog practice |
| **Boss** | Crown | Current, Locked | Hard challenge |

## Node States

### Visual States
```
COMPLETED:      🟢 Green with checkmark, glowing
CURRENT:        🔵 Blue pulsing, "START" button
LOCKED:         ⚪ Gray, faded, lock icon
MILESTONE:      🏆 Golden with sparkles when reached
```

### Animations
- **Completed → Current**: Smooth scroll and pulse
- **Unlock**: Scale up + sparkle effect
- **Milestone**: Confetti + level-up style animation
- **Scroll**: Parallax effect on background

## UI Design

### Path Layout
```
    [1-1]      [1-3]
         \    /    \
          [1-2]    [1-4] ← Current
         /    \    /
    [1-5]      [1-6]
              |
         [Milestone]
              |
           [2-1]
```

### Screen Layout
```
┌─────────────────────────────┐
│  🔥 12   💎 150   ⚡ Level 5 │  <- Streak/Gems/Level
├─────────────────────────────┤
│                             │
│      [Path ScrollView]      │
│                             │
│         ◯───◯              │
│        /     \             │
│       ◯       ◯ ← Current  │
│        \     /             │
│         ◯───◯              │
│            |               │
│         🏆 Milestone       │
│                             │
├─────────────────────────────┤
│  [Home] [Path] [Profile]   │  <- Bottom nav
└─────────────────────────────┘
```

### Node Component
```kotlin
@Composable
fun PathNode(
    node: PathNode,
    state: NodeState,
    onClick: () -> Unit
) {
    // Size: 80dp x 80dp
    // Shape: Circle for lessons, Star for milestones
    // Animation: Pulsing for current, glow for completed
}
```

## Technical Requirements

### Backend
- [ ] New endpoint: `GET /api/path` - весь путь пользователя
- [ ] New endpoint: `GET /api/path/progress` - текущий прогресс
- [ ] Extend `User` entity: `currentPathNodeId`, `pathProgress`
- [ ] New entity: `PathNode` (id, unitId, order, type, contentId)
- [ ] Migration: существующие users получают начальный прогресс

### Mobile
- [ ] New screen: `PathScreen`
- [ ] New ViewModel: `PathViewModel`
- [ ] New components: `PathNode`, `PathLine`, `PathMilestone`
- [ ] Animation utilities: `PathAnimations`
- [ ] Integration: Navigation from Home

### Data Models
```kotlin
data class PathNode(
    val id: String,
    val unitId: Int,
    val order: Int,
    val type: NodeType, // LESSON, QUIZ, MILESTONE, STORY, BOSS
    val title: String,
    val description: String?,
    val contentId: String?, // link to lesson/test
    val rewards: Rewards?,
    val requirements: Requirements?
)

data class UserPathProgress(
    val userId: String,
    val currentNodeId: String,
    val completedNodes: List<String>,
    val unlockedNodes: List<String>,
    val overallProgress: Float // 0.0 - 1.0
)

enum class NodeType {
    LESSON, QUIZ, MILESTONE, STORY, BOSS
}

enum class NodeState {
    COMPLETED, CURRENT, LOCKED, MILESTONE_REACHED
}
```

## Open Questions

- [ ] Как мигрировать существующий прогресс пользователей?
- [ ] Будут ли branching paths в будущем?
- [ ] Нужен ли offline mode для path?
- [ ] Как часто обновлять path content?

## Dependencies

- Backend: Path management APIs
- Mobile: Animation libraries (already have)
- Design: Icons for node types

## Affected Areas

- Backend: New controllers, services, repositories
- Mobile: New screen, components, navigation
- Admin: Path management UI (future)

## Rollout Plan

1. **Phase 1**: Backend APIs and data model
2. **Phase 2**: Basic Path UI (no animations)
3. **Phase 3**: Animations and polish
4. **Phase 4**: A/B test with 50% users
5. **Phase 5**: Full rollout

## Related

- Duolingo Path UI analysis
- Gamification research
- IMPROVEMENTS-2025-001
