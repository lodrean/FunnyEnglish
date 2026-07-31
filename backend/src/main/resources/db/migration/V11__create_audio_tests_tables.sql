-- V11__create_audio_tests_tables.sql
-- Audio Tests System: Tables for audio-based listening comprehension tests

-- Enable UUID extension if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Main audio tests table
CREATE TABLE audio_tests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    audio_file_url VARCHAR(500) NOT NULL,
    duration_seconds INTEGER NOT NULL,
    difficulty INTEGER NOT NULL CHECK (difficulty BETWEEN 1 AND 5),
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    is_published BOOLEAN NOT NULL DEFAULT false,
    plays_limit INTEGER, -- NULL means unlimited
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Audio test questions with timing
CREATE TABLE audio_test_questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    audio_test_id UUID NOT NULL REFERENCES audio_tests(id) ON DELETE CASCADE,
    question_type VARCHAR(50) NOT NULL CHECK (question_type IN ('LISTENING_COMPREHENSION', 'FILL_BLANK', 'TRUE_FALSE', 'DICTATION')),
    title VARCHAR(500),
    text TEXT,
    start_time_seconds INTEGER NOT NULL,
    end_time_seconds INTEGER NOT NULL,
    points INTEGER NOT NULL DEFAULT 1,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT valid_time_range CHECK (start_time_seconds < end_time_seconds),
    CONSTRAINT non_negative_time CHECK (start_time_seconds >= 0)
);

-- Answers for audio test questions
CREATE TABLE audio_test_answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_id UUID NOT NULL REFERENCES audio_test_questions(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER NOT NULL DEFAULT 0,
    match_target VARCHAR(255) -- For drag-drop matching if needed
);

-- Audio transcripts (for reference/study)
CREATE TABLE audio_transcripts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    audio_test_id UUID NOT NULL REFERENCES audio_tests(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    is_generated BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    UNIQUE(audio_test_id, language)
);

-- User progress on audio tests
CREATE TABLE audio_test_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    audio_test_id UUID NOT NULL REFERENCES audio_tests(id) ON DELETE CASCADE,
    score INTEGER NOT NULL DEFAULT 0,
    max_score INTEGER NOT NULL DEFAULT 0,
    stars INTEGER NOT NULL DEFAULT 0,
    attempts_count INTEGER NOT NULL DEFAULT 0,
    best_score INTEGER NOT NULL DEFAULT 0,
    time_spent_seconds INTEGER,
    plays_used INTEGER NOT NULL DEFAULT 0,
    completed_at TIMESTAMP WITH TIME ZONE,
    last_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    UNIQUE(user_id, audio_test_id)
);

-- User answers for audio test attempts
CREATE TABLE audio_test_user_answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    progress_id UUID NOT NULL REFERENCES audio_test_progress(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES audio_test_questions(id) ON DELETE CASCADE,
    selected_answer_ids UUID[] NOT NULL DEFAULT '{}',
    text_answer TEXT, -- For dictation/fill blank
    is_correct BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_audio_tests_category ON audio_tests(category_id);
CREATE INDEX idx_audio_tests_published ON audio_tests(is_published);
CREATE INDEX idx_audio_tests_difficulty ON audio_tests(difficulty);
CREATE INDEX idx_audio_questions_test ON audio_test_questions(audio_test_id);
CREATE INDEX idx_audio_questions_time ON audio_test_questions(start_time_seconds, end_time_seconds);
CREATE INDEX idx_audio_answers_question ON audio_test_answers(question_id);
CREATE INDEX idx_audio_transcripts_test ON audio_transcripts(audio_test_id);
CREATE INDEX idx_audio_progress_user ON audio_test_progress(user_id);
CREATE INDEX idx_audio_progress_test ON audio_test_progress(audio_test_id);
CREATE INDEX idx_audio_user_answers_progress ON audio_test_user_answers(progress_id);

-- Comments
COMMENT ON TABLE audio_tests IS 'Audio-based listening comprehension tests';
COMMENT ON TABLE audio_test_questions IS 'Questions associated with specific time ranges in audio';
COMMENT ON TABLE audio_test_answers IS 'Possible answers for audio test questions';
COMMENT ON TABLE audio_transcripts IS 'Text transcripts of audio files';
COMMENT ON TABLE audio_test_progress IS 'User progress tracking for audio tests';
COMMENT ON TABLE audio_test_user_answers IS 'Detailed user answers for each attempt';
