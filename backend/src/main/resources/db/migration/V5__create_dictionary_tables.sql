-- Migration: Create dictionary tables
-- Date: 2026-02-02

-- Word difficulty enum
CREATE TYPE word_difficulty AS ENUM ('EASY', 'MEDIUM', 'HARD');

-- Part of speech enum
CREATE TYPE part_of_speech AS ENUM (
    'NOUN', 'VERB', 'ADJECTIVE', 'ADVERB', 
    'PRONOUN', 'PREPOSITION', 'CONJUNCTION', 'INTERJECTION'
);

-- User word status enum
CREATE TYPE user_word_status AS ENUM ('NEW', 'LEARNING', 'LEARNED', 'HARD');

-- Words table
CREATE TABLE IF NOT EXISTS words (
    id UUID PRIMARY KEY,
    word VARCHAR(100) NOT NULL,
    transcription VARCHAR(200),
    translation VARCHAR(200) NOT NULL,
    part_of_speech part_of_speech,
    audio_url VARCHAR(500),
    example_sentence TEXT,
    difficulty word_difficulty DEFAULT 'MEDIUM',
    category VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User words table (user's vocabulary with progress)
CREATE TABLE IF NOT EXISTS user_words (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    word_id UUID NOT NULL REFERENCES words(id) ON DELETE CASCADE,
    progress INTEGER NOT NULL DEFAULT 0 CHECK (progress >= 0 AND progress <= 100),
    status user_word_status NOT NULL DEFAULT 'NEW',
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_reviewed_at TIMESTAMP,
    review_count INTEGER NOT NULL DEFAULT 0,
    UNIQUE(user_id, word_id)
);

-- Indexes for words
CREATE INDEX IF NOT EXISTS idx_words_word ON words(word);
CREATE INDEX IF NOT EXISTS idx_words_word_lower ON words(LOWER(word));
CREATE INDEX IF NOT EXISTS idx_words_difficulty ON words(difficulty);
CREATE INDEX IF NOT EXISTS idx_words_category ON words(category);
CREATE INDEX IF NOT EXISTS idx_words_part_of_speech ON words(part_of_speech);

-- Full text search index (PostgreSQL specific)
-- Note: This requires the pg_trgm extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_words_word_trgm ON words USING gin (word gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_words_translation_trgm ON words USING gin (translation gin_trgm_ops);

-- Indexes for user_words
CREATE INDEX IF NOT EXISTS idx_user_words_user ON user_words(user_id);
CREATE INDEX IF NOT EXISTS idx_user_words_word ON user_words(word_id);
CREATE INDEX IF NOT EXISTS idx_user_words_status ON user_words(user_id, status);
CREATE INDEX IF NOT EXISTS idx_user_words_progress ON user_words(user_id, progress);

-- Sample words for testing
INSERT INTO words (id, word, transcription, translation, part_of_speech, difficulty, category, example_sentence)
VALUES 
    (gen_random_uuid(), 'apple', '/ˈæp.əl/', 'яблоко', 'NOUN', 'EASY', 'Food', 'I eat an apple every day.'),
    (gen_random_uuid(), 'book', '/bʊk/', 'книга', 'NOUN', 'EASY', 'Education', 'This is an interesting book.'),
    (gen_random_uuid(), 'run', '/rʌn/', 'бежать', 'VERB', 'EASY', 'Actions', 'I run every morning.'),
    (gen_random_uuid(), 'beautiful', '/ˈbjuː.t̬ə.fəl/', 'красивый', 'ADJECTIVE', 'MEDIUM', 'Description', 'She has a beautiful smile.'),
    (gen_random_uuid(), 'serendipity', '/ˌser.ənˈdɪp.ə.ti/', 'счастливая случайность', 'NOUN', 'HARD', 'Abstract', 'Finding this restaurant was pure serendipity.')
ON CONFLICT DO NOTHING;
