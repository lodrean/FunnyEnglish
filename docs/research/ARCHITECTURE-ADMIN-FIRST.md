# Research: Admin-First Architecture

## Context
Приложение для преподавателя английского и группы учеников.
Ключевой фокус: удобное создание контента (тестов/уроков) преподавателем.

## Key Requirements

### 1. Расширяемая система типов вопросов
Нужна архитектура, позволяющая легко добавлять новые типы вопросов без изменения БД.

### 2. Типы вопросов для MVP
На основе дизайна (слайд 5 - Lesson Screen):
- **Text Select** - выбор текстового ответа
- **Image Select** - выбор картинки (с эмодзи/иконками)
- **Audio Select** - аудирование + выбор ответа
- **Drag & Drop** - перенос слова на область картинки
- **Fill Blank** - заполнение пропуска

### 3. Admin Experience
- WYSIWYG редактор тестов
- Загрузка медиа (картинки, аудио)
- Drag-n-drop для reorder
- Preview теста
- Дублирование вопросов

## Architecture Decision: JSON-based Question Content

### Decision
Хранить content вопроса как JSONB в PostgreSQL вместо реляционных таблиц.

### Schema
```sql
questions:
  - id: UUID
  - test_id: UUID (FK)
  - type: VARCHAR (TEXT_SELECT, IMAGE_SELECT, etc.)
  - title: VARCHAR
  - content: JSONB (гибкая структура)
  - order_index: INT
  - points: INT
  - media_url: VARCHAR (опционально - картинка/аудио вопроса)
```

### Content JSON Structure по типам

#### Text Select
```json
{
  "text": "Как переводится 'Apple'?",
  "answers": [
    {"id": "a1", "text": "Яблоко", "isCorrect": true},
    {"id": "a2", "text": "Апельсин", "isCorrect": false},
    {"id": "a3", "text": "Груша", "isCorrect": false}
  ]
}
```

#### Image Select
```json
{
  "text": "Выберите яблоко:",
  "answers": [
    {"id": "a1", "imageUrl": "...", "emoji": "🍎", "isCorrect": true},
    {"id": "a2", "imageUrl": "...", "emoji": "🍊", "isCorrect": false}
  ]
}
```

#### Audio Select
```json
{
  "audioUrl": "...",
  "transcript": "(опционально для админа)",
  "text": "Что вы услышали?",
  "answers": [...]
}
```

#### Drag & Drop (Match)
```json
{
  "text": "Соедините слова с картинками:",
  "items": [
    {"id": "i1", "text": "Apple", "targetId": "t1"},
    {"id": "i2", "text": "Orange", "targetId": "t2"}
  ],
  "targets": [
    {"id": "t1", "imageUrl": "...", "emoji": "🍎"},
    {"id": "t2", "imageUrl": "...", "emoji": "🍊"}
  ]
}
```

#### Fill Blank
```json
{
  "text": "I ___ an apple every day.",
  "blankPosition": 1,
  "answers": [
    {"id": "a1", "text": "eat", "isCorrect": true},
    {"id": "a2", "text": "eats", "isCorrect": false}
  ]
}
```

## Admin Panel Flow

### 1. Test Builder Page
```
┌─────────────────────────────────────────────┐
│  Test: "Фрукты - Базовый"          [Save]   │
├─────────────────────────────────────────────┤
│                                             │
│  [+ Add Question]  [Preview]  [Settings]    │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │ 1. Text Select        [⋮] [✏️] [🗑] │   │
│  │    "Как переводится..."            │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │ 2. Image Select       [⋮] [✏️] [🗑] │   │
│  │    [🍎] [🍊] [🍇] [🍌]            │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │ 3. Drag & Drop        [⋮] [✏️] [🗑] │   │
│  │    [Слово] → [🖼️]                  │   │
│  └─────────────────────────────────────┘   │
│                                             │
└─────────────────────────────────────────────┘
```

### 2. Question Editor Modal
```
┌─────────────────────────────────────────────┐
│  Edit Question: Text Select         [X]     │
├─────────────────────────────────────────────┤
│                                             │
│  Type: [Text Select ▼]                     │
│                                             │
│  Question Text:                            │
│  ┌─────────────────────────────────────┐   │
│  │ Как переводится 'Apple'?           │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  Answers:                                  │
│  ○ Яблоко                    [Correct ☑]  │
│  ○ Апельсин                               │
│  ○ Груша                                  │
│  [+ Add Answer]                           │
│                                             │
│  Points: [10]                             │
│                                             │
│           [Cancel]        [Save Question]   │
└─────────────────────────────────────────────┘
```

### 3. Media Upload Flow
```
Upload Area (Drag & Drop)
├─ Картинки: .png, .jpg, .webp → S3/MinIO
├─ Аудио: .mp3, .wav → S3/MinIO
└─ Иконки: Emoji picker или загрузка SVG
```

## Расширяемость

### Добавление нового типа вопроса:
1. Добавить значение в QuestionType enum
2. Создать React компонент редактора (admin)
3. Создать React компонент отображения (mobile)
4. Добавить валидацию JSON схемы

### Пример: Добавление типа "Ordering"
```kotlin
enum class QuestionType {
    TEXT_SELECT,
    IMAGE_SELECT,
    AUDIO_SELECT,
    DRAG_DROP,
    FILL_BLANK,
    ORDERING  // новый тип
}
```

JSON Schema:
```json
{
  "text": "Расположите в правильном порядке:",
  "items": [
    {"id": "i1", "text": "I", "correctOrder": 1},
    {"id": "i2", "text": "eat", "correctOrder": 2},
    {"id": "i3", "text": "apple", "correctOrder": 3}
  ]
}
```

## Database Changes

### New Tables
```sql
-- question_types (reference table)
CREATE TABLE question_types (
    type VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    description TEXT,
    schema JSONB,  -- JSON Schema для валидации
    is_active BOOLEAN DEFAULT true
);

-- questions (updated structure)
ALTER TABLE questions 
ADD COLUMN content JSONB,
ADD COLUMN question_type VARCHAR(50),
ADD COLUMN media_url VARCHAR(500),
DROP COLUMN text,  -- переезжает в content
DROP COLUMN type;  -- заменяется на question_type

-- media_files (for admin uploads)
CREATE TABLE media_files (
    id UUID PRIMARY KEY,
    filename VARCHAR(255),
    url VARCHAR(500),
    type VARCHAR(50), -- IMAGE, AUDIO
    size_bytes BIGINT,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMP
);
```

## Implementation Priority

### Phase 1: Core Infrastructure
1. JSONB content structure
2. QuestionType enum
3. Validation service

### Phase 2: Admin Panel (CRITICAL)
1. Test builder page
2. Question editor components (по типам)
3. Media upload
4. Preview mode

### Phase 3: Mobile Display
1. Question renderers (по типам)
2. Answer validation
3. Progress tracking

## Recommendations
1. Использовать react-beautiful-dnd для drag-n-drop в админке
2. Использовать zod/yup для валидации JSON на фронтенде
3. Сделать компоненты редактора переиспользуемыми
4. Добавить "Duplicate" для быстрого создания похожих вопросов
