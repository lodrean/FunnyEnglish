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
