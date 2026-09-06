-- V27__resubmit_after_reviewed.sql
-- bd FunnyEnglish-h3l.2 (решение владельца 2026-09-06, DECISIONS_BATCH #1):
-- повторная Practice-отправка разрешена после REVIEWED («получил оценку → попробовал лучше»).
-- Жёсткий UNIQUE (user_id, topic_id) из V25 заменяется частичным индексом:
-- не более одной ОЖИДАЮЩЕЙ (status = 'NEW') записи на пару (user_id, topic_id) —
-- race двух параллельных POST по-прежнему закрыт (fallback 409 DUPLICATE_SUBMISSION).

ALTER TABLE practice_submissions
    DROP CONSTRAINT IF EXISTS uq_practice_submissions_user_topic;

DROP INDEX IF EXISTS uq_practice_submissions_user_topic_pending;

CREATE UNIQUE INDEX uq_practice_submissions_user_topic_pending
    ON practice_submissions (user_id, topic_id)
    WHERE status = 'NEW';

COMMENT ON INDEX uq_practice_submissions_user_topic_pending
    IS 'Не более одной ожидающей (NEW) Practice-записи на (user_id, topic_id); после REVIEWED повторная разрешена (bd h3l.2)';
