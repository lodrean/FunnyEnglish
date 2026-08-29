-- V25__unique_practice_submission_user_topic.sql
-- SEC: UNIQUE (user_id, topic_id) на practice_submissions — одна Practice-отправка на топик.
-- Закрывает race двух параллельных POST, обходящий 409-гейт (PROJECT-REVIEW-2026-08-28 §2.1 Важно).
-- IF NOT EXISTS через pg_constraint (PostgreSQL не поддерживает ADD CONSTRAINT IF NOT EXISTS).

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_practice_submissions_user_topic'
    ) THEN
        ALTER TABLE practice_submissions
            ADD CONSTRAINT uq_practice_submissions_user_topic UNIQUE (user_id, topic_id);
    END IF;
END $$;

-- Если миграция падает на существующих дублях — сначала зачистить вручную:
--   DELETE FROM practice_submissions a USING practice_submissions b
--   WHERE a.user_id = b.user_id AND a.topic_id = b.topic_id AND a.created_at > b.created_at;

COMMENT ON CONSTRAINT uq_practice_submissions_user_topic ON practice_submissions
    IS 'Одна Practice-отправка ученика на топик; fallback для 409-гейта DUPLICATE_SUBMISSION';
