# CLAUDE.md - FunnyEnglish Project Instructions

## Shared Pipeline

This project uses the shared AIDD pipeline from `../.claude-shared/`

**Key resources:**
- `../.claude-shared/workflow.md` - Development pipeline stages
- `../.claude-shared/templates/` - Document templates (research, plan, PRD, tasklist, QA)
- `../.claude-shared/agents/` - Agent definitions (analyst, developer, reviewer, QA)
- `../.claude-shared/commands/` - Slash commands (/research, /plan, /implement, /review, /qa)

**Project artifacts:**
- `docs/research/` - Research documents
- `docs/plan/` - Implementation plans
- `docs/prd/` - Product requirements
- `docs/tasklist/` - Task lists with acceptance criteria
- `reports/qa/` - QA test reports

## Project Overview

FunnyEnglish is a cross-platform English learning application with gamification. The project consists of:
- **Backend**: Spring Boot 3 + Kotlin + PostgreSQL
- **Mobile/Desktop**: Kotlin Multiplatform + Compose Multiplatform
- **Admin Panel**: React 18 + TypeScript + Material UI

## Quick Start

```bash
# Backend (requires PostgreSQL running on port 5432)
cd backend && ./gradlew bootRun

# Admin Web
cd admin-web && npm install && npm run dev

# Mobile/Desktop
./gradlew :composeApp:run
```

## Project Structure

```
FunnyEnglish/
├── backend/                 # Spring Boot API
│   └── src/main/kotlin/com/funnyenglish/
│       ├── controller/      # REST controllers
│       ├── service/         # Business logic
│       ├── repository/      # JPA repositories
│       ├── entity/          # JPA entities
│       ├── dto/             # Data Transfer Objects
│       └── security/        # JWT authentication
├── admin-web/               # React Admin Panel
│   └── src/
│       ├── pages/           # Page components
│       ├── components/      # Reusable UI components
│       ├── api/             # Axios client
│       └── store/           # Zustand stores
├── composeApp/              # Compose Multiplatform UI
│   └── src/commonMain/kotlin/com/funnyenglish/app/
│       ├── screens/         # UI screens
│       ├── viewmodel/       # ViewModels (StateFlow)
│       ├── theme/           # Material 3 themes
│       ├── di/              # Koin DI modules
│       └── components/      # Shared components
├── shared/                  # KMP Shared Module
│   └── src/commonMain/kotlin/com/funnyenglish/shared/
│       ├── api/             # Ktor HTTP client
│       ├── model/           # Data models
│       └── platform/        # Platform-specific code
└── docs/                    # Documentation
    ├── API.md               # REST API docs
    └── ARCHITECTURE.md      # System architecture
```

## Coding Conventions

### Kotlin (Backend & Mobile)
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use `camelCase` for functions/variables, `PascalCase` for classes
- Composable functions start with uppercase: `@Composable fun MyScreen()`
- Use state hoisting pattern in Compose
- Use `StateFlow` for ViewModel state management

### TypeScript/React (Admin Web)
- Use functional components with hooks
- TypeScript strict mode enabled
- Use TanStack Query for data fetching
- Use Zustand for global state

### Error Handling
- Backend: Use proper HTTP status codes and error DTOs
- Mobile: Catch exceptions in ViewModel, expose via StateFlow
- Admin: Use TanStack Query error handling

### Tests
- Backend: JUnit 5 + MockK
- Mobile: commonTest with Kotlin test framework
- Admin: Jest + React Testing Library

## Before Modifying Code

1. **Check existing patterns**: Read similar files to understand conventions
2. **Review documentation**: Check `docs/API.md` for API contracts
3. **Understand dependencies**: Check `build.gradle.kts` or `package.json`
4. **Check PRD/Plan**: If working on a ticket, review `docs/prd/<ticket>.prd.md`

## After Modifying Code

1. **Verify compilation**:
   - Backend: `cd backend && ./gradlew build`
   - Mobile: `./gradlew :composeApp:build`
   - Admin: `cd admin-web && npm run build`

2. **Run tests**:
   - Backend: `./gradlew :backend:test`
   - Mobile: `./gradlew :shared:allTests`

3. **Update documentation** if API changed:
   - Update `docs/API.md`
   - Update DTOs in `shared/model/`
   - Update `admin-web/src/api/client.ts`

## Git Workflow

### Branches
- `main` - production-ready code
- `develop` - current development
- `feature/*` - new features
- `fix/*` - bug fixes

### Commit Format
```
type(scope): description

Types: feat, fix, refactor, docs, test, chore
Scopes: backend, mobile, admin, shared, docs
```

### Feature Development
```bash
git checkout develop
git pull origin develop
git checkout -b feature/my-feature
# ... work ...
git commit -m "feat(scope): description"
git checkout develop
git merge feature/my-feature
```

## Key Files Reference

### Backend
- `SecurityConfig.kt` - CORS and auth configuration
- `JwtAuthenticationFilter.kt` - JWT token validation
- `AdminController.kt` - Admin-only endpoints

### Mobile
- `App.kt` - Navigation and main app structure
- `AppModule.kt` - Koin DI setup
- `Theme.kt` - Material 3 theme configuration
- `FunnyEnglishApi.kt` - API client in shared module

### Admin Web
- `client.ts` - Axios API client with interceptors
- `authStore.ts` - Authentication state
- `Layout.tsx` - Main layout with navigation

## Environment Variables

### Backend (required)
```
DATABASE_URL=jdbc:postgresql://localhost:5432/funnyenglish
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=your-secret-key-minimum-32-characters
ADMIN_EMAIL=admin@funnyenglish.app
ADMIN_PASSWORD=admin123
```

### Mobile
```
# gradle.properties
FUNNYENGLISH_API_BASE_URL=http://10.0.2.2:8080
```

## Common Tasks

### Add New Screen (Mobile)
1. Create `NewScreen.kt` in `screens/`
2. Create `NewViewModel.kt` in `viewmodel/` if needed
3. Register ViewModel in `di/AppModule.kt`
4. Add route in `App.kt` (sealed class AppScreen)
5. Add navigation in `MainAppContent`

### Add New Endpoint (Backend)
1. Create/update DTO in `dto/`
2. Add method in Service
3. Add endpoint in Controller
4. Update `SecurityConfig` if special permissions needed
5. Update `docs/API.md`

### Add New Admin Page
1. Create page component in `pages/`
2. Add route in `App.tsx`
3. Add navigation item in `Layout.tsx`
4. Create API functions in `api/client.ts`

## Troubleshooting

### Backend won't start
- Check PostgreSQL is running: `docker ps` or `pg_isready`
- Check environment variables are set
- Check port 8080 is free

### Mobile app can't connect
- Check backend is running on correct port
- For emulator: use `10.0.2.2` instead of `localhost`
- For physical device: use computer's local IP

### Admin build fails
- Delete `node_modules` and reinstall: `rm -rf node_modules && npm install`
- Check Node.js version: requires 18+
