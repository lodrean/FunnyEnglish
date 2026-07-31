-- Add gamification columns to users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS longest_streak INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS previous_streak_before_break INTEGER;

-- Update existing users
UPDATE users SET longest_streak = current_streak WHERE longest_streak = 0;
