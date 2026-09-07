-- Аудит-лог изменений оценок (bd FunnyEnglish-h3l.10, PROJECT_AUDIT F-5).
-- IF NOT EXISTS — дисциплина грабли №62 (ddl может отставать от Hibernate в dev).
CREATE TABLE IF NOT EXISTS grade_audit (
    id            UUID PRIMARY KEY,
    submission_id UUID         NOT NULL,
    reviewer_id   UUID         NOT NULL,
    action        VARCHAR(16)  NOT NULL,
    grammar       INTEGER      NOT NULL,
    vocabulary    INTEGER      NOT NULL,
    pronunciation INTEGER      NOT NULL,
    fluency       INTEGER      NOT NULL,
    comment       TEXT,
    created_at    TIMESTAMP    NOT NULL,
    FOREIGN KEY (submission_id) REFERENCES practice_submissions (id),
    FOREIGN KEY (reviewer_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_grade_audit_submission
    ON grade_audit (submission_id, created_at DESC);
