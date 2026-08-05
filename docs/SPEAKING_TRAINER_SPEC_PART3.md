# Speaking Trainer — Техническая спецификация (Part 3: Admin-web)

> **Ticket**: SPEAKING-TRAINER-001
> **Статус**: Draft
> **Version**: 1.2 (2026-08-02: добавлен §2.5 «Переключатель темы» и E2E-проверка; v1.1 — контрактная адаптация к реализованному backend — §3.4 «Адаптер speakingApi.ts»; v1.0 — первоначальная спека)
> **Дата**: 2026-08-02
> **Связанные документы**:
> - PRD: `docs/prd/SPEAKING-TRAINER-001.prd.md` (Story 6 — администрирование контента, Story 7 — Grading)
> - Backend: `docs/SPEAKING_TRAINER_SPEC_PART1.md` (эндпоинты, сущности, Flyway V17+)
> - Mobile: `docs/SPEAKING_TRAINER_SPEC_PART2.md`
> - Дизайн-система: `.docs/design-system/` (tokens.json — палитра для MUI-темы; mockups.html — эталон Grading detail со слайдерами рубрики)
>
> Документ написан под реализацию AI-агентом: все пути файлов, имена компонентов и testid — конкретные и проверены по кодовой базе `admin-web/`.

## 📑 Table of Contents

1. [Scope и обзор](#1-scope-и-обзор)
2. [Навигация и роутинг](#2-навигация-и-роутинг)
3. [API-клиент: типы и методы](#3-api-клиент-типы-и-методы)
4. [Раздел «Speaking» — управление контентом](#4-раздел-speaking--управление-контентом)
5. [Раздел «Grading» — проверка записей](#5-раздел-grading--проверка-записей)
6. [UX-детали: empty/loading/error, аудио-плеер](#6-ux-детали)
7. [Тестирование: vitest + Playwright](#7-тестирование)
8. [Разбивка на задачи (для bd)](#8-разбивка-на-задачи)
9. [File Checklist](#9-file-checklist)
10. [Open Questions](#10-open-questions)

---

## 1. Scope и обзор

### 1.1 Что делаем

В админ-панель (`admin-web/`, React 18 + TS + MUI 6 + TanStack Query 5 + react-router-dom 6, Vite 5) добавляются **два новых раздела**:

| Раздел | Назначение | PRD Story |
|---|---|---|
| **Speaking** | CRUD контента: Libraries → Topics (видео + субтитры WebVTT) → Questions | Story 6 |
| **Grading** | Inbox practice-записей учеников: прослушивание + оценка по рубрике | Story 7 |

### 1.2 Ключевые решения (зафиксированы в PRD 2026-07-30)

- Оценивание — **рубрика**: `grammar`, `vocabulary`, `pronunciation`, `fluency` (каждый 1–10) + **общий балл = авто-усреднение** (1 знак после запятой) + текстовый комментарий.
- Видео и субтитры хостятся в **MinIO**, загрузка — через существующий `POST /api/admin/media/upload` (переиспользуем `MediaUploader`).
- Статусы submission: `NEW` → `REVIEWED`. Статус меняется **только на backend** при сохранении grade — фронт статус напрямую не патчит. Оценку можно редактировать (аудит по `updatedAt`).
- Удаление топика с записями — **soft delete** (архив), записи остаются доступны в Grading.

### 1.3 Важно: реальная структура admin-web

В кодовой базе страницы лежат в **`src/screens/`** (не `src/pages/`): `Dashboard.tsx`, `Categories.tsx`, `Tests.tsx`, `TestEditor.tsx`, `Users.tsx`, `Analytics.tsx`, `Settings.tsx`, `Login.tsx` + `index.ts` (re-export). Новые экраны кладём туда же.

Существующие переиспользуемые строительные блоки (НЕ создавать дубли):

| Блок | Путь | Назначение |
|---|---|---|
| Axios-инстанс + auth | `src/api/client.ts` | `api` с интерцептором токена из `localStorage`, редирект на `/login` при 401. baseURL = `VITE_API_URL` или `/api` |
| Медиа-загрузка | `src/components/MediaUploader.tsx` | dropzone + preview + delete, поверх `uploadMedia`/`deleteMedia` |
| Навигация | `src/components/navigation/navItems.ts` | конфиг сайдбара; Sidebar рендерит `data-testid={nav-${item.id}}` |
| Layout | `src/components/layout/AdminLayout.tsx` (+ `Sidebar.tsx`, `Header.tsx`, `Breadcrumbs.tsx`) | обёртка защищённых роутов |
| Feedback | `src/components/feedback/`: `ConfirmDialog`, `EmptyState`, `PageLoader`, `SkeletonCard`, `ToastProvider` | готовые состояния UI |
| Forms | `src/components/forms/`: `FormField`, `FormActions`, `SearchInput` | поля форм |
| Хуки | `src/hooks/`: `useTable` (клиентская сортировка/пагинация), `useConfirm`, `useToast` | логика таблиц/диалогов |
| Ошибки | `src/components/ErrorDisplay.tsx` | отображение ошибок запросов |
| Роут-валидатор | `src/components/navigation/RouteValidator.tsx` | dev-проверка; **обязательно дополнить `VALID_ROUTES`** |

Грабля №18 memory.md: backend сериализует Boolean как `isPublished`/`isArchived` (jackson-module-kotlin) — в TS-типах использовать именно `isPublished`, `isArchived`.

---

## 2. Навигация и роутинг

### 2.1 Изменения в `src/components/navigation/navItems.ts`

Добавить два пункта верхнего уровня (после `content`, перед `users`):

```ts
import {
  // ...существующие
  RecordVoiceOver as RecordVoiceOverIcon,
  RateReview as RateReviewIcon,
  MenuBook as MenuBookIcon,
  OndemandVideo as OndemandVideoIcon,
} from '@mui/icons-material';

// Внутри navItems, между блоками content и users:
{
  id: 'speaking',
  label: 'Speaking',
  path: '/speaking',
  icon: RecordVoiceOverIcon,
  children: [
    {
      id: 'speaking-libraries',
      label: 'Libraries',
      path: '/speaking/libraries',
      icon: MenuBookIcon,
    },
    {
      id: 'speaking-topics',
      label: 'Topics',
      path: '/speaking/topics',
      icon: OndemandVideoIcon,
    },
  ],
},
{
  id: 'grading',
  label: 'Grading',
  path: '/grading',
  icon: RateReviewIcon,
  // badge: количество NEW — опционально, см. §5.3
},
```

Sidebar автоматически выдаст `data-testid="nav-speaking"`, `data-testid="nav-speaking-libraries"`, `data-testid="nav-speaking-topics"`, `data-testid="nav-grading"`. Хелперы `getFlattenedNavItems`, `getBreadcrumbPath`, `isNavItemActive` подхватят новые пункты без изменений.

### 2.2 Роуты в `src/App.tsx`

Внутрь защищённого `<Route path="/" element={<ProtectedRoute><AdminLayout /></ProtectedRoute>}>` добавить:

```tsx
{/* Speaking Content */}
<Route path="speaking">
  <Route index element={<Navigate to="/speaking/libraries" replace />} />
  <Route path="libraries" element={<SpeakingLibraries />} />
  <Route path="libraries/new" element={<SpeakingLibraryEditor />} />
  <Route path="libraries/:id/edit" element={<SpeakingLibraryEditor />} />
  <Route path="topics" element={<SpeakingTopics />} />
  <Route path="topics/new" element={<SpeakingTopicEditor />} />
  <Route path="topics/:id/edit" element={<SpeakingTopicEditor />} />
</Route>

{/* Grading */}
<Route path="grading">
  <Route index element={<GradingInbox />} />
  <Route path="submissions/:id" element={<GradingDetail />} />
</Route>
```

Импорты — из `src/screens/index.ts` (дополнить re-export'ы, как существующие `Dashboard, Categories, ...`).

### 2.3 `RouteValidator.tsx`

Дополнить `VALID_ROUTES`:

```ts
'/speaking/libraries',
'/speaking/libraries/new',
'/speaking/libraries/:id/edit',
'/speaking/topics',
'/speaking/topics/new',
'/speaking/topics/:id/edit',
'/grading',
'/grading/submissions/:id',
```

### 2.4 Таблица маршрутов

| URL | Экран | Описание |
|---|---|---|
| `/speaking/libraries` | `SpeakingLibraries` | Список тем (таблица, поиск, publish toggle) |
| `/speaking/libraries/new` | `SpeakingLibraryEditor` | Создание темы |
| `/speaking/libraries/:id/edit` | `SpeakingLibraryEditor` | Редактирование темы |
| `/speaking/topics` | `SpeakingTopics` | Список топиков (фильтр по library) |
| `/speaking/topics/new?libraryId=` | `SpeakingTopicEditor` | Создание топика (+ вкладка вопросов после сохранения) |
| `/speaking/topics/:id/edit` | `SpeakingTopicEditor` | Редактирование топика + вопросы |
| `/grading` | `GradingInbox` | Inbox записей с фильтрами |
| `/grading/submissions/:id` | `GradingDetail` | Плеер + вопросы + рубрика |

### 2.5 Переключатель темы

- Расположение: правая часть `Header.tsx` рядом с уведомлениями.
- Компонент: `IconButton` с `data-testid="theme-toggle-button"`.
- Иконка: `DarkModeIcon` в светлом режиме, `LightModeIcon` в тёмном (`isDarkMode ? <LightModeIcon /> : <DarkModeIcon />`).
- Хук: `const { toggleTheme, isDarkMode } = useTheme()` из `src/theme/ThemeProvider.tsx`.
- Поведение: клик переключает `mode` (`light` ↔ `dark`), сохраняет `sotospeak-theme-mode` в `localStorage`, применяет MUI-тему.
- Default: системное предпочтение (`prefers-color-scheme`), если значение в localStorage отсутствует.
- E2E: `e2e/tests/theme-toggle.spec.ts` — проверяет flip `localStorage` и смену `aria-label` кнопки.

---

## 3. API-клиент: типы и методы

### 3.1 Новый файл `src/api/speakingApi.ts`

Паттерн — как `src/api/audioTestApi.ts`: `import api from './client';` (инстанс уже с токеном и обработкой 401). **Не** добавлять всё в `client.ts` — он уже перегружен; отдельный модуль чище. Re-export в `src/api/index.ts`.

Все пути относительно baseURL `/api` (context-path backend, грабля №9 memory.md). Эндпоинты соответствуют Part 1 (`docs/SPEAKING_TRAINER_SPEC_PART1.md`); если при интеграции имена полей разойдутся — правим здесь, а не в компонентах.

### 3.2 TypeScript-типы

```ts
// ==================== Speaking Libraries ====================

export interface SpeakingLibrary {
  id: string;
  name: string;
  description?: string;
  coverUrl?: string;
  displayOrder: number;
  isPublished: boolean;
  topicsCount: number;       // агрегат с backend
  createdAt: string;
  updatedAt: string;
}

export interface CreateLibraryRequest {
  name: string;
  description?: string;
  coverUrl?: string;
  displayOrder: number;
  isPublished: boolean;
}

export type UpdateLibraryRequest = Partial<CreateLibraryRequest>;

// ==================== Speaking Topics ====================

export interface SpeakingTopic {
  id: string;
  libraryId: string;
  libraryName?: string;      // для таблицы без join на клиенте
  name: string;
  videoUrl: string;
  subtitlesUrl?: string;     // WebVTT
  durationSeconds?: number;
  displayOrder: number;
  isPublished: boolean;
  isArchived: boolean;       // soft delete (PRD Edge Case)
  questionsCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTopicRequest {
  libraryId: string;
  name: string;
  videoUrl: string;
  subtitlesUrl?: string;
  durationSeconds?: number;
  displayOrder: number;
  isPublished: boolean;
}

export type UpdateTopicRequest = Partial<Omit<CreateTopicRequest, 'libraryId'>>;

// ==================== Speaking Questions ====================

export interface SpeakingQuestion {
  id: string;
  topicId: string;
  text: string;              // английский текст вопроса
  displayOrder: number;
}

export interface CreateSpeakingQuestionRequest {
  text: string;
  displayOrder: number;
}

export interface ReorderSpeakingQuestionsRequest {
  questionIds: string[];     // полный упорядоченный список id
}

// ==================== Grading (Submissions) ====================

export type SubmissionStatus = 'NEW' | 'REVIEWED';

export interface Grade {
  grammar: number;           // 1..10
  vocabulary: number;        // 1..10
  pronunciation: number;     // 1..10
  fluency: number;           // 1..10
  totalScore: number;        // среднее, вычисляет backend; на клиенте — превью
  comment?: string;
  gradedAt: string;
  updatedAt: string;
}

export interface SpeakingSubmission {
  id: string;
  student: {
    id: string;
    name: string;
    email: string;
  };
  topic: {
    id: string;
    name: string;
    libraryName: string;
    isArchived?: boolean;    // soft-deleted топик — показывать суффикс «(archived)»
  };
  audioUrl: string;          // публичный MinIO URL (S3_PUBLIC_URL, BUG-004)
  durationSeconds: number;
  status: SubmissionStatus;
  submittedAt: string;
  grade?: Grade;             // заполнено при status = REVIEWED
}

export interface SubmissionFilters {
  status?: SubmissionStatus;
  userId?: string;
  topicId?: string;
  from?: string;             // ISO date 'yyyy-MM-dd'
  to?: string;               // ISO date
  page?: number;             // 0-based
  size?: number;             // default 20
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface GradeRequest {
  grammar: number;
  vocabulary: number;
  pronunciation: number;
  fluency: number;
  comment?: string;
}
```

### 3.3 Axios-методы

```ts
// ---------- Libraries (admin CRUD) ----------
export const getSpeakingLibraries = async (): Promise<SpeakingLibrary[]> => {
  const response = await api.get<SpeakingLibrary[]>('/admin/speaking/libraries');
  return response.data;
};

export const getSpeakingLibrary = async (id: string): Promise<SpeakingLibrary> => {
  const response = await api.get<SpeakingLibrary>(`/admin/speaking/libraries/${id}`);
  return response.data;
};

export const createSpeakingLibrary = async (data: CreateLibraryRequest): Promise<SpeakingLibrary> => {
  const response = await api.post<SpeakingLibrary>('/admin/speaking/libraries', data);
  return response.data;
};

export const updateSpeakingLibrary = async (id: string, data: UpdateLibraryRequest): Promise<SpeakingLibrary> => {
  const response = await api.put<SpeakingLibrary>(`/admin/speaking/libraries/${id}`, data);
  return response.data;
};

export const deleteSpeakingLibrary = async (id: string): Promise<void> => {
  await api.delete(`/admin/speaking/libraries/${id}`);
};

export const publishSpeakingLibrary = async (id: string, isPublished: boolean): Promise<SpeakingLibrary> => {
  const response = await api.patch<SpeakingLibrary>(`/admin/speaking/libraries/${id}/publish`, { isPublished });
  return response.data;
};

// ---------- Topics (admin CRUD) ----------
export const getSpeakingTopics = async (libraryId?: string): Promise<SpeakingTopic[]> => {
  const response = await api.get<SpeakingTopic[]>('/admin/speaking/topics', {
    params: { libraryId },
  });
  return response.data;
};

export const getSpeakingTopic = async (id: string): Promise<SpeakingTopic> => {
  const response = await api.get<SpeakingTopic>(`/admin/speaking/topics/${id}`);
  return response.data;
};

export const createSpeakingTopic = async (data: CreateTopicRequest): Promise<SpeakingTopic> => {
  const response = await api.post<SpeakingTopic>('/admin/speaking/topics', data);
  return response.data;
};

export const updateSpeakingTopic = async (id: string, data: UpdateTopicRequest): Promise<SpeakingTopic> => {
  const response = await api.put<SpeakingTopic>(`/admin/speaking/topics/${id}`, data);
  return response.data;
};

export const deleteSpeakingTopic = async (id: string): Promise<void> => {
  // Backend делает soft delete → топик isArchived, записи учеников сохраняются
  await api.delete(`/admin/speaking/topics/${id}`);
};

export const publishSpeakingTopic = async (id: string, isPublished: boolean): Promise<SpeakingTopic> => {
  const response = await api.patch<SpeakingTopic>(`/admin/speaking/topics/${id}/publish`, { isPublished });
  return response.data;
};

// ---------- Questions ----------
export const getTopicQuestions = async (topicId: string): Promise<SpeakingQuestion[]> => {
  const response = await api.get<SpeakingQuestion[]>(`/admin/speaking/topics/${topicId}/questions`);
  return response.data;
};

export const createTopicQuestion = async (topicId: string, data: CreateSpeakingQuestionRequest): Promise<SpeakingQuestion> => {
  const response = await api.post<SpeakingQuestion>(`/admin/speaking/topics/${topicId}/questions`, data);
  return response.data;
};

export const updateTopicQuestion = async (id: string, data: CreateSpeakingQuestionRequest): Promise<SpeakingQuestion> => {
  const response = await api.put<SpeakingQuestion>(`/admin/speaking/questions/${id}`, data);
  return response.data;
};

export const deleteTopicQuestion = async (id: string): Promise<void> => {
  await api.delete(`/admin/speaking/questions/${id}`);
};

export const reorderTopicQuestions = async (topicId: string, data: ReorderSpeakingQuestionsRequest): Promise<void> => {
  await api.post(`/admin/speaking/topics/${topicId}/questions/reorder`, data);
};

// ---------- Grading ----------
export const getSubmissions = async (filters: SubmissionFilters = {}): Promise<PagedResponse<SpeakingSubmission>> => {
  const response = await api.get<PagedResponse<SpeakingSubmission>>('/admin/speaking/submissions', {
    params: {
      status: filters.status,
      userId: filters.userId,
      topicId: filters.topicId,
      from: filters.from,
      to: filters.to,
      page: filters.page ?? 0,
      size: filters.size ?? 20,
    },
  });
  return response.data;
};

export const getSubmission = async (id: string): Promise<SpeakingSubmission> => {
  const response = await api.get<SpeakingSubmission>(`/admin/speaking/submissions/${id}`);
  return response.data;
};

// Вопросы топика для панели проверки
export const getSubmissionQuestions = async (submissionId: string): Promise<SpeakingQuestion[]> => {
  const response = await api.get<SpeakingQuestion[]>(`/admin/speaking/submissions/${submissionId}/questions`);
  return response.data;
};

export const createGrade = async (submissionId: string, data: GradeRequest): Promise<SpeakingSubmission> => {
  const response = await api.post<SpeakingSubmission>(`/admin/speaking/submissions/${submissionId}/grade`, data);
  return response.data;
};

export const updateGrade = async (submissionId: string, data: GradeRequest): Promise<SpeakingSubmission> => {
  const response = await api.put<SpeakingSubmission>(`/admin/speaking/submissions/${submissionId}/grade`, data);
  return response.data;
};

// Счётчик NEW для badge в сайдбаре (опционально, см. §5.3)
export const getNewSubmissionsCount = async (): Promise<number> => {
  const response = await api.get<{ count: number }>('/admin/speaking/submissions/count', {
    params: { status: 'NEW' },
  });
  return response.data.count;
};
```

### 3.4 Контрактный адаптер к реализованному backend (v1.1, 2026-08-01)

Спека §3.1–3.3 писалась до backend (Part 1). При реализации (Фаза 3, bd `8tg.3.*`) расхождения сосредоточены в адаптере `src/api/speakingApi.ts` — это каноническое описание фактического контракта:

| Аспект | Спека v1.0 (ожидание) | Фактический backend (реализация) |
|---|---|---|
| Publish library/topic | `PATCH …/publish` | Отдельного endpoint нет — `PUT` с `isPublished: true` |
| Детали library/topic/submission | `GET …/{id}` | GET by id нет — детали берутся из кэша списков TanStack Query |
| Reorder вопросов/топиков | batch endpoint | Цепочка `PUT` с обновлённым `displayOrder` |
| Вопросы топика | `GET …/topics/{id}/questions` | Вложенное поле `questions` в `AdminTopicResponse` (публичный detail отдаёт только опубликованное) |
| Submissions (inbox) | вложенная структура | Плоский ответ — нормализация во вложенную на клиенте; фильтры `dateFrom/dateTo`; пагинация Spring Page (поле `number`) |
| Маппинг полей | спековые имена | `title/topicCount/isDeleted/total/durationSec` — маппятся в спековые типы в адаптере |
| DELETE Library | soft delete | Hard delete каскадом; 400 при наличии submissions по теме |

При изменении backend-контракта правится ТОЛЬКО адаптер (+ эта таблица), экраны не трогаем.

### 3.5 TanStack Query hooks — `src/hooks/useSpeaking.ts`

Обёртки по паттерну из `src/screens/Categories.tsx` (`useQuery` + `queryKey` + `queryClient.invalidateQueries`):

```ts
// Query keys — централизованно, для точечной инвалидации
export const speakingKeys = {
  libraries: ['speaking', 'libraries'] as const,
  library: (id: string) => ['speaking', 'libraries', id] as const,
  topics: (libraryId?: string) => ['speaking', 'topics', libraryId ?? 'all'] as const,
  topic: (id: string) => ['speaking', 'topics', 'detail', id] as const,
  questions: (topicId: string) => ['speaking', 'topics', topicId, 'questions'] as const,
  submissions: (filters: SubmissionFilters) => ['speaking', 'submissions', filters] as const,
  submission: (id: string) => ['speaking', 'submissions', 'detail', id] as const,
  newCount: ['speaking', 'submissions', 'new-count'] as const,
};
```

Хуки (имена финальные, используются в компонентах ниже):

- `useSpeakingLibraries()` → `useQuery({ queryKey: speakingKeys.libraries, queryFn: getSpeakingLibraries })`
- `useSpeakingLibrary(id?)` — `enabled: !!id`
- `useSaveLibrary()` — mutation create/update (ветвление по наличию `id`), invalidate `libraries`
- `useDeleteLibrary()` / `usePublishLibrary()` — invalidate `libraries`
- `useSpeakingTopics(libraryId?)` — `queryKey: speakingKeys.topics(libraryId)`
- `useSpeakingTopic(id?)` — `enabled: !!id`
- `useSaveTopic()` / `useDeleteTopic()` / `usePublishTopic()` — invalidate `['speaking', 'topics']` (все варианты)
- `useTopicQuestions(topicId?)` — `enabled: !!topicId`
- `useSaveQuestion(topicId)` / `useDeleteQuestion(topicId)` / `useReorderQuestions(topicId)` — invalidate `speakingKeys.questions(topicId)` + `['speaking', 'topics']` (questionsCount)
- `useSubmissions(filters)` — `placeholderData: keepPreviousData` (TanStack Query 5) для плавной пагинации
- `useSubmission(id)` — `enabled: !!id`
- `useSaveGrade(submissionId, mode: 'create' | 'edit')` — create/update; invalidate `submission(id)`, `['speaking', 'submissions']`, `newCount`
- `useNewSubmissionsCount()` — `refetchInterval: 60_000` (опционально, T12)

---

## 4. Раздел «Speaking» — управление контентом

### 4.1 Доработка `MediaUploader` (БЛОКЕР для видео/субтитров)

Текущий `src/components/MediaUploader.tsx` поддерживает только две ветки: `accept === 'image/*'` → иначе считает аудио (`{ 'audio/*': [] }` в dropzone, `<audio>`-превью, подпись «MP3, WAV, OGG»). Для видео и `.vtt` этого недостаточно.

**Минимальная доработка (обратная совместимость обязательна — компонент используется в `src/screens/TestEditor.tsx` и `src/components/audio-tests/AudioTestEditor.tsx`):**

```ts
interface MediaUploaderProps {
  value?: string;
  onChange: (url: string | undefined) => void;
  folder?: string;
  /** Раньше: 'image/*' | 'audio/*'. Теперь любая accept-строка */
  accept?: string;
  /** Явный тип для превью; по умолчанию выводится из accept */
  mediaKind?: 'image' | 'audio' | 'video' | 'file';
  label?: string;
  /** Подпись под dropzone, напр. 'MP4, WebM до 50 МБ' */
  hint?: string;
}
```

- Маппинг accept для react-dropzone: `video/*` → `{ 'video/*': [] }`, `.vtt` → `{ 'text/vtt': ['.vtt'] }`. Старые строковые значения (`image/*`, `audio/*`) маппить как сейчас — существующие вызовы остаются рабочими.
- Превью по `mediaKind`: `image` → `<img>` (как сейчас), `audio` → `<audio controls>`, `video` → `<video controls preload="metadata" style={{ width: '100%', maxHeight: 240 }}>` (без autoplay, `preload="metadata"` — не тянуть 50+ МБ при открытии редактора), `file` → имя файла + ссылка «Открыть» (для `.vtt` опционально показать первые 3 строки содержимого — fetch текстом — чтобы учитель проверил субтитры).
- `handleReplace`: `input.accept = accept` напрямую (сейчас хардкод двух вариантов).

Папки загрузки (параметр `folder`, backend кладёт в MinIO): `speaking/covers`, `speaking/videos`, `speaking/subtitles`.

> Лимит: nginx `client_max_body_size 50m` (memory.md §5) — в UI для видео показывать подсказку «до 50 МБ» и валидировать размер файла до upload (`file.size > 50 * 1024 * 1024` → ошибка без запроса). PRD Open Question про максимальный размер видео — до ответа держим 50 МБ.

### 4.2 Экран `SpeakingLibraries` — `src/screens/SpeakingLibraries.tsx`

Список тем. По образцу `src/screens/Tests.tsx`.

**Структура:**

- Заголовок страницы: `<Typography variant="h4" data-testid="page-title">Speaking Libraries</Typography>` (testid обязателен — на него ждут E2E, грабля №22б).
- Toolbar: `SearchInput` (`data-testid="search-libraries"`) + кнопка «Add Library» (`data-testid="add-library-button"`, navigate → `/speaking/libraries/new`).
- Таблица (MUI `Table`, `data-testid="libraries-table"`): Cover (превью 48px), Name, Topics count, Order, Published (`Switch`, `data-testid={`publish-switch-${row.id}`}`), Actions (Edit/Delete `IconButton`).
- Сортировка/клиентская фильтрация по поиску — хук `useTable` (данные немногочисленны, серверная пагинация не нужна).
- Publish toggle → `usePublishLibrary` (Switch в MUI: input скрыт — в E2E кликать `{ force: true }`, грабля №22г).
- Delete → `useConfirm` + `ConfirmDialog` (`danger: true`); при `topicsCount > 0` в диалоге предупреждение «В теме N топиков, они будут архивированы» (каскадное поведение backend — уточнить в Part 1).
- Состояния: загрузка — `SkeletonCard` ×3; пусто — `EmptyState` («Нет ни одной темы. Создайте первую.», `data-testid="libraries-empty"`); ошибка — `ErrorDisplay` + retry.

### 4.3 Экран `SpeakingLibraryEditor` — `src/screens/SpeakingLibraryEditor.tsx`

Создание/редактирование темы. Общий компонент, режим по `useParams<{ id }>()` (`id` есть → edit: `useSpeakingLibrary(id)` + prefill).

**Форма** (react-hook-form + zod; `@hookform/resolvers` уже в зависимостях):

```ts
const librarySchema = z.object({
  name: z.string().min(1, 'Название обязательно').max(120),
  description: z.string().max(1000).optional(),
  coverUrl: z.string().url().optional(),
  displayOrder: z.number().int().min(0),
  isPublished: z.boolean(),
});
```

**Поля:**

| Поле | Компонент | testid |
|---|---|---|
| Name | `FormField` (TextField) | `library-name-input` |
| Description | TextField multiline rows=3 | `library-description-input` |
| Cover | `MediaUploader` (`folder="speaking/covers"`, `accept="image/*"`, `mediaKind="image"`, label «Обложка темы») | `library-cover-uploader` |
| Order | TextField type=number | `library-order-input` |
| Published | FormControlLabel + Switch | `library-published-switch` |

**Поведение:**

- Save (`FormActions`, `data-testid="save-library-button"`): create → navigate на `/speaking/libraries` + toast «Тема создана»; update → invalidate + toast «Сохранено». Ошибка 4xx/5xx → toast с сообщением (через `useToast`).
- Cancel → `navigate(-1)`; при dirty-форме — `useConfirm` («Несохранённые изменения будут потеряны»). Примечание: у нас legacy `<Routes>` (не data-router), `useBlocker` недоступен — MVP: предупреждение только на `beforeunload`, навигацию внутри SPA не блокируем; зафиксировать как известное ограничение.
- Кнопка Save disabled, пока `isSubmitting` или идёт загрузка обложки.

### 4.4 Экран `SpeakingTopics` — `src/screens/SpeakingTopics.tsx`

Список топиков всех тем.

- Toolbar: `SearchInput` (`data-testid="search-topics"`); фильтр по теме — MUI `Select` (`data-testid="library-filter-select"`, опции из `useSpeakingLibraries`, первая — «All libraries»); кнопка «Add Topic» (`data-testid="add-topic-button"` → `/speaking/topics/new`, при выбранном фильтре — `?libraryId=<id>` для предзаполнения).
- Таблица (`data-testid="topics-table"`): Name, Library, Duration (мм:сс из `durationSeconds`, «—» если null), Video (иконка-ссылка открыть в новой вкладке), Subtitles (✓/✗), Questions count, Published (Switch), Archived (chip «Archived», строки архивных — `bgcolor: 'action.hover'`, действия ограничены Edit), Actions.
- Дополнительно: warning-иконка «not playable» для опубликованного топика без видео или с 0 вопросов — такой топик ученик фактически не сможет пройти (tooltip с пояснением).
- Delete → ConfirmDialog «Топик будет архивирован. Записи учеников сохранятся.» (soft delete, PRD Edge Case).
- Empty state: если тем вообще нет → `EmptyState` с CTA «Сначала создайте тему» (navigate на `/speaking/libraries/new`), `data-testid="topics-empty"`.

### 4.5 Экран `SpeakingTopicEditor` — `src/screens/SpeakingTopicEditor.tsx`

Самый сложный экран раздела. По образцу `src/components/audio-tests/AudioTestEditor.tsx` (там тот же паттерн: медиа + вложенные вопросы).

**Layout:** MUI `Tabs` с двумя вкладками: «Details» и «Questions». Вкладка Questions disabled в режиме create до первого сохранения (вопросы привязаны к `topicId`; tooltip «Сначала сохраните топик»).

**Вкладка Details** (react-hook-form + zod):

```ts
const topicSchema = z.object({
  libraryId: z.string().min(1, 'Выберите тему'),
  name: z.string().min(1, 'Название обязательно').max(160),
  videoUrl: z.string().min(1, 'Загрузите видео'),
  subtitlesUrl: z.string().optional(),
  durationSeconds: z.number().int().positive().optional(),
  displayOrder: z.number().int().min(0),
  isPublished: z.boolean(),
});
```

| Поле | Компонент | testid |
|---|---|---|
| Library | MUI `Select` (опции `useSpeakingLibraries`) | `topic-library-select` |
| Name | FormField | `topic-name-input` |
| Video | `MediaUploader` (`folder="speaking/videos"`, `accept="video/*"`, `mediaKind="video"`, hint «MP4, WebM до 50 МБ») | `topic-video-uploader` |
| Subtitles (WebVTT) | `MediaUploader` (`folder="speaking/subtitles"`, `accept=".vtt"`, `mediaKind="file"`, hint «WebVTT (.vtt)») + кнопка «Убрать субтитры» (setValue undefined) | `topic-subtitles-uploader` |
| Duration (sec) | TextField type=number; автозаполнение: после загрузки видео читаем длительность через временный `HTMLVideoElement` (`loadedmetadata` → `duration`) и подставляем, поле остаётся редактируемым | `topic-duration-input` |
| Order | TextField type=number | `topic-order-input` |
| Published | Switch | `topic-published-switch` |

Валидация `.vtt` на клиенте до upload: расширение файла + первая строка начинается с `WEBVTT` (`file.text()` → `startsWith('WEBVTT')`), иначе ошибка в форме без запроса.

Save → `useSaveTopic`; после **create** — navigate на `/speaking/topics/:id/edit` (замена URL, чтобы включилась вкладка Questions) + toast.

**Вкладка Questions** — компонент `src/components/speaking/TopicQuestionsEditor.tsx`:

- Список вопросов (`useTopicQuestions(topicId)`), отсортирован по `displayOrder`.
- Каждый item (`data-testid={`question-item-${q.id}`}`): порядковый номер, текст (многострочный, до 500 символов), кнопки Edit/Delete.
- Добавление: TextField + кнопка «Add question» (`data-testid="add-question-button"`) → `useSaveQuestion` с `displayOrder = max + 1`; trim-валидация пустого текста.
- Редактирование inline: клик Edit → текст превращается в TextField с Save/Cancel.
- Порядок: кнопки ↑/↓ на item (`data-testid={`question-up-${q.id}`}` / `question-down-${q.id}`) → локальная перестановка + кнопка «Save order» (`data-testid="save-order-button"`) → `useReorderQuestions`. DnD (@dnd-kit есть в зависимостях) — **не** требуется в MVP, кнопок достаточно и они стабильнее в E2E.
- Empty state: «У топика пока нет вопросов. Добавьте первый — ученики отвечают на вопросы голосом.» (`data-testid="questions-empty"`).
- Валидация: текст обязателен, ≤500 символов; дубликат текста в рамках топика — предупреждение (не блокер).

---

## 5. Раздел «Grading» — проверка записей

### 5.1 Экран `GradingInbox` — `src/screens/GradingInbox.tsx`

Inbox practice-записей. **Серверная** пагинация и фильтрация (записей много) — `useTable` здесь НЕ подходит (он клиентский), используем MUI `TablePagination` с `count={totalElements}`.

**Фильтры (toolbar, `data-testid="grading-filters"`):**

| Фильтр | Компонент | testid | Параметр запроса |
|---|---|---|---|
| Status | MUI Select: `All` / `NEW` / `REVIEWED` (дефолт — `NEW`: учитель открывает inbox ради непроверенных) | `filter-status-select` | `status` |
| Student | MUI `Autocomplete` (опции — существующий `getAdminUsers({ query })` из `client.ts`, debounce 300 мс) | `filter-student-autocomplete` | `userId` |
| Topic | MUI `Autocomplete` (опции — `getSpeakingTopics()`, клиентский фильтр по вводу) | `filter-topic-autocomplete` | `topicId` |
| Date from/to | `DatePicker` из `@mui/x-date-pickers` (уже в зависимостях; `LocalizationProvider` + `AdapterDateFns`, date-fns установлен) | `filter-date-from` / `filter-date-to` | `from` / `to` |
| Reset | Button «Сбросить» | `filters-reset-button` | — |

- Состояние фильтров — `useState` + **синхронизация в query string** (`useSearchParams`: `status`, `userId`, `topicId`, `from`, `to`): ссылка на отфильтрованный inbox шэarabельна, возврат «назад» из детали сохраняет фильтры, переход `/grading?userId=<id>` даёт «записи студента» из других разделов.
- Любая смена фильтра → `setPage(0)` + новый `queryKey` (`speakingKeys.submissions(filters)`).
- Сортировка фиксированная: `submittedAt DESC` (новые сверху).

**Таблица (`data-testid="submissions-table"`):**

| Колонка | Данные |
|---|---|
| Student | `student.name` (+ email второй строкой, `variant="caption"`) |
| Topic | `topic.name` (+ `topic.libraryName` caption; если `topic.isArchived` — суффикс «(archived)») |
| Date | `format(new Date(submittedAt), 'dd.MM.yyyy HH:mm')` (date-fns) |
| Duration | `мм:сс` из `durationSeconds` |
| Status | MUI `Chip`: NEW — `color="warning"`, REVIEWED — `color="success"`; при REVIEWED рядом `totalScore` («7.5») |
| Actions | кнопка «Review» (NEW) / «View» (REVIEWED) → navigate `/grading/submissions/${id}` |

Пагинация: MUI `TablePagination` (`component="div"`, `count={totalElements}`, server-side; `data-testid="submissions-pagination"`). Размер страницы 20 (опции 10/20/50).

Состояния: загрузка — skeleton-строки (`Skeleton` в 3–5 `TableRow`); пусто — `EmptyState` (`data-testid="submissions-empty"`), **различать тексты**: без активных фильтров (дефолт NEW) — «Новых записей нет. Всё проверено 🎉»; с фильтрами — «Записи не найдены. Измените фильтры.» + кнопка Reset; ошибка — `ErrorDisplay` + retry.

### 5.2 Экран `GradingDetail` — `src/screens/GradingDetail.tsx`

Страница проверки одной записи. Layout — две колонки (desktop, MUI `Grid`): левая — плеер + вопросы, правая — рубрика. На mobile — вертикальный стек (Grid breakpoints).

**Шапка:** Breadcrumbs (автоматически из `getBreadcrumbPath`) + заголовок `data-testid="page-title"` = «Submission: {student.name} — {topic.name}», Chip статуса, дата/длительность.

**Левая колонка:**

1. **Аудио-плеер** — компонент `src/components/speaking/SubmissionAudioPlayer.tsx` (см. §6.3): `HTMLAudioElement` + кастомные контролы play/pause, seek-slider, текущее время/длительность, скорость (0.75/1/1.25/1.5 — удобно учителю). `data-testid="submission-audio-player"`, кнопка play — `data-testid="audio-play-button"`. Под плеером — ссылка «Download audio» (`<a href={audioUrl} download>`) как fallback, если браузер не играет формат.
2. **Вопросы топика** (`useQuery` на `getSubmissionQuestions(id)`; fallback — `useTopicQuestions(submission.topic.id)`): нумерованный `List`, `data-testid="submission-questions"`. Подзаголовок: «Ученик отвечал на эти вопросы за 30 секунд». При >5 вопросах — сворачиваемый `Accordion`.

**Правая колонка — рубрика, компонент `src/components/speaking/RubricForm.tsx`** (`data-testid="rubric-form"`):

- Четыре критерия, для каждого ряд: label («Grammar», «Vocabulary», «Pronunciation», «Fluency»), MUI `Slider` (`min=1, max=10, step=1, marks, valueLabelDisplay="auto"`, `data-testid={`rubric-slider-${criterion}`}`) + связанный `TextField type="number"` (inputProps min=1/max=10, `data-testid={`rubric-input-${criterion}`}`). Двусторонняя синхронизация slider ↔ input, clamp 1–10.
- **Начальное состояние (NEW):** критерии «не выставлены» (slider визуально на 5, но критерий помечен touched только после явного изменения slider/input). Кнопка Save **disabled, пока все 4 критерия не выставлены осознанно** — защита от отправки дефолтов.
- **Total** (`data-testid="rubric-total"`): вычисляется на клиенте как среднее с округлением до 1 знака — `Math.round((g + v + p + f) / 4 * 10) / 10`. Отображается крупно (`variant="h5"`) + Chip с цветовой шкалой (≤4 — error, 5–7 — warning, ≥8 — success), подпись «авто-усреднение». Backend пересчитывает сам; клиентское значение — только превью, в `GradeRequest` **не отправляем**.
- **Comment** (`data-testid="rubric-comment"`): TextField multiline rows=4, счётчик 0/2000, необязательный.
- Кнопки: «Save grade» (`data-testid="save-grade-button"`) и «Back to inbox» (`data-testid="back-to-inbox"`, `navigate(-1)` — фильтры inbox сохранятся через query string).

**Режимы:**

- `status === 'NEW'` → submit → `createGrade` → toast «Оценка сохранена», invalidate → статус становится REVIEWED, форма переходит в режим просмотра (остаёмся на странице).
- `status === 'REVIEWED'` → форма **предзаполнена** из `submission.grade`, по умолчанию readonly (поля disabled) + кнопка «Edit grade» (`data-testid="edit-grade-button"`) → поля активны, submit → `updateGrade` → toast «Оценка обновлена». Под формой: `Graded at …, updated at …` (аудит, PRD Story 7).

**Валидация (zod):** каждый критерий int 1–10 (обязателен), comment ≤2000. Save disabled при невалидной форме или пока идёт мутация.

**Повторные отправки ученика** на тот же топик — отдельные строки inbox, никакой дедупликации на фронте (PRD Edge Case).

**Навигация между записями (не MVP):** кнопки «Prev/Next submission» в рамках текущего фильтра inbox — передавать ordered ids через `location.state`. В MVP — только «Back to inbox».

### 5.3 Badge NEW в сайдбаре (опционально, T12)

`NavItem.badge?: number` уже предусмотрен интерфейсом. `Sidebar` подставляет `useNewSubmissionsCount()` для пункта `grading`. При реализации проверить, что `Sidebar.tsx` реально рендерит `badge` (в текущем коде поле есть в интерфейсе; если рендеринга нет — обернуть label в MUI `<Badge>`). Не блокер MVP.

---

## 6. UX-детали

### 6.1 Общие состояния (чеклист для всех новых экранов)

| Состояние | Реализация | Где |
|---|---|---|
| Loading (первичный) | `PageLoader` | detail-экраны |
| Loading (таблица/refetch) | `SkeletonCard` / MUI `Skeleton` в 3–5 `TableRow` | все списки |
| Empty | `EmptyState` + уникальный `data-testid` (`libraries-empty`, `topics-empty`, `questions-empty`, `submissions-empty`) | все списки |
| Error (query) | `ErrorDisplay` / `Alert severity="error"` + «Retry» → `refetch()`; текст человеческий, не axios-дамп (философия грабли №15 memory.md) | все страницы |
| Error (mutation) | toast через `useToast` (`error.response?.data?.message ?? error.message`); введённые данные формы **не очищаются** | редакторы, рубрика |
| Confirm delete | `useConfirm` (`danger: true`), текст с именем сущности | все delete |
| Disabled | Save disabled при `isPending` мутации и невалидной форме | все формы |
| 401 | глобальный интерцептор `client.ts` (редирект на `/login`) — ничего дополнительного не делать | везде |

### 6.2 data-testid конвенции

Следуем существующей схеме (`page-title`, `add-test-button`, `search-tests`, `nav-{id}`): kebab-case, префикс сущности, суффикс типа контрола (`-input`, `-button`, `-select`, `-switch`, `-table`, `-empty`, `-uploader`). Динамические — с id: `publish-switch-${id}`, `question-item-${id}`, `rubric-slider-grammar`. Полный реестр новых testid сведён таблицами в §4–5 — E2E пишется строго по ним.

### 6.3 Воспроизведение m4a в браузере

Practice-записи — AAC/m4a (Android MediaRecorder, PRD Technical Constraints).

- `HTMLAudioElement` играет AAC-in-MP4: Chrome/Edge/Safari — из коробки; Firefox — зависит от OS-кодеков (на Windows/macOS работает). Принимаемый риск: учительский инструмент, рекомендация Chrome. Сторонних плееров/библиотек не ставим.
- `SubmissionAudioPlayer`: рендерим `<audio ref={audioRef} src={audioUrl} preload="metadata" />` скрытым и управляем им (`play()`, `pause()`, `currentTime`, `playbackRate`); UI — MUI Slider (seek), IconButton (play/pause), Select скорости. События: `timeupdate` → позиция, `loadedmetadata` → длительность, `ended` → сброс состояния, `error` → сообщение «Не удалось загрузить аудио» + retry (пересоздание `src` с cache-buster `?t=${Date.now()}`) + видимая ссылка «Download audio».
- Длительность в UI брать из `durationSeconds` API-ответа, НЕ из `audio.duration` (может быть `Infinity`/NaN до `loadedmetadata`).
- В `MediaUploader` (`mediaKind="video"`) — нативный `<video controls preload="metadata">`, кастомные контролы не нужны.
- MinIO URL приходит с backend уже публичным (`S3_PUBLIC_URL`, BUG-004 закрыт); если в dev встречается `http://minio:9000/...` — это misconfig окружения, чинить env, не фронт (memory.md №2, №12). На клиенте URL не нормализуем.

### 6.4 Мелочи

- Длительность везде форматировать `мм:сс` хелпером `formatDuration(seconds)` — положить в `src/utils/formatters.ts` (каталог `src/utils` существует).
- Даты — date-fns `format(..., 'dd.MM.yyyy HH:mm')`.
- Тексты UI разделов — на английском (как существующие «Dashboard/Tests/Users»); toasts и сообщения ошибок — на русском (как в `MediaUploader`) — консистентно с текущим стилем админки.
- Toast'ы успеха единым стилем через `useToast`: «Тема создана», «Топик сохранён», «Вопрос добавлен», «Оценка сохранена».

---

## 7. Тестирование

### 7.1 Vitest (unit/component)

Паттерны: `src/__tests__/components/Button.test.tsx`, `src/hooks/__tests__/`, `src/__tests__/setup.ts`, `src/__tests__/client.test.ts` (jsdom + Testing Library). Тестовые файлы — рядом с тестируемым кодом (`src/components/speaking/__tests__/`, `src/screens/__tests__/`).

| Файл | Что проверяем |
|---|---|
| `src/components/speaking/__tests__/RubricForm.test.tsx` | (1) авто-усреднение total: 8/7/9/6 → «7.5», обновление при изменении критерия; (2) clamp input 0→1, 11→10; (3) sync slider↔input; (4) Save disabled, пока не выставлены все 4 критерия (дефолт «не выставлен», не auto-5); (5) режим REVIEWED — prefill + disabled до клика «Edit grade», отображение `updatedAt`; (6) submit вызывает mutation с `GradeRequest` БЕЗ `totalScore` |
| `src/components/speaking/__tests__/TopicQuestionsEditor.test.tsx` | добавление вопроса (`displayOrder = max + 1`), trim-валидация пустого текста, inline-edit, delete с подтверждением, reorder ↑/↓ → «Save order» вызывает `reorderTopicQuestions` с новым порядком ids, empty state |
| `src/screens/__tests__/GradingInbox.test.tsx` | рендер строк из мока `getSubmissions`, Chip NEW/REVIEWED, смена status-фильтра → api вызван с `params.status`, empty state при `content: []` (разные тексты с/без фильтров), начальные фильтры из searchParams (`?status=NEW&userId=u1`) |
| `src/screens/__tests__/SpeakingLibraries.test.tsx` | поиск фильтрует строки, publish toggle → `publishSpeakingLibrary(id, !isPublished)`, delete → ConfirmDialog → `deleteSpeakingLibrary` |
| `src/__tests__/speakingApi.test.ts` | axios-мок (`vi.mock`): URL и params каждого метода (особенно `getSubmissions` — маппинг фильтров → query params, дефолты page=0/size=20; payload `createGrade`/`updateGrade`) |
| `src/utils/__tests__/formatDuration.test.ts` | 0 → «0:00», 65 → «1:05», 3600 → «60:00» |

QueryClient в тестах — обёртка `QueryClientProvider` со свежим `new QueryClient({ defaultOptions: { queries: { retry: false } } })`. Моки API — `vi.mock('../../api/speakingApi')`.

### 7.2 Playwright E2E

Конфиг — существующий `admin-web/playwright.config.ts`: setup-проект (`e2e/auth.setup.ts` → storageState `e2e/.auth/admin.json`), `workers: 1`, `fullyParallel: false`. **Конфиг не менять**: новые спеки подхватываются существующими проектами; наши спеки лежат **не** в `tests/navigation/` — под действующие `testIgnore` не попадают (грабля №22з). Навигационный смоук-тест новых пунктов меню включить в `speaking-content.spec.ts`, а не в navigation-каталог.

Запуск против docker-стека (грабля №11):

```bash
SKIP_WEB_SERVER=1 ADMIN_URL=http://localhost:3000 npx playwright test e2e/tests/speaking-content.spec.ts e2e/tests/grading.spec.ts
```

Сьюты запускать последовательно с другими E2E (грабля №26 — флаки под параллельной нагрузкой). Проверка exit code — без маскирующих пайпов (грабля №30).

**Page Objects** (паттерн `e2e/pages/TestsPage.ts`, экспорт добавить в `e2e/pages/index.ts`):

| Файл | Класс | Ключевые методы |
|---|---|---|
| `e2e/pages/SpeakingLibrariesPage.ts` | `SpeakingLibrariesPage` | `goto()` (`page.goto('/speaking/libraries')` + `pageTitle.waitFor()` — НЕ networkidle, грабля №22б), `clickAdd()`, `search(name)`, `togglePublish(rowName)` — **force-click Switch** (№22г), `deleteLibrary(rowName)` с подтверждением диалога |
| `e2e/pages/SpeakingLibraryEditorPage.ts` | `SpeakingLibraryEditorPage` | `fillForm({name, description, order})`, `save()`, `expectSaved()` |
| `e2e/pages/SpeakingTopicsPage.ts` | `SpeakingTopicsPage` | `goto()`, `filterByLibrary(name)` — **MUI Select клавиатурой**: клик combobox → ждать `[role="option"]` → пауза 300–400 мс → `ArrowDown`/`Enter` (№22в), `clickAdd()` |
| `e2e/pages/SpeakingTopicEditorPage.ts` | `SpeakingTopicEditorPage` | `fillDetails(...)`, `uploadVideo(path)` / `uploadSubtitles(path)` — `page.setInputFiles` на скрытый `<input type="file">` внутри dropzone (не drag&drop в E2E — флаки), `addQuestion(text)`, `expectQuestion(index, text)`, `switchToQuestionsTab()` |
| `e2e/pages/GradingInboxPage.ts` | `GradingInboxPage` | `goto()`, `filterStatus('NEW')` (keyboard!), `openFirstSubmission()`, `expectStatusChip(row, 'REVIEWED')` |
| `e2e/pages/GradingDetailPage.ts` | `GradingDetailPage` | `expectAudioPlayer()` (виден `audio` с `src`), `expectQuestionsVisible()`, `setCriterion(criterion, value)` — заполнять **number input** (`rubric-input-*`), не драгать слайдер (флаки!), `expectTotal('7.5')`, `fillComment(text)`, `saveGrade()`, `expectStatusReviewed()` |

**Тестовые фикстуры** (каталог `e2e/fixtures/` существует): `sample-video.mp4` (~200 КБ), `sample-subtitles.vtt` (валидный `WEBVTT` + 1–2 cue), `sample-audio.m4a` (для сида submission).

**Спеки:**

`e2e/tests/speaking-content.spec.ts` (чейн, как `e2e-chain-a-create.spec.ts` — создаём реальные сущности последовательно):

1. Навигационный смоук: открыть `/speaking/libraries` из сайдбара (`nav-speaking` → `nav-speaking-libraries`), виден `page-title`; `nav-grading` ведёт на `/grading`. На mobile-проекте drawer — проверка по `[data-testid^="nav-"]` в `.MuiModal-root` (№22е).
2. Создать тему «E2E Speaking Library» (имя + порядок) → появилась в списке.
3. Создать топик: выбрать тему (keyboard-Select), загрузить `sample-video.mp4` и `sample-subtitles.vtt` через `setInputFiles`, сохранить → редирект на `:id/edit`, вкладка Questions активна.
4. Добавить 2 вопроса → reorder ↑/↓ → «Save order» → после reload порядок сохранён → edit вопроса → delete вопроса.
5. Publish toggle топика и темы (force-click Switch).
6. Удалить топик → подтверждение → chip «Archived» (поведение зафиксировать в тесте).
7. Cleanup: архивировать тестовую тему (префикс имён «E2E …» — как принято в существующих спеках).

`e2e/tests/grading.spec.ts`:

1. **Подготовка (beforeAll)**: submission создаётся через API — `request.post` practice-записи от имени demo-юзера (`demo@sotospeak.app`, dev-compose) на `POST /api/speaking/practice` (multipart, как мобильный клиент; контракт — Part 1) с `sample-audio.m4a`. Если сид через API невозможен — backend-сид скрипт (согласовать с Part 1). Cleanup после прогона — спеки независимы при `workers: 1`.
2. Открыть `/grading` (`nav-grading`), дефолтный фильтр NEW → запись видна.
3. Открыть запись → виден плеер (`audio` с `src`), список вопросов.
4. Выставить оценки 8/7/9/6 (через inputs) → total «7.5» → комментарий → Save → toast, статус REVIEWED.
5. Вернуться в inbox → при фильтре NEW записи нет, при REVIEWED — есть, chip с «7.5».
6. Открыть снова → форма предзаполнена и disabled → «Edit grade» → fluency 10 → Save → в inbox «8.5», «updated at» обновлён.
7. Фильтр по студенту (Autocomplete: ввод → ждать `[role="option"]` → Enter), фильтр по дате (fill в DatePicker input `MM/DD/YYYY`).

**Жёсткие правила E2E (memory.md №22, 23, 26, 30 — обязательны):**

- Авторизация — только storageState из setup-проекта (№22а: ручной логин в beforeEach запрещён). Rate limit логина в dev-compose уже ослаблен (№23) — новые спеки не должны добавлять логинов.
- Стабильные локаторы — только `data-testid`; ожидание готовности страницы — `waitFor` на `page-title`, не `networkidle`/`waitForTimeout` (кроме задокументированных пауз стабилизации MUI-меню).
- MUI Select — keyboard-выбор; MUI Switch — `click({ force: true })`.

### 7.3 Что НЕ тестируем здесь

- Мобильные (composeApp) экраны — Part 2.
- Backend-контракты — Part 1 (Spring-тесты). Наши E2E сквозные, но ответственность за API — на Part 1.

---

## 8. Разбивка на задачи

Нумерованные задачи → bd issues (`bd create`; по AGENTS.md задачи ведутся в beads, этот список — только план-источник). Оценки — в идеальных часах для разработчика/AI-агента, знающего кодовую базу.

| # | Задача | Зависимости | Оценка |
|---|---|---|---|
| T1 | `MediaUploader`: поддержка `video/*`, `.vtt`, `mediaKind`, `hint`, универсальный accept + client-side валидация размера (50 МБ) и WebVTT-заголовка. Регрессия существующих использований (TestEditor, AudioTestEditor) | — | 3h |
| T2 | `speakingApi.ts`: типы + все axios-методы (§3), re-export в `src/api/index.ts`; `formatDuration` в `src/utils/formatters.ts` | — (контракт Part 1) | 3h |
| T3 | `src/hooks/useSpeaking.ts`: `speakingKeys` + все хуки §3.5 | T2 | 3h |
| T4 | Навигация: `navItems.ts` (2 раздела), роуты в `App.tsx`, `VALID_ROUTES` в `RouteValidator.tsx`, re-export экранов-заглушек в `src/screens/index.ts` | — | 2h |
| T5 | `SpeakingLibraries` + `SpeakingLibraryEditor` (список, поиск, CRUD, publish, delete-confirm) | T1, T3, T4 | 6h |
| T6 | `SpeakingTopics` (список, фильтр по теме, publish, archive, warning «not playable») | T3, T4 | 4h |
| T7 | `SpeakingTopicEditor` вкладка Details (форма, video/subtitles upload, автодлительность видео) | T1, T3, T4, T6 | 5h |
| T8 | `TopicQuestionsEditor` (вкладка Questions: CRUD + reorder ↑/↓) | T3, T7 | 4h |
| T9 | `GradingInbox`: таблица + 5 фильтров (Select, Autocomplete×2, DatePicker×2) + серверная пагинация + фильтры в query string | T3, T4 | 7h |
| T10 | `SubmissionAudioPlayer` (HTMLAudioElement + кастомные контролы + скорость + fallback download) | — | 3h |
| T11 | `GradingDetail` + `RubricForm` (слайдеры/inputs, touched-tracking, авто-total с цветовой шкалой, комментарий, create/edit режимы) | T3, T4, T10 | 7h |
| T12 | (Опционально) Badge NEW в сайдбаре + `getNewSubmissionsCount` | T2, T4 | 2h |
| T13 | Vitest: все тесты §7.1 (≥20 кейсов), `npm test` зелёный | T5–T11 | 6h |
| T14 | E2E: фикстуры (mp4/vtt/m4a), 6 Page Objects, `speaking-content.spec.ts` | T5–T8 | 6h |
| T15 | E2E: `grading.spec.ts` (включая сид submission через API) | T9–T11, T14 | 5h |
| T16 | Финальный прогон: `npm run lint` (учесть граблю №27 — удалить `storybook-static/` перед lint), `npm test`, E2E против docker последовательно (№26); обновить `memory.md` (новые testid/грабли), закрыть bd issues | T13–T15 | 2h |

**Итого MVP (T1–T11, T13–T16): ~58h (~7.5 рабочих дней)**; с опциональным T12 — ~60h.

Порядок волн: T1–T4 (фундамент, параллелятся) → T5–T8 (контент) → T9–T11 (grading) → T13–T16 (качество). Backend-эндпоинты (Part 1) должны быть доступны к T9; до этого контентные задачи можно вести против моков.

---

## 9. File Checklist

### Новые файлы

```
admin-web/src/api/speakingApi.ts
admin-web/src/hooks/useSpeaking.ts
admin-web/src/screens/SpeakingLibraries.tsx
admin-web/src/screens/SpeakingLibraryEditor.tsx
admin-web/src/screens/SpeakingTopics.tsx
admin-web/src/screens/SpeakingTopicEditor.tsx
admin-web/src/screens/GradingInbox.tsx
admin-web/src/screens/GradingDetail.tsx
admin-web/src/components/speaking/TopicQuestionsEditor.tsx
admin-web/src/components/speaking/RubricForm.tsx
admin-web/src/components/speaking/SubmissionAudioPlayer.tsx
admin-web/src/components/speaking/__tests__/RubricForm.test.tsx
admin-web/src/components/speaking/__tests__/TopicQuestionsEditor.test.tsx
admin-web/src/screens/__tests__/GradingInbox.test.tsx
admin-web/src/screens/__tests__/SpeakingLibraries.test.tsx
admin-web/src/__tests__/speakingApi.test.ts
admin-web/src/utils/__tests__/formatDuration.test.ts
admin-web/e2e/pages/SpeakingLibrariesPage.ts
admin-web/e2e/pages/SpeakingLibraryEditorPage.ts
admin-web/e2e/pages/SpeakingTopicsPage.ts
admin-web/e2e/pages/SpeakingTopicEditorPage.ts
admin-web/e2e/pages/GradingInboxPage.ts
admin-web/e2e/pages/GradingDetailPage.ts
admin-web/e2e/tests/speaking-content.spec.ts
admin-web/e2e/tests/grading.spec.ts
admin-web/e2e/fixtures/sample-video.mp4
admin-web/e2e/fixtures/sample-subtitles.vtt
admin-web/e2e/fixtures/sample-audio.m4a
```

### Изменяемые файлы

```
admin-web/src/components/MediaUploader.tsx              # mediaKind, accept, hint, валидации (T1)
admin-web/src/components/navigation/navItems.ts         # разделы Speaking + Grading (T4)
admin-web/src/App.tsx                                   # новые роуты (T4)
admin-web/src/components/navigation/RouteValidator.tsx  # VALID_ROUTES (T4)
admin-web/src/screens/index.ts                          # re-export новых экранов (T4)
admin-web/src/api/index.ts                              # re-export speakingApi (T2)
admin-web/src/utils/formatters.ts                       # formatDuration (T2)
admin-web/e2e/pages/index.ts                            # re-export новых Page Objects (T14)
admin-web/src/components/layout/Sidebar.tsx             # только если добавляем badge (T12)
```

---

## 10. Open Questions

Для сверки с Part 1 (backend) / владельцем продукта:

1. Точные пути/имена полей эндпоинтов `/admin/speaking/*` — если в Part 1 отличаются, правим только `speakingApi.ts`.
2. Формат пагинации submissions — Spring `Page` vs кастомный `{content, totalElements, ...}` (влияет на тип `PagedResponse<T>`).
3. Отдаёт ли `GET /admin/speaking/submissions/{id}` вопросы топика сразу или нужен отдельный `.../questions` (в спеке предусмотрен отдельный + fallback).
4. Каскад при удалении Library с топиками (архивируются ли топики) — влияет на текст ConfirmDialog.
5. Эндпоинт сида submission для E2E (или разрешённый способ создания practice-записи от demo-юзера).
6. Лимит размера видео на upload (nginx `client_max_body_size` — сейчас 50m) — подтвердить целевое значение из PRD Open Questions.
