-- Migration: Create tables for IMAGE_WORD_MATCH questions
-- Separate tables because JSONB content is temporarily disabled

-- Main table for IMAGE_WORD_MATCH question data
CREATE TABLE image_word_match_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL UNIQUE,
    test_id UUID NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    instruction VARCHAR(500) NOT NULL,
    points INTEGER NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_iw_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_iw_test FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);

-- Words for IMAGE_WORD_MATCH questions
CREATE TABLE image_word_match_words (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL,
    word_id VARCHAR(50) NOT NULL,  -- Client-generated ID
    text VARCHAR(100) NOT NULL,
    translation VARCHAR(100),
    audio_url VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_iw_word_question FOREIGN KEY (question_id) REFERENCES image_word_match_questions(question_id) ON DELETE CASCADE
);

-- Hotspots (areas) for IMAGE_WORD_MATCH questions
CREATE TABLE image_word_match_hotspots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL,
    hotspot_id VARCHAR(50) NOT NULL,  -- Client-generated ID
    x FLOAT NOT NULL,
    y FLOAT NOT NULL,
    width FLOAT NOT NULL,
    height FLOAT NOT NULL,
    shape VARCHAR(20) NOT NULL DEFAULT 'RECTANGLE',
    word_id VARCHAR(50) NOT NULL,  -- Reference to image_word_match_words.word_id
    
    CONSTRAINT fk_iw_hotspot_question FOREIGN KEY (question_id) REFERENCES image_word_match_questions(question_id) ON DELETE CASCADE,
    CONSTRAINT chk_x CHECK (x >= 0.0 AND x <= 1.0),
    CONSTRAINT chk_y CHECK (y >= 0.0 AND y <= 1.0),
    CONSTRAINT chk_width CHECK (width >= 0.0 AND width <= 1.0),
    CONSTRAINT chk_height CHECK (height >= 0.0 AND height <= 1.0)
);

-- Indexes for performance
CREATE INDEX idx_iw_question_test ON image_word_match_questions(test_id);
CREATE INDEX idx_iw_word_question ON image_word_match_words(question_id);
CREATE INDEX idx_iw_hotspot_question ON image_word_match_hotspots(question_id);

-- Comments
COMMENT ON TABLE image_word_match_questions IS 'Stores IMAGE_WORD_MATCH question specific data';
COMMENT ON TABLE image_word_match_words IS 'Words for IMAGE_WORD_MATCH questions';
COMMENT ON TABLE image_word_match_hotspots IS 'Hotspots (areas) on images for IMAGE_WORD_MATCH questions';
