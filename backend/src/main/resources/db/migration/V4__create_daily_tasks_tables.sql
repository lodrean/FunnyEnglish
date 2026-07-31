-- Migration: Create daily tasks tables
-- Date: 2026-02-02

-- Daily task types enum
CREATE TYPE task_type AS ENUM (
    'COMPLETE_LESSON',
    'LEARN_WORDS', 
    'PRACTICE_MINUTES',
    'COMPLETE_TESTS',
    'STREAK_MAINTAIN'
);

-- Daily tasks table
CREATE TABLE IF NOT EXISTS daily_tasks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_type task_type NOT NULL,
    target_value INTEGER NOT NULL DEFAULT 1,
    current_value INTEGER NOT NULL DEFAULT 0,
    reward_xp INTEGER NOT NULL DEFAULT 10,
    task_date DATE NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Daily task templates (for generating tasks)
CREATE TABLE IF NOT EXISTS daily_task_templates (
    id UUID PRIMARY KEY,
    task_type task_type NOT NULL,
    min_target INTEGER DEFAULT 1,
    max_target INTEGER DEFAULT 5,
    base_reward_xp INTEGER NOT NULL DEFAULT 10,
    is_active BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 0,
    description VARCHAR(500)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_daily_tasks_user ON daily_tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_date ON daily_tasks(task_date);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_user_date ON daily_tasks(user_id, task_date);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_completed ON daily_tasks(user_id, task_date, is_completed);

CREATE INDEX IF NOT EXISTS idx_task_templates_active ON daily_task_templates(is_active, priority);

-- Insert default task templates
INSERT INTO daily_task_templates (id, task_type, min_target, max_target, base_reward_xp, is_active, priority, description)
VALUES 
    (gen_random_uuid(), 'COMPLETE_LESSON', 1, 3, 20, true, 1, 'Пройти урок'),
    (gen_random_uuid(), 'LEARN_WORDS', 5, 15, 15, true, 2, 'Выучить новые слова'),
    (gen_random_uuid(), 'PRACTICE_MINUTES', 5, 15, 10, true, 3, 'Практиковаться'),
    (gen_random_uuid(), 'COMPLETE_TESTS', 1, 2, 25, true, 4, 'Пройти тест')
ON CONFLICT DO NOTHING;
