# So to Speak Coding Conventions

## General Principles

1. **Simplicity over cleverness**: Write clear, readable code
2. **Consistency**: Follow existing patterns in the codebase
3. **Single responsibility**: One class/function does one thing
4. **DRY but pragmatic**: Don't repeat, but don't over-abstract

## Kotlin (Backend & Mobile)

### Naming
```kotlin
// Classes - PascalCase
class UserService
data class UserDto
sealed class AppScreen

// Functions - camelCase
fun loadUserProfile()
suspend fun fetchCategories()

// Variables - camelCase
val userRepository: UserRepository
var isLoading = false

// Constants - SCREAMING_SNAKE_CASE
const val MAX_RETRY_COUNT = 3
val DEFAULT_TIMEOUT = 30_000L

// Composables - PascalCase (like classes)
@Composable
fun HomeScreen()

@Composable
fun LoadingButton()
```

### File Structure
```kotlin
// 1. Package declaration
package com.sotospeak.service

// 2. Imports (sorted, no wildcards)
import com.sotospeak.dto.UserDto
import com.sotospeak.repository.UserRepository
import org.springframework.stereotype.Service

// 3. Class declaration
@Service
class UserService(
    private val userRepository: UserRepository
) {
    // 4. Public methods first
    fun getUser(id: Long): UserDto { ... }

    // 5. Private methods last
    private fun mapToDto(user: User): UserDto { ... }
}
```

### Spring Boot Specifics

```kotlin
// Controllers
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserDto> =
        ResponseEntity.ok(userService.getUser(id))
}

// Services - use constructor injection
@Service
class UserService(
    private val userRepository: UserRepository,
    private val mapper: UserMapper
)

// DTOs - use data classes
data class UserDto(
    val id: Long,
    val email: String,
    val displayName: String
)

// Entities - use data classes with JPA annotations
@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val email: String,
    val displayName: String
)
```

### Compose Multiplatform Specifics

```kotlin
// Screen structure
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigate: (AppScreen) -> Unit
) {
    val state by viewModel.state.collectAsState()

    HomeScreenContent(
        state = state,
        onNavigate = onNavigate,
        onRefresh = viewModel::refresh
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeState,
    onNavigate: (AppScreen) -> Unit,
    onRefresh: () -> Unit
) {
    // UI implementation
}

// ViewModel pattern
class HomeViewModel(
    private val api: SoToSpeakApi
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val data = api.fetchData()
                _state.update { it.copy(data = data, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}

// State class
data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: List<Item> = emptyList()
)
```

### Error Handling

```kotlin
// Backend - use specific exceptions
class UserNotFoundException(userId: Long) :
    RuntimeException("User not found: $userId")

// Global exception handler
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFound(ex: UserNotFoundException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorDto(ex.message ?: "Not found"))
}

// Mobile - handle in ViewModel
viewModelScope.launch {
    try {
        val result = api.fetchData()
        _state.update { it.copy(data = result) }
    } catch (e: Exception) {
        _state.update { it.copy(error = e.message) }
    }
}
```

## TypeScript/React (Admin Web)

### Naming
```typescript
// Components - PascalCase
function UserList() { ... }
const DashboardPage: React.FC = () => { ... }

// Hooks - camelCase starting with 'use'
function useAuth() { ... }
function useUsers() { ... }

// Variables/functions - camelCase
const userCount = 10;
function handleSubmit() { ... }

// Constants - SCREAMING_SNAKE_CASE
const MAX_PAGE_SIZE = 100;
const API_BASE_URL = '/api';

// Types/Interfaces - PascalCase
interface User { ... }
type UserRole = 'USER' | 'ADMIN';
```

### Component Structure
```typescript
// 1. Imports
import React, { useState, useEffect } from 'react';
import { Box, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';

// 2. Types
interface UserListProps {
  onSelect: (user: User) => void;
}

// 3. Component
export function UserList({ onSelect }: UserListProps) {
  // Hooks first
  const [filter, setFilter] = useState('');
  const { data, isLoading } = useUsers();

  // Effects
  useEffect(() => {
    // ...
  }, []);

  // Handlers
  const handleClick = (user: User) => {
    onSelect(user);
  };

  // Early returns for loading/error
  if (isLoading) return <CircularProgress />;

  // Main render
  return (
    <Box>
      {data?.map(user => (
        <UserCard key={user.id} user={user} onClick={handleClick} />
      ))}
    </Box>
  );
}
```

### API Calls
```typescript
// Use TanStack Query for data fetching
export function useUsers() {
  return useQuery({
    queryKey: ['users'],
    queryFn: () => apiClient.get<User[]>('/admin/users').then(r => r.data),
  });
}

export function useCreateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateUserDto) => apiClient.post('/admin/users', data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
  });
}

// API client with interceptors
const apiClient = axios.create({ baseURL: '/api' });

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

### State Management
```typescript
// Use Zustand for global state
interface AuthState {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: localStorage.getItem('token'),

  login: async (email, password) => {
    const { token, user } = await authApi.login(email, password);
    localStorage.setItem('token', token);
    set({ token, user });
  },

  logout: () => {
    localStorage.removeItem('token');
    set({ token: null, user: null });
  },
}));
```

## SQL & Database

### Naming
```sql
-- Tables - plural, snake_case
CREATE TABLE users ( ... );
CREATE TABLE test_questions ( ... );

-- Columns - snake_case
user_id, display_name, created_at

-- Foreign keys - referenced_table_singular_id
category_id, user_id

-- Indexes - idx_table_column
CREATE INDEX idx_users_email ON users(email);

-- Constraints - type_table_column
CONSTRAINT pk_users PRIMARY KEY (id)
CONSTRAINT fk_tests_category FOREIGN KEY (category_id)
CONSTRAINT uq_users_email UNIQUE (email)
```

### Migrations (Flyway)
```sql
-- V1__create_users.sql
-- V2__add_achievements.sql
-- V3__add_user_stats.sql

-- Always include IF NOT EXISTS for safety
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Git

### Branch Naming
```
feature/add-user-authentication
fix/jwt-null-pointer
refactor/user-service-cleanup
docs/api-documentation
```

### Commit Messages
```
feat(backend): add JWT authentication endpoint
fix(mobile): resolve crash on empty list
refactor(admin): extract user form component
docs: update API documentation
test(backend): add UserService unit tests
chore: update dependencies
```

### Pull Request Title
```
feat(mobile): Add bottom navigation with Home, Categories, Profile tabs
```

## Testing

### Unit Tests (Kotlin)
```kotlin
class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val userService = UserService(userRepository)

    @Test
    fun `getUser returns user when exists`() {
        // Given
        val user = User(id = 1, email = "test@test.com")
        every { userRepository.findById(1) } returns Optional.of(user)

        // When
        val result = userService.getUser(1)

        // Then
        assertEquals("test@test.com", result.email)
    }

    @Test
    fun `getUser throws when not found`() {
        every { userRepository.findById(any()) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            userService.getUser(999)
        }
    }
}
```

### Component Tests (React)
```typescript
describe('UserList', () => {
  it('renders users', async () => {
    const users = [{ id: 1, email: 'test@test.com' }];
    render(<UserList users={users} />);

    expect(screen.getByText('test@test.com')).toBeInTheDocument();
  });

  it('calls onSelect when clicked', async () => {
    const onSelect = jest.fn();
    render(<UserList users={[mockUser]} onSelect={onSelect} />);

    await userEvent.click(screen.getByRole('button'));

    expect(onSelect).toHaveBeenCalledWith(mockUser);
  });
});
```

## Security

### Backend
- Never log sensitive data (passwords, tokens)
- Use parameterized queries (JPA handles this)
- Validate all input at controller level
- Use BCrypt for passwords (strength 10+)
- JWT tokens expire in 24 hours

### Frontend
- Never store sensitive data in localStorage except tokens
- Sanitize user input before display
- Use HTTPS in production
- Implement CSRF protection

### Secrets
- Never commit secrets to git
- Use environment variables
- Add `.env` to `.gitignore`

## AI-Driven Development (AIDD) Conventions

### Overview

This project follows the AIDD methodology where LLM acts as a team of specialized roles rather than a single "magic brain". Each role has specific responsibilities and outputs.

### Quality Gates

Every feature must pass through 9 quality gates:

| Gate | Name | Artifact | Validator |
|------|------|----------|-----------|
| 1 | IDEA_CAPTURED | Problem statement | Product Manager |
| 2 | RESEARCH_COMPLETE | `docs/research/<ticket>.md` | Analyst |
| 3 | PLAN_APPROVED | `docs/plan/<ticket>.md` | Architect |
| 4 | PRD_READY | `docs/prd/<ticket>.prd.md` | Analyst |
| 5 | TASKLIST_READY | `docs/tasklist/<ticket>.md` | Developer |
| 6 | IMPLEMENT_COMPLETE | Code changes | Developer |
| 7 | REVIEW_OK | Review approval | Reviewer |
| 8 | QA_PASS | `reports/qa/<ticket>.md` | QA |
| 9 | DOCS_UPDATED | Updated docs | Tech Writer |

### Document Standards

#### Research Document
Location: `docs/research/<ticket>.md`

```markdown
# Research: <Title>

## Ticket
<ticket-id>

## Objective
What we're investigating

## Affected Areas
- File 1: reason
- File 2: reason

## Existing Patterns
How similar features are implemented

## Complexity Assessment
- Scope: Low/Medium/High
- Risk areas: list

## Open Questions
- [ ] Question 1 (Status: OPEN/RESOLVED)

## Recommendation
Proposed approach
```

#### Plan Document
Location: `docs/plan/<ticket>.md`

```markdown
# Plan: <Title>

## Ticket
<ticket-id>

## Status
DRAFT | REVIEW | APPROVED

## Approach
High-level description

## Architecture Decisions
### Decision 1
- Context: situation
- Decision: what we decided
- Consequences: tradeoffs

## Implementation Steps
1. Step 1
2. Step 2

## Risks
| Risk | Mitigation |
|------|------------|
| Risk 1 | How to handle |

## Dependencies
- External dependency
```

#### PRD Document
Location: `docs/prd/<ticket>.prd.md`

```markdown
# PRD: <Feature Name>

## Ticket
<ticket-id>

## Status
DRAFT | REVIEW | READY

## Context
Why this feature is needed

## Goals
1. Goal 1
2. Goal 2

## Success Metrics
- Metric 1: target value

## User Stories
### Story 1
As a <user>, I want <action> so that <benefit>

**Acceptance Criteria:**
- [ ] Criterion 1
- [ ] Criterion 2

## Out of Scope
- Feature X

## Open Questions
- [ ] Question 1 (Status: OPEN)
```

#### Tasklist Document
Location: `docs/tasklist/<ticket>.md`

```markdown
# Tasklist: <Title>

## Ticket
<ticket-id>

## Status
IN_PROGRESS | BLOCKED | COMPLETE

## Tasks

### Backend
- [ ] Task 1
  - AC: Test passes
  - AC: Endpoint returns 200

### Mobile
- [ ] Task 3
  - AC: Screen renders correctly

## Blockers
Any blocking issues

## Notes
Additional context
```

#### QA Report
Location: `reports/qa/<ticket>.md`

```markdown
# QA Report: <Title>

## Ticket
<ticket-id>

## Test Date
YYYY-MM-DD

## Summary
PASS | FAIL | PARTIAL

## Automated Tests
- Backend: X/Y passed
- Mobile: X/Y passed

## Acceptance Criteria Verification
- [ ] Criterion 1: PASS/FAIL

## Edge Cases Tested
- [ ] Edge case 1: PASS/FAIL

## Issues Found
1. Issue description (Severity: Low/Medium/High)

## Sign-off
- [ ] All criteria verified
- [ ] No critical issues
- [ ] Ready for release
```

### Working with Subagents

Use subagents for specific tasks to keep main context clean:

```python
# Research and analysis
Task(subagent_name="analyst",
     description="Analyze requirements",
     prompt="Analyze the requirements for...")

# Architecture design
Task(subagent_name="architect",
     description="Design architecture",
     prompt="Design the architecture for...")

# Implementation
Task(subagent_name="developer",
     description="Implement feature",
     prompt="Implement the feature following...")

# Code review
Task(subagent_name="reviewer",
     description="Review code",
     prompt="Review this code for...")

# Testing
Task(subagent_name="qa",
     description="Test implementation",
     prompt="Test this feature and...")
```

### Commit Message Convention

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

Types:
- `feat` - New feature
- `fix` - Bug fix
- `refactor` - Code restructuring
- `docs` - Documentation only
- `test` - Adding tests
- `chore` - Maintenance tasks

Scopes:
- `backend` - Spring Boot backend
- `mobile` - Compose Multiplatform app
- `admin` - React admin panel
- `shared` - KMP shared module
- `docs` - Documentation

Examples:
```
feat(backend): add JWT refresh token endpoint
fix(mobile): resolve crash on empty category list
refactor(admin): extract user form component
docs: update API documentation for v2 endpoints
```

### Before Starting Work

1. Check active tasklist: `docs/tasklist/<ticket>.md`
2. Review PRD: `docs/prd/<ticket>.prd.md`
3. Review Plan: `docs/plan/<ticket>.md`
4. Understand conventions: `conventions.md`

### After Completing Work

1. Verify code compiles
2. Run tests
3. Update tasklist (mark tasks complete)
4. Update documentation if API changed
5. Commit with conventional message
6. Request review if needed

### Prohibited Practices

- ❌ "Vibe coding" - generating code without following AIDD process
- ❌ Skipping quality gates
- ❌ Large commits (>1 day of work)
- ❌ TODOs left in committed code
- ❌ Hardcoded secrets
- ❌ Breaking changes without documentation

### Required Practices

- ✅ One task at a time
- ✅ Clear acceptance criteria for each task
- ✅ Incremental commits
- ✅ Documentation as source of truth
- ✅ Code review before merge
- ✅ QA verification before release
