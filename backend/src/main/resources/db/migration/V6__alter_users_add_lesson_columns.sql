-- Migration: Add lesson-related columns to users table
-- Date: 2026-02-02

-- Add current lesson reference
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS current_lesson_id UUID REFERENCES lessons(id);

-- Add last activity date (separate from updated_at)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS last_activity_date DATE;

-- Add index for current lesson
CREATE INDEX IF NOT EXISTS idx_users_current_lesson ON users(current_lesson_id);

-- Update existing users to have current date
UPDATE users SET last_activity_date = CURRENT_DATE WHERE last_activity_date IS NULL;
