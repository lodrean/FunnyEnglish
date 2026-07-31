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
