-- Migration: Create lessons tables
-- Date: 2026-02-02

-- Lessons table
CREATE TABLE IF NOT EXISTS lessons (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES categories(id),
    icon_url VARCHAR(500),
    emoji VARCHAR(10),
    duration_minutes INTEGER DEFAULT 5,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Learning paths table
CREATE TABLE IF NOT EXISTS learning_paths (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Path lessons (junction table for path-lesson relationship with order)
CREATE TABLE IF NOT EXISTS path_lessons (
    id UUID PRIMARY KEY,
    path_id UUID NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_required BOOLEAN NOT NULL DEFAULT true,
    UNIQUE(path_id, lesson_id)
);

-- User path progress
CREATE TABLE IF NOT EXISTS user_path_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    path_id UUID NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
    current_lesson_id UUID REFERENCES lessons(id),
    completed_lessons INTEGER NOT NULL DEFAULT 0,
    total_lessons INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    last_activity_at TIMESTAMP,
    UNIQUE(user_id, path_id)
);

-- Completed lessons
CREATE TABLE IF NOT EXISTS completed_lessons (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    xp_earned INTEGER DEFAULT 0,
    time_spent_seconds INTEGER DEFAULT 0,
    UNIQUE(user_id, lesson_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_lessons_category ON lessons(category_id);
CREATE INDEX IF NOT EXISTS idx_lessons_published ON lessons(is_published);
CREATE INDEX IF NOT EXISTS idx_lessons_order ON lessons(display_order);

CREATE INDEX IF NOT EXISTS idx_path_lessons_path ON path_lessons(path_id);
CREATE INDEX IF NOT EXISTS idx_path_lessons_lesson ON path_lessons(lesson_id);
CREATE INDEX IF NOT EXISTS idx_path_lessons_order ON path_lessons(display_order);

CREATE INDEX IF NOT EXISTS idx_user_path_progress_user ON user_path_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_user_path_progress_path ON user_path_progress(path_id);
CREATE INDEX IF NOT EXISTS idx_user_path_progress_current ON user_path_progress(current_lesson_id);

CREATE INDEX IF NOT EXISTS idx_completed_lessons_user ON completed_lessons(user_id);
CREATE INDEX IF NOT EXISTS idx_completed_lessons_lesson ON completed_lessons(lesson_id);
CREATE INDEX IF NOT EXISTS idx_completed_lessons_date ON completed_lessons(completed_at);
