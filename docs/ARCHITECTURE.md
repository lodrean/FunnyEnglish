# FunnyEnglish Architecture

## Overview

FunnyEnglish - это кроссплатформенное приложение для изучения английского языка, построенное на современном стеке технологий.

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENTS                                   │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   Android App   │   iOS App       │   Desktop App               │
│   (Compose)     │   (Compose)     │   (Compose)                 │
├─────────────────┴─────────────────┴─────────────────────────────┤
│                    Shared KMP Module                             │
│              (API Client, Models, Platform)                      │
├─────────────────────────────────────────────────────────────────┤
│                    Admin Web (React)                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Backend (Spring Boot)                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │
│  │Controllers│  │Services  │  │Repos     │  │Security (JWT)    │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                           │
└─────────────────────────────────────────────────────────────────┘
```

## Technology Stack

### Backend
| Технология | Версия | Назначение |
|------------|--------|------------|
| Kotlin | 1.9.x | Язык программирования |
| Spring Boot | 3.x | Web framework |
| Spring Security | 6.x | Аутентификация/авторизация |
| Spring Data JPA | 3.x | ORM |
| PostgreSQL | 15+ | База данных |
| Flyway | 9.x | Миграции БД |
| JWT (jjwt) | 0.12.x | Токены авторизации |
| AWS SDK | 2.x | S3 для медиафайлов |

### Mobile (Kotlin Multiplatform)
| Технология | Версия | Назначение |
|------------|--------|------------|
| Kotlin | 1.9.x | Язык программирования |
| Compose Multiplatform | 1.5.x | UI framework |
| Ktor Client | 2.x | HTTP клиент |
| Koin | 3.x | Dependency Injection |
| Kotlinx Serialization | 1.6.x | JSON сериализация |
| Kotlinx Coroutines | 1.7.x | Асинхронность |

### Admin Web
| Технология | Версия | Назначение |
|------------|--------|------------|
| React | 18.x | UI framework |
| TypeScript | 5.x | Типизация |
| Vite | 5.x | Сборщик |
| Material UI | 5.x | UI компоненты |
| TanStack Query | 5.x | Data fetching |
| Zustand | 4.x | State management |
| React Hook Form | 7.x | Формы |
| Axios | 1.x | HTTP клиент |
| Recharts | 2.x | Графики |

## Database Schema

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   users     │     │  categories │     │   tests     │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ id (PK)     │     │ id (PK)     │     │ id (PK)     │
│ email       │     │ name        │     │ category_id │──┐
│ password    │     │ description │     │ title       │  │
│ displayName │     │ icon_url    │     │ description │  │
│ avatar_url  │     │ is_active   │     │ difficulty  │  │
│ level       │     │ display_ord │     │ points      │  │
│ total_points│     └─────────────┘     │ time_limit  │  │
│ streak      │            ▲            │ is_published│  │
│ role        │            │            │ display_ord │  │
│ created_at  │            │            └─────────────┘  │
└─────────────┘            │                   │         │
      │                    └───────────────────┘         │
      │                                                  │
      │    ┌─────────────┐     ┌─────────────┐          │
      │    │  questions  │     │   answers   │          │
      │    ├─────────────┤     ├─────────────┤          │
      │    │ id (PK)     │     │ id (PK)     │          │
      │    │ test_id (FK)│◄────│ question_id │          │
      │    │ type        │     │ text        │          │
      │    │ text        │     │ image_url   │          │
      │    │ audio_url   │     │ audio_url   │          │
      │    │ image_url   │     │ is_correct  │          │
      │    │ points      │     │ match_target│          │
      │    │ display_ord │     │ display_ord │          │
      │    └─────────────┘     └─────────────┘          │
      │                                                  │
      │    ┌─────────────┐     ┌─────────────────┐      │
      │    │  progress   │     │  achievements   │      │
      │    ├─────────────┤     ├─────────────────┤      │
      └───►│ user_id(FK) │     │ id (PK)         │      │
           │ test_id(FK) │◄────│ code            │      │
           │ score       │     │ name            │      │
           │ max_score   │     │ description     │      │
           │ stars       │     │ icon            │      │
           │ best_score  │     │ points_reward   │      │
           │ attempts    │     │ is_visible      │      │
           │ time_spent  │     └─────────────────┘      │
           │ last_attempt│            │                  │
           └─────────────┘            │                  │
                                      ▼                  │
                          ┌─────────────────────┐       │
                          │ user_achievements   │       │
                          ├─────────────────────┤       │
                          │ user_id (FK)        │◄──────┘
                          │ achievement_id (FK) │
                          │ unlocked_at         │
                          └─────────────────────┘
```

## Question Types

| Тип | Описание | Поля |
|-----|----------|------|
| `TEXT_SELECT` | Выбор текстового ответа | question.text, answers[].text |
| `IMAGE_SELECT` | Выбор картинки | question.text, answers[].imageUrl |
| `AUDIO_SELECT` | Выбор после аудио | question.audioUrl, answers[].text |
| `DRAG_DROP_IMAGE` | Сопоставление | answers[].imageUrl, answers[].matchTarget |
| `FILL_BLANK` | Заполнить пропуск | question.text (с _____), answers[].text |

## Authentication Flow

```
┌────────┐     ┌────────┐     ┌────────┐     ┌────────┐
│ Client │     │ Backend│     │JWT Svc │     │Database│
└───┬────┘     └───┬────┘     └───┬────┘     └───┬────┘
    │              │              │              │
    │ POST /login  │              │              │
    │─────────────►│              │              │
    │              │ validate     │              │
    │              │─────────────►│              │
    │              │              │ find user    │
    │              │              │─────────────►│
    │              │              │◄─────────────│
    │              │ generate JWT │              │
    │              │─────────────►│              │
    │              │◄─────────────│              │
    │◄─────────────│              │              │
    │ {token, user}│              │              │
    │              │              │              │
    │ GET /users/me│              │              │
    │ + Bearer token               │              │
    │─────────────►│              │              │
    │              │ validate JWT │              │
    │              │─────────────►│              │
    │              │◄─────────────│              │
    │              │ get user     │              │
    │              │─────────────────────────────►│
    │              │◄─────────────────────────────│
    │◄─────────────│              │              │
    │ {user data}  │              │              │
```

## Mobile App Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         App.kt                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ Navigation   │  │ Theme        │  │ Koin DI              │  │
│  │ (AppScreen)  │  │ (FunnyTheme) │  │ (appModule)          │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Screen     │     │   Screen     │     │   Screen     │
│ (Composable) │     │ (Composable) │     │ (Composable) │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       ▼                    ▼                    ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  ViewModel   │     │  ViewModel   │     │  ViewModel   │
│ (StateFlow)  │     │ (StateFlow)  │     │ (StateFlow)  │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       └────────────────────┼────────────────────┘
                            ▼
                  ┌──────────────────┐
                  │ FunnyEnglishApi  │
                  │   (Ktor HTTP)    │
                  └──────────────────┘
```

### State Management

Каждый ViewModel использует `StateFlow` для управления состоянием:

```kotlin
data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile? = null,
    val categories: List<Category> = emptyList(),
    val recentTests: List<TestListItem> = emptyList()
)

class HomeViewModel(private val api: FunnyEnglishApi) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // Load data...
            _state.update { it.copy(isLoading = false, ...) }
        }
    }
}
```

## Scoring System

### Points Calculation
```
Base Points = test.pointsReward
Star Bonus = stars * 2
Total Points = Base Points + Star Bonus
```

### Stars Calculation
| Процент | Звёзды |
|---------|--------|
| 95%+ | 3 |
| 80-94% | 2 |
| 60-79% | 1 |
| <60% | 0 |

### Level System
```
Level = 1 + floor(totalPoints / 100)
Points to Next Level = 100 - (totalPoints % 100)
```

## Achievements

| Code | Название | Условие |
|------|----------|---------|
| FIRST_TEST | Первые шаги | Пройти 1 тест |
| PERFECT_SCORE | Перфекционист | 100% на любом тесте |
| STREAK_3 | Трёхдневка | 3 дня подряд |
| STREAK_7 | Неделя успеха | 7 дней подряд |
| STREAK_30 | Месяц мастерства | 30 дней подряд |
| TESTS_10 | Десятка | Пройти 10 тестов |
| TESTS_50 | Полтинник | Пройти 50 тестов |

## Security

### JWT Token
- Algorithm: HS256
- Expiration: 24 hours
- Claims: userId, role, exp

### Password Storage
- BCrypt with strength 10

### CORS
- Allowed origins: configured per environment
- Allowed methods: GET, POST, PUT, DELETE
- Credentials: true

### Role-Based Access
| Endpoint | USER | ADMIN |
|----------|------|-------|
| /auth/* | ✓ | ✓ |
| /users/me/* | ✓ | ✓ |
| /categories/* | ✓ | ✓ |
| /tests/* | ✓ | ✓ |
| /leaderboard | ✓ | ✓ |
| /admin/* | ✗ | ✓ |

## Deployment

### Backend
- Docker container
- PostgreSQL database
- S3 for media storage
- Environment variables for configuration

### Admin Web
- Static hosting (Vercel, Netlify, etc.)
- API proxy configuration

### Mobile
- Android: Google Play Store
- iOS: App Store
- Desktop: Direct download
