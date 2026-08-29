# 02-execute — bd FunnyEnglish-wy7.1: BE: отключить OSIV + транзакции на read-методах

## Что сделано

### 1. OSIV отключён (3 профиля)
`spring.jpa.open-in-view: false` добавлен в `application.yml`, `application-test.yml`,
`application-integration-test.yml`. Тестовые профили — обязательно: иначе H2-гейт
маскировал бы LazyInitializationException, которая в prod стала бы 500.
Дополнительно `hibernate.default_batch_fetch_size: 16` (все 3 профиля) — батчевая
догрузка lazy-коллекций для пейджированных списков, где join-fetch коллекции
невозможен (Page + collection fetch = in-memory пагинация HHH90003004).
YAML проверен `yaml.safe_load` — валиден, дублей `spring:` нет.

### 2. @Transactional(readOnly = true) на read-методах (ранее без транзакций — после
отключения OSIV они падали бы с LazyInitializationException):
- **TestService**: getCategories, getTestsByCategory, getAllTests, getTestById, getTestByIdForAdmin
- **ProgressService**: getUserProgress, getUserProgressSummary
- **StudentGroupService**: getTeacherGroups, getGroupDetail, getPendingRequests, getStudentGroups, getStudentGroupDetail, getGroupProgress, getStudentProgress
- **PublicAdaptiveService**: createRandomLesson, validateAnswer (+ import)
- **AudioTestService**: getPublishedAudioTests, getPublishedAudioTestById, getUserProgress, getUserProgressForTest, getAllAudioTests, getAudioTestById
- **AuthService**: login, refreshToken
- **AdminService**: getAnalytics, getDailyActivity, getLevelDistribution, getPopularTests, getGuestAnalytics, getRecentActivity (+ import)
- **UserService**: getUserById, getAllUsers, getUserStats, getUserProfile, getLeaderboard
- **AchievementService**: getAllAchievements, getUserAchievements, getAchievementDetail, getAchievementStats (+ import)
- **TestValidationService**: validateTest (+ import)
- **QuestionService**: getPublishedQuestionsByTest, validateImageWordMatchAnswer

### 3. @Transactional на write-методах, живших без транзакции (per-call tx репозиториев):
- **AchievementService**: checkAchievements, checkAndAwardAchievements, updateProgress
- **QuestService**: getDailyQuests, getWeeklyQuests (read+save ленивой генерации), claimReward, updateQuestProgress (+ import)
- **StreakService**: getStreakData (read+save в getOrCreate), recordActivity, useStreakFreeze, recoverStreak (+ import)

### 4. Побочный фикс по ходу
**AdaptiveLessonService.getNextQuestion**: был `@Transactional(readOnly = true)`, но
мутировал `lesson.status` + save → в readOnly-tx изменение молча терялось
(FlushMode.MANUAL). Переведён на `@Transactional` (read-write).

### 5. Join-fetch / EntityGraph против N+1:
- **TestRepository**: `@EntityGraph(["category","questions"])` на findByCategoryIdAndIsPublishedTrueOrderByDisplayOrder и findByIsPublishedTrueOrderByDisplayOrder (toListResponse читает questions.size/category)
- **CategoryRepository**: `@EntityGraph(["tests"])` на findByIsActiveTrueOrderByDisplayOrder (testsCount)
- **ProgressRepository**: `@EntityGraph(["test","test.category"])` на findByUserId (test.title, category.name)
- **StudentGroupRepository**: `@EntityGraph(["teacher"])` на findByTeacherId; GroupMemberRepository — `@EntityGraph(["user"])` на findByGroupId, `@EntityGraph(["group","group.teacher"])` на findByUserId
- **MessageRepository**: `@EntityGraph(["sender","recipient"])` на findByRecipientIdOrderByCreatedAtDesc
- **AudioTestRepository**: +`LEFT JOIN FETCH at.category` в findPublishedByIdWithDetails; `@EntityGraph(["category"])` + override findAll(Pageable) для admin-списка
- **AudioTestProgressRepository**: +`LEFT JOIN FETCH p.audioTest` в findByUserIdAndAudioTestId (playsLimit)
- **MessageController** (прямой доступ к репозиторию из контроллера): `@Transactional(readOnly=true)` на getMessagesForUser/getInbox, `@Transactional` на markAsRead (+ import)
- **AdminLogController.getLogs**: `@Transactional(readOnly = true)`

Намеренно НЕ тронуто: `TestRepository.findByIdWithQuestions` (answers — второй bag,
MultipleBagFetchException; догружается явным циклом в сервисе внутри tx + batch fetch),
`CategoryRepository.findAllWithPublishedTests` (мёртвый код с фильтрующей семантикой),
speaking-пакет (уже был полностью аннотирован и зафетчен).

## Изменённые файлы (20)
- backend/src/main/resources/application.yml
- backend/src/main/resources/application-test.yml
- backend/src/main/resources/application-integration-test.yml
- backend/.../service/TestService.kt, ProgressService.kt, StudentGroupService.kt,
  PublicAdaptiveService.kt, AuthService.kt, AdminService.kt, UserService.kt,
  AchievementService.kt, QuestService.kt, StreakService.kt, TestValidationService.kt,
  QuestionService.kt, AdaptiveLessonService.kt, audio/AudioTestService.kt
- backend/.../repository/TestRepository.kt, CategoryRepository.kt, ProgressRepository.kt,
  StudentGroupRepository.kt, MessageRepository.kt, audio/AudioTestRepository.kt,
  audio/AudioTestProgressRepository.kt
- backend/.../controller/MessageController.kt, ClientLogController.kt
- memory.md (запись в «Решения и договорённости», правило AGENTS.md №2)

## Как проверить
1. `.\gradlew.bat :backend:test` — гейт драйвера (H2 test-profile, open-in-view=false).
2. Живой прогон против PostgreSQL: основные GET-эндпоинты (/categories, /tests,
   /progress, /groups, /audio-tests, /users/me/messages, /admin/analytics) — не должно
   быть 500 LazyInitializationException.
3. Риск-зона: если какой-то всплывший LazyInitializationException укажет на непокрытый
   путь — добавить `@Transactional(readOnly = true)` на соответствующий сервисный метод
   (правило зафиксировано в memory.md).

Сборки/тесты сам не запускал (гейт — драйвер). Спеки/PRD не тронуты.
