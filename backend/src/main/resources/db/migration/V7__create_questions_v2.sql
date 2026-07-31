-- Migration: Update questions table with JSONB content
-- Date: 2026-02-02

-- Question types enum
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'question_type') THEN
        CREATE TYPE question_type AS ENUM (
            'TEXT_SELECT',
            'IMAGE_SELECT', 
            'AUDIO_SELECT',
            'DRAG_DROP_MATCH',
            'DRAG_DROP_SORT',
            'FILL_BLANK'
        );
    END IF;
END
$$;

-- Add new columns to existing questions table if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'question_type') THEN
        ALTER TABLE questions ADD COLUMN question_type question_type;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'content') THEN
        ALTER TABLE questions ADD COLUMN content JSONB;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'media_url') THEN
        ALTER TABLE questions ADD COLUMN media_url VARCHAR(500);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'order_index') THEN
        ALTER TABLE questions ADD COLUMN order_index INTEGER NOT NULL DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'time_limit_seconds') THEN
        ALTER TABLE questions ADD COLUMN time_limit_seconds INTEGER;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'explanation') THEN
        ALTER TABLE questions ADD COLUMN explanation TEXT;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'hint') THEN
        ALTER TABLE questions ADD COLUMN hint VARCHAR(500);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'is_published') THEN
        ALTER TABLE questions ADD COLUMN is_published BOOLEAN NOT NULL DEFAULT false;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'created_at') THEN
        ALTER TABLE questions ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'updated_at') THEN
        ALTER TABLE questions ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'lesson_id') THEN
        ALTER TABLE questions ADD COLUMN lesson_id UUID REFERENCES lessons(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'questions' AND column_name = 'title') THEN
        ALTER TABLE questions ADD COLUMN title VARCHAR(500) NOT NULL DEFAULT '';
    END IF;
END
$$;

-- Update existing data: set question_type based on old type column
UPDATE questions SET question_type = 'TEXT_SELECT'::question_type WHERE type = 'TEXT_SELECT';
UPDATE questions SET question_type = 'IMAGE_SELECT'::question_type WHERE type = 'IMAGE_SELECT';
UPDATE questions SET question_type = 'AUDIO_SELECT'::question_type WHERE type = 'AUDIO_SELECT';
UPDATE questions SET question_type = 'FILL_BLANK'::question_type WHERE type = 'FILL_BLANK';
UPDATE questions SET question_type = 'DRAG_DROP_MATCH'::question_type WHERE type = 'DRAG_DROP_IMAGE';

-- Set default for rows that didn't match
UPDATE questions SET question_type = 'TEXT_SELECT'::question_type WHERE question_type IS NULL;

-- Migrate old answers to JSONB content
UPDATE questions q
SET content = jsonb_build_object(
    'text', COALESCE(q.text, ''),
    'answers', COALESCE((
        SELECT jsonb_agg(jsonb_build_object(
            'id', a.id::text,
            'text', COALESCE(a.text, ''),
            'imageUrl', a.image_url,
            'isCorrect', a.is_correct
        ) ORDER BY a.display_order)
        FROM answers a
        WHERE a.question_id = q.id
    ), '[]'::jsonb)
)
WHERE content IS NULL;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_questions_test ON questions(test_id);
CREATE INDEX IF NOT EXISTS idx_questions_lesson ON questions(lesson_id);
CREATE INDEX IF NOT EXISTS idx_questions_type ON questions(question_type);
CREATE INDEX IF NOT EXISTS idx_questions_order ON questions(order_index);
CREATE INDEX IF NOT EXISTS idx_questions_published ON questions(is_published);

-- GIN index for JSONB content search (PostgreSQL specific)
CREATE INDEX IF NOT EXISTS idx_questions_content ON questions USING gin (content);

-- Enable pg_trgm extension for text search in JSONB
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Index for searching text within JSON
CREATE INDEX IF NOT EXISTS idx_questions_content_text ON questions 
    USING gin ((content->>'text') gin_trgm_ops);

-- Media files table
CREATE TABLE IF NOT EXISTS media_files (
    id UUID PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL, -- IMAGE, AUDIO, VIDEO
    content_type VARCHAR(100),
    size_bytes BIGINT DEFAULT 0,
    width INTEGER,
    height INTEGER,
    duration_seconds INTEGER,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    folder VARCHAR(100),
    is_archived BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_media_files_user ON media_files(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_media_files_type ON media_files(type);
CREATE INDEX IF NOT EXISTS idx_media_files_folder ON media_files(folder);

-- Media library for quick reuse
CREATE TABLE IF NOT EXISTS media_library_items (
    id UUID PRIMARY KEY,
    media_id UUID NOT NULL REFERENCES media_files(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tag VARCHAR(100),
    usage_count INTEGER NOT NULL DEFAULT 0,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(media_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_media_library_user ON media_library_items(user_id);
CREATE INDEX IF NOT EXISTS idx_media_library_tag ON media_library_items(tag);
