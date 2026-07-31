# Speaking Trainer — Техническая спецификация (Part 1: Backend)

**Feature ID:** SPEAKING-TRAINER-001
**Version:** 1.1
**Date:** 2026-07-30
**PRD:** `docs/prd/SPEAKING-TRAINER-001.prd.md`
**Scope Part 1:** Backend (Spring Boot) + модель данных + REST API. Admin-web и KMP-клиент — Part 2/Part 3.
**Blueprint-фича:** Audio Tests (`entity/audio/*`, `controller/audio/AudioTestController.kt`, `service/audio/AudioTestService.kt`, миграция `V11__create_audio_tests_tables.sql`, интеграционный тест `AudioTestIntegrationTest.kt`) — все новые компоненты повторяют её паттерны.

---

## 📑 Оглавление

1. [Архитектурный обзор](#1-архитектурный-обзор)
2. [Модель данных (ERD)](#2-модель-данных-erd)
3. [Flyway-миграции (V17, V18)](#3-flyway-миграции-v17-v18)
4. [JPA-сущности и DTO](#4-jpa-сущности-и-dto)
5. [REST API](#5-rest-api)
6. [Service-слой: бизнес-логика](#6-service-слой-бизнес-логика)
7. [Нефункциональные требования](#7-нефункциональные-требования)
8. [Требования к тестированию](#8-требования-к-тестированию)
9. [Разбивка на задачи](#9-разбивка-на-задачи)

---

## 1. Архитектурный обзор

### 1.1 Существующая инфраструктура (переиспользуем)

| Компонент | Файл | Что переиспользуем |
|---|---|---|
| Spring Boot 3.4.1, Kotlin 2.1.0, Java 21 | `backend/build.gradle.kts` | — |
| PostgreSQL + Flyway | `backend/src/main/resources/db/migration/` | **Последняя миграция — `V16__create_guest_events.sql`. Новые — V17+.** |
| JPA/Hibernate, `ddl-auto: validate` | `application.yml` | Сущности обязаны точно соответствовать DDL миграций |
| JWT-аутентификация | `security/JwtService.kt`, `security/JwtAuthenticationFilter.kt` (principal: `UserPrincipal(userId: String, role: String)`) | Без изменений |
| Правила доступа | `config/SecurityConfig.kt` | `/public/**` уже `permitAll()`, `/admin/**` уже `hasAuthority("ROLE_ADMIN")`, остальное — `authenticated()`. **Изменений в SecurityConfig НЕ требуется** (см. 5.6) |
| Загрузка файлов в MinIO/S3 | `service/StorageService.kt` (`uploadFile(file, folder): String`) | Требуется расширение whitelist расширений (см. 6.1) |
| Публичный URL медиа (BUG-004) | `service/MediaUrlService.kt`, `app.s3.public-url` (`S3_PUBLIC_URL`) | Все URL, отдаваемые клиенту, строятся через `StorageService.buildObjectUrl` (publicUrl) — внутренний `http://minio:9000` наружу не утекает |
| Admin media upload | `controller/AdminController.kt` → `POST /api/admin/media/upload?folder=...` | Admin-web грузит видео/субтитры через него, затем сохраняет URL в сущность |
| Обработка ошибок | `controller/GlobalExceptionHandler.kt` (`ErrorResponse(error, message, details?)`) | `IllegalArgumentException` → 400, `NoSuchElementException` → 404 — используем те же исключения |
| Blueprint full-stack | `entity/audio/*`, `service/audio/AudioTestService.kt`, `controller/audio/AudioTestController.kt`, `repository/audio/*` | Стиль сущностей (`@CreationTimestamp`/`@UpdateTimestamp`, LAZY-связи, add/remove-хелперы, кастомные equals/hashCode), JPQL `JOIN FETCH`, `Page<T>` |

### 1.2 Важные конвенции путей (грабля!)

`server.servlet.context-path: /api` (`application.yml`). Поэтому:

- **Доминирующая конвенция контроллеров — БЕЗ `/api`-префикса в `@RequestMapping`**: `AdminController` → `@RequestMapping("/admin")` (итог `/api/admin/...`), `PublicTestController` → `@RequestMapping("/public/tests")`.
- **Новые контроллеры этой фичи маппятся БЕЗ `/api`**: `/public/speaking`, `/speaking`, `/admin/speaking`.
- ⚠️ Не копировать `@RequestMapping("/api/audio-tests")` из `AudioTestController` — тот фактически живёт на `/api/api/audio-tests` (исторический квирк, на который завязан его SecurityConfig-матчер `"/api/audio-tests/**"`). Это НЕ образец для путей.
- Matchers в `SecurityConfig` относительны context-path: `"/public/**"` покрывает `/api/public/**`.

### 1.3 Целевая структура пакетов (новые файлы)

```
backend/src/main/kotlin/com/funnyenglish/
├── entity/speaking/
│   ├── Library.kt                  # NEW
│   ├── Topic.kt                    # NEW (soft delete)
│   ├── Video.kt                    # NEW
│   ├── SpeakingQuestion.kt         # NEW
│   ├── PracticeSubmission.kt       # NEW (+ enum SubmissionStatus)
│   └── Grade.kt                    # NEW
├── dto/
│   └── SpeakingDtos.kt             # NEW: все request/response DTO + мапперы
├── repository/speaking/
│   ├── LibraryRepository.kt        # NEW
│   ├── TopicRepository.kt          # NEW
│   ├── VideoRepository.kt          # NEW
│   ├── SpeakingQuestionRepository.kt # NEW
│   ├── PracticeSubmissionRepository.kt # NEW
│   └── GradeRepository.kt          # NEW
├── service/speaking/
│   ├── SpeakingContentService.kt   # NEW: публичный read + admin CRUD контента
│   └── PracticeSubmissionService.kt# NEW: upload submissions + grading
├── controller/speaking/
│   ├── SpeakingPublicController.kt # NEW: /public/speaking/**
│   ├── SpeakingSubmissionController.kt # NEW: /speaking/**
│   └── SpeakingAdminController.kt  # NEW: /admin/speaking/**
└── service/StorageService.kt       # MODIFY: +video/vtt расширения

backend/src/main/resources/db/migration/
├── V17__create_speaking_content_tables.sql   # NEW
└── V18__create_speaking_submissions_tables.sql # NEW

backend/src/test/kotlin/com/funnyenglish/
├── service/speaking/PracticeSubmissionServiceTest.kt   # NEW (unit, mockk)
└── controller/SpeakingFlowIntegrationTest.kt           # NEW (@SpringBootTest + MockMvc)
```

---

## 2. Модель данных (ERD)

### 2.1 Диаграмма

```
┌──────────────┐ 1     * ┌──────────────┐ 1     1 ┌──────────────┐
│  libraries   │─────────│    topics    │─────────│    videos    │
│              │         │ (soft delete)│         │ video_url    │
│ cover_url    │         │              │         │ subtitle_url │
└──────────────┘         └──────┬───────┘         │ duration_sec │
                                │ 1               └──────────────┘
                                │
                ┌───────────────┴────────────┐
                │ *                          │ *
     ┌──────────▼──────────┐      ┌──────────▼──────────┐ *        1 ┌──────────────┐
     │ speaking_questions  │      │ practice_submissions│────────────│    grades    │
     │ text, display_order │      │ audio_url           │            │ grammar      │
     └─────────────────────┘      │ status NEW/REVIEWED │            │ vocabulary   │
                                  │ user_id ──FK──► users          │ pronunciation│
                                  │ duration_sec        │            │ fluency      │
                                  └─────────────────────┘            │ total (gen)  │
                                                                     │ comment      │
                                                                     │ reviewer_id ─┼─FK──► users
                                                                     └──────────────┘
```

### 2.2 Таблицы (логическое описание; точный DDL — в разделе 3)

**`libraries`** — темы верхнего уровня (Story 1, 6).
`id UUID PK`, `title VARCHAR(255) NOT NULL`, `description TEXT`, `cover_url VARCHAR(500)` (MinIO через `/api/admin/media/upload`), `display_order INT NOT NULL DEFAULT 0`, `is_published BOOLEAN NOT NULL DEFAULT false`, `created_at/updated_at TIMESTAMPTZ`.
Индексы: `(is_published, display_order)`.

**`topics`** — топики внутри темы (Story 1, 6). Soft delete: `deleted_at TIMESTAMPTZ NULL` (Edge Case «Удаление топика, к которому есть записи» — записи сохраняются, топик архивируется).
`id UUID PK`, `library_id UUID NOT NULL FK → libraries(id) ON DELETE CASCADE`, `title VARCHAR(255) NOT NULL`, `description TEXT`, `display_order INT NOT NULL DEFAULT 0`, `is_published BOOLEAN NOT NULL DEFAULT false`, `deleted_at TIMESTAMPTZ`, `created_at/updated_at`.
Индексы: `(library_id, display_order)`, частичный `WHERE deleted_at IS NULL`.

**`videos`** — видео + субтитры топика, 1:1 с топиком (Story 2, 6). Отдельная таблица (не колонки в topics), чтобы не раздувать topics и дать независимый CRUD/замену файла.
`id UUID PK`, `topic_id UUID NOT NULL UNIQUE FK → topics(id) ON DELETE CASCADE`, `video_url VARCHAR(500) NOT NULL` (mp4/webm в MinIO, папка `speaking/videos`), `subtitle_url VARCHAR(500)` (WebVTT `.vtt` в MinIO, папка `speaking/subtitles`; NULL → режим «с субтитрами» недоступен на клиенте), `duration_seconds INT NOT NULL CHECK (> 0)`, `created_at/updated_at`.

**`speaking_questions`** — вопросы к топику (Story 3, 6).
`id UUID PK`, `topic_id UUID NOT NULL FK → topics(id) ON DELETE CASCADE`, `text TEXT NOT NULL` (на английском), `display_order INT NOT NULL DEFAULT 0`, `created_at`.
Индекс: `(topic_id, display_order)`.

**`practice_submissions`** — practice-записи учеников (Story 5, 7).
`id UUID PK`, `user_id UUID NOT NULL FK → users(id) ON DELETE CASCADE`, `topic_id UUID NOT NULL FK → topics(id) ON DELETE RESTRICT` (нельзя физически удалить топик с записями — только soft delete), `audio_url VARCHAR(500) NOT NULL` (AAC/m4a в MinIO, папка `speaking/submissions`), `duration_sec INT NOT NULL CHECK (duration_sec BETWEEN 1 AND 60)` (клиент шлёт ~30; допуск на сервере до 60 с запасом), `status VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW','REVIEWED'))`, `created_at/updated_at`.
Индексы: `(status, created_at DESC)` — inbox учителя; `(user_id, created_at DESC)` — «мои отправки»; `(topic_id)` — фильтр inbox.
Повторные отправки на один топик разрешены (PRD Edge Case) — уникального constraint на (user_id, topic_id) НЕТ.

**`grades`** — оценка учителя по рубрике, 1:1 с submission (Story 7).
`id UUID PK`, `submission_id UUID NOT NULL UNIQUE FK → practice_submissions(id) ON DELETE CASCADE`, `grammar/vocabulary/pronunciation/fluency INTEGER NOT NULL CHECK (BETWEEN 1 AND 10)`, `total NUMERIC(4,2) GENERATED ALWAYS AS ((grammar + vocabulary + pronunciation + fluency) / 4.0) STORED` (авто-усреднение на уровне БД — единый источник правды), `comment TEXT`, `reviewer_id UUID NOT NULL FK → users(id) ON DELETE RESTRICT`, `created_at/updated_at TIMESTAMPTZ` (updated_at — аудит редактирования оценки, PRD Story 7).

### 2.3 Правила целостности

- Публичная выдача видит только `is_published = true` и `deleted_at IS NULL`; библиотеки без опубликованных топиков скрываются (в выдаче — `topicCount > 0`, фильтр на бэкенде).
- Каскад `ON DELETE CASCADE` на контентных связях (library→topic→video/questions) — физическое удаление контента допустимо, пока нет submissions.
- `practice_submissions.topic_id ON DELETE RESTRICT` + soft delete topics — записи учеников не теряются никогда.
- Статус-машина: `NEW → REVIEWED` (однонаправленная при создании оценки; редактирование оценки статус НЕ меняет — уже REVIEWED).

---

## 3. Flyway-миграции (V17, V18)

**Проверено:** в `backend/src/main/resources/db/migration/` последняя версия — `V16__create_guest_events.sql`. Новые миграции: **V17** (контент) и **V18** (сабмитты/оценки). Разнесены в два файла для независимого ревью; можно сливать в одну, если задача делает и то, и другое.

### 3.1 `V17__create_speaking_content_tables.sql`

```sql
-- V17__create_speaking_content_tables.sql
-- Speaking Trainer: контентная модель (libraries, topics, videos, speaking_questions)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Темы (Library)
CREATE TABLE libraries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover_url VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Топики внутри темы (soft delete через deleted_at)
CREATE TABLE topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    library_id UUID NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Видео + субтитры топика (1:1)
CREATE TABLE videos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic_id UUID NOT NULL UNIQUE REFERENCES topics(id) ON DELETE CASCADE,
    video_url VARCHAR(500) NOT NULL,
    subtitle_url VARCHAR(500), -- WebVTT (.vtt), NULL = без субтитров
    duration_seconds INTEGER NOT NULL CHECK (duration_seconds > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Вопросы к топику
CREATE TABLE speaking_questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic_id UUID NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_libraries_published_order ON libraries(is_published, display_order);
CREATE INDEX idx_topics_library_order ON topics(library_id, display_order);
CREATE INDEX idx_topics_active ON topics(library_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_videos_topic ON videos(topic_id);
CREATE INDEX idx_speaking_questions_topic ON speaking_questions(topic_id, display_order);

COMMENT ON TABLE libraries IS 'Speaking trainer: темы верхнего уровня';
COMMENT ON TABLE topics IS 'Speaking trainer: топики; soft delete через deleted_at';
COMMENT ON TABLE videos IS 'Speaking trainer: видео и WebVTT-субтитры топика (MinIO URLs)';
COMMENT ON TABLE speaking_questions IS 'Speaking trainer: вопросы для устных ответов';
```

### 3.2 `V18__create_speaking_submissions_tables.sql`

```sql
-- V18__create_speaking_submissions_tables.sql
-- Speaking Trainer: practice-записи учеников и оценки учителя

CREATE TABLE practice_submissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id UUID NOT NULL REFERENCES topics(id) ON DELETE RESTRICT,
    audio_url VARCHAR(500) NOT NULL,
    duration_sec INTEGER NOT NULL CHECK (duration_sec BETWEEN 1 AND 60),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'REVIEWED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Оценка по рубрике, 1:1 с submission
CREATE TABLE grades (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submission_id UUID NOT NULL UNIQUE REFERENCES practice_submissions(id) ON DELETE CASCADE,
    grammar INTEGER NOT NULL CHECK (grammar BETWEEN 1 AND 10),
    vocabulary INTEGER NOT NULL CHECK (vocabulary BETWEEN 1 AND 10),
    pronunciation INTEGER NOT NULL CHECK (pronunciation BETWEEN 1 AND 10),
    fluency INTEGER NOT NULL CHECK (fluency BETWEEN 1 AND 10),
    total NUMERIC(4,2) GENERATED ALWAYS AS ((grammar + vocabulary + pronunciation + fluency) / 4.0) STORED,
    comment TEXT,
    reviewer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_submissions_status_created ON practice_submissions(status, created_at DESC);
CREATE INDEX idx_submissions_user_created ON practice_submissions(user_id, created_at DESC);
CREATE INDEX idx_submissions_topic ON practice_submissions(topic_id);
CREATE INDEX idx_grades_submission ON grades(submission_id);
CREATE INDEX idx_grades_reviewer ON grades(reviewer_id);

COMMENT ON TABLE practice_submissions IS 'Speaking trainer: голосовые practice-записи учеников (NEW/REVIEWED)';
COMMENT ON TABLE grades IS 'Speaking trainer: оценка учителя по рубрике; total — авто-усреднение (generated column)';
```

> **Замечание по `total`:** `GENERATED ALWAYS AS ... STORED` требует PostgreSQL 12+ (в проекте PostgreSQL из compose — 16, ок). Hibernate с `ddl-auto: validate` колонку проверяет по имени/типу; generated-выражение не валидируется. В JPA-сущности поле помечается `insertable = false, updatable = false` (см. 4.1).

---

## 4. JPA-сущности и DTO

### 4.1 Сущности (`entity/speaking/`)

Стиль — как `entity/audio/AudioTest.kt`: `class` (не data class) с mutable `var`-полями, `@CreationTimestamp/@UpdateTimestamp`, LAZY-связи, кастомные `equals/hashCode` по id, add/remove-хелперы для коллекций.

```kotlin
// entity/speaking/Library.kt
package com.funnyenglish.entity.speaking

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "libraries")
class Library(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "cover_url", length = 500)
    var coverUrl: String? = null,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = false,

    @OneToMany(mappedBy = "library", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    var topics: MutableSet<Topic> = mutableSetOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Library) return false
        return id != null && id == other.id
    }
    override fun hashCode(): Int = id?.hashCode() ?: 0
}
```

```kotlin
// entity/speaking/Topic.kt
package com.funnyenglish.entity.speaking

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "topics")
class Topic(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    var library: Library? = null,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = false,

    /** Soft delete: не-null = топик архивирован, записи учеников сохраняются */
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,

    @OneToOne(mappedBy = "topic", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var video: Video? = null,

    @OneToMany(mappedBy = "topic", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    var questions: MutableSet<SpeakingQuestion> = mutableSetOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
) {
    fun addQuestion(question: SpeakingQuestion) {
        questions.add(question)
        question.topic = this
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Topic) return false
        return id != null && id == other.id
    }
    override fun hashCode(): Int = id?.hashCode() ?: 0
}
```

```kotlin
// entity/speaking/Video.kt
package com.funnyenglish.entity.speaking

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "videos")
class Video(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false, unique = true)
    var topic: Topic? = null,

    @Column(name = "video_url", nullable = false, length = 500)
    var videoUrl: String,

    /** WebVTT (.vtt) в MinIO; null = субтитров нет */
    @Column(name = "subtitle_url", length = 500)
    var subtitleUrl: String? = null,

    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
)
```

```kotlin
// entity/speaking/SpeakingQuestion.kt
package com.funnyenglish.entity.speaking

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "speaking_questions")
class SpeakingQuestion(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    var topic: Topic? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var text: String,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
)
```

```kotlin
// entity/speaking/PracticeSubmission.kt
package com.funnyenglish.entity.speaking

import com.funnyenglish.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

enum class SubmissionStatus { NEW, REVIEWED }

@Entity
@Table(name = "practice_submissions")
class PracticeSubmission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    var topic: Topic? = null,

    @Column(name = "audio_url", nullable = false, length = 500)
    var audioUrl: String,

    @Column(name = "duration_sec", nullable = false)
    var durationSec: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SubmissionStatus = SubmissionStatus.NEW,

    @OneToOne(mappedBy = "submission", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var grade: Grade? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PracticeSubmission) return false
        return id != null && id == other.id
    }
    override fun hashCode(): Int = id?.hashCode() ?: 0
}
```

```kotlin
// entity/speaking/Grade.kt
package com.funnyenglish.entity.speaking

import com.funnyenglish.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "grades")
class Grade(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    var submission: PracticeSubmission? = null,

    @Column(nullable = false)
    var grammar: Int,

    @Column(nullable = false)
    var vocabulary: Int,

    @Column(nullable = false)
    var pronunciation: Int,

    @Column(nullable = false)
    var fluency: Int,

    /** Generated column в БД — только чтение */
    @Column(nullable = false, insertable = false, updatable = false)
    var total: BigDecimal? = null,

    @Column(columnDefinition = "TEXT")
    var comment: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    var reviewer: User? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
)
```

### 4.2 DTO (`dto/SpeakingDtos.kt`)

Паттерн — как `dto/AudioTestDtos.kt`: `data class` + `jakarta.validation` аннотации на request-DTO, id как `String` в ответах.

⚠️ **jackson-module-kotlin (грабля №18 memory.md):** модуль в backend подключён (2026-07-21), поэтому `isPublished` в Kotlin-DTO биндится/сериализуется корректно (`"isPublished"` в JSON). Правила:
- В JSON-контракте используем имена **`isPublished`**, **`isNewBestScore`-style** — то есть с `is`-префиксом; shared-клиент (kotlinx.serialization) ждёт именно их.
- Если при интеграционном тесте видим `"published": ...` в ответе — модуль не подхвачен, это регрессия (см. memory.md №18, №newBestScore-фикс через `@get:JsonProperty`).
- Request-DTO с Kotlin-дефолтами требуют module-kotlin (без него — NPE/500); это уже покрыто, но новые DTO проверять интеграционным тестом.

```kotlin
// dto/SpeakingDtos.kt
package com.funnyenglish.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.Instant

// ============ Public responses ============

data class LibraryResponse(
    val id: String,
    val title: String,
    val description: String?,
    val coverUrl: String?,
    val topicCount: Int          // только опубликованные и не удалённые топики
)

data class TopicListItemResponse(
    val id: String,
    val title: String,
    val description: String?,
    val durationSeconds: Int?,   // из Video; null если видео ещё не загружено
    val questionCount: Int,
    val hasSubtitles: Boolean
)

data class TopicDetailResponse(
    val id: String,
    val libraryId: String,
    val title: String,
    val description: String?,
    val video: VideoResponse?,
    val questions: List<SpeakingQuestionResponse>
)

data class VideoResponse(
    val videoUrl: String,
    val subtitleUrl: String?,    // WebVTT, публичный URL (S3_PUBLIC_URL)
    val durationSeconds: Int
)

data class SpeakingQuestionResponse(
    val id: String,
    val text: String,
    val displayOrder: Int
)

// ============ Submissions (user) ============

data class SubmissionResponse(
    val id: String,
    val topicId: String,
    val topicTitle: String,
    val audioUrl: String,
    val durationSec: Int,
    val status: String,          // "NEW" | "REVIEWED"
    val grade: GradeResponse?,
    val createdAt: Instant?
)

data class GradeResponse(
    val grammar: Int,
    val vocabulary: Int,
    val pronunciation: Int,
    val fluency: Int,
    val total: BigDecimal,       // авто-усреднённый балл
    val comment: String?,
    val reviewerName: String,
    val createdAt: Instant?,
    val updatedAt: Instant?
)

// ============ Admin requests ============

data class CreateLibraryRequest(
    @field:NotBlank @field:Size(max = 255) val title: String,
    @field:Size(max = 2000) val description: String? = null,
    @field:Size(max = 500) val coverUrl: String? = null,
    val displayOrder: Int = 0,
    val isPublished: Boolean = false
)

data class UpdateLibraryRequest(
    @field:Size(max = 255) val title: String? = null,
    @field:Size(max = 2000) val description: String? = null,
    @field:Size(max = 500) val coverUrl: String? = null,
    val displayOrder: Int? = null,
    val isPublished: Boolean? = null
)

data class CreateTopicRequest(
    @field:NotNull val libraryId: String,      // UUID строкой — паттерн AudioTestDtos
    @field:NotBlank @field:Size(max = 255) val title: String,
    @field:Size(max = 2000) val description: String? = null,
    val displayOrder: Int = 0,
    val isPublished: Boolean = false
)

data class UpdateTopicRequest(
    @field:Size(max = 255) val title: String? = null,
    @field:Size(max = 2000) val description: String? = null,
    val displayOrder: Int? = null,
    val isPublished: Boolean? = null
)

data class UpsertVideoRequest(
    @field:NotBlank @field:Size(max = 500) val videoUrl: String,
    @field:Size(max = 500) val subtitleUrl: String? = null,
    @field:NotNull @field:Min(1) val durationSeconds: Int
)

data class CreateSpeakingQuestionRequest(
    @field:NotBlank val text: String,
    val displayOrder: Int = 0
)

data class GradeSubmissionRequest(
    @field:NotNull @field:Min(1) @field:Max(10) val grammar: Int,
    @field:NotNull @field:Min(1) @field:Max(10) val vocabulary: Int,
    @field:NotNull @field:Min(1) @field:Max(10) val pronunciation: Int,
    @field:NotNull @field:Min(1) @field:Max(10) val fluency: Int,
    @field:Size(max = 5000) val comment: String? = null
)

// ============ Admin responses ============

data class AdminLibraryResponse(
    val id: String, val title: String, val description: String?,
    val coverUrl: String?, val displayOrder: Int, val isPublished: Boolean,
    val topicCount: Int, val createdAt: Instant?, val updatedAt: Instant?
)

data class AdminTopicResponse(
    val id: String, val libraryId: String, val title: String, val description: String?,
    val displayOrder: Int, val isPublished: Boolean, val isDeleted: Boolean,
    val video: VideoResponse?, val questions: List<SpeakingQuestionResponse>,
    val createdAt: Instant?, val updatedAt: Instant?
)

data class AdminSubmissionResponse(
    val id: String,
    val userId: String,
    val userEmail: String,
    val userDisplayName: String,
    val topicId: String,
    val topicTitle: String,
    val audioUrl: String,
    val durationSec: Int,
    val status: String,
    val grade: GradeResponse?,
    val createdAt: Instant?
)
```

---

## 5. REST API

### 5.1 Сводная таблица

| Метод | Путь (полный) | Контроллер/маппинг | Доступ |
|---|---|---|---|
| GET | `/api/public/speaking/libraries` | `SpeakingPublicController` → `/public/speaking` | Гость |
| GET | `/api/public/speaking/libraries/{id}/topics` | ↑ | Гость |
| GET | `/api/public/speaking/topics/{id}` | ↑ | Гость |
| POST | `/api/speaking/submissions` (multipart) | `SpeakingSubmissionController` → `/speaking` | ROLE_USER+ |
| GET | `/api/speaking/submissions/my` | ↑ | ROLE_USER+ |
| GET | `/api/admin/speaking/libraries` | `SpeakingAdminController` → `/admin/speaking` | ROLE_ADMIN |
| POST | `/api/admin/speaking/libraries` | ↑ | ROLE_ADMIN |
| PUT | `/api/admin/speaking/libraries/{id}` | ↑ | ROLE_ADMIN |
| DELETE | `/api/admin/speaking/libraries/{id}` | ↑ | ROLE_ADMIN |
| GET | `/api/admin/speaking/topics?libraryId=` | ↑ | ROLE_ADMIN |
| POST | `/api/admin/speaking/topics` | ↑ | ROLE_ADMIN |
| PUT | `/api/admin/speaking/topics/{id}` | ↑ | ROLE_ADMIN |
| DELETE | `/api/admin/speaking/topics/{id}` (soft) | ↑ | ROLE_ADMIN |
| PUT | `/api/admin/speaking/topics/{id}/video` | ↑ | ROLE_ADMIN |
| POST | `/api/admin/speaking/topics/{id}/questions` | ↑ | ROLE_ADMIN |
| PUT | `/api/admin/speaking/questions/{id}` | ↑ | ROLE_ADMIN |
| DELETE | `/api/admin/speaking/questions/{id}` | ↑ | ROLE_ADMIN |
| GET | `/api/admin/speaking/submissions?status=NEW&...` | ↑ | ROLE_ADMIN |
| POST | `/api/admin/speaking/submissions/{id}/grade` | ↑ | ROLE_ADMIN |
| PUT | `/api/admin/speaking/submissions/{id}/grade` | ↑ | ROLE_ADMIN |

### 5.2 Public API (гость может читать контент)

**`GET /api/public/speaking/libraries`** — список опубликованных тем; темы без опубликованных топиков скрыты (`topicCount > 0`). Сортировка: `display_order ASC`.

Ответ `200 OK`:
```json
[
  {
    "id": "a1b2c3d4-0000-4000-8000-000000000001",
    "title": "Everyday Life",
    "description": "Daily routines and small talk",
    "coverUrl": "https://media.funnyenglish.app/funnyenglish/speaking/covers/3f2a....webp",
    "topicCount": 4
  }
]
```

**`GET /api/public/speaking/libraries/{id}/topics`** — опубликованные, не удалённые топики темы (тема тоже должна быть опубликована; иначе 404).

Ответ `200 OK`:
```json
[
  {
    "id": "b2c3d4e5-0000-4000-8000-000000000010",
    "title": "My Morning Routine",
    "description": "Talk about your typical morning",
    "durationSeconds": 95,
    "questionCount": 3,
    "hasSubtitles": true
  }
]
```

**`GET /api/public/speaking/topics/{id}`** — детали топика: видео + субтитры + вопросы. Доступен только опубликованный и не удалённый топик в опубликованной теме; иначе `404 {"error":"Not found","message":"Topic not found"}`.

Ответ `200 OK`:
```json
{
  "id": "b2c3d4e5-0000-4000-8000-000000000010",
  "libraryId": "a1b2c3d4-0000-4000-8000-000000000001",
  "title": "My Morning Routine",
  "description": "Talk about your typical morning",
  "video": {
    "videoUrl": "https://media.funnyenglish.app/funnyenglish/speaking/videos/9c1e....mp4",
    "subtitleUrl": "https://media.funnyenglish.app/funnyenglish/speaking/subtitles/7ab0....vtt",
    "durationSeconds": 95
  },
  "questions": [
    { "id": "c3d4e5f6-...-21", "text": "What time do you usually wake up?", "displayOrder": 0 },
    { "id": "c3d4e5f6-...-22", "text": "What do you eat for breakfast?", "displayOrder": 1 },
    { "id": "c3d4e5f6-...-23", "text": "How do you get to work or school?", "displayOrder": 2 }
  ]
}
```
`video: null` допустимо (видео не загружено) — клиент ведёт на вопросы напрямую (PRD: «к вопросам можно перейти без видео»).

### 5.3 Auth User API (practice)

**`POST /api/speaking/submissions`** — загрузка practice-записи. `Content-Type: multipart/form-data`.

Параметры формы:
| Поле | Тип | Обяз. | Описание |
|---|---|---|---|
| `file` | File | да | Аудио AAC/m4a (допустимы также mp3/wav/ogg — whitelist StorageService), ≤ 5 МБ, длительность соответствует `durationSec` |
| `topicId` | String (UUID) | да | Топик должен существовать, быть опубликован и не удалён |
| `durationSec` | Int | да | 1..60 (ожидается ~30) |

Ответ `201 Created`:
```json
{
  "id": "d4e5f6a7-...-31",
  "topicId": "b2c3d4e5-...-10",
  "topicTitle": "My Morning Routine",
  "audioUrl": "https://media.funnyenglish.app/funnyenglish/speaking/submissions/u_<userId>/e8f1....m4a",
  "durationSec": 30,
  "status": "NEW",
  "grade": null,
  "createdAt": "2026-07-30T16:40:00Z"
}
```

Ошибки: `400` (валидация файла/параметров, `ErrorResponse`), `401/403` (без JWT), `404` (топик не найден/не опубликован), `413` (nginx/spring лимит).

**`GET /api/speaking/submissions/my`** — мои отправки, новые сверху, с оценками.

Ответ `200 OK`:
```json
[
  {
    "id": "d4e5f6a7-...-31",
    "topicId": "b2c3d4e5-...-10",
    "topicTitle": "My Morning Routine",
    "audioUrl": "https://media.funnyenglish.app/.../e8f1....m4a",
    "durationSec": 30,
    "status": "REVIEWED",
    "grade": {
      "grammar": 7,
      "vocabulary": 8,
      "pronunciation": 6,
      "fluency": 7,
      "total": 7.00,
      "comment": "Good ideas, work on /th/ sounds.",
      "reviewerName": "Teacher Anna",
      "createdAt": "2026-07-30T18:00:00Z",
      "updatedAt": "2026-07-30T18:00:00Z"
    },
    "createdAt": "2026-07-30T16:40:00Z"
  }
]
```

### 5.4 Admin API (контент)

CRUD стандартный, по паттерну `AudioTestController` admin-части (`@PreAuthorize("hasRole('ADMIN')")` дополнительно к SecurityConfig-матчеру):

- `GET /api/admin/speaking/libraries` → `List<AdminLibraryResponse>` (все, включая неопубликованные).
- `POST /api/admin/speaking/libraries` ← `CreateLibraryRequest` → `201 AdminLibraryResponse`.
- `PUT /api/admin/speaking/libraries/{id}` ← `UpdateLibraryRequest` (partial update, null-поля игнорируются — паттерн `updateAudioTest`) → `200`.
- `DELETE /api/admin/speaking/libraries/{id}` → `204`. Каскадно удалит топики/видео/вопросы; **если есть submissions — БД откажет (RESTRICT)** → сервис ловит `DataIntegrityViolationException` и бросает `IllegalArgumentException("Library has submissions; archive topics instead")` → `400`.
- `GET /api/admin/speaking/topics?libraryId={uuid}` → `List<AdminTopicResponse>` (включая soft-deleted, с `isDeleted: true` — admin видит архив).
- `POST /api/admin/speaking/topics` ← `CreateTopicRequest` → `201`.
- `PUT /api/admin/speaking/topics/{id}` ← `UpdateTopicRequest` → `200`.
- `DELETE /api/admin/speaking/topics/{id}` → **soft delete**: `deleted_at = NOW()`, `204`. Идемпотентно.
- `PUT /api/admin/speaking/topics/{id}/video` ← `UpsertVideoRequest` → `200 AdminTopicResponse`. Upsert: создаёт Video или заменяет; при замене старые файлы из MinIO удаляются через `StorageService.deleteFile` (best-effort, ошибка удаления не откатывает транзакцию).
- `POST /api/admin/speaking/topics/{id}/questions` ← `CreateSpeakingQuestionRequest` → `201 SpeakingQuestionResponse`.
- `PUT /api/admin/speaking/questions/{id}` ← `CreateSpeakingQuestionRequest` → `200`.
- `DELETE /api/admin/speaking/questions/{id}` → `204`.

Загрузка самих файлов (обложки, видео, субтитры) — через существующий `POST /api/admin/media/upload?folder=speaking/videos|speaking/subtitles|speaking/covers`, возвращаемый `url` подставляется в DTO. Папки фиксируем: `speaking/covers`, `speaking/videos`, `speaking/subtitles`.

### 5.5 Admin API (grading inbox)

**`GET /api/admin/speaking/submissions`** — inbox учителя.

Query-параметры (все optional):
| Параметр | Тип | Описание |
|---|---|---|
| `status` | `NEW`/`REVIEWED` | Фильтр по статусу (дефолт: все) |
| `userId` | UUID | Фильтр по ученику |
| `topicId` | UUID | Фильтр по топику |
| `dateFrom`, `dateTo` | ISO date (`2026-07-30`) | По `created_at` (включительно) |
| `page`, `size`, `sort` | Spring Pageable | Дефолт: `page=0,size=20`, сортировка `createdAt,DESC` |

Ответ `200 OK` (Spring `Page`):
```json
{
  "content": [
    {
      "id": "d4e5f6a7-...-31",
      "userId": "u-...",
      "userEmail": "student@example.com",
      "userDisplayName": "Ivan",
      "topicId": "b2c3d4e5-...-10",
      "topicTitle": "My Morning Routine",
      "audioUrl": "https://media.funnyenglish.app/.../e8f1....m4a",
      "durationSec": 30,
      "status": "NEW",
      "grade": null,
      "createdAt": "2026-07-30T16:40:00Z"
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "totalPages": 1
}
```

**`POST /api/admin/speaking/submissions/{id}/grade`** ← `GradeSubmissionRequest` → `201 GradeResponse`. Если оценка уже есть → `400 {"error":"Bad request","message":"Submission already graded; use PUT to edit"}`. В той же транзакции статус submission → `REVIEWED`.

**`PUT /api/admin/speaking/submissions/{id}/grade`** ← `GradeSubmissionRequest` → `200 GradeResponse`. Редактирование: обновляет критерии/комментарий, `reviewer_id` — текущий админ, `updated_at` обновляется (аудит). Статус остаётся `REVIEWED`. Если оценки нет → `404`.

### 5.6 Security-правила

Изменений в `SecurityConfig.kt` **не требуется** — покрыто существующими матчерами (пути указаны относительно context-path `/api`):

| Новый путь | Существующий матчер | Эффект |
|---|---|---|
| `/public/speaking/**` | `.requestMatchers("/public/**").permitAll()` | Гость читает контент (Story 1–3) |
| `/speaking/**` | `.anyRequest().authenticated()` | Practice только авторизованным (Story 5); в контроллере дополнительно `@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")` не требуется — любой authenticated (роль USER/ADMIN) допустим |
| `/admin/speaking/**` | `.requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")` | Только админ |

В `SpeakingAdminController` добавить `@PreAuthorize("hasRole('ADMIN')")` на класс — defense-in-depth, паттерн `AudioTestController`.

⚠️ Контроллеры маппятся **без** `/api`-префикса (context-path сам его добавит): `@RequestMapping("/public/speaking")`, `@RequestMapping("/speaking")`, `@RequestMapping("/admin/speaking")`. Не повторять квирк `AudioTestController` с `/api/audio-tests` (см. 1.2).

---

## 6. Service-слой: бизнес-логика

### 6.1 Расширение `StorageService` (MODIFY)

Текущий whitelist (`StorageService.kt:24-25`): изображения + аудио (`mp3, wav, ogg, m4a, aac, flac`). Для фичи добавить:

```kotlin
private val allowedVideoExtensions = setOf("mp4", "webm", "mov", "m4v")
private val allowedSubtitleExtensions = setOf("vtt")
```

и расширить `validateFileType`: content-type `video/*` ↔ video-расширения; `text/vtt`, `text/plain`, `application/octet-stream` ↔ `vtt`. Логика cross-check «extension ↔ contentType» — та же, что для image/audio.

⚠️ Лимит размера: `StorageService` размер не проверяет; действуют `spring.servlet.multipart.max-file-size: 50MB` (application.yml) и nginx `client_max_body_size 50m` (docker/nginx.conf:10). Для practice-аудио сервис дополнительно валидирует **≤ 5 МБ** в коде (PRD: «~1–2 МБ на practice-запись») — бросать `IllegalArgumentException("Audio file too large (max 5 MB)")`.

### 6.2 `PracticeSubmissionService` — upload flow

```
POST /speaking/submissions (multipart file, topicId, durationSec)
 1. topic = topicRepository.findPublishedActiveById(topicId)
      ?: throw NoSuchElementException("Topic not found")            → 404
 2. validate: durationSec in 1..60                                   → 400
    validate: file.size <= 5 * 1024 * 1024                           → 400
    (расширение/content-type проверит StorageService.uploadFile)     → 400
 3. audioUrl = storageService.uploadFile(file, "speaking/submissions/u_${userId}")
    — key: speaking/submissions/u_<userId>/<uuid>.m4a
    — URL строится из app.s3.public-url (S3_PUBLIC_URL) — НЕ внутренний
      endpoint minio:9000 (BUG-004, memory.md №2; фикс зафиксирован 2026-07-20)
 4. submission = PracticeSubmission(user, topic, audioUrl, durationSec, NEW)
    repository.save(submission)                                       → 201 SubmissionResponse
```

Порядок важен: сначала валидация, потом upload в MinIO, потом INSERT. Если INSERT упал — файл-сирота в MinIO допустим (периодическая зачистка out of scope).

### 6.3 Grading flow (транзакция)

```kotlin
@Transactional
fun gradeSubmission(submissionId: UUID, request: GradeSubmissionRequest, reviewerId: UUID): GradeResponse {
    val submission = submissionRepository.findByIdWithDetails(submissionId)
        .orElseThrow { NoSuchElementException("Submission not found") }        // 404
    require(submission.grade == null) { "Submission already graded; use PUT to edit" } // 400

    val grade = Grade(..., reviewer = reviewer)
    submission.grade = grade
    grade.submission = submission
    submission.status = SubmissionStatus.REVIEWED      // смена статуса в той же транзакции
    submissionRepository.save(submission)
    // total вычисляется generated column; вернуть перечитанную сущность (refresh)
    return submissionRepository.saveAndFlush(submission)
        .let { gradeRepository.findBySubmissionId(it.id!!)!!.toResponse() }
}
```

- `editGrade` (PUT): ищем `gradeRepository.findBySubmissionId`, обновляем 4 критерия + comment + reviewer; статус НЕ трогаем. `@UpdateTimestamp` проставит `updated_at` (аудит PRD).
- Статус-машина: `NEW → REVIEWED`. Обратного перехода нет; удаление оценки не предусмотрено API.

### 6.4 `SpeakingContentService` — публичная выдача

- `getPublishedLibraries()`: `libraries WHERE is_published`, `topicCount` — подзапросом/JPQL по `topics WHERE is_published AND deleted_at IS NULL`; фильтровать `topicCount > 0` (пустые темы скрыты — Story 1). Можно JPQL-проекцией, **не native query** (грабля №21 memory.md: projection + Timestamp).
- `getPublishedTopics(libraryId)`: тема должна быть `is_published`, иначе `NoSuchElementException` → 404 (не палим существование).
- `getTopicDetail(id)`: `JOIN FETCH video, questions` (паттерн `findPublishedByIdWithDetails` из `AudioTestRepository`).
- Media URL уже публичные из `StorageService`; для страховки прогонять через `MediaUrlService.normalize()` на чтении — защита от legacy-записей с внутренним endpoint (BUG-004).

### 6.5 Репозитории (JPQL-паттерны)

```kotlin
@Repository
interface TopicRepository : JpaRepository<Topic, UUID> {
    @Query("""
        SELECT t FROM Topic t
        LEFT JOIN FETCH t.video
        LEFT JOIN FETCH t.questions
        WHERE t.id = :id AND t.isPublished = true AND t.deletedAt IS NULL
        AND t.library.isPublished = true
    """)
    fun findPublishedActiveByIdWithDetails(@Param("id") id: UUID): Optional<Topic>

    fun findByIdAndIsPublishedTrueAndDeletedAtIsNull(id: UUID): Optional<Topic>
}

@Repository
interface PracticeSubmissionRepository : JpaRepository<PracticeSubmission, UUID> {
    @Query("""
        SELECT s FROM PracticeSubmission s
        LEFT JOIN FETCH s.user
        LEFT JOIN FETCH s.topic
        LEFT JOIN FETCH s.grade
        WHERE (:status IS NULL OR s.status = :status)
        AND (:userId IS NULL OR s.user.id = :userId)
        AND (:topicId IS NULL OR s.topic.id = :topicId)
        AND (CAST(:dateFrom AS timestamp) IS NULL OR s.createdAt >= :dateFrom)
        AND (CAST(:dateTo AS timestamp) IS NULL OR s.createdAt < :dateTo)
        ORDER BY s.createdAt DESC
    """)
    fun search(..., pageable: Pageable): Page<PracticeSubmission>

    @Query("SELECT s FROM PracticeSubmission s LEFT JOIN FETCH s.grade g LEFT JOIN FETCH g.reviewer LEFT JOIN FETCH s.topic WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    fun findByUserIdWithGrade(@Param("userId") userId: UUID): List<PracticeSubmission>
}
```

⚠️ `dateTo` передавать как «следующий день 00:00» из контроллера/сервиса (включительная дата). Если JPQL с nullable timestamp будет капризничать — fallback на `JpaSpecificationExecutor` (допустимо, но JPQL предпочтительнее для консистентности с проектом).

---

## 7. Нефункциональные требования

### 7.1 Лимиты загрузки

| Слой | Текущее | Требование фичи | Действие |
|---|---|---|---|
| nginx `docker/nginx.conf:10` | `client_max_body_size 50m` | Видео топиков может быть 50–200 МБ | **Увеличить до `200m`** в nginx.conf (и в prod Caddy — проверить лимиты; Caddy по умолчанию не лимитит тело) |
| Spring `application.yml` | `max-file-size: 50MB`, `max-request-size: 50MB` | Соответственно | Поднять до `200MB` (или env `MULTIPART_MAX_FILE_SIZE`) |
| Код (`PracticeSubmissionService`) | — | Аудио ≤ 5 МБ, duration ≤ 60с | Валидация в сервисе (6.2) |
| Код (`StorageService`) | whitelist image/audio | + video (mp4/webm/mov/m4v), + vtt | См. 6.1 |

Рекомендация по видео (задокументировать в admin-web Part 2): сжимать до ≤ 100 МБ / 720p перед загрузкой; жёсткий предел — 200 МБ инфраструктурой.

### 7.2 Rate limiting

- Существующий `security/RateLimitingFilter.kt` лимитит логин/регистрацию (грабля №23 — лимиты через env).
- Новых лимитов на MVP не вводим, но фиксируем риски: `POST /speaking/submissions` — тяжёлый (upload в MinIO); при росте добавить bucket-лимит (например, 10/час/пользователь) в тот же фильтр. Public GET-эндпоинты — read-only, кандидаты на HTTP-кэш (`Cache-Control: public, max-age=60`) — отдельная задача, не блокер.

### 7.3 Валидация файлов

- По расширению + content-type cross-check (уже в `StorageService`).
- Для `.vtt`: дополнительно проверять первые байты `WEBVTT` (magic string) в `UpsertVideoRequest`-flow — дёшево, отсекает переименованные файлы. Реализация: при сохранении видео скачивать из MinIO не нужно — admin-web валидирует на клиенте; backend-проверка content-type достаточна для MVP.
- Аудио practice: только whitelist StorageService; Android-клиент пишет AAC/m4a (MediaRecorder) — он в whitelist уже есть.

### 7.4 Безопасность

- JWT — существующий; practice/submissions и grading разделены ролями (5.6).
- URL аудио submissions публично читаемые из MinIO (как всё медиа). Ключи содержат UUID и `u_<userId>` — неугадуемые; presigned URL НЕ вводим в MVP (принятый риск: запись доступна по ссылке).
- Stacktrace из 500 не утекает (`GlobalExceptionHandler`, memory.md решение 2026-07-20) — новые контроллеры используют те же типы исключений.

### 7.5 Производительность

- Индексы под inbox-запросы созданы в V18 (status+created_at, user_id, topic_id).
- Публичные списки — `JOIN FETCH` без N+1 (паттерн AudioTestRepository).
- `Page` для inbox обязателен (Spring Pageable), лимит `size ≤ 100`.

---

## 8. Требования к тестированию

Паттерны — существующие: unit на mockk (`UserServiceTest`), интеграция `@SpringBootTest + @AutoConfigureMockMvc + @ActiveProfiles("test")` (`AudioTestIntegrationTest.kt`, `UserControllerIntegrationTest.kt`). Тестовый профиль использует H2/Testcontainers — **проверить совместимость generated column `total` с тестовой БД**; если H2 — проверить поддержку `GENERATED ALWAYS AS ... STORED` (H2 поддерживает generated columns; при проблемах — в test profile считать total в маппере).

### 8.1 Unit-тесты (`backend/src/test/kotlin/com/funnyenglish/service/speaking/PracticeSubmissionServiceTest.kt`)

1. `createSubmission` — успех: файл валиден → save вызван, статус NEW, URL из StorageService.
2. `createSubmission` — топик не опубликован/удалён → `NoSuchElementException`.
3. `createSubmission` — `durationSec = 0 / 61` → `IllegalArgumentException`.
4. `createSubmission` — файл > 5 МБ → `IllegalArgumentException`.
5. `gradeSubmission` — успех: grade создан, статус NEW→REVIEWED, reviewer проставлен.
6. `gradeSubmission` — повторный POST на оценённый → `IllegalArgumentException`.
7. `editGrade` — успех: поля обновлены, статус остался REVIEWED.
8. `editGrade` — оценки нет → `NoSuchElementException`.

### 8.2 Unit-тесты `SpeakingContentServiceTest.kt`

1. Пустые темы (topicCount=0) отфильтрованы из публичной выдачи.
2. Soft-deleted топик не возвращается публичным API.
3. `upsertVideo` — создание и замена (старый URL удаляется через `StorageService.deleteFile`).

### 8.3 Интеграционный тест (`controller/SpeakingFlowIntegrationTest.kt`)

По образцу `AudioTestIntegrationTest`: токены через `jwtService.generateToken(adminId, ..., "ADMIN"/"USER")`, seed-пользователи в `@BeforeEach`.

1. Гость (без токена): `GET /api/public/speaking/libraries` → 200; неопубликованная тема отсутствует.
2. Гость: `POST /api/speaking/submissions` → 403.
3. USER: multipart `POST /api/speaking/submissions` (MockMultipartFile `file`, params `topicId`, `durationSec`) → 201, `status == "NEW"`, `audioUrl` содержит public-url.
4. USER: `GET /api/speaking/submissions/my` → содержит созданную запись.
5. USER: `GET /api/admin/speaking/submissions` → 403.
6. ADMIN: `POST .../submissions/{id}/grade` → 201; затем `GET /api/speaking/submissions/my` от USER → `status == "REVIEWED"`, grade с `total == среднее`.
7. ADMIN: повторный POST grade → 400; PUT grade → 200, `updatedAt` изменился.
8. ADMIN: CRUD topics: create → publish → public GET видит; DELETE → public GET 404, admin GET видит `isDeleted: true`.
9. **Контрактный тест сериализации (грабля №18):** в ответе admin-эндпоинта поле называется `"isPublished"`, а не `"published"` — `jsonPath("$.isPublished").exists()`.

---

## 9. Разбивка на задачи

Каждая задача → отдельный bd issue (`bd create`). Оценки в идеальных днях AI-агента.

| # | Задача | Файлы | Оценка | Зависимости |
|---|---|---|---|---|
| 1 | Flyway V17: контентные таблицы (libraries, topics, videos, speaking_questions) | `V17__create_speaking_content_tables.sql` | 0.5 | — |
| 2 | Flyway V18: submissions + grades (generated column total) | `V18__create_speaking_submissions_tables.sql` | 0.5 | 1 |
| 3 | JPA-сущности `entity/speaking/*` + репозитории `repository/speaking/*` (JPQL join-fetch) | 6 entity + 6 repository файлов | 1.0 | 1, 2 |
| 4 | DTO `dto/SpeakingDtos.kt` + мапперы | `SpeakingDtos.kt` | 0.5 | 3 |
| 5 | Расширить `StorageService` (video/vtt whitelist) + лимиты nginx/spring 200MB | `StorageService.kt`, `application.yml`, `docker/nginx.conf` | 0.5 | — |
| 6 | `SpeakingContentService` + `SpeakingPublicController` (3 public GET) | service/speaking + controller/speaking | 1.0 | 3, 4 |
| 7 | Admin CRUD контента (`SpeakingAdminController`, часть ContentService): libraries/topics/video/questions, soft delete | controller + service | 1.5 | 6 |
| 8 | `PracticeSubmissionService` + `SpeakingSubmissionController`: multipart upload + my-submissions | controller + service | 1.0 | 5, 6 |
| 9 | Grading: inbox с фильтрами/пагинацией + POST/PUT grade + статус-машина | service + controller (admin) | 1.5 | 8 |
| 10 | Unit-тесты сервисов (mockk) | `service/speaking/*Test.kt` | 1.0 | 8, 9 |
| 11 | Интеграционный тест SpeakingFlowIntegrationTest (11 сценариев из 8.3) | `controller/SpeakingFlowIntegrationTest.kt` | 1.0 | 9 |
| 12 | shared: методы `FunnyEnglishApi` (public content, multipart submit через `submitFormWithBinaryData`, my submissions) + модели | `shared/.../api/FunnyEnglishApi.kt`, `shared/.../model/*` | 1.0 | 6, 8 |
| 13 | Регрессия: прогон существующих backend-тестов (34 шт.) + обновление `api-tests/funnyenglish-api-collection.json` новыми эндпоинтами | — | 0.5 | 11 |

**Итого backend+shared:** ~10.5 идеальных дней. Из них критический путь: 1→3→6→8→9→11.

**Замечания для исполнителя:**
- Контроллеры маппятся БЕЗ `/api` (context-path), см. 1.2 — самая вероятная ошибка.
- `isPublished`-контракт проверять jsonPath-тестом (задача 11, сценарий 9).
- После реализации — дополнить `memory.md` (раздел «Решения и договорённости») решением про generated column `total` и soft delete topics.
- Admin-web (Speaking Content + Grading UI) и KMP-клиент (Library→…→MySubmissions экраны, VoiceRecorder expect/actual, WebVTT-парсинг) — Part 2 и Part 3 спецификации.

---

*Part 2: Admin-web (Speaking Content, Grading inbox) — отдельный документ.*

---

## Changelog

- **v1.1 (2026-07-31, patch):** `grades.grammar/vocabulary/pronunciation/fluency`: SMALLINT → INTEGER. Причина: Hibernate маппит Kotlin `Int` на `INTEGER`; со SMALLINT `ddl-auto: validate` падал («wrong column type: found int2, expecting integer»). Поймано на первом применении V18.
*Part 3: KMP-клиент (composeApp, VoiceRecorder, Media3 video, Training/Practice flows) — отдельный документ.*
